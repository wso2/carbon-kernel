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

package org.apache.axiom.util.stax;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.util.stax.WrappedTextNodeStreamReader;
import org.custommonkey.xmlunit.XMLTestCase;

public class WrappedTextNodeStreamReaderTest extends XMLTestCase {
    //
    // Tests that construct the Axiom tree and check the result
    //
    
    private void testUsingBuilder(QName wrapperElementName,
                                  String testString,
                                  int chunkSize) {
        StringReader reader = new StringReader(testString);
        XMLStreamReader xmlStreamReader
            = new WrappedTextNodeStreamReader(wrapperElementName, reader, chunkSize);
        OMElement element = new StAXOMBuilder(xmlStreamReader).getDocumentElement();
        assertEquals(wrapperElementName, element.getQName());
        assertEquals(wrapperElementName.getPrefix(), element.getQName().getPrefix());
        assertEquals(testString, element.getText());
    }
    
    public void testShortStringUsingBuilder() {
        testUsingBuilder(
                new QName("urn:test", "test"),
                "This is a test string for WrappedTextNodeStreamReader",
                4096);
    }
    
    public void testLongStringUsingBuilder() {
        // "Long" is relative to the chunk size
        testUsingBuilder(
                new QName("urn:test", "test"),
                "This is a test string for WrappedTextNodeStreamReader",
                10);
    }
    
    public void testWrapperElementWithoutNamespaceUsingBuilder() {
        testUsingBuilder(
                new QName("test"),
                "This is a test string for WrappedTextNodeStreamReader",
                4096);
    }
    
    public void testWrapperElementWithPrefixUsingBuilder() {
        testUsingBuilder(
                new QName("urn:test", "bar", "foo"),
                "This is a test string for WrappedTextNodeStreamReader",
                4096);
    }
    
    //
    // Test that serialize the stream of XML events to plain XML and compare
    // with the expected result.
    //
    
    private void testUsingSerializer(QName wrapperElementName,
                                     String testString,
                                     int chunkSize,
                                     String expectedXML) throws Exception {
        StringReader reader = new StringReader(testString);
        XMLStreamReader xmlStreamReader
            = new WrappedTextNodeStreamReader(wrapperElementName, reader, chunkSize);
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlStreamWriter = StAXUtils.createXMLStreamWriter(writer);
        new StreamingOMSerializer().serialize(xmlStreamReader, xmlStreamWriter);
        xmlStreamWriter.flush();
        assertXMLEqual(expectedXML, writer.toString());
    }

    public void testShortStringUsingSerializer() throws Exception {
        String testString = "This is a test string for WrappedTextNodeStreamReader";
        testUsingSerializer(
                new QName("urn:test", "test"),
                testString,
                4096,
                "<test xmlns=\"urn:test\">" + testString + "</test>");
    }

    public void testLongStringUsingSerializer() throws Exception {
        String testString = "This is a test string for WrappedTextNodeStreamReader";
        testUsingSerializer(
                new QName("urn:test", "test"),
                testString,
                10,
                "<test xmlns=\"urn:test\">" + testString + "</test>");
    }

    public void testStringWithAmpersandUsingSerializer() throws Exception {
        testUsingSerializer(
                new QName("urn:test", "test"),
                "String containing ampersand (&)",
                4096,
                "<test xmlns=\"urn:test\">String containing ampersand (&amp;)</test>");
    }
    
    //
    // Tests that construct the Axiom tree, serialize it using serializeAndConsume and
    // compare with the expected result.
    //
    
    private void testUsingSerializeAndConsume(QName wrapperElementName,
                                              String testString,
                                              int chunkSize,
                                              String expectedXML) throws Exception {
        StringReader reader = new StringReader(testString);
        XMLStreamReader xmlStreamReader
            = new WrappedTextNodeStreamReader(wrapperElementName, reader, chunkSize);
        OMElement element = new StAXOMBuilder(xmlStreamReader).getDocumentElement();
        StringWriter writer = new StringWriter();
        element.serializeAndConsume(writer);
        assertXMLEqual(expectedXML, writer.toString());
    }
    
    public void testShortStringUsingSerializeAndConsume() throws Exception {
        String testString = "This is a test string for WrappedTextNodeStreamReader";
        testUsingSerializeAndConsume(
                new QName("urn:test", "test"),
                testString,
                4096,
                "<test xmlns=\"urn:test\">" + testString + "</test>");
    }
}
