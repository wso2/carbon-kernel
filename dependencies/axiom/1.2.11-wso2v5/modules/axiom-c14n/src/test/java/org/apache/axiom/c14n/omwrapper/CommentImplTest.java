/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.c14n.omwrapper;

import org.apache.axiom.c14n.omwrapper.interfaces.Comment;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.DataParser;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class CommentImplTest extends TestCase {
    private DataParser dp = null;
    private Comment comment = null;

    public CommentImplTest(String name){
        super(name);
    }

    public static Test suite() {
        return new TestSuite(CommentImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public void setUp() throws Exception{
        dp = new DataParser("/sample1.xml");
        dp.init();
        // <!-- Comment 2 -->
        comment = (Comment)dp.doc.getFirstChild().getNextSibling();
    }

    public void testGetData() {
        assertEquals(" Comment 2 ", comment.getData());
    }

    public void testGetParent(){
        assertEquals(Node.DOCUMENT_NODE, comment.getParentNode().getNodeType());
    }

    public void testGetNextSibling(){
        // this comment is inside the document so we don't want the next newline character
        // to get as a node
        assertEquals(Node.ELEMENT_NODE, comment.getNextSibling().getNodeType());
    }

    public void testGetPreviousSibling(){
        // this comment is inside the document so we don't want the previous newline character
        // to get as a node
        assertEquals(Node.DOCUMENT_TYPE_NODE, comment.getPreviousSibling().getNodeType());
    }

}
