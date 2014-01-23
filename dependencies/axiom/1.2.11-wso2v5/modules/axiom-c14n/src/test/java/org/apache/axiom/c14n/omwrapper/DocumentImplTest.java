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

import org.apache.axiom.c14n.omwrapper.interfaces.NodeList;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.DataParser;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class DocumentImplTest extends TestCase {
    private DataParser dp = null;

    public DocumentImplTest(String name){
        super(name);
    }

    public static Test suite() {
        return new TestSuite(DocumentImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public void setUp() throws Exception{
        dp = new DataParser("/sample1.xml");
        dp.init();
    }

    public void testGetChildNode() {
        // this nl should not contain any text node
        NodeList nl = dp.doc.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            assertFalse("node type can't be Text", (Node.TEXT_NODE == nl.item(i).getNodeType()));
        }
    }

    public void testGetNextSibling() {
        assertNull("getNextSibling() should return null", dp.doc.getNextSibling());
    }

    public void testGetPreviousSibling() {
        assertNull("getPreviousSibling() should return null", dp.doc.getPreviousSibling());
    }

    public void testGetParentNode() {
        assertNull("getParentNode() should return null", dp.doc.getParentNode());
    }

    public void testGetFirstChild(){
        assertFalse("document should not return Text nodes",
                (Node.TEXT_NODE == dp.doc.getFirstChild().getNodeType()));

        assertEquals(Node.DOCUMENT_TYPE_NODE, dp.doc.getFirstChild().getNodeType());
    }

    public void testGetDocumentElement() {
        assertEquals(Node.ELEMENT_NODE, dp.doc.getDocumentElement().getNodeType());
        assertEquals("doc", dp.doc.getDocumentElement().getNodeName());
    }
}
