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

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;

import java.util.*;


public class QueryTest extends BaseTestCase {

    protected static EmbeddedRegistryService embeddedRegistryService = null;
    protected static Registry registry = null;
    protected static Registry systemRegistry = null;

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
            registry = embeddedRegistryService.getConfigUserRegistry(
                    realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
            systemRegistry = embeddedRegistryService.getConfigSystemRegistry();
        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }


    public void testBackwardCompatibility() throws RegistryException {
        Registry rootRegistry = embeddedRegistryService.getSystemRegistry();
        Resource r1 = rootRegistry.newResource();
        r1.setContent("r1 content");
        rootRegistry.put("/test/comments/r1", r1);

        rootRegistry.addComment("/test/comments/r1",
                new Comment("backward-compatibility1 on this resource :)"));
        rootRegistry.addComment("/test/comments/r1",
                new Comment("backward-compatibility2 on this resource :)"));

        String sql = "SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC " +
                "WHERE C.REG_COMMENT_TEXT LIKE ? AND C.REG_ID=RC.REG_COMMENT_ID";

        Resource queryR = rootRegistry.newResource();
        queryR.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        queryR.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.COMMENTS_RESULT_TYPE);
        rootRegistry.put("/beep/x", queryR);
        Map<String, String> params = new HashMap <String, String> ();
        params.put("query", sql);
        params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.COMMENTS_RESULT_TYPE);
        params.put("1", "backward-compatibility1%");
        Collection qResults = rootRegistry.executeQuery("/beep/x", params);

        String[] qPaths = (String[]) qResults.getContent();

        assertEquals("Query result count should be 1", qPaths.length, 1);
    }


    public void testDefaultQuery() throws Exception {
        Resource r1 = registry.newResource();
        String r1Content = "this is r1 content";
        r1.setContent(r1Content.getBytes());
        r1.setDescription("production ready.");
        String r1Path = "/c3/r1";
        registry.put(r1Path, r1);

        Resource r2 = registry.newResource();
        String r2Content = "content for r2 :)";
        r2.setContent(r2Content);
        r2.setDescription("ready for production use.");
        String r2Path = "/c3/r2";
        registry.put(r2Path, r2);

        Resource r3 = registry.newResource();
        String r3Content = "content for r3 :)";
        r3.setContent(r3Content);
        r3.setDescription("only for government use.");
        String r3Path = "/c3/r3";
        registry.put(r3Path, r3);

        registry.applyTag("/c3/r1", "java");
        registry.applyTag("/c3/r2", "jsp");
        registry.applyTag("/c3/r3", "ajax");


        String sql1 = "SELECT RT.REG_TAG_ID FROM REG_RESOURCE_TAG RT, REG_RESOURCE R " +
                "WHERE (R.REG_VERSION=RT.REG_VERSION OR " +
                "(R.REG_PATH_ID=RT.REG_PATH_ID AND R.REG_NAME=RT.REG_RESOURCE_NAME)) " +
                "AND R.REG_DESCRIPTION LIKE ? ORDER BY RT.REG_TAG_ID";

        Resource q1 = systemRegistry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.TAGS_RESULT_TYPE);
        systemRegistry.put("/qs/q3", q1);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("1", "%production%");
        Collection result = registry.executeQuery("/qs/q3", parameters);

        String[] tagPaths = result.getChildren();
        assertEquals("There should be two matching tags.", tagPaths.length, 2);

        Resource tag2 = registry.get(tagPaths[0]);
        assertEquals("First matching tag should be 'java'",
                (String)tag2.getContent(), "java");

        Resource tag1 = registry.get(tagPaths[1]);
        assertEquals("Second matching tag should be 'jsp'",
                (String)tag1.getContent(), "jsp");
    }


    public void testWithSpecialCharactersQuery() throws Exception {
        Resource r1 = registry.newResource();
        String r1Content = "this is r1 content";
        r1.setContent(r1Content.getBytes());
        r1.setDescription("production ready.");
        String r1Path = "/c3/r1";
        registry.put(r1Path, r1);

        Resource r2 = registry.newResource();
        String r2Content = "content for r2 :)";
        r2.setContent(r2Content);
        r2.setDescription("ready for production use.");
        String r2Path = "/c3/r2";
        registry.put(r2Path, r2);

        Resource r3 = registry.newResource();
        String r3Content = "content for r3 :)";
        r3.setContent(r3Content);
        r3.setDescription("only for government use.");
        String r3Path = "/c3/r3";
        registry.put(r3Path, r3);

        registry.applyTag("/c3/r1", "java");
        registry.applyTag("/c3/r2", "jsp");
        registry.applyTag("/c3/r3", "ajax");


        String sql1 = "SELECT\nRT.REG_TAG_ID\nFROM REG_RESOURCE_TAG\nRT,\nREG_RESOURCE\nR\n" +
                "WHERE\n(R.REG_VERSION=RT.REG_VERSION\nOR\n" +
                "(R.REG_PATH_ID=RT.REG_PATH_ID\nAND\nR.REG_NAME=RT.REG_RESOURCE_NAME))\n" +
                "AND R.REG_DESCRIPTION\nLIKE\n?\nORDER BY\nRT.REG_TAG_ID";

        Resource q1 = systemRegistry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.TAGS_RESULT_TYPE);
        systemRegistry.put("/qs/q3", q1);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("1", "%production%");
        Collection result = registry.executeQuery("/qs/q3", parameters);

        String[] tagPaths = result.getChildren();
        assertEquals("There should be two matching tags.", tagPaths.length, 2);

        Resource tag2 = registry.get(tagPaths[0]);
        assertEquals("First matching tag should be 'java'",
                (String)tag2.getContent(), "java");

        Resource tag1 = registry.get(tagPaths[1]);
        assertEquals("Second matching tag should be 'jsp'",
                (String)tag1.getContent(), "jsp");
    }


    public void testWithoutTableParamsQuery() throws Exception {
        Resource r1 = registry.newResource();
        String r1Content = "this is r1 content";
        r1.setContent(r1Content.getBytes());
        r1.setDescription("production ready.");
        String r1Path = "/c1/r1";
        registry.put(r1Path, r1);

        Resource r2 = registry.newResource();
        String r2Content = "content for r2 :)";
        r2.setContent(r2Content);
        r2.setDescription("ready for production use.");
        String r2Path = "/c2/r2";
        registry.put(r2Path, r2);

        Resource r3 = registry.newResource();
        String r3Content = "content for r3 :)";
        r3.setContent(r3Content);
        r3.setDescription("only for government use.");
        String r3Path = "/c2/r3";
        registry.put(r3Path, r3);



        String sql1 = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE, " +
                "REG_TAG WHERE REG_DESCRIPTION LIKE ?";
        Resource q1 = systemRegistry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.RESOURCES_RESULT_TYPE);
        systemRegistry.put("/qs/q1", q1);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("1", "%production%");
        Resource result = registry.executeQuery("/qs/q1", parameters);

        assertTrue("Search with result type Resource should return a directory.",
                result instanceof org.wso2.carbon.registry.core.Collection);

        List<String> matchingPaths = new ArrayList<String>();
        String[] paths = (String[])result.getContent();
        matchingPaths.addAll(Arrays.asList(paths));

        assertTrue("Path /c1/r1 should be in the results.", matchingPaths.contains("/c1/r1"));
        assertTrue("Path /c2/r2 should be in the results.", matchingPaths.contains("/c2/r2"));
    }


    public void testWithoutWhereQuery() throws Exception {
        Resource r1 = registry.newResource();
        String r1Content = "this is r1 content";
        r1.setContent(r1Content.getBytes());
        r1.setDescription("production ready.");
        String r1Path = "/c1/r1";
        registry.put(r1Path, r1);

        Resource r2 = registry.newResource();
        String r2Content = "content for r2 :)";
        r2.setContent(r2Content);
        r2.setDescription("ready for production use.");
        String r2Path = "/c2/r2";
        registry.put(r2Path, r2);

        Resource r3 = registry.newResource();
        String r3Content = "content for r3 :)";
        r3.setContent(r3Content);
        r3.setDescription("only for government use.");
        String r3Path = "/c2/r3";
        registry.put(r3Path, r3);



        String sql1 = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE, REG_TAG";
        Resource q1 = systemRegistry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.RESOURCES_RESULT_TYPE);
        systemRegistry.put("/qs/q1", q1);

        Map parameters = new HashMap();
        Resource result = registry.executeQuery("/qs/q1", parameters);

        assertTrue("Search with result type Resource should return a directory.",
                result instanceof org.wso2.carbon.registry.core.Collection);

        String[] paths = (String[])result.getContent();
        assertTrue("Should return all the resources", paths.length >=3);
    }
}