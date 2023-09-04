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
 * Constants related to the PubCertDAOConstants
 */
public class PubCertDAOConstants {

    private PubCertDAOConstants() {}

    public static class PubCertTableColumns {

        private PubCertTableColumns() {}
        public static final String ID = "ID";
        public static final String FILE_NAME_APPENDER = "FILE_NAME_APPENDER";
        public static final String TENANT_UUID = "TENANT_UUID";
        public static final String CONTENT = "CONTENT";
    }

    public static class SQLQueries {

        private SQLQueries() {}

        public static final String ADD_PUB_CERT = "INSERT INTO IDN_PUB_CERT (ID, FILE_NAME_APPENDER, CONTENT) " +
            "VALUES (:ID;, :FILE_NAME_APPENDER;, ?)";

        public static final String GET_PUB_CERT =
                "SELECT * FROM IDN_PUB_CERT WHERE ID = :ID;";
    }

    public static class ErrorMessages {

        private ErrorMessages() {}

        public static final String ERROR_MESSAGE_ADDING_PUB_CERT = "Error while adding public certificate.";
        public static final String ERROR_MESSAGE_RETRIEVING_PUB_CERT = "Error while retrieving public certificate.";
        public static final String DB_CONN_RETRIEVAL_ERROR_MSG = "Error while getting the DB connection.";
    }

}
