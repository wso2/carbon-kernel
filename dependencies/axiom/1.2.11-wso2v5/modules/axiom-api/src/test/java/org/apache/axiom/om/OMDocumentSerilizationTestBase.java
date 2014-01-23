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

import junit.framework.TestCase;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/** This tests the serializeAndConsume method */
public class OMDocumentSerilizationTestBase extends TestCase {
    private final OMMetaFactory omMetaFactory;

    private OMDocument document;
    private String xmlDeclStart = "<?xml";
    private String encoding = "encoding='UTF-8'";
    private String encoding_UTF16 = "encoding='UTF-16'";
    private String encoding2 = "encoding=\"UTF-8\"";
    private String encoding2_UTF16 = "encoding=\"UTF-16\"";
    private String version = "version='1.0'";
    private String version_11 = "version='1.1'";
    private String version2 = "version=\"1.0\"";
    private String version2_11 = "version=\"1.1\"";

    public OMDocumentSerilizationTestBase(OMMetaFactory omMetaFactory) {
        this.omMetaFactory = omMetaFactory;
    }

    public void setUp() {
        OMFactory factory = omMetaFactory.getOMFactory();

        OMNamespace namespace = factory.createOMNamespace("http://testuri.org", "test");
        OMElement documentElement = factory.createOMElement("DocumentElement", namespace);

        OMElement child1 = factory.createOMElement("Child1", namespace);
        child1.setText("TestText");
        documentElement.addChild(child1);

        document = factory.createOMDocument();
        document.addChild(documentElement);

    }

    public void testXMLDecleration() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.serializeAndConsume(baos);

        String xmlDocument = new String(baos.toByteArray());

        assertTrue("XML Declaration missing", -1 < xmlDocument.indexOf(xmlDeclStart));
    }

    public void testExcludeXMLDeclaration() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setIgnoreXMLDeclaration(true);
        document.serializeAndConsume(baos, format);

        String xmlDocument = new String(baos.toByteArray());

        assertTrue(
                "XML Declaration is included when serilizing without the declaration",
                -1 == xmlDocument.indexOf(xmlDeclStart));
    }

    public void testCharsetEncoding() throws XMLStreamException {
        // LLOM already sets the charset encoding to UTF-8, but DOOM does not
        document.setCharsetEncoding("UTF-8");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.serializeAndConsume(baos);

        String xmlDocument = new String(baos.toByteArray());

        assertTrue("Charset declaration missing", -1 < xmlDocument.indexOf(encoding) ||
                -1 < xmlDocument.indexOf(encoding.toLowerCase()) ||
                -1 < xmlDocument.indexOf(encoding2.toLowerCase()) ||
                -1 < xmlDocument.indexOf(encoding2));
    }

    public void testCharsetEncodingUTF_16()
            throws XMLStreamException, UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding("UTF-16");
        document.serializeAndConsume(baos, format);

        String xmlDocument = new String(baos.toByteArray(), "UTF-16");
        assertTrue("Charset declaration missing", -1 < xmlDocument.indexOf(encoding_UTF16) ||
                -1 < xmlDocument.indexOf(encoding2_UTF16));
    }


    public void testXMLVersion() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.serializeAndConsume(baos);

        String xmlDocument = new String(baos.toByteArray());
        assertTrue("Charset declaration missing", -1 < xmlDocument.indexOf(version) ||
                -1 < xmlDocument.indexOf(version2));
    }

    public void testXMLVersion_11() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.setXMLVersion("1.1");
        document.serializeAndConsume(baos);

        String xmlDocument = new String(baos.toByteArray());
        assertTrue("Charset declaration missing", -1 < xmlDocument.indexOf(version_11) ||
                -1 < xmlDocument.indexOf(version2_11));
    }
}
