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

package org.apache.axiom.om.impl.builder;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.testutils.InvocationCounter;
import org.apache.axiom.testutils.io.ExceptionInputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLStreamReader;

public class StAXOMBuilderTest extends AbstractTestCase {
    StAXOMBuilder stAXOMBuilder;
    private OMElement rootElement;

    /** Constructor. */
    public StAXOMBuilderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        stAXOMBuilder =
                OMXMLBuilderFactory.createStAXOMBuilder(
                        OMAbstractFactory.getSOAP11Factory(),
                        StAXUtils.createXMLStreamReader(
                                getTestResource("non_soap.xml")));
    }

    protected void tearDown() throws Exception {
        stAXOMBuilder.close();
    }

    public void testGetRootElement() throws Exception {
        rootElement = stAXOMBuilder.getDocumentElement();
        assertTrue("Root element can not be null", rootElement != null);
        assertTrue(" Name of the root element is wrong",
                   rootElement.getLocalName().equalsIgnoreCase("Root"));
        // get the first OMElement child
        OMNode omnode = rootElement.getFirstOMChild();
        while (omnode instanceof OMText) {
            omnode = omnode.getNextOMSibling();
        }
        Iterator children = ((OMElement) omnode).getChildren();
        int childrenCount = 0;
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();
            if (node instanceof OMElement)
                childrenCount++;
        }
        assertTrue(childrenCount == 5);
    }
    
    public void testClose1() throws Exception {
        rootElement = stAXOMBuilder.getDocumentElement();
        assertTrue("Root element can not be null", rootElement != null);
        assertTrue(" Name of the root element is wrong",
                   rootElement.getLocalName().equalsIgnoreCase("Root"));
        // get the first OMElement child
        OMNode omnode = rootElement.getFirstOMChild();
        while (omnode instanceof OMText) {
            omnode = omnode.getNextOMSibling();
        }
        // Close the element immediately
        OMElement omElement = (OMElement) omnode;
        omElement.close(false);
        
        Iterator children = ((OMElement) omnode).getChildren();
        int childrenCount = 0;
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();
            if (node instanceof OMElement)
                childrenCount++;
        }
        
        assertTrue(childrenCount == 0);
    }
    
    public void testClose2() throws Exception {
        rootElement = stAXOMBuilder.getDocumentElement();
        assertTrue("Root element can not be null", rootElement != null);
        assertTrue(" Name of the root element is wrong",
                   rootElement.getLocalName().equalsIgnoreCase("Root"));
        // get the first OMElement child
        OMNode omnode = rootElement.getFirstOMChild();
        while (omnode instanceof OMText) {
            omnode = omnode.getNextOMSibling();
        }
        // Close the element after building the element
        OMElement omElement = (OMElement) omnode;
        omElement.close(true);
        
        Iterator children = ((OMElement) omnode).getChildren();
        int childrenCount = 0;
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();
            if (node instanceof OMElement)
                childrenCount++;
        }
        
        assertTrue(childrenCount == 5);
    }
    
    public void testInvalidXML() throws Exception {
        XMLStreamReader originalReader = StAXUtils.createXMLStreamReader(getTestResource("invalid_xml.xml"));
        InvocationCounter invocationCounter = new InvocationCounter();
        XMLStreamReader reader = (XMLStreamReader)invocationCounter.createProxy(originalReader);
        
        StAXOMBuilder stAXOMBuilder =
                OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getSOAP11Factory(),
                                                        reader);
        
        Exception exception = null;
        while (exception == null || stAXOMBuilder.isCompleted()) {
            try {
                stAXOMBuilder.next();
            } catch (Exception e) {
                exception =e;
            }
        }
        
        assertTrue("Expected an exception because invalid_xml.xml is wrong", exception != null);
        
        assertTrue(invocationCounter.getInvocationCount() > 0);
        invocationCounter.reset();
        
        // Intentionally call builder again to make sure the same error is returned.
        Exception exception2 = null;
        try {
            stAXOMBuilder.next();
        } catch (Exception e) {
            exception2 = e;
        }
        
        assertEquals(0, invocationCounter.getInvocationCount());
        
        assertTrue("Expected a second exception because invalid_xml.xml is wrong", exception2 != null);
        assertTrue("Expected the same exception. first=" + exception + " second=" + exception2, 
                    exception.getMessage().equals(exception2.getMessage()));
        
    }
    
    /**
     * Test the behavior of the builder when an exception is thrown by
     * {@link XMLStreamReader#getText()}. The test is only effective if the StAX
     * implementation lazily loads the character data for a
     * {@link javax.xml.stream.XMLStreamConstants#CHARACTERS} event. This is the
     * case for Woodstox. It checks that after the exception is thrown by the
     * parser, the builder no longer attempts to access the parser.
     * 
     * @throws Exception
     */
    public void testIOExceptionInGetText() throws Exception {
        // Construct a stream that will throw an exception in the middle of a text node.
        // We need to create a very large document, because some parsers (such as some
        // versions of XLXP) have a large input buffer and would throw an exception already
        // when the XMLStreamReader is created.
        StringBuffer xml = new StringBuffer("<root>");
        for (int i=0; i<100000; i++) {
            xml.append('x');
        }
        InputStream in = new ExceptionInputStream(new ByteArrayInputStream(xml.toString().getBytes("ASCII")));
        
        XMLStreamReader originalReader = StAXUtils.createXMLStreamReader(in);
        InvocationCounter invocationCounter = new InvocationCounter();
        XMLStreamReader reader = (XMLStreamReader)invocationCounter.createProxy(originalReader);
        
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        
        try {
            while (true) {
                builder.next();
            }
        } catch (Exception ex) {
            // Expected
        }

        assertTrue(invocationCounter.getInvocationCount() > 0);
        invocationCounter.reset();

        Exception exception;
        try {
            builder.next();
            exception = null;
        } catch (Exception ex) {
            exception = ex;
        }
        if (exception == null) {
            fail("Expected exception");
        }
        
        assertEquals(0, invocationCounter.getInvocationCount());
    }
}