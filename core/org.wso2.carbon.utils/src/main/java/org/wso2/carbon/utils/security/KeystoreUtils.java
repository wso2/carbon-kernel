package org.wso2.carbon.utils.security;

import org.wso2.carbon.utils.CarbonUtils;

/**
 * A collection of key store and trust store related utility methods.
 */
public class KeystoreUtils {

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
         * Get the file extension for give store file type (ex: .jks, .p12).
         *
         * @return File extension.
         */
        public static String getFileTypeByExtension(String fileType) {

            return StoreFileType.valueOf(fileType).extension;
        }
    }

    /**
     * Retrieve keystore file location.
     *
     * @return File location.
     */
    public static String getKeyStoreFileLocation() {

        return CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStore.Location");
    }

    /**
     * Retrieve keystore file type (ex: JKS, PKCS12).
     *
     * @return File type.
     */
    public static String getKeyStoreFileType() {

        return CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStore.Type");
    }

    /**
     * Retrieve keystore file extension (ex: .jks, .p12).
     *
     * @return File extension.
     */
    public static String getKeyStoreFileExtension() {

        return StoreFileType.getFileTypeByExtension(getKeyStoreFileType());
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

        return CarbonUtils.getServerConfiguration().getFirstProperty("Security.TrustStore.Type");
    }

    /**
     * Retrieve truststore file extension (ex: .jks, .p12).
     *
     * @return File extension.
     */
    public static String getTrustStoreFileExtension() {

        return StoreFileType.getFileTypeByExtension(getTrustStoreFileType());
    }
}
