package org.wso2.carbon.security.keystore.dao.constants;

public class KeyStoreDAOConstants {

    private KeyStoreDAOConstants() {}

    public static class KeyStoreTableColumns {

        private KeyStoreTableColumns() {}
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

        private SqlQueries() {}

        public static final String ADD_KEY_STORE = "INSERT INTO IDN_KEY_STORE " +
            "(ID, FILE_NAME, TYPE, PROVIDER, PASSWORD, PRIVATE_KEY_ALIAS, PRIVATE_KEY_PASS, TENANT_UUID, LAST_UPDATED, CONTENT) " +
            "VALUES (:ID;, :FILE_NAME;, :TYPE;, :PROVIDER;, :PASSWORD;, :PRIVATE_KEY_ALIAS;, :PRIVATE_KEY_PASS;, :TENANT_UUID;, :LAST_UPDATED;, ?)";

        // TODO: do we really need to qualify this with tenant id?
        public static final String GET_KEY_STORE_BY_ID =
                "SELECT * FROM IDN_KEY_STORE WHERE ID = :ID; AND TENANT_UUID = :TENANT_UUID;";

        // TODO: add unique constraint to file name and tenant id
        public static final String GET_KEY_STORE_BY_FILE_NAME =
                "SELECT * FROM IDN_KEY_STORE WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";

        public static final String GET_KEY_STORES =
                "SELECT * FROM IDN_KEY_STORE WHERE TENANT_UUID = :TENANT_UUID;";

        public static final String DELETE_KEY_STORE_BY_ID =
                "DELETE FROM IDN_KEY_STORE WHERE ID = :ID; AND TENANT_UUID = :TENANT_UUID;";

        public static final String DELETE_KEY_STORE_BY_FILE_NAME =
                "DELETE FROM IDN_KEY_STORE WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";

        // TODO: refactor to use named prep statement after adding a method to set named bytes in named prep statement.
        public static final String UPDATE_KEY_STORE_BY_FILE_NAME =
                "UPDATE IDN_KEY_STORE SET TYPE = ?, PROVIDER = ?, " +
                        "PASSWORD = ?, PRIVATE_KEY_ALIAS = ?, " +
                        "PRIVATE_KEY_PASS = ?, LAST_UPDATED = ?, CONTENT = ? " +
                        "WHERE FILE_NAME = ? AND TENANT_UUID = ?";

        public static final String GET_PUB_CERT_ID_OF_KEY_STORE =
                "SELECT PUB_CERT_ID FROM IDN_KEY_STORE WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";

        public static final String ADD_PUB_CERT_ID_TO_KEY_STORE =
                "UPDATE IDN_KEY_STORE SET PUB_CERT_ID = :PUB_CERT_ID;, LAST_UPDATED = :LAST_UPDATED; WHERE FILE_NAME = :FILE_NAME; AND TENANT_UUID = :TENANT_UUID;";
    }

}
