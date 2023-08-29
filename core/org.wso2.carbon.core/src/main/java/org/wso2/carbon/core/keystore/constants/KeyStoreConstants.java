/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.core.keystore.constants;

import org.wso2.carbon.core.RegistryResources;

public class KeyStoreConstants {

    public static final String FILTER_FIELD_ALIAS = "alias";
    public static final String FILTER_OPERATION_EQUALS = "eq";
    public static final String FILTER_OPERATION_STARTS_WITH = "sw";
    public static final String FILTER_OPERATION_ENDS_WITH = "ew";
    public static final String FILTER_OPERATION_CONTAINS = "co";
    public static final int ITEMS_PER_PAGE = 10;
    public static final int CACHING_PAGE_SIZE = 5;
    public static final String KEY_STORES = RegistryResources.SecurityManagement.KEY_STORES;
    public static final String SERVER_TRUSTSTORE_FILE = "Security.TrustStore.Location";
    public static final String KEYSTORE_DATASOURCE = "KeyStoreDataPersistenceManager.DataSource.Name";

    /**
     * Enum for Keystore management service related errors.
     */
    public enum ErrorMessage {

        /**
         * Server errors.
         */
        ERROR_CODE_RETRIEVE_KEYSTORE("KSS-65001",
                "Unable to retrieve the keystore for tenant: %s."),
        ERROR_CODE_RETRIEVE_KEYSTORE_INFORMATION("KSS-65002",
                "Unable to retrieve keystore information for keystore: %s"),
        ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE("KSS-65003",
                "Unable to retrieve client truststore for tenant: %s"),
        ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE_ALIASES("KSS-65004",
                "Unable to retrieve the client truststore aliases for tenant: %s."),
        ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE_CERTIFICATE("KSS-65005",
                "Unable to retrieve the client truststore certificate for alias: %s."),
        ERROR_CODE_ADD_CERTIFICATE("KSS-65006",
                "Unable to add certificate with alias: %s"),
        ERROR_CODE_DELETE_CERTIFICATE("KSS-65007",
                "Unable to delete certificate with alias: %s"),
        ERROR_CODE_VALIDATE_CERTIFICATE("KSS-65008", "Error occurred while validating the " +
                "certificate."),
        ERROR_CODE_INITIALIZE_REGISTRY("KSS-65009",
                "Unable to initialize the registry for the tenant: %s."),
        /**
         * Client error.
         */
        ERROR_CODE_CERTIFICATE_EXISTS("KSS-60001",
                "Provided certificate already exists with the alias: %s"),
        ERROR_CODE_ALIAS_EXISTS("KSS-60002",
                "Provided alias '%s' is already available in the keystore."),
        ERROR_CODE_BAD_VALUE_FOR_FILTER("KSS-60003",
                "Unsupported filter: %s."),
        ERROR_CODE_UNSUPPORTED_FILTER_OPERATION("KSS-60004",
                "Unsupported filter operation %s."),
        ERROR_CODE_EMPTY_ALIAS("KSS-60005", "Alias value can not be null."),
        ERROR_CODE_INVALID_CERTIFICATE("KSS-60006", "Provided certificate is invalid."),
        ERROR_CODE_CANNOT_DELETE_TENANT_CERT("KSS-60007", "Not allowed to delete the tenant certificate %s."),

        /**
         * Common Error messages without a code.
         */
        ERROR_MESSAGE_RETRIEVE_KEYSTORE("KSS-62501", "Error when getting keyStore data."),
        ERROR_MESSAGE_RETRIEVE_PUBLIC_CERT("KSS-62502", "Error when getting public certificate data.");
        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + " : " + message;
        }
    }
}
