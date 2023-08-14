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

package org.wso2.carbon.security.keystore.dao.constants;

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

        public static final String ADD_PUB_CERT = "INSERT INTO IDN_PUB_CERT " +
            "(ID, FILE_NAME_APPENDER, TENANT_UUID, CONTENT) " +
            "VALUES (:ID;, :FILE_NAME_APPENDER;, :TENANT_UUID;, ?)";

        // TODO: think whether we need to qualify this using tenant id?
        //  First impression is not needed.
        //  Maybe we won't need tenant id in this table at all
        public static final String GET_PUB_CERT =
                "SELECT * FROM IDN_PUB_CERT WHERE ID = :ID; AND TENANT_UUID = :TENANT_UUID;";
    }

}
