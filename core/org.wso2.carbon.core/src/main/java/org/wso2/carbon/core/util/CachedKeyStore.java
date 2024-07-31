/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.core.util;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.HashMap;

/**
 * A proxy class that provides cached access to the contents of a KeyStore.
 */
public class CachedKeyStore {

    private final KeyStore keyStore;
    private final HashMap<String, Key> privateKeyMap = new HashMap<>();
    private final HashMap<String, Certificate> certMap = new HashMap<>();
    private final HashMap<String, Certificate[]> certChainMap = new HashMap<>();

    public CachedKeyStore(KeyStore keyStore) {

        this.keyStore = keyStore;
    }

    /**
     * Returns the cached KeyStore instance.
     *
     * @return The KeyStore.
     */
    public KeyStore getKeyStore() {

        return keyStore;
    }

    /**
     * Retrieves the private key from the KeyStore using the given alias and password.
     * The key is cached after the first retrieval.
     *
     * @param alias    The alias of the private key.
     * @param password The password to access the private key.
     * @return The default PrivateKey.
     */
     public Key getKey(String alias, char[] password) {

         return privateKeyMap.computeIfAbsent(alias, key -> {
             try {
                 return keyStore.getKey(alias, password);
             } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
                 throw new RuntimeException(e);
             }
         });
    }

    /**
     * Retrieves the certificate from the KeyStore using the given alias.
     * The certificate is cached after the first retrieval.
     *
     * @param alias The alias of the certificate.
     * @return The default X509Certificate.
     */
    public Certificate getCertificate(String alias) {

        return certMap.computeIfAbsent(alias, key -> {
            try {
                return keyStore.getCertificate(alias);
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Retrieves the certificate chain from the KeyStore using the given alias.
     * The certificate chain is cached after the first retrieval.
     *
     * @param alias The alias of the certificate chain.
     * @return An array of Certificates representing the certificate chain.
     */
    public Certificate[] getCertificateChain(String alias) {

        return certChainMap.computeIfAbsent(alias, key -> {
            try {
                return keyStore.getCertificateChain(alias);
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
