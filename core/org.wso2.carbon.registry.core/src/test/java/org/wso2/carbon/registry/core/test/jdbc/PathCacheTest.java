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

package org.wso2.carbon.registry.core.test.jdbc;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.caching.PathCache;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

public class PathCacheTest extends BaseTestCase {

    protected static EmbeddedRegistryService embeddedRegistryService = null;
    protected static Registry registry = null;
    

    public void setUp() {
        super.setUp();
        if (embeddedRegistryService != null) {
            return;
        }
        try {
            embeddedRegistryService = ctx.getEmbeddedRegistryService();
            RealmUnawareRegistryCoreServiceComponent comp =
                    new RealmUnawareRegistryCoreServiceComponent();
            comp.setRealmService(ctx.getRealmService());
            comp.registerBuiltInHandlers(embeddedRegistryService);
            
            // get the realm config to retrieve admin username, password
            RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
            registry = embeddedRegistryService.getUserRegistry(
                realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        } catch (RegistryException e) {
                fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    public void testPathCache() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("some content");
        registry.put("/test/paths/r1", r1);

        double rate1 = PathCache.getPathCache().hitRate();
        registry.put("/test/paths/r2", r1);
        double rate2 = PathCache.getPathCache().hitRate();
        assertTrue("Rate2 >= Rate1", rate2 >= rate1);

        registry.get("/test");
        double rate3 = PathCache.getPathCache().hitRate();
        assertTrue("Rate3 >= Rate2", rate3 >= rate2);

        registry.get("/test/");
        double rate4 = PathCache.getPathCache().hitRate();
        assertTrue("Rate4 >= Rate3", rate4 >= rate3);

        registry.get("/test");
        double rate5 = PathCache.getPathCache().hitRate();
        assertTrue("Rate5 >= Rate4", rate5 >= rate4);

        registry.get("/test");
        double rate6 = PathCache.getPathCache().hitRate();
        assertTrue("Rate6 >= Rate5", rate6 >= rate5);

        registry.get("/test");
        double rate7 = PathCache.getPathCache().hitRate();
        assertTrue("Rate7 >= Rate6", rate7 >= rate6);
    }
}