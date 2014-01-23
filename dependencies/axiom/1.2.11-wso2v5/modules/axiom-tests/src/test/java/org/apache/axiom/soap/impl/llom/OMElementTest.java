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

package org.apache.axiom.soap.impl.llom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMTestCase;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;

public class OMElementTest extends OMTestCase implements OMConstants {
    private static final String WSA_URI = "http://schemas.xmlsoap.org/ws/2004/03/addressing";
    private static final String WSA_TO = "To";
    private static Log log = LogFactory.getLog(OMElementTest.class);

    OMFactory factory = OMAbstractFactory.getOMFactory();
    private OMElement firstElement;
    private OMElement secondElement;


    public OMElementTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        OMNamespace testingNamespace = factory.createOMNamespace(
                "http://testing.ws.org", "ws");
        firstElement = factory.createOMElement("FirstElement", testingNamespace);
        secondElement = factory.createOMElement("SecondElement", factory.createOMNamespace(
                "http://moretesting.ws.org", "ws"), firstElement);
    }
    
    protected void tearDown() throws Exception {
    }

    public void testGetText() {
        try {
            StAXSOAPModelBuilder soapBuilder = getOMBuilder(
                    "soap/OMElementTest.xml");
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) soapBuilder.getDocumentElement();
            OMElement wsaTo = soapEnvelope.getHeader().getFirstChildWithName(
                    new QName(WSA_URI, WSA_TO));

            String expectedString = "http://localhost:8081/axis/services/BankPort";
            assertEquals("getText is not returning the correct value",
                         wsaTo.getText().trim(),
                         expectedString);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void testConstructors() {

        try {
            factory.createOMElement("", null);
            fail("This should fail as OMElement should not be allowed to create without a local name ");
        } catch (Exception e) {
            assertTrue(true);
        }

        assertTrue("Namespace having same information, declared in the same context, should share" +
                " the same namespace object",
                   firstElement.getNamespace() != secondElement.getNamespace());
        assertEquals("OMElement children addition has not worked properly", secondElement,
                     firstElement.getFirstElement());

        OMNamespace testNamespace2 = factory.createOMNamespace("ftp://anotherTest.ws.org", "ws");
        firstElement.declareNamespace(testNamespace2);

        OMNamespace inheritedSecondNamespace =
                secondElement.findNamespace(testNamespace2.getNamespaceURI(),
                                            testNamespace2.getPrefix());
        assertNotNull("Children should inherit namespaces declared in parent",
                      inheritedSecondNamespace);
        assertEquals("inherited namespace uri should be equal",
                     inheritedSecondNamespace.getNamespaceURI(), testNamespace2.getNamespaceURI());
        assertEquals("inherited namespace prefix should be equal",
                     inheritedSecondNamespace.getPrefix(), testNamespace2.getPrefix());


    }

    public void testChildDetachment() {
        OMNamespace testNamespace2 = factory.createOMNamespace("ftp://anotherTest.ws.org", "ws");

        secondElement.detach();
        assertTrue("OMElement children detachment has not worked properly",
                   !secondElement.equals(firstElement.getFirstElement()));
        assertNull("First Element should not contain elements after detaching. ",
                   firstElement.getFirstElement());
        assertNull("First Element should not contain elements after detaching. ",
                   firstElement.getFirstOMChild());
        assertNull(secondElement.findNamespace(testNamespace2.getNamespaceURI(),
                                               testNamespace2.getPrefix()));

        firstElement.addChild(secondElement);
        firstElement.setText("Some Sample Text");

        assertTrue("First added child must be the first child",
                   secondElement.equals(firstElement.getFirstOMChild()));
        Iterator children = firstElement.getChildren();
        int childCount = 0;
        while (children.hasNext()) {
            children.next();
            childCount++;
        }
        assertEquals("Children count should be two", childCount, 2);

        secondElement.detach();
        assertTrue("First child should be the text child",
                   firstElement.getFirstOMChild() instanceof OMText);

    }

    public void testAddDOOMElementAsChild() throws XMLStreamException {
        OMFactory doomFactory = DOOMAbstractFactory.getOMFactory();
        OMFactory llomFactory = OMAbstractFactory.getOMFactory();
        String text = "This was a DOOM Text";

        OMElement llomRoot = llomFactory.createOMElement("root", null);
        OMElement doomElement = doomFactory.createOMElement("second", "test", "a");
        doomElement.setText(text);
        llomRoot.addChild(doomElement);

        OMElement newElement = (new StAXOMBuilder(this.factory, llomRoot
                .getXMLStreamReader())).getDocumentElement();
        newElement.build();
        OMElement secondElement = newElement.getFirstElement();
        assertNotNull(secondElement);
        assertEquals(secondElement.getText(), text);
    }

    public void testAddDOOMTextAsChild() throws XMLStreamException {
        OMFactory doomFactory = DOOMAbstractFactory.getOMFactory();
        OMFactory llomFactory = OMAbstractFactory.getOMFactory();
        String text = "This was a DOOM Text";

        OMElement llomRoot = llomFactory.createOMElement("root", null);
        OMText doomText = doomFactory.createOMText(text);
        llomRoot.addChild(doomText);

        OMElement newElement = (new StAXOMBuilder(this.factory, llomRoot
                .getXMLStreamReader())).getDocumentElement();
        newElement.build();
        assertEquals(newElement.getText(), text);
    }

    public void testAddLLOMElementChildToDOOM() throws XMLStreamException {
        OMFactory doomFactory = DOOMAbstractFactory.getOMFactory();
        OMFactory llomFactory = OMAbstractFactory.getOMFactory();
        String text = "This was a LLOM Text";

        OMElement doomRoot = doomFactory.createOMElement("root", null);
        OMElement llomElement = llomFactory.createOMElement("second", "test", "a");
        llomElement.setText(text);
        doomRoot.addChild(llomElement);

        OMElement newElement = (new StAXOMBuilder(this.factory, doomRoot
                .getXMLStreamReader())).getDocumentElement();
        newElement.build();
        OMElement secondElement = newElement.getFirstElement();
        assertNotNull(secondElement);
        assertEquals(secondElement.getText(), text);
    }

    public void testAddLLOMTextChildToDOOM() throws XMLStreamException {
        OMFactory doomFactory = DOOMAbstractFactory.getOMFactory();
        OMFactory llomFactory = OMAbstractFactory.getOMFactory();
        String text = "This was a DOOM Text";

        OMElement doomRoot = doomFactory.createOMElement("root", null);
        OMText llomText = llomFactory.createOMText(text);
        OMComment comment = llomFactory.createOMComment(null, "comment");
        doomRoot.addChild(llomText);
        doomRoot.addChild(comment);

        OMElement newElement = (new StAXOMBuilder(this.factory, doomRoot
                .getXMLStreamReader())).getDocumentElement();
        newElement.build();
        assertEquals(newElement.getText(), text);
    }

}
