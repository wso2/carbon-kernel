/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.test.jdbc;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CustomQueryTest  extends BaseTestCase {

    protected static Registry registry = null;
    protected static Registry configSystemRegistry = null;

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

            RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
            registry = embeddedRegistryService.getGovernanceUserRegistry(
                    realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
            configSystemRegistry = embeddedRegistryService.getConfigSystemRegistry();
        } catch (RegistryException e){
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    @Test
    public void test1AssociationsQuery() throws Exception {
        Resource r1 = registry.newResource();
        String r1Content = "this is r1 content";
        r1.setContent(r1Content.getBytes());
        r1.setDescription("production ready.");
        String r1Path = "/ca1/r1";
        registry.put(r1Path, r1);

        Resource r2 = registry.newResource();
        String r2Content = "content for r2 :)";
        r2.setContent(r2Content);
        r2.setDescription("ready for production use.");
        String r2Path = "/ca1/r2";
        registry.put(r2Path, r2);

        Resource r3 = registry.newResource();
        String r3Content = "content for r3 :)";
        r3.setContent(r3Content);
        r3.setDescription("only for government use.");
        String r3Path = "/ca1/r3";
        registry.put(r3Path, r3);

        Resource r4 = registry.newResource();
        String r4Content = "this is r4 content";
        r4.setContent(r4Content.getBytes());
        r4.setDescription("production ready.");
        String r4Path = "/ca1/r4";
        registry.put(r4Path, r4);

        registry.addAssociation("/ca1/r1", "http://localhost/", "depends");
        registry.addAssociation("/ca1/r1", "/ca1/r3", "usedBy");
        registry.addAssociation("/ca1/r2", "/ca1/r3", "depends");

        String sql1 = "SELECT * FROM REG_RESOURCE R, REG_PATH P, REG_ASSOCIATION A WHERE " +
                "P.REG_PATH_ID = R.REG_PATH_ID AND A.REG_SOURCEPATH LIKE P.REG_PATH_VALUE " +
                "|| '/' || R.REG_NAME AND A.REG_TARGETPATH LIKE ? AND A.REG_ASSOCIATION_TYPE = ?";
        Resource q1 = configSystemRegistry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.RESOURCES_RESULT_TYPE);
        configSystemRegistry.put("/qs/qa1", q1);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("1", "%http%");
        parameters.put("2", "depends");
        Resource result = registry.executeQuery("/qs/qa1", parameters);

        assertTrue("Search with result type Resource should return a directory.",
                result instanceof org.wso2.carbon.registry.core.Collection);

        List<String> matchingPaths = new ArrayList<String>();
        String[] paths = (String[])result.getContent();
        matchingPaths.addAll(Arrays.asList(paths));

        assertTrue("Path /ca1/r1 should be in the results.", matchingPaths.contains("/ca1/r1"));
        assertEquals("Incorrect number of results returned", 1, matchingPaths.size());
    }

    @Test
    public void test2AssociationsCollectionQuery() throws Exception {
        Resource r1 = registry.newCollection();
        r1.setDescription("production ready.");
        String r1Path = "/cb1/r1";
        registry.put(r1Path, r1);

        Resource r2 = registry.newCollection();
        r2.setDescription("ready for production use.");
        String r2Path = "/cb1/r2";
        registry.put(r2Path, r2);

        Resource r3 = registry.newCollection();
        r3.setDescription("only for government use.");
        String r3Path = "/cb1/r3";
        registry.put(r3Path, r3);

        Resource r4 = registry.newCollection();
        r4.setDescription("production ready.");
        String r4Path = "/cb1/r4";
        registry.put(r4Path, r4);

        registry.addAssociation("/cb1/r1", "http://localhost/", "depends");
        registry.addAssociation("/cb1/r2", "/cb1/r3", "usedBy");
        registry.addAssociation("/cb1/r2", "/cb1/r3", "depends");

        String sql1 = "SELECT * FROM REG_RESOURCE R, REG_PATH P, REG_ASSOCIATION A WHERE " +
                "P.REG_PATH_ID = R.REG_PATH_ID AND A.REG_SOURCEPATH LIKE P.REG_PATH_VALUE " +
                "AND A.REG_TARGETPATH LIKE ? AND A.REG_ASSOCIATION_TYPE = ?";
        Resource q1 = configSystemRegistry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.RESOURCES_RESULT_TYPE);
        configSystemRegistry.put("/qs/qa1", q1);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("1", "%r3%");
        parameters.put("2", "usedBy");
        Resource result = registry.executeQuery("/qs/qa1", parameters);

        assertTrue("Search with result type Resource should return a directory.",
                result instanceof org.wso2.carbon.registry.core.Collection);

        List<String> matchingPaths = new ArrayList<String>();
        String[] paths = (String[])result.getContent();
        matchingPaths.addAll(Arrays.asList(paths));

        assertTrue("Path /cb1/r2 should be in the results.", matchingPaths.contains("/cb1/r2"));
        assertEquals("Incorrect number of results returned", 1, matchingPaths.size());
    }

    @Test
    public void test3QueryAsParameter() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        registry.put("/test/comments/r1", r1);

        registry.addComment("/test/comments/r1", new Comment("commentXX1 on this resource :)"));
        registry.addComment("/test/comments/r1", new Comment("commentXX2 on this resource :)"));

        String sql = "SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC " +
                "WHERE C.REG_COMMENT_TEXT LIKE ? AND C.REG_ID=RC.REG_COMMENT_ID";

        Map<String, String> params = new HashMap <String, String> ();
        params.put("query", sql);
        params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.COMMENTS_RESULT_TYPE);
        params.put("1", "commentXX1%");
        Collection qResults = registry.executeQuery("/test/qap", params);

        String[] qPaths = (String[]) qResults.getContent();

        assertEquals("Query result count should be 1", qPaths.length, 1);
    }

    @Test
    public void test4CustomQueryResultsOrderForComments() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("r1x content");
        registry.put("/test/comments/r1x", r1);

        registry.addComment("/test/comments/r1x", new Comment("commentXX1 on this resource :)"));
        registry.addComment("/test/comments/r1x", new Comment("commentXX2 on this resource :)"));
        registry.addComment("/test/comments/r1x", new Comment("commentXX3 on this resource :)"));
        registry.addComment("/test/comments/r1x", new Comment("a new test comment"));

        Resource comQuery = configSystemRegistry.newResource();
        String sql = "SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC " +
                "WHERE C.REG_COMMENT_TEXT LIKE ? AND C.REG_ID=RC.REG_COMMENT_ID " +
                "AND RC.REG_RESOURCE_NAME=? ORDER BY C.REG_COMMENTED_TIME DESC";

        comQuery.setContent(sql);

        comQuery.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        comQuery.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.COMMENTS_RESULT_TYPE);

        configSystemRegistry.put("/test/comments/q1", comQuery);

        Map<String, String> params = new HashMap<String, String>();
        params.put("1", "%comment%");
        params.put("2", "r1x");
        Collection qResults = registry.executeQuery("/test/comments/q1", params);

        String[] qPaths = (String[]) qResults.getContent();

        assertEquals("Query result count should be 4", qPaths.length, 4);
        // we can't expect the comment number to be 4 , 3, 2, 1 if already comments there in the
        // database it will take different numbers
        assertEquals("Comment query result is invalid",
                ((Comment) registry.get(qPaths[0])).getText(),
                "a new test comment");
        assertEquals("Comment query result is invalid",
                ((Comment) registry.get(qPaths[1])).getText(),
                "commentXX3 on this resource :)");
        assertEquals("Comment query result is invalid",
                ((Comment) registry.get(qPaths[2])).getText(),
                "commentXX2 on this resource :)");
        assertEquals("Comment query result is invalid",
                ((Comment) registry.get(qPaths[3])).getText(),
                "commentXX1 on this resource :)");

        Resource qResult = registry.get(qPaths[0]);

        assertTrue("Comment query should return Comment objects as results",
                qResult instanceof Comment);

        Comment c1 = (Comment) qResult;

        assertNotNull("Comment query result is invalid", c1.getText());
        assertNotNull("Comment query result is invalid", c1.getUser());
        assertNotNull("Comment query result is invalid", c1.getCreatedTime());

        Resource r2 = registry.newResource();
        r2.setContent("r2 content");
        registry.put("/test/comments/r2", r2);

        registry.addComment("/test/comments/r2", new Comment("commentXX1 on this resource :)"));
        registry.addComment("/test/comments/r2", new Comment("commentXX2 on this resource :)"));
        registry.addComment("/test/comments/r2", new Comment("commentXX3 on this resource :)"));
        registry.addComment("/test/comments/r2", new Comment("a new test comment"));

        comQuery = configSystemRegistry.newResource();
        sql = "SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC " +
                "WHERE C.REG_COMMENT_TEXT LIKE ? AND C.REG_ID=RC.REG_COMMENT_ID " +
                "AND RC.REG_RESOURCE_NAME=? ORDER BY C.REG_COMMENTED_TIME LIMIT ?, ?";

        comQuery.setContent(sql);

        comQuery.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        comQuery.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.COMMENTS_RESULT_TYPE);

        configSystemRegistry.put("/test/comments/q1", comQuery);

        params = new HashMap<String, String>();
        params.put("1", "%comment%");
        params.put("2", "r2");
        params.put("3", "1");
        params.put("4", "4");
        qResults = registry.executeQuery("/test/comments/q1", params);

        qPaths = (String[]) qResults.getContent();

        assertEquals("Query result count should be 3", qPaths.length, 3);
        assertEquals("Comment query result is invalid",
                ((Comment) registry.get(qPaths[2])).getText(),
                "a new test comment");
        assertEquals("Comment query result is invalid",
                ((Comment) registry.get(qPaths[1])).getText(),
                "commentXX3 on this resource :)");
        assertEquals("Comment query result is invalid",
                ((Comment) registry.get(qPaths[0])).getText(),
                "commentXX2 on this resource :)");

        qResult = registry.get(qPaths[0]);

        assertTrue("Comment query should return Comment objects as results",
                qResult instanceof Comment);

        c1 = (Comment) qResult;

        assertNotNull("Comment query result is invalid", c1.getText());
        assertNotNull("Comment query result is invalid", c1.getUser());
        assertNotNull("Comment query result is invalid", c1.getCreatedTime());
    }

    @Test
    public void test5CustomQueryResultsOrderForResources() throws RegistryException {
        // This is a legacy test to ensure ordering doesn't break..

        Registry rootSystemRegistry = embeddedRegistryService.getSystemRegistry();

        Resource r1 = rootSystemRegistry.newResource();
        r1.setDescription("r1 content");
        r1.setContent("r1 content");
        rootSystemRegistry.put("/test/resources/rx1", r1);
        Resource r2 = rootSystemRegistry.newResource();
        r2.setDescription("rq content");
        r2.setContent("r2 content");
        rootSystemRegistry.put("/test/resources/rx2", r2);
        Resource r0 = rootSystemRegistry.newResource();
        r0.setDescription("r0 content");
        r0.setContent("r0 content");
        rootSystemRegistry.put("/test/resources/rx0", r0);

        Resource comQuery = rootSystemRegistry.newResource();
        String sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE R " +
                "WHERE R.REG_DESCRIPTION LIKE ? ORDER BY R.REG_CREATED_TIME DESC";

        comQuery.setContent(sql);

        comQuery.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);

        rootSystemRegistry.put("/test/resources/q1", comQuery);

        Map<String, String> params = new HashMap<String, String>();
        params.put("1", "%content");
        Collection qResults = rootSystemRegistry.executeQuery("/test/resources/q1", params);

        String[] qPaths = (String[]) qResults.getContent();

        assertEquals("Query result count should be 3", qPaths.length, 3);
        assertEquals("Comment query result is invalid", qPaths[0], "/test/resources/rx0");
        assertEquals("Comment query result is invalid", qPaths[1], "/test/resources/rx2");
        assertEquals("Comment query result is invalid", qPaths[2], "/test/resources/rx1");

        comQuery = rootSystemRegistry.newResource();
        sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE R " +
                "ORDER BY R.REG_CREATED_TIME DESC";

        comQuery.setContent(sql);

        comQuery.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);

        rootSystemRegistry.put("/test/resources/q1", comQuery);

        params = new HashMap<String, String>();

        qResults = rootSystemRegistry.executeQuery("/test/resources/q1", params);

        qPaths = (String[]) qResults.getContent();

        List<String> paths = new LinkedList<String>();
        for (String temp : qPaths) {
            if (temp.startsWith("/test/resources/rx")) {
                paths.add(temp);
            }
        }
        qPaths = paths.toArray(new String[paths.size()]);

        assertEquals("Comment query result is invalid", qPaths[0], "/test/resources/rx0");
        assertEquals("Comment query result is invalid", qPaths[1], "/test/resources/rx2");
        assertEquals("Comment query result is invalid", qPaths[2], "/test/resources/rx1");
    }
}
