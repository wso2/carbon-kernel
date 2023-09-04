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

package org.wso2.carbon.core.keystore.dao.constants;

/**
 * Constants related to the KeyStoreDAO.
 */
public class KeyStoreDAOConstants {

    private KeyStoreDAOConstants() {

    }

    public static class KeyStoreTableColumns {

        private KeyStoreTableColumns() {

        }

        public static final String ID = "ID";
        public static final String FILE_NAME = "FILE_NAME";
        public static final String TYPE = "TYPE";
        public static final String PROVIDER = "PROVIDER";
        public static final String PASSWORD = "PASSWORD";
        public static final String PRIVATE_KEY_ALIAS = "PRIVATE_KEY_ALIAS";
        public static final String PRIVATE_KEY_PASS = "PRIVATE_KEY_PASS";
        public static final String TENANT_UUID = "TENANT_UUID";
        public static final String PUB_CERT_ID = "PUB_CERT_ID";
        public static final String LAST_UPDATED = "LAST_UPDATED";
        public static final String CONTENT = "CONTENT";
    }

    public static class SqlQueries {

        private SqlQueries() {

        }

        public static final String ADD_KEY_STORE = "INSERT INTO IDN_KEY_STORE " +
                "(ID, FILE_NAME, TYPE, PROVIDER, PASSWORD, PRIVATE_KEY_ALIAS, PRIVATE_KEY_PASS, TENANT_UUID, " +
                "LAST_UPDATED, CONTENT) VALUES (:ID;, :FILE_NAME;, :TYPE;, :PROVIDER;, :PASSWORD;, " +
                ":PRIVATE_KEY_ALIAS;, :PRIVATE_KEY_PASS;, :TENANT_UUID;, :LAST_UPDATED;, ?)";

        public static final String UPDATE_KEY_STORE_BY_FILE_NAME = "UPDATE IDN_KEY_STORE SET TYPE = :TYPE;, " +
                "PROVIDER = :PROVIDER;, PASSWORD = :PASSWORD;, PRIVATE_KEY_ALIAS = :PRIVATE_KEY_ALIAS;, " +
                "PRIVATE_KEY_PASS = :PRIVATE_KEY_PASS;, LAST_UPDATED = :LAST_UPDATED;, CONTENT = ? " +
                "WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";

        public static final String GET_KEY_STORE_BY_ID =
                "SELECT * FROM IDN_KEY_STORE WHERE ID = :ID;";

        public static final String GET_KEY_STORE_BY_FILE_NAME =
                "SELECT * FROM IDN_KEY_STORE WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";

        public static final String GET_KEY_STORES =
                "SELECT * FROM IDN_KEY_STORE WHERE TENANT_UUID = :TENANT_UUID;";

        public static final String DELETE_KEY_STORE_BY_ID =
                "DELETE FROM IDN_KEY_STORE WHERE ID = :ID; AND TENANT_UUID = :TENANT_UUID;";

        public static final String DELETE_KEY_STORE_BY_FILE_NAME =
                "DELETE FROM IDN_KEY_STORE WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";

        // TODO: refactor to use named prep statement after adding a method to set named bytes in named prep statement.

        public static final String GET_PUB_CERT_ID_OF_KEY_STORE =
                "SELECT PUB_CERT_ID FROM IDN_KEY_STORE WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";

        public static final String ADD_PUB_CERT_ID_TO_KEY_STORE =
                "UPDATE IDN_KEY_STORE SET PUB_CERT_ID = :PUB_CERT_ID;, LAST_UPDATED = :LAST_UPDATED; " +
                        "WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";
    }

    public static class ErrorMessages {

        private ErrorMessages() {

        }

        public static final String ERROR_ADD_KEY_STORE = "Error while adding key store.";
        public static final String ERROR_UPDATE_KEY_STORE = "Error while updating key store.";
        public static final String ERROR_GET_KEY_STORES = "Error while retrieving key stores.";
        public static final String ERROR_GET_KEY_STORE = "Error while retrieving the key store.";
        public static final String ERROR_DELETE_KEY_STORE_BY_FILE_NAME = "Error while deleting key store by file name.";
        public static final String ERROR_LINK_PUB_CERT_TO_KEY_STORE = "Error while linking public certificate to key " +
                "store.";
        public static final String ERROR_GET_PUB_CERT_OF_KEY_STORE = "Error while retrieving public certificate of " +
                "key store.";
        public static final String ERROR_CANNOT_RETRIEVE_DB_CONN = "Error while retrieving database connection.";
    }
}
