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
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
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

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Signs a SOAP envelope according to WS Specification, X509 profile, and adds
 * the signature data.
 *
 * @author Davanum Srinivas (dims@yahoo.com)
 * @author Werner Dittmann (Werner.Dittman@siemens.com)
 */
public class WSSignEnvelope extends WSBaseMessage {

    private static Log log = LogFactory.getLog(WSSignEnvelope.class.getName());

    private static Log tlog = LogFactory.getLog("org.apache.ws.security.TIME");

    protected boolean useSingleCert = true;

    protected String sigAlgo = null;

    protected String canonAlgo = Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;

    protected WSSAddUsernameToken usernameToken = null;

    protected byte[] signatureValue = null;

    /**
     * Constructor.
     * @deprecated replaced by {@link WSSecSignature#WSSecSignature()}
     */
    public WSSignEnvelope() {
    }

    /**
     * Constructor.
     *
     * @param actor The actor name of the <code>wsse:Security</code> header
     * 
     * @deprecated replaced by {@link WSSecSignature#WSSecSignature()}
     *             and {@link WSSecHeader} for actor specification.
     */
    public WSSignEnvelope(String actor) {
        super(actor);
    }

    /**
     * Constructor.
     *
     * @param actor The actor name of the <code>wsse:Security</code> header
     * @param mu    Set <code>mustUnderstand</code> to true or false
     * 
     * @deprecated replaced by {@link WSSecSignature#WSSecSignature()}
     *             and {@link WSSecHeader} for actor and mustunderstand
     *             specification.
     */
    public WSSignEnvelope(String actor, boolean mu) {
        super(actor, mu);
    }

    /**
     * set the single cert flag.
     *
     * @param useSingleCert
     * @deprecated replaced by {@link WSSecSignature#setUseSingleCertificate(boolean)}
     */
    public void setUseSingleCertificate(boolean useSingleCert) {
        this.useSingleCert = useSingleCert;
    }

    /**
     * Get the single cert flag.
     *
     * @return If to use a single cert
     * @deprecated replaced by {@link WSSecSignature#isUseSingleCertificate()}
     */
    public boolean isUseSingleCertificate() {
        return this.useSingleCert;
    }

    /**
     * Set the name of the signature encryption algorithm to use.
     *
     * If the algorithm is not set then Triple RSA is used. Refer to WSConstants
     * which algorithms are supported.
     *
     * @param algo
     *            Is the name of the signature algorithm
     * @see WSConstants#RSA
     * @see WSConstants#DSA
     * @deprecated replaced by {@link WSSecSignature#setSignatureAlgorithm(String)}
     */
    public void setSignatureAlgorithm(String algo) {
        sigAlgo = algo;
    }

    /**
     * Get the name of the signature algorithm that is being used.
     *
     * If the algorithm is not set then RSA is default.
     *
     * @return the identifier URI of the signature algorithm
     * @deprecated replaced by {@link WSSecSignature#getSignatureAlgorithm()}
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
     * @param algo
     *            Is the name of the signature algorithm
     * @see WSConstants#C14N_OMIT_COMMENTS
     * @see WSConstants#C14N_WITH_COMMENTS
     * @see WSConstants#C14N_EXCL_OMIT_COMMENTS
     * @see WSConstants#C14N_EXCL_WITH_COMMENTS
     * @deprecated replaced by {@link WSSecSignature#setSigCanonicalization(String)}
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
     * @return TODO
     * @deprecated replaced by {@link WSSecSignature#getSigCanonicalization()}
     */
    public String getSigCanonicalization() {
        return canonAlgo;
    }

    /**
     * @param usernameToken The usernameToken to set.
     * @deprecated replaced by {@link WSSecSignature#setUsernameToken(WSSecUsernameToken)}
     */
    public void setUsernameToken(WSSAddUsernameToken usernameToken) {
        this.usernameToken = usernameToken;
    }

    /**
     * @return Returns the signatureValue.
     * @deprecated replaced by {@link WSSecSignature#getSignatureValue()}
     */
    public byte[] getSignatureValue() {
        return signatureValue;
    }

    /**
     * Builds a signed soap envelope.
     *
     * The method first gets an appropriate
     * security header. According to the defined parameters for certificate
     * handling the signature elements are constructed and inserted into the
     * <code>wsse:Signature</code>
     *
     * @param doc    The unsigned SOAP envelope as <code>Document</code>
     * @param crypto An instance of the Crypto API to handle keystore and
     *               certificates
     * @return A signed SOAP envelope as <code>Document</code>
     * @throws WSSecurityException
     * @deprecated replaced by {@link WSSecSignature#build(Document, Crypto, WSSecHeader)}
     */
    public Document build(Document doc, Crypto crypto)
            throws WSSecurityException {
        doDebug = log.isDebugEnabled();

        long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }
        if (doDebug) {
            log.debug("Beginning signing...");
        }

        /*
         * Gather some info about the document to process and store it for
         * retrieval
         */
        WSDocInfo wsDocInfo = new WSDocInfo(doc);
        wsDocInfo.setCrypto(crypto);

        Element envelope = doc.getDocumentElement();
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(envelope);

        Element securityHeader = insertSecurityHeader(doc);

        // Set the id of the elements to be used as digest source
        // String id = setBodyID(doc);
        String certUri = null;
        X509Certificate[] certs = null;
        if (keyIdentifierType != WSConstants.UT_SIGNING) {
            certs = crypto.getCertificates(user);
            if (certs == null || certs.length <= 0) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "noUserCertsFound", 
                    new Object[] { user, "signature" }
                );
            }
            certUri = wssConfig.getIdAllocator().createSecureId("CertId-", certs[0]);  
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
                        new Object[] {
                            pubKeyAlgo
                        }
                    );
                }
            }
        }
        XMLSignature sig = null;

        if (canonAlgo.equals(WSConstants.C14N_EXCL_OMIT_COMMENTS)) {
            Element canonElem = XMLUtils.createElementInSignatureSpace(doc,
                    Constants._TAG_CANONICALIZATIONMETHOD);

            canonElem.setAttributeNS(null, Constants._ATT_ALGORITHM, canonAlgo);

            if (wssConfig.isWsiBSPCompliant()) {
                Set prefixes = getInclusivePrefixes(securityHeader, false);

                InclusiveNamespaces inclusiveNamespaces = new InclusiveNamespaces(
                        doc, prefixes);

                canonElem.appendChild(inclusiveNamespaces.getElement());
            }

            try {
                SignatureAlgorithm signatureAlgorithm = new SignatureAlgorithm(
                        doc, sigAlgo);
                sig = new XMLSignature(doc, null, signatureAlgorithm
                        .getElement(), canonElem);
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
        /*
         * If we don't generate a new Transforms for each addDocument here, then
         * only the last Transforms is put into the according ds:Reference
         * element, i.e. the first ds:Reference does not contain a Transforms
         * element. Thus the verification fails (somehow)
         */

        KeyInfo info = sig.getKeyInfo();
        String keyInfoUri = wssConfig.getIdAllocator().createSecureId("KeyId-", info);
        info.setId(keyInfoUri);

        SecurityTokenReference secRef = new SecurityTokenReference(doc);
        String secRefId = wssConfig.getIdAllocator().createSecureId("STRId-", info); 
        secRef.setID(secRefId);

        if (tlog.isDebugEnabled()) {
            t1 = System.currentTimeMillis();
        }

        if (parts == null) {
            parts = new Vector();
            WSEncryptionPart encP = new WSEncryptionPart(soapConstants
                    .getBodyQName().getLocalPart(), soapConstants
                    .getEnvelopeURI(), "Content");
            parts.add(encP);
        }

        Transforms transforms = null;

        for (int part = 0; part < parts.size(); part++) {
            WSEncryptionPart encPart = (WSEncryptionPart) parts.get(part);

            String idToSign = encPart.getId();

            String elemName = encPart.getName();
            String nmSpace = encPart.getNamespace();

            /*
             * Set up the elements to sign. There are two reserved element
             * names: "Token" and "STRTransform" "Token": Setup the Signature to
             * either sign the information that points to the security token or
             * the token itself. If its a direct reference sign the token,
             * otherwise sign the KeyInfo Element. "STRTransform": Setup the
             * ds:Reference to use STR Transform
             *
             */
            try {
                if (idToSign != null) {
                    Element toSignById = WSSecurityUtil
                            .findElementById(doc.getDocumentElement(),
                                    idToSign, WSConstants.WSU_NS);
                    if (toSignById == null) {
                        toSignById = WSSecurityUtil.findElementById(doc
                                .getDocumentElement(), idToSign, null);
                    }
                    transforms = new Transforms(doc);
                    transforms
                            .addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (wssConfig.isWsiBSPCompliant()) {
                        transforms.item(0).getElement().appendChild(
                                new InclusiveNamespaces(doc,
                                        getInclusivePrefixes(toSignById))
                                        .getElement());
                    }
                    sig.addDocument("#" + idToSign, transforms);
                }
                else if (elemName.equals("Token")) {
                    transforms = new Transforms(doc);
                    transforms
                            .addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (keyIdentifierType == WSConstants.BST_DIRECT_REFERENCE) {
                        if (wssConfig.isWsiBSPCompliant()) {
                            transforms
                                    .item(0)
                                    .getElement()
                                    .appendChild(
                                            new InclusiveNamespaces(
                                                    doc,
                                                    getInclusivePrefixes(securityHeader))
                                                    .getElement());
                        }
                        sig.addDocument("#" + certUri, transforms);
                    } else {
                        if (wssConfig.isWsiBSPCompliant()) {
                            transforms.item(0).getElement().appendChild(
                                    new InclusiveNamespaces(doc,
                                            getInclusivePrefixes(info
                                                    .getElement()))
                                            .getElement());
                        }
                        sig.addDocument("#" + keyInfoUri, transforms);
                    }
                } else if (elemName.equals("STRTransform")) { // STRTransform
                    Element ctx = createSTRParameter(doc);
                    transforms = new Transforms(doc);
                    transforms.addTransform(
                            STRTransform.implementedTransformURI, ctx);
                    sig.addDocument("#" + secRefId, transforms);
                } else if (elemName.equals("Assertion")) { // Assertion

                    String id = null;
                    id = SAMLUtil.getAssertionId(envelope, elemName, nmSpace);

                    Element body = (Element) WSSecurityUtil.findElement(
                            envelope, elemName, nmSpace);
                    if (body == null) {
                        throw new WSSecurityException(
                                WSSecurityException.FAILURE, "noEncElement",
                                new Object[] { nmSpace + ", " + elemName });
                    }
                    transforms = new Transforms(doc);
                    transforms
                            .addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (wssConfig.isWsiBSPCompliant()) {
                        transforms.item(0).getElement().appendChild(
                                new InclusiveNamespaces(doc,
                                        getInclusivePrefixes(body))
                                        .getElement());
                    }
                    String prefix = WSSecurityUtil.setNamespace(body,
                            WSConstants.WSU_NS, WSConstants.WSU_PREFIX);
                    body.setAttributeNS(WSConstants.WSU_NS, prefix + ":Id",
                            id);
                    sig.addDocument("#" + id, transforms);

                } else {
                    Element body = (Element) WSSecurityUtil.findElement(
                            envelope, elemName, nmSpace);
                    if (body == null) {
                        throw new WSSecurityException(
                                WSSecurityException.FAILURE, "noEncElement",
                                new Object[] { nmSpace + ", " + elemName });
                    }
                    transforms = new Transforms(doc);
                    transforms
                            .addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (wssConfig.isWsiBSPCompliant()) {
                        transforms.item(0).getElement().appendChild(
                                new InclusiveNamespaces(doc,
                                        getInclusivePrefixes(body))
                                        .getElement());
                    }
                    sig.addDocument("#" + setWsuId(body), transforms);
                }
            } catch (TransformationException e1) {
                throw new WSSecurityException(
                        WSSecurityException.FAILED_SIGNATURE, "noXMLSig", null,
                        e1);
            } catch (XMLSignatureException e1) {
                throw new WSSecurityException(
                        WSSecurityException.FAILED_SIGNATURE, "noXMLSig", null,
                        e1);
            }
        }

        sig.addResourceResolver(EnvelopeIdResolver.getInstance());

        WSSecurityUtil.prependChildElement(securityHeader, sig.getElement());
        if (tlog.isDebugEnabled()) {
            t2 = System.currentTimeMillis();
        }

        byte[] secretKey = null;
        switch (keyIdentifierType) {
        case WSConstants.BST_DIRECT_REFERENCE:
            Reference ref = new Reference(doc);
            ref.setURI("#" + certUri);
            BinarySecurity bstToken = null;
            if (!useSingleCert) {
                bstToken = new PKIPathSecurity(doc);
                ((PKIPathSecurity) bstToken).setX509Certificates(certs, false,
                        crypto);
            } else {
                bstToken = new X509Security(doc);
                ((X509Security) bstToken).setX509Certificate(certs[0]);
            }
            ref.setValueType(bstToken.getValueType());
            secRef.setReference(ref);
            bstToken.setID(certUri);
            WSSecurityUtil.prependChildElement(securityHeader, bstToken.getElement());
            wsDocInfo.setBst(bstToken.getElement());
            break;

        case WSConstants.ISSUER_SERIAL:
            XMLX509IssuerSerial data = new XMLX509IssuerSerial(doc, certs[0]);
            X509Data x509Data = new X509Data(doc);
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
            Reference refUt = new Reference(doc);
            refUt.setValueType(WSConstants.USERNAMETOKEN_NS + "#UsernameToken");
            String utId = usernameToken.getId();
            if (utId == null) {
                utId = wssConfig.getIdAllocator().createId("usernameTokenId-", usernameToken);
                usernameToken.setId(utId);
            }
            refUt.setURI("#" + utId);
            secRef.setReference(refUt);
            secretKey = usernameToken.getSecretKey();
            break;

        case WSConstants.THUMBPRINT_IDENTIFIER:
            secRef.setKeyIdentifierThumb(certs[0]);
            break;

        default:
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "unsupportedKeyId");
        }
        if (tlog.isDebugEnabled()) {
            t3 = System.currentTimeMillis();
        }
        info.addUnknownElement(secRef.getElement());

        boolean remove = WSDocInfoStore.store(wsDocInfo);
        try {
            if (keyIdentifierType == WSConstants.UT_SIGNING) {
                sig.sign(sig.createSecretKey(secretKey));
            } else {
                sig.sign(crypto.getPrivateKey(user, password));
            }
            signatureValue = sig.getSignatureValue();
        } catch (XMLSignatureException e1) {
            throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE,
                    null, null, e1);
        } catch (Exception e1) {
            throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE,
                    null, null, e1);
        } finally {
            if (remove) {
                WSDocInfoStore.delete(wsDocInfo);
            }
        }
        if (tlog.isDebugEnabled()) {
            t4 = System.currentTimeMillis();
            tlog.debug("SignEnvelope: cre-Sig= " + (t1 - t0)
                    + " set transform= " + (t2 - t1) + " sec-ref= " + (t3 - t2)
                    + " signature= " + (t4 - t3));
        }
        if (doDebug) {
            log.debug("Signing complete.");
        }
        return (doc);
    }

    protected Element createSTRParameter(Document doc) {
        Element transformParam = doc.createElementNS(WSConstants.WSSE_NS,
                WSConstants.WSSE_PREFIX + ":TransformationParameters");

        WSSecurityUtil.setNamespace(transformParam, WSConstants.WSSE_NS,
                WSConstants.WSSE_PREFIX);

        Element canonElem = doc.createElementNS(WSConstants.SIG_NS,
                WSConstants.SIG_PREFIX + ":CanonicalizationMethod");

        WSSecurityUtil.setNamespace(canonElem, WSConstants.SIG_NS,
                WSConstants.SIG_PREFIX);

        canonElem.setAttributeNS(null, "Algorithm",
                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        transformParam.appendChild(canonElem);
        return transformParam;
    }

    protected Set getInclusivePrefixes(Element target) {
        return getInclusivePrefixes(target, true);
    }

    protected Set getInclusivePrefixes(Element target, boolean excludeVisible) {
        Set result = new HashSet();
        Node parent = target;
        NamedNodeMap attributes;
        Node attribute;
        while (!(parent.getParentNode() instanceof Document)) {
            parent = parent.getParentNode();
            attributes = parent.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                attribute = attributes.item(i);
                if (attribute.getNamespaceURI() != null
                        && attribute.getNamespaceURI().equals(
                                org.apache.ws.security.WSConstants.XMLNS_NS)) {
                    if (attribute.getNodeName().equals("xmlns")) {
                        result.add("#default");
                    } else {
                        result.add(attribute.getLocalName());
                    }
                }
            }
        }

        if (excludeVisible == true) {
            attributes = target.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                attribute = attributes.item(i);
                if (attribute.getNamespaceURI() != null
                        && attribute.getNamespaceURI().equals(
                                org.apache.ws.security.WSConstants.XMLNS_NS)) {
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
}
