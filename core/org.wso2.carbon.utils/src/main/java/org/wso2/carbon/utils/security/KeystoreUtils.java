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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * A collection of key store and trust store related utility methods.
 */
public class KeystoreUtils {

    private static Log LOG = LogFactory.getLog(KeystoreUtils.class);
    private static final String FALLBACK_TENANTED_KEYSTORE_FILE_TYPE = "JKS";
    private static final String KEY_STORES = "/repository/security/key-stores";

    /**
     * A collection of file type extensions against the store file type.
     */
    public enum StoreFileType {
        JKS(".jks"),
        PKCS12(".p12");

        private final String extension;
        private static final String defaultFileType = "PKCS12";

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
     * Retrieve keystore file type (ex: JKS, PKCS12).
     * @param tenantDomain  Tenant domain the keystore need to be resolved.
     *
     * @return File type.
     */
    public static String getKeyStoreFileType(String tenantDomain) {

        String keystoreType = CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStore.Type");
        try {
            StoreFileType.validateFileType(keystoreType);
        } catch (CarbonException e) {
            LOG.error("Unsupported file type for key store file", e);
        }

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return keystoreType;
        }

        String ksName = tenantDomain.trim().replace(".", "-");
        if (isFileExistInRegistry(ksName + getExtensionByFileType(keystoreType))) {
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

    private static boolean isFileExistInRegistry(String keyStoreName) {

        boolean isKeyStoreExists = false;
        try {
            if (PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE)
                    .resourceExists(KEY_STORES + "/" + keyStoreName)) {
                isKeyStoreExists = true;
            }
        } catch (RegistryException e) {
            String msg = "Error while checking the existance of keystore.  ";
            LOG.error(msg + e.getMessage());
        }
        return isKeyStoreExists;
    }
}
