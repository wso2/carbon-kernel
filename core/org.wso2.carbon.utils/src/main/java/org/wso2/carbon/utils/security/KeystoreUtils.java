/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.utils.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.keystore.persistence.KeyStorePersistenceManager;
import org.wso2.carbon.keystore.persistence.KeyStorePersistenceManagerFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;

/**
 * A collection of key store and trust store related utility methods.
 */
public class KeystoreUtils {

    private static Log LOG = LogFactory.getLog(KeystoreUtils.class);
    private static final String FALLBACK_TENANTED_KEYSTORE_FILE_TYPE = "JKS";

    /**
     * Key algorithm used for the keys of newly created tenant keystores when nothing is configured.
     */
    public static final String DEFAULT_TENANT_KEY_ALGORITHM = "RSA";

    /**
     * Key size (in bits) used for the keys of newly created tenant keystores when nothing is configured.
     */
    public static final int DEFAULT_TENANT_KEY_SIZE = 2048;

    private static final String TENANT_KEY_ALGORITHM_CONFIG = "Security.TenantKeyStore.KeyAlgorithm";
    private static final String TENANT_KEY_SIZE_CONFIG = "Security.TenantKeyStore.KeySize";
    private static final int MIN_TENANT_KEY_SIZE = 2048;
    private static final int MAX_TENANT_KEY_SIZE = 8192;
    private static final int TENANT_KEY_SIZE_STEP = 1024;
    private static final KeyStorePersistenceManager keyStorePersistenceManager =
            KeyStorePersistenceManagerFactory.getKeyStorePersistenceManager();

    /**
     * A collection of file type extensions against the store file type.
     */
    public enum StoreFileType {
        JKS(".jks"),
        PKCS12(".p12");

        private final String extension;

        StoreFileType(String extension) {

            this.extension = extension;
        }

        /**
         * Get extension of the store file type (ex: .jks, .p12).
         *
         * @throws IllegalArgumentException the File type .
         */
        public static String getExtension(StoreFileType tileType) {

            return tileType.extension;
        }

        /**
         * If the `Security.TenantKeyStore.Type` is defined, return the type,
         * otherwise return FALLBACK_TENANTED_KEYSTORE_FILE_TYPE.
         */
        public static String defaultFileType() {

            String keystoreTypesForNewTenants = CarbonUtils.getServerConfiguration().getFirstProperty(
                    "Security.TenantKeyStore.Type");
            if (StringUtils.isNotBlank(keystoreTypesForNewTenants)) {
                return keystoreTypesForNewTenants;
            }
            return FALLBACK_TENANTED_KEYSTORE_FILE_TYPE;
        }

        /**
         * Check the configured store file type is supporting (ex: JKS, PKCS12).
         *
         * @throws IllegalArgumentException the File type .
         */
        public static void validateFileType(String fileType) throws CarbonException {
            try {
                StoreFileType.valueOf(fileType);
            } catch (IllegalArgumentException e) {
                throw new CarbonException("Unsupported store file type:" + fileType);
            }
        }
    }

    /**
     * Get the file extension for give store file type (ex: .jks, .p12).
     *
     * @return File extension.
     */
    public static String getExtensionByFileType(String fileType) {

        return StoreFileType.getExtension(StoreFileType.valueOf(fileType)) ;
    }

    /**
     * Get the file type for give store file extension (ex: JKS, PKCS12).
     *
     * @return File type.
     */
    public static String getFileTypeByExtension(String extension) throws CarbonException {

        for (StoreFileType fileTypes: StoreFileType.values()) {
            if (StoreFileType.getExtension(fileTypes).equals(extension)) {
                return fileTypes.name();
            }
        }
        throw new CarbonException("Unsupported store file extension type:" + extension);
    }

    /**
     * Retrieve keystore file location.
     *
     * @return File location.
     */
    public static String getKeyStoreFileLocation(String tenantDomain) {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStore.Location");
        }
        return tenantDomain.trim().replace(".", "-") + getKeyStoreFileExtension(tenantDomain);
    }

    /**
     * Retrieve keystore file location for a given context.
     * This won't consider the server configuration for super tenant as the context is provided.
     *
     * @return File location.
     */
    public static String getKeyStoreFileLocation(String ksName, String tenantDomain) {

        return ksName + getKeyStoreFileExtension(ksName, tenantDomain);
    }

    /**
     * Retrieve keystore file type (ex: JKS, PKCS12).
     * @param tenantDomain  Tenant domain the keystore need to be resolved.
     *
     * @return File type.
     */
    public static String getKeyStoreFileType(String tenantDomain) {

        String keystoreType;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            keystoreType = CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStore.Type");
            try {
                StoreFileType.validateFileType(keystoreType);
            } catch (CarbonException e) {
                LOG.error("Unsupported file type for key store file", e);
            }
            return keystoreType;
        } else {
            keystoreType = StoreFileType.defaultFileType();
            String ksName = tenantDomain.trim().replace(".", "-");
            String ksExtension = getExtensionByFileType(keystoreType);
            if (StoreFileType.PKCS12.name().equals(keystoreType) && isKeyStoreExists(ksName + ksExtension)) {
                return keystoreType;
            }
            return FALLBACK_TENANTED_KEYSTORE_FILE_TYPE;
        }
    }

    /**
     * Retrieve keystore file type (ex: JKS, PKCS12).
     * @param tenantDomain  Tenant domain the keystore need to be resolved.
     *
     * @return File type.
     */
    public static String getKeyStoreFileType(String ksName, String tenantDomain) {

        String keystoreType;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            keystoreType = CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStore.Type");
            try {
                StoreFileType.validateFileType(keystoreType);
            } catch (CarbonException e) {
                LOG.error("Unsupported file type for key store file", e);
            }
        } else {
            keystoreType = StoreFileType.defaultFileType();
        }
        String ksExtension = getExtensionByFileType(keystoreType);
        if (StoreFileType.PKCS12.name().equals(keystoreType) && isKeyStoreExists(ksName + ksExtension)) {
            return keystoreType;
        }
        return FALLBACK_TENANTED_KEYSTORE_FILE_TYPE;
    }

    /**
     * Retrieve keystore file extension (ex: .jks, .p12).
     *
     * @return File extension.
     */
    public static String getKeyStoreFileExtension(String tenantDomain) {

        return getExtensionByFileType(getKeyStoreFileType(tenantDomain));
    }

    /**
     * Retrieve keystore file extension (ex: .jks, .p12).
     *
     * @return File extension.
     */
    public static String getKeyStoreFileExtension(String ksName, String tenantDomain) {

        return getExtensionByFileType(getKeyStoreFileType(ksName, tenantDomain));
    }

    /**
     * Retrieve truststore file location.
     *
     * @return File location.
     */
    public static String getTrustStoreFileLocation() {

        return CarbonUtils.getServerConfiguration().getFirstProperty("Security.TrustStore.Location");
    }

    /**
     * Retrieve truststore file type (ex: JKS, PKCS12).
     *
     * @return File type.
     */
    public static String getTrustStoreFileType() {

        String truststore = CarbonUtils.getServerConfiguration().getFirstProperty("Security.TrustStore.Type");
        try {
            StoreFileType.validateFileType(truststore);
        } catch (CarbonException e) {
            LOG.error("Unsupported file type for trust store file", e);
        }
        return truststore;
    }

    /**
     * Retrieve truststore file extension (ex: .jks, .p12).
     *
     * @return File extension.
     */
    public static String getTrustStoreFileExtension() {

        return getExtensionByFileType(getTrustStoreFileType());
    }

    /**
     * Returns the key algorithm to use when generating the key pair of a new tenant keystore.
     * <p>
     * The value is read from {@code Security.TenantKeyStore.KeyAlgorithm} in carbon.xml
     * ({@code [keystore.tenant] key_algorithm} in deployment.toml). If it is not configured,
     * {@value #DEFAULT_TENANT_KEY_ALGORITHM} is returned.
     * <p>
     * This setting applies only to tenants (and tenant context keystores) created after it is set.
     * Keys of existing tenants are not changed.
     * <p>
     * Only {@code RSA} is supported. This method is the extension point for other algorithms.
     *
     * @return Key algorithm name, for example {@code RSA}.
     * @throws CarbonException If the configured algorithm is not supported.
     */
    public static String getTenantKeyAlgorithm() throws CarbonException {

        String keyAlgorithm = CarbonUtils.getServerConfiguration().getFirstProperty(TENANT_KEY_ALGORITHM_CONFIG);
        if (StringUtils.isBlank(keyAlgorithm)) {
            return DEFAULT_TENANT_KEY_ALGORITHM;
        }
        keyAlgorithm = keyAlgorithm.trim();
        if (DEFAULT_TENANT_KEY_ALGORITHM.equalsIgnoreCase(keyAlgorithm)) {
            return DEFAULT_TENANT_KEY_ALGORITHM;
        }
        throw new CarbonException("Unsupported key algorithm '" + keyAlgorithm + "' configured in "
                + TENANT_KEY_ALGORITHM_CONFIG + " ([keystore.tenant] key_algorithm). Supported algorithms: "
                + DEFAULT_TENANT_KEY_ALGORITHM + ".");
    }

    /**
     * Returns the key size, in bits, to use when generating the key pair of a new tenant keystore.
     * <p>
     * The value is read from {@code Security.TenantKeyStore.KeySize} in carbon.xml
     * ({@code [keystore.tenant] key_size} in deployment.toml). If it is not configured,
     * {@value #DEFAULT_TENANT_KEY_SIZE} is returned.
     * <p>
     * A configured value must be a multiple of 1024 between 2048 and 8192 (inclusive). An invalid value causes an
     * exception instead of a fallback, so a smaller key than the configured one is never generated.
     * <p>
     * This setting applies only to tenants (and tenant context keystores) created after it is set.
     * Keys of existing tenants are not changed.
     *
     * @return Key size in bits.
     * @throws CarbonException If the configured key size is not valid.
     */
    public static int getTenantKeySize() throws CarbonException {

        String keySizeValue = CarbonUtils.getServerConfiguration().getFirstProperty(TENANT_KEY_SIZE_CONFIG);
        if (StringUtils.isBlank(keySizeValue)) {
            return DEFAULT_TENANT_KEY_SIZE;
        }
        keySizeValue = keySizeValue.trim();
        int keySize;
        try {
            keySize = Integer.parseInt(keySizeValue);
        } catch (NumberFormatException e) {
            throw new CarbonException(buildInvalidKeySizeMessage(keySizeValue), e);
        }
        if (keySize < MIN_TENANT_KEY_SIZE || keySize > MAX_TENANT_KEY_SIZE || keySize % TENANT_KEY_SIZE_STEP != 0) {
            throw new CarbonException(buildInvalidKeySizeMessage(keySizeValue));
        }
        return keySize;
    }

    private static String buildInvalidKeySizeMessage(String keySizeValue) {

        return "Invalid key size '" + keySizeValue + "' configured in " + TENANT_KEY_SIZE_CONFIG
                + " ([keystore.tenant] key_size). The key size must be a multiple of " + TENANT_KEY_SIZE_STEP
                + " between " + MIN_TENANT_KEY_SIZE + " and " + MAX_TENANT_KEY_SIZE + ".";
    }

    private static boolean isKeyStoreExists(String keyStoreName) {

        return keyStorePersistenceManager.isKeyStoreExists(keyStoreName,
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
    }

    /**
     * Returns a {@link KeyStore} instance for the given type.
     * Uses the BouncyCastle provider for the p12 file type, otherwise the default JCE provider.
     *
     * @param keyStoreType  The type of the keystore.
     * @return              {@link KeyStore} instance.
     * @throws KeyStoreException If the keystore type is unavailable.
     * @throws NoSuchProviderException If the security provider is unavailable.
     */
    public static KeyStore getKeystoreInstance(String keyStoreType) throws KeyStoreException, NoSuchProviderException {

        if (StoreFileType.PKCS12.name().equals(keyStoreType)) {
            return KeyStore.getInstance(keyStoreType, getJCEProvider());
        } else {
            return KeyStore.getInstance(keyStoreType);
        }
    }

    private static String getJCEProvider() {

        String provider = ServerConfiguration.getInstance().getFirstProperty(ServerConstants.JCE_PROVIDER);
        if (!StringUtils.isBlank(provider)) {
            return provider;
        }
        return ServerConstants.JCE_PROVIDER_BC;
    }
}
