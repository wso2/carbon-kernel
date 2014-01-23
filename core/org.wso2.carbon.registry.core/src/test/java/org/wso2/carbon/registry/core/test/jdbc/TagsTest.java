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

import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;

public class TagsTest extends BaseTestCase {
    /**
     * Registry instance for use in tests. Note that there should be only one Registry instance in a
     * JVM.
     */
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
            comp.registerBuiltInHandlers(embeddedRegistryService);
            
            // get the realm config to retrieve admin username, password
            RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
            registry = embeddedRegistryService.getUserRegistry(
                realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    public void testTagging() throws Exception {
        // add a resource
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d11/r1", r1);

        Resource r2 = registry.newResource();
        byte[] r2content = "R2 content".getBytes();
        r2.setContent(r2content);
        registry.put("/d11/r2", r2);

        Resource r3 = registry.newResource();
        byte[] r3content = "R3 content".getBytes();
        r3.setContent(r3content);
        registry.put("/d11/r3", r3);

        registry.applyTag("/d11/r1", "JSP");
        registry.applyTag("/d11/r2", "jsp");
        registry.applyTag("/d11/r3", "jaVa");

        registry.applyTag("/d11/r1", "jsp");
        Tag[] r11Tags = registry.getTags("/d11/r1");
        assertEquals(1, r11Tags.length);
        
        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("jsp");
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            if (path.getResourcePath().equals("/d11/r1")) {
                artifactFound = true;
                break;
            }
        }
        assertTrue("/d11/r1 is not tagged with the tag \"jsp\"", artifactFound);

        Tag[] tags = registry.getTags("/d11/r1");

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equalsIgnoreCase("jsp")) {
                tagFound = true;
                break;
            }
        }
        assertTrue("tag 'jsp' is not associated with the artifact /d11/r1", tagFound);

        registry.delete("/d11");

        TaggedResourcePath[] paths2 = registry.getResourcePathsWithTag("jsp");

        assertEquals("Tag based search should not return paths of deleted resources.",
                paths2.length, 0);
    }

    public void testMultipleUserTags() throws Exception {

        // embeddedRegistryService = new InMemoryEmbeddedRegistryService();
        // get the realm config to retrieve admin username, password
        RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
        
        UserRegistry adminRegistry = embeddedRegistryService.
                getUserRegistry(realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        UserRealm adminRealm = adminRegistry.getUserRealm();

        adminRealm.getUserStoreManager().addUser("foo", "cce123", null, null, null);
        adminRealm.getUserStoreManager();

        adminRealm.getAuthorizationManager().
                authorizeUser("foo", RegistryConstants.ROOT_PATH, ActionConstants.PUT);
        adminRealm.getUserStoreManager().addUser("bar", "swe123", null, null, null);

        UserRegistry fooRegistry = embeddedRegistryService.getUserRegistry("foo", "cce123");
        UserRegistry barRegistry = embeddedRegistryService.getUserRegistry("bar", "swe123");

        String r1Content = "R1";
        Resource r1 = fooRegistry.newResource();
        r1.setContent(r1Content.getBytes());

        fooRegistry.put("/r1", r1);

        adminRegistry.applyTag("/r1", "java");
        fooRegistry.applyTag("/r1", "java");
        barRegistry.applyTag("/r1", "java");

        Tag[] tags1 = adminRegistry.getTags("/r1");
        assertEquals("There should be 3 taggings on resource '/r1'", 3, tags1[0].getTagCount());

        // owner of the /r1 removes a tag. all tags should be removed
        fooRegistry.removeTag("/r1", "java");
        Tag[] tags2 = adminRegistry.getTags("/r1");
        assertEquals("There should be 0 taggings on resource '/r1'", 0, tags2.length);

        adminRegistry.applyTag("/r1", "java");
        fooRegistry.applyTag("/r1", "java");
        barRegistry.applyTag("/r1", "java");

        // admin removes a tag. all tags should be removed
        adminRegistry.removeTag("/r1", "java");
        Tag[] tags3 = adminRegistry.getTags("/r1");
        assertEquals("There should be 0 taggings on resource '/r1'", 0, tags3.length);

        adminRegistry.applyTag("/r1", "java");
        fooRegistry.applyTag("/r1", "java");
        barRegistry.applyTag("/r1", "java");

        // normal user removes a tag. only the tag applied by the user is removed
        barRegistry.removeTag("/r1", "java");
        Tag[] tags4 = adminRegistry.getTags("/r1");
        assertEquals("There should be 2 taggings on resource '/r1'", 2, tags4[0].getTagCount());
    }
}
