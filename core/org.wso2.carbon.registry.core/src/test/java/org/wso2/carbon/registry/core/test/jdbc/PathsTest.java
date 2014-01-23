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

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

public class PathsTest extends BaseTestCase {

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

    public void testGetOnPaths() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("some content");
        registry.put("/test/paths/r1", r1);

        assertTrue("Resource not found.", registry.resourceExists("/test"));
        assertTrue("Resource not found.", registry.resourceExists("/test/"));
        assertTrue("Resource not found.", registry.resourceExists("/test/paths/r1"));
        assertTrue("Resource not found.", registry.resourceExists("/test/paths/r1/"));

        registry.get("/test");
        registry.get("/test/");
        registry.get("/test/paths/r1");
        registry.get("/test/paths/r1/");
    }

    public void testColon() throws RegistryException {
        // Test for CARBON-5226
        Resource r1 = registry.newResource();
        r1.setContent("some content");
        registry.put("/con-delete/?aTest:/pp:", r1);

        assertTrue("Resource not found.", registry.resourceExists("/con-delete"));
        assertTrue("Resource not found.", registry.resourceExists("/con-delete"));
        assertTrue("Resource not found.", registry.resourceExists("/con-delete/?aTest:/pp:"));
        assertTrue("Resource not found.", registry.resourceExists("/con-delete/?aTest:/pp:"));

        registry.get("/con-delete");
        registry.get("/con-delete/");
        registry.get("/con-delete/?aTest:/pp:");
        registry.get("/con-delete/?aTest:/pp:");
    }

    public void testPutOnPaths() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("some content");
        registry.put("/test/paths2/r1", r1);

        Resource r2 = registry.newResource();
        r2.setContent("another content");
        registry.put("/test/paths2/r2/", r2);

        Collection c1 = registry.newCollection();
        registry.put("/test/paths2/c1", c1);

        Collection c2 = registry.newCollection();
        registry.put("/test/paths2/c2/", c2);

        assertTrue("Resource not found.", registry.resourceExists("/test/paths2/r1/"));
        assertTrue("Resource not found.", registry.resourceExists("/test/paths2/r2"));
        assertTrue("Resource not found.", registry.resourceExists("/test/paths2/c1/"));
        assertTrue("Resource not found.", registry.resourceExists("/test/paths2/c2"));
    }
}
