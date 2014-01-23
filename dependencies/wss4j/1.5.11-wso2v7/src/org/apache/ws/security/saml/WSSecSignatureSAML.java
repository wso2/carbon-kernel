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

package org.apache.ws.security.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSDocInfoStore;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.EnvelopeIdResolver;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.message.token.X509Security;
import org.apache.ws.security.transform.STRTransform;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.InclusiveNamespaces;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLException;
import org.opensaml.SAMLObject;
import org.opensaml.SAMLSubject;
import org.opensaml.SAMLSubjectStatement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class WSSecSignatureSAML extends WSSecSignature {

    private static Log log = LogFactory.getLog(WSSecSignatureSAML.class.getName());
    private boolean senderVouches = false;
    private SecurityTokenReference secRefSaml = null;
    private Element samlToken = null;
    private Crypto userCrypto = null;
    private Crypto issuerCrypto = null;
    private String issuerKeyName = null;
    private String issuerKeyPW = null;

    /**
     * Constructor.
     */
    public WSSecSignatureSAML() {
        doDebug = log.isDebugEnabled();
    }

    /**
     * Builds a signed soap envelope with SAML token.
     * 
     * The method first gets an appropriate security header. According to the
     * defined parameters for certificate handling the signature elements are
     * constructed and inserted into the <code>wsse:Signature</code>
     * 
     * @param doc
     *            The unsigned SOAP envelope as <code>Document</code>
     * @param uCrypto
     *            The user's Crypto instance
     * @param assertion
     *            the complete SAML assertion
     * @param iCrypto
     *            An instance of the Crypto API to handle keystore SAML token
     *            issuer and to generate certificates
     * @param iKeyName
     *            Private key to use in case of "sender-Vouches"
     * @param iKeyPW
     *            Password for issuer private key
     * @param secHeader
     *            The Security header
     * @return A signed SOAP envelope as <code>Document</code>
     * @throws org.apache.ws.security.WSSecurityException
     */
    public Document build(
        Document doc, Crypto uCrypto, SAMLAssertion assertion, 
        Crypto iCrypto, String iKeyName, String iKeyPW, WSSecHeader secHeader
    ) throws WSSecurityException {

        prepare(doc, uCrypto, assertion, iCrypto, iKeyName, iKeyPW, secHeader);

        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc
                .getDocumentElement());

        if (parts == null) {
            parts = new Vector();
            WSEncryptionPart encP = new WSEncryptionPart(soapConstants
                    .getBodyQName().getLocalPart(), soapConstants
                    .getEnvelopeURI(), "Content");
            parts.add(encP);
        }
        addReferencesToSign(parts, secHeader);

        //
        // The order to prepend is: - signature Element - BinarySecurityToken
        // (depends on mode) - SecurityTokenRefrence (depends on mode) - SAML
        // token
        //
        prependToHeader(secHeader);

        //
        // if we have a BST prepend it in front of the Signature according to
        // strict layout rules.
        //
        if (bstToken != null) {
            prependBSTElementToHeader(secHeader);
        }

        prependSAMLElementsToHeader(secHeader);

        computeSignature();

        return doc;
    }

    /**
     * Initialize a WSSec SAML Signature.
     * 
     * The method sets up and initializes a WSSec SAML Signature structure after
     * the relevant information was set. After setup of the references to
     * elements to sign may be added. After all references are added they can be
     * signed.
     * 
     * This method does not add the Signature element to the security header.
     * See <code>prependSignatureElementToHeader()</code> method.
     * 
     * @param doc
     *            The SOAP envelope as <code>Document</code>
     * @param uCrypto
     *            The user's Crypto instance
     * @param assertion
     *            the complete SAML assertion
     * @param iCrypto
     *            An instance of the Crypto API to handle keystore SAML token
     *            issuer and to generate certificates
     * @param iKeyName
     *            Private key to use in case of "sender-Vouches"
     * @param iKeyPW
     *            Password for issuer private key
     * @param secHeader
     *            The Security header
     * @throws WSSecurityException
     */
    public void prepare(
        Document doc, Crypto uCrypto, SAMLAssertion assertion, Crypto iCrypto, 
        String iKeyName, String iKeyPW, WSSecHeader secHeader
    ) throws WSSecurityException {

        if (doDebug) {
            log.debug("Beginning ST signing...");
        }

        userCrypto = uCrypto;
        issuerCrypto = iCrypto;
        document = doc;
        issuerKeyName = iKeyName;
        issuerKeyPW = iKeyPW;

        //
        // Get some information about the SAML token content. This controls how
        // to deal with the whole stuff. First get the Authentication statement
        // (includes Subject), then get the _first_ confirmation method only
        // thats if "senderVouches" is true.
        //
        SAMLSubjectStatement samlSubjS = null;
        Iterator it = assertion.getStatements();
        while (it.hasNext()) {
            SAMLObject so = (SAMLObject) it.next();
            if (so instanceof SAMLSubjectStatement) {
                samlSubjS = (SAMLSubjectStatement) so;
                break;
            }
        }
        SAMLSubject samlSubj = null;
        if (samlSubjS != null) {
            samlSubj = samlSubjS.getSubject();
        }
        if (samlSubj == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidSAMLToken", new Object[] { "for Signature" });
        }

        String confirmMethod = null;
        it = samlSubj.getConfirmationMethods();
        if (it.hasNext()) {
            confirmMethod = (String) it.next();
        }
        if (SAMLSubject.CONF_SENDER_VOUCHES.equals(confirmMethod)) {
            senderVouches = true;
        }
        //
        // Gather some info about the document to process and store it for
        // retrieval
        //
        wsDocInfo = new WSDocInfo(doc);

        X509Certificate[] certs = null;
        PublicKey publicKey = null;

        if (senderVouches) {
            certs = issuerCrypto.getCertificates(issuerKeyName);
            wsDocInfo.setCrypto(issuerCrypto);
        }
        //
        // in case of key holder: - get the user's certificate that _must_ be
        // included in the SAML token. To ensure the cert integrity the SAML
        // token must be signed (by the issuer). Just check if its signed, but
        // don't verify this SAML token's signature here (maybe later).
        //
        else {
            if (userCrypto == null || !assertion.isSigned()) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "invalidSAMLsecurity",
                    new Object[] { "for SAML Signature (Key Holder)" }
                );
            }
            Element e = samlSubj.getKeyInfo();
            try {
                KeyInfo ki = new KeyInfo(e, null);

                if (ki.containsX509Data()) {
                    X509Data data = ki.itemX509Data(0);
                    if (data != null && data.containsCertificate()) {
                        XMLX509Certificate certElem = data.itemCertificate(0);
                        if (certElem != null) {
                            X509Certificate cert = certElem.getX509Certificate();
                            certs = new X509Certificate[1];
                            certs[0] = cert;
                        }
                    } else if (data != null && data.containsIssuerSerial()) {
                        XMLX509IssuerSerial issuerSerial = data.itemIssuerSerial(0);
                        String alias = 
                            userCrypto.getAliasForX509Cert(
                                issuerSerial.getIssuerName(), issuerSerial.getSerialNumber()
                            );
                        certs = userCrypto.getCertificates(alias);
                    }
                }  else if (ki.containsKeyValue()) {
                    publicKey = ki.getPublicKey();
                }
                // TODO: get alias name for cert, check against username set by
                // caller
            } catch (XMLSecurityException e3) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "invalidSAMLsecurity",
                    new Object[] { "cannot get certificate (key holder)" },
                    e3
                );
            }
            wsDocInfo.setCrypto(userCrypto);
        }
        if ((certs == null || certs.length == 0 || certs[0] == null) 
                && publicKey == null) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE,
                "noCertsFound",
                new Object[] { "SAML signature" }
            );
        }
        if (sigAlgo == null) {
            PublicKey key = null;
            if (certs != null && certs[0] != null) {
                key = certs[0].getPublicKey();
            } else if (publicKey != null) {
                key = publicKey;
            }
            
            String pubKeyAlgo = key.getAlgorithm();
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
        sig = null;
        if (canonAlgo.equals(WSConstants.C14N_EXCL_OMIT_COMMENTS)) {
            Element canonElem = 
                XMLUtils.createElementInSignatureSpace(doc, Constants._TAG_CANONICALIZATIONMETHOD);

            canonElem.setAttributeNS(null, Constants._ATT_ALGORITHM, canonAlgo);

            if (wssConfig.isWsiBSPCompliant()) {
                Set prefixes = getInclusivePrefixes(secHeader.getSecurityHeader(), false);

                InclusiveNamespaces inclusiveNamespaces = 
                    new InclusiveNamespaces(doc, prefixes);

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
        String sigUri = wssConfig.getIdAllocator().createId("Signature-", sig);
        sig.setId(sigUri);

        keyInfo = sig.getKeyInfo();
        keyInfoUri = wssConfig.getIdAllocator().createSecureId("KeyId-", keyInfo);
        keyInfo.setId(keyInfoUri);

        secRef = new SecurityTokenReference(doc);
        strUri = wssConfig.getIdAllocator().createSecureId("STRId-", secRef);
        secRef.setID(strUri);

        if (certs != null && certs.length != 0) {
            certUri = wssConfig.getIdAllocator().createSecureId("CertId-", certs[0]);
        }

        //
        // If the sender vouches, then we must sign the SAML token _and_ at
        // least one part of the message (usually the SOAP body). To do so we
        // need to - put in a reference to the SAML token. Thus we create a STR
        // and insert it into the wsse:Security header - set a reference of the
        // created STR to the signature and use STR Transform during the
        // signature
        //
        Transforms transforms = null;
        try {
            if (senderVouches) {
                secRefSaml = new SecurityTokenReference(doc);
                String strSamlUri = 
                    wssConfig.getIdAllocator().createSecureId("STRSAMLId-", secRefSaml);
                secRefSaml.setID(strSamlUri);

                if (WSConstants.X509_KEY_IDENTIFIER == keyIdentifierType) {
                    Element keyId = doc.createElementNS(WSConstants.WSSE_NS, "wsse:KeyIdentifier");
                    keyId.setAttributeNS(
                        null, "ValueType", WSConstants.WSS_SAML_KI_VALUE_TYPE
                    );
                    keyId.appendChild(doc.createTextNode(assertion.getId()));
                    Element elem = secRefSaml.getElement();
                    elem.appendChild(keyId);
                } else {
                    Reference ref = new Reference(doc);
                    ref.setURI("#" + assertion.getId());
                    ref.setValueType(WSConstants.WSS_SAML_KI_VALUE_TYPE);
                    secRefSaml.setReference(ref);
                }

                Element ctx = createSTRParameter(doc);
                transforms = new Transforms(doc);
                transforms.addTransform(STRTransform.implementedTransformURI, ctx);
                sig.addDocument("#" + strSamlUri, transforms);
                wsDocInfo.setSecurityTokenReference(secRefSaml.getElement());
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

        if (senderVouches) {
            switch (keyIdentifierType) {
            case WSConstants.BST_DIRECT_REFERENCE:
                Reference ref = new Reference(doc);
                ref.setURI("#" + certUri);
                bstToken = new X509Security(doc);
                ((X509Security) bstToken).setX509Certificate(certs[0]);
                bstToken.setID(certUri);
                wsDocInfo.setBst(bstToken.getElement());
                ref.setValueType(bstToken.getValueType());
                secRef.setReference(ref);
                break;
                
            case WSConstants.X509_KEY_IDENTIFIER :
                secRef.setKeyIdentifier(certs[0]);
                break;

            default:
                throw new WSSecurityException(WSSecurityException.FAILURE, "unsupportedKeyId");
            }
        } else {
            switch (keyIdentifierType) {
            case WSConstants.BST_DIRECT_REFERENCE:
                Reference ref = new Reference(doc);
                ref.setURI("#" + assertion.getId());
                ref.setValueType(WSConstants.WSS_SAML_KI_VALUE_TYPE);
                secRef.setReference(ref);
                break;
                
            case WSConstants.X509_KEY_IDENTIFIER :
                Element keyId = doc.createElementNS(WSConstants.WSSE_NS, "wsse:KeyIdentifier");
                keyId.setAttributeNS(
                    null, "ValueType", WSConstants.WSS_SAML_KI_VALUE_TYPE
                );
                keyId.appendChild(doc.createTextNode(assertion.getId()));
                Element elem = secRef.getElement();
                elem.appendChild(keyId);
                break;

            default:
                throw new WSSecurityException(WSSecurityException.FAILURE, "unsupportedKeyId");
            }
        }
        keyInfo.addUnknownElement(secRef.getElement());
        wsDocInfo.setSecurityTokenReference(secRef.getElement());
        
        Element keyInfoElement = keyInfo.getElement();
        keyInfoElement.setAttributeNS(
            WSConstants.XMLNS_NS, "xmlns:" + WSConstants.SIG_PREFIX, WSConstants.SIG_NS
        );

        try {
            samlToken = (Element) assertion.toDOM(doc);
        } catch (SAMLException e2) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_SIGNATURE, "noSAMLdoc", null, e2
            );
        }
        wsDocInfo.setAssertion(samlToken);
    }

    /**
     * Prepend the SAML elements to the elements already in the Security header.
     * 
     * The method can be called any time after <code>prepare()</code>. This
     * allows to insert the SAML elements at any position in the Security
     * header.
     * 
     * This methods first prepends the SAML security reference if mode is
     * <code>senderVouches</code>, then the SAML token itself,
     * 
     * @param secHeader
     *            The security header that holds the BST element.
     */
    public void prependSAMLElementsToHeader(WSSecHeader secHeader) {
        if (senderVouches) {
            WSSecurityUtil.prependChildElement(
                secHeader.getSecurityHeader(), secRefSaml.getElement()
            );
        }

        WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), samlToken);
    }

    /**
     * This method adds references to the Signature.
     * 
     * The added references are signed when calling
     * <code>computeSignature()</code>. This method can be called several
     * times to add references as required. <code>addReferencesToSign()</code>
     * can be called anytime after <code>prepare</code>.
     * 
     * @param references
     *            A vector containing <code>WSEncryptionPart</code> objects
     *            that define the parts to sign.
     * @param secHeader
     *            Used to compute namespaces to be inserted by
     *            InclusiveNamespaces to be WSI compliant.
     * @throws WSSecurityException
     */
    public void addReferencesToSign(Vector references, WSSecHeader secHeader)
        throws WSSecurityException {
        Transforms transforms = null;

        Element envelope = document.getDocumentElement();
        for (int part = 0; part < parts.size(); part++) {
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
            transforms = new Transforms(document);
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
                    transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (wssConfig.isWsiBSPCompliant()) {
                        transforms.item(0).getElement().appendChild(
                            new InclusiveNamespaces(
                                document,
                                getInclusivePrefixes(toSignById)
                            ).getElement());
                    }
                    sig.addDocument("#" + idToSign, transforms, this.getDigestAlgo());
                } else if (elemName.equals("Token")) {
                    transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (keyIdentifierType == WSConstants.BST_DIRECT_REFERENCE) {
                        if (wssConfig.isWsiBSPCompliant()) {
                            transforms.item(0).getElement().appendChild(
                                new InclusiveNamespaces(
                                    document,
                                    getInclusivePrefixes(secHeader.getSecurityHeader())
                                ).getElement());
                        }
                        sig.addDocument("#" + certUri, transforms, this.getDigestAlgo());
                    } else {
                        if (wssConfig.isWsiBSPCompliant()) {
                            transforms.item(0).getElement().appendChild(
                                new InclusiveNamespaces(
                                    document,
                                    getInclusivePrefixes(keyInfo.getElement())
                                ).getElement());
                        }
                        sig.addDocument("#" + keyInfoUri, transforms, this.getDigestAlgo());
                    }
                } else if (elemName.equals("STRTransform")) { // STRTransform
                    Element ctx = createSTRParameter(document);
                    transforms.addTransform(STRTransform.implementedTransformURI, ctx);
                    sig.addDocument("#" + strUri, transforms, this.getDigestAlgo());
                } else {
                    Element body = 
                        (Element) WSSecurityUtil.findElement(envelope, elemName, nmSpace);
                    if (body == null) {
                        throw new WSSecurityException(
                            WSSecurityException.FAILURE, "noEncElement",
                            new Object[] { nmSpace + ", " + elemName }
                        );
                    }
                    transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (wssConfig.isWsiBSPCompliant()) {
                        transforms.item(0).getElement().appendChild(
                            new InclusiveNamespaces(
                                document,
                                getInclusivePrefixes(body)
                            ).getElement());
                    }
                    sig.addDocument("#" + setWsuId(body), transforms, this.getDigestAlgo());
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
     * Compute the Signature over the references.
     * 
     * After references are set this method computes the Signature for them.
     * This method can be called anytime after the references were set. See
     * <code>addReferencesToSign()</code>.
     * 
     * @throws WSSecurityException
     */
    public void computeSignature() throws WSSecurityException {
        boolean remove = WSDocInfoStore.store(wsDocInfo);

        try {
            if (senderVouches) {
                sig.sign(issuerCrypto.getPrivateKey(issuerKeyName, issuerKeyPW));
            } else {
                sig.sign(userCrypto.getPrivateKey(user, password));
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
}
