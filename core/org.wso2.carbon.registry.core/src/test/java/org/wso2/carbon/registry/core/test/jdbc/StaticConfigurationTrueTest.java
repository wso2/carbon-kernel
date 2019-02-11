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
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;

public class StaticConfigurationTrueTest extends BaseTestCase {

    protected static EmbeddedRegistryService embeddedRegistryService = null;
    protected static Registry registry = null;
    

    public void setUp() {
        setupCarbonHome();

        StaticConfiguration.setVersioningProperties(true);
        StaticConfiguration.setVersioningComments(true);
        StaticConfiguration.setVersioningTags(true);
        StaticConfiguration.setVersioningRatings(true);

        setupContext();

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
            throw new RuntimeException(e);
            //fail("Failed to initialize the registry. Caused by: " + e.getMessage());

        }
    }

    public void testVersioningProperties() throws RegistryException {
        assertEquals(StaticConfiguration.isVersioningProperties(), true);
        Resource r = registry.newResource();
        r.setProperty("key1", "value1");
        r.setProperty("key2", "value2");
        registry.put("/testProperties", r);

        r = registry.get("/testProperties");

        r.setProperty("key3", "value3");
        r.setProperty("key1", "value1dup");
        registry.put("/testProperties", r);
        // to create the version
        registry.put("/testProperties", r);

        r = registry.get("/testProperties");
        assertEquals(r.getProperties().size(), 3);
        

        // retrieve versions
        String []versionPaths = registry.getVersions("/testProperties");

        assertEquals(versionPaths.length, 2);

        registry.restoreVersion(versionPaths[1]);

        r = registry.get("/testProperties");
        // still there should be a resource
        assertEquals(r.getProperties().size(), 2);
        assertEquals(r.getProperty("key1"), "value1");

        // again getting the latest version
        r = registry.get(versionPaths[0]);
        // still there should be a resource
        assertEquals(r.getProperties().size(), 3);
        assertEquals(r.getProperty("key1"), "value1dup");

        // same should be done to collections as well
        Resource c = registry.newCollection();
        c.setProperty("key1", "value1");
        c.setProperty("key2", "value2");
        registry.put("/testPropertiesC", c);
        registry.createVersion("/testPropertiesC");

        c = registry.get("/testPropertiesC");

        c.setProperty("key3", "value3");
        c.setProperty("key1", "value1dup");
        registry.put("/testPropertiesC", c);

        // to create the version
        registry.createVersion("/testPropertiesC");
        c = registry.get("/testPropertiesC");
        assertEquals(c.getProperties().size(), 3);

        // retrieve versions
        versionPaths = registry.getVersions("/testPropertiesC");

        assertEquals(versionPaths.length, 2);

        registry.restoreVersion(versionPaths[1]);

        c = registry.get("/testPropertiesC");
        // still there should be a resource
        assertEquals(c.getProperties().size(), 2);
        assertEquals(c.getProperty("key1"), "value1");

        c = registry.get(versionPaths[0]);
        // still there should be a resource
        assertEquals(c.getProperties().size(), 3);
        assertEquals(c.getProperty("key1"), "value1dup");

    }

    public void testVersioningComments() throws RegistryException {
        assertEquals(StaticConfiguration.isVersioningComments(), true);
        Resource r = registry.newResource();
        registry.put("/testComments", r);
        Comment c1 = new Comment("comment1");
        registry.addComment("/testComments", c1);
        registry.createVersion("/testComments");

        Comment c2 = new Comment("comment2");
        registry.addComment("/testComments", c2);
        // to create the version
        registry.createVersion("/testComments");

        // retrieve versions
        String []versionPaths = registry.getVersions("/testComments");

        assertEquals(versionPaths.length, 2);

        // still there should be the comments
        registry.restoreVersion(versionPaths[1]);
        Comment[] cs = registry.getComments("/testComments");
        assertEquals(cs.length, 1);
        assertEquals(cs[0].getText(), "comment1");


        Resource c = registry.newCollection();
        registry.put("/testCommentsC", c);
        c1 = new Comment("comment1");
        registry.addComment("/testCommentsC", c1);
        registry.createVersion("/testCommentsC");

        c2 = new Comment("comment2");
        registry.addComment("/testCommentsC", c2);
        // to create the version
        registry.createVersion("/testCommentsC");

        // retrieve versions
        versionPaths = registry.getVersions("/testCommentsC");

        assertEquals(versionPaths.length, 2);

        // still there should be the comments
        registry.restoreVersion(versionPaths[1]);
        cs = registry.getComments("/testCommentsC");
        assertEquals(cs.length, 1);
        assertEquals(cs[0].getText(), "comment1");
    }


    public void testVersioningTags() throws RegistryException {
        assertEquals(StaticConfiguration.isVersioningTags(), true);
        Resource r = registry.newResource();
        registry.put("/testTags", r);
        registry.applyTag("/testTags", "tag1");
        registry.createVersion("/testTags");

        registry.applyTag("/testTags", "tag2");
        // to create the version
        registry.createVersion("/testTags");

        // retrieve versions
        String []versionPaths = registry.getVersions("/testTags");

        assertEquals(versionPaths.length, 2);

        // still there should be the comments
        registry.restoreVersion(versionPaths[1]);
        Tag[] tags = registry.getTags("/testTags");
        assertEquals(tags.length, 1);
        assertEquals(tags[0].getTagName(), "tag1");


        Resource c = registry.newCollection();
        registry.put("/testTagsC", c);
        registry.applyTag("/testTagsC", "tag1");
        registry.createVersion("/testTagsC");

        registry.applyTag("/testTagsC", "tag2");
        // to create the version
        registry.createVersion("/testTagsC");

        // retrieve versions
        versionPaths = registry.getVersions("/testTagsC");

        assertEquals(versionPaths.length, 2);

        // still there should be the comments
        registry.restoreVersion(versionPaths[1]);
        tags = registry.getTags("/testTagsC");
        assertEquals(tags.length, 1);
        assertEquals(tags[0].getTagName(), "tag1");
    }


    public void testVersioningRatings() throws RegistryException {
        assertEquals(StaticConfiguration.isVersioningRatings(), true);
        Resource r = registry.newResource();
        registry.put("/testRatings", r);
        registry.rateResource("/testRatings", 3);
        registry.createVersion("/testRatings");

        registry.rateResource("/testRatings", 4);
        // to create the version
        registry.createVersion("/testRatings");

        // retrieve versions
        String []versionPaths = registry.getVersions("/testRatings");

        assertEquals(versionPaths.length, 2);

        // still there should be the comments
        registry.restoreVersion(versionPaths[1]);
        int rating = registry.getRating("/testRatings", "admin");
        assertEquals(rating, 3);


        Resource c = registry.newCollection();
        registry.put("/testRatingsC", c);
        registry.rateResource("/testRatingsC", 2);
        registry.createVersion("/testRatingsC");

        registry.rateResource("/testRatingsC", 5);
        // to create the version
        registry.createVersion("/testRatingsC");

        // retrieve versions
        versionPaths = registry.getVersions("/testRatingsC");

        assertEquals(versionPaths.length, 2);

        // still there should be the comments
        registry.restoreVersion(versionPaths[1]);
        rating = registry.getRating("/testRatingsC", "admin");
        assertEquals(rating, 2);
    }
}
