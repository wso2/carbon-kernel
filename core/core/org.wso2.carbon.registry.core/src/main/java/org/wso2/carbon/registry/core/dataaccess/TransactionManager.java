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
 * This describes a an instance of the manager class that can be used to manage transactions on the
 * back-end database.
 */
public interface TransactionManager {

    /**
     * Start a new transaction
     *
     * @throws RegistryException If an error occurs while starting a transaction
     */
    void beginTransaction() throws RegistryException;

    /**
     * Commit the currently active transaction
     *
     * @throws RegistryException If an error occurs while committing a transaction
     */
    void commitTransaction() throws RegistryException;

    /**
     * Rollback the currently active transaction
     *
     * @throws RegistryException If an error occurs while rolling back a transaction
     */
    void rollbackTransaction() throws RegistryException;
}
