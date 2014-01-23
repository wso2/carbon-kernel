package org.apache.ws.security.processor;

import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.KerberosTokenPrincipal;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSDocInfoStore;
import org.apache.ws.security.WSParameterCallback;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.kerberos.KrbSession;
import org.apache.ws.security.kerberos.KrbSessionCache;
import org.apache.ws.security.kerberos.KrbTicketDecoder;
import org.apache.ws.security.message.CredentialsCallbackHandler;
import org.apache.ws.security.message.EnvelopeIdResolver;
import org.apache.ws.security.message.WSSecKerberosToken;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.KerberosSecurity;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.w3c.dom.Element;

public class KerberosTokenProcessor implements Processor {

	private static Log log = LogFactory.getLog(KerberosTokenProcessor.class);
	private String tokenId;;
	private Subject subject;
	private KerberosTokenPrincipal lastPrincipalFound;

	/**
	 * 
	 * @param returnResults
	 */
	public KerberosTokenProcessor(Vector returnResults) {
		for (int j = 0; j < returnResults.size(); j++) {
			WSSecurityEngineResult wser = (WSSecurityEngineResult) returnResults
					.get(j);
			final Integer actInt = (Integer) wser
					.get(WSSecurityEngineResult.TAG_ACTION);
			if (WSConstants.KERBEROS_ENCR == actInt.intValue()) {
				lastPrincipalFound = (KerberosTokenPrincipal) wser
						.get(WSSecurityEngineResult.TAG_PRINCIPAL);
				break;
			}
			if (WSConstants.KERBEROS == actInt.intValue()) {
				lastPrincipalFound = (KerberosTokenPrincipal) wser
						.get(WSSecurityEngineResult.TAG_PRINCIPAL);
				break;
			}
			if (WSConstants.KERBEROS_SIGN == actInt.intValue()) {
				lastPrincipalFound = (KerberosTokenPrincipal) wser
						.get(WSSecurityEngineResult.TAG_PRINCIPAL);
				break;
			}
		}
	}

	/**
	 * 
	 */
	public void handleToken(Element elem, Crypto crypto, Crypto decCrypto,
			CallbackHandler handler, WSDocInfo wsDocInfo, Vector returnResults,
			WSSConfig wsc) throws WSSecurityException {
		X509Certificate returnCert[];
		Set returnElements;
		Set protectedElements;
		byte signatureValue[][];
		KerberosTokenPrincipal lastPrincipalFound;

		WSDocInfoStore.store(wsDocInfo);
		returnCert = new X509Certificate[1];
		returnElements = new HashSet();
		protectedElements = new TreeSet();
		signatureValue = new byte[1][];
		lastPrincipalFound = null;

		tokenId = elem.getAttributeNS(WSConstants.WSU_NS, "Id");

		boolean remove = WSDocInfoStore.store(wsDocInfo);

		for (int j = 0; j < returnResults.size(); j++) {
			WSSecurityEngineResult wser = (WSSecurityEngineResult) returnResults
					.get(j);
			final Integer actInt = (Integer) wser
					.get(WSSecurityEngineResult.TAG_ACTION);
			if (WSConstants.KERBEROS_ENCR == actInt.intValue()) {
				lastPrincipalFound = (KerberosTokenPrincipal) wser
						.getPrincipal();
				break;
			}
			if (WSConstants.KERBEROS == actInt.intValue()) {
				lastPrincipalFound = (KerberosTokenPrincipal) wser
						.getPrincipal();
				break;
			}
			if (WSConstants.KERBEROS_SIGN == actInt.intValue()) {
				lastPrincipalFound = (KerberosTokenPrincipal) wser
						.getPrincipal();
				break;
			}
		}

		try {
			if (lastPrincipalFound == null) {
				lastPrincipalFound = validateToken(elem, crypto, returnCert,
						returnElements, protectedElements, signatureValue,
						handler);
			}
			this.lastPrincipalFound = lastPrincipalFound;
		} finally {
			if (remove) {
				WSDocInfoStore.delete(wsDocInfo);
			}
		}

		returnResults.add(0, new WSSecurityEngineResult(WSConstants.KERBEROS,
				lastPrincipalFound, null, returnElements, protectedElements,
				null));
	}

	/**
	 * 
	 * @param elem
	 * @param crypto
	 * @param returnCert
	 * @param returnElements
	 * @param protectedElements
	 * @param signatureValue
	 * @param handler
	 * @return
	 * @throws WSSecurityException
	 */
	protected KerberosTokenPrincipal validateToken(Element elem, Crypto crypto,
			X509Certificate returnCert[], Set returnElements,
			Set protectedElements, byte signatureValue[][],
			CallbackHandler handler) throws WSSecurityException {

		SecretKey secretKey = null;
		KerberosTokenPrincipal principal = null;

		try {
			authenticate(handler);
			KerberosSecurity ks = createSecurityToken(elem);
			GSSContext context = acceptSecurityContext(ks);
			secretKey = getSessionKey(ks.getToken());

			if (log.isDebugEnabled()) {
				log.debug((new StringBuilder())
						.append("security context accepted with ")
						.append(context.getSrcName().toString())
						.append(",")
						.append(context.getSrcName().getStringNameType()
								.toString()).toString());
			}

			principal = new KerberosTokenPrincipal(context.getSrcName()
					.toString());
			principal.setTokenElement(elem);

			if (secretKey != null) {
				principal.setSessionKey(secretKey.getEncoded());
			} else {
				log.error("null secret key");
				throw new WSSecurityException(3, "nullSecretKey",
						new Object[] { "null secret key" });
			}

			principal.setSecretKey(secretKey);

			KrbSession kerberosSession = new KrbSession(ks.getSHA1(), secretKey);
			kerberosSession.setClientPrincipalName(context.getSrcName()
					.toString());
			kerberosSession.setServerPrincipalName(context.getTargName()
					.toString());
			KrbSessionCache.getInstance().addSession(kerberosSession);

			principal.setClientPrincipalName(kerberosSession
					.getClientPrincipalName());
			principal.setServicePrincipalName(kerberosSession
					.getServerPrincipalName());

		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			throw new WSSecurityException(3, "kerberosAcceptCtxFailed",
					new Object[] { e.getMessage() });
		} catch (LoginException e) {
			log.error(e.getMessage(), e);
			throw new WSSecurityException(3, "kerberosLoginFailed",
					new Object[] { e.getMessage() });
		} catch (GSSException e) {
			log.error(e.getMessage(), e);
			throw new WSSecurityException(3, "kerberosSTCreateFailed",
					new Object[] { e.getMessage() });
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WSSecurityException(3, "kerberosSTCreateFailed",
					new Object[] { e.getMessage() });
		}

		return principal;
	}

	/**
	 * 
	 * @param elem
	 * @param crypto
	 * @param returnCert
	 * @param returnElements
	 * @param protectedElements
	 * @param signatureValue
	 * @param cb
	 * @return
	 * @throws WSSecurityException
	 */
	protected Principal verifyXMLSignature(Element elem, Crypto crypto,
			X509Certificate returnCert[], Set returnElements,
			List protectedElements, byte signatureValue[][], CallbackHandler cb)
			throws WSSecurityException {

		if (log.isDebugEnabled()) {
			log.debug("KerberosTokenProcessor.verifyXMLSignature invoked");
		}

		XMLSignature sig = null;

		try {
			sig = new XMLSignature(elem, null);
		} catch (XMLSecurityException e) {
			log.error("Fail to build the XMLSignature");
			throw new WSSecurityException(6, "noXMLSig");
		}

		sig.addResourceResolver(EnvelopeIdResolver.getInstance());
		KeyInfo info = sig.getKeyInfo();
		SecretKey secretKey = null;

		KerberosTokenPrincipal principal = null;

		if (info != null) {
			org.w3c.dom.Node node = WSSecurityUtil.getDirectChild(
					info.getElement(), "SecurityTokenReference",
					WSConstants.WSSE_NS);

			if (node == null) {
				throw new WSSecurityException(3, "unsupportedKeyInfo");
			}

			SecurityTokenReference secRef = new SecurityTokenReference(
					(Element) node);
			WSDocInfo wsDocInfo = WSDocInfoStore
					.lookup(elem.getOwnerDocument());

			if (secRef.containsReference()) {
				Element token = secRef.getTokenElement(elem.getOwnerDocument(),
						wsDocInfo, cb);
				QName el = new QName(token.getNamespaceURI(),
						token.getLocalName());
				if (el.equals(WSSecurityEngine.binaryToken))
					try {
						if (lastPrincipalFound == null) {
							authenticate(cb);
							KerberosSecurity ks = createSecurityToken(token);
							GSSContext context = acceptSecurityContext(ks);
							secretKey = getSessionKey(ks.getToken());
							if (log.isDebugEnabled()) {
								log.debug("security context accepted with "
										+ context.getSrcName().toString()
										+ ","
										+ context.getSrcName()
												.getStringNameType());
							}
							principal = new KerberosTokenPrincipal(context
									.getSrcName().toString());
							principal.setTokenElement(token);
							principal.setSessionKey(secretKey.getEncoded());
							principal.setSecretKey(secretKey);
							KrbSession kerberosSession = new KrbSession(
									ks.getSHA1(), secretKey);
							kerberosSession.setClientPrincipalName(context
									.getSrcName().toString());
							kerberosSession.setServerPrincipalName(context
									.getTargName().toString());
							KrbSessionCache.getInstance().addSession(
									kerberosSession);
							principal.setClientPrincipalName(kerberosSession
									.getClientPrincipalName());
							principal.setServicePrincipalName(kerberosSession
									.getServerPrincipalName());
						} else {
							secretKey = lastPrincipalFound.getSecretKey();
						}
					} catch (RuntimeException e) {
						log.error(e.getMessage(), e);
						throw new WSSecurityException(3,
								"kerberosAcceptCtxFailed",
								new Object[] { e.getMessage() });
					} catch (LoginException e) {
						log.error(e.getMessage(), e);
						throw new WSSecurityException(3, "kerberosLoginFailed",
								new Object[] { e.getMessage() });
					} catch (GSSException e) {
						log.error(e.getMessage(), e);
						throw new WSSecurityException(3,
								"kerberosSTCreateFailed",
								new Object[] { e.getMessage() });
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						throw new WSSecurityException(3,
								"kerberosSTCreateFailed",
								new Object[] { e.getMessage() });
					}
				else
					throw new WSSecurityException(4, "unsupportedKeyInfo",
							new Object[] { el.getNamespaceURI() });
			} else {
				throw new WSSecurityException(3, "unsupportedKeyInfo",
						new Object[] { node.toString() });
			}
		} else {
			throw new WSSecurityException(3, "unsupportedKeyInfo");
		}

		if (secretKey == null)
			throw new WSSecurityException(6);
		try {
			boolean signatureOk = false;
			signatureOk = sig.checkSignatureValue(secretKey);

			if (signatureOk) {
				signatureValue[0] = sig.getSignatureValue();
				SignedInfo si = sig.getSignedInfo();
				int numReferences = si.getLength();
				for (int i = 0; i < numReferences; i++) {
					Reference siRef;
					try {
						siRef = si.item(i);
					} catch (XMLSecurityException e3) {
						throw new WSSecurityException(6);
					}
					String uri = siRef.getURI();
					if (uri != null && !"".equals(uri)) {
						Element se = WSSecurityUtil.getElementByWsuId(
								elem.getOwnerDocument(), uri);
						if (se == null)
							se = WSSecurityUtil.getElementByGenId(
									elem.getOwnerDocument(), uri);
						if (se == null)
							throw new WSSecurityException(6);
						returnElements.add(WSSecurityUtil
								.getIDfromReference(uri));
					} else {
						returnElements.add(siRef);
					}
				}

				return principal;
			}
		} catch (XMLSignatureException e1) {
			throw new WSSecurityException(6);
		}
		throw new WSSecurityException(6);
	}

	/**
	 * 
	 * @param handler
	 * @throws LoginException
	 */
	private void authenticate(CallbackHandler handler) throws LoginException {

		WSPasswordCallback[] cb = { new WSPasswordCallback(
				WSSecKerberosToken.KERBEROS_SERVICE_PRINCIPLE_UNKNOWN,
				WSPasswordCallback.KERBEROS_TOKEN) };
		String password = null;
		WSParameterCallback[] para = { new WSParameterCallback(
				WSParameterCallback.SERVICE_PRINCIPLE_PASSWORD) };

		try {
			handler.handle(cb);
			if (cb[0].getPassword() != null && !"".equals(cb[0].getPassword())) {
				password = cb[0].getPassword();
			} else {
				handler.handle(para);
				password = para[0].getStringValue();
			}

			if (password == null) {
				// If there's no password then throw an exception
				throw new LoginException("noPasswordForUser");
			}

		} catch (IOException e) {
			throw new LoginException("errorInGettingPasswordForUser");
		} catch (UnsupportedCallbackException e) {
			throw new LoginException("errorInGettingPasswordForUser");
		}

		LoginContext loginContext = new LoginContext("Server",
				new CredentialsCallbackHandler(password));
		loginContext.login();
		subject = loginContext.getSubject();
	}

	/**
	 * 
	 * @param element
	 * @return
	 * @throws WSSecurityException
	 */
	private KerberosSecurity createSecurityToken(Element element)
			throws WSSecurityException {
		BinarySecurity token = new BinarySecurity(element);
		String type = token.getValueType();
		KerberosSecurity krbTkn = null;
		if (KerberosSecurity.GSS_KERBEROSv5_AP_REQ.equals(type)) {
			krbTkn = new KerberosSecurity(element);
			return krbTkn;
		} else {
			throw new WSSecurityException(1, "unsupportedBinaryTokenType",
					new Object[] { type });
		}
	}

	GSSContext gssContext = null;

	/**
	 * 
	 * @param ks
	 * @return
	 * @throws GSSException
	 */
	private GSSContext acceptSecurityContext(final KerberosSecurity ks)
			throws GSSException {

		Subject.doAs(subject, new PrivilegedAction<GSSContext>() {
			public GSSContext run() {
				try {
					GSSManager gssManager = null;
					gssManager = GSSManager.getInstance();
					gssContext = gssManager.createContext((GSSCredential) null);
					byte[] token = ks.getToken();
					gssContext.acceptSecContext(token, 0, token.length);
					return gssContext;
				} catch (GSSException e) {
					e.printStackTrace();
					return null;
				}
			}
		});
		return gssContext;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	protected SecretKey getSessionKey(byte[] serviceTicket) throws Exception {
		KrbTicketDecoder decoder = new KrbTicketDecoder(serviceTicket, subject);
		return decoder.getSessionKey();
	}

	/**
	 * 
	 */
	public String getId() {
		return tokenId;
	}

	/**
	 * 
	 * @return
	 */
	public KerberosTokenPrincipal getLastPrincipalFound() {
		return lastPrincipalFound;
	}
}