/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.dataaccess;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This describes a an instance of the manager class that can be used to obtain access to the
 * back-end database.
 */
public interface DataAccessManager {

    /**
     * Method to obtain an instance of the cluster lock.
     *
     * @return instance of the lock implementation.
     */
    ClusterLock getClusterLock();

    /**
     * Method to obtain an instance of the transaction manager.
     *
     * @return instance of the transaction manager implementation.
     */
    TransactionManager getTransactionManager();

    /**
     * Method to obtain an instance of the database transaction.
     *
     * @return instance of the database transaction implementation.
     */
    DatabaseTransaction getDatabaseTransaction();

    /**
     * Method to obtain an instance of the data access object manager.
     *
     * @return instance of the data access object manager implementation.
     */
    DAOManager getDAOManager();

    /**
     * Method to create the database for the registry.
     *
     * @throws RegistryException if the operation failed.
     */
    void createDatabase() throws RegistryException;

    /**
     * Method to test whether the registry database is existing.
     *
     * @return true if the database is existing or false if not.
     */
    boolean isDatabaseExisting();

    /**
     * Method obtain an instance of the query processor used to run queries.
     *
     * @return the query processor to execute sql queries.
     */
    QueryProcessor getQueryProcessor();

}
