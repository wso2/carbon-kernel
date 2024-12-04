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

package org.wso2.carbon.core.keystore.persistence;

/**
 * DB related constant values.
 */
public class PersistenceManagerConstants {

    private PersistenceManagerConstants() {

    }

    public static final String DEFAULT_VERSION = "1.0.0";

    public static class KeyStoreTableColumns {

        private KeyStoreTableColumns() {

        }

        public static final String ID = "ID";
        public static final String NAME = "NAME";
        public static final String TYPE = "TYPE";
        public static final String PROVIDER = "PROVIDER";
        public static final String PASSWORD = "PASSWORD";
        public static final String PRIVATE_KEY_ALIAS = "PRIVATE_KEY_ALIAS";
        public static final String PRIVATE_KEY_PASS = "PRIVATE_KEY_PASS";
        public static final String CONTENT = "CONTENT";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String PUB_CERT_ID = "PUB_CERT_ID";
        public static final String CREATED_AT = "CREATED_AT";
        public static final String UPDATED_AT = "UPDATED_AT";
        public static final String VERSION = "VERSION";
    }

    public static class SqlQueries {

        private SqlQueries() {

        }

        public static final String ADD_KEY_STORE = "INSERT INTO KEY_STORE (NAME, TYPE, PROVIDER, PASSWORD, " +
                "PRIVATE_KEY_ALIAS, PRIVATE_KEY_PASS, CONTENT, PUB_CERT_ID, TENANT_ID, CREATED_AT, UPDATED_AT," +
                "VERSION) VALUES (:NAME;, :TYPE;, :PROVIDER;, :PASSWORD;, :PRIVATE_KEY_ALIAS;, :PRIVATE_KEY_PASS;, " +
                ":CONTENT;, :PUB_CERT_ID;, :TENANT_ID;, :CREATED_AT;, :UPDATED_AT;, :VERSION;);";
        public static final String GET_KEY_STORE_BY_NAME =
                "SELECT * FROM KEY_STORE WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_KEY_STORE_LAST_UPDATED_TIME =
                "SELECT UPDATED_AT FROM KEY_STORE WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_ENCRYPTED_KEY_STORE_PASSWORD =
                "SELECT PASSWORD FROM KEY_STORE WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_ENCRYPTED_PRIVATE_KEY_PASSWORD =
                "SELECT PRIVATE_KEY_PASS FROM KEY_STORE WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_KEY_STORES = "SELECT * FROM KEY_STORE WHERE TENANT_ID = :TENANT_ID;";
        public static final String GET_KEY_STORES_EXISTENCE = "SELECT ID FROM KEY_STORE WHERE TENANT_ID = :TENANT_ID;";
        public static final String DELETE_KEY_STORE_BY_NAME =
                "DELETE FROM KEY_STORE WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
        public static final String UPDATE_KEY_STORE_BY_NAME = "UPDATE KEY_STORE SET CONTENT = :CONTENT;, " +
                "UPDATED_AT = :UPDATED_AT; WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
    }
}
