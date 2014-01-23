/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ws.security.saml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.processor.EncryptedKeyProcessor;
import org.apache.ws.security.util.Base64;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.KeyInfoConfirmationDataType;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialContextSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class SAML2Util {
    
    /**
     * Extract certificates or the key available in the SAMLAssertion
     *
     * @param elem
     * @return the SAML2 Key Info
     * @throws org.apache.ws.security.WSSecurityException
     *
     */

    public static boolean bootstrapped = false;

    public static void doBootstrap() throws WSSecurityException {
        if(!bootstrapped){
            try {
                DefaultBootstrap.bootstrap();
            } catch (ConfigurationException e) {
                throw new WSSecurityException("errorBootstrapping", e);
            }
            bootstrapped = true;
        }
    }
    public static SAML2KeyInfo getSAML2KeyInfo(Element elem, Crypto crypto,
                                              CallbackHandler cb) throws WSSecurityException {
        Assertion assertion;

        //build the assertion by unmarhalling the DOM element.
        try {
            doBootstrap();

            String keyInfoElementString = elem.toString();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new ByteArrayInputStream(keyInfoElementString.trim().getBytes()));
            Element element = document.getDocumentElement();
            // Check for duplicate saml:Assertion
			NodeList list = element.getElementsByTagNameNS( WSConstants.SAML2_NS,"Assertion");
			if (list != null && list.getLength() > 0) {
				throw new WSSecurityException("invalidSAMLSecurity");
			}

            UnmarshallerFactory unmarshallerFactory = Configuration
                    .getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory
                    .getUnmarshaller(element);
            assertion = (Assertion) unmarshaller
                    .unmarshall(element);
        } catch (UnmarshallingException e) {
            throw new WSSecurityException(
                    WSSecurityException.FAILURE, "Failure in unmarshelling the assertion", null, e);
        } catch (IOException e) {
            throw new WSSecurityException(
                    WSSecurityException.FAILURE, "Failure in unmarshelling the assertion", null, e);
        } catch (SAXException e) {
            throw new WSSecurityException(
                    WSSecurityException.FAILURE, "Failure in unmarshelling the assertion", null, e);
        } catch (ParserConfigurationException e) {
            throw new WSSecurityException(
                    WSSecurityException.FAILURE, "Failure in unmarshelling the assertion", null, e);
        }
        return getSAML2KeyInfo(assertion, crypto, cb);

    }

    public static SAML2KeyInfo getSAML2KeyInfo(Assertion assertion, Crypto crypto,
                                               CallbackHandler cb) throws WSSecurityException {

        //First ask the cb whether it can provide the secret
        WSPasswordCallback pwcb = new WSPasswordCallback(assertion.getID(), WSPasswordCallback.CUSTOM_TOKEN);
        if (cb != null) {
            try {
                cb.handle(new Callback[]{pwcb});
            } catch (Exception e1) {
                throw new WSSecurityException(WSSecurityException.FAILURE, "noKey",
                        new Object[]{assertion.getID()}, e1);
            }
        }

        byte[] key = pwcb.getKey();

        if (key != null) {
            return new SAML2KeyInfo(assertion, key);
        } else {
            // if the cb fails to provide the secret.
            try {
                // extract the subject
                Subject samlSubject = assertion.getSubject();
                if (samlSubject == null) {
                    throw new WSSecurityException(WSSecurityException.FAILURE,
                            "invalidSAML2Token", new Object[]{"for Signature (no Subject)"});
                }

                // extract the subject confirmation element from the subject
                SubjectConfirmation subjectConf = (SubjectConfirmation) samlSubject.getSubjectConfirmations().get(0);
                if (subjectConf == null) {
                    throw new WSSecurityException(WSSecurityException.FAILURE,
                            "invalidSAML2Token", new Object[]{"for Signature (no Subject Confirmation)"});
                }

                // Get the subject confirmation data, KeyInfoConfirmationDataType extends SubjectConfirmationData.
                KeyInfoConfirmationDataType scData = (KeyInfoConfirmationDataType) subjectConf.getSubjectConfirmationData();
                if (scData == null) {
                    throw new WSSecurityException(WSSecurityException.FAILURE,
                            "invalidSAML2Token", new Object[]{"for Signature (no Subject Confirmation Data)"});
                }

                // Get the SAML specific XML representation of the keyInfo object
                XMLObject KIElem = scData.getKeyInfos() != null ? (XMLObject) scData.getKeyInfos().get(0) : null;

                Element keyInfoElement;

                // Generate a DOM element from the XMLObject.
                if (KIElem != null) {

                    // Set the "javax.xml.parsers.DocumentBuilderFactory" system property to make sure the endorsed JAXP
                    // implementation is picked over the default jaxp impl shipped with the JDK.
                    String jaxpProperty = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
                    System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

                    MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
                    Marshaller marshaller = marshallerFactory.getMarshaller(KIElem);
                    keyInfoElement = marshaller.marshall(KIElem);

                    // Reset the sys. property to its previous value.
                    if (jaxpProperty == null) {
                        System.getProperties().remove("javax.xml.parsers.DocumentBuilderFactory");
                    } else {
                        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", jaxpProperty);
                    }

                } else {
                    throw new WSSecurityException(WSSecurityException.FAILURE,
                            "invalidSAML2Token", new Object[]{"for Signature (no key info element)"});
                }

                AttributeStatement attrStmt = assertion.getAttributeStatements().size() != 0 ?
                        (AttributeStatement) assertion.getAttributeStatements().get(0) : null;
                AuthnStatement authnStmt = assertion.getAuthnStatements().size() != 0 ?
                        (AuthnStatement) assertion.getAuthnStatements().get(0) : null;
                        
                boolean usePublicKey = false;

                // if an attr stmt is present, then it has a symmetric key.
                if (attrStmt != null) {
                    NodeList children = keyInfoElement.getChildNodes();
                    int len = children.getLength();

                    for (int i = 0; i < len; i++) {
                        Node child = children.item(i);
                        if (child.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        QName el = new QName(child.getNamespaceURI(), child.getLocalName());
                        if (el.equals(WSSecurityEngine.ENCRYPTED_KEY)) {

                            EncryptedKeyProcessor proc = new EncryptedKeyProcessor();
                            proc.handleEncryptedKey((Element) child, cb, crypto, null);

                            return new SAML2KeyInfo(assertion, proc.getDecryptedBytes());
                        } else if (el.equals(new QName(WSConstants.WST_NS, "BinarySecret"))) {
                            Text txt = (Text) child.getFirstChild();
                            return new SAML2KeyInfo(assertion, Base64.decode(txt.getData()));
                        } else if (el.equals(new QName(WSConstants.SIG_NS, "X509Data"))) {
			    X509Certificate[] certs = null;
                            try {
                                KeyInfo ki = new KeyInfo(keyInfoElement, null);

                                if (ki.containsX509Data()) {
                                    org.apache.xml.security.keys.content.X509Data data = ki
                                            .itemX509Data(0);
                                    XMLX509Certificate certElem = null;
                                    if (data != null && data.containsCertificate()) {
                                        certElem = data.itemCertificate(0);
                                    }
                                    if (certElem != null) {
                                        X509Certificate cert = certElem
                                                .getX509Certificate();
                                        certs = new X509Certificate[1];
                                        certs[0] = cert;
                                        return new SAML2KeyInfo(assertion, certs);
                                    }
                                }

                            } catch (XMLSecurityException e3) {
                                throw new WSSecurityException(
                                        WSSecurityException.FAILURE,
                                        "invalidSAMLSecurity",
                                        new Object[] { "cannot get certificate (key holder)" },
                                        e3);
                            }
                        }
                    }

                }

                // If an authn stmt is presentm then it has a public key.
				if (authnStmt != null || usePublicKey) {

					X509Certificate[] certs = null;
					try {
						KeyInfo ki = new KeyInfo(keyInfoElement, null);

						if (ki.containsX509Data()) {
							org.apache.xml.security.keys.content.X509Data data = ki
									.itemX509Data(0);
							XMLX509Certificate certElem = null;
							if (data != null && data.containsCertificate()) {
								certElem = data.itemCertificate(0);
							}
							if (certElem != null) {
								X509Certificate cert = certElem
										.getX509Certificate();
								certs = new X509Certificate[1];
								certs[0] = cert;
								return new SAML2KeyInfo(assertion, certs);
							}
						}

					} catch (XMLSecurityException e3) {
						throw new WSSecurityException(
								WSSecurityException.FAILURE,
								"invalidSAMLSecurity",
								new Object[] { "cannot get certificate (key holder)" },
								e3);
					}

				} else {
					throw new WSSecurityException(WSSecurityException.FAILURE,
							"invalidSAMLSecurity",
							new Object[] { "cannot get certificate or key " });
				}


                throw new WSSecurityException(WSSecurityException.FAILURE,
                                              "invalidSAMLSecurity",
                        new Object[]{"cannot get certificate or key "});

            } catch (MarshallingException e) {
                throw new WSSecurityException(WSSecurityException.FAILURE,
                        "Failed marshalling the SAML Assertion", null, e);
            }
        }
    }

    /**
        * Create a timestamp object from the SAML 2.0 Assertion
        * @param assertion
        * @return
        * @throws WSSecurityException
        */
       public static Timestamp getTimestampForSAMLAssertion(Assertion assertion) throws WSSecurityException {

        Subject subject = assertion.getSubject();
        SubjectConfirmationData scData = ((SubjectConfirmation) subject.getSubjectConfirmations().get(0)).getSubjectConfirmationData();

        String notBefore = null;
        String notOnOrAfter = null;

        // read the validity period from Conditions, if fails read it from SCData
        if (assertion.getConditions() != null) {
            Conditions conditions = assertion.getConditions();
            if (conditions.getNotBefore() != null) {
                notBefore = conditions.getNotBefore().toString();
            }
            if (conditions.getNotOnOrAfter() != null) {
                notOnOrAfter = conditions.getNotOnOrAfter().toString();
            }
        } else if (scData != null) {
            if (scData.getNotBefore() != null) {
                notBefore = scData.getNotBefore().toString();
            }
            if (scData.getNotOnOrAfter() != null) {
                notOnOrAfter = scData.getNotOnOrAfter().toString();
            }
        }

        if (notBefore == null || notOnOrAfter == null) {
            return null;
        }

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            Document document = dbFactory.newDocumentBuilder().newDocument();
            Element element = document.createElement("SAMLTimestamp");

            Element createdElement = document.createElementNS(WSConstants.WSU_NS, WSConstants.CREATED_LN);
            createdElement.setTextContent(notBefore);
            element.appendChild(createdElement);

            Element expiresElement = document.createElementNS(WSConstants.WSU_NS, WSConstants.EXPIRES_LN);
            expiresElement.setTextContent(notOnOrAfter);
            element.appendChild(expiresElement);

            return new Timestamp(element);

        } catch (ParserConfigurationException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "SAMLTimeStampBuildError", null, e);
        } catch (WSSecurityException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "SAMLTimeStampBuildError", null, e);
        }
    }

    /**
     * Extract the URIs of the set of claims available in a SAML 1.0/1.1 assertion. This method will
     * iterate through the set of AttributeStatements available and extract the namespaces of the claim.
     * @param assertion SAML 2.0 Assertion
     * @return TreeSet of claims contained in the SAML 2.0 assertion
     */
    public static Set getClaims(Assertion assertion){
        Set claimSet = new TreeSet();
        List attributeStatements = assertion.getAttributeStatements();
        for(int attrStmtIndex = 0 ; attrStmtIndex < attributeStatements.size(); attrStmtIndex++){
            AttributeStatement attributeStatement = (AttributeStatement) attributeStatements.get(
                    attrStmtIndex);
            List attributes  = attributeStatement.getAttributes();
            for(int attrIndex = 0 ; attrIndex < attributes.size(); attrIndex++){
                claimSet.add(((Attribute)attributes.get(attrIndex)).getName());
            }
        }
        return claimSet;
    }

    /**
     * Validate the signature of the SAML assertion
     * @param assertion SAML 2.0 assertion
     * @param crypto Crypto object containing the certificate of the token issuer
     * @throws WSSecurityException if the token does not contain certificate information, the certificate
     *          of the issuer is not trusted or the signature is invalid.
     */
    public static void validateSignature(Assertion assertion, Crypto crypto)
            throws WSSecurityException {
        // Get the <ds:X509Data/> elements
        List x509Data = assertion.getSignature().getKeyInfo().getX509Datas();
        if (x509Data != null && x509Data.size() > 0) {
            // Pick the first <ds:X509Data/> element
            X509Data x509Cred = (X509Data) x509Data.get(0);
            // Get the <ds:X509Certificate/> elements
            List x509Certs = x509Cred.getX509Certificates();
            if (x509Certs != null && x509Certs.size() > 0) {
                // Pick the first <ds:X509Certificate/> element
                org.opensaml.xml.signature.X509Certificate cert = (org.opensaml.xml.signature.X509Certificate)
                        x509Certs.get(0);
                try {
                    // Instantiate a java.security.cert.X509Certificate object out of the
                    // base64 decoded byte[] of the certificate
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    java.security.cert.X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(
                            new ByteArrayInputStream(org.opensaml.xml.util.Base64.decode(cert.getValue())));
                    // if this certificate is available in the key store represented by SigCrypto
                    if (crypto.getAliasForX509CertThumb(calculateThumbPrint(x509Certificate)) != null) {
                        class X509CredentialImpl implements X509Credential {
                            private PublicKey publicKey = null;

                            public X509CredentialImpl(X509Certificate cert) {
                                publicKey = cert.getPublicKey();
                            }

                            public X509Certificate getEntityCertificate() {
                                return null;
                            }

                            public Collection<X509Certificate> getEntityCertificateChain() {
                                return null;
                            }

                            public Collection<X509CRL> getCRLs() {
                                return null;
                            }

                            public String getEntityId() {
                                return null;
                            }

                            public UsageType getUsageType() {
                                return null;
                            }

                            public Collection<String> getKeyNames() {
                                return null;
                            }

                            public PublicKey getPublicKey() {
                                return publicKey;
                            }

                            public PrivateKey getPrivateKey() {
                                return null;
                            }

                            public SecretKey getSecretKey() {
                                return null;
                            }

                            public CredentialContextSet getCredentalContextSet() {
                                return null;
                            }

                            public Class<? extends Credential> getCredentialType() {
                                return null;
                            }
                        }
                        // validate the signature
                        SignatureValidator signatureValidator = new SignatureValidator(
                                new X509CredentialImpl(x509Certificate));
                        signatureValidator.validate(assertion.getSignature());
                    }
                    else{
                        throw new WSSecurityException(WSSecurityException.FAILURE, "SAMLTokenUntrustedSignatureKey");
                    }

                } catch (java.security.cert.CertificateException e) {
                    throw new WSSecurityException("SAMLTokenErrorGeneratingX509CertInstance", e);
                } catch (ValidationException e) {
                    throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE,
                                                  "SAMLTokenInvalidSignature");
                }
            }
            else{
                throw new WSSecurityException(WSSecurityException.FAILURE, "SAMLTokenInvalidX509Data");
            }
        }
        else{
            throw new WSSecurityException(WSSecurityException.FAILURE, "SAMLTokenInvalidX509Data");
        }
    }

    /**
     * Calculate the thumbprint-sha1 value of a certificate
     * @param x509Certificate X509Certificate instance
     * @return Calculated thumbprint-sha1 value
     */
    private static byte[] calculateThumbPrint(X509Certificate x509Certificate){
        byte[] thumbPrintValue = new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(x509Certificate.getEncoded());
            thumbPrintValue = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return thumbPrintValue;
    }
}

