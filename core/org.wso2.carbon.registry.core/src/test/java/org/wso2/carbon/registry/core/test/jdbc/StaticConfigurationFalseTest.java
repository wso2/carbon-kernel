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

public class StaticConfigurationFalseTest extends BaseTestCase {

    protected static EmbeddedRegistryService embeddedRegistryService = null;
    protected static Registry registry = null;
    

    public void setUp() {
        setupCarbonHome();

        StaticConfiguration.setVersioningProperties(false);
        StaticConfiguration.setVersioningComments(false);
        StaticConfiguration.setVersioningTags(false);
        StaticConfiguration.setVersioningRatings(false);

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
                fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    public void testVersioningProperties() throws RegistryException {
        assertEquals(StaticConfiguration.isVersioningProperties(), false);
        Resource r = registry.newResource();
        r.setProperty("key1", "value1");
        registry.put("/testProperties", r);

        r = registry.get("/testProperties");

        r.setProperty("key2", "value2");
        registry.put("/testProperties", r);
        // to create the version
        registry.put("/testProperties", r);

        r = registry.get("/testProperties");
        assertEquals(r.getProperties().size(), 2);
        // retrieve versions
        String []versionPaths = registry.getVersions("/testProperties");

        assertEquals(versionPaths.length, 2);
        r = registry.get(versionPaths[1]);
        // still there should be a resource
        assertEquals(r.getProperties().size(), 2);
        assertEquals(r.getProperty("key2"), "value2");

        // same should be done to collections as well
        Resource c = registry.newCollection();
        c.setProperty("key1", "value1");
        registry.put("/testPropertiesC", c);
        registry.createVersion("/testPropertiesC");

        c.setProperty("key2", "value2");
        registry.put("/testPropertiesC", c);
        // to create the version
        registry.createVersion("/testPropertiesC");

        // retrieve versions
        versionPaths = registry.getVersions("/testPropertiesC");

        assertEquals(versionPaths.length, 2);
        c = registry.get(versionPaths[1]);
        // still there should be a resource
        assertEquals(c.getProperties().size(), 2);
        assertEquals(c.getProperty("key2"), "value2");

    }

    public void testVersioningComments() throws RegistryException {
        assertEquals(StaticConfiguration.isVersioningComments(), false);
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
        assertEquals(cs.length, 2);
        assertEquals(cs[1].getText(), "comment2");


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
        assertEquals(cs.length, 2);
        assertEquals(cs[1].getText(), "comment2");
    }


    public void testVersioningTags() throws RegistryException {
        assertEquals(StaticConfiguration.isVersioningTags(), false);
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
        assertEquals(tags.length, 2);
        assertEquals(tags[1].getTagName(), "tag2");


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
        assertEquals(tags.length, 2);
        assertEquals(tags[1].getTagName(), "tag2");
    }


    public void testVersioningRatings() throws RegistryException {
        assertEquals(StaticConfiguration.isVersioningRatings(), false);
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
        assertEquals(rating, 4);


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
        assertEquals(rating, 5);
    }
}
