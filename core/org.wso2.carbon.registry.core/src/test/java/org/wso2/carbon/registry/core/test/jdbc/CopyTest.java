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
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.RealmConfiguration;

public class CopyTest extends BaseTestCase {

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
            
            RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
            registry = embeddedRegistryService.getUserRegistry(
                realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        } catch (RegistryException e) {
                fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    public void testResourceCopy() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        r1.setContent("c");
        registry.put("/test/copy/c1/copy1", r1);

        Collection c1 = registry.newCollection();
        registry.put("/test/move", c1);

        registry.copy("/test/copy/c1/copy1", "/test/copy/c2/copy1");

        Resource newR1 = registry.get("/test/copy/c2/copy1");
        assertEquals("Copied resource should have a property named 'test' with value 'copy'.",
                newR1.getProperty("test"), "copy");

        Resource oldR1 = registry.get("/test/copy/c1/copy1");
        assertEquals("Original resource should have a property named 'test' with value 'copy'.",
                oldR1.getProperty("test"), "copy");

        String newContent = RegistryUtils.decodeBytes((byte[]) newR1.getContent());
        String oldContent = RegistryUtils.decodeBytes((byte[]) oldR1.getContent());
        assertEquals("Contents are not equal in copied resources", newContent, oldContent);
    }

    public void testCollectionCopy() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        r1.setContent("c");
        registry.put("/test/copy/copy3/c3/resource1", r1);

        Collection c1 = registry.newCollection();
        registry.put("/test/move", c1);

        registry.copy("/test/copy/copy3", "/test/newCol/copy3");

        Resource newR1 = registry.get("/test/newCol/copy3/c3/resource1");
        assertEquals("Copied resource should have a property named 'test' with value 'copy'.",
                newR1.getProperty("test"), "copy");

        Resource oldR1 = registry.get("/test/copy/copy3/c3/resource1");
        assertEquals("Original resource should have a property named 'test' with value 'copy'.",
                oldR1.getProperty("test"), "copy");
    }

    public void testResourceCopyWithComments() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        r1.setContent("c");
        registry.put("/test/copy/c1/copy1", r1);
        registry.addComment("/test/copy/c1/copy1", new Comment("comment1"));
        registry.addComment("/test/copy/c1/copy1", new Comment("comment2"));

        Collection c1 = registry.newCollection();
        registry.put("/test/move", c1);

        registry.copy("/test/copy/c1/copy1", "/test/copy/c4/copy1");

        Resource newR1 = registry.get("/test/copy/c4/copy1");
        assertEquals("Copied resource should have a property named 'test' with value 'copy'.",
                newR1.getProperty("test"), "copy");

        Resource oldR1 = registry.get("/test/copy/c1/copy1");
        assertEquals("Original resource should have a property named 'test' with value 'copy'.",
                oldR1.getProperty("test"), "copy");

        String newContent = RegistryUtils.decodeBytes((byte[]) newR1.getContent());
        String oldContent = RegistryUtils.decodeBytes((byte[]) oldR1.getContent());
        assertEquals("Contents are not equal in copied resources", newContent, oldContent);

        Comment[] comments = registry.getComments("/test/copy/c4/copy1");
        assertEquals("Copied resource should have two comments", 2, comments.length);
    }

    public void testResourceCopyWithTags() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        r1.setContent("c");
        registry.put("/test/copy/c1/copy1", r1);
        registry.applyTag("/test/copy/c1/copy1", "tag1");
        registry.applyTag("/test/copy/c1/copy1", "tag2");

        Collection c1 = registry.newCollection();
        registry.put("/test/move", c1);

        registry.copy("/test/copy/c1/copy1", "/test/copy/c5/copy1");

        Resource newR1 = registry.get("/test/copy/c5/copy1");
        assertEquals("Copied resource should have a property named 'test' with value 'copy'.",
                newR1.getProperty("test"), "copy");

        Resource oldR1 = registry.get("/test/copy/c1/copy1");
        assertEquals("Original resource should have a property named 'test' with value 'copy'.",
                oldR1.getProperty("test"), "copy");

        String newContent = RegistryUtils.decodeBytes((byte[]) newR1.getContent());
        String oldContent = RegistryUtils.decodeBytes((byte[]) oldR1.getContent());
        assertEquals("Contents are not equal in copied resources", newContent, oldContent);

        Tag[] tags = registry.getTags("/test/copy/c5/copy1");
        assertEquals("Copied resource should have two tags", 2, tags.length);
    }
}
