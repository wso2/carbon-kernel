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

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Test for serialization and deserialization using UTF-16 character encoding */
public class CharacterEncodingTest extends TestCase {

    public static final String UTF_16 = "utf-16";

    public CharacterEncodingTest(String name) {
        super(name);
    }

    public void runTest(String value, String expected)
            throws XMLStreamException, FactoryConfigurationError, IOException {

        SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        String ns = "http://testuri.org";
        OMNamespace namespace = factory.createOMNamespace(ns, "tst");

        String ln = "Child";

        OMElement bodyChild = factory.createOMElement(ln, namespace);
        bodyChild.addChild(factory.createOMText(value));

        envelope.getBody().addChild(bodyChild);


        ByteArrayOutputStream byteOutStr = new ByteArrayOutputStream();

        OMOutputFormat outputFormat = new OMOutputFormat();
        outputFormat.setCharSetEncoding(UTF_16);
        envelope.serialize(byteOutStr, outputFormat);

        ByteArrayInputStream byteInStr = new ByteArrayInputStream(byteOutStr.toByteArray());

        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(
                StAXUtils.createXMLStreamReader(byteInStr, UTF_16), null);

        SOAPEnvelope resultEnv = builder.getSOAPEnvelope();

        OMElement bodyChildResult = resultEnv.getBody().getFirstElement();

        assertNotNull("No child in body element", bodyChildResult);

        String result = bodyChildResult.getText();

        assertNotNull("No value for testParam param", result);

        assertEquals("Expected result not received.", expected, result);

        builder.close();
    }

    private void runtest(String value) throws Exception {
        runTest(value, value);
    }

    public void testSimpleString() throws Exception {
        runtest("a simple string");
    }

    public void testStringWithApostrophes() throws Exception {
        runtest("this isn't a simple string");
    }

    public void testStringWithEntities() throws Exception {
        runTest("&amp;&lt;&gt;&apos;&quot;", "&amp;&lt;&gt;&apos;&quot;");
    }

    public void testStringWithRawEntities() throws Exception {
        runTest("&<>'\"", "&<>'\"");
    }

    public void testStringWithLeadingAndTrailingSpaces() throws Exception {
        runtest("          centered          ");
    }

    public void testWhitespace() throws Exception {
        runtest(" \n \t "); // note: \r fails
    }

    public void testFrenchAccents() throws Exception {
        runtest("\u00e0\u00e2\u00e4\u00e7\u00e8\u00e9\u00ea\u00eb\u00ee\u00ef\u00f4\u00f6\u00f9\u00fb\u00fc");
    }

    public void testGermanUmlauts() throws Exception {
        runtest(" Some text \u00df with \u00fc special \u00f6 chars \u00e4.");
    }

    public void testWelcomeUnicode() throws Exception {
        // welcome in several languages
        runtest(
                "Chinese (trad.) : \u6b61\u8fce  ");
    }

    public void testWelcomeUnicode2() throws Exception {
        // welcome in several languages
        runtest(
                "Greek : \u03ba\u03b1\u03bb\u03ce\u03c2 \u03bf\u03c1\u03af\u03c3\u03b1\u03c4\u03b5");
    }

    public void testWelcomeUnicode3() throws Exception {
        // welcome in several languages
        runtest(
                "Japanese : \u3088\u3046\u3053\u305d");
    }

}
