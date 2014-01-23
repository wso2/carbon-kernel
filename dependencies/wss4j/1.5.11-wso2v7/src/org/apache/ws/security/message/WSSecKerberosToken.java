package org.apache.ws.security.message;

import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSDocInfoStore;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.kerberos.KrbSession;
import org.apache.ws.security.kerberos.KrbSessionCache;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.KerberosSecurity;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.SecurityUtil;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.params.InclusiveNamespaces;
import org.apache.xml.security.utils.XMLUtils;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WSSecKerberosToken extends WSSecSignature {

	private static Log log = LogFactory.getLog(WSSecKerberosToken.class);
	public static final String KERBEROS_SERVICE_PRINCIPLE_UNKNOWN = "servicePrincipalUnknown";
	protected String tokenUri;
	protected Subject subject;
	private CredentialsCallbackHandler credHandler;
	private String servicePrincipalName = KERBEROS_SERVICE_PRINCIPLE_UNKNOWN;
	protected WSSecHeader wsSecHeader;
	private SecretKey sessionKey;
    private KrbSession krbSession;

	public KrbSession getKrbSession() {
        return krbSession;
    }

    public void setKrbSession(KrbSession krbSession) {
        this.krbSession = krbSession;
    }
    
	public SecurityTokenReference getSecurityTokenReference() {
		return secRef;
	}

	public SecretKey getSessionKey() {
		return sessionKey;
	}

	public void setBSTToken(BinarySecurity bstToken) {
		this.bstToken = bstToken;
	}

	public void setServicePrincipalName(String servicePrincipalName) {
		this.servicePrincipalName = servicePrincipalName;
	}

	/**
	 * 
	 * @param doc
	 * @param secHeader
	 * @return
	 * @throws WSSecurityException
	 */
	public Document build(Document doc, WSSecHeader secHeader) throws WSSecurityException {
		if (log.isDebugEnabled()) {
			log.debug("Beginning kerberos token processing...");
		}
		credHandler = new CredentialsCallbackHandler(user, password);
		document = doc;
		wsSecHeader = secHeader;
		prepare();
		if (bstToken != null)
			prependBSTElementToHeader(secHeader);
		return document;
	}

	/**
	 * 
	 * @return
	 * @throws LoginException
	 */
	private KerberosTicket getTicketGrantingTicket() throws LoginException {
		LoginContext loginContext = new LoginContext("Client", credHandler);
		loginContext.login();
		subject = loginContext.getSubject();
		KerberosTicket ticket = (KerberosTicket) subject
				.getPrivateCredentials(KerberosTicket.class).iterator().next();
		return ticket;
	}

	/**
	 * 
	 * @param servicePrincipalName
	 * @return
	 * @throws GSSException
	 */
	private byte[] getServiceTicketData(final String servicePrincipalName) throws GSSException {
		byte serviceTicket[] = null;
		serviceTicket = (byte[]) (byte[]) Subject.doAs(subject, new PrivilegedAction<byte[]>() {
			public byte[] run() {
				try {
					GSSManager manager = GSSManager.getInstance();
					Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
					Oid krb5PrincipalNameType = new Oid("1.2.840.113554.1.2.2.1");
					GSSName serverName = manager.createName(servicePrincipalName,
							krb5PrincipalNameType);
					final GSSContext context = manager.createContext(serverName, krb5Oid, null, 0);
					byte[] token = new byte[0];
					context.requestMutualAuth(false);
					context.requestCredDeleg(false);
					return context.initSecContext(token, 0, token.length);
				} catch (GSSException e) {
					e.printStackTrace();
					return null;
				}
			}
		});

		return serviceTicket;
	}

	/**
	 * 
	 * @param tgt
	 * @return
	 * @throws WSSecurityException
	 */
	private SecretKey getSessionKey(KerberosTicket tgt) throws WSSecurityException {
		for (Iterator creds = subject.getPrivateCredentials().iterator(); creds.hasNext();) {
			Object cred = creds.next();
			if ((cred instanceof KerberosTicket) && !cred.equals(tgt)) {
				KerberosTicket ticket = (KerberosTicket) cred;
				return ticket.getSessionKey();
			}
		}

		throw new WSSecurityException((new StringBuilder())
				.append("Could not find service ticket with server principal name ")
				.append(servicePrincipalName).toString());
	}

	private boolean receiver = false;

	public boolean isReceiver() {
		return receiver;
	}

	public void setReceiver(boolean receiver) {
		this.receiver = receiver;
	}

	private void prepare() throws WSSecurityException {

		boolean needSession = false;
		KrbSession krbSession = null;

		krbSession = KrbSessionCache.getInstance().getCurrentSession();

		if (krbSession == null) {
			// Session is null, at client end when initiating a kerberos request.
			// This is not null, at the service end when sending the response back.
			needSession = true;
		}

		secRef = new SecurityTokenReference(document);
		strUri = (new StringBuilder()).append("STRId-").append(secRef.hashCode()).toString();
		secRef.setID(strUri);
		byte tokenData[] = null;

		if (needSession) {
			try {
				KerberosTicket tgt = getTicketGrantingTicket();
				tokenData = getServiceTicketData(servicePrincipalName);
				sessionKey = getSessionKey(tgt);
				krbSession = new KrbSession(SecurityUtil.getSHA1(tokenData), sessionKey);
				krbSession.setClientPrincipalName(user);
				krbSession.setServerPrincipalName(servicePrincipalName);
				KrbSessionCache.getInstance().addSession(krbSession);
			} catch (LoginException e) {
				throw new WSSecurityException(5, "kerberosLoginFailed",
						new Object[] { e.getMessage() });
			} catch (GSSException e) {
				throw new WSSecurityException(5, "kerberosSTReqFailed", new Object[] {
						servicePrincipalName, e.getMessage() });
			} catch (Exception e) {
				throw new WSSecurityException(5, "kerberosSTReqFailed", new Object[] {
						servicePrincipalName, e.getMessage() });
			}

			if (tokenData == null) {
				throw new WSSecurityException(5, "kerberosSTReqFailed", new Object[] {
						servicePrincipalName, "Check service principal exists in KDC" });
			}

			tokenUri = (new StringBuilder()).append("KerbTokenId-").append(tokenData.hashCode())
					.toString();
		} else {
			keyIdentifierType = WSConstants.THUMBPRINT_IDENTIFIER;
		}

		wsDocInfo = new WSDocInfo(document);
		switch (keyIdentifierType) {
		case WSConstants.BST_DIRECT_REFERENCE:
			Reference ref = new Reference(document);
			ref.setURI((new StringBuilder()).append("#").append(tokenUri).toString());
			bstToken = new KerberosSecurity(document);
			((KerberosSecurity) bstToken).setKerberosToken(tokenData);
			ref.setValueType(bstToken.getValueType());
			secRef.setReference(ref);
			bstToken.setID(tokenUri);
			wsDocInfo.setBst(bstToken.getElement());
			break;

		case WSConstants.THUMBPRINT_IDENTIFIER:
			secRef.setKerberosIdentifierThumb(krbSession);
			sessionKey = krbSession.getSessionKey();
			break;

		default:
			throw new WSSecurityException(0, "unsupportedKeyId");
		}
	}

	public void signMessage() throws WSSecurityException {
		if (sigAlgo == null)
			sigAlgo = WSConstants.SIG_NS + "hmac-sha1";
		if (canonAlgo.equals(WSConstants.EX_C14N)) {
			Element canonElem = XMLUtils.createElementInSignatureSpace(document,
					"CanonicalizationMethod");
			canonElem.setAttributeNS(null, "Algorithm", canonAlgo);
			if (wssConfig.isWsiBSPCompliant()) {
				Set prefixes = getInclusivePrefixes(wsSecHeader.getSecurityHeader(), false);
				InclusiveNamespaces inclusiveNamespaces = new InclusiveNamespaces(document,
						prefixes);
				canonElem.appendChild(inclusiveNamespaces.getElement());
			}
			try {
				SignatureAlgorithm signatureAlgorithm = new SignatureAlgorithm(document, sigAlgo);
				sig = new XMLSignature(document, null, signatureAlgorithm.getElement(), canonElem);
			} catch (XMLSecurityException e) {
				log.error("", e);
				throw new WSSecurityException(9, "noXMLSig");
			}
		} else {
			try {
				sig = new XMLSignature(document, null, sigAlgo, canonAlgo);
			} catch (XMLSecurityException e) {
				log.error("", e);
				throw new WSSecurityException(9, "noXMLSig");
			}
		}
		
		sig.addResourceResolver(EnvelopeIdResolver.getInstance());
		String sigUri = (new StringBuilder()).append("Signature-").append(sig.hashCode())
				.toString();
		sig.setId(sigUri);
		keyInfo = sig.getKeyInfo();
		keyInfoUri = (new StringBuilder()).append("KeyId-").append(keyInfo.hashCode()).toString();
		keyInfo.setId(keyInfoUri);
		keyInfo.addUnknownElement(secRef.getElement());
		SOAPConstants soapConstants = WSSecurityUtil
				.getSOAPConstants(document.getDocumentElement());
		if (parts == null) {
			parts = new Vector();
			WSEncryptionPart encP = new WSEncryptionPart(soapConstants.getBodyQName()
					.getLocalPart(), soapConstants.getEnvelopeURI(), "Content");
			parts.add(encP);
		}
		addReferencesToSign(parts, wsSecHeader);
		computeSignature();
	}

	/**
	 * 
	 */
	public void computeSignature() throws WSSecurityException {
		WSDocInfoStore.store(wsDocInfo);
		try {
			sig.sign(sessionKey);
			signatureValue = sig.getSignatureValue();
		} catch (Exception e) {
			throw new WSSecurityException(9, null, null, e);
		} finally {
			WSDocInfoStore.delete(wsDocInfo);
		}
	}

	/**
	 * 
	 */
	public void prependBSTElementToHeader(WSSecHeader secHeader) {
		if (bstToken != null)
			WSSecurityUtil.prependChildElement(document, secHeader.getSecurityHeader(),
					bstToken.getElement(), false);
	}
}
