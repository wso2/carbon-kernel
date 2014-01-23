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

package org.apache.axiom.om;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

public class StaxParserTest extends AbstractTestCase {

    XMLStreamReader parser1;
    OMXMLParserWrapper builder2;
    XMLStreamReader parser2;
    OMXMLParserWrapper builder3;
    XMLStreamReader parser3;
    XMLStreamReader parser4;
    String xmlDocument = "<purchase-order xmlns=\"http://openuri.org/easypo\">" +
            "<customer>" +
            "    <name>Gladys Kravitz</name>" +
            "    <address>Anytown, PA</address>" +
            "  </customer>" +
            "  <date>2005-03-06T14:06:12.697+06:00</date>" +
            "</purchase-order>";

    public StaxParserTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        //make the parsers
        //Parser 1 is a plain parser from the stax implementation
        parser1 = StAXUtils.createXMLStreamReader(
                        new ByteArrayInputStream(xmlDocument.getBytes()));

        //parser 2 is one of our parsers taken with cache. i.e. when the parser
        //proceeds the object model will be built
        builder2 = OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getSOAP11Factory(),
                StAXUtils.createXMLStreamReader(
                        new ByteArrayInputStream(xmlDocument.getBytes())));
        parser2 = builder2.getDocumentElement().getXMLStreamReader();

        //same as parser2 but this time the parser is not a caching parser. Once the
        //parser proceeds, it's gone forever.
        builder3 = OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getSOAP11Factory(),
                StAXUtils.createXMLStreamReader(
                        new ByteArrayInputStream(xmlDocument.getBytes())));
        parser3 =
                builder3.getDocumentElement().getXMLStreamReaderWithoutCaching();

        //parser4 is another instance of our parser accessing the same stream as parser3.
        // Note - The implementation returns a *new* instance but with reference to
        //the same underlying stream!
        parser4 = builder2.getDocumentElement().getXMLStreamReaderWithoutCaching();

    }

    protected void tearDown() throws Exception {
        parser1.close();
        ((StAXOMBuilder)builder2).close();
        ((StAXOMBuilder)builder3).close();
    }

    public void testParserEventsWithCache() throws Exception {

        //check the initial event
        assertEquals(parser1.getEventType(), parser2.getEventType());

        //check the other events
        while (parser1.hasNext()) {

            int parser1Event = parser1.next();
            int parser2Event = parser2.next();
            assertEquals(parser1Event, parser2Event);

        }


    }

    public void testParserEventsWithoutCache() throws Exception {

        assertEquals(parser1.getEventType(), parser3.getEventType());

        while (parser1.hasNext()) {
            int parser1Event = parser1.next();
            int parser2Event = parser3.next();
            assertEquals(parser1Event, parser2Event);
        }


    }

    public void testParserEvents2WithCache() throws Exception {
        while (parser1.hasNext()) {
            int parser1Event = parser1.getEventType();
            int parser2Event = parser2.getEventType();
            parser1.next();
            parser2.next();
            assertEquals(parser1Event, parser2Event);
        }


    }


    public void testParserBehaviornonCaching() throws Exception {

        OMXMLParserWrapper builder2 = OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getOMFactory(),
                StAXUtils.createXMLStreamReader(
                        new ByteArrayInputStream(xmlDocument.getBytes())));

        OMElement documentElement = builder2.getDocumentElement();
        XMLStreamReader originalParser =
                documentElement.getXMLStreamReaderWithoutCaching();

        //consume the parser. this should force the xml stream to be exhausted without
        //building the tree
        while (originalParser.hasNext()) {
            originalParser.next();
        }

        //try to find the children of the document element. This should produce an
        //error since the underlying stream is fully consumed without building the object tree
        Iterator childElements = documentElement.getChildElements();
        try {
            while (childElements.hasNext()) {
                childElements.next();
                fail("The stream should've been consumed by now!");
            }
        } catch (Exception e) {
            //if we are here without failing, then we are successful
        }
        
        documentElement.close(false);
    }


    public void testParserBehaviorCaching() throws Exception {

        OMXMLParserWrapper builder2 = OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getSOAP11Factory(),
                StAXUtils.createXMLStreamReader(
                        new ByteArrayInputStream(xmlDocument.getBytes())));

        OMElement documentElement = builder2.getDocumentElement();
        XMLStreamReader originalParser =
                documentElement.getXMLStreamReader();

        //consume the parser. this should force the xml stream to be exhausted but the
        //tree to be fully built
        while (originalParser.hasNext()) {
            originalParser.next();
        }

        //try to find the children of the document element. This should *NOT* produce an
        //error even when the underlying stream is fully consumed , the object tree is already complete
        Iterator childElements = documentElement.getChildElements();
        int count = 0;
        try {
            while (childElements.hasNext()) {
                childElements.next();
                count++;
            }
        } catch (Exception e) {
            fail("The object tree needs to be built and traversing the children is to be a success!");
        }

        assertEquals("Number of elements need to be 2", count, 2);
        
        documentElement.close(false);
    }


    public void testParserBehaviorNonCaching2() throws Exception {

        OMXMLParserWrapper builder2 = OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getSOAP11Factory(),
                StAXUtils.createXMLStreamReader(
                        new ByteArrayInputStream(xmlDocument.getBytes())));

        OMElement documentElement = builder2.getDocumentElement();

        XMLStreamReader originalParser =
                documentElement.getXMLStreamReaderWithoutCaching();

        //consume the parser. this should force the xml stream to be exhausted without
        //building the tree
        while (originalParser.hasNext()) {
            originalParser.next();
        }

        //try to find the children of the document element. This should produce an
        //error since the underlying stream is fully consumed without building the object tree
        Iterator childElements = documentElement.getChildElements();
        try {
            XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(System.out);
            documentElement.serializeAndConsume(writer);
            fail("Stream should be consumed by now");
        } catch (XMLStreamException e) {
            //wea re cool
        } catch (Exception e) {
            fail("This should throw an XMLStreamException");
        }
        
        documentElement.close(false);
    }

}

