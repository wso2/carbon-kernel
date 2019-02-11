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
 * A lock implementation to avoid database schema creation and data population from multiple nodes.
 */
public interface ClusterLock {

    /**
     * Initializes the Node Group Lock Object. This method is responsible of initializing the
     * database, which is required for creating locks in the future.
     *
     * @param dataAccessManager         the data access manager to be used to communicate with the
     *                                  database.
     * @param clusterLockTableStatement the SQL statement required create the cluster lock table.
     *
     * @throws RegistryException if the creation of the cluster lock table failed, or if any
     *                           exceptions occur during the initialization.
     */
    void init(DataAccessManager dataAccessManager, String clusterLockTableStatement)
            throws RegistryException;

    /**
     * Creates a lock by the given name
     *
     * @param lockName the name of the lock.
     *
     * @throws RegistryException if an exception occurred while locking.
     */
    void lock(String lockName) throws RegistryException;

    /**
     * Unlocks a named lock.
     *
     * @param lockName the name of the lock.
     *
     * @throws RegistryException if an exception occurred while unlocking.
     */
    void unlock(String lockName) throws RegistryException;

}