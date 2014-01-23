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
package org.apache.axiom.om.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.om.util.StAXUtils;

public class OMStAXWrapperTestBase extends TestCase {
    protected final OMMetaFactory omMetaFactory;
    
    public OMStAXWrapperTestBase(OMMetaFactory omMetaFactory) {
        this.omMetaFactory = omMetaFactory;
    }

    // Regression test for WSCOMMONS-338 and WSCOMMONS-341
    public void testCDATAEvent_FromParser() throws Exception {
        // Create an element with a CDATA section.
        InputStream is = new ByteArrayInputStream("<test><![CDATA[hello world]]></test>".getBytes());
        // Make sure that the parser is non coalescing (otherwise no CDATA events will be
        // reported). This is not the default for Woodstox (see WSTX-140).
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(StAXParserConfiguration.NON_COALESCING, is);
        
        OMElement element = omMetaFactory.createStAXOMBuilder(omMetaFactory.getOMFactory(), reader).getDocumentElement();
        
        // Build the element so we have a full StAX tree
        element.build();
        
        // Get the XMLStreamReader for the element. This will return an OMStAXWrapper.
        XMLStreamReader reader2 = element.getXMLStreamReader();
        // Check the sequence of events
        int event = reader2.next();
        assertEquals(XMLStreamReader.START_ELEMENT, event);
        
        while (reader2.hasNext() && event != XMLStreamReader.CDATA) {
           event = reader2.next();
        }
        
        // Only woodstox is guaranteed to generate CDATA events if javax.xml.stream.isCoalescing=false
        if (reader.toString().indexOf("wstx")!=-1) {
            assertEquals(XMLStreamReader.CDATA, event);
            assertEquals("hello world", reader2.getText()); // WSCOMMONS-341
            assertTrue(Arrays.equals("hello world".toCharArray(), reader2.getTextCharacters())); // WSCOMMONS-338
            assertEquals(XMLStreamReader.END_ELEMENT, reader2.next());
        }
    }
    
    public void testCDATAEvent_FromElement() throws Exception {
        OMFactory omfactory = omMetaFactory.getOMFactory();
        OMElement element = omfactory.createOMElement("test", null);
        OMText cdata = omfactory.createOMText("hello world", OMNode.CDATA_SECTION_NODE);
        element.addChild(cdata);
        
        // Get the XMLStreamReader for the element. This will return an OMStAXWrapper.
        XMLStreamReader reader2 = element.getXMLStreamReader();
        // Check the sequence of events
        int event = reader2.next();
        assertEquals(XMLStreamReader.START_ELEMENT, event);
        
        while (reader2.hasNext() && event != XMLStreamReader.CDATA) {
           event = reader2.next();
        }
        
        assertEquals(XMLStreamReader.CDATA, event);
        assertEquals("hello world", reader2.getText()); // WSCOMMONS-341
        assertTrue(Arrays.equals("hello world".toCharArray(), reader2.getTextCharacters())); // WSCOMMONS-338
        assertEquals(XMLStreamReader.END_ELEMENT, reader2.next());
    }
    
    public void testCommentEvent() throws Exception {
        OMElement element = AXIOMUtil.stringToOM(omMetaFactory.getOMFactory(),
                "<a><!--comment text--></a>");
        XMLStreamReader reader = element.getXMLStreamReader();
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.COMMENT, reader.next());
        assertEquals("comment text", reader.getText());
        assertEquals("comment text", new String(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength()));
        StringBuffer text = new StringBuffer();
        char[] buf = new char[5];
        for (int sourceStart = 0; ; sourceStart += buf.length) {
            int nCopied = reader.getTextCharacters(sourceStart, buf, 0, buf.length);
            text.append(buf, 0, nCopied);
            if (nCopied < buf.length) {
                break;
            }
        }
        assertEquals("comment text", text.toString());
        element.close(false);
    }
    
    public void testGetElementText() throws Exception {
        OMFactory factory = omMetaFactory.getOMFactory();

        OMNamespace namespace = factory.createOMNamespace("http://testuri.org", "test");
        OMElement documentElement = factory.createOMElement("DocumentElement", namespace);
        factory.createOMText(documentElement, "this is a TEXT");
        factory.createOMComment(documentElement, "this is a comment");
        factory.createOMText(documentElement, "this is a TEXT block 2");
        
        XMLStreamReader xmlStreamReader = documentElement.getXMLStreamReader();
        //move to the Start_Element
        while (xmlStreamReader.getEventType() != XMLStreamReader.START_ELEMENT) {
            xmlStreamReader.next();
        }

        String elementText = xmlStreamReader.getElementText();
        assertEquals("this is a TEXTthis is a TEXT block 2", elementText);
    }
    
    private void testNonRootElement(boolean cache) throws Exception {
        OMElement root = AXIOMUtil.stringToOM(omMetaFactory.getOMFactory(),
                "<a><b><c/></b></a>");
        OMElement child = (OMElement)root.getFirstOMChild();
        XMLStreamReader stream = cache ? child.getXMLStreamReader()
                : child.getXMLStreamReaderWithoutCaching();
        assertEquals(XMLStreamReader.START_DOCUMENT, stream.getEventType());
        assertEquals(XMLStreamReader.START_ELEMENT, stream.next());
        assertEquals("b", stream.getLocalName());
        assertEquals(XMLStreamReader.START_ELEMENT, stream.next());
        assertEquals("c", stream.getLocalName());
        assertEquals(XMLStreamReader.END_ELEMENT, stream.next());
        assertEquals(XMLStreamReader.END_ELEMENT, stream.next());
        assertEquals(XMLStreamReader.END_DOCUMENT, stream.next());
        root.close(false);
    }
    
    public void testNonRootElementWithCaching() throws Exception {
        testNonRootElement(true);
    }
    
    public void testNonRootElementWithoutCaching() throws Exception {
        testNonRootElement(false);
    }
    
    public void testNextTag() throws Exception {
        OMElement element = AXIOMUtil.stringToOM(omMetaFactory.getOMFactory(),
                "<a> <b> </b> <?pi?> <!--comment--> <c/> </a>");
        XMLStreamReader stream = element.getXMLStreamReaderWithoutCaching();
        assertEquals(XMLStreamReader.START_ELEMENT, stream.next());
        stream.nextTag();
        assertEquals(XMLStreamReader.START_ELEMENT, stream.getEventType());
        assertEquals("b", stream.getLocalName());
        stream.nextTag();
        assertEquals(XMLStreamReader.END_ELEMENT, stream.getEventType());
        stream.nextTag();
        assertEquals(XMLStreamReader.START_ELEMENT, stream.getEventType());
        assertEquals("c", stream.getLocalName());
        element.close(false);
    }
    
    private void testGetNamespaceContext(boolean cache) throws Exception {
        OMElement element = AXIOMUtil.stringToOM(omMetaFactory.getOMFactory(),
                "<a xmlns='urn:ns1' xmlns:ns2='urn:ns2'><b xmlns:ns3='urn:ns3'/></a>");
        XMLStreamReader stream = cache ? element.getXMLStreamReader()
                : element.getXMLStreamReaderWithoutCaching();
        stream.next();
        assertEquals(XMLStreamReader.START_ELEMENT, stream.next());
        assertEquals("b", stream.getLocalName());
        NamespaceContext context = stream.getNamespaceContext();
        assertEquals("urn:ns1", context.getNamespaceURI(""));
        assertEquals("urn:ns2", context.getNamespaceURI("ns2"));
        assertEquals("urn:ns3", context.getNamespaceURI("ns3"));
        assertEquals("ns2", context.getPrefix("urn:ns2"));
        element.close(false);
    }
    
    public void testGetNamespaceContextWithCaching() throws Exception {
        testGetNamespaceContext(true);
    }
    
    public void testGetNamespaceContextWithoutCaching() throws Exception {
        testGetNamespaceContext(false);
    }
}
