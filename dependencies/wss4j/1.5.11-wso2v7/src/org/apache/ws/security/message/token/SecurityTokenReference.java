/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ws.security.message.token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.kerberos.KrbSession;
import org.apache.ws.security.kerberos.KrbSessionCache;
import org.apache.ws.security.util.DOM2Writer;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.ws.security.util.Base64;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * Security Token Reference.
 * 
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class SecurityTokenReference {
	private static Log log = LogFactory.getLog(SecurityTokenReference.class.getName());
	public static final String SECURITY_TOKEN_REFERENCE = "SecurityTokenReference";
	public static final String KEY_NAME = "KeyName";
	public static final String SKI_URI = WSConstants.X509TOKEN_NS + "#X509SubjectKeyIdentifier";
	public static final String THUMB_URI = WSConstants.SOAPMESSAGE_NS11 + "#"
			+ WSConstants.THUMBPRINT;
	public static final String SAML_ID_URI = WSConstants.SAMLTOKEN_NS + "#"
			+ WSConstants.SAML_ASSERTION_ID;
	public static final String ENC_KEY_SHA1_URI = WSConstants.SOAPMESSAGE_NS11 + "#"
			+ WSConstants.ENC_KEY_SHA1_URI;
	protected Element element = null;
	private XMLX509IssuerSerial issuerSerial = null;
	private byte[] skiBytes = null;
	private static boolean doDebug = false;

	/**
	 * Constructor.
	 * 
	 * @param elem
	 *            TODO
	 * @throws WSSecurityException
	 */
	public SecurityTokenReference(Element elem) throws WSSecurityException {
		doDebug = log.isDebugEnabled();
		this.element = elem;
		boolean goodElement = false;
		if (SECURITY_TOKEN_REFERENCE.equals(element.getLocalName())) {
			goodElement = WSConstants.WSSE_NS.equals(element.getNamespaceURI());
			// } else if (KEY_NAME.equals(element.getLocalName())) {
			// goodElement = WSConstants.SIG_NS.equals(element.getNamespaceURI());
		}
		if (!goodElement) {
			throw new WSSecurityException(WSSecurityException.FAILURE, "badElement", null);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param doc
	 *            TODO
	 */
	public SecurityTokenReference(Document doc) {
		doDebug = log.isDebugEnabled();
		this.element = doc.createElementNS(WSConstants.WSSE_NS, "wsse:SecurityTokenReference");
		WSSecurityUtil.setNamespace(this.element, WSConstants.WSSE_NS, WSConstants.WSSE_PREFIX);
	}

	/**
	 * set the reference.
	 * 
	 * @param ref
	 */
	public void setReference(Reference ref) {
		Element elem = getFirstElement();
		if (elem != null) {
			this.element.replaceChild(ref.getElement(), elem);
		} else {
			this.element.appendChild(ref.getElement());
		}
	}

	/**
	 * Gets the Reference.
	 * 
	 * @return the <code>Reference</code> element contained in this SecurityTokenReference
	 * @throws WSSecurityException
	 */
	public Reference getReference() throws WSSecurityException {
		Element elem = getFirstElement();
		return new Reference(elem);
	}

	/**
	 * Gets the signing token element, which maybe a <code>BinarySecurityToken
	 * </code> or a SAML token.
	 * 
	 * The method gets the URI attribute of the {@link Reference} contained in the
	 * {@link SecurityTokenReference} and tries to find the referenced Element in the document.
	 * 
	 * @param doc
	 *            the document that contains the binary security token element. This could be
	 *            different from the document that contains the SecurityTokenReference (STR). See
	 *            STRTransform.derefenceBST() method
	 * @return Element containing the signing token, must be a BinarySecurityToken
	 * @throws WSSecurityException
	 *             When either no <code>Reference</code> element, or the found reference contains no
	 *             URI, or the referenced signing not found.
	 */
	public Element getTokenElement(Document doc, WSDocInfo docInfo, CallbackHandler cb)
			throws WSSecurityException {
		Reference ref = getReference();
		String uri = ref.getURI();
		if (doDebug) {
			log.debug("Token reference uri: " + uri);
		}
		if (uri == null) {
			throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "badReferenceURI");
		}

		Element tokElement = findTokenElement(doc, docInfo, cb, uri, ref.getValueType());

		if (tokElement == null) {
			throw new WSSecurityException(WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
					"noToken", new Object[] { uri });
		}
		return tokElement;
	}

	/**
	 * Gets the signing token element, which may be a <code>BinarySecurityToken
	 * </code> or a SAML token.
	 * 
	 * The method gets the value of the KeyIdentifier contained in the
	 * {@link SecurityTokenReference} and tries to find the referenced Element in the document.
	 * 
	 * @param doc
	 *            the document that contains the binary security token element. This could be
	 *            different from the document that contains the SecurityTokenReference (STR). See
	 *            STRTransform.derefenceBST() method
	 * @return Element containing the signing token
	 */
	public Element getKeyIdentifierTokenElement(Document doc, WSDocInfo docInfo, CallbackHandler cb)
			throws WSSecurityException {
		String value = getKeyIdentifierValue();
		String type = getKeyIdentifierValueType();
		if (doDebug) {
			log.debug("Token reference uri: " + value);
		}
		if (value == null) {
			throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "badReferenceURI");
		}

		Element tokElement = findTokenElement(doc, docInfo, cb, value, type);

		if (tokElement == null) {
			throw new WSSecurityException(WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
					"noToken", new Object[] { value });
		}
		return tokElement;
	}

	private Element findTokenElement(Document doc, WSDocInfo docInfo, CallbackHandler cb,
			String uri, String type) {
		Element tokElement = null;
		String id = uri;
		if (id.charAt(0) == '#') {
			id = id.substring(1);
		}
		//
		// If the type is a SAMLAssertionID then find the SAML assertion - first check
		// if it has been previously processed, else search the header for it
		//
		String assertionStr = WSConstants.WSS_SAML_NS + WSConstants.ASSERTION_LN;
		if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(type) || assertionStr.equals(type)) {
			Element sa = docInfo.getAssertion();
			if(sa == null){
                sa = (Element) WSSecurityUtil.findElement(docInfo.getDocument().getDocumentElement(),
                                                          WSConstants.ASSERTION_LN,
                                                          WSConstants.SAML_NS);
            }
            if (sa != null) {
				String saID = sa.getAttribute("AssertionID");
				if (doDebug) {
					log.debug("SAML token ID: " + saID);
				}
				if (saID.equals(id)) {
					tokElement = sa;
				}
			}
			if (tokElement == null) {
				Node assertion = WSSecurityUtil.findSAMLAssertionElementById(
						doc.getDocumentElement(), id);
				if (assertion != null) {
					tokElement = (Element) assertion;
				}
			}
		} else if (WSConstants.WSS_SAML2_KI_VALUE_TYPE
				.equals(type) || assertionStr.equals(type)) {
			Element sa = docInfo.getAssertion();
			if (sa == null) {
				sa = (Element) WSSecurityUtil.findElement(docInfo.getDocument()
						.getDocumentElement(), "Assertion",WSConstants.SAML2_NS);
			}
			if (sa != null) {
				String saID = sa.getAttribute("ID");
				if (doDebug)
					log.debug((new StringBuilder()).append("SAML token ID: ")
							.append(saID).toString());
				if (saID.equals(id))
					tokElement = sa;
			}
			if (tokElement == null) {
				Node assertion = WSSecurityUtil.findSAMLAssertionElementById(
						doc.getDocumentElement(), id);
				if (assertion != null)
					tokElement = (Element) assertion;
			}
		}

		//
		// Try to find a custom token
		//
		if (tokElement == null
				&& cb != null
				&& (WSConstants.WSC_SCT.equals(type)
						|| WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(type) 
						|| WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(type) 
						|| assertionStr.equals(type))) {
			// try to find a custom token
			WSPasswordCallback pwcb = new WSPasswordCallback(id, WSPasswordCallback.CUSTOM_TOKEN);
			try {
				cb.handle(new Callback[] { pwcb });
				Element assertionElem = pwcb.getCustomToken();
				if (assertionElem != null) {
					tokElement = (Element) doc.importNode(assertionElem, true);
				}
			} catch (Exception e) {
				log.debug(e.getMessage(), e);
				// Consume this failure
			}
		}

		//
		// Finally try to find the element by its Id
		//
		if (tokElement == null) {
			tokElement = WSSecurityUtil.getElementByWsuId(doc, uri);

			// In some scenarios id is used rather than wsu:Id
			if (tokElement == null) {
				tokElement = WSSecurityUtil.getElementByGenId(doc, uri);
			}
		}

		return tokElement;
	}

	/**
	 * Sets the KeyIdentifier Element as a X509 certificate. Takes a X509 certificate, converts its
	 * data into base 64 and inserts it into a <code>wsse:KeyIdentifier</code> element, which is
	 * placed in the <code>wsse:SecurityTokenReference</code> element.
	 * 
	 * @param cert
	 *            is the X509 certificate to be inserted as key identifier
	 */
	public void setKeyIdentifier(X509Certificate cert) throws WSSecurityException {
		Document doc = this.element.getOwnerDocument();
		byte data[] = null;
		try {
			data = cert.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new WSSecurityException(WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
					"encodeError", null, e);
		}
		Text text = doc.createTextNode(Base64.encode(data));

		createKeyIdentifier(doc, X509Security.X509_V3_TYPE, text, true);
	}

	/**
	 * Sets the KeyIdentifier Element as a X509 Subject-Key-Identifier (SKI). Takes a X509
	 * certificate, gets it SKI data, converts into base 64 and inserts it into a
	 * <code>wsse:KeyIdentifier</code> element, which is placed in the
	 * <code>wsse:SecurityTokenReference</code> element.
	 * 
	 * @param cert
	 *            is the X509 certificate to get the SKI
	 * @param crypto
	 *            is the Crypto implementation. Used to read SKI info bytes from certificate
	 */
	public void setKeyIdentifierSKI(X509Certificate cert, Crypto crypto) throws WSSecurityException {
		//
		// As per the 1.1 specification, SKI can only be used for a V3 certificate
		//
		if (cert.getVersion() != 3) {
			throw new WSSecurityException(WSSecurityException.UNSUPPORTED_SECURITY_TOKEN,
					"invalidCertForSKI", new Object[] { new Integer(cert.getVersion()) });
		}

		Document doc = this.element.getOwnerDocument();
		byte data[] = crypto.getSKIBytesFromCert(cert);

		org.w3c.dom.Text text = doc.createTextNode(Base64.encode(data));
		createKeyIdentifier(doc, SKI_URI, text, true);
	}

	/**
	 * Sets the KeyIdentifier Element as a Thumbprint.
	 * 
	 * Takes a X509 certificate, computes its thumbprint using SHA-1, converts into base 64 and
	 * inserts it into a <code>wsse:KeyIdentifier</code> element, which is placed in the
	 * <code>wsse:SecurityTokenReference</code> element.
	 * 
	 * @param cert
	 *            is the X509 certificate to get the thumbprint
	 */
	public void setKeyIdentifierThumb(X509Certificate cert) throws WSSecurityException {
		Document doc = this.element.getOwnerDocument();
		MessageDigest sha = null;
		try {
			sha = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			throw new WSSecurityException(WSSecurityException.FAILURE, "noSHA1availabe", null, e1);
		}
		sha.reset();
		try {
			sha.update(cert.getEncoded());
		} catch (CertificateEncodingException e1) {
			throw new WSSecurityException(WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
					"encodeError", null, e1);
		}
		byte[] data = sha.digest();

		org.w3c.dom.Text text = doc.createTextNode(Base64.encode(data));
		createKeyIdentifier(doc, THUMB_URI, text, true);
	}

	public void setKeyIdentifierEncKeySHA1(String value) throws WSSecurityException {
		Document doc = this.element.getOwnerDocument();
		org.w3c.dom.Text text = doc.createTextNode(value);
		createKeyIdentifier(doc, ENC_KEY_SHA1_URI, text, true);
	}

	public void setSAMLKeyIdentifier(String keyIdVal) throws WSSecurityException {
		Document doc = this.element.getOwnerDocument();
		createKeyIdentifier(doc, SAML_ID_URI, doc.createTextNode(keyIdVal), false);
	}

	public void setKeyIdentifier(String valueType, String keyIdVal) throws WSSecurityException {
		Document doc = this.element.getOwnerDocument();
		createKeyIdentifier(doc, valueType, doc.createTextNode(keyIdVal), false);
	}

	private void createKeyIdentifier(Document doc, String uri, Node node, boolean base64) {
		Element keyId = doc.createElementNS(WSConstants.WSSE_NS, "wsse:KeyIdentifier");
		keyId.setAttributeNS(null, "ValueType", uri);
		if (base64) {
			keyId.setAttributeNS(null, "EncodingType", BinarySecurity.BASE64_ENCODING);
		}

		keyId.appendChild(node);
		Element elem = getFirstElement();
		if (elem != null) {
			this.element.replaceChild(keyId, elem);
		} else {
			this.element.appendChild(keyId);
		}
	}

	/**
	 * get the first child element.
	 * 
	 * @return the first <code>Element</code> child node
	 */
	public Element getFirstElement() {
		for (Node currentChild = this.element.getFirstChild(); currentChild != null; currentChild = currentChild
				.getNextSibling()) {
			if (currentChild instanceof Element) {
				return (Element) currentChild;
			}
		}
		return null;
	}

	/**
	 * Gets the KeyIdentifier.
	 * 
	 * @return the the X509 certificate or zero if a unknown key identifier type was detected.
	 */
	public X509Certificate[] getKeyIdentifier(Crypto crypto) throws WSSecurityException {
		Element elem = getFirstElement();
		String value = elem.getAttribute("ValueType");
		String alias = null;

		if (X509Security.X509_V3_TYPE.equals(value)) {
			X509Security token = new X509Security(elem);
			if (token != null) {
				X509Certificate cert = token.getX509Certificate(crypto);
				X509Certificate[] certs = new X509Certificate[1];
				certs[0] = cert;
				return certs;
			}
		} else if (SKI_URI.equals(value)) {
			alias = getX509SKIAlias(crypto);
		} else if (THUMB_URI.equals(value)) {
			Node node = getFirstElement().getFirstChild();
			if (node == null) {
				return null;
			}
			if (node.getNodeType() == Node.TEXT_NODE) {
				byte[] thumb = Base64.decode(((Text) node).getData());
				alias = crypto.getAliasForX509CertThumb(thumb);
			}
		}

		if (alias != null) {
			return crypto.getCertificates(alias);
		}
		return null;
	}

	public String getKeyIdentifierValue() {
		if (containsKeyIdentifier()) {
			Node node = getFirstElement().getFirstChild();
			if (node == null) {
				return null;
			}
			if (node.getNodeType() == Node.TEXT_NODE) {
				return ((Text) node).getData();
			}
		}
		return null;
	}

	public String getKeyIdentifierValueType() {
		if (containsKeyIdentifier()) {
			Element elem = getFirstElement();
			return elem.getAttribute("ValueType");
		}
		return null;
	}

	public String getX509SKIAlias(Crypto crypto) throws WSSecurityException {
		if (skiBytes == null) {
			skiBytes = getSKIBytes();
			if (skiBytes == null) {
				return null;
			}
		}
		String alias = crypto.getAliasForX509Cert(skiBytes);
		if (doDebug) {
			log.info("X509 SKI alias: " + alias);
		}
		return alias;
	}

	public byte[] getSKIBytes() {
		if (skiBytes != null) {
			return skiBytes;
		}
		Node node = getFirstElement().getFirstChild();
		if (node == null) {
			return null;
		}
		if (node.getNodeType() == Node.TEXT_NODE) {
			try {
				skiBytes = Base64.decode(((Text) node).getData());
			} catch (WSSecurityException e) {
				return null;
			}
		}
		return skiBytes;
	}

	/**
	 * Sets the X509 IssuerSerial data.
	 * 
	 * @param ref
	 *            the {@link XMLX509IssuerSerial} to put into this SecurityTokenReference
	 */
	public void setX509IssuerSerial(X509Data ref) {
		Element elem = getFirstElement();
		if (elem != null) {
			this.element.replaceChild(ref.getElement(), elem);
		} else {
			this.element.appendChild(ref.getElement());
		}
	}

	/**
	 * Gets the certificate identified with X509 issuerSerial data. This method first tries to get
	 * the embedded certificate. If this fails it checks if the certificate is in the keystore.
	 * 
	 * @return a certificate array or null if nothing found
	 */
	public X509Certificate[] getX509IssuerSerial(Crypto crypto) throws WSSecurityException {
		String alias = getX509IssuerSerialAlias(crypto);
		if (alias != null) {
			return crypto.getCertificates(alias);
		}
		return null;
	}

	/**
	 * Gets the alias name of the certificate identified with X509 issuerSerial data. The keystore
	 * identifies the certificate and the key with this alias name.
	 * 
	 * @return the alias name for the certificate or null if nothing found
	 */
	public String getX509IssuerSerialAlias(Crypto crypto) throws WSSecurityException {
		if (issuerSerial == null) {
			issuerSerial = getIssuerSerial();
			if (issuerSerial == null) {
				return null;
			}
		}

		String alias = crypto.getAliasForX509Cert(issuerSerial.getIssuerName(),
				issuerSerial.getSerialNumber());
		if (doDebug) {
			log.info("X509IssuerSerial alias: " + alias);
		}
		return alias;
	}

	private XMLX509IssuerSerial getIssuerSerial() throws WSSecurityException {
		if (issuerSerial != null) {
			return issuerSerial;
		}
		Element elem = getFirstElement();
		if (elem == null) {
			return null;
		}
		try {
			if (Constants._TAG_X509DATA.equals(elem.getLocalName())) {
				elem = (Element) WSSecurityUtil.findElement(elem, Constants._TAG_X509ISSUERSERIAL,
						Constants.SignatureSpecNS);
			}
			issuerSerial = new XMLX509IssuerSerial(elem, "");
		} catch (XMLSecurityException e) {
			throw new WSSecurityException(WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
					"noToken", new Object[] { "Issuer/Serial data element missing" }, e);
		}
		return issuerSerial;
	}

	/**
	 * Method containsReference
	 * 
	 * @return true if the <code>SecurtityTokenReference</code> contains a
	 *         <code>wsse:Reference</code> element
	 */
	public boolean containsReference() {
		return this.lengthReference() > 0;
	}

	/**
	 * Method lengthReference.
	 * 
	 * @return number of <code>wsse:Reference</code> elements in the
	 *         <code>SecurtityTokenReference</code>
	 */
	public int lengthReference() {
		return this.length(WSConstants.WSSE_NS, "Reference");
	}

	/**
	 * Method containsX509IssuerSerial
	 * 
	 * @return true if the <code>SecurtityTokenReference</code> contains a
	 *         <code>ds:IssuerSerial</code> element
	 */
	public boolean containsX509IssuerSerial() {
		return this.lengthX509IssuerSerial() > 0;
	}

	/**
	 * Method containsX509Data
	 * 
	 * @return true if the <code>SecurtityTokenReference</code> contains a <code>ds:X509Data</code>
	 *         element
	 */
	public boolean containsX509Data() {
		return this.lengthX509Data() > 0;
	}

	/**
	 * Method lengthX509IssuerSerial.
	 * 
	 * @return number of <code>ds:IssuerSerial</code> elements in the
	 *         <code>SecurtityTokenReference</code>
	 */
	public int lengthX509IssuerSerial() {
		return this.length(WSConstants.SIG_NS, Constants._TAG_X509ISSUERSERIAL);
	}

	/**
	 * Method lengthX509Data.
	 * 
	 * @return number of <code>ds:IssuerSerial</code> elements in the
	 *         <code>SecurtityTokenReference</code>
	 */
	public int lengthX509Data() {
		return this.length(WSConstants.SIG_NS, Constants._TAG_X509DATA);
	}

	/**
	 * Method containsKeyIdentifier.
	 * 
	 * @return true if the <code>SecurtityTokenReference</code> contains a
	 *         <code>wsse:KeyIdentifier</code> element
	 */
	public boolean containsKeyIdentifier() {
		return this.lengthKeyIdentifier() > 0;
	}

	/**
	 * Method lengthKeyIdentifier.
	 * 
	 * @return number of <code>wsse:KeyIdentifier</code> elements in the
	 *         <code>SecurtityTokenReference</code>
	 */
	public int lengthKeyIdentifier() {
		return this.length(WSConstants.WSSE_NS, "KeyIdentifier");
	}

	/**
	 * Method length.
	 * 
	 * @param namespace
	 * @param localname
	 * @return number of elements with matching localname and namespace
	 */
	public int length(String namespace, String localname) {
		NodeList childNodes = this.element.getChildNodes();
		int result = 0;
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node n = childNodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				String ns = n.getNamespaceURI();
				String name = n.getLocalName();
				if ((((namespace != null) && namespace.equals(ns)) || ((namespace == null) && (ns == null)))
						&& (localname.equals(name))) {
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * get the dom element.
	 * 
	 * @return TODO
	 */
	public Element getElement() {
		return this.element;
	}

	/**
	 * set the id.
	 * 
	 * @param id
	 */
	public void setID(String id) {
		String prefix = WSSecurityUtil.setNamespace(this.element, WSConstants.WSU_NS,
				WSConstants.WSU_PREFIX);
		this.element.setAttributeNS(WSConstants.WSU_NS, prefix + ":Id", id);
	}

	/**
	 * return the string representation.
	 * 
	 * @return TODO
	 */
	public String toString() {
		return DOM2Writer.nodeToString((Node) this.element);
	}

	/**
	 * 
	 * @param session
	 */
	public void setKerberosIdentifierThumb(KrbSession session) {
		Document doc = element.getOwnerDocument();
		Text node = doc.createTextNode(session.getThumbPrintEncoded());
		createKeyIdentifier(doc, KerberosSecurity.GSS_KERBEROSv5_AP_REQ_SHA1, node, false);
	}
	
	public boolean containsKerberosThumbprint() {
		NodeList childNodes = element.getChildNodes();
		int maxLength = childNodes.getLength();
		for (int i = 0; i < maxLength; i++) {
			Node n = childNodes.item(i);
			if (n.getNodeType() != 1)
				continue;
			Node valueType = n.getAttributes().getNamedItem("ValueType");
			if (valueType != null
					&& valueType
							.getNodeValue()
							.equals("http://docs.oasis-open.org/wss/oasis-wss-kerberos-token-profile-1.1#Kerberosv5APREQSHA1"))
				return true;
		}

		return false;
	}

	public KrbSession getKerberosSession() {
		KrbSession result = null;
		Element elem = (Element) WSSecurityUtil
				.findElement(
						element,
						"KeyIdentifier",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");

		String thumbPrint = elem.toString();
		thumbPrint = thumbPrint.substring(thumbPrint.indexOf(">") + 1);
		thumbPrint = thumbPrint.substring(0, thumbPrint.indexOf("<"));
		result = KrbSessionCache.getInstance().getSession(thumbPrint);
		return result;
	}
}
