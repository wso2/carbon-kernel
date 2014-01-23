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

package org.apache.axis2.databinding;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.databinding.utils.PrintEvents;
import org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class ADBSOAPModelBuilderTest extends XMLTestCase {
    public void testSimpleArrayList() throws Exception {
        String expectedXML = "<?xml version='1.0' encoding='utf-8'?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soapenv:Body>" +
                "<Person xmlns=\"\">" +
                "<Name xmlns=\"\">FooOne</Name>" +
                "<DependentOne xmlns=\"\"><Name xmlns=\"\">FooTwo</Name>" +
                "<Age xmlns=\"\">25</Age>" +
                "<Sex xmlns=\"\">Male</Sex></DependentOne>" +
                "<DependentTwo xmlns=\"\">" +
                "<Name xmlns=\"\">FooTwo</Name>" +
                "<Age xmlns=\"\">25</Age>" +
                "<Sex xmlns=\"\">Male</Sex></DependentTwo>" +
                "<Organization xmlns=\"\">Apache</Organization>" +
                "</Person></soapenv:Body></soapenv:Envelope>";
        ArrayList propertyList = new ArrayList();
        propertyList.add("Name");
        propertyList.add("FooOne");
        propertyList.add(new QName("DependentOne"));
        propertyList.add(new DummyADBBean());
        propertyList.add(new QName("DependentTwo"));
        propertyList.add(new DummyADBBean());
        propertyList.add("Organization");
        propertyList.add("Apache");
        QName projectQName = new QName("Person");

        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);
        ADBSOAPModelBuilder builder = new ADBSOAPModelBuilder(
                pullParser, OMAbstractFactory.getSOAP11Factory());

        OMElement root = builder.getDocumentElement();
        assertTrue("Root element can not be null", root != null);
        Document expectedDOM = newDocument(expectedXML);
        Document actualDom = newDocument(root.toString());
        assertXMLEqual(actualDom, expectedDOM);
    }

    public void testPrintEvents() throws Exception {
        XMLStreamReader r = getTestEnvelope().getXMLStreamReader();
        PrintEvents.print(r);
    }

    public void testPrintEvents2() throws Exception {
        //TODO: FIXME. Check the output difference between this method and the testPrintEvents method
        XMLStreamReader r = getTestEnvelope().getXMLStreamReaderWithoutCaching();
        PrintEvents.print(r);
    }

    public void testConvertToDOOM() throws Exception {
        String xml = "<?xml version='1.0' encoding='utf-8'?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soapenv:Body><ns1:createAccountRequest xmlns:ns1=\"http://www.wso2.com/types\">" +
                "<clientinfo xmlns=\"http://www.wso2.com/types\"><name>bob</name><ssn>123456789</ssn></clientinfo>" +
                "<password xmlns=\"\">passwd</password></ns1:createAccountRequest></soapenv:Body></soapenv:Envelope>";

        StAXSOAPModelBuilder builder2 = new StAXSOAPModelBuilder(
                getTestEnvelope().getXMLStreamReader(),
                DOOMAbstractFactory.getSOAP11Factory(),
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        SOAPEnvelope envelope = builder2.getSOAPEnvelope();
        envelope.build();

        StringWriter writer = new StringWriter();
        envelope.serialize(writer);
        writer.flush();

        String s2 = writer.toString();

        assertXMLEqual(s2, xml);
    }

    private SOAPEnvelope getTestEnvelope() {
        CreateAccountRequest request = new CreateAccountRequest();
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setName("bob");
        clientInfo.setSsn("123456789");
        request.setClientInfo(clientInfo);
        request.setPassword("passwd");

        ADBSOAPModelBuilder builder = new ADBSOAPModelBuilder(request
                .getPullParser(CreateAccountRequest.MY_QNAME),
                                                              OMAbstractFactory.getSOAP11Factory());

        return builder.getEnvelope();
    }

    public void testConvertToDOOM2() throws Exception {
        String xml =
                "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header /><soapenv:Body><ns1:createAccountRequest xmlns:ns1=\"http://www.wso2.com/types\"><ns1:clientinfo><name xmlns=\"\">bob</name><ssn xmlns=\"\">123456789</ssn></ns1:clientinfo><password xmlns=\"\">passwd</password></ns1:createAccountRequest></soapenv:Body></soapenv:Envelope>";

        CreateAccountRequest request = new CreateAccountRequest();
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setName("bob");
        clientInfo.setSsn("123456789");
        request.setClientInfo(clientInfo);
        request.setPassword("passwd");

        ADBSOAPModelBuilder builder = new ADBSOAPModelBuilder(request
                .getPullParser(CreateAccountRequest.MY_QNAME),
                                                              OMAbstractFactory.getSOAP11Factory());

        SOAPEnvelope env = builder.getEnvelope();

        StAXSOAPModelBuilder builder2 = new StAXSOAPModelBuilder(
                getTestEnvelope().getXMLStreamReaderWithoutCaching(),
                DOOMAbstractFactory.getSOAP11Factory(),
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        SOAPEnvelope envelope = builder2.getSOAPEnvelope();
        envelope.build();

        StringWriter writer = new StringWriter();
        envelope.serialize(writer);
        writer.flush();

        XMLStreamReader r = StAXUtils.createXMLStreamReader(new StringReader(writer.toString()));
        PrintEvents.print(r);

        //TODO: FIXME. Simpler test in testPrintEvents2
        //assertXMLEqual(writer.toString(),xml);
    }

    public class DummyADBBean implements ADBBean {
        ArrayList propertyList = new ArrayList();

        public DummyADBBean() {
            propertyList.add("Name");
            propertyList.add("FooTwo");
            propertyList.add("Age");
            propertyList.add("25");
            propertyList.add("Sex");
            propertyList.add("Male");
        }

        public DummyADBBean addAnotherBean() {
            propertyList.add(new QName("Depemdent"));
            DummyADBBean dummyBean = new DummyADBBean();
            propertyList.add(dummyBean);
            return dummyBean;
        }

        public XMLStreamReader getPullParser(QName adbBeanQName) {
            return new ADBXMLStreamReaderImpl(adbBeanQName, propertyList.toArray(), null);
        }

        public OMElement getOMElement(QName parentQName, OMFactory factory) throws ADBException {
            throw new UnsupportedOperationException("Unimplemented method");
        }

        public void serialize(final QName parentQName,
                              XMLStreamWriter xmlWriter)
                throws XMLStreamException, ADBException {
            serialize(parentQName,xmlWriter,false);
        }

        public void serialize(final QName parentQName,
                              XMLStreamWriter xmlWriter,
                              boolean serializeType)
                throws XMLStreamException, ADBException {
            throw new UnsupportedOperationException("Unimplemented method");
        }
    }

    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }
}
