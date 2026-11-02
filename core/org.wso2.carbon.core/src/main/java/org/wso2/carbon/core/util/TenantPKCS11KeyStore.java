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
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.ProviderException;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Tenant key store whose key pair is held in a PKCS#11 token (HSM) shared by many tenants.
 * <p>
 * The persisted key store record does not contain key material. Its content is a small descriptor in
 * {@link Properties} format that names the token entry of the tenant: {@code label=<token entry label>}. The label is
 * derived from the tenant's unique ID ({@link #labelFor(String)}), which is never reused, so a new tenant can't get the
 * entry of a deleted tenant. {@link #load} and {@link #deleteEntry} refuse a descriptor whose label does not belong to
 * the tenant. Token entry labels must be unique on the token.
 * <p>
 * The token PIN is not a per-tenant secret: once the token is logged in, SunPKCS11 accepts any PIN. Callers pass the
 * PIN and provider of the tenant key store token from the server configuration, not from the tenant's record.
 * <p>
 * {@link #load} returns a read-only view that exposes only the tenant's entry, under the alias used for tenant keys
 * (the tenant domain). The view's provider is the token's provider, so callers that bind signers to the key store
 * provider sign on the token. The view holds the tenant's key handle and certificate chain, read once when it is
 * created, and no reference to the token key store.
 * <p>
 * Loading a SunPKCS11 key store lists every key and certificate on the token, so its cost grows with the number of
 * tenants on the token. One loaded token key store is therefore kept per provider and used to create all tenant views.
 * It lists the entries that existed when it was loaded, so it is loaded again when a tenant's label is not found, for
 * example for a tenant whose key pair was generated after the load. A label that stays missing causes at most one
 * reload per {@link #MISSING_LABEL_RELOAD_INTERVAL_MILLIS}.
 * <p>
 * A server key store (the primary key store or a custom key store) can be held in the same token as the tenant key
 * pairs. {@link #excludeTenantEntries(KeyStore)} gives the server a view of such a token without the tenant entries,
 * so that they are not used or published as server keys.
 */
public final class TenantPKCS11KeyStore {

    /**
     * Descriptor property that holds the label of the tenant's entry in the token.
     */
    public static final String LABEL_PROPERTY = "label";

    /**
     * Prefix of the token entry label of a tenant key pair. The tenant's unique ID follows it.
     */
    public static final String LABEL_PREFIX = "wso2-tenant-";

    /**
     * Minimum time between two reloads of the token key store for the same missing label.
     */
    public static final long MISSING_LABEL_RELOAD_INTERVAL_MILLIS = 10_000L;

    // Labels are written to the descriptor as is, so only characters that need no escaping are allowed.
    private static final Pattern LABEL_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");

    // SunPKCS11 separates the parts of the alias it gives an entry whose certificate label is not unique.
    private static final String SHARED_LABEL_ALIAS_SEPARATOR = "/";

    // Loaded token key store per provider name, used to create the tenant views of that token.
    private static final Map<String, KeyStore> TOKENS = new ConcurrentHashMap<>();

    // Time of the last reload caused by a missing label, per provider and label.
    private static final Map<String, Long> MISSING_LABEL_RELOADS = new ConcurrentHashMap<>();

    private TenantPKCS11KeyStore() {

    }

    /**
     * Get the token entry label of a tenant key pair. It is based on the tenant's unique ID, so it does not change on
     * a tenant rename, it is unique per tenant, and it is not given to a later tenant that gets the same tenant ID.
     *
     * @param tenantUniqueId Unique ID (UUID) of the tenant.
     * @return Token entry label.
     */
    public static String labelFor(String tenantUniqueId) {

        if (StringUtils.isBlank(tenantUniqueId)) {
            throw new IllegalArgumentException("The tenant unique ID can't be empty.");
        }
        return LABEL_PREFIX + tenantUniqueId.trim();
    }

    /**
     * Check whether a token alias belongs to a tenant key pair. This includes the alias SunPKCS11 gives an entry whose
     * label is not unique ({@code <label>/<issuer>/<serial number>}), which also starts with the tenant label.
     *
     * @param alias Token alias.
     * @return True if the alias starts with {@link #LABEL_PREFIX}.
     */
    public static boolean isTenantTokenAlias(String alias) {

        return alias != null && alias.startsWith(LABEL_PREFIX);
    }

    /**
     * Get a view of a server key store held in a PKCS#11 token that hides the tenant key pairs on the token. The
     * token of {@code [keystore.tenant]} can be the token of the primary key store or of a custom key store. Without
     * this view, code that lists the server key store (for example the super tenant's JWKS) would see every tenant's
     * key pair. Tenant entries can't be read, added, replaced or deleted through the view. Other entries are read
     * from and written to the token as before.
     *
     * @param token Loaded PKCS11 key store of the token.
     * @return Key store with the provider and type of the token, without the tenant entries.
     * @throws KeyStoreException If the view can't be created.
     */
    public static KeyStore excludeTenantEntries(KeyStore token) throws KeyStoreException {

        KeyStore view = new View(new ServerEntriesSpi(token), token);
        try {
            view.load(null, null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreException("Error while initializing the server PKCS11 key store view.", e);
        }
        return view;
    }

    /**
     * Build the descriptor that is persisted as the content of a PKCS11 tenant key store record.
     *
     * @param label Label of the tenant's entry in the token.
     * @return Descriptor bytes.
     */
    public static byte[] createDescriptor(String label) {

        if (label == null || !LABEL_PATTERN.matcher(label).matches()) {
            throw new IllegalArgumentException("Invalid token entry label: " + label + ". Allowed characters: " +
                    "letters, digits, '.', '_' and '-'.");
        }
        return (LABEL_PROPERTY + "=" + label + "\n").getBytes(StandardCharsets.ISO_8859_1);
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
     * Read the tenant's entry from the token through the given provider and return a read-only view that exposes only
     * that entry.
     *
     * @param provider       Name of the registered JCA provider of the token (for example a SunPKCS11 instance).
     * @param pin            Token user PIN.
     * @param descriptor     Persisted descriptor of the tenant key store.
     * @param tenantUniqueId Unique ID of the tenant that owns the key store. The descriptor must name its label.
     * @param alias          Alias under which the view exposes the tenant's entry.
     * @return Read-only key store view.
     * @throws KeyStoreException       If the descriptor does not belong to the tenant, the token cannot be loaded,
     *                                 or the tenant's entry does not exist or its label is not unique.
     * @throws NoSuchProviderException If the provider is not registered.
     */
    public static KeyStore load(String provider, char[] pin, byte[] descriptor, String tenantUniqueId, String alias)
            throws KeyStoreException, NoSuchProviderException {

        if (StringUtils.isBlank(alias)) {
            throw new KeyStoreException("Alias of the tenant key can't be empty.");
        }
        String label = getTenantLabel(descriptor, tenantUniqueId);
        KeyStore token = getToken(provider, pin);
        if (!token.isKeyEntry(label)) {
            // The entry may have been added after the shared token key store was loaded.
            token = reloadForMissingLabel(provider, pin, token, label);
            if (!token.isKeyEntry(label)) {
                checkLabelNotShared(token, label, provider);
                throw new KeyStoreException("No key entry with label '" + label + "' in the PKCS11 token of " +
                        "provider: " + provider);
            }
        }
        MISSING_LABEL_RELOADS.remove(missingLabelKey(provider, label));

        Key key;
        try {
            key = token.getKey(label, null);
        } catch (GeneralSecurityException e) {
            throw new KeyStoreException("Error while reading the key with label '" + label + "' from the PKCS11 " +
                    "token of provider: " + provider, e);
        }
        Certificate[] chain = token.getCertificateChain(label);
        if (key == null || chain == null || chain.length == 0) {
            throw new KeyStoreException("The entry with label '" + label + "' in the PKCS11 token of provider: " +
                    provider + " has no key or certificate.");
        }
        KeyStore view = new View(new SingleEntrySpi(alias, key, chain, getCreationDate(token, label)), token);
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
     * @param provider       Name of the registered JCA provider of the token.
     * @param pin            Token user PIN.
     * @param descriptor     Persisted descriptor of the tenant key store.
     * @param tenantUniqueId Unique ID of the tenant that owns the key store. The descriptor must name its label.
     * @return True if an entry was deleted, false if it did not exist.
     * @throws KeyStoreException       If the descriptor does not belong to the tenant, the label is not unique, or
     *                                 the entry cannot be deleted.
     * @throws NoSuchProviderException If the provider is not registered.
     */
    public static boolean deleteEntry(String provider, char[] pin, byte[] descriptor, String tenantUniqueId)
            throws KeyStoreException, NoSuchProviderException {

        String label = getTenantLabel(descriptor, tenantUniqueId);
        KeyStore token = getToken(provider, pin);
        if (!token.containsAlias(label)) {
            token = reloadToken(provider, pin, token);
            if (!token.containsAlias(label)) {
                checkLabelNotShared(token, label, provider);
                return false;
            }
        }
        try {
            try {
                token.deleteEntry(label);
            } catch (KeyStoreException e) {
                // The shared token key store refuses changes if a certificate label was not unique when it was
                // loaded. Load it again in case the duplicate has been removed since then.
                reloadToken(provider, pin, token).deleteEntry(label);
            }
        } catch (KeyStoreException e) {
            // SunPKCS11 refuses all changes to a token key store in which certificate labels are not unique.
            throw new KeyStoreException("Error while deleting the entry with label '" + label + "' from the " +
                    "PKCS11 token of provider: " + provider + ". The token refuses changes when certificate labels " +
                    "on it are not unique.", e);
        }
        MISSING_LABEL_RELOADS.remove(missingLabelKey(provider, label));
        return true;
    }

    /**
     * Forget the loaded token key stores. For tests.
     */
    static void clearLoadedTokens() {

        TOKENS.clear();
        MISSING_LABEL_RELOADS.clear();
    }

    /**
     * PKCS#11 tokens don't record when an entry was created; SunPKCS11 throws for it. Other providers may support it.
     */
    private static Date getCreationDate(KeyStore token, String label) {

        try {
            return token.getCreationDate(label);
        } catch (KeyStoreException | RuntimeException e) {
            return null;
        }
    }

    private static String getTenantLabel(byte[] descriptor, String tenantUniqueId) throws KeyStoreException {

        String label = getLabel(descriptor);
        if (StringUtils.isBlank(tenantUniqueId) || !labelFor(tenantUniqueId).equals(label)) {
            throw new KeyStoreException("The PKCS11 key store descriptor of tenant: " + tenantUniqueId + " names " +
                    "the token entry '" + label + "', which does not belong to the tenant.");
        }
        return label;
    }

    /**
     * SunPKCS11 does not use a certificate label as the alias when more than one certificate on the token has it.
     * It uses {@code <label>/<issuer>/<serial number>} instead, so the tenant's entry is not found by its label.
     */
    private static void checkLabelNotShared(KeyStore token, String label, String provider)
            throws KeyStoreException {

        String sharedLabelPrefix = label + SHARED_LABEL_ALIAS_SEPARATOR;
        for (String tokenAlias : Collections.list(token.aliases())) {
            if (tokenAlias.startsWith(sharedLabelPrefix)) {
                throw new KeyStoreException("More than one certificate in the PKCS11 token of provider: " + provider +
                        " has the label '" + label + "'. Token entry labels must be unique.");
            }
        }
    }

    /**
     * Get the shared token key store of the provider, loading it if it is not loaded yet or if the provider has been
     * registered again since it was loaded.
     */
    private static KeyStore getToken(String provider, char[] pin) throws KeyStoreException, NoSuchProviderException {

        if (StringUtils.isBlank(provider)) {
            throw new KeyStoreException("A JCA provider is required for a PKCS11 key store.");
        }
        KeyStore token = TOKENS.get(provider);
        if (isCurrent(token, provider)) {
            return token;
        }
        return reloadToken(provider, pin, token);
    }

    /**
     * Reload the token key store for a missing label, unless a reload for the same label happened within
     * {@link #MISSING_LABEL_RELOAD_INTERVAL_MILLIS}. Then the current shared token key store is returned as is.
     */
    private static KeyStore reloadForMissingLabel(String provider, char[] pin, KeyStore seen, String label)
            throws KeyStoreException, NoSuchProviderException {

        String key = missingLabelKey(provider, label);
        long now = System.currentTimeMillis();
        Long lastReload = MISSING_LABEL_RELOADS.get(key);
        if (lastReload != null && now - lastReload < MISSING_LABEL_RELOAD_INTERVAL_MILLIS) {
            KeyStore current = TOKENS.get(provider);
            return isCurrent(current, provider) ? current : seen;
        }
        MISSING_LABEL_RELOADS.put(key, now);
        return reloadToken(provider, pin, seen);
    }

    /**
     * Load the token key store of the provider again and share the new instance, unless another caller has already
     * replaced the instance this caller saw. Views created before are not affected.
     *
     * @param seen The shared token key store the caller used, or null.
     */
    private static synchronized KeyStore reloadToken(String provider, char[] pin, KeyStore seen)
            throws KeyStoreException, NoSuchProviderException {

        KeyStore current = TOKENS.get(provider);
        if (current != null && current != seen && isCurrent(current, provider)) {
            return current;
        }
        KeyStore token = loadToken(provider, pin);
        TOKENS.put(provider, token);
        return token;
    }

    private static boolean isCurrent(KeyStore token, String provider) {

        return token != null && token.getProvider() == Security.getProvider(provider);
    }

    private static String missingLabelKey(String provider, String label) {

        return provider + SHARED_LABEL_ALIAS_SEPARATOR + label;
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
     * Key store that uses the token's provider and type, backed by {@link SingleEntrySpi} or {@link ServerEntriesSpi}.
     */
    private static final class View extends KeyStore {

        View(KeyStoreSpi spi, KeyStore token) {

            super(spi, token.getProvider(), token.getType());
        }
    }

    /**
     * Read-only key store SPI that exposes one token entry under a fixed alias. It holds the entry's key handle and
     * certificate chain, so reading them does not call the token or lock the shared token key store.
     */
    private static final class SingleEntrySpi extends KeyStoreSpi {

        private static final String READ_ONLY = "The tenant PKCS11 key store view is read-only.";

        private final String alias;
        private final Key key;
        private final Certificate[] chain;
        private final Date creationDate;

        SingleEntrySpi(String alias, Key key, Certificate[] chain, Date creationDate) {

            this.alias = alias;
            this.key = key;
            this.chain = chain.clone();
            this.creationDate = creationDate == null ? null : new Date(creationDate.getTime());
        }

        private boolean isTenantAlias(String requestedAlias) {

            return alias.equals(requestedAlias);
        }

        @Override
        public Key engineGetKey(String requestedAlias, char[] password) {

            return isTenantAlias(requestedAlias) ? key : null;
        }

        @Override
        public Certificate[] engineGetCertificateChain(String requestedAlias) {

            return isTenantAlias(requestedAlias) ? chain.clone() : null;
        }

        @Override
        public Certificate engineGetCertificate(String requestedAlias) {

            return isTenantAlias(requestedAlias) ? chain[0] : null;
        }

        @Override
        public Date engineGetCreationDate(String requestedAlias) {

            return isTenantAlias(requestedAlias) && creationDate != null ? new Date(creationDate.getTime()) : null;
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

            return chain[0].equals(cert) ? alias : null;
        }

        @Override
        public void engineStore(OutputStream stream, char[] password) throws IOException {

            throw new IOException(READ_ONLY);
        }

        @Override
        public void engineLoad(InputStream stream, char[] password) {

            // The view is created with the entry already read from the token.
        }
    }

    /**
     * Key store SPI over a server key store held in a token that hides the tenant entries
     * ({@link #isTenantTokenAlias(String)}) and passes everything else to the token key store.
     */
    private static final class ServerEntriesSpi extends KeyStoreSpi {

        private static final String TENANT_ENTRY = "Tenant key pairs in the PKCS11 token can't be changed through " +
                "a server key store. Alias: ";

        private final KeyStore token;
        private boolean initialized;

        ServerEntriesSpi(KeyStore token) {

            this.token = token;
        }

        @Override
        public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException,
                UnrecoverableKeyException {

            if (isTenantTokenAlias(alias)) {
                return null;
            }
            try {
                return token.getKey(alias, password);
            } catch (KeyStoreException e) {
                throw notLoaded(e);
            }
        }

        @Override
        public Certificate[] engineGetCertificateChain(String alias) {

            return isTenantTokenAlias(alias) ? null : read(() -> token.getCertificateChain(alias));
        }

        @Override
        public Certificate engineGetCertificate(String alias) {

            return isTenantTokenAlias(alias) ? null : read(() -> token.getCertificate(alias));
        }

        @Override
        public Date engineGetCreationDate(String alias) {

            return isTenantTokenAlias(alias) ? null : read(() -> token.getCreationDate(alias));
        }

        @Override
        public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
                throws KeyStoreException {

            checkNotTenantAlias(alias);
            token.setKeyEntry(alias, key, password, chain);
        }

        @Override
        public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {

            checkNotTenantAlias(alias);
            token.setKeyEntry(alias, key, chain);
        }

        @Override
        public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {

            checkNotTenantAlias(alias);
            token.setCertificateEntry(alias, cert);
        }

        @Override
        public void engineDeleteEntry(String alias) throws KeyStoreException {

            checkNotTenantAlias(alias);
            token.deleteEntry(alias);
        }

        @Override
        public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam)
                throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {

            return isTenantTokenAlias(alias) ? null : token.getEntry(alias, protParam);
        }

        @Override
        public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam)
                throws KeyStoreException {

            checkNotTenantAlias(alias);
            token.setEntry(alias, entry, protParam);
        }

        @Override
        public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {

            return !isTenantTokenAlias(alias) && read(() -> token.entryInstanceOf(alias, entryClass));
        }

        @Override
        public Enumeration<String> engineAliases() {

            return Collections.enumeration(serverAliases());
        }

        @Override
        public boolean engineContainsAlias(String alias) {

            return !isTenantTokenAlias(alias) && read(() -> token.containsAlias(alias));
        }

        @Override
        public int engineSize() {

            return serverAliases().size();
        }

        @Override
        public boolean engineIsKeyEntry(String alias) {

            return !isTenantTokenAlias(alias) && read(() -> token.isKeyEntry(alias));
        }

        @Override
        public boolean engineIsCertificateEntry(String alias) {

            return !isTenantTokenAlias(alias) && read(() -> token.isCertificateEntry(alias));
        }

        @Override
        public String engineGetCertificateAlias(Certificate cert) {

            for (String alias : serverAliases()) {
                if (cert != null && cert.equals(engineGetCertificate(alias))) {
                    return alias;
                }
            }
            return null;
        }

        @Override
        public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
                CertificateException {

            try {
                token.store(stream, password);
            } catch (KeyStoreException e) {
                throw notLoaded(e);
            }
        }

        @Override
        public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
                CertificateException {

            // The view is created over a loaded token key store. A later load loads the token key store again.
            if (initialized) {
                token.load(stream, password);
            }
            initialized = true;
        }

        private List<String> serverAliases() {

            List<String> aliases = new ArrayList<>();
            for (String alias : Collections.list(read(token::aliases))) {
                if (!isTenantTokenAlias(alias)) {
                    aliases.add(alias);
                }
            }
            return aliases;
        }

        private static void checkNotTenantAlias(String alias) throws KeyStoreException {

            if (isTenantTokenAlias(alias)) {
                throw new KeyStoreException(TENANT_ENTRY + alias);
            }
        }

        private static <T> T read(TokenRead<T> tokenRead) {

            try {
                return tokenRead.read();
            } catch (KeyStoreException e) {
                throw notLoaded(e);
            }
        }

        private static ProviderException notLoaded(KeyStoreException e) {

            // The token key store is loaded before the view is created, so this is not expected.
            return new ProviderException("The PKCS11 token key store is not loaded.", e);
        }

        @FunctionalInterface
        private interface TokenRead<T> {

            T read() throws KeyStoreException;
        }
    }
}
