/*
 * Copyright  2003-2008 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ws.security.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.processor.EncryptedKeyProcessor;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAttribute;
import org.opensaml.SAMLAttributeStatement;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLException;
import org.opensaml.SAMLObject;
import org.opensaml.SAMLStatement;
import org.opensaml.SAMLSubject;
import org.opensaml.SAMLSubjectStatement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility methods for SAML stuff
 */
public class SAMLUtil {
    private static Log log = LogFactory.getLog(SAMLUtil.class.getName());

    
    
    /**
     * Extract certificates or the key available in the SAMLAssertion
     * @param elem
     * @return the SAML Key Info
     * @throws WSSecurityException
     */
    public static SAMLKeyInfo getSAMLKeyInfo(Element elem, Crypto crypto,
            CallbackHandler cb) throws WSSecurityException {
        SAMLAssertion assertion;
        try {
            // Check for duplicate saml:Assertion
			NodeList list = elem.getElementsByTagNameNS( WSConstants.SAML_NS,"Assertion");
			if (list != null && list.getLength() > 0) {
				throw new WSSecurityException("invalidSAMLSecurity");
			}
            assertion = new SAMLAssertion(elem);
            return getSAMLKeyInfo(assertion, crypto, cb);
        } catch (SAMLException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidSAMLToken", new Object[]{"for Signature (cannot parse)"}, e);
        }

    }
    
    public static SAMLKeyInfo getSAMLKeyInfo(SAMLAssertion assertion, Crypto crypto,
            CallbackHandler cb) throws WSSecurityException {
        
        //First ask the cb whether it can provide the secret
        WSPasswordCallback pwcb = new WSPasswordCallback(assertion.getId(), WSPasswordCallback.CUSTOM_TOKEN);
        if (cb != null) {
            try {
                cb.handle(new Callback[]{pwcb});
            } catch (Exception e1) {
                throw new WSSecurityException(WSSecurityException.FAILURE, "noKey",
                        new Object[] { assertion.getId() }, e1);
            }
        }
        
        byte[] key = pwcb.getKey();
        
        if (key != null) {
            return new SAMLKeyInfo(assertion, key);
        } else {
            Iterator statements = assertion.getStatements();
            while (statements.hasNext()) {
                SAMLStatement stmt = (SAMLStatement) statements.next();
                if (stmt instanceof SAMLAttributeStatement) {
                    SAMLAttributeStatement attrStmt = (SAMLAttributeStatement) stmt;
                    SAMLSubject samlSubject = attrStmt.getSubject();
                    Element kiElem = samlSubject.getKeyInfo();
                    
                    NodeList children = kiElem.getChildNodes();
                    int len = children.getLength();
                    
                    for (int i = 0; i < len; i++) {
                        Node child = children.item(i);
                        if (child.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        QName el = new QName(child.getNamespaceURI(), child.getLocalName());
                        if (el.equals(WSSecurityEngine.ENCRYPTED_KEY)) {
                            
                            EncryptedKeyProcessor proc = new EncryptedKeyProcessor();
                            proc.handleEncryptedKey((Element)child, cb, crypto, null);
                            
                            return new SAMLKeyInfo(assertion, proc.getDecryptedBytes());
                        } else if (el.equals(new QName(WSConstants.WST_NS, "BinarySecret"))) {
                            Text txt = (Text)child.getFirstChild();
                            return new SAMLKeyInfo(assertion, Base64.decode(txt.getData()));
                        }
                    }

                } else if (stmt instanceof SAMLAuthenticationStatement) {
                    SAMLAuthenticationStatement authStmt = (SAMLAuthenticationStatement)stmt;
                    SAMLSubject samlSubj = authStmt.getSubject(); 
                    if (samlSubj == null) {
                        throw new WSSecurityException(WSSecurityException.FAILURE,
                                "invalidSAMLToken", new Object[]{"for Signature (no Subject)"});
                    }

                    Element e = samlSubj.getKeyInfo();
                    X509Certificate[] certs = null;
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
                                return new SAMLKeyInfo(assertion, certs);
                            }
                        }

                    } catch (XMLSecurityException e3) {
                        throw new WSSecurityException(WSSecurityException.FAILURE,
                                                      "invalidSAMLSecurity",
                                new Object[]{"cannot get certificate (key holder)"}, e3);
                    }
                    
                } else {
                    throw new WSSecurityException(WSSecurityException.FAILURE,
                                                  "invalidSAMLSecurity",
                            new Object[]{"cannot get certificate or key "});
                }
            }
            
            throw new WSSecurityException(WSSecurityException.FAILURE,
                                          "invalidSAMLSecurity",
                    new Object[]{"cannot get certificate or key "});
                        
        }

    }
    
    /**
     * Extracts the certificate(s) from the SAML token reference.
     * <p/>
     *
     * @param elem The element containing the SAML token.
     * @return an array of X509 certificates
     * @throws org.apache.ws.security.WSSecurityException
     */
    public static X509Certificate[] getCertificatesFromSAML(Element elem)
            throws WSSecurityException {

        /*
         * Get some information about the SAML token content. This controls how
         * to deal with the whole stuff. First get the Authentication statement
         * (includes Subject), then get the _first_ confirmation method only.
         */
        SAMLAssertion assertion;
        try {
            assertion = new SAMLAssertion(elem);
        } catch (SAMLException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidSAMLToken", new Object[]{"for Signature (cannot parse)"}, e);
        }
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
                    "invalidSAMLToken", new Object[]{"for Signature (no Subject)"});
        }

//        String confirmMethod = null;
//        it = samlSubj.getConfirmationMethods();
//        if (it.hasNext()) {
//            confirmMethod = (String) it.next();
//        }
//        boolean senderVouches = false;
//        if (SAMLSubject.CONF_SENDER_VOUCHES.equals(confirmMethod)) {
//            senderVouches = true;
//        }
        Element e = samlSubj.getKeyInfo();
        X509Certificate[] certs = null;
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
            // TODO: get alias name for cert, check against username set by caller
        } catch (XMLSecurityException e3) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                                          "invalidSAMLSecurity",
                    new Object[]{"cannot get certificate (key holder)"}, e3);
        }
        return certs;
    }

    public static String getAssertionId(Element envelope, String elemName, String nmSpace) throws WSSecurityException {
        String id;
        // Make the AssertionID the wsu:Id and the signature reference the same
        SAMLAssertion assertion;

        Element assertionElement = (Element) WSSecurityUtil
                .findElement(envelope, elemName, nmSpace);

        try {
            assertion = new SAMLAssertion(assertionElement);
            id = assertion.getId();
        } catch (Exception e1) {
            log.error(e1);
            throw new WSSecurityException(
                    WSSecurityException.FAILED_SIGNATURE,
                    "noXMLSig", null, e1);
        }
        return id;
    }

     /**
     * Create a TimeStamp object from the SAML assertion.
     * @param assertion
     * @return
     * @throws WSSecurityException
     */
    public static Timestamp getTimestampForSAMLAssertion(Element assertion) throws WSSecurityException {

        String[] validityPeriod = getValidityPeriod(assertion);
        // If either of the timestamps are missing, then return a null
        if(validityPeriod[0] == null || validityPeriod[1] == null){
            return null;
        }

        try {
            DocumentBuilderFactory dbFactory =  DocumentBuilderFactory.newInstance();
            Document document =  dbFactory.newDocumentBuilder().newDocument();
            Element element = document.createElement("SAMLTimestamp");

            Element createdElement =  document.createElementNS( WSConstants.WSU_NS,WSConstants.CREATED_LN);
            createdElement.setTextContent(validityPeriod[0]);
            element.appendChild(createdElement);

            Element expiresElement = document.createElementNS( WSConstants.WSU_NS,WSConstants.EXPIRES_LN);
            expiresElement.setTextContent(validityPeriod[1]);
            element.appendChild(expiresElement);

            return new Timestamp(element);

        } catch (ParserConfigurationException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE,"SAMLTimeStampBuildError", null , e );
        } catch (WSSecurityException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE,"SAMLTimeStampBuildError", null , e );
        }
    }

    /**
     * Extract the URIs of the set of claims available in a SAML 1.0/1.1 assertion. This method will
     * iterate through the set of AttributeStatements available and extract the namespaces of the claim.
     * @param assertion SAML 1.0/1.1 assertion
     * @return  A TreeSet instance comprise of all the claims available in a SAML assertion
     */
    public static Set getClaims(SAMLAssertion assertion){
        Set claims = new TreeSet();
        Iterator statements = assertion.getStatements();
        // iterate over the statements
        while(statements.hasNext()){
            SAMLStatement statement = (SAMLStatement) statements.next();
            // if it is AttributeStatement, then extract the attributes
            if(statement instanceof SAMLAttributeStatement){
                Iterator attributes = ((SAMLAttributeStatement)statement).getAttributes();
                while(attributes.hasNext()){
                    SAMLAttribute attribute = (SAMLAttribute)attributes.next();
                    claims.add(attribute.getName());
                }
            }
        }
        return claims;
    }

    /**
     * Validate the signature of the SAML assertion
     * @param assertion SAML 1.0/1.1 assertion
     * @param sigCrypto Crypto object containing the certificate of the token issuer
     * @throws WSSecurityException if the token does not contain certificate information, the certificate
     *          of the issuer is not trusted or the signature is invalid.
     */
    public static void validateSignature(SAMLAssertion assertion, Crypto sigCrypto)
            throws WSSecurityException {
        Iterator x509Certificates = null;
        try {
            x509Certificates = assertion.getX509Certificates();
        } catch (SAMLException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "SAMLTokenInvalidX509Data");
        }

        try {
            if (x509Certificates.hasNext()) {
                X509Certificate x509Certificate = (X509Certificate) x509Certificates.next();

                // check whether the issuer's certificate is available in the signature crypto
                if (sigCrypto.getAliasForX509Cert(x509Certificate) != null) {
                    assertion.verify(x509Certificate);
                } else {
                    throw new WSSecurityException(WSSecurityException.FAILURE, "SAMLTokenUntrustedSignatureKey");
                }
            } else {
                throw new WSSecurityException(WSSecurityException.FAILURE, "SAMLTokenInvalidX509Data");
            }
        } catch (SAMLException e) {
            throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE,
                                          "SAMLTokenInvalidSignature");
        }
    }

    private static String[] getValidityPeriod(Element assertion){
        String[] validityPeriod = new String[2];
        for (Node currentChild = assertion.getFirstChild();
             currentChild != null;
             currentChild = currentChild.getNextSibling()
         ){
            if(WSConstants.SAML_CONDITION.equals(currentChild.getLocalName())
                    && WSConstants.SAML_NS.equals(currentChild.getNamespaceURI())){
                NamedNodeMap attributes = currentChild.getAttributes();
                for(int i=0; i < attributes.getLength(); i++){
                    Node attr = attributes.item(i);
                    if(WSConstants.SAML_NOT_BEFORE.equals(attr.getLocalName())){
                       validityPeriod[0] = attr.getNodeValue();
                    }
                    else if(WSConstants.SAML_NOT_AFTER.equals(attr.getLocalName())){
                        validityPeriod[1] = attr.getNodeValue();
                    }
                }

                break;
            }
        }

        return validityPeriod;
    }

}
