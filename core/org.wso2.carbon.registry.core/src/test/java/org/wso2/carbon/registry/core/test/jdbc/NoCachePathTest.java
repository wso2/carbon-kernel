/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.test.jdbc;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

/**
 * This class tests adding and removing resource paths from NoCachePath List which was
 * changed to a CopyOnWriteArrayList in RegistryContext class.
 */
public class NoCachePathTest extends BaseTestCase {

    protected static EmbeddedRegistryService embeddedRegistryService = null;
    protected static Registry registry = null;

    public void setUp() {
        super.setUp();
        if (embeddedRegistryService != null) {
            return;
        }
        try {
            embeddedRegistryService = ctx.getEmbeddedRegistryService();
            RealmUnawareRegistryCoreServiceComponent comp = new RealmUnawareRegistryCoreServiceComponent();
            comp.setRealmService(ctx.getRealmService());
            comp.registerBuiltInHandlers(embeddedRegistryService);

            // get the realm config to retrieve admin username, password
            RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
            registry = embeddedRegistryService
                    .getUserRegistry(realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    /**
     * Method to check adding resource paths to NoCachePath List of RegistryContext class.
     */
    public void testAddToNoCachePath() {
        String path = "_/system/governance/trunk/test1";
        RegistryContext registryContext = registry.getRegistryContext();
        registryContext.registerNoCachePath(path);
        assertTrue("Path is not added as a no cache path", registryContext.isNoCachePath(path));
        registryContext.removeNoCachePath(path);
    }

    /**
     * Method to check removing resource paths from NoCachePath List of RegistryContext class.
     */
    public void testRemoveFromNoCachePath() {
        String path = "_/system/governance/trunk/test2";
        RegistryContext registryContext = registry.getRegistryContext();
        registryContext.registerNoCachePath(path);
        assertTrue("Path is not added as a no cache path", registryContext.isNoCachePath(path));

        // Removes the resource path from no cache path list.
        registryContext.removeNoCachePath(path);
        assertFalse("Path is not removed from no cache path", registryContext.isNoCachePath(path));
    }
}

