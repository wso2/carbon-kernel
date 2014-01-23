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

package org.apache.ws.security.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.CustomTokenPrincipal;
import org.apache.ws.security.KerberosTokenPrincipal;
import org.apache.ws.security.PublicKeyCallback;
import org.apache.ws.security.PublicKeyPrincipal;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDataRef;
import org.apache.ws.security.WSDerivedKeyTokenPrincipal;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSDocInfoStore;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.EnvelopeIdResolver;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.DerivedKeyToken;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.message.token.X509Security;
import org.apache.ws.security.saml.SAML2KeyInfo;
import org.apache.ws.security.saml.SAML2Util;
import org.apache.ws.security.saml.SAMLKeyInfo;
import org.apache.ws.security.saml.SAMLUtil;
import org.apache.ws.security.transform.STRTransform;
import org.apache.ws.security.transform.STRTransformUtil;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.Transforms;
import org.opensaml.SAMLAssertion;
import org.opensaml.saml2.core.Assertion;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;

import java.security.Principal;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class SignatureProcessor implements Processor {
    private static Log log = LogFactory.getLog(SignatureProcessor.class.getName());
    private static Log tlog = LogFactory.getLog("org.apache.ws.security.TIME");
    
    private String signatureId;
    
    private Vector returnResults;

    private X509Certificate[] certificates;
    
    private byte[] secretKey;
    
    private int secretKeyLength = WSConstants.WSE_DERIVED_KEY_LEN;
    
    private String signatureMethod;
    
    private String c14nMethod;

    public void handleToken(
        Element elem, 
        Crypto crypto, 
        Crypto decCrypto, 
        CallbackHandler cb, 
        WSDocInfo wsDocInfo, 
        Vector returnResults, 
        WSSConfig wsc
    ) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Found signature element");
        }
        boolean remove = WSDocInfoStore.store(wsDocInfo);
        X509Certificate[] returnCert = new X509Certificate[1];
        Set returnElements = new HashSet();
        List protectedElements = new java.util.ArrayList();
        byte[][] signatureValue = new byte[1][];
        Principal lastPrincipalFound = null;
        this.returnResults = returnResults;
        secretKeyLength = wsc.getSecretKeyLength();
        signatureMethod = c14nMethod = null;
        certificates = null;
        
        try {
            lastPrincipalFound = 
                verifyXMLSignature(
                    elem, crypto, returnCert, returnElements,
                    protectedElements, signatureValue, cb,
                    wsDocInfo
                );
        } catch (WSSecurityException ex) {
            throw ex;
        } finally {
            if (remove) {
                WSDocInfoStore.delete(wsDocInfo);
            }
        }
        byte[] storedKey = null;
        if (secretKey != null && secretKey.length > 0) {
            storedKey = new byte[secretKey.length];
            for (int i = 0; i < secretKey.length; i++) {
                storedKey[i] = secretKey[i];
            }
        }
        if (lastPrincipalFound instanceof WSUsernameTokenPrincipal) {
            WSSecurityEngineResult result = new WSSecurityEngineResult(
                    WSConstants.UT_SIGN, lastPrincipalFound, null,
                    returnElements, protectedElements, signatureValue[0]);
            result.put(WSSecurityEngineResult.TAG_SIGNATURE_METHOD, signatureMethod);
            result.put(WSSecurityEngineResult.TAG_CANONICALIZATION_METHOD, c14nMethod);
            result.put(WSSecurityEngineResult.TAG_DECRYPTED_KEY, storedKey);
            returnResults.add(
                0, 
                result
            );
        }else if(lastPrincipalFound instanceof KerberosTokenPrincipal) {
            returnResults.add(
                    0, 
                    new WSSecurityEngineResult(
                        WSConstants.KERBEROS_SIGN, 
                        lastPrincipalFound,
                        returnCert[0], 
                        returnElements, 
                        protectedElements, 
                        signatureValue[0]
                    )
                ); 
        } else {
            WSSecurityEngineResult result = new WSSecurityEngineResult(
                    WSConstants.SIGN, lastPrincipalFound, returnCert[0], 
                    returnElements, protectedElements, signatureValue[0]);
            result.put(WSSecurityEngineResult.TAG_SIGNATURE_METHOD, signatureMethod);
            result.put(WSSecurityEngineResult.TAG_CANONICALIZATION_METHOD, c14nMethod);
            result.put(WSSecurityEngineResult.TAG_X509_CERTIFICATES, certificates);
            result.put(WSSecurityEngineResult.TAG_DECRYPTED_KEY, storedKey);
            returnResults.add(
                0, 
                result
            );
        }
        signatureId = elem.getAttributeNS(null, "Id");
        secretKey = null;
    }

    /**
     * Verify the WS-Security signature.
     * 
     * The functions at first checks if then <code>KeyInfo</code> that is
     * contained in the signature contains standard X509 data. If yes then
     * get the certificate data via the standard <code>KeyInfo</code> methods.
     * 
     * Otherwise, if the <code>KeyInfo</code> info does not contain X509 data, check
     * if we can find a <code>wsse:SecurityTokenReference</code> element. If yes, the next
     * step is to check how to get the certificate. Two methods are currently supported
     * here:
     * <ul>
     * <li> A URI reference to a binary security token contained in the <code>wsse:Security
     * </code> header.  If the dereferenced token is
     * of the correct type the contained certificate is extracted.
     * </li>
     * <li> Issuer name an serial number of the certificate. In this case the method
     * looks up the certificate in the keystore via the <code>crypto</code> parameter.
     * </li>
     * </ul>
     * 
     * The methods checks is the certificate is valid and calls the
     * {@link org.apache.xml.security.signature.XMLSignature#checkSignatureValue(X509Certificate) 
     * verification} function.
     *
     * @param elem        the XMLSignature DOM Element.
     * @param crypto      the object that implements the access to the keystore and the
     *                    handling of certificates.
     * @param returnCert  verifyXMLSignature stores the certificate in the first
     *                    entry of this array. The caller may then further validate
     *                    the certificate
     * @param returnElements verifyXMLSignature adds the wsu:ID attribute values for
     *               the signed elements to this Set
     * @param cb CallbackHandler instance to extract key passwords
     * @return the subject principal of the validated X509 certificate (the
     *         authenticated subject). The calling function may use this
     *         principal for further authentication or authorization.
     * @throws WSSecurityException
     */
    protected Principal verifyXMLSignature(
        Element elem,
        Crypto crypto,
        X509Certificate[] returnCert,
        Set returnElements,
        List protectedElements,
        byte[][] signatureValue,
        CallbackHandler cb,
        WSDocInfo wsDocInfo
    ) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Verify XML Signature");
        }
        long t0 = 0, t1 = 0, t2 = 0;
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }

        XMLSignature sig = null;
        try {
            sig = new XMLSignature(elem, null);
        } catch (XMLSecurityException e2) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_CHECK, "noXMLSig", null, e2
            );
        }

        sig.addResourceResolver(EnvelopeIdResolver.getInstance());

        KeyInfo info = sig.getKeyInfo();
        UsernameToken ut = null;
        DerivedKeyToken dkt = null;
        SAMLKeyInfo samlKi = null;
        SAML2KeyInfo saml2Ki = null;
        String customTokenId = null;
        java.security.PublicKey publicKey = null;
        X509Certificate[] certs = null;
        boolean validateCertificateChain = false;
        boolean kerbThumbPrint = false;
        X509Certificate cert = null;

        try {
            cert = info.getX509Certificate();
        } catch (KeyResolverException ignore) {
            if (log.isDebugEnabled()) {
                log.debug(ignore);
            }
        }

        if (info != null && info.containsKeyValue()) {
            try {
                publicKey = info.getPublicKey();
            } catch (Exception ex) {
                throw new WSSecurityException(ex.getMessage(), ex);
            }
        } else if (info != null && cert != null) {
            certs = new X509Certificate[]{cert};
        } else if (info != null) {
            Node node = 
                WSSecurityUtil.getDirectChild(
                    info.getElement(),
                    SecurityTokenReference.SECURITY_TOKEN_REFERENCE,
                    WSConstants.WSSE_NS
                );
            if (node == null) {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY, "unsupportedKeyInfo"
                );
            }
            SecurityTokenReference secRef = new SecurityTokenReference((Element) node);
            //
            // Here we get some information about the document that is being
            // processed, in particular the crypto implementation, and already
            // detected BST that may be used later during dereferencing.
            //
            if (secRef.containsReference()) {
                org.apache.ws.security.message.token.Reference ref = secRef.getReference();
                Element krbToken = secRef.getTokenElement(elem.getOwnerDocument(), wsDocInfo, cb);
                
                if (krbToken
						.getAttribute("ValueType")
						.equals(
								"http://docs.oasis-open.org/wss/oasis-wss-kerberos-token-profile-1.1#GSS_Kerberosv5_AP_REQ")) {
					
                	KerberosTokenProcessor krbp = new KerberosTokenProcessor(this.returnResults);
					return krbp.verifyXMLSignature(elem, crypto, returnCert,
							returnElements, protectedElements, signatureValue,
							cb);
				}
                
                String uri = ref.getURI();
                if (uri.charAt(0) == '#') {
                    uri = uri.substring(1);
                }
                Processor processor = wsDocInfo.getProcessor(uri);
                if (processor == null) {
                    Element token = secRef.getTokenElement(elem.getOwnerDocument(), wsDocInfo, cb);
                    //
                    // at this point check token type: Binary, SAML, EncryptedKey, Custom
                    //
                    QName el = new QName(token.getNamespaceURI(), token.getLocalName());
                    if (el.equals(WSSecurityEngine.binaryToken)) {
                        certs = getCertificatesTokenReference(token, crypto);
                        if (certs != null && certs.length > 1) {
                            validateCertificateChain = true;
                        }
                    } else if (el.equals(WSSecurityEngine.SAML_TOKEN)) {
                        samlKi = SAMLUtil.getSAMLKeyInfo(token, crypto, cb);
                        certs = samlKi.getCerts();
                        secretKey = samlKi.getSecret();
					} else if (el.equals(WSSecurityEngine.SAML2_TOKEN)) {
						saml2Ki = SAML2Util.getSAML2KeyInfo(token, crypto, cb);
						certs = saml2Ki.getCerts();
						secretKey = saml2Ki.getSecret();

					} else if (el.equals(WSSecurityEngine.ENCRYPTED_KEY)){
                        if (crypto == null) {
                            throw new WSSecurityException(
                                WSSecurityException.FAILURE, "noSigCryptoFile"
                            );
                        }
                        EncryptedKeyProcessor encryptKeyProcessor = new EncryptedKeyProcessor();
                        encryptKeyProcessor.handleEncryptedKey(token, cb, crypto);
                        secretKey = encryptKeyProcessor.getDecryptedBytes();
                    } else {
                        // Try custom token through callback handler
                        // try to find a custom token
                        String id = secRef.getReference().getURI();
                        if (id.charAt(0) == '#') {
                            id = id.substring(1);
                        }
                        WSPasswordCallback pwcb = 
                            new WSPasswordCallback(id, WSPasswordCallback.CUSTOM_TOKEN);
                        try {
                            Callback[] callbacks = new Callback[]{pwcb};
                            cb.handle(callbacks);
                        } catch (Exception e) {
                            throw new WSSecurityException(
                                    WSSecurityException.FAILURE,
                                    "noPassword", 
                                    new Object[] {id}, 
                                    e
                            );
                        }

                        secretKey = pwcb.getKey();
                        customTokenId = id;
                        if (secretKey == null) {
                            throw new WSSecurityException(
                                    WSSecurityException.INVALID_SECURITY,
                                    "unsupportedKeyInfo", 
                                    new Object[]{el.toString()}
                            );
                        }
                    }
                } else if (processor instanceof UsernameTokenProcessor) {
                    ut = ((UsernameTokenProcessor)processor).getUt();
                    if (ut.isDerivedKey()) {
                        secretKey = ut.getDerivedKey();
                    } else {
                        secretKey = ut.getSecretKey(secretKeyLength);
                    }
                } else if (processor instanceof BinarySecurityTokenProcessor) {
                    certs = ((BinarySecurityTokenProcessor)processor).getCertificates();
                    if (certs != null && certs.length > 1) {
                        validateCertificateChain = true;
                    }
                } else if (processor instanceof EncryptedKeyProcessor) {
                    EncryptedKeyProcessor ekProcessor = (EncryptedKeyProcessor)processor;
                    secretKey = ekProcessor.getDecryptedBytes();
                    customTokenId = ekProcessor.getId();
                } else if (processor instanceof SecurityContextTokenProcessor) {
                    SecurityContextTokenProcessor sctProcessor = 
                        (SecurityContextTokenProcessor)processor;
                    secretKey = sctProcessor.getSecret();
                    customTokenId = sctProcessor.getIdentifier();
                } else if (processor instanceof DerivedKeyTokenProcessor) {
                    DerivedKeyTokenProcessor dktProcessor = 
                        (DerivedKeyTokenProcessor) processor;
                    String signatureMethodURI = sig.getSignedInfo().getSignatureMethodURI();
                    dkt = dktProcessor.getDerivedKeyToken();
                    int keyLength = (dkt.getLength() > 0) ? dkt.getLength() : 
                        WSSecurityUtil.getKeyLength(signatureMethodURI);
                    
                    secretKey = dktProcessor.getKeyBytes(keyLength);
                } else if (processor instanceof SAMLTokenProcessor) {
                    if (crypto == null) {
                        throw new WSSecurityException(
                            WSSecurityException.FAILURE, "noSigCryptoFile"
                        );
                    }
                    SAMLTokenProcessor samlp = (SAMLTokenProcessor) processor;
                    samlKi = SAMLUtil.getSAMLKeyInfo(samlp.getSamlTokenElement(), crypto, cb);
                    certs = samlKi.getCerts();
                    secretKey = samlKi.getSecret();
                    publicKey = samlKi.getPublicKey();
				} else if (processor instanceof SAML2TokenProcessor) {
					if (crypto == null)
						throw new WSSecurityException(0, "noSigCryptoFile");
					SAML2TokenProcessor samlp = (SAML2TokenProcessor) processor;
					saml2Ki = SAML2Util.getSAML2KeyInfo(
							samlp.getSamlTokenElement(), crypto, cb);
					certs = saml2Ki.getCerts();
					secretKey = saml2Ki.getSecret();
				}
            } else if (secRef.containsX509Data() || secRef.containsX509IssuerSerial()) {
                certs = secRef.getX509IssuerSerial(crypto);
            } else if (secRef.containsKeyIdentifier()) {
                if (secRef.getKeyIdentifierValueType().equals(SecurityTokenReference.ENC_KEY_SHA1_URI)) {
                    String id = secRef.getKeyIdentifierValue();
                    WSPasswordCallback pwcb = 
                        new WSPasswordCallback(
                            id,
                            null,
                            SecurityTokenReference.ENC_KEY_SHA1_URI,
                            WSPasswordCallback.ENCRYPTED_KEY_TOKEN
                        );
                    try {
                        Callback[] callbacks = new Callback[]{pwcb};
                        cb.handle(callbacks);
                    } catch (Exception e) {
                        throw new WSSecurityException(
                            WSSecurityException.FAILURE,
                            "noPassword", 
                            new Object[] {id}, 
                            e
                        );
                    }
                    secretKey = pwcb.getKey();
                } else if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(secRef.getKeyIdentifierValueType())) { 
                    Element token = 
                        secRef.getKeyIdentifierTokenElement(elem.getOwnerDocument(), wsDocInfo, cb);

                    samlKi = SAMLUtil.getSAMLKeyInfo(token, crypto, cb);
                    certs = samlKi.getCerts();
                    secretKey = samlKi.getSecret();
                    publicKey = samlKi.getPublicKey();
				} else if (WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(secRef
						.getKeyIdentifierValueType())) {
					Element token = secRef.getKeyIdentifierTokenElement(
							elem.getOwnerDocument(), wsDocInfo, cb);
					saml2Ki = SAML2Util.getSAML2KeyInfo(token, crypto, cb);
					certs = saml2Ki.getCerts();
					secretKey = saml2Ki.getSecret();
                } else {
                    certs = secRef.getKeyIdentifier(crypto);
                    if (certs == null && secRef.containsKerberosThumbprint()) {
						secretKey = secRef.getKerberosSession().getSessionKey()
								.getEncoded();
						kerbThumbPrint = true;
					}
                }
            } else {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY,
                    "unsupportedKeyInfo", 
                    new Object[]{node.toString()}
                );
            }
        } else {
            if (crypto == null) {
                throw new WSSecurityException(WSSecurityException.FAILURE, "noSigCryptoFile");
            }
            if (crypto.getDefaultX509Alias() != null) {
                certs = crypto.getCertificates(crypto.getDefaultX509Alias());
            } else {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY, "unsupportedKeyInfo"
                );
            }
        }
        if (tlog.isDebugEnabled()) {
            t1 = System.currentTimeMillis();
        }
        if ((certs == null || certs.length == 0 || certs[0] == null) 
            && secretKey == null
            && publicKey == null
            && !kerbThumbPrint) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK);
        }
        if (certs != null) {
            try {
                for (int i = 0; i < certs.length; i++) {
                    certs[i].checkValidity();
                }
            } catch (CertificateExpiredException e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILED_CHECK, "invalidCert", null, e
                );
            } catch (CertificateNotYetValidException e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILED_CHECK, "invalidCert", null, e
                );
            }
        }
        //
        // Delegate verification of a public key to a Callback Handler
        //
        if (publicKey != null) {
            PublicKeyCallback pwcb = 
                new PublicKeyCallback(publicKey);
            try {
                Callback[] callbacks = new Callback[]{pwcb};
                cb.handle(callbacks);
                if (!pwcb.isVerified()) {
                    throw new WSSecurityException(
                        WSSecurityException.FAILED_AUTHENTICATION, null, null, null
                    );
                }
            } catch (Exception e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILED_AUTHENTICATION, null, null, e
                );
            }
        }
        try {
            boolean signatureOk = false;
            if (certs != null) {
                signatureOk = sig.checkSignatureValue(certs[0]);
            } else if (publicKey != null) {
                signatureOk = sig.checkSignatureValue(publicKey);
            } else {
                signatureOk = sig.checkSignatureValue(sig.createSecretKey(secretKey));
            }
            if (signatureOk) {
                if (tlog.isDebugEnabled()) {
                    t2 = System.currentTimeMillis();
                    tlog.debug(
                        "Verify: total= " + (t2 - t0) + ", prepare-cert= " + (t1 - t0) 
                        + ", verify= " + (t2 - t1)
                    );
                }
                signatureValue[0] = sig.getSignatureValue();
                //
                // Now dig into the Signature element to get the elements that
                // this Signature covers. Build the QName of these Elements and
                // return them to caller
                //
                SignedInfo si = sig.getSignedInfo();
                int numReferences = si.getLength();
                for (int i = 0; i < numReferences; i++) {
                    Reference siRef;
                    try {
                        siRef = si.item(i);
                    } catch (XMLSecurityException e3) {
                        throw new WSSecurityException(
                            WSSecurityException.FAILED_CHECK, null, null, e3
                        );
                    }
                    String uri = siRef.getURI();
                    if (uri != null && !"".equals(uri)) {
                        
                        Element se = null;
                        try {
                            Transforms transforms = siRef.getTransforms();
                            for (int j = 0; j < transforms.getLength(); j++) {
                                Transform transform = transforms.item(j);
                                // We have some transforming to do before we can 
                                // determine the protected element.
                                if (STRTransform.implementedTransformURI
                                        .equals(transform.getURI())) {
                                    
                                    XMLSignatureInput signatureInput = 
                                        siRef.getContentsBeforeTransformation();
                                    
                                    if (signatureInput.isElement()) {
                                        // The signature was already validated,
                                        // meaning that this element was already
                                        // parsed.  We can therefore be pretty
                                        // confident that this constructor will work.
                                        SecurityTokenReference secTokenRef = 
                                            new SecurityTokenReference(
                                                    (Element) signatureInput.getSubNode());
                                        
                                        // Use the utility to extract the element (or
                                        // generate a new one in some cases) from the
                                        // message.
                                        se = STRTransformUtil.dereferenceSTR(
                                                transform.getDocument(),
                                                secTokenRef, wsDocInfo);
                                    } else {
                                        // The internal impl of Reference changed.
                                        // We expect it to return the signature input
                                        // based on a node/element.
                                        throw new WSSecurityException(
                                                WSSecurityException.FAILURE);
                                    }
                                }
                            }
                        }
                        catch (XMLSecurityException e) {
                            log.warn("Error processing signature coverage elements.", e);
                            throw new WSSecurityException(WSSecurityException.FAILURE);
                        }
                        
                        if (se == null) {
                            se = WSSecurityUtil.getElementByWsuId(elem.getOwnerDocument(), uri);
                        }
                        if (se == null) {
                            se = WSSecurityUtil.getElementByGenId(elem.getOwnerDocument(), uri);
                        }
                        if (se == null) {
                            throw new WSSecurityException(WSSecurityException.FAILED_CHECK);
                        }
                        WSDataRef ref = new WSDataRef(uri);
                        ref.setWsuId(uri);
                        ref.setName(new QName(se.getNamespaceURI(), se.getLocalName()));
                        ref.setProtectedElement(se);
                        ref.setXpath(ReferenceListProcessor.getXPath(se));
                        ref.setAlgorithm(si.getSignatureMethodURI());
                        ref.setDigestAlgorithm(siRef.getMessageDigestAlgorithm().getAlgorithmURI());
                        protectedElements.add(ref);
                        returnElements.add(WSSecurityUtil.getIDFromReference(uri));
                    } else {
                       // This is the case where the signed element is identified 
                       // by a transform such as XPath filtering
                       // We add the complete reference element to the return 
                       // elements
                       returnElements.add(siRef); 
                    }
                }
                
                // Algorithms used for signature and c14n
                signatureMethod = si.getSignatureMethodURI();
                c14nMethod = si.getCanonicalizationMethodURI();
                
                if (certs != null) {
                    returnCert[0] = certs[0];
                    if (validateCertificateChain) {
                        certificates = certs;
                    }
                    return certs[0].getSubjectX500Principal();
                } else if (publicKey != null) {
                    return new PublicKeyPrincipal(publicKey);
                } else if (ut != null) {
                    WSUsernameTokenPrincipal principal = 
                        new WSUsernameTokenPrincipal(ut.getName(), ut.isHashed());
                    principal.setNonce(ut.getNonce());
                    principal.setPassword(ut.getPassword());
                    principal.setCreatedTime(ut.getCreated());
                    return principal;
                } else if (dkt != null) {
                    WSDerivedKeyTokenPrincipal principal = new WSDerivedKeyTokenPrincipal(dkt.getID());
                    principal.setNonce(dkt.getNonce());
                    principal.setLabel(dkt.getLabel());
                    principal.setLength(dkt.getLength());
                    principal.setOffset(dkt.getOffset());
                    String basetokenId = null;
                    SecurityTokenReference securityTokenReference = dkt.getSecurityTokenReference();
                    if (securityTokenReference.containsReference()) {
                        basetokenId = securityTokenReference.getReference().getURI();
                        if (basetokenId.charAt(0) == '#') {
                            basetokenId = basetokenId.substring(1);
                        }
                    } else {
                        // KeyIdentifier
                        basetokenId = securityTokenReference.getKeyIdentifierValue();
                    }
                    principal.setBasetokenId(basetokenId);
                    return principal;
                } else if (samlKi != null) {
                    final SAMLAssertion assertion = samlKi.getAssertion();
                    CustomTokenPrincipal principal = new CustomTokenPrincipal(assertion.getId());
                    principal.setTokenObject(assertion);
                    return principal;
                    
				} else if (saml2Ki != null) {
					Assertion assertion = saml2Ki.getAssertion();
					CustomTokenPrincipal principal = new CustomTokenPrincipal(
							assertion.getID());
					principal.setTokenObject(assertion);
					return principal;
				} else if (secretKey != null) {
                    // This is the custom key scenario
                    CustomTokenPrincipal principal = new CustomTokenPrincipal(customTokenId);
                    return principal;
                } else {
                    throw new WSSecurityException("Cannot determine principal");
                }
            } else {
                throw new WSSecurityException(WSSecurityException.FAILED_CHECK);
            }
        } catch (XMLSignatureException e1) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_CHECK, null, null, e1
            );
        }
    }

    
    /**
     * Extracts the certificate(s) from the Binary Security token reference.
     *
     * @param elem The element containing the binary security token. This is
     *             either X509 certificate(s) or a PKIPath.
     * @return an array of X509 certificates
     * @throws WSSecurityException
     */
    public X509Certificate[] getCertificatesTokenReference(Element elem, Crypto crypto)
        throws WSSecurityException {
        if (crypto == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noSigCryptoFile");
        }
        BinarySecurity token = createSecurityToken(elem);
        if (token instanceof PKIPathSecurity) {
            return ((PKIPathSecurity) token).getX509Certificates(false, crypto);
        } else if (token instanceof X509Security) {
            X509Certificate cert = ((X509Security) token).getX509Certificate(crypto);
            return new X509Certificate[]{cert};
        }
        return null;
    }


    /**
     * Checks the <code>element</code> and creates appropriate binary security object.
     *
     * @param element The XML element that contains either a <code>BinarySecurityToken
     *                </code> or a <code>PKIPath</code> element. Other element types a not
     *                supported
     * @return the BinarySecurity object, either a <code>X509Security</code> or a
     *         <code>PKIPathSecurity</code> object.
     * @throws WSSecurityException
     */
    private BinarySecurity createSecurityToken(Element element) throws WSSecurityException {

        String type = element.getAttribute("ValueType");
        if (X509Security.X509_V3_TYPE.equals(type)) {
            X509Security x509 = new X509Security(element);
            return (BinarySecurity) x509;
        } else if (PKIPathSecurity.getType().equals(type)) {
            PKIPathSecurity pkiPath = new PKIPathSecurity(element);
            return (BinarySecurity) pkiPath;
        }
        throw new WSSecurityException(
            WSSecurityException.UNSUPPORTED_SECURITY_TOKEN,
            "unsupportedBinaryTokenType", 
            new Object[]{type}
        );
    }

    public String getId() {
        return signatureId;
    }

}
