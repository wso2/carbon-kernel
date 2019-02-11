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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JDBCRegistryTest extends BaseTestCase {

    /**
     * Registry instance for use in tests. Note that there should be only one Registry instance in a
     * JVM.
     */
    protected static Registry registry = null;
    protected static Registry systemRegistry = null;
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
            
            registry = embeddedRegistryService.getUserRegistry("admin", "admin");
            systemRegistry = embeddedRegistryService.getSystemRegistry();
        } catch (RegistryException e) {
                fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    @Test
    public void test1IllegalCharacters() throws Exception {
        Resource r1 = registry.newResource();
        String str = "My Content";
        r1.setContentStream(new ByteArrayInputStream(str.getBytes()));

        String illegal = "~!@#%^*+={}|\\<>\"\',";
                
        char[] illegalChars = illegal.toCharArray();
        for (char character : illegalChars) {
            try {
                registry.put("/a" + character + "b", r1);
                fail("Should not be able to add resource with path containing '" + character + "'");
                break;
            } catch (RegistryException e) {}
        }
    }

    @Test
    public void test2MediaTypesInCaps() throws Exception {
        MediaTypesUtils.getResourceMediaTypeMappings(
                embeddedRegistryService.getConfigSystemRegistry());
        Resource r1 = registry.newResource();
        String str = "My Content";
        r1.setContentStream(new ByteArrayInputStream(str.getBytes()));

        registry.put("/abc.JPG", r1);
        assertEquals("image/jpeg", registry.get("/abc.JPG").getMediaType());
    }

    @Test
    public void test3CollectionDetails() throws Exception {
        Resource r1 = registry.newResource();
        String str = "My Content";
        r1.setContentStream(new ByteArrayInputStream(str.getBytes()));
        registry.put("/c1/c2/c3/c4/r1", r1);
        r1 = registry.newCollection();
        r1.setDescription("This is test description");
        r1.addProperty("p1", "value1");
        registry.put("/c1/c2/c3", r1);
        r1.discard();

        r1 = registry.get("/c1/c2/c3/c4/r1");
        InputStream inContent = r1.getContentStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int c;
        while ((c = inContent.read()) != -1) {
            outStream.write(c);
        }
        inContent.close();
        assertEquals(str, RegistryUtils.decodeBytes(outStream.toByteArray()));
        r1.discard();
    }

    @Test
    public void test4FlatResourceHandling() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setDescription("This is a test resource used for registry testing.");
        String r1Content = "<c>This is r1 content</c>";
        r1.setContent(r1Content.getBytes());
        r1.addProperty("p1", "v1");
        registry.put("/r1", r1);

        Resource r1f = registry.get("/r1");

        assertEquals("Content is not equal.",
                RegistryUtils.decodeBytes((byte[])r1.getContent()),
                RegistryUtils.decodeBytes((byte[])r1f.getContent()));

        assertEquals("Description is not equal.", r1.getDescription(), r1f.getDescription());

        assertEquals("Property p1 should contain the value v1", r1f.getProperty("p1"), "v1");

        registry.delete("/r1");

        boolean failed = false;
        try {
            registry.get("/r1");
        } catch (RegistryException e) {
            failed = true;
        }

        assertTrue("Deleted resource /r1 is returned on get.", failed);

        r1.discard();
        r1f.discard();
    }

    @Test
    public void test5HierarchicalResourceHandling() throws Exception {

        // add a resource

        Resource r1 = registry.newResource();
        String r1content = "R1 content";
        r1.setContent(r1content.getBytes());

        registry.put("/d1/r1", r1);

        Resource d1 = registry.get("/d1");

        assertTrue("/d1 should be a collection.",
                d1 instanceof org.wso2.carbon.registry.core.Collection);

        assertTrue("Content of /d1 should be a String[]", d1.getContent() instanceof String[]);

        String[] children = (String[])d1.getContent();
        boolean found = false;
        for (String aChildren : children) {
            if (aChildren.startsWith("/d1/r1")) {
                found = true;
                break;
            }
        }
        assertTrue("/d1/r1 should be a child of /d1", found);

        Resource r1f = registry.get("/d1/r1");

        assertEquals("Resource content is not stored correctly.", r1content,
                RegistryUtils.decodeBytes((byte[])r1f.getContent()));

        registry.delete("/d1");

        boolean f1 = false;
        try {
            registry.get("/d1");
        } catch (RegistryException e) {
            f1 = true;
        }
        assertTrue("Deleted collection /d1 is not marked as deleted.", f1);

        boolean f2 = false;
        try {
            registry.get("/d1/r1");
        } catch (RegistryException e) {
            f2 = true;
        }
        assertTrue("Deleted collection /d1/r1 is not marked as deleted.", f2);
    }

    @Test
    public void test6ResourceVersioning() throws Exception {
        boolean isVersionOnChange = registry.getRegistryContext().isVersionOnChange();
        Resource r1 = registry.newResource();
        byte[] r1Content = "R1 content".getBytes();
        r1.setContent(r1Content);

        registry.put("/r5", r1);

        // first update
        Resource readIt1 = registry.get("/r5");
        byte[] newR1Content = "New content".getBytes();
        readIt1.setContent(newR1Content);

        if (!isVersionOnChange) {
            registry.createVersion("/r5");
        }
        registry.put("/r5", readIt1);

        // second update
        Resource readIt2 = registry.get("/r5");
        byte[] newR1Content2 = "New content2".getBytes();
        readIt2.setContent(newR1Content2);

        if (!isVersionOnChange) {
            registry.createVersion("/r5");
        }
        registry.put("/r5", readIt2);

        // after the redesigning of the database, we need to put another do
        // set the database
        if (!isVersionOnChange) {
            registry.createVersion("/r5");
        }
        registry.put("/r5", readIt2);

        String[] versionPaths = registry.getVersions("/r5");

        Resource v1 = registry.get(versionPaths[2]);
        Resource v2 = registry.get(versionPaths[1]);
        Resource v3 = registry.get(versionPaths[0]);

        String content1 = RegistryUtils.decodeBytes((byte[])v1.getContent());
        String content2 = RegistryUtils.decodeBytes((byte[])v2.getContent());
        String content3 = RegistryUtils.decodeBytes((byte[])v3.getContent());

        assertEquals("Content is not versioned properly.", content1, "R1 content");
        assertEquals("Content is not versioned properly.", content2, "New content");
        assertEquals("Content is not versioned properly.", content3, "New content2");

        try {
            registry.restoreVersion(versionPaths[2]);
        } catch (RegistryException e) {
            fail("Valid restore version failed.");
        }

        Resource r5restored = registry.get("/r5");

        String restoredContent = RegistryUtils.decodeBytes((byte[])r5restored.getContent());
        assertEquals("Content is not restored properly.", "R1 content", restoredContent);
    }

    // todo: test this with persistence db
    @Test
    public void test7PutOnSamePath() {

        try {
            Resource userProfile = registry.newResource();
            userProfile.setContent("test".getBytes());

            registry.put("/foo/bar", userProfile);

            Resource userProfile2 = registry.newResource();
            userProfile2.setContent("test".getBytes());
            registry.put("/foo/bar", userProfile2);

            Resource myUserProfile = registry.get("/foo/bar");
            myUserProfile.getContent();
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test8CollectionVersioning() throws Exception {

        String r1Content = "r1 content1";
        Resource r1 = registry.newResource();
        r1.setContent(r1Content.getBytes());

        registry.put("/c10/r1", r1);

        registry.createVersion("/c10");

        String r2Content = "r2 content2";
        Resource r2 = registry.newResource();
        r2.setContent(r2Content.getBytes());

        registry.put("/c10/r2", r2);

        registry.createVersion("/c10");

        String[] versionPaths = registry.getVersions("/c10");

        Resource c10v1 = registry.get(versionPaths[1]);
        Resource c10v2 = registry.get(versionPaths[0]);

        String[] childrenOfv1 = (String[])c10v1.getContent();
        assertTrue("collection content is not versioned properly.",
                containsString(childrenOfv1, "/c10/r1"));

        String[] childrenOfv2 = (String[])c10v2.getContent();
        assertTrue("collection content is not versioned properly.",
                containsString(childrenOfv2, "/c10/r1"));
        assertTrue("collection content is not versioned properly.",
                containsString(childrenOfv2, "/c10/r2"));

        registry.restoreVersion(versionPaths[1]);

        Resource restoredC10 = registry.get("/c10");

        String[] restoredC10Children = (String[])restoredC10.getContent();
        assertTrue("Collection children are not restored properly.",
                containsString(restoredC10Children, "/c10/r1"));
        assertTrue("Collection children are not restored properly.",
                !containsString(restoredC10Children, "/c10/r2"));
    }

    @Test
    public void test9ValueChange() throws Exception {
        Resource r1 = registry.newResource();
        String content1 = "Content1";
        r1.setContent(content1.getBytes());
        registry.put("/abc/foo", r1);
        String content2 = "Content2";
        r1.setContent(content2.getBytes());
        registry.put("/abc/foo", r1);
        r1 = registry.get("/abc/foo");
        Object resourceContent = r1.getContent();

        boolean value = Arrays.equals(content2.getBytes(), (byte[])resourceContent);
        assertTrue(value);

    }

    @Test
    public void test10Comments() throws Exception {
        // add a resource
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d12/r1", r1);

        String comment1 = "this can be used as a test resource.";
        String comment2 = "I like this";
        registry.addComment("/d12/r1", new Comment(comment1));
        registry.addComment("/d12/r1", new Comment(comment2));

        Comment[] comments = registry.getComments("/d12/r1");

        boolean commentFound = false;
        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;
                break;
            }
        }
        assertTrue("comment '" + comment1 +
                "' is not associated with the artifact /d12/r1", commentFound);

        Resource commentsResource = registry.get("/d12/r1;comments");
        assertTrue("Comment collection resource should be a directory.",
                commentsResource instanceof Collection);
        comments = (Comment[])commentsResource.getContent();

        List<Object> commentTexts = new ArrayList<Object>();
        for (Comment comment : comments) {
            Resource commentResource = registry.get(comment.getPath());
            commentTexts.add(commentResource.getContent());
        }

        assertTrue(comment1 + " is not associated for resource /d12/r1.",
                commentTexts.contains(comment1));
        assertTrue(comment2 + " is not associated for resource /d12/r1.",
                commentTexts.contains(comment2));

        registry.delete("/d12");
    }

    public void test11Ratings() throws Exception {
        // add a resource
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d13/r1", r1);
        registry.rateResource("/d13/r1", 4);

        float rating = registry.getAverageRating("/d13/r1");

        assertEquals("Rating of the resource /d13/r1 should be 4.", (float)4.0, rating,
                (float)0.01);

        registry.delete("/d13");
    }

    @Test
    public void test12UserDefinedResourceQuery() throws Exception {
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



        String sql1 = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_DESCRIPTION LIKE ?";
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

    @Test
    public void test13UserDefinedRatingsQuery() throws Exception {
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

        registry.rateResource("/c3/r1", 3);
        registry.rateResource("/c3/r2", 4);
        registry.rateResource("/c3/r3", 5);

        String sql1 = "SELECT RT.REG_RATING_ID FROM REG_RESOURCE_RATING RT, REG_RESOURCE R " +
                "WHERE (R.REG_VERSION=RT.REG_VERSION OR " +
                "(R.REG_PATH_ID=RT.REG_PATH_ID AND R.REG_NAME=RT.REG_RESOURCE_NAME)) " +
                "AND R.REG_DESCRIPTION LIKE ?";
        Resource q1 = systemRegistry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.RATINGS_RESULT_TYPE);
        systemRegistry.put("/qs/q2", q1);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("1", "%production%");
        Collection result = registry.executeQuery("/qs/q2", parameters);

        String[] ratingPaths = result.getChildren();
        assertEquals("There should be two match ratings.", ratingPaths.length, 2);

//        Resource rating1 = registry.get(ratingPaths[0]);
//        assertEquals("First matching rating should be 3",
//                rating1.getContent().toString(), "3");
//
//        Resource rating2 = registry.get(ratingPaths[1]);
//        assertEquals("Second matching rating should be 4",
//                rating2.getContent().toString(), "4");
    }

    @Test
    public void test14UserDefinedTagsQuery() throws Exception {
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
        assertEquals("second matching tag should be 'jsp'",
                (String)tag1.getContent(), "jsp");
    }

    @Test
    public void test15UserDefinedCommentsQuery() throws Exception {
        Resource r1 = registry.newResource();
        String r1Content = "this is r1 content";
        r1.setContent(r1Content.getBytes());
        r1.setDescription("production ready.");
        String r1Path = "/c5/r1";
        registry.put(r1Path, r1);

        Resource r2 = registry.newResource();
        String r2Content = "content for r2 :)";
        r2.setContent(r2Content);
        r2.setDescription("ready for production use.");
        String r2Path = "/c5/r2";
        registry.put(r2Path, r2);

        Resource r3 = registry.newResource();
        String r3Content = "content for r3 :)";
        r3.setContent(r3Content);
        r3.setDescription("only for government use.");
        String r3Path = "/c5/r3";
        registry.put(r3Path, r3);

        registry.addComment("/c5/r1", new Comment("we have to change this file."));
        registry.addComment("/c5/r2", new Comment("replace this with a better one"));
        registry.addComment("/c5/r3", new Comment("import all dependencies for this resource"));

        String sql1 = "SELECT RC.REG_COMMENT_ID FROM REG_RESOURCE_COMMENT RC, REG_RESOURCE R " +
                        "WHERE (R.REG_VERSION=RC.REG_VERSION OR " +
                        "(R.REG_PATH_ID=RC.REG_PATH_ID AND R.REG_NAME=RC.REG_RESOURCE_NAME)) " +
                        "AND R.REG_DESCRIPTION LIKE ?";
               
        Resource q1 = systemRegistry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.COMMENTS_RESULT_TYPE);
        systemRegistry.put("/qs/q4", q1);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("1", "%production%");
        Collection result = registry.executeQuery("/qs/q4", parameters);

        String[] commentPaths = result.getChildren();
        assertEquals("There should be two match comments.", commentPaths.length, 2);

        Resource c1 = registry.get(commentPaths[0]);
        Resource c2 = registry.get(commentPaths[1]);

        if (!c1.getContent().equals("we have to change this file.")) {
            // change c1 and c2.
            Resource temp = c1;
            c1 = c2;
            c2 = temp;
        }
        assertEquals("First matching comment is incorrect",
                (String)c1.getContent(), "we have to change this file.");

        assertEquals("Second matching comment is incorrect",
                (String)c2.getContent(), "replace this with a better one");
    }

    @Test
    public void test16TagsAsResources() {
        //Resource r1 = new Resource();
        //String r1Content = "this is r1 content";
        //r1.setContent(r1Content.getBytes());
        //r1.setDescription("production ready.");
        //String r1Path = "/c1/r1";
        //
        //try {
        //    registry.put(r1Path, r1);
        //} catch (RegistryException e) {
        //    fail("Valid put scenario failed.");
        //}
        //
        //try {
        //    registry.applyTag("/c1/r1", "java");
        //    registry.applyTag("/c1/r1", "bookmarking");
        //    registry.applyTag("/c1/r1", "registry");
        //} catch (RegistryException e) {
        //    fail("Valid tagging scenario failed.");
        //}
        //
        //Resource tags = null;
        //try {
        //    tags = registry.get("/c1/r1?tags");
        //} catch (RegistryException e) {
        //    fail("Failed to get tags using URL.");
        //}
        //
        //String[] tagPaths = (String[]) tags.getContent();
        //List tagStrings = new ArrayList();
        //
        //for (int i = 0; i < tagPaths.length; i++) {
        //    try {
        //        Resource c1 = registry.get(tagPaths[i]);
        //        String tagString = (String) c1.getContent();
        //        tagStrings.add(tagString);
        //    } catch (RegistryException e) {
        //        fail("Failed to get comment using a path.");
        //    }
        //}
        //
        //assertTrue("Tags are not retrieved properly as resources.",
        //        tagStrings.contains("java"));
        //
        //assertTrue("Tags are not retrieved properly as resources.",
        //        tagStrings.contains("bookmarking"));
        //
        //assertTrue("Tags are not retrieved properly as resources.",
        //        tagStrings.contains("registry"));
        //
        //int a = 1;
    }

    @Test
    public void test17CommentsAsResources() throws RegistryException {
        Resource r1 = registry.newResource();
        String r1Content = "this is r1 content";
        r1.setContent(r1Content.getBytes());
        r1.setDescription("production ready.");
        String r1Path = "/c1/r1";

        try {
            registry.put(r1Path, r1);
        } catch (RegistryException e) {
            fail("Valid put scenario failed.");
        }

        try {
            registry.addComment("/c1/r1", new Comment("this is used to test comments."));
            registry.addComment("/c1/r1", new Comment("dummy resource."));
            registry.addComment("/c1/r1", new Comment("simple test resource."));
        } catch (RegistryException e) {
            fail("Valid tagging scenario failed.");
        }

        Resource comments = null;
        try {
            comments = registry.get("/c1/r1;comments");
        } catch (RegistryException e) {
            fail("Failed to get comments using URL.");
        }

        Comment[] commentPaths = (Comment[])comments.getContent();
        List<Object> commentStrings = new ArrayList<Object>();

        for (Comment comment : commentPaths) {
            try {
                Resource c1 = registry.get(comment.getPath());
                String commentString = (String)c1.getContent();
                commentStrings.add(commentString);
            } catch (RegistryException e) {
                fail("Failed to get comment using a path.");
            }
        }

        assertTrue("Comments are not retrieved properly as resources.",
                commentStrings.contains("this is used to test comments."));

        assertTrue("Comments are not retrieved properly as resources.",
                commentStrings.contains("dummy resource."));

        assertTrue("Comments are not retrieved properly as resources.",
                commentStrings.contains("simple test resource."));
    }

    @Test
    public void test18RatingsAsResources() throws RegistryException {
        Resource r5 = registry.newResource();
        String r5Content = "this is r5 content";
        r5.setContent(r5Content.getBytes());
        r5.setDescription("production ready.");
        String r5Path = "/c1/r5";

        try {
            registry.put(r5Path, r5);
        } catch (RegistryException e) {
            fail("Valid put scenario failed.");
        }

        try {
            registry.rateResource("/c1/r5", 3);
        } catch (RegistryException e) {
            fail("Valid rating scenario failed.");
        }

        Resource ratings = null;
        try {
            ratings = registry.get("/c1/r5;ratings");
        } catch (RegistryException e) {
            fail("Failed to get ratings using URL.");
        }

        String[] ratingPaths = (String[])ratings.getContent();

        int rating = 0;
        try {
            Resource c1 = registry.get(ratingPaths[0]);

            Object o = c1.getContent();
            if (o instanceof Integer) {
                rating = (Integer)o;
            } else {
                rating = Integer.parseInt(o.toString());
            }

        } catch (RegistryException e) {
            fail("Failed to get rating using a path.");
        }

        assertEquals("Ratings are not retrieved properly as resources.", rating, 3);
    }

    @Test
    public void test19Logs() throws RegistryException {
        String r1Content = "this is the r200 content.";
        Resource r1 = registry.newResource();
        r1.setContent(r1Content.getBytes());

        try {
            registry.put("/r200", r1);
        } catch (RegistryException e) {
            fail("Couldn't put a content resource in to path /r200");
        }

        registry.rateResource("/r200", 5);

    }

    @Test
    public void test20ResourceDelete() throws RegistryException {
        String content1 = "Content1";
        Resource r1 = registry.newResource();
        r1.setContent(content1);
        try {
            //Adding a resource
            registry.put("/wso2/wsas/v1/r1", r1);

        } catch (RegistryException e) {
            fail("Couldn't put a content resource in to path /wso2/wsas/v1/r1");
        }

        // Adding a dummy resource
        Resource v2 = registry.newResource();
        registry.put("/wso2/wsas/v2", v2);

        //getting the resource
        Resource r2 = registry.get("/wso2/wsas/v1");

        //check whether the content is correct
        assertEquals("/wso2/wsas/v1/r1", ((String[])r2.getContent())[0]);
        Resource wsas = registry.get("/wso2/wsas");
        String[] wsasContent = (String[])wsas.getContent();
        assertNotNull(wsasContent);
        assertEquals(2, wsasContent.length);

        registry.delete("/wso2/wsas/v1");

        String content2 = "Content2";
        Resource resourceContent2 = registry.newResource();
        resourceContent2.setContent(content2);
        registry.put("/wso2/wsas/v1/r2", resourceContent2);

        wsas = registry.get("/wso2/wsas");
        wsasContent = (String[])wsas.getContent();
        assertNotNull(wsasContent);
        assertEquals(2, wsasContent.length);

        r2 = registry.get("/wso2/wsas/v1");
        //check whether the content is correct
        assertEquals("/wso2/wsas/v1/r2", ((String[])r2.getContent())[0]);

        // todo: uncomment after version handling is completed
        //registry.restoreVersion("/wso2/wsas;version=2");
        //r2 = registry.get("/wso2/wsas/v1");

        //check whether the content is correct
        //assertEquals("/wso2/wsas/v1/r1", ((String[])r2.getContent())[0]);

    }

    @Test
    public void test21CombinedScenario() throws RegistryException {
        // put a content resource in to the root
        String r1Content = "this is the r1 content.";
        Resource r1 = registry.newResource();
        r1.setContent(r1Content.getBytes());
        registry.put("/r11", r1);

        Resource r1New = registry.newResource();
        r1New.setContent("New r1");
        registry.put("/r1", r1New);

        // put a collection in to the root

        Collection c1 = registry.newCollection();
        registry.put("/c1", c1);

        // put a content artifact in to /c1/r2

        String r2Content = "this is r2 content";
        Resource r2 = registry.newResource();
        r2.setContent(r2Content.getBytes());

        registry.put("/c1/r2", r2);

        // put a content artifact in to non-existing collection

        String r3Content = "this is r3 content";
        Resource r3 = registry.newResource();

        r3.addProperty("Reviewer", "Foo");
        r3.addProperty("TestDone", "Axis2");
        r3.setContent(r3Content.getBytes());

        registry.put("/c2/r3", r3);

        // put c2/r4
        String r4Content = "this is r4 content";
        Resource r4 = registry.newResource();
        r4.setContent(r4Content.getBytes());

        registry.put("/c2/r4", r4);

        // do some taggings

        registry.applyTag("/c2/r4", "ui");
        registry.applyTag("/c2/r4", "swing");
        registry.applyTag("/r1", "test");
        registry.applyTag("/r1", "Java");
        registry.applyTag("/c1", "test");
        registry.applyTag("/c1/r2", "struts");
        registry.applyTag("/c1/r2", "ui");
        registry.applyTag("/c1/r2", "web");
        registry.applyTag("/c1/r2", "web2");
        registry.applyTag("/c1/r2", "web3");

        // add some comments

        registry.addComment("/r1", new Comment("this is a test artifact."));
        registry.addComment("/c1", new Comment("I don't agree with having this collection."));
        registry.addComment("/c1/r2", new Comment("Is this a collection ;)"));
        registry.addComment("/c2",
                new Comment("I can understand the purpose of this. Thanks."));

        // rate some artifacts

        registry.rateResource("/r11", 3);
        registry.rateResource("/r11", 4);
        registry.rateResource("/r11", 5);
        registry.rateResource("/c1", 2);
        registry.rateResource("/c1/r2", 5);
        registry.rateResource("/c2/r4", 1);
        registry.rateResource("/c2/r4", 2);

        registry.delete("/r11");
        registry.delete("/c1");
        registry.delete("/c2");
    }

    @Test
    public void test22GetMetaData() throws RegistryException {

        String r1Content = "this is the rgm content.";
        Resource r = registry.newResource();
        r.setContent(r1Content.getBytes());
        registry.put("/rgm", r);

        Resource rm = registry.getMetaData("/rgm");
        assertNull(rm.getContent());
        
        Resource rr = registry.get("/rgm");
        assertNotNull(rr.getContent());
        
    }

    @Test
    public void test23ResourceCollectionMix() throws RegistryException {
        Resource defaultGadgetCollection = registry.newResource();
        registry.put("/system/gadgets", defaultGadgetCollection);

        defaultGadgetCollection = registry.newCollection();
        registry.put("/system/gadgets", defaultGadgetCollection);

        Resource r = registry.get("/system/gadgets");

        assertTrue("R should be a collection", r instanceof Collection);
        assertTrue("R should exist", registry.resourceExists("/system/gadgets"));


        defaultGadgetCollection = registry.newResource();
        registry.put("/system/gadgets", defaultGadgetCollection);

        r = registry.get("/system/gadgets");

        assertFalse("R should not be a collection", r instanceof Collection);
        assertTrue("R should exist", registry.resourceExists("/system/gadgets"));
        defaultGadgetCollection = registry.newCollection();
        registry.put("/system/gadgets", defaultGadgetCollection);

        r = registry.get("/system/gadgets");

        assertTrue("R should be a collection", r instanceof Collection);
        assertTrue("R should exist", registry.resourceExists("/system/gadgets"));
    }

    @Test
    public void test24LastUpdateWithGet() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setContent("test");
        registry.put("/pqr/xyz", r1);

        Resource r2 = registry.get("/pqr/xyz");
        Date date2 = r2.getLastModified();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            
        }

        Resource r3 = registry.get("/pqr/xyz");
        Date date3 = r3.getLastModified();

        assertEquals("update time should be equal", date2, date3);
    }

    @Test
    public void test25LastUpdateWithPut() throws RegistryException {
        Resource r1 = registry.newResource();
        registry.put("/pqr/xyz", r1);

        Resource r2 = registry.get("/pqr/xyz");
        Date date2 = r2.getLastModified();

        registry.put("/pqr/xyz", r1);

        Resource r3 = registry.get("/pqr/xyz");
        Date date3 = r3.getLastModified();

        assertNotSame("update time should be different", date2, date3);
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
