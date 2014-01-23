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

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.conversation.dkalgo.AlgoFactory;
import org.apache.ws.security.conversation.dkalgo.DerivationAlgorithm;
import org.apache.ws.security.message.token.DerivedKeyToken;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.saml.SAMLKeyInfo;
import org.apache.ws.security.saml.SAMLUtil;
import org.apache.ws.security.util.Base64;
import org.w3c.dom.Element;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Vector;

/**
 * The processor to process <code>wsc:DerivedKeyToken</code>.
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class DerivedKeyTokenProcessor implements Processor {

    private String id;
    private byte[] keyBytes;
    private DerivedKeyToken dkt;
    
    private byte[] secret;
    private int length;
    private int offset;
    private byte[] nonce;
    private String label;
    private String algorithm;
    
    public void handleToken(
        Element elem, 
        Crypto crypto, 
        Crypto decCrypto,
        CallbackHandler cb, 
        WSDocInfo wsDocInfo, 
        Vector returnResults,
        WSSConfig config
    ) throws WSSecurityException {
        
        // Deserialize the DKT
        dkt = new DerivedKeyToken(elem);
        this.extractSecret(wsDocInfo, dkt, cb, crypto);
        
        String tempNonce = dkt.getNonce();
        if (tempNonce == null) {
            throw new WSSecurityException("Missing wsc:Nonce value");
        }
        this.nonce = Base64.decode(tempNonce);
        this.length = dkt.getLength();
        this.label = dkt.getLabel();
        this.algorithm = dkt.getAlgorithm();
        this.id = dkt.getID();
        if (length > 0) {
            this.deriveKey();
            returnResults.add(
                0, 
                new WSSecurityEngineResult(WSConstants.DKT, 
                                           secret,
                                           keyBytes, 
                                           id, 
                                           null)
            );

        }
    }

    private void deriveKey() throws WSSecurityException{
        try {
            DerivationAlgorithm algo = AlgoFactory.getInstance(this.algorithm);
            byte[] labelBytes = null;
            if (label == null || label.length() == 0) {
                labelBytes = 
                    (ConversationConstants.DEFAULT_LABEL + ConversationConstants.DEFAULT_LABEL).getBytes("UTF-8");
            } else {
                labelBytes = this.label.getBytes("UTF-8");
            }
            
            byte[] seed = new byte[labelBytes.length + nonce.length];
            System.arraycopy(labelBytes, 0, seed, 0, labelBytes.length);
            System.arraycopy(nonce, 0, seed, labelBytes.length, nonce.length);
            
            this.keyBytes = algo.createKey(this.secret, seed, offset, length);
            
        } catch (Exception e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, null, null, e
            );
        }
    }

    /**
     * @param wsDocInfo
     * @param dkt
     * @throws WSSecurityException
     */
    private void extractSecret(
        WSDocInfo wsDocInfo, 
        DerivedKeyToken dkt, 
        CallbackHandler cb, 
        Crypto crypto
    ) throws WSSecurityException {
        SecurityTokenReference str = dkt.getSecurityTokenReference();
        if (str != null) {
            Processor processor;
            String uri = null;
            String keyIdentifierValueType = null;
            String keyIdentifierValue = null;
            
            if (str.containsReference()) {
                Reference ref = str.getReference();
                
                uri = ref.getURI();
                if (uri.charAt(0) == '#') {
                    uri = uri.substring(1);
                }
                processor = wsDocInfo.getProcessor(uri);
            } else {
                // Contains key identifier
                keyIdentifierValue = str.getKeyIdentifierValue();
                keyIdentifierValueType = str.getKeyIdentifierValueType();
                processor = wsDocInfo.getProcessor(keyIdentifierValue);
            }
            
            if (processor == null && uri != null) {
                // Now use the callback and get it
                this.secret = this.getSecret(cb, uri);
            } else if (processor == null && keyIdentifierValue != null
                && keyIdentifierValueType != null) {
                X509Certificate[] certs = str.getKeyIdentifier(crypto);
                if (certs == null || certs.length < 1 || certs[0] == null) {
                    this.secret = this.getSecret(cb, keyIdentifierValue, keyIdentifierValueType); 
                } else {
                    this.secret = this.getSecret(cb, crypto, certs);
                }
            } else if (processor instanceof UsernameTokenProcessor) {
                this.secret = ((UsernameTokenProcessor) processor).getDerivedKey(cb);
            } else if (processor instanceof EncryptedKeyProcessor) {
                this.secret = ((EncryptedKeyProcessor) processor).getDecryptedBytes();
            } else if (processor instanceof SecurityContextTokenProcessor) {
                this.secret = ((SecurityContextTokenProcessor) processor).getSecret();
            } else if (processor instanceof SAMLTokenProcessor) {
                SAMLTokenProcessor samlp = (SAMLTokenProcessor) processor;
                SAMLKeyInfo keyInfo = 
                    SAMLUtil.getSAMLKeyInfo(samlp.getSamlTokenElement(), crypto, cb);
                // TODO Handle malformed SAML tokens where they don't have the 
                // secret in them
                this.secret = keyInfo.getSecret();
            } else {
                throw new WSSecurityException(
                    WSSecurityException.FAILED_CHECK, "unsupportedKeyId"
                );
            }
        } else {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, "noReference");
        }
    }

    private byte[] getSecret(CallbackHandler cb, String id) throws WSSecurityException {
        if (cb == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCallback");
        }

        WSPasswordCallback callback = 
            new WSPasswordCallback(id, WSPasswordCallback.SECURITY_CONTEXT_TOKEN);
        try {
            Callback[] callbacks = new Callback[]{callback};
            cb.handle(callbacks);
        } catch (IOException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, 
                "noKey",
                new Object[] {id}, 
                e
            );
        } catch (UnsupportedCallbackException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE,
                "noKey",
                new Object[] {id}, 
                e
            );
        }

        return callback.getKey();
    }
    
    private byte[] getSecret(
        CallbackHandler cb, 
        String keyIdentifierValue, 
        String keyIdentifierType
    ) throws WSSecurityException {
        if (cb == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCallback");
        }
        
        WSPasswordCallback pwcb = 
            new WSPasswordCallback(
                keyIdentifierValue, null, keyIdentifierType, WSPasswordCallback.ENCRYPTED_KEY_TOKEN
            );
        try {
            Callback[] callbacks = new Callback[]{pwcb};
            cb.handle(callbacks);
        } catch (IOException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, 
                "noKey",
                new Object[] {id}, 
                e
            );
        } catch (UnsupportedCallbackException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, 
                "noKey",
                new Object[] {id}, 
                e
            );
        }
            
        return pwcb.getKey();
    }
    
    
    private byte[] getSecret(
        CallbackHandler cb,
        Crypto crypto,
        X509Certificate certs[]
    ) throws WSSecurityException {
        if (cb == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCallback");
        }
        
        String alias = crypto.getAliasForX509Cert(certs[0]);

        WSPasswordCallback pwCb = 
            new WSPasswordCallback(alias, WSPasswordCallback.DECRYPT);
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

        java.security.Key privateKey;
        try {
            privateKey = crypto.getPrivateKey(alias, password);
            return privateKey.getEncoded();
        } catch (Exception e) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, e);
        }
    }
    

    /**
     * Returns the wsu:Id of the DerivedKeyToken
     * @see org.apache.ws.security.processor.Processor#getId()
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return Returns the keyBytes.
     */
    public byte[] getKeyBytes() {
        return keyBytes;
    }
    
    /**
     * Get the derived key bytes for a given length
     * @return Returns the keyBytes.
     */
    public byte[] getKeyBytes(int len) throws WSSecurityException {
        this.length = len;
        this.deriveKey();
        return keyBytes;
    }
    
    /**
     * Return the DerivedKeyToken object
     */
    public DerivedKeyToken getDerivedKeyToken() {
        return dkt;
    }
    
}
