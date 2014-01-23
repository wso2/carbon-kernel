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
import org.apache.ws.security.message.WSSignEnvelope;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.message.token.X509Security;
import org.apache.ws.security.transform.STRTransform;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLException;
import org.opensaml.SAMLObject;
import org.opensaml.SAMLSubject;
import org.opensaml.SAMLSubjectStatement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Vector;

public class WSSignSAMLEnvelope extends WSSignEnvelope {

    private static Log log = LogFactory.getLog(WSSignSAMLEnvelope.class
            .getName());

    private static Log tlog = LogFactory.getLog("org.apache.ws.security.TIME");

    /**
     * Constructor.
     * 
     * @deprecated replaced by {@link WSSecSignatureSAML#WSSecSignatureSAML()}
     */
    public WSSignSAMLEnvelope() {
    }

    /**
     * Constructor.
     * 
     * @param actor
     *            The actor name of the <code>wsse:Security</code> header
     * @param mu
     *            Set <code>mustUnderstand</code> to true or false
     * 
     * @deprecated replaced by {@link WSSecSignatureSAML#WSSecSignatureSAML()} and
     *             {@link WSSecHeader} for actor and mustunderstand
     *             specification.
     */
    public WSSignSAMLEnvelope(String actor, boolean mu) {
        super(actor, mu);
    }

    /**
     * Builds a signed soap envelope with SAML token. <p/>The method first gets
     * an appropriate security header. According to the defined parameters for
     * certificate handling the signature elements are constructed and inserted
     * into the <code>wsse:Signature</code>
     * 
     * @param doc
     *            The unsigned SOAP envelope as <code>Document</code>
     * @param assertion
     *            the complete SAML assertion
     * @param issuerCrypto
     *            An instance of the Crypto API to handle keystore SAML token
     *            issuer and to generate certificates
     * @param issuerKeyName
     *            Private key to use in case of "sender-Vouches"
     * @param issuerKeyPW
     *            Password for issuer private key
     * @return A signed SOAP envelope as <code>Document</code>
     * @throws org.apache.ws.security.WSSecurityException
     * @deprecated replaced by
     *             {@link WSSecSignatureSAML#build(Document, Crypto, SAMLAssertion, Crypto, String, String, WSSecHeader)}
     */
    public Document build(Document doc, Crypto userCrypto,
            SAMLAssertion assertion, Crypto issuerCrypto, String issuerKeyName,
            String issuerKeyPW) throws WSSecurityException {

        doDebug = log.isDebugEnabled();

        long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }
        if (doDebug) {
            log.debug("Beginning ST signing...");
        }
        /*
         * Get some information about the SAML token content. This controls how
         * to deal with the whole stuff. First get the Authentication statement
         * (includes Subject), then get the _first_ confirmation method only.
         */
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
        boolean senderVouches = false;
        if (SAMLSubject.CONF_SENDER_VOUCHES.equals(confirmMethod)) {
            senderVouches = true;
        }
        /*
         * Gather some info about the document to process and store it for
         * retrieval
         */
        WSDocInfo wsDocInfo = new WSDocInfo(doc);

        Element envelope = doc.getDocumentElement();
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(envelope);

        Element securityHeader = insertSecurityHeader(doc);
        X509Certificate[] certs = null;

        if (senderVouches) {
            certs = issuerCrypto.getCertificates(issuerKeyName);
            wsDocInfo.setCrypto(issuerCrypto);
        }
        /*
         * in case of key holder: - get the user's certificate that _must_ be
         * included in the SAML token. To ensure the cert integrity the SAML
         * token must be signed (by the issuer). Just check if its signed, but
         * don't verify this SAML token's signature here (maybe later).
         */
        else {
            if (userCrypto == null || assertion.isSigned() == false) {
                throw new WSSecurityException(WSSecurityException.FAILURE,
                        "invalidSAMLsecurity",
                        new Object[] { "for SAML Signature (Key Holder)" });
            }
            Element e = samlSubj.getKeyInfo();
            try {
                KeyInfo ki = new KeyInfo(e, null);

                if (ki.containsX509Data()) {
                    X509Data data = ki.itemX509Data(0);
                    XMLX509Certificate certElem = null;
                    if (data != null && data.containsCertificate()) {
                        certElem = data.itemCertificate(0);
                    }
                    if (certElem != null) {
                        X509Certificate cert = certElem.getX509Certificate();
                        certs = new X509Certificate[1];
                        certs[0] = cert;
                    }
                }
                // TODO: get alias name for cert, check against username set by
                // caller
            } catch (XMLSecurityException e3) {
                throw new WSSecurityException(WSSecurityException.FAILURE,
                        "invalidSAMLsecurity",
                        new Object[] { "cannot get certificate (key holder)" },
                        e3);
            }
            wsDocInfo.setCrypto(userCrypto);
        }
        // Set the id of the elements to be used as digest source
        // String id = setBodyID(doc);
        if (certs == null || certs.length <= 0) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE,
                "noCertsFound",
                new Object[] { "SAML signature" }
            );
        }
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
        XMLSignature sig = null;
        try {
            sig = new XMLSignature(doc, null, sigAlgo, canonAlgo);
        } catch (XMLSecurityException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_SIGNATURE, "noXMLSig", null, e
            );
        }

        KeyInfo info = sig.getKeyInfo();
        String keyInfoUri = wssConfig.getIdAllocator().createSecureId("KeyId-", info);
        info.setId(keyInfoUri);

        SecurityTokenReference secRef = new SecurityTokenReference(doc);
        String strUri = wssConfig.getIdAllocator().createSecureId("STRId-", secRef);
        secRef.setID(strUri);

        String certUri = wssConfig.getIdAllocator().createSecureId("CertId-", certs[0]);

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

        /*
         * If the sender vouches, then we must sign the SAML token _and_ at
         * least one part of the message (usually the SOAP body). To do so we
         * need to - put in a reference to the SAML token. Thus we create a STR
         * and insert it into the wsse:Security header - set a reference of the
         * created STR to the signature and use STR Transfrom during the
         * signature
         */
        Transforms transforms = null;
        SecurityTokenReference secRefSaml = null;

        try {
            if (senderVouches) {
                secRefSaml = new SecurityTokenReference(doc);
                String strSamlUri = wssConfig.getIdAllocator().createSecureId("STRSAMLId-", secRefSaml);
                secRefSaml.setID(strSamlUri);
                // Decouple Refernce/KeyInfo setup - quick shot here
                Reference ref = new Reference(doc);
                ref.setURI("#" + assertion.getId());
                ref.setValueType(WSConstants.WSS_SAML_NS
                        + WSConstants.WSS_SAML_ASSERTION);
                secRefSaml.setReference(ref);
                // up to here
                Element ctx = createSTRParameter(doc);
                transforms = new Transforms(doc);
                transforms.addTransform(STRTransform.implementedTransformURI,
                        ctx);
                sig.addDocument("#" + strSamlUri, transforms);
            }
            for (int part = 0; part < parts.size(); part++) {
                WSEncryptionPart encPart = (WSEncryptionPart) parts.get(part);
                String elemName = encPart.getName();
                String nmSpace = encPart.getNamespace();

                /*
                 * Set up the elements to sign. There are two resevered element
                 * names: "Token" and "STRTransform" "Token": Setup the
                 * Signature to either sign the information that points to the
                 * security token or the token itself. If its a direct reference
                 * sign the token, otherwise sign the KeyInfo Element.
                 * "STRTransform": Setup the ds:Reference to use STR Transform
                 * 
                 */
                if (elemName.equals("Token")) {
                    transforms = new Transforms(doc);
                    transforms
                            .addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                    if (keyIdentifierType == WSConstants.BST_DIRECT_REFERENCE) {
                        sig.addDocument("#" + certUri, transforms);
                    } else {
                        sig.addDocument("#" + keyInfoUri, transforms);
                    }
                } else if (elemName.equals("STRTransform")) { // STRTransform
                    Element ctx = createSTRParameter(doc);
                    transforms = new Transforms(doc);
                    transforms.addTransform(
                            STRTransform.implementedTransformURI, ctx);
                    sig.addDocument("#" + strUri, transforms);
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
                    sig.addDocument("#" + setWsuId(body), transforms);
                }
            }
        } catch (TransformationException e1) {
            throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE,
                    "noXMLSig", null, e1);
        } catch (XMLSignatureException e1) {
            throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE,
                    "noXMLSig", null, e1);
        }

        sig.addResourceResolver(EnvelopeIdResolver.getInstance());

        /*
         * The order to prepend is: - signature - BinarySecurityToken (depends
         * on mode) - SecurityTokenRefrence (depends on mode) - SAML token
         */

        WSSecurityUtil.prependChildElement(securityHeader, sig.getElement());

        if (tlog.isDebugEnabled()) {
            t2 = System.currentTimeMillis();
        }
        switch (keyIdentifierType) {
        case WSConstants.BST_DIRECT_REFERENCE:
            Reference ref = new Reference(doc);
            if (senderVouches) {
                ref.setURI("#" + certUri);
                BinarySecurity bstToken = null;
                bstToken = new X509Security(doc);
                ((X509Security) bstToken).setX509Certificate(certs[0]);
                bstToken.setID(certUri);
                WSSecurityUtil.prependChildElement(securityHeader, bstToken.getElement());
                wsDocInfo.setBst(bstToken.getElement());
                ref.setValueType(bstToken.getValueType());
            } else {
                ref.setURI("#" + assertion.getId());
                ref.setValueType(WSConstants.WSS_SAML_NS
                        + WSConstants.WSS_SAML_ASSERTION);
            }
            secRef.setReference(ref);
            break;
        //
        // case WSConstants.ISSUER_SERIAL :
        // XMLX509IssuerSerial data =
        // new XMLX509IssuerSerial(doc, certs[0]);
        // secRef.setX509IssuerSerial(data);
        // break;
        //
        // case WSConstants.X509_KEY_IDENTIFIER :
        // secRef.setKeyIdentifier(certs[0]);
        // break;
        //
        // case WSConstants.SKI_KEY_IDENTIFIER :
        // secRef.setKeyIdentifierSKI(certs[0], crypto);
        // break;
        //
        default:
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "unsupportedKeyId");
        }

        if (tlog.isDebugEnabled()) {
            t3 = System.currentTimeMillis();
        }
        info.addUnknownElement(secRef.getElement());
        
        Element keyInfoElement = info.getElement();
        keyInfoElement.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:"
                + WSConstants.SIG_PREFIX, WSConstants.SIG_NS);

        Element samlToken = null;
        try {
            samlToken = (Element) assertion.toDOM(doc);
        } catch (SAMLException e2) {
            throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE,
                    "noSAMLdoc", null, e2);
        }
        if (senderVouches) {
            WSSecurityUtil.prependChildElement(securityHeader, secRefSaml.getElement());
        }

        wsDocInfo.setAssertion(samlToken);
        WSSecurityUtil.prependChildElement(securityHeader, samlToken);

        boolean remove = WSDocInfoStore.store(wsDocInfo);
        try {
            if (senderVouches) {
                sig
                        .sign(issuerCrypto.getPrivateKey(issuerKeyName,
                                issuerKeyPW));
            } else {
                sig.sign(userCrypto.getPrivateKey(user, password));
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
}
