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

public class SymlinkTest extends BaseTestCase {

    protected static Registry registry = null;
    
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
            comp.registerBuiltInHandlers(embeddedRegistryService);
            registry = embeddedRegistryService.getSystemRegistry();
        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }



    public void testSymbolicLinks() throws RegistryException {
        Collection testCollection = registry.newCollection();
        Resource testResource = registry.newResource();
        testCollection.setProperty("name", "valueC");
        registry.put("/testCollection", testCollection);
        testResource.setProperty("name", "value1");
        registry.put("/testCollection/testResource", testResource);
        
        registry.createLink("/mountCollection", "/testCollection");
        Resource mountCollection = registry.get("/mountCollection");
        assertEquals(mountCollection.getProperty("name"), "valueC");
        String [] children = (String [])mountCollection.getContent();
        assertEquals(children[0], "/mountCollection/testResource");
        Resource mountedResource = registry.get("/mountCollection/testResource");
        assertEquals(mountedResource.getProperty("name"), "value1");

        // trying to create a symlink in an already existing location
        registry.createLink("/testCollection", "/");
        boolean exceptionOccurred = false;
        try {
            registry.createLink("/testCollection", "/testCollection");
        } catch (Exception e) {
            exceptionOccurred = true;
        }
        assertTrue("Symlink link to itself is not valid", exceptionOccurred);

    }


    public void testSymbolicLinksRoots() throws RegistryException {
        Collection testCollection = registry.newCollection();
        Resource testResource = registry.newResource();
        testCollection.setProperty("name", "valueC");
        registry.put("/", testCollection);
        testResource.setProperty("name", "value1");
        registry.put("/testCollection2", testResource);

        registry.createLink("/mountCollection2", "/");
        Resource mountCollection = registry.get("/mountCollection2");
        assertEquals(mountCollection.getProperty("name"), "valueC");
        String [] children = (String [])mountCollection.getContent();
        assertEquals(children[0], "/mountCollection2/testCollection2");
        Resource mountedResource = registry.get("/mountCollection2/testCollection2");
        assertEquals(mountedResource.getProperty("name"), "value1");
    }    

    public void testCopySourceSymLinkRoot() throws RegistryException {
        Collection testResource = registry.newCollection();
        registry.put("/a/b/c", testResource);
        registry.createLink("/p", "/a/b"); // now p is a link to b

        registry.copy("/p", "/q");

        assertTrue("q should have resource c", registry.resourceExists("/q/c"));

    }

    public void testCopySourceFromSymlink() throws RegistryException {
        Collection testResource = registry.newCollection();
        registry.put("/a1/b1/c1/d1", testResource);
        registry.createLink("/p1", "/a1/b1"); // now p is a link to b

        registry.copy("/p1/c1", "/q1");

        assertTrue("q1 should have resource d1", registry.resourceExists("/q1/d1"));

        
    }

    public void testCopyTargetToSymLink() throws RegistryException {

        Collection testResource = registry.newCollection();
        registry.put("/a2/b2/", testResource);
        registry.put("/c2/d2/", testResource);
        registry.createLink("/p2", "/a2/b2"); // now p is a link to b

        registry.copy("/c2", "/p2/c2");

        assertTrue("q2 should have resource c2", registry.resourceExists("/p2/c2/d2"));
        assertTrue("q2 should have resource c2", registry.resourceExists("/a2/b2/c2/d2"));
    }

    public void testCopyBothSymLink() throws RegistryException {

        Collection testResource = registry.newCollection();
        registry.put("/a3/b3/", testResource);
        registry.put("/c3/d3/e3", testResource);
        registry.createLink("/p3", "/a3/b3");   
        registry.createLink("/q3", "/c3/d3");

        registry.copy("/q3/e3", "/p3/e3");

        assertTrue("p3 should have resource e3", registry.resourceExists("/p3/e3"));
        assertTrue("a3/b3/ should have resource e3", registry.resourceExists("/a3/b3/e3"));
    }


    public void testMoveSourceSymLinkRoot() throws RegistryException {
        Collection testResource = registry.newCollection();
        registry.put("/a4/b4/c4", testResource);
        registry.createLink("/p4", "/a4/b4"); // now p is a link to b

        registry.move("/p4", "/q4");

        assertTrue("q4 should have resource c4", registry.resourceExists("/q4/c4"));

    }

    public void testMoveSourceFromSymlink() throws RegistryException {
        Collection testResource = registry.newCollection();
        registry.put("/a11/b11/c11/d11", testResource);
        registry.createLink("/p11", "/a11/b11"); // now p is a link to b

        registry.move("/p11/c11", "/q11");

        assertTrue("q11 should have resource d11", registry.resourceExists("/q11/d11"));


    }

    public void testMoveTargetToSymLink() throws RegistryException {

        Collection testResource = registry.newCollection();
        registry.put("/a21/b21/", testResource);
        registry.put("/c21/d21/", testResource);
        registry.createLink("/p21", "/a21/b21"); // now p is a link to b

        registry.move("/c21", "/p21/c21");

        assertTrue("q21 should have resource c21", registry.resourceExists("/p21/c21/d21"));
        assertTrue("q21 should have resource c21", registry.resourceExists("/a21/b21/c21/d21"));
    }

    public void testMoveBothSymLink() throws RegistryException {

        Collection testResource = registry.newCollection();
        registry.put("/a31/b31/", testResource);
        registry.put("/c31/d31/e31", testResource);
        registry.createLink("/p31", "/a31/b31");
        registry.createLink("/q31", "/c31/d31");

        registry.move("/q31/e31", "/p31/e31");

        assertTrue("p31 should have resource e31", registry.resourceExists("/p31/e31"));
        assertTrue("a31/b31/ should have resource e31", registry.resourceExists("/a31/b31/e31"));
    }


    public void testMoveSourceSymLinkRootCollection() throws RegistryException {
        Resource testResource = registry.newResource();
        registry.put("/a6/b6/c6", testResource);
        registry.createLink("/p6", "/a6/b6"); // now p is a link to b
        registry.createLink("/x6", "/a6/b6/c6");

        registry.move("/p6", "/q6");
        registry.move("/x6", "/y6");

        assertTrue("q6 should be a collection", registry.get("/q6") instanceof Collection);
        assertTrue("y6 should be a non-collection", !(registry.get("/y6") instanceof Collection));

    }

    public void testCopyParentWithSymlink() throws RegistryException {
        // adding basic resources.
        Resource testR = registry.newResource();
        testR.setContent("test R content");
        registry.put("/target/originalR", testR);
        Resource testR2 = registry.newResource();
        testR2.setContent("test R2 content");
        registry.put("/target/originalC/r2", testR2);

        Collection col = registry.newCollection();
        registry.put("/source", col);

        // creating the sym link
        registry.createLink("/source/symR", "/target/originalR");
        registry.createLink("/source/symC", "/target/originalC");

        // now just check the link is created
        Resource testRSym1 = registry.get("/source/symR");
        byte[] testRSym1Bytes = (byte[])testRSym1.getContent();
        assertEquals("test R content", RegistryUtils.decodeBytes(testRSym1Bytes));
        
        Resource testR2Sym1 = registry.get("/source/symC/r2");
        byte[] testR2Sym1Bytes = (byte[])testR2Sym1.getContent();
        assertEquals("test R2 content", RegistryUtils.decodeBytes(testR2Sym1Bytes));

        // now copy the source
        registry.copy("/source", "/source-copy");

        // now check the copied symbolic links
        Resource testRSym2 = registry.get("/source-copy/symR");
        byte[] testRSym2Bytes = (byte[])testRSym2.getContent();
        assertEquals("test R content", RegistryUtils.decodeBytes(testRSym2Bytes));

        Resource testR2Sym2 = registry.get("/source-copy/symC/r2");
        byte[] testR2Sym2Bytes = (byte[])testR2Sym2.getContent();
        assertEquals("test R2 content", RegistryUtils.decodeBytes(testR2Sym2Bytes));

        // change the symbolic links and whether the original is updated
        testRSym2.setContent("test R updated content");
        registry.put("/source-copy/symR", testRSym2);
        testR2Sym2.setContent("test R2 updated content");
        registry.put("/source-copy/symC/r2", testR2Sym2);

        // and check whether the original is updated.
        testR = registry.get("/target/originalR");
        byte[] testRBytes = (byte[])testR.getContent();
        assertEquals("test R updated content", RegistryUtils.decodeBytes(testRBytes));

        testR2 = registry.get("/target/originalC/r2");
        byte[] testR2Bytes = (byte[])testR2.getContent();
        assertEquals("test R2 updated content", RegistryUtils.decodeBytes(testR2Bytes));

        // cleaning up for the next test case
        registry.delete("/source");
        registry.delete("/source-copy");
        registry.delete("/target");
    }

    public void testMoveParentWithSymlink() throws RegistryException {
        // adding basic resources.
        Resource testR = registry.newResource();
        testR.setContent("test R content");
        registry.put("/target/originalR", testR);
        Resource testR2 = registry.newResource();
        testR2.setContent("test R2 content");
        registry.put("/target/originalC/r2", testR2);

        Collection col = registry.newCollection();
        registry.put("/source", col);

        // creating the sym link
        registry.createLink("/source/symR", "/target/originalR");
        registry.createLink("/source/symC", "/target/originalC");

        // now just check the link is created
        Resource testRSym1 = registry.get("/source/symR");
        byte[] testRSym1Bytes = (byte[])testRSym1.getContent();
        assertEquals("test R content", RegistryUtils.decodeBytes(testRSym1Bytes));


        Resource testR2Sym1 = registry.get("/source/symC/r2");
        byte[] testR2Sym1Bytes = (byte[])testR2Sym1.getContent();
        assertEquals("test R2 content", RegistryUtils.decodeBytes(testR2Sym1Bytes));

        // now copy the source
        registry.move("/source", "/source-copy");

        // now check the copied symbolic links
        Resource testRSym2 = registry.get("/source-copy/symR");
        byte[] testRSym2Bytes = (byte[])testRSym2.getContent();
        assertEquals("test R content", RegistryUtils.decodeBytes(testRSym2Bytes));

        Resource testR2Sym2 = registry.get("/source-copy/symC/r2");
        byte[] testR2Sym2Bytes = (byte[])testR2Sym2.getContent();
        assertEquals("test R2 content", RegistryUtils.decodeBytes(testR2Sym2Bytes));

        // change the symbolic links and whether the original is updated
        testRSym2.setContent("test R updated content");
        registry.put("/source-copy/symR", testRSym2);
        testR2Sym2.setContent("test R2 updated content");
        registry.put("/source-copy/symC/r2", testR2Sym2);

        // and check whether the original is updated.
        testR = registry.get("/target/originalR");
        byte[] testRBytes = (byte[])testR.getContent();
        assertEquals("test R updated content", RegistryUtils.decodeBytes(testRBytes));

        testR2 = registry.get("/target/originalC/r2");
        byte[] testR2Bytes = (byte[])testR2.getContent();
        assertEquals("test R2 updated content", RegistryUtils.decodeBytes(testR2Bytes));

        // cleaning up for the next test case
        registry.delete("/source-copy");
        registry.delete("/target");
    }


    public void testRenameParentWithSymlink() throws RegistryException {
        // adding basic resources.
        Resource testR = registry.newResource();
        testR.setContent("test R content");
        registry.put("/target/originalR", testR);
        Resource testR2 = registry.newResource();
        testR2.setContent("test R2 content");
        registry.put("/target/originalC/r2", testR2);

        Collection col = registry.newCollection();
        registry.put("/source", col);

        // creating the sym link
        registry.createLink("/source/symR", "/target/originalR");
        registry.createLink("/source/symC", "/target/originalC");

        // now just check the link is created
        Resource testRSym1 = registry.get("/source/symR");
        byte[] testRSym1Bytes = (byte[])testRSym1.getContent();
        assertEquals("test R content", RegistryUtils.decodeBytes(testRSym1Bytes));

        Resource testR2Sym1 = registry.get("/source/symC/r2");
        byte[] testR2Sym1Bytes = (byte[])testR2Sym1.getContent();
        assertEquals("test R2 content", RegistryUtils.decodeBytes(testR2Sym1Bytes));

        // now copy the source
        registry.rename("/source", "source-copy");

        // now check the copied symbolic links
        Resource testRSym2 = registry.get("/source-copy/symR");
        byte[] testRSym2Bytes = (byte[])testRSym2.getContent();
        assertEquals("test R content", RegistryUtils.decodeBytes(testRSym2Bytes));

        Resource testR2Sym2 = registry.get("/source-copy/symC/r2");
        byte[] testR2Sym2Bytes = (byte[])testR2Sym2.getContent();
        assertEquals("test R2 content", RegistryUtils.decodeBytes(testR2Sym2Bytes));

        // change the symbolic links and whether the original is updated
        testRSym2.setContent("test R updated content");
        registry.put("/source-copy/symR", testRSym2);
        testR2Sym2.setContent("test R2 updated content");
        registry.put("/source-copy/symC/r2", testR2Sym2);

        // and check whether the original is updated.
        testR = registry.get("/target/originalR");
        byte[] testRBytes = (byte[])testR.getContent();
        assertEquals("test R updated content", RegistryUtils.decodeBytes(testRBytes));

        testR2 = registry.get("/target/originalC/r2");
        byte[] testR2Bytes = (byte[])testR2.getContent();
        assertEquals("test R2 updated content", RegistryUtils.decodeBytes(testR2Bytes));

        // cleaning up for the next test case
        registry.delete("/source-copy");
        registry.delete("/target");
    }

    public void testSymlinkOrder() throws RegistryException {
        // here is the plan: we are creating following 2 circular symbolic links in reverse order
        // and get it working somehow (the following should be considered as
        // sym-link => real-path
        // 1. /Root_fake => /Root_real
        // 2. /Root_real/insiderA/resource_fake => /Root_fake/insiderB/insiderC/resource_real
        // so we will start with putting the following resources
        // 1. /Root_real/insiderA (collection)
        // 2. /Root_real/insiderB/insiderC/resource_real (resource).

        Collection c1 = registry.newCollection();
        registry.put("/Root_real/insiderA", c1);

        Resource r1 = registry.newResource();
        r1.setContent("guess me if you can");
        r1.setProperty("key1", "cycle-value1");
        r1.setProperty("key2", "cycle-value2");
        registry.put("/Root_real/insiderB/insiderC/resource_real", r1);

        // bang... now create the symbolic links in the opposite order

        registry.createLink("/Root_real/insiderA/resource_fake",
                "/Root_fake/insiderB/insiderC/resource_real");

        registry.createLink("/Root_fake", "/Root_real");

        registry.rateResource("/Root_real/insiderB/insiderC/resource_real", 3);
        registry.addComment("/Root_real/insiderB/insiderC/resource_real", new Comment("cycle-cm1"));
        registry.addComment("/Root_real/insiderB/insiderC/resource_real", new Comment("cycle-cm2"));

        registry.addAssociation("/Root_real/insiderB/insiderC/resource_real",
                "/Root_real/insiderA", "bang1");
        registry.addAssociation("/Root_real/insiderB/insiderC/resource_real",
                "/", "bang2");


        // so just checking our circular symlink work just checking whether our resource can be
        // accessed through the first symlink.

        Resource r2 = registry.get("/Root_real/insiderA/resource_fake");
        assertTrue(!(r2 instanceof Collection));

        byte[] content = (byte[])r2.getContent();
        String contentStr = RegistryUtils.decodeBytes(content);
        assertEquals("guess me if you can", contentStr);
        assertEquals("cycle-value1", r2.getProperty("key1"));
        assertEquals("cycle-value2", r2.getProperty("key2"));
        assertEquals(3, registry.getRating("/Root_real/insiderA/resource_fake",
                "wso2.system.user"));
        Comment[] comments = registry.getComments("/Root_real/insiderA/resource_fake");
        assertTrue(("cycle-cm1".equals(comments[0].getText()) &&
                "cycle-cm2".equals(comments[1].getText())) ||
                ("cycle-cm2".equals(comments[0].getText()) &&
                        "cycle-cm1".equals(comments[1].getText())));

        registry.getAllAssociations("/Root_real/insiderA/resource_fake");

    }

    public void testSymlinkOrder2() throws RegistryException {
        // here is the plan: we are creating following 2 circular symbolic links in reverse order
        // and get it working somehow (the following should be considered as
        // sym-link => real-path
        // 1. /Root_fake => /Root_real
        // 2. /Root_real/insiderA/resource_fake => /Root_fake/insiderB/insiderC/resource_real
        // so we will start with putting the following resources
        // 1. /Root_real/insiderA (collection)
        // 2. /Root_real/insiderB/insiderC/resource_real (resource).

        Collection c1 = registry.newCollection();
        registry.put("/myRoot/test", c1);

        registry.createLink("/mySymbolicLink", "/myRoot");

        Resource r1 = registry.newResource();
        r1.setContent("guess me if you can");
        r1.setProperty("key1", "cycle-value1");
        r1.setProperty("key2", "cycle-value2");
        registry.put("/mySymbolicLink/test", r1);

        Collection c2 = registry.newCollection();
        registry.put("/myRoot/aaa", c2);

        // bang... now create the symbolic links in the opposite order

        registry.createLink("/myRoot/aaa/symlink2",
                "/mySymbolicLink/test");


        registry.rateResource("/myRoot/aaa/symlink2", 3);
        registry.addComment("/myRoot/aaa/symlink2", new Comment("cycle-cm1"));
        registry.addComment("/myRoot/aaa/symlink2", new Comment("cycle-cm2"));

        registry.addAssociation("/myRoot/aaa/symlink2",
                "/carbon", "bang1");
        registry.addAssociation("/myRoot/aaa/symlink2",
                "/system", "bang2");


        // so just checking our circular symlink work just checking whether our resource can be
        // accessed through the first symlink.

        Resource r2 = registry.get("/myRoot/aaa/symlink2");
        assertTrue(!(r2 instanceof Collection));

        byte[] content = (byte[])r2.getContent();
        String contentStr = RegistryUtils.decodeBytes(content);
        assertEquals("guess me if you can", contentStr);
        assertEquals("cycle-value1", r2.getProperty("key1"));
        assertEquals("cycle-value2", r2.getProperty("key2"));
        assertEquals(3, registry.getRating("/myRoot/aaa/symlink2", "wso2.system.user"));
        Comment[] comments = registry.getComments("/myRoot/aaa/symlink2");
        assertTrue(("cycle-cm1".equals(comments[0].getText()) &&
                "cycle-cm2".equals(comments[1].getText())) ||
                ("cycle-cm2".equals(comments[0].getText()) &&
                        "cycle-cm1".equals(comments[1].getText())));

        Association[] associations = registry.getAllAssociations("/myRoot/aaa/symlink2");

        assertEquals(associations[0].getDestinationPath(), "/carbon");
        assertEquals(associations[0].getSourcePath(), "/myRoot/aaa/symlink2");

        assertEquals(associations[1].getDestinationPath(), "/system");
        assertEquals(associations[1].getSourcePath(), "/myRoot/aaa/symlink2");
    }


//    public void testSimilarCollectionNames() throws RegistryException {
//        // put https like collections
//        Resource r = registry.newResource();
//        registry.put("/fooTest-src/https/listener/keystore", r);
//        r = registry.newResource();
//        registry.put("/fooTest-src/https/listener/port", r);
//        r = registry.newResource();
//        registry.put("/fooTest-src/https/sender/keystore", r);
//        r = registry.newResource();
//        registry.put("/fooTest-src/https/sender/port", r);
//
//        r = registry.newResource();
//        registry.put("/fooTest-src/http/listener/keystore", r);
//        r = registry.newResource();
//        registry.put("/fooTest-src/http/listener/port", r);
//        r = registry.newResource();
//        registry.put("/fooTest-src/http/sender/keystore", r);
//        r = registry.newResource();
//        registry.put("/fooTest-src/http/sender/port", r);
//
//        Writer writer = new StringWriter();
//        registry.dump("/fooTest-src", writer);
//
//        Reader reader = new StringReader(writer.toString());
//        registry.restore("/fooTest-destination", reader);
//
//        // now check the https resources
//        assertTrue(registry.resourceExists("/fooTest-destination/http/listener/keystore"));
//        assertTrue(registry.resourceExists("/fooTest-destination/https/listener/keystore"));
//    }

//    public void testSymlinkUpdates() throws RegistryException {
//        Resource r = registry.newResource();
//        registry.put("/barTest/Root1/bang", r);
//
//        registry.createLink("/barTest/Root", "/barTest/Root1");
//        assertTrue(registry.resourceExists("/barTest/Root1/bang"));
//        assertTrue(registry.resourceExists("/barTest/Root/bang"));
//
//        Writer writer1 = new StringWriter();
//        registry.dump("/barTest", writer1);
//
//        // check-in to separate location
//        Reader reader1 = new StringReader(writer1.toString());
//        registry.restore("/barTest-target", reader1);
//        assertTrue(registry.resourceExists("/barTest-target/Root1/bang"));
//        assertTrue(registry.resourceExists("/barTest-target/Root/bang"));
//
//        // now change the symlink to Root2
//        r = registry.newResource();
//        registry.put("/barTest/Root2/kaboom", r);
//
//        registry.removeLink("/barTest/Root");
//        registry.createLink("/barTest/Root", "/barTest/Root2");
//        assertFalse(registry.resourceExists("/barTest/Root/bang"));
//        assertTrue(registry.resourceExists("/barTest/Root2/kaboom"));
//        assertTrue(registry.resourceExists("/barTest/Root/kaboom"));
//
//        // then get the dump of the thing again
//
//        registry.put("/barTest/Root1/bang", r);
//        Writer writer2 = new StringWriter();
//        registry.dump("/barTest", writer2);
//
//        Reader reader2 = new StringReader(writer2.toString());
//        registry.restore("/barTest-target", reader2);
//        assertTrue(registry.resourceExists("/barTest-target/Root1/bang"));
//        assertTrue(registry.resourceExists("/barTest-target/Root2/kaboom"));
//        assertFalse(registry.resourceExists("/barTest-target/Root/bang"));
//        assertTrue(registry.resourceExists("/barTest-target/Root/kaboom"));
//    }

    public void testTransitiveSymLinks() throws RegistryException {
        // create set of collections
        Resource r = registry.newResource();
        r.setContent("01");
        registry.put("/Root_01/r1", r);

        r.setContent("02");
        registry.put("/Root_02/r1", r);

        r.setContent("03");
        registry.put("/Root_03/r1", r);

        registry.createLink("/Root", "/Root_01");
        r = registry.get("/Root/r1");
        assertEquals("01", RegistryUtils.decodeBytes((byte[])r.getContent()));

        // dynamically change /Root to each of the above collections
        registry.createLink("/Root", "/Root_02");
        r = registry.get("/Root/r1");
        assertEquals("02", RegistryUtils.decodeBytes((byte[])r.getContent()));

        registry.createLink("/Root", "/Root_03");
        r = registry.get("/Root/r1");
        assertEquals("03", RegistryUtils.decodeBytes((byte[])r.getContent()));

        // create the transitive super link and check the behaviour by changing /Root symlink target
        registry.createLink("/super-link", "/Root/r1");

        registry.createLink("/Root", "/Root_03");
        r = registry.get("/super-link");
        assertEquals("03", RegistryUtils.decodeBytes((byte[])r.getContent()));

        registry.createLink("/Root", "/Root_02");
        r = registry.get("/super-link");
        assertEquals("02", RegistryUtils.decodeBytes((byte[])r.getContent()));

        registry.createLink("/Root", "/Root_01");
        r = registry.get("/super-link");
        assertEquals("01", RegistryUtils.decodeBytes((byte[])r.getContent()));


        // create a transitive and cyclic symlink at the same time and check the behaviour by
        // changing /Root
        registry.createLink("/Root_01/bang", "/Root/r1");

        registry.createLink("/Root", "/Root_03");
        r = registry.get("/Root_01/bang");
        assertEquals("03", RegistryUtils.decodeBytes((byte[])r.getContent()));

        registry.createLink("/Root", "/Root_02");
        r = registry.get("/Root_01/bang");
        assertEquals("02", RegistryUtils.decodeBytes((byte[])r.getContent()));

        registry.createLink("/Root", "/Root_01");
        r = registry.get("/Root_01/bang");
        assertEquals("01", RegistryUtils.decodeBytes((byte[])r.getContent()));
    }

}
