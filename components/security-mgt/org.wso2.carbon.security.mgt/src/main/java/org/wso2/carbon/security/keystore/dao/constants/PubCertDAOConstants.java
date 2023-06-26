package org.wso2.carbon.security.keystore.dao.constants;

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
