/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.user.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test JCA provider that offers a "PKCS11" key store backed by an in-memory key store, to simulate a PKCS#11 token
 * shared by many tenants. It behaves like SunPKCS11:
 * <ul>
 * <li>The first {@code load(null, pin)} logs in to the token and fails with a wrong PIN. Once the token is logged in,
 * {@code load} accepts any PIN, because SunPKCS11 does not log in again.</li>
 * <li>Key passwords are ignored, and changes apply to the token immediately.</li>
 * <li>A loaded key store lists the entries that existed when it was loaded, and those it added itself.</li>
 * <li>An alias that contains {@code /} stands for an entry whose certificate label is not unique (SunPKCS11 names it
 * {@code <label>/<issuer>/<serial number>}). While the token has one, it refuses all changes.</li>
 * </ul>
 */
public class InMemoryTokenProvider extends Provider {

    private static final long serialVersionUID = 1L;

    private final transient KeyStore token;
    private final transient char[] tokenPassword;
    private final char[] pin;
    private volatile boolean loggedIn;
    private final AtomicInteger loadCount = new AtomicInteger();

    /**
     * @param token         In-memory key store that acts as the token contents.
     * @param tokenPassword Password of the in-memory key store and its key entries.
     * @param pin           PIN that {@code load(null, pin)} must present.
     */
    public InMemoryTokenProvider(String name, KeyStore token, char[] tokenPassword, char[] pin) {

        super(name, "1.0", "In-memory PKCS11-like token for tests");
        this.token = token;
        this.tokenPassword = tokenPassword.clone();
        this.pin = pin.clone();
        putService(new Service(this, "KeyStore", "PKCS11", TokenSpi.class.getName(), null, null) {
            @Override
            public Object newInstance(Object constructorParameter) {

                return new TokenSpi((InMemoryTokenProvider) getProvider());
            }
        });
    }

    /**
     * @return The in-memory key store that holds the token contents.
     */
    public KeyStore getToken() {

        return token;
    }

    /**
     * @return True if a {@code load} with the right PIN has logged in to the token.
     */
    public boolean isLoggedIn() {

        return loggedIn;
    }

    /**
     * @return Number of times a key store of this token has been loaded.
     */
    public int getLoadCount() {

        return loadCount.get();
    }

    private static final class TokenSpi extends KeyStoreSpi {

        private final InMemoryTokenProvider provider;
        // Like SunPKCS11, a loaded key store lists the token entries that existed when it was loaded.
        private final Set<String> listed = ConcurrentHashMap.newKeySet();

        TokenSpi(InMemoryTokenProvider provider) {

            this.provider = provider;
        }

        private KeyStore token() {

            return provider.token;
        }

        private boolean isListed(String alias) {

            return alias != null && listed.contains(alias);
        }

        private void checkWrite() throws KeyStoreException {

            for (String alias : listed) {
                if (alias.contains("/")) {
                    throw new KeyStoreException("This PKCS11KeyStore does not support write capabilities");
                }
            }
        }

        private <T> T read(String alias, TokenRead<T> tokenRead) {

            if (!isListed(alias)) {
                return null;
            }
            try {
                return tokenRead.read(token());
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Key engineGetKey(String alias, char[] password) {

            return read(alias, token -> token.getKey(alias, provider.tokenPassword));
        }

        @Override
        public Certificate[] engineGetCertificateChain(String alias) {

            return read(alias, token -> token.getCertificateChain(alias));
        }

        @Override
        public Certificate engineGetCertificate(String alias) {

            return read(alias, token -> token.getCertificate(alias));
        }

        @Override
        public Date engineGetCreationDate(String alias) {

            return read(alias, token -> token.getCreationDate(alias));
        }

        @Override
        public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
                throws KeyStoreException {

            checkWrite();
            token().setKeyEntry(alias, key, provider.tokenPassword, chain);
            listed.add(alias);
        }

        @Override
        public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {

            throw new KeyStoreException("Not supported.");
        }

        @Override
        public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {

            checkWrite();
            token().setCertificateEntry(alias, cert);
            listed.add(alias);
        }

        @Override
        public void engineDeleteEntry(String alias) throws KeyStoreException {

            checkWrite();
            token().deleteEntry(alias);
            listed.remove(alias);
        }

        @Override
        public Enumeration<String> engineAliases() {

            return Collections.enumeration(new ArrayList<>(listed));
        }

        @Override
        public boolean engineContainsAlias(String alias) {

            return isListed(alias);
        }

        @Override
        public int engineSize() {

            return listed.size();
        }

        @Override
        public boolean engineIsKeyEntry(String alias) {

            Boolean keyEntry = read(alias, token -> token.isKeyEntry(alias));
            return Boolean.TRUE.equals(keyEntry);
        }

        @Override
        public boolean engineIsCertificateEntry(String alias) {

            Boolean certificateEntry = read(alias, token -> token.isCertificateEntry(alias));
            return Boolean.TRUE.equals(certificateEntry);
        }

        @Override
        public String engineGetCertificateAlias(Certificate cert) {

            for (String alias : listed) {
                if (cert.equals(engineGetCertificate(alias))) {
                    return alias;
                }
            }
            return null;
        }

        @Override
        public void engineStore(OutputStream stream, char[] password) {

            // The token is updated immediately; nothing to store.
        }

        @Override
        public void engineLoad(InputStream stream, char[] password) throws IOException {

            // Like SunPKCS11, the PIN is checked only when the token is not logged in yet.
            if (!provider.loggedIn) {
                if (password == null || !Arrays.equals(password, provider.pin)) {
                    throw new IOException("Token login failed: wrong PIN.");
                }
                provider.loggedIn = true;
            }
            provider.loadCount.incrementAndGet();
            try {
                listed.clear();
                listed.addAll(Collections.list(token().aliases()));
            } catch (KeyStoreException e) {
                throw new IOException(e);
            }
        }

        @FunctionalInterface
        private interface TokenRead<T> {

            T read(KeyStore token) throws GeneralSecurityException;
        }
    }

    /**
     * Load the in-memory token contents from a PKCS12 file.
     *
     * @param in       PKCS12 content.
     * @param password Password of the file and its entries.
     * @return Loaded key store.
     */
    public static KeyStore loadPKCS12(InputStream in, char[] password) throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(in, password);
        return keyStore;
    }
}
