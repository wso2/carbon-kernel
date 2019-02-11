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

import java.util.List;

public class PropertiesTest extends BaseTestCase {

    protected static Registry registry = null;
    
    protected static EmbeddedRegistryService embeddedRegistryService = null;

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

    public void testRootLevelProperties() throws RegistryException {

        Resource root = registry.get("/");
        root.addProperty("p1", "v1");
        registry.put("/", root);

        Resource rootB = registry.get("/");
        assertEquals("Root should have a property named p1 with value v1",
                rootB.getProperty("p1"), "v1");
    }

    public void testSingleValuedProperties() throws RegistryException {

        Resource r2 = registry.newResource();
        r2.setContent("Some content for r2");
        r2.addProperty("p1", "p1v1");
        registry.put("/propTest/r2", r2);

        Resource r2b = registry.get("/propTest/r2");
        String p1Value = r2b.getProperty("p1");

        assertEquals("Property p1 of /propTest/r2 should contain the value p1v1",
                p1Value, "p1v1");
    }

    public void testMultiValuedProperties() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("Some content for r1");
        r1.addProperty("p1", "p1v1");
        r1.addProperty("p1", "p1v2");
        registry.put("/propTest/r1", r1);

        Resource r1b = registry.get("/propTest/r1");
        List<String> propValues = r1b.getPropertyValues("p1");

        assertTrue("Property p1 of /propTest/r1 should contain the value p1v1",
                propValues.contains("p1v1"));

        assertTrue("Property p1 of /propTest/r1 should contain the value p1v2",
                propValues.contains("p1v2"));
    }


    public void testNullValuedProperties() throws RegistryException {

        Resource r2 = registry.newResource();
        r2.setContent("Some content for r2");
        r2.addProperty("p1", null);
        registry.put("/propTest3/r2", r2);

        Resource r2b = registry.get("/propTest3/r2");
        String p1Value = r2b.getProperty("p1");

        assertEquals("Property p1 of /propTest3/r2 should contain the value null",
                p1Value, null);
    }

    public void testNullMultiValuedProperties() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("Some content for r1");
        r1.addProperty("p1", null);
        r1.addProperty("p1", null);
        registry.put("/propTest4/r1", r1);

        Resource r1b = registry.get("/propTest4/r1");
        List<String> propValues = r1b.getPropertyValues("p1");

        assertEquals("Property p1 of /propTest4/r1 should contain the value null",
                propValues.get(0), null);

        assertEquals("Property p1 of /propTest4/r1 should contain the value null",
                propValues.get(1), null);
    }

    public void testRemovingProperties() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.setProperty("p1", "v1");
        r1.setProperty("p2", "v2");
        registry.put("/props/t1/r1", r1);

        Resource r1e1 = registry.get("/props/t1/r1");
        r1e1.removeProperty("p1");
        registry.put("/props/t1/r1", r1e1);

        Resource r1e2 = registry.get("/props/t1/r1");

        assertEquals("Property is not removed.", r1e2.getProperty("p1"), null);
        assertNotNull("Wrong property is removed.", r1e2.getProperty("p2"));
    }

     public void testRemovingMultivaluedProperties() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.addProperty("p1", "v1");
        r1.addProperty("p1", "v2");
        registry.put("/props/t2/r1", r1);

        Resource r1e1 = registry.get("/props/t2/r1");
        r1e1.removePropertyValue("p1", "v1");
        registry.put("/props/t2/r1", r1e1);

        Resource r1e2 = registry.get("/props/t2/r1");

        assertFalse("Property is not removed.", r1e2.getPropertyValues("p1").contains("v1"));
        assertTrue("Wrong property is removed.", r1e2.getPropertyValues("p1").contains("v2"));
    }

     public void testEditingMultivaluedProperties() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.addProperty("p1", "v1");
        r1.addProperty("p1", "v2");
        registry.put("/props/t3/r1", r1);

        Resource r1e1 = registry.get("/props/t3/r1");
        r1e1.editPropertyValue("p1", "v1", "v3");
        registry.put("/props/t3/r1", r1e1);

        Resource r1e2 = registry.get("/props/t3/r1");

        assertFalse("Property is not edited.", r1e2.getPropertyValues("p1").contains("v1"));
        assertTrue("Property is not edited.", r1e2.getPropertyValues("p1").contains("v3"));
        assertTrue("Wrong property is removed.", r1e2.getPropertyValues("p1").contains("v2"));
    }
}
