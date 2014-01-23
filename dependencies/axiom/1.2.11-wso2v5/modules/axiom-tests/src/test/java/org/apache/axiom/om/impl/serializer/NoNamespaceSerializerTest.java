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

package org.apache.axiom.om.impl.serializer;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

public class NoNamespaceSerializerTest extends TestCase {

    private String xmlTextOne =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soapenv:Body>\n" +
                    "   <ns1:getBalance xmlns:ns1=\"http://localhost:8081/axis/services/BankPort/\">\n" +
                    "      <accountNo href=\"#id0\"/>\n" +
                    "   </ns1:getBalance>\n" +
                    " </soapenv:Body></soapenv:Envelope>";

    private String xmlText2 = "<purchase-order xmlns=\"http://openuri.org/easypo\">\n" +
            "  <customer>\n" +
            "    <name>Gladys Kravitz</name>\n" +
            "    <address>Anytown, PA</address>\n" +
            "  </customer>\n" +
            "  <date>2005-03-06T14:06:12.697+06:00</date>\n" +
            "</purchase-order>";

    private String xmlTextTwo =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soapenv:Body>\n" +
                    "   <getBalance xmlns=\"http://localhost:8081/axis/services/BankPort/\">\n" +
                    "      <accountNo href=\"#id0\"/>\n" +
                    "   </getBalance>\n" +
                    " </soapenv:Body></soapenv:Envelope>";

    private XMLStreamReader readerOne;
    private XMLStreamReader readerTwo;
    private XMLStreamWriter writer;

    // private OMXMLParserWrapper builder;
    // private File tempFile;

    private OMXMLParserWrapper builderOne;
    private OMXMLParserWrapper builderTwo;
    // private File tempFile;


    protected void setUp() throws Exception {
        readerOne = StAXUtils.createXMLStreamReader(
                                new InputStreamReader(
                                        new ByteArrayInputStream(xmlTextOne.getBytes())));
        readerTwo = StAXUtils.createXMLStreamReader(
                                new InputStreamReader(
                                        new ByteArrayInputStream(xmlTextTwo.getBytes())));
        writer = StAXUtils.createXMLStreamWriter(new ByteArrayOutputStream(),
                OMConstants.DEFAULT_CHAR_SET_ENCODING);
        builderOne =
                OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                        OMAbstractFactory.getSOAP11Factory(), readerOne);
        builderTwo =
                OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                        OMAbstractFactory.getSOAP11Factory(), readerTwo);
    }

    protected void tearDown() throws Exception {
        readerOne.close();
        readerTwo.close();
        writer.close();
    }

    public void testSerilizationWithDefaultNamespaces() throws Exception {
        SOAPEnvelope env = (SOAPEnvelope) builderTwo.getDocumentElement();
        env.serialize(writer);
        OMElement balanceElement = env.getBody().getFirstElement();
        assertEquals("Deafualt namespace has not been set properly",
                     balanceElement.getNamespace().getNamespaceURI(),
                     "http://localhost:8081/axis/services/BankPort/");

        OMElement accountNo = balanceElement.getFirstElement();
        assertEquals(
                "Deafualt namespace of children has not been set properly",
                accountNo.getNamespace().getNamespaceURI(),
                "http://localhost:8081/axis/services/BankPort/");

    }

    public void submitPurchaseOrderTest()
            throws Exception {
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = omFactory.getDefaultEnvelope();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXOMBuilder(
                omFactory,
                StAXUtils.createXMLStreamReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(xmlText2.getBytes()))));
        env.getBody().addChild(builder.getDocumentElement());

        // not sure why this test was created. Just checking whether serialization has worked or not. Someone
        // wanna check the correct thing later?
        String outputString = env.toString();
        assertTrue(outputString != null && !"".equals(outputString) && outputString.length() > 1);
    }

    /** Will just do a probe test to check serialize with caching on works without any exception */
    public void testSerilizationWithCacheOn() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        writer = StAXUtils.createXMLStreamWriter(byteArrayOutputStream,
                OMConstants.DEFAULT_CHAR_SET_ENCODING);

        SOAPEnvelope env = (SOAPEnvelope) builderOne.getDocumentElement();
        env.serialize(writer);
        writer.flush();
        assertTrue(new String(byteArrayOutputStream.toByteArray()).length() > 1);
    }

    /** Will just do a probe test to check serialize with caching off works without any exception */
    public void testSerilizationWithCacheOff() throws Exception {
        writer = StAXUtils.createXMLStreamWriter(new ByteArrayOutputStream(),
                OMConstants.DEFAULT_CHAR_SET_ENCODING);

        SOAPEnvelope env = (SOAPEnvelope) builderOne.getDocumentElement();
        env.serializeAndConsume(writer);
        writer.flush();
    }
}
