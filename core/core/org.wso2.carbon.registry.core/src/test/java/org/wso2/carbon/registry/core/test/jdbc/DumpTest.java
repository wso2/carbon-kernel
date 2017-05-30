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

import javax.xml.stream.XMLStreamException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;


public class DumpTest extends BaseTestCase {

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

    public void testResourceDump() throws RegistryException, XMLStreamException {
        Resource r = registry.newResource();
        r.setProperty("key1", "value1");
        r.setProperty("key2", "value2");
        r.setContent("content 1");
        registry.put("/testDump", r);

        registry.addComment("/testDump", new Comment("comment1"));
        registry.addComment("/testDump", new Comment("comment2"));

        registry.applyTag("/testDump", "tag1");
        registry.applyTag("/testDump", "tag2");

        registry.rateResource("/testDump", 3);

        // doing the dump
        StringWriter writer = new StringWriter();
        registry.dump("/testDump", writer);
        Reader input = new StringReader(writer.toString());
        registry.restore("/testDumpDup", input);
        r = registry.get("/testDumpDup");

        assertEquals(RegistryUtils.decodeBytes((byte[])r.getContent()), "content 1");

        // checking the properties.
        assertEquals(r.getProperties().size(), 2);
        assertEquals(r.getProperty("key1"), "value1");
        assertEquals(r.getProperty("key2"), "value2");

        // getting the comments
        Comment[] comments = registry.getComments("/testDumpDup");
        assertEquals(comments.length, 2);
        assertEquals(comments[0].getText(), "comment1");
        assertEquals(comments[1].getText(), "comment2");

        // getting the tags
        Tag[] tags = registry.getTags("/testDumpDup");
        assertEquals(tags[0].getTagName(), "tag1");
        assertEquals(tags[1].getTagName(), "tag2");

        // getting the ratings
        int rate = registry.getRating("/testDumpDup", "admin");
        assertEquals(rate, 3);
    }


    public void testCollectionDump() throws RegistryException, XMLStreamException {
        Resource r = registry.newCollection();
        r.setProperty("key1", "value1");
        r.setProperty("key2", "value2");
        registry.put("/testDumpC", r);

        // adding children
        Resource r1 = registry.newCollection();
        r1.setProperty("key1", "value1C");
        r1.setProperty("key2", "value2C");
        registry.put("/testDumpC/child1C", r1);

        Resource r2 = registry.newResource();
        r2.setContent("content child2R");
        registry.put("/testDumpC/child2R", r2);

        registry.addComment("/testDumpC", new Comment("comment1"));
        registry.addComment("/testDumpC", new Comment("comment2"));

        registry.applyTag("/testDumpC", "tag1");
        registry.applyTag("/testDumpC", "tag2");

        registry.rateResource("/testDumpC", 3);

        // doing the dump
        StringWriter writer = new StringWriter();
        registry.dump("/testDumpC", writer);
        Reader input = new StringReader(writer.toString());

        // now restoring and retrieving the dumped element
        registry.restore("/testDumpDupC", input);
        r = registry.get("/testDumpDupC");

        // checking the properties.
        assertEquals(r.getProperties().size(), 2);
        assertEquals(r.getProperty("key1"), "value1");
        assertEquals(r.getProperty("key2"), "value2");

        // getting the comments
        Comment[] comments = registry.getComments("/testDumpDupC");
        assertEquals(comments.length, 2);
        assertEquals(comments[0].getText(), "comment1");
        assertEquals(comments[1].getText(), "comment2");

        // getting the tags
        Tag[] tags = registry.getTags("/testDumpDupC");
        assertEquals(tags[0].getTagName(), "tag1");
        assertEquals(tags[1].getTagName(), "tag2");

        // getting the ratings
        int rate = registry.getRating("/testDumpDupC", "admin");
        assertEquals(rate, 3);

        // getting the children
        r1 = registry.get("/testDumpDupC/child1C");
        assertEquals(r1.getProperties().size(), 2);
        assertEquals(r1.getProperty("key1"), "value1C");
        assertEquals(r1.getProperty("key2"), "value2C");

        r2 = registry.get("/testDumpDupC/child2R");
        assertEquals(RegistryUtils.decodeBytes((byte[])r2.getContent()), "content child2R");
    }


    public void testRootDump() throws RegistryException, XMLStreamException {
        Resource r = registry.newCollection();
        r.setProperty("key1", "value1");
        r.setProperty("key2", "value2");
        registry.put("/", r);

        // adding children
        Resource r1 = registry.newCollection();
        r1.setProperty("key1", "value1C");
        r1.setProperty("key2", "value2C");
        registry.put("/child1CX", r1);

        Resource r2 = registry.newResource();
        r2.setContent("content child2R");
        registry.put("/child2RX", r2);

        registry.addComment("/", new Comment("comment1"));
        registry.addComment("/", new Comment("comment2"));

        registry.applyTag("/", "tag1");
        registry.applyTag("/", "tag2");

        registry.rateResource("/", 3);

        // doing the dump
        StringWriter writer = new StringWriter();
        registry.dump("/", writer);
        Reader input = new StringReader(writer.toString());
        registry.restore("/testDumpDupR", input);
        r = registry.get("/testDumpDupR");

        // checking the properties.
        assertEquals(r.getProperties().size(), 2);
        assertEquals(r.getProperty("key1"), "value1");
        assertEquals(r.getProperty("key2"), "value2");

        // getting the comments
        Comment[] comments = registry.getComments("/testDumpDupR");
        assertEquals(comments.length, 2);
        assertEquals(comments[0].getText(), "comment1");
        assertEquals(comments[1].getText(), "comment2");

        // getting the tags
        Tag[] tags = registry.getTags("/testDumpDupR");
        assertEquals(tags[0].getTagName(), "tag1");
        assertEquals(tags[1].getTagName(), "tag2");

        // getting the ratings
        int rate = registry.getRating("/testDumpDupR", "admin");
        assertEquals(rate, 3);
        
        // getting the children
        r1 = registry.get("/testDumpDupR/child1CX");
        assertEquals(r1.getProperties().size(), 2);
        assertEquals(r1.getProperty("key1"), "value1C");
        assertEquals(r1.getProperty("key2"), "value2C");

        r2 = registry.get("/testDumpDupR/child2RX");
        assertEquals(RegistryUtils.decodeBytes((byte[])r2.getContent()), "content child2R");
    }

    public void testRootRestore() throws RegistryException, XMLStreamException {
        Resource r = registry.newCollection();
        r.setProperty("key1", "value3");
        r.setProperty("key2", "value4");
        registry.put("/testSomewhereElse1", r);

        // adding children
        Resource r1 = registry.newCollection();
        r1.setProperty("key1", "value1C");
        r1.setProperty("key2", "value2C");
        registry.put("/testSomewhereElse1/child1CY", r1);

        Resource r2 = registry.newResource();
        r2.setContent("content child2R");
        registry.put("/testSomewhereElse1/child2RY", r2);

        registry.addComment("/testSomewhereElse1", new Comment("comment3"));
        registry.addComment("/testSomewhereElse1", new Comment("comment4"));

        registry.applyTag("/testSomewhereElse1", "tag3");
        registry.applyTag("/testSomewhereElse1", "tag4");

        registry.rateResource("/testSomewhereElse1", 2);

        Collection collection = registry.newCollection();
        registry.put("/anotherLocation", collection);

        // doing the dump
        StringWriter writer = new StringWriter();
        registry.dump("/testSomewhereElse1", writer);
        Reader input = new StringReader(writer.toString());
        registry.restore("/anotherLocation", input);
        r = registry.get("/anotherLocation");

        // checking the properties.
        assertEquals(r.getProperties().size(), 2);
        assertEquals(r.getProperty("key1"), "value3");
        assertEquals(r.getProperty("key2"), "value4");

        // getting the comments
        Comment[] comments = registry.getComments("/anotherLocation");
        assertEquals(comments.length, 2);
        assertEquals(comments[0].getText(), "comment3");
        assertEquals(comments[1].getText(), "comment4");

        // getting the tags
        Tag[] tags = registry.getTags("/anotherLocation");
        assertEquals(tags[0].getTagName(), "tag3");
        assertEquals(tags[1].getTagName(), "tag4");

        // getting the ratings
        int rate = registry.getRating("/anotherLocation", "admin");
        assertEquals(rate, 2);

        // getting the children
        r1 = registry.get("/anotherLocation/child1CY");
        assertEquals(r1.getProperties().size(), 2);
        assertEquals(r1.getProperty("key1"), "value1C");
        assertEquals(r1.getProperty("key2"), "value2C");

        r2 = registry.get("/anotherLocation/child2RY");
        assertEquals(RegistryUtils.decodeBytes((byte[])r2.getContent()), "content child2R");
    }

    public void testSimpleNewRestore() throws RegistryException, XMLStreamException {
        Resource r1 = registry.newCollection();
        r1.setProperty("key1", "value1C");
        r1.setProperty("key2", "value2C");
        registry.put("/testSomewhereElse2/child1CY/foo", r1);

        Collection collection = registry.newCollection();
        registry.put("/anotherLocation", collection);

        // doing the dump
        StringWriter writer = new StringWriter();
        registry.dump("/testSomewhereElse2", writer);
        Reader input = new StringReader(writer.toString());
        registry.restore("/anotherLocation", input);
        
        Resource r2 = registry.get("/anotherLocation/child1CY/foo");
        assertTrue((r2 instanceof CollectionImpl));
    }

    public void testNewRestore() throws RegistryException, XMLStreamException {
        Resource r = registry.newCollection();
        r.setProperty("key1", "value3");
        r.setProperty("key2", "value4");
        registry.put("/testSomewhereElse3", r);

        // adding children
        Resource r1 = registry.newCollection();
        r1.setProperty("key1", "value1C");
        r1.setProperty("key2", "value2C");
        registry.put("/testSomewhereElse3/child1CY", r1);
        
        r1 = registry.newCollection();
        r1.setProperty("key1", "value1C");
        r1.setProperty("key2", "value2C");
        registry.put("/testSomewhereElse3/child1CY/foo", r1);
        
        r1 = registry.newCollection();
        r1.setProperty("key1", "value1C");
        r1.setProperty("key2", "value2C");
        registry.put("/testSomewhereElse3/child1CY/bar", r1);

        Resource r2 = registry.newCollection();
        //r2.setContent("content child2R");
        registry.put("/testSomewhereElse3/newWWC1", r2);

        r2 = registry.newCollection();
        //r2.setContent("content child2R");
        registry.put("/testSomewhereElse3/newWWC2", r2);

        r2 = registry.newResource();
        r2.setContent("content child2R");
        registry.put("/testSomewhereElse3/child2RY", r2);

        registry.addComment("/testSomewhereElse3", new Comment("comment3"));
        registry.addComment("/testSomewhereElse3", new Comment("comment4"));

        registry.applyTag("/testSomewhereElse3", "tag3");
        registry.applyTag("/testSomewhereElse3", "tag4");

        registry.rateResource("/testSomewhereElse3", 2);

        Collection collection = registry.newCollection();
        registry.put("/anotherLocation", collection);

        // doing the dump
        StringWriter writer = new StringWriter();
        registry.dump("/testSomewhereElse3", writer);
        Reader input = new StringReader(writer.toString());
        registry.restore("/anotherLocation", input);
        r = registry.get("/anotherLocation");

        // checking the properties.
        assertEquals(r.getProperties().size(), 2);
        assertEquals(r.getProperty("key1"), "value3");
        assertEquals(r.getProperty("key2"), "value4");

        // getting the comments
        Comment[] comments = registry.getComments("/anotherLocation");
        assertEquals(comments.length, 2);
        assertEquals(comments[0].getText(), "comment3");
        assertEquals(comments[1].getText(), "comment4");

        // getting the tags
        Tag[] tags = registry.getTags("/anotherLocation");
        assertEquals(tags[0].getTagName(), "tag3");
        assertEquals(tags[1].getTagName(), "tag4");

        // getting the ratings
        int rate = registry.getRating("/anotherLocation", "admin");
        assertEquals(rate, 2);

        // getting the children
        r1 = registry.get("/anotherLocation/child1CY");
        assertEquals(r1.getProperties().size(), 2);
        assertEquals(r1.getProperty("key1"), "value1C");
        assertEquals(r1.getProperty("key2"), "value2C");

        r2 = registry.get("/anotherLocation/child1CY/foo");
        assertTrue((r2 instanceof CollectionImpl));
        r2 = registry.get("/anotherLocation/child1CY/bar");
        assertTrue((r2 instanceof CollectionImpl));

        r2 = registry.get("/anotherLocation/child2RY");
        assertEquals(RegistryUtils.decodeBytes((byte[])r2.getContent()), "content child2R");
    }

    public void testAbsoluteAssociationPath() throws Exception {
        assertEquals("/abc", RegistryUtils.getAbsoluteAssociationPath("../abc", "/lm/pqr"));
        assertEquals("/abc/def",
                RegistryUtils.getAbsoluteAssociationPath("../../../abc/def", "/lm/pqr/b/boo"));
        assertEquals("/abc/hag/def",
                RegistryUtils.getAbsoluteAssociationPath("../hag/def", "/abc/boo/lm"));
        assertEquals("/abc", RegistryUtils.getAbsoluteAssociationPath("abc", "/pqr"));
        assertEquals("/bloom/squid/abc",
                RegistryUtils.getAbsoluteAssociationPath("squid/abc", "/bloom/squid2"));
        assertEquals("/abc", RegistryUtils.getAbsoluteAssociationPath("abc", "/abc"));

        // go beyond cases
        assertEquals("//abc", RegistryUtils.getAbsoluteAssociationPath("../../abc", "/lm/pqr"));
        assertEquals("///abc", RegistryUtils.getAbsoluteAssociationPath("../../../abc", "/lm/pqr"));
        assertEquals("////abc",
                RegistryUtils.getAbsoluteAssociationPath("../../../../abc", "/lm/pqr"));
    }


    public void testRelativeAssociationPath() throws Exception {
        assertEquals("../abc", RegistryUtils.getRelativeAssociationPath("/abc", "/lm/pqr"));
        assertEquals("../../../abc/def",
                RegistryUtils.getRelativeAssociationPath("/abc/def", "/lm/pqr/b/boo"));
        assertEquals("../hag/def",
                RegistryUtils.getRelativeAssociationPath("/abc/hag/def", "/abc/boo/lm"));
        assertEquals("abc", RegistryUtils.getRelativeAssociationPath("/abc", "/pqr"));
        assertEquals("squid/abc",
                RegistryUtils.getRelativeAssociationPath("/bloom/squid/abc", "/bloom/squid2"));
        assertEquals("abc", RegistryUtils.getRelativeAssociationPath("/abc", "/abc"));

        assertEquals("../../abc", RegistryUtils.getRelativeAssociationPath("//abc", "/lm/pqr"));
        assertEquals("../../../abc", RegistryUtils.getRelativeAssociationPath("///abc", "/lm/pqr"));
        assertEquals("../../../../abc",
                RegistryUtils.getRelativeAssociationPath("////abc", "/lm/pqr"));
    }

    public void testDumpWithSymLink() throws Exception {

        Resource r = registry.newResource();
        r.setProperty("key1", "value3");
        r.setProperty("key2", "value4");
        registry.put("/my/original/link/resource", r);

        registry.createLink("/my/sym/link/resource", "/my/original/link/resource");

        // just check the sym
        Resource r2 = registry.get("/my/sym/link/resource");
        assertEquals("value3", r2.getProperty("key1"));
        assertEquals("value4", r2.getProperty("key2"));

        // now get a dump of /my
        StringWriter writer = new StringWriter();
        registry.dump("/my", writer);

        StringReader reader = new StringReader(writer.toString());
        // putting reader
        registry.restore("/restored", reader);

        Resource r3 =  registry.get("/restored/sym/link/resource");
        assertEquals("value3", r3.getProperty("key1"));
        assertEquals("value4", r3.getProperty("key2"));

        // do some changes to the original and check the sym link changing
        Resource r4 = registry.get("/restored/original/link/resource");
        r4.setProperty("key3", "value5");
        registry.put("/restored/original/link/resource", r4);


        Resource r5 =  registry.get("/restored/sym/link/resource");
        assertEquals("value5", r5.getProperty("key3"));

    }


    public void testNewerVersionException() throws Exception {
        Resource r = registry.newResource();
        r.setContent("abc123");
        registry.put("/aaa3/bb/def", r);

        // now get a dump
        StringWriter writer = new StringWriter();
        registry.dump("/aaa3", writer);

        // now update the resource
        r.setContent("abc1234");
        registry.put("/aaa3/bb/def", r);

        String dumpStr = writer.toString();
        dumpStr = dumpStr.replaceAll("<resource", "<resource ignoreConflicts=\"false\"");
        StringReader reader = new StringReader(dumpStr);
        // putting reader
        try {
            registry.restore("/aaa3", reader);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }

        writer = new StringWriter();
        registry.dump("/aaa3", writer);
        reader = new StringReader(writer.toString());
        try {
            registry.restore("/aaa3", reader);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
    }
}
