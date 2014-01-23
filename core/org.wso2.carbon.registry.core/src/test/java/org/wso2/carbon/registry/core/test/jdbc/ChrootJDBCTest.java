/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.RealmConfiguration;

/**
 * Execute all the JDBC registry tests with a non-standard root.
 */
public class ChrootJDBCTest extends JDBCRegistryTest {
    protected static Registry originalRegistry = null;

    public void setUp() {
        super.setUp();
        try {
            RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
            originalRegistry =
                    embeddedRegistryService.getUserRegistry(realmConfig.getAdminUserName());
            registry = embeddedRegistryService.getUserRegistry(realmConfig.getAdminUserName(), 
            		MultitenantConstants.SUPER_TENANT_ID, "/basePrefix");
            systemRegistry = embeddedRegistryService.getSystemRegistry(
            		MultitenantConstants.SUPER_TENANT_ID, "/basePrefix");
        } catch (RegistryException e) {
                e.printStackTrace();
                fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    public void testPrefixManagement() throws Exception {
        Resource r = registry.newResource();
        r.setContent("hi there");
        registry.put("/testResource", r);
        r = originalRegistry.get("/basePrefix/testResource");
        // Make sure that this got put in the right "real" place
        assertNotNull(r);
    }
}
