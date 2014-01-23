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
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

public class MoveTest extends BaseTestCase {

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


    public void testResourceMoveFromRoot() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/move1", r1);

        Collection c1 = registry.newCollection();
        registry.put("/test/move", c1);

        registry.move("/move1", "/test/move/move1");

        Resource newR1 = registry.get("/test/move/move1");
        assertEquals("Moved resource should have a property named 'test' with value 'move'.",
                newR1.getProperty("test"), "move");

        boolean failed = false;
        try {
            registry.get("/move1");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Moved resource should not be accessible from the old path.", failed);
    }

    public void testResourceMoveToRoot() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/move/move2", r1);

        registry.move("/test/move/move2", "/move2");

        Resource newR1 = registry.get("/move2");
        assertEquals("Moved resource should have a property named 'test' with value 'move'.",
                newR1.getProperty("test"), "move");

        boolean failed = false;
        try {
            registry.get("/test/move/move2");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Moved resource should not be accessible from the old path.", failed);
    }

    public void testGeneralResourceMove() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/c1/move/move3", r1);
        registry.addComment("/test/c1/move/move3", new Comment("comment1"));
        registry.applyTag("/test/c1/move/move3", "test1");
        registry.rateResource("/test/c1/move/move3", 4);

        Collection c2 = registry.newCollection();
        registry.put("/test/c2/move", c2);

        registry.move("/test/c1/move/move3", "/test/c2/move/move3");

        Resource newR1 = registry.get("/test/c2/move/move3");
        assertEquals("Moved resource should have a property named 'test' with value 'move'.",
                newR1.getProperty("test"), "move");
        Comment[] comments = registry.getComments("/test/c2/move/move3");
        assertEquals("Moved resource resource should have 1 comment", comments.length, 1);
        assertEquals("Moved Resource comment", comments[0].getText(), "comment1");
        Tag[] tags = registry.getTags("/test/c2/move/move3");
        assertEquals("Moved resource should have 1 tag", tags.length, 1);
        assertEquals("Moved Resource Tag", tags[0].getTagName(), "test1");
        int rating = registry.getRating("/test/c2/move/move3", "admin");
        assertEquals("Rating should be 4", rating, 4);


        boolean failed = false;
        try {
            registry.get("/test/c1/move/move3");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Moved resource should not be accessible from the old path.", failed);
    }

    public void testGeneralCollectionMove() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/c1/move5/move/dummy", r1);

        Collection c2 = registry.newCollection();
        registry.put("/test/c3", c2);

        registry.move("/test/c1/move5", "/test/c3/move5");

        Resource newR1 = registry.get("/test/c3/move5/move/dummy");
        assertEquals("Moved resource should have a property named 'test' with value 'move'.",
                newR1.getProperty("test"), "move");

        boolean failed = false;
        try {
            registry.get("/test/c1/move5/move/dummy");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Moved resource should not be accessible from the old path.", failed);
    }


    public void testCollectionMoveWithChild() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("testX", "move");
        r1.setContent("c");
        registry.put("/testX/c1/move/move3", r1);
        registry.addComment("/testX/c1/move/move3", new Comment("comment1"));
        registry.applyTag("/testX/c1/move/move3", "test1");
        registry.rateResource("/testX/c1/move/move3", 4);


        registry.move("/testX/c1/", "/testX/c2/");

        Resource newR1 = registry.get("/testX/c2/move/move3");
        assertEquals("Moved resource should have a property named 'testX' with value 'move'.",
                newR1.getProperty("testX"), "move");
        Comment[] comments = registry.getComments("/testX/c2/move/move3");
        assertEquals("Moved resource resource should have 1 comment", comments.length, 1);
        assertEquals("Moved Resource comment", comments[0].getText(), "comment1");
        Tag[] tags = registry.getTags("/testX/c2/move/move3");
        assertEquals("Moved resource should have 1 tag", tags.length, 1);
        assertEquals("Moved Resource Tag", tags[0].getTagName(), "test1");
        int rating = registry.getRating("/testX/c2/move/move3", "admin");
        assertEquals("Rating should be 4", rating, 4);


        boolean failed = false;
        try {
            registry.get("/testX/c1/move/move3");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Moved resource should not be accessible from the old path.", failed);
    }
    

    public void testGeneralCacheCollectionMove() throws RegistryException {
        Collection c1 = registry.newCollection();
        registry.put("/test/c1", c1);
        registry.move("/test/c1", "/test/c2");

        boolean failed = false;
        try {
            registry.get("/test/c1");
        } catch (RegistryException e) {
            failed = true;
         }
        assertTrue("Moved collection should not be accessible from the old path.", failed);
    }

    public void testGeneralCacheResourceMove() throws RegistryException {
        Resource r1 = registry.newResource();
        registry.put("/test/r1", r1);
        registry.move("/test/r1", "/test/r2");

        boolean failed = false;
        try {
            registry.get("/test/r1");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("Moved resource should not be accessible from the old path.", failed);
    }
}
