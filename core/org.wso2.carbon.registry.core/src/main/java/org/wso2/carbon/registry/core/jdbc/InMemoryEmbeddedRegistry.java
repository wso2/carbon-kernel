/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.jdbc;

import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;

/**
 * In-memory implementation of the Embedded Registry.
 *
 * @see EmbeddedRegistry
 */
public class InMemoryEmbeddedRegistry extends EmbeddedRegistry {

    /**
     * The database URL of the In-memory embedded database.
     */
    public static final String IN_MEMORY_DB_URL = "jdbc:h2:./target/databasetest/CARBON_TEST";

    /**
     * The database driver of the In-memory embedded database.
     */
    public static final String IN_MEMORY_DB_DRIVER_NAME = "org.h2.Driver";

    /**
     * The username for the In-memory embedded database.
     */
    public static final String IN_MEMORY_DB_USER_NAME = "";

    /**
     * The password for the In-memory embedded database.
     */
    public static final String IN_MEMORY_DB_PASSWORD = "";

    /**
     * Creates an in-memory H2 database based registry without the support for authentication and
     * authorization.
     *
     * @throws RegistryException if the creation of the in-memory embedded registry fails.
     */
    public InMemoryEmbeddedRegistry() throws RegistryException {

        DataBaseConfiguration dbConfiguration = new DataBaseConfiguration();
        dbConfiguration.setDbUrl(IN_MEMORY_DB_URL);
        dbConfiguration.setDriverName(IN_MEMORY_DB_DRIVER_NAME);
        dbConfiguration.setUserName(IN_MEMORY_DB_USER_NAME);
        dbConfiguration.setPassWord(IN_MEMORY_DB_PASSWORD);
        JDBCDataAccessManager jdbcDataAccessManager = new JDBCDataAccessManager(dbConfiguration);

        super.configure(jdbcDataAccessManager, null);
    }
}
