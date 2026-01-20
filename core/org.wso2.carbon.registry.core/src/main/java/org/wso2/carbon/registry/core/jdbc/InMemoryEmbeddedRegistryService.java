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

import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.InputStream;

/**
 * This is an extension to the {@link EmbeddedRegistryService} which is tailored to expose {@link
 * InMemoryEmbeddedRegistry} instances as OSGi services.
 */
public class InMemoryEmbeddedRegistryService extends EmbeddedRegistryService {

    private RealmService realmService;

    /**
     * Default constructor
     *
     * @throws RegistryException if construction fails.
     */
    public InMemoryEmbeddedRegistryService() throws RegistryException {
        realmService = new InMemoryRealmService();
        registryContext = RegistryContext.getBaseInstance(realmService);
        registryContext.setSetup(true);
        registryContext.selectDBConfig("h2-db");
        super.configure(realmService);
    }

    /**
     * This constructor can be used to create a registry service by providing a registry.xml config
     * file as a stream
     *
     * @param configStream - registry.xml as a stream
     *
     * @throws RegistryException - on failure
     */
    public InMemoryEmbeddedRegistryService(InputStream configStream) throws RegistryException {
        realmService = new InMemoryRealmService();
        registryContext = RegistryContext.getBaseInstance(configStream, realmService);
        registryContext.setSetup(true);
        registryContext.selectDBConfig("h2-db");
        super.configure(realmService);
    }

    /**
     * Method to obtain bootstrap user realm configuration.
     *
     * @return the realm configuration.
     */
    public RealmConfiguration getBootstrapRealmConfiguration() {
        return realmService.getBootstrapRealmConfiguration();
    }

    /**
     * Method to obtain the user realm service.
     *
     * @return the user realm service.
     */
    public RealmService getRealmService() {
        return realmService;
    }
}
