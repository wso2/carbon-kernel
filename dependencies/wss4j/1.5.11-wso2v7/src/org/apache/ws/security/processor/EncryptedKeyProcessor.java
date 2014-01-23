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
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDataRef;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.message.token.X509Security;
import org.apache.ws.security.saml.SAML2KeyInfo;
import org.apache.ws.security.saml.SAML2Util;
import org.apache.ws.security.saml.SAMLKeyInfo;
import org.apache.ws.security.saml.SAMLUtil;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Vector;

public class EncryptedKeyProcessor implements Processor {
    private static Log log = LogFactory.getLog(EncryptedKeyProcessor.class.getName());
    private static Log tlog =
            LogFactory.getLog("org.apache.ws.security.TIME");
    private byte[] encryptedEphemeralKey;
    
    private byte[] decryptedBytes = null;
    
    private String encryptedKeyId = null;
    private X509Certificate cert = null;
    
    private String encryptedKeyTransportMethod = null;
    
    private WSDocInfo docInfo = null;

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
            log.debug("Found encrypted key element");
        }
        if (decCrypto == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noDecCryptoFile");
        }
        if (cb == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCallback");
        }
        docInfo = wsDocInfo;
        ArrayList dataRefUris = handleEncryptedKey((Element) elem, cb, decCrypto);
        encryptedKeyId = elem.getAttributeNS(null, "Id");
        
        WSSecurityEngineResult result = new WSSecurityEngineResult(
                    WSConstants.ENCR, 
                    this.decryptedBytes,
                    this.encryptedEphemeralKey,
                    this.encryptedKeyId, 
                    dataRefUris,
                    cert);
        
        result.put(WSSecurityEngineResult.TAG_ENCRYPTED_KEY_TRANSPORT_METHOD, this.encryptedKeyTransportMethod);
        
        returnResults.add(
            0, 
            result
        );
    }

    public ArrayList handleEncryptedKey(
            Element xencEncryptedKey,
            CallbackHandler cb, 
            Crypto crypto
    ) throws WSSecurityException {
        return handleEncryptedKey(xencEncryptedKey, cb, crypto, null);
    }

    public ArrayList handleEncryptedKey(
        Element xencEncryptedKey,
        PrivateKey privatekey
    ) throws WSSecurityException {
        return handleEncryptedKey(xencEncryptedKey, null, null, privatekey);
    }

    public ArrayList handleEncryptedKey(
        Element xencEncryptedKey,
        CallbackHandler cb, 
        Crypto crypto, 
        PrivateKey privateKey
    ) throws WSSecurityException {
        long t0 = 0, t1 = 0, t2 = 0;
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }
        // need to have it to find the encrypted data elements in the envelope
        Document doc = xencEncryptedKey.getOwnerDocument();

        // lookup xenc:EncryptionMethod, get the Algorithm attribute to determine
        // how the key was encrypted. Then check if we support the algorithm

        Node tmpE = null;    // short living Element used for lookups only
        tmpE = 
            (Element) WSSecurityUtil.getDirectChild(
                (Node) xencEncryptedKey, "EncryptionMethod", WSConstants.ENC_NS
            );
        if (tmpE != null) {
            this.encryptedKeyTransportMethod = ((Element) tmpE).getAttribute("Algorithm");
        }
        if (this.encryptedKeyTransportMethod == null) {
            throw new WSSecurityException(WSSecurityException.UNSUPPORTED_ALGORITHM, "noEncAlgo");
        }
        Cipher cipher = WSSecurityUtil.getCipherInstance(this.encryptedKeyTransportMethod);
        //
        // Well, we can decrypt the session (symmetric) key. Now lookup CipherValue, this is the 
        // value of the encrypted session key (session key usually is a symmetrical key that encrypts
        // the referenced content). This is a 2-step lookup
        //
        Element xencCipherValue = null;
        tmpE = 
            (Element) WSSecurityUtil.getDirectChild(
                (Node) xencEncryptedKey, "CipherData", WSConstants.ENC_NS
            );
        if (tmpE != null) {
            xencCipherValue = 
                (Element) WSSecurityUtil.getDirectChild(tmpE, "CipherValue", WSConstants.ENC_NS);
        }
        if (xencCipherValue == null) {
            throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "noCipher");
        }

        if (privateKey == null) {
            Element keyInfo = 
                (Element) WSSecurityUtil.getDirectChild(
                    (Node) xencEncryptedKey, "KeyInfo", WSConstants.SIG_NS
                );
            String alias;
            if (keyInfo != null) {
                Element secRefToken = 
                    (Element) WSSecurityUtil.getDirectChild(
                        keyInfo, "SecurityTokenReference", WSConstants.WSSE_NS
                    );
                //
                // EncryptedKey must a a STR as child of KeyInfo, KeyName  
                // valid only for EncryptedData
                //
                //  if (secRefToken == null) {
                //      secRefToken = (Element) WSSecurityUtil.getDirectChild(keyInfo,
                //              "KeyName", WSConstants.SIG_NS);
                //  }
                if (secRefToken == null) {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY, "noSecTokRef"
                    );
                }
                SecurityTokenReference secRef = new SecurityTokenReference(secRefToken);
                //
                // Well, at this point there are several ways to get the key.
                // Try to handle all of them :-).
                //
                alias = null;
                //
                // handle X509IssuerSerial here. First check if all elements are available,
                // get the appropriate data, check if all data is available.
                // If all is ok up to that point, look up the certificate alias according
                // to issuer name and serial number.
                // This method is recommended by OASIS WS-S specification, X509 profile
                //
                if (secRef.containsX509Data() || secRef.containsX509IssuerSerial()) {
                    alias = secRef.getX509IssuerSerialAlias(crypto);
                    if (log.isDebugEnabled()) {
                        log.debug("X509IssuerSerial alias: " + alias);
                    }
                }
                //
                // If wsse:KeyIdentifier found, then the public key of the attached cert was used to
                // encrypt the session (symmetric) key that encrypts the data. Extract the certificate
                // using the BinarySecurity token (was enhanced to handle KeyIdentifier too).
                // This method is _not_ recommended by OASIS WS-S specification, X509 profile
                //
                else if (secRef.containsKeyIdentifier()) {
                    X509Certificate[] certs = null;
					if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(secRef
							.getKeyIdentifierValueType())) {
						Element token = secRef.getKeyIdentifierTokenElement(
								doc, docInfo, cb);

						if (crypto == null) {
							throw new WSSecurityException(
									WSSecurityException.FAILURE,
									"noSigCryptoFile");
						}
						SAMLKeyInfo samlKi = SAMLUtil.getSAMLKeyInfo(token,
								crypto, cb);
						certs = samlKi.getCerts();
					} else if (WSConstants.WSS_SAML2_KI_VALUE_TYPE
							.equals(secRef.getKeyIdentifierValueType())) {
						Element token = secRef.getKeyIdentifierTokenElement(
								doc, docInfo, cb);
						if (crypto == null) {
							throw new WSSecurityException(0, "noSigCryptoFile");
						}
						SAML2KeyInfo samlKi = SAML2Util.getSAML2KeyInfo(token,
								crypto, cb);
						certs = samlKi.getCerts();
					} else {
						certs = secRef.getKeyIdentifier(crypto);
					}
                    if (certs == null || certs.length < 1 || certs[0] == null) {
                        throw new WSSecurityException(
                            WSSecurityException.FAILURE,
                            "noCertsFound", 
                            new Object[] {"decryption (KeyId)"}
                        );
                    }
                    //
                    // Here we have the certificate. Now find the alias for it. Needed to identify
                    // the private key associated with this certificate
                    //
                    alias = crypto.getAliasForX509Cert(certs[0]);
                    cert = certs[0];
                    if (log.isDebugEnabled()) {
                        log.debug("cert: " + certs[0]);
                        log.debug("KeyIdentifier Alias: " + alias);
                    }
                } else if (secRef.containsReference()) {
                    Element bstElement = secRef.getTokenElement(doc, null, cb);

                    // at this point ... check token type: Binary
                    QName el = 
                        new QName(bstElement.getNamespaceURI(), bstElement.getLocalName());
                    if (el.equals(WSSecurityEngine.binaryToken)) {
                        X509Security token = new X509Security(bstElement);
                        String value = bstElement.getAttribute(WSSecurityEngine.VALUE_TYPE);
                        if (!X509Security.X509_V3_TYPE.equals(value) || (token == null)) {
                            throw new WSSecurityException(
                                WSSecurityException.UNSUPPORTED_SECURITY_TOKEN,
                                "unsupportedBinaryTokenType",
                                new Object[] {"for decryption (BST)"}
                            );
                        }
                        cert = token.getX509Certificate(crypto);
                        if (cert == null) {
                            throw new WSSecurityException(
                                WSSecurityException.FAILURE,
                                "noCertsFound", 
                                new Object[] {"decryption"}
                            );
                        }
                        //
                        // Here we have the certificate. Now find the alias for it. Needed to identify
                        // the private key associated with this certificate
                        //
                        alias = crypto.getAliasForX509Cert(cert);
                        if (log.isDebugEnabled()) {
                            log.debug("BST Alias: " + alias);
                        }
                    } else {
                        throw new WSSecurityException(
                            WSSecurityException.UNSUPPORTED_SECURITY_TOKEN,
                            "unsupportedBinaryTokenType",
                            null
                        );
                    }
                    //
                    // The following code is somewhat strange: the called crypto method gets
                    // the keyname and searches for a certificate with an issuer's name that is
                    // equal to this keyname. No serialnumber is used - IMHO this does
                    // not identifies a certificate. In addition neither the WSS4J encryption
                    // nor signature methods use this way to identify a certificate. Because of that
                    // the next lines of code are disabled.  
                    //
                  // } else if (secRef.containsKeyName()) {
                  //    alias = crypto.getAliasForX509Cert(secRef.getKeyNameValue());
                  //    if (log.isDebugEnabled()) {
                  //        log.debug("KeyName alias: " + alias);
                  //    }
                } else {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY, "unsupportedKeyId"
                    );
                }
            } else if (crypto.getDefaultX509Alias() != null) {
                alias = crypto.getDefaultX509Alias();
            } else {
                throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "noKeyinfo");
            }
            //
            // At this point we have all information necessary to decrypt the session
            // key:
            // - the Cipher object intialized with the correct methods
            // - The data that holds the encrypted session key
            // - the alias name for the private key
            //
            // Now use the callback here to get password that enables
            // us to read the private key
            //
            WSPasswordCallback pwCb = new WSPasswordCallback(alias, WSPasswordCallback.DECRYPT);
            try {
                Callback[] callbacks = new Callback[]{pwCb};
                cb.handle(callbacks);
            } catch (IOException e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "noPassword",
                    new Object[]{alias}, 
                    e
                );
            } catch (UnsupportedCallbackException e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "noPassword",
                    new Object[]{alias}, 
                    e
                );
            }
            String password = pwCb.getPassword();
            if (password == null) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "noPassword", new Object[]{alias}
                );
            }

            try {
                privateKey = crypto.getPrivateKey(alias, password);
            } catch (Exception e) {
                throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, e);
            }
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (Exception e1) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, e1);
        }

        try {
            encryptedEphemeralKey = getDecodedBase64EncodedData(xencCipherValue);
            decryptedBytes = cipher.doFinal(encryptedEphemeralKey);
        } catch (IllegalStateException e2) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, e2);
        } catch (IllegalBlockSizeException e2) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, e2);
        } catch (BadPaddingException e2) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, e2);
        }

        if (tlog.isDebugEnabled()) {
            t1 = System.currentTimeMillis();
        }

        // At this point we have the decrypted session (symmetric) key. According
        // to W3C XML-Enc this key is used to decrypt _any_ references contained in
        // the reference list
        // Now lookup the references that are encrypted with this key
        //
        Element refList = 
            (Element) WSSecurityUtil.getDirectChild(
                (Node) xencEncryptedKey, "ReferenceList", WSConstants.ENC_NS
            );
        ArrayList dataRefs = new ArrayList();
        if (refList != null) {
            for (tmpE = refList.getFirstChild();
                 tmpE != null; 
                 tmpE = tmpE.getNextSibling()
            ) {
                if (tmpE.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                if (!tmpE.getNamespaceURI().equals(WSConstants.ENC_NS)) {
                    continue;
                }
                if (tmpE.getLocalName().equals("DataReference")) {                   
                    String dataRefURI = ((Element) tmpE).getAttribute("URI");
                    if (dataRefURI.charAt(0) == '#') {
                        dataRefURI = dataRefURI.substring(1);
                    }
                    WSDataRef dataRef = decryptDataRef(doc, dataRefURI, decryptedBytes);
                    dataRefs.add(dataRef);
                }
            }
            return dataRefs;
        }

        if (tlog.isDebugEnabled()) {
            t2 = System.currentTimeMillis();
            tlog.debug(
                "XMLDecrypt: total= " + (t2 - t0) + ", get-sym-key= " + (t1 - t0) 
                + ", decrypt= " + (t2 - t1)
            );
        }
        
        return null;
    }

    /**
     * Method getDecodedBase64EncodedData
     *
     * @param element
     * @return a byte array containing the decoded data
     * @throws WSSecurityException
     */
    public static byte[] getDecodedBase64EncodedData(Element element) throws WSSecurityException {
        StringBuffer sb = new StringBuffer();
        NodeList children = element.getChildNodes();
        int iMax = children.getLength();
        for (int i = 0; i < iMax; i++) {
            Node curr = children.item(i);
            if (curr.getNodeType() == Node.TEXT_NODE) {
                sb.append(((Text) curr).getData());
            }
        }
        String encodedData = sb.toString();
        return Base64.decode(encodedData);
    }

    /**
     * Decrypt an EncryptedData element referenced by dataRefURI
     */
    private WSDataRef decryptDataRef(
        Document doc, 
        String dataRefURI, 
        byte[] decryptedData
    ) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("found data reference: " + dataRefURI);
        }
        //
        // Find the encrypted data element referenced by dataRefURI
        //
        Element encryptedDataElement = 
            ReferenceListProcessor.findEncryptedDataElement(doc, dataRefURI);
        //
        // Prepare the SecretKey object to decrypt EncryptedData
        //
        String symEncAlgo = X509Util.getEncAlgo(encryptedDataElement);
        SecretKey symmetricKey = 
            WSSecurityUtil.prepareSecretKey(symEncAlgo, decryptedData);

        return ReferenceListProcessor.decryptEncryptedData(
            doc, dataRefURI, encryptedDataElement, symmetricKey, symEncAlgo
        );
    }
    
    /**
     * Get the Id of the encrypted key element.
     * 
     * @return The Id string
     */
    public String getId() {
        return encryptedKeyId;
    }
    
    
    /**
     * Get the decrypted key.
     * 
     * The encrypted key element contains an encrypted session key. The
     * security functions use the session key to encrypt contents of the message
     * with symmetrical encryption methods.
     *  
     * @return The decrypted key.
     */
    public byte[] getDecryptedBytes() {
        return decryptedBytes;
    }

    public byte[] getEncryptedEphemeralKey() {
        return encryptedEphemeralKey;
    }
  
}
