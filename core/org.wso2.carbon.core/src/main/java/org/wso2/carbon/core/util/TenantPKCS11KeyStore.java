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

package org.wso2.carbon.core.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Tenant key store whose key pair is held in a PKCS#11 token (HSM) shared by many tenants.
 * <p>
 * The persisted key store record does not contain key material. Its content is a small descriptor in
 * {@link Properties} format that names the token entry of the tenant: {@code label=<token entry label>}.
 * <p>
 * {@link #load(String, char[], byte[], String)} returns a read-only view over the token key store that exposes only
 * the tenant's entry, under the alias used for tenant keys (the tenant domain). Without the view, the token key store
 * would list the keys of all tenants. The view's provider is the token's provider, so callers that bind signers to
 * the key store provider sign on the token.
 */
public final class TenantPKCS11KeyStore {

    /**
     * Descriptor property that holds the label of the tenant's entry in the token.
     */
    public static final String LABEL_PROPERTY = "label";

    private TenantPKCS11KeyStore() {

    }

    /**
     * Build the descriptor that is persisted as the content of a PKCS11 tenant key store record.
     *
     * @param label Label of the tenant's entry in the token.
     * @return Descriptor bytes.
     */
    public static byte[] createDescriptor(String label) {

        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("Token entry label can't be empty.");
        }
        return (LABEL_PROPERTY + "=" + label.trim() + "\n").getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * Read the token entry label from a PKCS11 tenant key store descriptor.
     *
     * @param descriptor Descriptor bytes.
     * @return Token entry label.
     * @throws KeyStoreException If the descriptor is invalid.
     */
    public static String getLabel(byte[] descriptor) throws KeyStoreException {

        if (descriptor == null || descriptor.length == 0) {
            throw new KeyStoreException("Empty PKCS11 key store descriptor.");
        }
        Properties properties = new Properties();
        try (InputStream in = new ByteArrayInputStream(descriptor)) {
            properties.load(in);
        } catch (IOException | IllegalArgumentException e) {
            throw new KeyStoreException("Invalid PKCS11 key store descriptor.", e);
        }
        String label = properties.getProperty(LABEL_PROPERTY);
        if (StringUtils.isBlank(label)) {
            throw new KeyStoreException("PKCS11 key store descriptor has no '" + LABEL_PROPERTY + "'.");
        }
        return label.trim();
    }

    /**
     * Load the token through the given provider and return a read-only view that exposes only the tenant's entry.
     *
     * @param provider   Name of the registered JCA provider of the token (for example a SunPKCS11 instance).
     * @param pin        Token user PIN.
     * @param descriptor Persisted descriptor of the tenant key store.
     * @param alias      Alias under which the view exposes the tenant's entry.
     * @return Read-only key store view.
     * @throws KeyStoreException        If the token cannot be loaded or the tenant's entry does not exist.
     * @throws NoSuchProviderException  If the provider is not registered.
     */
    public static KeyStore load(String provider, char[] pin, byte[] descriptor, String alias)
            throws KeyStoreException, NoSuchProviderException {

        if (StringUtils.isBlank(alias)) {
            throw new KeyStoreException("Alias of the tenant key can't be empty.");
        }
        String label = getLabel(descriptor);
        KeyStore token = loadToken(provider, pin);
        if (!token.isKeyEntry(label)) {
            throw new KeyStoreException("No key entry with label '" + label + "' in the PKCS11 token of provider: " +
                    provider);
        }
        KeyStore view = new View(new SingleEntrySpi(token, alias, label), token);
        try {
            view.load(null, null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreException("Error while initializing the tenant key store view.", e);
        }
        return view;
    }

    /**
     * Delete the tenant's entry (private key and certificate) from the token.
     *
     * @param provider   Name of the registered JCA provider of the token.
     * @param pin        Token user PIN.
     * @param descriptor Persisted descriptor of the tenant key store.
     * @return True if an entry was deleted, false if it did not exist.
     * @throws KeyStoreException       If the entry cannot be deleted.
     * @throws NoSuchProviderException If the provider is not registered.
     */
    public static boolean deleteEntry(String provider, char[] pin, byte[] descriptor)
            throws KeyStoreException, NoSuchProviderException {

        String label = getLabel(descriptor);
        KeyStore token = loadToken(provider, pin);
        if (!token.containsAlias(label)) {
            return false;
        }
        token.deleteEntry(label);
        return true;
    }

    private static KeyStore loadToken(String provider, char[] pin) throws KeyStoreException, NoSuchProviderException {

        if (StringUtils.isBlank(provider)) {
            throw new KeyStoreException("A JCA provider is required for a PKCS11 key store.");
        }
        KeyStore token = KeystoreUtils.getKeystoreInstance(KeystoreUtils.StoreFileType.PKCS11.name(), provider);
        try {
            token.load(null, pin);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreException("Error while loading the PKCS11 token of provider: " + provider, e);
        }
        return token;
    }

    /**
     * Key store that uses the token's provider and type, backed by {@link SingleEntrySpi}.
     */
    private static final class View extends KeyStore {

        View(KeyStoreSpi spi, KeyStore token) {

            super(spi, token.getProvider(), token.getType());
        }
    }

    /**
     * Read-only key store SPI that exposes one entry of the token under a fixed alias.
     */
    private static final class SingleEntrySpi extends KeyStoreSpi {

        private static final String READ_ONLY = "The tenant PKCS11 key store view is read-only.";

        private final KeyStore token;
        private final String alias;
        private final String label;

        SingleEntrySpi(KeyStore token, String alias, String label) {

            this.token = token;
            this.alias = alias;
            this.label = label;
        }

        private boolean isTenantAlias(String requestedAlias) {

            return alias.equals(requestedAlias);
        }

        @Override
        public Key engineGetKey(String requestedAlias, char[] password)
                throws NoSuchAlgorithmException, UnrecoverableKeyException {

            if (!isTenantAlias(requestedAlias)) {
                return null;
            }
            try {
                return token.getKey(label, password);
            } catch (KeyStoreException e) {
                throw new IllegalStateException("Error while reading the tenant key from the PKCS11 token.", e);
            }
        }

        @Override
        public Certificate[] engineGetCertificateChain(String requestedAlias) {

            return isTenantAlias(requestedAlias) ? call(() -> token.getCertificateChain(label)) : null;
        }

        @Override
        public Certificate engineGetCertificate(String requestedAlias) {

            return isTenantAlias(requestedAlias) ? call(() -> token.getCertificate(label)) : null;
        }

        @Override
        public Date engineGetCreationDate(String requestedAlias) {

            return isTenantAlias(requestedAlias) ? call(() -> token.getCreationDate(label)) : null;
        }

        @Override
        public void engineSetKeyEntry(String requestedAlias, Key key, char[] password, Certificate[] chain)
                throws KeyStoreException {

            throw new KeyStoreException(READ_ONLY);
        }

        @Override
        public void engineSetKeyEntry(String requestedAlias, byte[] key, Certificate[] chain)
                throws KeyStoreException {

            throw new KeyStoreException(READ_ONLY);
        }

        @Override
        public void engineSetCertificateEntry(String requestedAlias, Certificate cert) throws KeyStoreException {

            throw new KeyStoreException(READ_ONLY);
        }

        @Override
        public void engineDeleteEntry(String requestedAlias) throws KeyStoreException {

            throw new KeyStoreException(READ_ONLY);
        }

        @Override
        public Enumeration<String> engineAliases() {

            return Collections.enumeration(Collections.singletonList(alias));
        }

        @Override
        public boolean engineContainsAlias(String requestedAlias) {

            return isTenantAlias(requestedAlias);
        }

        @Override
        public int engineSize() {

            return 1;
        }

        @Override
        public boolean engineIsKeyEntry(String requestedAlias) {

            return isTenantAlias(requestedAlias);
        }

        @Override
        public boolean engineIsCertificateEntry(String requestedAlias) {

            return false;
        }

        @Override
        public String engineGetCertificateAlias(Certificate cert) {

            Certificate tenantCertificate = engineGetCertificate(alias);
            return tenantCertificate != null && tenantCertificate.equals(cert) ? alias : null;
        }

        @Override
        public void engineStore(OutputStream stream, char[] password) throws IOException {

            throw new IOException(READ_ONLY);
        }

        @Override
        public void engineLoad(InputStream stream, char[] password) {

            // The view wraps an already loaded token key store.
        }

        private static <T> T call(TokenCall<T> tokenCall) {

            try {
                return tokenCall.call();
            } catch (KeyStoreException e) {
                throw new IllegalStateException("Error while reading the tenant entry from the PKCS11 token.", e);
            }
        }

        @FunctionalInterface
        private interface TokenCall<T> {

            T call() throws KeyStoreException;
        }
    }
}
