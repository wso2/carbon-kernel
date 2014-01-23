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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMElementTestBase;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axiom.om.impl.dom.factory.OMDOMMetaFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

public class ElementImplTest extends OMElementTestBase {
    public ElementImplTest() {
        super(new OMDOMMetaFactory());
    }

    public void testSerialize() throws Exception {
        OMDOMFactory factory = new OMDOMFactory();
        String localName = "TestLocalName";
        String namespace = "http://ws.apache.org/axis2/ns";
        String prefix = "axis2";
        String tempText = "The quick brown fox jumps over the lazy dog";
        String textToAppend = " followed by another";

        OMElement elem = factory.createOMElement(localName, namespace, prefix);
        OMText textNode = factory.createOMText(elem, tempText);

        ((Text) textNode).appendData(textToAppend);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        elem.serialize(baos);
        String xml = new String(baos.toByteArray());
        assertEquals("Incorrect serialized xml", 0, xml.indexOf("<axis2:TestLocalName"));
    }

    public void testAppendChild() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                String elementName = "TestElem";
                String childElemName = "TestChildElem";
                String childTextValue = "text value of the child text node";
        
                //Apending am Element node
                Document doc = dbf.newDocumentBuilder().newDocument();
                Element elem = doc.createElement(elementName);
                Element childElem = doc.createElement(childElemName);
        
                elem.appendChild(childElem);
        
                Element addedChild = (Element) elem.getFirstChild();
                assertNotNull("Child Element node missing", addedChild);
                assertEquals("Incorre node object", childElem, addedChild);
        
                elem = doc.createElement(elementName);
                Text text = doc.createTextNode(childTextValue);
                elem.appendChild(text);
        
                Text addedTextnode = (Text) elem.getFirstChild();
                assertNotNull("Child Text node missing", addedTextnode);
                assertEquals("Incorrect node object", text, addedTextnode);
            }
        });
    }
    
    public void testRemoveSingleChild() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                DocumentBuilder builder = dbf.newDocumentBuilder();
                Element element = builder.parse(new InputSource(new StringReader(
                        "<root><a/></root>"))).getDocumentElement();
                element.removeChild(element.getFirstChild());
                assertNull(element.getFirstChild());
                assertNull(element.getLastChild());
            }
        });
    }
    
    // Regression test for WSCOMMONS-435
    public void testRemoveFirstChild() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                DocumentBuilder builder = dbf.newDocumentBuilder();
                Element element = builder.parse(new InputSource(new StringReader(
                        "<root><a/><b/><c/></root>"))).getDocumentElement();
                element.removeChild(element.getFirstChild());
                Node firstChild = element.getFirstChild();
                assertNotNull(firstChild);
                assertEquals("b", firstChild.getNodeName());
            }
        });
    }

    public void testRemoveLastChild() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                DocumentBuilder builder = dbf.newDocumentBuilder();
                Element element = builder.parse(new InputSource(new StringReader(
                        "<root><a/><b/><c/></root>"))).getDocumentElement();
                element.removeChild(element.getLastChild());
                Node lastChild = element.getLastChild();
                assertNotNull(lastChild);
                assertEquals("b", lastChild.getNodeName());
            }
        });
    }
    
    /** Testing the NodeList returned with the elements's children */
    public void testGetElementsByTagName() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                String childElementLN = "Child";
        
                Document doc = dbf.newDocumentBuilder().newDocument();
                Element docElem = doc.getDocumentElement();
                assertNull("The document element shoudl be null", docElem);
        
                docElem = doc.createElement("Test");
                docElem.appendChild(doc.createElement(childElementLN));
                docElem.appendChild(doc.createElement(childElementLN));
                docElem.appendChild(doc.createElement(childElementLN));
                docElem.appendChild(doc.createElement(childElementLN));
                docElem.appendChild(doc.createElement(childElementLN));
                docElem.appendChild(doc.createElement(childElementLN));
                docElem.appendChild(doc.createElement(childElementLN));
        
                NodeList list = docElem.getElementsByTagName(childElementLN);
        
                assertEquals("Incorrect number of child elements", 7, list.getLength());
            }
        });
    }

    public void testGetElementsByTagNameWithNamespaces() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().newDocument();
                Element root = doc.createElementNS("urn:ns1", "ns1:root");
                for (int i=0; i<3; i++) {
                    root.appendChild(doc.createElementNS("urn:ns2", "ns2:child"));
                }
                assertEquals(3, root.getElementsByTagName("ns2:child").getLength());
                assertEquals(0, root.getElementsByTagName("child").getLength());
            }
        });
    }
    
    public void testGetElementsByTagNameWithWildcard() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().newDocument();
                Element root = doc.createElement("root");
                for (int i=0; i<3; i++) {
                    root.appendChild(doc.createElement("child" + i));
                }
                assertEquals(3, root.getElementsByTagName("*").getLength());
            }
        });
    }
    
    public void testGetElementsByTagNameNS() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                String childElementQN = "test:Child";
                String childElementLN = "Child";
                String childElementNS = "http://ws.apache.org/ns/axis2/dom";
        
                Document doc = dbf.newDocumentBuilder().newDocument();
                Element docElem = doc.getDocumentElement();
                assertNull("The document element shoudl be null", docElem);
        
                docElem = doc.createElementNS("http://test.org", "test:Test");
        
                docElem.appendChild(doc.createElementNS(childElementNS, childElementQN));
                docElem.appendChild(doc.createElementNS(childElementNS, childElementQN));
                docElem.appendChild(doc.createElementNS(childElementNS, childElementQN));
                docElem.appendChild(doc.createElementNS(childElementNS, childElementQN));
                docElem.appendChild(doc.createElementNS(childElementNS, childElementQN));
                docElem.appendChild(doc.createElementNS(childElementNS, childElementQN));
                docElem.appendChild(doc.createElementNS(childElementNS, childElementQN));
        
                NodeList list = docElem.getElementsByTagNameNS(childElementNS, childElementLN);
        
                assertEquals("Incorrect number of child elements", 7, list.getLength());
            }
        });
    }

    public void testGetElementsByTagNameRecursive() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc =
                        dbf.newDocumentBuilder().parse(getTestResource("xml/numbers.xml"));
                Element element = doc.getDocumentElement();
                NodeList list = element.getElementsByTagName("nr");
                assertEquals(10, list.getLength());
            }
        });
    }
    
    public void testGetNamespaceURIWithNoNamespace() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().newDocument();
                Element element = doc.createElement("test");
                assertNull(element.getNamespaceURI());
                element = doc.createElementNS(null, "test");
                assertNull(element.getNamespaceURI());
                element = doc.createElementNS("", "test");
                assertNull(element.getNamespaceURI());
            }
        });
    }
    
    public void testGetTextContent() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().parse(new InputSource(
                        new StringReader("<a>1<!--c--><b>2</b>3</a>")));
                assertEquals("123", doc.getDocumentElement().getTextContent());
            }
        });
    }
    
    public void testSetTextContent() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().parse(new InputSource(
                        new StringReader("<a>1<!--c--><b>2</b>3</a>")));
                Element element = doc.getDocumentElement();
                element.setTextContent("test");
                Node firstChild = element.getFirstChild();
                assertTrue(firstChild instanceof Text);
                assertEquals("test", firstChild.getNodeValue());
                assertNull(firstChild.getNextSibling());
            }
        });
    }

    public void testAttributes() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().parse(getTestResource("attributetest.xml"));

                // Check whether body has attributes
                Element bodyElement = doc.getDocumentElement();
                assertTrue(bodyElement.hasAttributes());

                Element directionResponse = (Element)bodyElement.getElementsByTagName("GetDirectionsResponse").item(0);
                assertTrue(directionResponse.hasAttributes());

                NamedNodeMap attributes = directionResponse.getAttributes();
                Attr attr = (Attr)attributes.item(0);
                assertEquals("xmlns", attr.getName());
                assertEquals("http://www.example.org/webservices/", attr.getValue());

                Element directionResult = (Element)bodyElement.getElementsByTagName("GetDirectionsResult").item(0);
                assertFalse(directionResult.hasAttributes());

                Element drivingDirection = (Element)directionResult.getElementsByTagName("drivingdirections").item(0);
                assertTrue(drivingDirection.hasAttributes());

                attributes = drivingDirection.getAttributes();
                attr = (Attr)attributes.item(0);
                assertEquals("xmlns", attr.getName());
                assertEquals("", attr.getValue());


                Element route = (Element)drivingDirection.getElementsByTagName("route").item(0);
                assertTrue(route.hasAttributes());

                attributes = route.getAttributes();
                attr = (Attr)attributes.item(0);
                assertEquals("distanceToTravel", attr.getName());
                assertEquals("500m", attr.getValue());

                attr = (Attr)attributes.item(1);
                assertEquals("finalStep", attr.getName());
                assertEquals("false", attr.getValue());

                attr = (Attr)attributes.item(2);
                assertEquals("id", attr.getName());
                assertEquals("0", attr.getValue());
            }
        });
    }

    public void testAttributes2() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(
                        "<root><child xmlns=\"\"/></root>")));
                Element element = (Element)doc.getDocumentElement().getFirstChild();
                assertTrue(element.hasAttributes());
                NamedNodeMap attributes = element.getAttributes();
                assertEquals(1, attributes.getLength());
                Attr attr = (Attr)attributes.item(0);
                assertEquals("xmlns", attr.getName());
                assertNull(attr.getPrefix());
                assertEquals("xmlns", attr.getLocalName());
                assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, attr.getNamespaceURI());
                assertEquals("", attr.getValue());
            }
        });
    }

    public void testAttributes3() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(
                        "<root><child xmlns:p=\"urn:ns1\"/></root>")));
                Element element = (Element)doc.getDocumentElement().getFirstChild();
                assertTrue(element.hasAttributes());
                NamedNodeMap attributes = element.getAttributes();
                assertEquals(1, attributes.getLength());
                Attr attr = (Attr)attributes.item(0);
                assertEquals("xmlns:p", attr.getName());
                assertEquals("xmlns", attr.getPrefix());
                assertEquals("p", attr.getLocalName());
                assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, attr.getNamespaceURI());
                assertEquals("urn:ns1", attr.getValue());
            }
        });
    }

    public void testAttributes4() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(
                        "<root><child/></root>")));
                Element element = (Element)doc.getDocumentElement().getFirstChild();
                assertFalse(element.hasAttributes());                  
            }
        });
    }
}
