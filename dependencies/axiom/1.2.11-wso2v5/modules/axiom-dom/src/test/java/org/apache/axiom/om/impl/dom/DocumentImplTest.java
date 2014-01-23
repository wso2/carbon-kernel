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

package org.apache.axiom.om.impl.dom;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axiom.om.OMDocumentTestBase;
import org.apache.axiom.om.impl.dom.factory.OMDOMMetaFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class DocumentImplTest extends OMDocumentTestBase {
    public DocumentImplTest() {
        super(new OMDOMMetaFactory());
    }

    public void testCreateElement() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                String tagName = "LocalName";
                String namespace = "http://ws.apache.org/axis2/ns";
                Document doc = dbf.newDocumentBuilder().newDocument();
                Element elem = doc.createElement(tagName);

                assertEquals("Local name misnatch", tagName, elem.getNodeName());

                elem = doc.createElementNS(namespace, "axis2:" + tagName);
                assertEquals("Local name misnatch", tagName, elem.getLocalName());
                assertEquals("Namespace misnatch", namespace, elem.getNamespaceURI());
            }
        });
    }

    public void testCreateAttribute() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                String attrName = "attrIdentifier";
                String attrValue = "attrValue";
                String attrNs = "http://ws.apache.org/axis2/ns";
                String attrNsPrefix = "axis2";
        
                Document doc = dbf.newDocumentBuilder().newDocument();
                Attr attr = doc.createAttribute(attrName);
        
                assertEquals("Attr name mismatch", attrName, attr.getName());
                assertNull("Namespace value should be null", attr.getNamespaceURI());
        
        
                attr = doc.createAttributeNS(attrNs, attrNsPrefix + ":" + attrName);
                assertEquals("Attr name mismatch", attrName, attr.getLocalName());
                assertNotNull("Namespace value should not be null", attr.getNamespaceURI());
                assertEquals("NamsspaceURI mismatch", attrNs, attr.getNamespaceURI());
                assertEquals("namespace prefix mismatch", attrNsPrefix, attr.getPrefix());
        
                attr.setValue(attrValue);
            }
        });
    }

    public void testCreateText() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                String textValue = "temp text value";
        
                Document doc = dbf.newDocumentBuilder().newDocument();
                Text txt = doc.createTextNode(textValue);
        
                assertEquals("Text value mismatch", textValue, txt.getData());
            }
        });
    }

    public void testDocumentSiblings() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().newDocument();
                Element elem = doc.createElement("test");
                doc.appendChild(elem);
        
                Node node = doc.getNextSibling();
                assertNull("Document's next sibling has to be null", node);
                Node node2 = doc.getPreviousSibling();
                assertNull("Document's previous sibling has to be null", node2);
                Node node3 = doc.getParentNode();
                assertNull("Document's parent has to be null", node3);
            }
        });
    }

    public void testAllowedChildren() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().newDocument();
                
                doc.appendChild(doc.createComment("some comment"));
                doc.appendChild(doc.createProcessingInstruction("pi", "data"));
                
                // Document Object Model (DOM) Level 3 Core Specification, section 1.1.1
                // says that text nodes are not allowed as children of a document.
                try {
                    doc.appendChild(doc.createTextNode("    "));
                    fail("Expected DOMException");
                } catch (DOMException ex) {
                    assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);
                }
                
                doc.appendChild(doc.createElement("root1"));
                
                // Multiple document elements are not allowed
                try {
                    doc.appendChild(doc.createElement("root2"));
                    fail("Expected DOMException");
                } catch (DOMException ex) {
                    assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);
                }
                
                // PIs and comments after the document element are allowed
                doc.appendChild(doc.createProcessingInstruction("pi", "data"));
                doc.appendChild(doc.createComment("some comment"));
                
                // Again, text nodes are not allowed
                try {
                    doc.appendChild(doc.createTextNode("    "));
                    fail("Expected DOMException");
                } catch (DOMException ex) {
                    assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);
                }
            }
        });
    }
}
