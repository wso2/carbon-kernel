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
package org.wso2.carbon.registry.core.service;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This interface can be used to implement a Provider of a Registry as an OSGi Service. The Registry
 * Kernel consumes such services for the purpose of establishing remote mounts.
 */
@SuppressWarnings("unused")
public interface RegistryProvider {

    /**
     * Method to obtain a new registry instance.
     *
     * @param registryURL the URL of the remote instance.
     * @param username    the username used to connect to the registry.
     * @param password    the password used to connect to the registry.
     *
     * @return instance of a registry.
     * @throws RegistryException if the operation failed.
     */
    Registry getRegistry(String registryURL, String username, String password)
            throws RegistryException;

}
