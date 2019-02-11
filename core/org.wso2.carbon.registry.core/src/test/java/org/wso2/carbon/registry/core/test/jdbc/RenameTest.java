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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

public class RenameTest extends BaseTestCase {

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

    public void testRootLevelResourceRename() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/rename1", r1);

        registry.rename("/rename1", "/rename2");

        boolean failed = false;
        try {
            registry.get("/rename1");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Resource should not be accessible from the old path after renaming.", failed);

        Resource newR1 = registry.get("/rename2");
        assertEquals("Resource should contain a property with name test and value rename.",
                newR1.getProperty("test"), "rename");
    }

     public void testGeneralResourceRename() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/tests/rename1", r1);

        registry.rename("/tests/rename1", "rename2");

        boolean failed = false;
        try {
            registry.get("/tests/rename1");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Resource should not be accessible from the old path after renaming.", failed);

        Resource newR1 = registry.get("/tests/rename2");
        assertEquals("Resource should contain a property with name test and value rename.",
                newR1.getProperty("test"), "rename");
    }

     public void testRootLevelCollectionRename() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/rename3/c1/dummy", r1);

        registry.rename("/rename3", "rename4");

        boolean failed = false;
        try {
            registry.get("/rename3/c1/dummy");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Resource should not be " +
                "accessible from the old path after renaming the parent.", failed);

        Resource newR1 = registry.get("/rename4/c1/dummy");
        assertEquals("Resource should contain a property with name test and value rename.",
                newR1.getProperty("test"), "rename");
    }

    public void testGeneralCollectionRename() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/c2/rename3/c1/dummy", r1);

        registry.rename("/c2/rename3", "rename4");

        boolean failed = false;
        try {
            registry.get("/c2/rename3/c1/dummy");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Resource should not be " +
                "accessible from the old path after renaming the parent.", failed);

        Resource newR1 = registry.get("/c2/rename4/c1/dummy");
        assertEquals("Resource should contain a property with name test and value rename.",
                newR1.getProperty("test"), "rename");
    }
}
