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

import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

public class TransactionsTest extends BaseTestCase {

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
            
            RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
            registry = embeddedRegistryService.getUserRegistry(
                realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        } catch (RegistryException e) {
                fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    public void testSuccessfulSimpleTransaction() throws RegistryException {

        registry.beginTransaction();

        Resource r1 = registry.newResource();
        r1.setProperty("test", "t1");
        r1.setContent("some content");
        registry.put("/t1/r1", r1);

        registry.commitTransaction();

        Resource r1b = registry.get("/t1/r1");
        assertEquals("Completed transaction resource should have a " +
                "property named 'test' with value 't1'", r1b.getProperty("test"), "t1");
    }

     public void testFailedSimpleTransaction() throws RegistryException {

        registry.beginTransaction();

        Resource r2 = registry.newResource();
        r2.setProperty("test", "t2");
        r2.setContent("some content");
        registry.put("/t1/r2", r2);

        registry.rollbackTransaction();

        try {
            registry.get("/t1/r2");
            fail("Resource added by the incomplete transaction should be deleted.");
        } catch (RegistryException e) {}
    }

    public void testSuccessfulMultiOperationTransaction() throws RegistryException {

        registry.beginTransaction();

        Resource r1 = registry.newResource();
        r1.setProperty("test", "t2");
        r1.setContent("some content");
        registry.put("/t2/r1", r1);

        registry.addComment("/t2/r1", new Comment("this is a good one"));

        registry.rateResource("/t2/r1", 3);

        registry.applyTag("/t2/r1", "cool");

        registry.commitTransaction();

        Resource r1b = registry.get("/t2/r1");
        assertEquals("Completed transaction resource should have a " +
                "property named 'test' with value 't2'", r1b.getProperty("test"), "t2");

        Comment[] comments = registry.getComments("/t2/r1");
        assertEquals("Resource should have a comment", comments[0].getText(), "this is a good one");

        Tag[] tags = registry.getTags("/t2/r1");
        assertEquals("Resource should have a tag", tags[0].getTagName(), "cool");

        assertEquals("Average rating of /t2/r1 should be 3",
                registry.getAverageRating("/t2/r1"), 3f);
    }

    public void testFailedMultiOperationTransaction() throws RegistryException {

        registry.beginTransaction();

        Resource r1 = registry.newResource();
        r1.setProperty("test", "t2");
        r1.setContent("some content");
        registry.put("/t3/r1", r1);

        registry.addComment("/t3/r1", new Comment("this is a good one"));

        registry.rateResource("/t3/r1", 3);

        registry.applyTag("/t3/r1", "cool");

        registry.rollbackTransaction();

        try {
            registry.get("/t3/r1");
            fail("Resource /t3/r1 should be deleted after transaction is rolled back.");
        } catch (RegistryException e) {}

        Comment[] comments = registry.getComments("/t3/r1");
        assertEquals("Resource should not have comments", comments.length, 0);

        Tag[] tags = registry.getTags("/t3/r1");
        assertEquals("Resource should not have tags", tags.length, 0);

        assertEquals("Average rating of /t3/r1 should be 0",
                registry.getAverageRating("/t3/r1"), 0f);
    }


    public void testNestedSuccessfulMultiOperationTransaction() throws RegistryException {

        registry.beginTransaction();

        Resource r1 = registry.newResource();
        r1.setProperty("test", "t2");
        r1.setContent("some content");
        registry.put("t2/r1", r1);

        registry.beginTransaction();
        registry.addComment("/t2/r1", new Comment("this is a good one"));

        registry.rateResource("/t2/r1", 3);
        registry.commitTransaction();

        registry.applyTag("/t2/r1", "cool");

        registry.commitTransaction();

        Resource r1b = registry.get("/t2/r1");
        assertEquals("Completed transaction resource should have a " +
                "property named 'test' with value 't2'", r1b.getProperty("test"), "t2");

        Comment[] comments = registry.getComments("/t2/r1");
        assertEquals("Resource should have a comment", comments[0].getText(), "this is a good one");

        Tag[] tags = registry.getTags("/t2/r1");
        assertEquals("Resource should have a tag", tags[0].getTagName(), "cool");

        assertEquals("Average rating of /t2/r1 should be 3",
                registry.getAverageRating("/t2/r1"), 3f);
    }



    public void testNestedFailedMultiOperationTransaction() throws RegistryException {

        registry.beginTransaction();

        Resource r1 = registry.newResource();
        r1.setProperty("test", "t2");
        r1.setContent("some content");
        registry.put("/t32/r1", r1);
        registry.beginTransaction();

        registry.addComment("/t32/r1", new Comment("this is a good one"));

        registry.commitTransaction();
        registry.rateResource("/t32/r1", 3);

        registry.applyTag("/t32/r1", "cool");

        registry.rollbackTransaction();

        try {
            registry.get("/t32/r1");
            fail("Resource /t32/r1 should be deleted after transaction is rolled back.");
        } catch (RegistryException e) {}

        Comment[] comments = registry.getComments("/t32/r1");
        assertEquals("Resource should not have comments", comments.length, 0);

        Tag[] tags = registry.getTags("/t32/r1");
        assertEquals("Resource should not have tags", tags.length, 0);

        assertEquals("Average rating of /t32/r1 should be 0",
                registry.getAverageRating("/t32/r1"), 0f);
    }
}
