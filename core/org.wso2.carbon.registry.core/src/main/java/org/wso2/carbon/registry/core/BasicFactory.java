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

package org.wso2.carbon.registry.core;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * A basic factory class which expects to be able to create a Registry with newInstance()
 */
@Deprecated class BasicFactory extends RegistryFactory {

    Class registryClass;

    BasicFactory(Class registryClass) {
        this.registryClass = registryClass;
    }

    public Registry getRegistry() throws RegistryException {
        try {
            return (Registry) registryClass.newInstance();
        } catch (Exception e) {
            throw new RegistryException("Couldn't create Registry class '" + registryClass + "'");
        }
    }

    public Registry getRegistry(String username, String password) throws RegistryException {
        return getRegistry();
    }
}
