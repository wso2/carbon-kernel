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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

import java.util.HashMap;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommentsTest extends BaseTestCase {

    protected static Registry registry = null;
    protected static Registry systemRegistry = null;
    protected static Registry configSystemRegistry = null;
    protected static Registry localRepository = null;

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
            systemRegistry = embeddedRegistryService.getSystemRegistry();
            configSystemRegistry = embeddedRegistryService.getConfigSystemRegistry();
            localRepository = embeddedRegistryService.getLocalRepository();
        } catch (RegistryException e){
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    @Test
    public void test1CommentQueryRootRegistry() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        registry.put("/test/comments/r1", r1);

        registry.addComment("/test/comments/r1", new Comment("commentXX1 on this resource :)"));
        registry.addComment("/test/comments/r1", new Comment("commentXX2 on this resource :)"));

        Resource comQuery = configSystemRegistry.newResource();
        String sql = "SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC " +
                    "WHERE C.REG_COMMENT_TEXT LIKE ? AND C.REG_ID=RC.REG_COMMENT_ID";
                
        comQuery.setContent(sql);

        comQuery.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        comQuery.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.COMMENTS_RESULT_TYPE);
        
        configSystemRegistry.put("/test/comments/q1", comQuery);

        Map<String, String> params = new HashMap <String, String> ();
        params.put("1", "commentXX1%");
        Collection qResults = registry.executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                "/test/comments/q1", params);

        String[] qPaths = (String[]) qResults.getContent();

        assertEquals("Query result count should be 1", qPaths.length, 1);

        Resource qResult = registry.get(qPaths[0]);

        assertTrue("Comment query should return Comment objects as results",
                qResult instanceof Comment);

        Comment c1 = (Comment) qResult;
        
        assertNotNull("Comment query result is invalid", c1.getText());
        assertNotNull("Comment query result is invalid", c1.getUser());
        assertNotNull("Comment query result is invalid", c1.getCreatedTime());
    }

    @Test
    public void test2CommentQueryNonRootRegistry() throws RegistryException {

        Resource r1 = localRepository.newResource();
        r1.setContent("r1 content");
        localRepository.put("/test/comments/r1", r1);

        localRepository.addComment("/test/comments/r1",
                new Comment("commentXX1 on this resource :)"));
        localRepository.addComment("/test/comments/r1",
                new Comment("commentXX2 on this resource :)"));

        Resource comQuery = configSystemRegistry.newResource();
        String sql = "SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC " +
                    "WHERE C.REG_COMMENT_TEXT LIKE ? AND C.REG_ID=RC.REG_COMMENT_ID";

        comQuery.setContent(sql);

        comQuery.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        comQuery.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.COMMENTS_RESULT_TYPE);

        configSystemRegistry.put("/test/comments/q1", comQuery);

        Map<String, String> params = new HashMap <String, String> ();
        params.put("1", "commentXX1%");
        Collection qResults = localRepository.executeQuery("/test/comments/q1", params);

        String[] qPaths = (String[]) qResults.getContent();

        assertEquals("Query result count should be 1", qPaths.length, 1);

        Resource qResult = localRepository.get(qPaths[0]);

        assertTrue("Comment query should return Comment objects as results",
                qResult instanceof Comment);

        Comment c1 = (Comment) qResult;

        assertNotNull("Comment query result is invalid", c1.getText());
        assertNotNull("Comment query result is invalid", c1.getUser());
        assertNotNull("Comment query result is invalid", c1.getCreatedTime());
    }

    @SuppressWarnings("unused")
    @Test
    public void test3CommentDelete() throws RegistryException {

        String r1Path = "/c1d1/c1";
        Collection r1 = registry.newCollection();
        registry.put(r1Path, r1);

        String c1Path = registry.addComment(r1Path, new Comment("test comment1"));
        String c2Path = registry.addComment(r1Path, new Comment("test comment2"));

        Comment[] comments1 = registry.getComments(r1Path);

        assertEquals("There should be two comments.", comments1.length, 2);

        String[] cTexts1 = {comments1[0].getText(), comments1[1].getText()};

        assertTrue("comment is missing", containsString(cTexts1, "test comment1"));
        assertTrue("comment is missing", containsString(cTexts1, "test comment2"));

        registry.delete(c1Path);

        Comment[] comments2 = registry.getComments(r1Path);

        assertEquals("There should be one comment.", 1, comments2.length);

        String[] cTexts2 = {comments2[0].getText()};

        assertTrue("comment is missing", containsString(cTexts2, "test comment2"));
        assertTrue("deleted comment still exists", !containsString(cTexts2, "test comment1"));
    }


    private boolean containsString(String[] array, String value) {

        boolean found = false;
        for (String anArray : array) {
            if (anArray.startsWith(value)) {
                found = true;
                break;
            }
        }

        return found;
    }
}
