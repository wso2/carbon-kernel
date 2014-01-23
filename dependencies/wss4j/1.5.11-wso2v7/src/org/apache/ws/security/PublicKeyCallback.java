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

package org.apache.ws.security;

import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.security.auth.callback.Callback;


/**
 */
public class PublicKeyCallback implements Callback {

    private java.security.PublicKey publicKey;
    private boolean verified = false;
    
    public PublicKeyCallback(java.security.PublicKey publicKey) {
        this.publicKey = publicKey;
    }
    
    public void setPublicKey(java.security.PublicKey publicKey) {
        this.publicKey = publicKey;
    }
    
    public java.security.PublicKey getPublicKey() {
        return publicKey;
    }
    
    public void setVerified(boolean b) {
        verified = b;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    /**
     * Evaluate whether a given public key should be trusted.
     * Essentially, this amounts to checking to see if there is a certificate in the keystore,
     * whose public key matches the transmitted public key.
     */
    public boolean verifyTrust(
        java.security.KeyStore keyStore
    ) throws WSSecurityException {
        //
        // If the public key is null, do not trust the signature
        //
        if (publicKey == null || keyStore == null) {
            return false;
        }
        
        //
        // Search the keystore for the transmitted public key (direct trust)
        //
        try {
            for (Enumeration e = keyStore.aliases(); e.hasMoreElements();) {
                String alias = (String) e.nextElement();
                Certificate[] certs = keyStore.getCertificateChain(alias);
                Certificate cert;
                if (certs == null || certs.length == 0) {
                    // no cert chain, so lets check if getCertificate gives us a result.
                    cert = keyStore.getCertificate(alias);
                    if (cert == null) {
                        continue;
                    }
                } else {
                    cert = certs[0];
                }
                if (!(cert instanceof X509Certificate)) {
                    continue;
                }
                X509Certificate x509cert = (X509Certificate) cert;
                if (publicKey.equals(x509cert.getPublicKey())) {
                    verified = true;
                    return true;
                }
            }
        } catch (KeyStoreException e) {
            return false;
        }
        return false;
    }
}


