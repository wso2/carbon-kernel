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

package org.apache.ws.security.message;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSDocInfoStore;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.message.token.X509Security;
import org.apache.ws.security.saml.SAMLUtil;
import org.apache.ws.security.transform.STRTransform;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.keyvalues.DSAKeyValue;
import org.apache.xml.security.keys.content.keyvalues.RSAKeyValue;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import org.apache.xml.security.keys.content.x509.XMLX509SubjectName;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.InclusiveNamespaces;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Creates a Signature according to WS Specification, X509 profile.
 * 
 * This class is a re-factored implementation of the previous WSS4J class
 * <code>WSSignEnvelope</code>. This new class allows better control of
 * the process to create a Signature and to add it to the Security header.
 * 
 * The flexibility and fine granular control is required to implement a handler
 * that uses WSSecurityPolicy files to control the setup of a Security header.
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 * @author Werner Dittmann (werner@apache.org)
 */
public class WSSecSignature extends WSSecBase {

    private static Log log = LogFactory.getLog(WSSecSignature.class.getName());

    protected boolean useSingleCert = true;

    protected String sigAlgo = null;

    protected String canonAlgo = Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;

    protected WSSecUsernameToken usernameToken = null;

    protected byte[] signatureValue = null;

    /*
     * The following private variable are setup during prepare().
     */
    protected Document document = null;

    private Crypto crypto = null;

    protected WSDocInfo wsDocInfo = null;

    protected String certUri = null;

    protected XMLSignature sig = null;

    protected KeyInfo keyInfo = null;

    protected String keyInfoUri = null;

    protected SecurityTokenReference secRef = null;

    protected String strUri = null;

    private byte[] secretKey = null;
    
    private String encrKeySha1value = null;

    protected BinarySecurity bstToken = null;

    private String customTokenValueType;

    private String customTokenId;
    
    private String digestAlgo = "http://www.w3.org/2000/09/xmldsig#sha1";
    
    private X509Certificate useThisCert = null;
    /**
     * Constructor.
     */
    public WSSecSignature() {
    }

    /**
     * set the single cert flag.
     * 
     * @param useSingleCert
     */
    public void setUseSingleCertificate(boolean useSingleCert) {
        this.useSingleCert = useSingleCert;
    }

    /**
     * Get the single cert flag.
     * 
     * @return A boolean if single certificate is set.
     */
    public boolean isUseSingleCertificate() {
        return this.useSingleCert;
    }

    /**
     * Set the name of the signature encryption algorithm to use.
     * 
     * If the algorithm is not set then an automatic detection of the signature
     * algorithm to use is performed during the <code>prepare()</code>
     * method. Refer to WSConstants which algorithms are supported.
     * 
     * @param algo Is the name of the signature algorithm
     * @see WSConstants#RSA
     * @see WSConstants#DSA
     */
    public void setSignatureAlgorithm(String algo) {
        sigAlgo = algo;
    }

    /**
     * Get the name of the signature algorithm that is being used.
     * 
     * Call this method after <code>prepare</code> to get the information
     * which signature algorithm was automatically detected if no signature
     * algorithm was preset.
     * 
     * @return the identifier URI of the signature algorithm
     */
    public String getSignatureAlgorithm() {
        return sigAlgo;
    }

    /**
     * Set the canonicalization method to use.
     * 
     * If the canonicalization method is not set then the recommended Exclusive
     * XML Canonicalization is used by default Refer to WSConstants which
     * algorithms are supported.
     * 
     * @param algo Is the name of the signature algorithm
     * @see WSConstants#C14N_OMIT_COMMENTS
     * @see WSConstants#C14N_WITH_COMMENTS
     * @see WSConstants#C14N_EXCL_OMIT_COMMENTS
     * @see WSConstants#C14N_EXCL_WITH_COMMENTS
     */
    public void setSigCanonicalization(String algo) {
        canonAlgo = algo;
    }

    /**
     * Get the canonicalization method.
     * 
     * If the canonicalization method was not set then Exclusive XML
     * Canonicalization is used by default.
     * 
     * @return The string describing the canonicalization algorithm.
     */
    public String getSigCanonicalization() {
        return canonAlgo;
    }

    /**
     * @return the digestAlgo
     */
    public String getDigestAlgo() {
        return digestAlgo;
    }

    /**
     * Set the string that defines which digest algorithm to use
     * 
     * @param digestAlgo the digestAlgo to set
     */
    public void setDigestAlgo(String digestAlgo) {
        this.digestAlgo = digestAlgo;
    }
    
    
    /**
     * @param usernameToken The usernameToken to set.
     */
    public void setUsernameToken(WSSecUsernameToken usernameToken) {
        this.usernameToken = usernameToken;
    }

    /**
     * Returns the computed Signature value.
     * 
     * Call this method after <code>computeSignature()</code> or <code>build()</code>
     * methods were called.
     * 
     * @return Returns the signatureValue.
     */
    public byte[] getSignatureValue() {
        return signatureValue;
    }

    /**
     * Get the id generated during <code>prepare()</code>.
     * 
     * Returns the the value of wsu:Id attribute of the Signature element.
     * 
     * @return Return the wsu:Id of this token or null if <code>prepare()</code>
     *         was not called before.
     */
    public String getId() {
        if (sig == null) {
            return null;
        }
        return sig.getId();
    }
    
    /**
     * Get the id of the BSt generated  during <code>prepare()</code>.
     * 
     * @return Returns the the value of wsu:Id attribute of the 
     * BinaruSecurityToken element.
     */
    public String getBSTTokenId() {
        if (this.bstToken == null) {
            return null;
        }
        return this.bstToken.getID();
    }

    /**
     * Initialize a WSSec Signature.
     * 
     * The method sets up and initializes a WSSec Signature structure after the
     * relevant information was set. After setup of the references to elements
     * to sign may be added. After all references are added they can be signed.
     * 
     * This method does not add the Signature element to the security header.
     * See <code>prependSignatureElementToHeader()</code> method.
     * 
     * @param doc The SOAP envelope as <code>Document</code>
     * @param cr An instance of the Crypto API to handle keystore and certificates
     * @param secHeader The security header that will hold the Signature. This is used
     *                   to construct namespace prefixes for Signature. This method
     * @throws WSSecurityException
     */
    public void prepare(Document doc, Crypto cr, WSSecHeader secHeader)
        throws WSSecurityException {
        //
        // Gather some info about the document to process and store it for
        // retrieval
        //
        crypto = cr;
        document = doc;
        wsDocInfo = new WSDocInfo(doc);
        wsDocInfo.setCrypto(cr);

        //
        // At first get the security token (certificate) according to the
        // parameters.
        //
        X509Certificate[] certs = null;
        if (keyIdentifierType != WSConstants.UT_SIGNING
            && keyIdentifierType != WSConstants.CUSTOM_SYMM_SIGNING
            && keyIdentifierType != WSConstants.CUSTOM_SYMM_SIGNING_DIRECT
            && keyIdentifierType != WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER
            && keyIdentifierType != WSConstants.CUSTOM_KEY_IDENTIFIER) {
            if (useThisCert == null) {
                certs = crypto.getCertificates(user);
            } else {
                certs = new X509Certificate[] {useThisCert};
            }
            if (certs == null || certs.length <= 0) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "noUserCertsFound", 
                    new Object[] { user, "signature" }
                );
            }
            certUri = wssConfig.getIdAllocator().createSecureId("CertId-", certs[0]);  
            //
            // If no signature algorithm was set try to detect it according to the
            // data stored in the certificate.
            //
            if (sigAlgo == null) {
                String pubKeyAlgo = certs[0].getPublicKey().getAlgorithm();
                log.debug("automatic sig algo detection: " + pubKeyAlgo);
                if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                    sigAlgo = XMLSignature.ALGO_ID_SIGNATURE_DSA;
                } else if (pubKeyAlgo.equalsIgnoreCase("RSA")) {
                    sigAlgo = XMLSignature.ALGO_ID_SIGNATURE_RSA;
                } else {
                    throw new WSSecurityException(
                        WSSecurityException.FAILURE,
                        "unknownSignatureAlgorithm",
                        new Object[] {pubKeyAlgo}
                    );
                }
            }
        }

        //
        // Get an initialized XMLSignature element.
        //
        if (canonAlgo.equals(WSConstants.C14N_EXCL_OMIT_COMMENTS)) {
            Element canonElem = 
                XMLUtils.createElementInSignatureSpace(doc, Constants._TAG_CANONICALIZATIONMETHOD);
            canonElem.setAttributeNS(null, Constants._ATT_ALGORITHM, canonAlgo);

            if (wssConfig.isWsiBSPCompliant()) {
                Set prefixes = getInclusivePrefixes(secHeader.getSecurityHeader(), false);
                InclusiveNamespaces inclusiveNamespaces = new InclusiveNamespaces(doc, prefixes);
                canonElem.appendChild(inclusiveNamespaces.getElement());
            }

            try {
                SignatureAlgorithm signatureAlgorithm = new SignatureAlgorithm(doc, sigAlgo);
                sig = new XMLSignature(doc, null, signatureAlgorithm.getElement(), canonElem);
            } catch (XMLSecurityException e) {
                log.error("", e);
                throw new WSSecurityException(
                    WSSecurityException.FAILED_SIGNATURE, "noXMLSig", null, e
                );
            }
        } else {
            try {
                sig = new XMLSignature(doc, null, sigAlgo, canonAlgo);
            } catch (XMLSecurityException e) {
                log.error("", e);
                throw new WSSecurityException(
                    WSSecurityException.FAILED_SIGNATURE, "noXMLSig", null, e
                );
            }
        }

        EnvelopeIdResolver resolver = (EnvelopeIdResolver)EnvelopeIdResolver.getInstance();
        resolver.setWsDocInfo(wsDocInfo);
        sig.addResourceResolver(resolver);
        sig.setId(wssConfig.getIdAllocator().createId("Signature-", sig));

        keyInfo = sig.getKeyInfo();
        keyInfoUri = wssConfig.getIdAllocator().createSecureId("KeyId-", keyInfo);
        keyInfo.setId(keyInfoUri);

        secRef = new SecurityTokenReference(doc);
        strUri = wssConfig.getIdAllocator().createSecureId("STRId-", secRef);
        secRef.setID(strUri);
        
        //
        // Prepare and setup the token references for this Signature
        //
        switch (keyIdentifierType) {
        case WSConstants.BST_DIRECT_REFERENCE:
            Reference ref = new Reference(document);
            ref.setURI("#" + certUri);
            if (!useSingleCert) {
                bstToken = new PKIPathSecurity(document);
                ((PKIPathSecurity) bstToken).setX509Certificates(certs, false, crypto);
            } else {
                bstToken = new X509Security(document);
                ((X509Security) bstToken).setX509Certificate(certs[0]);
            }
            ref.setValueType(bstToken.getValueType());
            secRef.setReference(ref);
            bstToken.setID(certUri);
            wsDocInfo.setBst(bstToken.getElement());
            break;
        case WSConstants.EMBED_SECURITY_TOKEN_REF:
            XMLX509Certificate cert = null;
            XMLX509SubjectName subject = null;
            try {
                cert = new XMLX509Certificate(document, certs[0]);
            } catch (XMLSecurityException e) {
                throw new WSSecurityException(WSSecurityException.FAILURE, "");
            }
            subject = new XMLX509SubjectName(doc, certs[0]);

            X509Data x509KeyVal = new X509Data(document);
            x509KeyVal.add(subject);
            x509KeyVal.add(cert);
            keyInfo.add(x509KeyVal);
            break;
        case WSConstants.ISSUER_SERIAL:
            XMLX509IssuerSerial data = new XMLX509IssuerSerial(document, certs[0]);
            X509Data x509Data = new X509Data(document);
            x509Data.add(data);
            secRef.setX509IssuerSerial(x509Data);
            break;
        case WSConstants.X509_KEY_IDENTIFIER:
            secRef.setKeyIdentifier(certs[0]);
            break;

        case WSConstants.SKI_KEY_IDENTIFIER:
            secRef.setKeyIdentifierSKI(certs[0], crypto);
            break;

        case WSConstants.UT_SIGNING:
            Reference refUt = new Reference(document);
            refUt.setValueType(WSConstants.USERNAMETOKEN_NS + "#UsernameToken");
            String utId = usernameToken.getId();
            refUt.setURI("#" + utId);
            secRef.setReference(refUt);
            secretKey = usernameToken.getSecretKey();
            break;

        case WSConstants.THUMBPRINT_IDENTIFIER:
            secRef.setKeyIdentifierThumb(certs[0]);
            break;
            
        case WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER:
            if (encrKeySha1value != null) {
                secRef.setKeyIdentifierEncKeySHA1(encrKeySha1value);
            } else {
                secRef.setKeyIdentifierEncKeySHA1(getSHA1(secretKey));
            }
            break;

        case WSConstants.CUSTOM_SYMM_SIGNING :

			if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(customTokenValueType)
					|| WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(customTokenValueType)) {
				secRef.setKeyIdentifier(customTokenValueType, customTokenId);
			} else {
				Reference refCust = new Reference(document);
				refCust.setValueType(customTokenValueType);
				refCust.setURI((new StringBuilder()).append("#")
						.append(customTokenId).toString());
				secRef.setReference(refCust);
			}
			break;

        case WSConstants.CUSTOM_SYMM_SIGNING_DIRECT :
            Reference refCustd = new Reference(document);
            refCustd.setValueType(this.customTokenValueType);
            refCustd.setURI(this.customTokenId);
            secRef.setReference(refCustd);
            break;
        case WSConstants.CUSTOM_KEY_IDENTIFIER:
            secRef.setKeyIdentifier(customTokenValueType, customTokenId);
            break;
        case WSConstants.KEY_VALUE:
            java.security.PublicKey publicKey = certs[0].getPublicKey();
            String pubKeyAlgo = publicKey.getAlgorithm();
            if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                DSAKeyValue dsaKeyValue = new DSAKeyValue(document, publicKey);
                keyInfo.add(dsaKeyValue);
            } else if (pubKeyAlgo.equalsIgnoreCase("RSA")) {
                RSAKeyValue rsaKeyValue = new RSAKeyValue(document, publicKey);
                keyInfo.add(rsaKeyValue);
            } else {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "unknownSignatureAlgorithm",
                    new Object[] {pubKeyAlgo}
                );
            }
            break;
        default:
            throw new WSSecurityException(WSSecurityException.FAILURE, "unsupportedKeyId");
        }

        if (keyIdentifierType != WSConstants.KEY_VALUE
            && keyIdentifierType != WSConstants.EMBED_SECURITY_TOKEN_REF) {
            keyInfo.addUnknownElement(secRef.getElement());
            wsDocInfo.setSecurityTokenReference(secRef.getElement());
        }
    }

    /**
     * This method adds references to the Signature.
     * 
     * The added references are signed when calling
     * <code>computeSignature()</code>. This method can be called several
     * times to add references as required. <code>addReferencesToSign()</code>
     * can be called any time after <code>prepare</code>.
     * 
     * @param references A vector containing <code>WSEncryptionPart</code> objects
     *                   that define the parts to sign.
     * @param secHeader Used to compute namespaces to be inserted by
     *                  InclusiveNamespaces to be WSI compliant.
     * @throws WSSecurityException
     */
    public void addReferencesToSign(Vector references, WSSecHeader secHeader)
        throws WSSecurityException {
        Element envelope = document.getDocumentElement();

        for (int part = 0; part < references.size(); part++) {
            WSEncryptionPart encPart = (WSEncryptionPart) references.get(part);

            String idToSign = encPart.getId();
            String elemName = encPart.getName();
            String nmSpace = encPart.getNamespace();

            //
            // Set up the elements to sign. There are two reserved element
            // names: "Token" and "STRTransform" "Token": Setup the Signature to
            // either sign the information that points to the security token or
            // the token itself. If its a direct reference sign the token,
            // otherwise sign the KeyInfo Element. "STRTransform": Setup the
            // ds:Reference to use STR Transform
            //
            Transforms transforms = new Transforms(document);
            try {
                if (idToSign != null) {
                    Element toSignById = 
                        WSSecurityUtil.findElementById(
                            document.getDocumentElement(), idToSign, WSConstants.WSU_NS
                        );
                    if (toSignById == null) {
                        toSignById = 
                            WSSecurityUtil.findElementById(
                                document.getDocumentElement(), idToSign, null
                            );
                    }
                    if(SecurityTokenReference.SECURITY_TOKEN_REFERENCE.equals(elemName)){
                        Element ctx = createSTRParameter(document);
                        transforms.addTransform(STRTransform.implementedTransformURI, ctx);
                        sig.addDocument("#" + idToSign, transforms, digestAlgo);
                        continue;
                    }
                    transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (wssConfig.isWsiBSPCompliant()) {
                        transforms.item(0).getElement().appendChild(
                            new InclusiveNamespaces(
                                document, getInclusivePrefixes(toSignById)).getElement()
                            );
                    }
                    sig.addDocument("#" + idToSign, transforms, digestAlgo);
                } else if (elemName.equals("Token")) {
                    transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (keyIdentifierType == WSConstants.BST_DIRECT_REFERENCE) {
                        if (wssConfig.isWsiBSPCompliant()) {
                            transforms.item(0).getElement().appendChild(
                                new InclusiveNamespaces(
                                    document,
                                    getInclusivePrefixes(secHeader.getSecurityHeader())).getElement()
                                );
                        }
                        sig.addDocument("#" + certUri, transforms, digestAlgo);
                    } else {
                        if (wssConfig.isWsiBSPCompliant()) {
                            transforms.item(0).getElement().appendChild(
                                new InclusiveNamespaces(
                                    document, getInclusivePrefixes(keyInfo.getElement())).getElement()
                                );
                        }
                        sig.addDocument("#" + keyInfoUri, transforms, digestAlgo);
                    }
                } else if (elemName.equals("STRTransform")) { // STRTransform
                    Element ctx = createSTRParameter(document);
                    transforms.addTransform(STRTransform.implementedTransformURI, ctx);
                    sig.addDocument("#" + strUri, transforms, digestAlgo);
                } else if (elemName.equals("Assertion")) { // Assertion
                    String id = null;
                    id = SAMLUtil.getAssertionId(envelope, elemName, nmSpace);

                    Element body = 
                        (Element) WSSecurityUtil.findElement(envelope, elemName, nmSpace);
                    if (body == null) {
                        throw new WSSecurityException(
                            WSSecurityException.FAILURE, "noEncElement",
                            new Object[] {nmSpace + ", " + elemName}
                        );
                    }
                    transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (wssConfig.isWsiBSPCompliant()) {
                        transforms.item(0).getElement().appendChild(
                            new InclusiveNamespaces(
                                document, getInclusivePrefixes(body)).getElement()
                            );
                    }
                    String prefix = 
                        WSSecurityUtil.setNamespace(body, WSConstants.WSU_NS, WSConstants.WSU_PREFIX);
                    body.setAttributeNS(WSConstants.WSU_NS, prefix + ":Id", id);
                    sig.addDocument("#" + id, transforms, digestAlgo);
                } else {
                    Element body = 
                        (Element)WSSecurityUtil.findElement(envelope, elemName, nmSpace);
                    if (body == null) {
                        throw new WSSecurityException(
                            WSSecurityException.FAILURE, 
                            "noEncElement",
                            new Object[] {nmSpace + ", " + elemName}
                        );
                    }
                    transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (wssConfig.isWsiBSPCompliant()) {
                        transforms.item(0).getElement().appendChild(
                            new InclusiveNamespaces(
                                document, getInclusivePrefixes(body)).getElement()
                            );
                    }
                    sig.addDocument("#" + setWsuId(body), transforms, digestAlgo);
                }
            } catch (TransformationException e1) {
                throw new WSSecurityException(
                    WSSecurityException.FAILED_SIGNATURE, "noXMLSig", null, e1
                );
            } catch (XMLSignatureException e1) {
                throw new WSSecurityException(
                    WSSecurityException.FAILED_SIGNATURE, "noXMLSig", null, e1
                );
            }
        }
    }

    /**
     * Prepends the Signature element to the elements already in the Security
     * header.
     * 
     * The method can be called any time after <code>prepare()</code>.
     * This allows to insert the Signature element at any position in the
     * Security header.
     * 
     * @param secHeader The secHeader that holds the Signature element.
     */
    public void prependToHeader(WSSecHeader secHeader) {
        WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), sig.getElement());
    }
    
    /**
     * Appends the Signature element to the elements already in the Security
     * header.
     * 
     * The method can be called any time after <code>prepare()</code>.
     * This allows to insert the Signature element at any position in the
     * Security header.
     * 
     * @param secHeader The secHeader that holds the Signature element.
     */
    public void appendToHeader(WSSecHeader secHeader) {
        Element secHeaderElement = secHeader.getSecurityHeader();
        secHeaderElement.appendChild(sig.getElement());
    }
    
    /**
     * Prepend the BinarySecurityToken to the elements already in the Security
     * header.
     * 
     * The method can be called any time after <code>prepare()</code>.
     * This allows to insert the BST element at any position in the Security
     * header.
     * 
     * @param secHeader The security header that holds the BST element.
     */
    public void prependBSTElementToHeader(WSSecHeader secHeader) {
        if (bstToken != null) {
            WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), bstToken.getElement());
        }
        bstToken = null;
    }

    /**
     * Returns the SignatureElement.
     * The method can be called any time after <code>prepare()</code>.
     * @return The DOM Element of the signature.
     */
    public Element getSignatureElement() {
        return this.sig.getElement();
    }
    
    /**
     * Returns the BST Token element.
     * The method can be called any time after <code>prepare()</code>.
     * @return the BST Token element
     */
    public Element getBinarySecurityTokenElement() {
        if (this.bstToken != null) {
            return this.bstToken.getElement();
        }
        return null;
    }
    
    public void appendBSTElementToHeader(WSSecHeader secHeader) {
        if (bstToken != null) {
            Element secHeaderElement = secHeader.getSecurityHeader();
            secHeaderElement.appendChild(bstToken.getElement());
        }
        bstToken = null;
    }
    
    /**
     * Compute the Signature over the references.
     * 
     * After references are set this method computes the Signature for them.
     * This method can be called any time after the references were set. See
     * <code>addReferencesToSign()</code>.
     * 
     * @throws WSSecurityException
     */
    public void computeSignature() throws WSSecurityException {
        boolean remove = WSDocInfoStore.store(wsDocInfo);
        try {
            if (keyIdentifierType == WSConstants.UT_SIGNING ||
                    keyIdentifierType == WSConstants.CUSTOM_SYMM_SIGNING ||
                    keyIdentifierType == WSConstants.CUSTOM_SYMM_SIGNING_DIRECT ||
                    keyIdentifierType == WSConstants.CUSTOM_KEY_IDENTIFIER || 
                    keyIdentifierType == WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER) {
                if (secretKey == null) {
                    sig.sign(crypto.getPrivateKey(user, password));
                } else {
                    sig.sign(sig.createSecretKey(secretKey));                    
                }
            } else {
                sig.sign(crypto.getPrivateKey(user, password));
            }
            signatureValue = sig.getSignatureValue();
        } catch (XMLSignatureException e1) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_SIGNATURE, null, null, e1
            );
        } catch (Exception e1) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_SIGNATURE, null, null, e1
            );
        } finally {
            if (remove) {
                WSDocInfoStore.delete(wsDocInfo);
            }
        }

    }

    /**
     * Builds a signed soap envelope.
     * 
     * This is a convenience method and for backward compatibility. The method
     * creates a Signature and puts it into the Security header. It does so by
     * calling the single functions in order to perform a <i>one shot signature</i>.
     * This method is compatible with the build method of the previous version
     * with the exception of the additional WSSecHeader parameter.
     * 
     * @param doc The unsigned SOAP envelope as <code>Document</code>
     * @param cr An instance of the Crypto API to handle keystore and certificates
     * @param secHeader the security header element to hold the encrypted key element.
     * @return A signed SOAP envelope as <code>Document</code>
     * @throws WSSecurityException
     */
    public Document build(Document doc, Crypto cr, WSSecHeader secHeader)
        throws WSSecurityException {
        doDebug = log.isDebugEnabled();

        if (doDebug) {
            log.debug("Beginning signing...");
        }

        prepare(doc, cr, secHeader);
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());

        if (parts == null) {
            parts = new Vector();
            WSEncryptionPart encP = 
                new WSEncryptionPart(
                    soapConstants.getBodyQName().getLocalPart(), 
                    soapConstants.getEnvelopeURI(), 
                    "Content"
                );
            parts.add(encP);
        }

        addReferencesToSign(parts, secHeader);
        prependToHeader(secHeader);
        
        //
        // if we have a BST prepend it in front of the Signature according to
        // strict layout rules.
        //
        if (bstToken != null) {
            prependBSTElementToHeader(secHeader);
        }

        computeSignature();
        
        return doc;
    }

    protected Element createSTRParameter(Document doc) {
        Element transformParam = 
            doc.createElementNS(
                WSConstants.WSSE_NS,
                WSConstants.WSSE_PREFIX + ":TransformationParameters"
            );

        WSSecurityUtil.setNamespace(
            transformParam, WSConstants.WSSE_NS, WSConstants.WSSE_PREFIX
        );

        Element canonElem = 
            doc.createElementNS(
                WSConstants.SIG_NS,
                WSConstants.SIG_PREFIX + ":CanonicalizationMethod"
            );

        WSSecurityUtil.setNamespace(
            canonElem, WSConstants.SIG_NS, WSConstants.SIG_PREFIX
        );

        canonElem.setAttributeNS(null, "Algorithm", Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        transformParam.appendChild(canonElem);
        return transformParam;
    }

    protected Set getInclusivePrefixes(Element target) {
        return getInclusivePrefixes(target, true);
    }

    protected Set getInclusivePrefixes(Element target, boolean excludeVisible) {
        Set result = new HashSet();
        Node parent = target;
        while (!(parent.getParentNode() instanceof Document)) {
            parent = parent.getParentNode();
            NamedNodeMap attributes = parent.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (attribute.getNamespaceURI() != null
                    && attribute.getNamespaceURI().equals(
                        org.apache.ws.security.WSConstants.XMLNS_NS
                    )
                ) {
                    if (attribute.getNodeName().equals("xmlns")) {
                        result.add("#default");
                    } else {
                        result.add(attribute.getLocalName());
                    }
                }
            }
        }

        if (excludeVisible == true) {
            NamedNodeMap attributes = target.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (attribute.getNamespaceURI() != null
                    && attribute.getNamespaceURI().equals(
                        org.apache.ws.security.WSConstants.XMLNS_NS
                    )
                ) {
                    if (attribute.getNodeName().equals("xmlns")) {
                        result.remove("#default");
                    } else {
                        result.remove(attribute.getLocalName());
                    }
                }
                if (attribute.getPrefix() != null) {
                    result.remove(attribute.getPrefix());
                }
            }

            if (target.getPrefix() == null) {
                result.remove("#default");
            } else {
                result.remove(target.getPrefix());
            }
        }

        return result;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public void setCustomTokenValueType(String customTokenValueType) {
        this.customTokenValueType = customTokenValueType;
    }

    public void setCustomTokenId(String customTokenId) {
        this.customTokenId = customTokenId;
    }

    public void setEncrKeySha1value(String encrKeySha1value) {
        this.encrKeySha1value = encrKeySha1value;
    }
    public void setX509Certificate(X509Certificate cer) {
        this.useThisCert = cer;
    }
    
    private String getSHA1(byte[] input) throws WSSecurityException {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.reset();
            sha.update(input);
            byte[] data = sha.digest();
            
            return Base64.encode(data);
        } catch (NoSuchAlgorithmException e) {
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e
            );
        }
    }
    
}
