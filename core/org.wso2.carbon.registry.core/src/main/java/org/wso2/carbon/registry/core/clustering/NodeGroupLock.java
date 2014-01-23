/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.clustering;

import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This utilizes the environment's cluster lock to ensure that no more than a single master node
 * exists.
 */
public class NodeGroupLock {

    /**
     * The name of the lock used during the initialization process.
     */
    public static final String INITIALIZE_LOCK = "INITIALIZE";
    private static DataAccessManager dataAccessManager = null;

    /**
     * Initializes the Node Group Lock Object. This method is responsible of initializing the
     * database, which is required for creating locks in the future.
     *
     * @param dataAccessManager         the data access manager to be used to communicate with the
     *                                  database.
     *
     * @throws RegistryException if the creation of the cluster lock table failed, or if any
     *                           exceptions occur during the initialization.
     */
    public synchronized static void init(DataAccessManager dataAccessManager)
            throws RegistryException {
        NodeGroupLock.dataAccessManager = dataAccessManager;
        //NodeGroupLock.dataAccessManager.getClusterLock().init(dataAccessManager, null);
    }

    /**
     * Creates a lock by the given name
     *
     * @param lockName    the name of the lock.
     *
     * @throws RegistryException if an exception occurred while locking.
     */
    public static void lock(String lockName) throws RegistryException {
        //NodeGroupLock.dataAccessManager.getClusterLock().lock(lockName);
    }

    /**
     * Unlocks a named lock.
     *
     * @param lockName    the name of the lock.
     *
     * @throws RegistryException if an exception occurred while unlocking.
     */
    public static void unlock(String lockName) throws RegistryException {
        //NodeGroupLock.dataAccessManager.getClusterLock().unlock(lockName);
    }

}
