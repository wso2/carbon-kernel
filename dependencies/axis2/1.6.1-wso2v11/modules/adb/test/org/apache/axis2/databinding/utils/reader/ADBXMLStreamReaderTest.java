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

package org.apache.axis2.databinding.utils.reader;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.utils.Constants;
import org.apache.axis2.util.StreamWrapper;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ADBXMLStreamReaderTest extends XMLTestCase {

    private DocumentBuilder db;

    protected void setUp() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        db = dbf.newDocumentBuilder();
    }

    /** complex array scenario */
    public void testComplexObjectArrayScenario() {
        try {
            String expectedXML =
                    "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                            "<Foo>Some Text</Foo>" +
                            "<Dependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</Dependent>" +
                            "<AdditionalDependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</AdditionalDependent>" +
                            "<AdditionalDependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</AdditionalDependent>" +
                            "<AdditionalDependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</AdditionalDependent>" +
                            "<AdditionalDependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</AdditionalDependent>" +
                            "<Bar>Some More Text</Bar><" +
                            "/ns1:TestComplexStringArrayScenario>";

            ArrayList propertyList = new ArrayList();
            propertyList.add("Foo");
            propertyList.add("Some Text");
            propertyList.add(new QName("Dependent"));
            DummyADBBean dummyBean = new DummyADBBean();
            propertyList.add(dummyBean);

            ADBBean[] adbBeans = new ADBBean[4];
            for (int i = 0; i < 4; i++) {
                adbBeans[i] = new DummyADBBean();
            }
            for (int i = 0; i < adbBeans.length; i++) {
                propertyList.add(new QName("AdditionalDependent"));
                propertyList.add(adbBeans[i]);

            }

            propertyList.add("Bar");
            propertyList.add("Some More Text");

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(new QName(
                    "http://testComplexStringArrayScenario.org", "TestComplexStringArrayScenario",
                    "ns1"), propertyList.toArray(), null);
            String actualXML = getStringXML(pullParser);


            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (Exception e) {
            fail("Error has occurred " + e);
        }
    }

    /** complex array scenario with nulls in between */
    public void testComplexObjectArrayScenarioWithNulls() {
        try {
            String expectedXML =
                    "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                            "<AdditionalDependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</AdditionalDependent>" +
                            "<AdditionalDependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</AdditionalDependent>" +
                            "<AdditionalDependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</AdditionalDependent>" +
                            "<AdditionalDependent xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                            "</AdditionalDependent>" +
                            "<Bar>Some More Text</Bar><" +
                            "/ns1:TestComplexStringArrayScenario>";

            ArrayList propertyList = new ArrayList();

            ADBBean[] adbBeans = new ADBBean[4];
            for (int i = 0; i < 4; i++) {
                adbBeans[i] = new DummyADBBean();
            }

            adbBeans[3] = null;

            for (int i = 0; i < adbBeans.length; i++) {
                propertyList.add(new QName("AdditionalDependent"));
                propertyList.add(adbBeans[i]);

            }

            propertyList.add("Bar");
            propertyList.add("Some More Text");

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(new QName(
                    "http://testComplexStringArrayScenario.org", "TestComplexStringArrayScenario",
                    "ns1"), propertyList.toArray(), null);
            String actualXML = getStringXML(pullParser);

            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (Exception e) {
            fail("Error has occurred " + e);
        }
    }

    /** Empty array */
    public void testComplexObjectArrayScenarioEmptyArray() {
        try {
            String expectedXML =
                    "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                            "<Foo>Some Text</Foo>" +
                            "<Dependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</Dependent>" +
                            "<Bar>Some More Text</Bar><" +
                            "/ns1:TestComplexStringArrayScenario>";

            ArrayList propertyList = new ArrayList();
            propertyList.add("Foo");
            propertyList.add("Some Text");
            propertyList.add(new QName("Dependent"));
            DummyADBBean dummyBean = new DummyADBBean();
            propertyList.add(dummyBean);

            String[] array = new String[] {};
            propertyList.add(new QName("AdditionalDependent"));
            propertyList.add(array);

            propertyList.add("Bar");
            propertyList.add("Some More Text");

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testComplexStringArrayScenario.org",
                              "TestComplexStringArrayScenario", "ns1"),
                    propertyList.toArray(),
                    null);
            String actualXML = getStringXML(pullParser);
            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));

        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (Exception e) {
            fail("Error has occurred " + e);
        }
    }

    /** test a complex array list */
    public void testComplexArrayList() {
        try {

            String exptectedXML = "<Person><Name>FooOne</Name><Organization>Apache</Organization>" +
                    "<Dependent><Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex><Depemdent>" +
                    "<Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex><Depemdent><Name>FooTwo</Name>" +
                    "<Age>25</Age><Sex>Male</Sex></Depemdent></Depemdent></Dependent>" +
                    "<test:Dependent xmlns:test=\"http://whatever.com\"><Name>FooTwo</Name><Age>25</Age>" +
                    "<Sex>Male</Sex><Depemdent><Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex>" +
                    "</Depemdent></test:Dependent></Person>";


            ArrayList propertyList = new ArrayList();
            propertyList.add("Name");
            propertyList.add("FooOne");

            propertyList.add("Organization");
            propertyList.add("Apache");

            propertyList.add(new QName("Dependent"));
            DummyADBBean dummyBean = new DummyADBBean();
            DummyADBBean nextdummyBean = dummyBean.addAnotherBean();
            nextdummyBean.addAnotherBean();
            propertyList.add(dummyBean);

            propertyList.add(new QName("http://whatever.com", "Dependent", "test"));
            dummyBean = new DummyADBBean();
            dummyBean.addAnotherBean();
            propertyList.add(dummyBean);

            QName projectQName = new QName("Person");
            XMLStreamReader pullParser =
                    new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

            Document actualDom = newDocument(getStringXML(pullParser));
            Document expectedDocument = newDocument(exptectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        } catch (XMLStreamException e) {
            fail("Exception in parsing documents " + e);
        }

    }

    public static class DummyADBBean implements ADBBean {
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

    public void testWithOMElements() throws XMLStreamException {

        String expectedXML =
                "<OMElementTest><axis2:FirstOMElement xmlns:axis2=\"http://ws.apache.org/namespaces/axis2\">" +
                        "<axis2:SecondOMElement></axis2:SecondOMElement></axis2:FirstOMElement><Foo>Some Text</Foo>" +
                        "<Dependent><Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex></Dependent>" +
                        "<axis2:SecondOMElement xmlns:axis2=\"http://ws.apache.org/namespaces/axis2\">" +
                        "</axis2:SecondOMElement></OMElementTest>";

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace axis2Namespace = factory.createOMNamespace(
                org.apache.axis2.Constants.AXIS2_NAMESPACE_URI,
                org.apache.axis2.Constants.AXIS2_NAMESPACE_PREFIX);
        OMElement firstElement = factory.createOMElement("FirstOMElement", axis2Namespace);
        OMElement secondElement =
                factory.createOMElement("SecondOMElement", axis2Namespace, firstElement);

        ArrayList propertyList = new ArrayList();

        // add an OMElement
        propertyList.add(firstElement.getQName());
        propertyList.add(firstElement);

        // add some more stuff
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyADBBean dummyBean = new DummyADBBean();
        propertyList.add(dummyBean);

//         lets add one more element
        propertyList.add(secondElement.getQName());
        propertyList.add(secondElement);


        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(new QName("OMElementTest"),
                                                                propertyList.toArray(), null);
        String stringXML = getStringXML(pullParser);
        try {
            Document actualDom = newDocument(stringXML);
            Document expectedDocument = newDocument(expectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        }

    }

    /** Test a completely null element */
    public void testNullableAttribute() {
        try {

            /*
            This is what I expect :

            */
            String exptectedXML =
                    "<Person xmlns=\"\"><Name xmlns=\"\">FooOne</Name><DependentOne xmlns=\"\" " +
                            "xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" +
                            "</Person>";

            ArrayList propertyList = new ArrayList();
            propertyList.add("Name");
            propertyList.add("FooOne");
            propertyList.add(new QName("DependentOne"));
            propertyList.add(null);

            QName projectQName = new QName("Person");
            XMLStreamReader pullParser =
                    new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

            Document actualDom = newDocument(getStringXML(pullParser));
            Document expectedDocument = newDocument(exptectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        } catch (XMLStreamException e) {
            fail("Exception in parsing documents " + e);
        }

    }

    /** Test a simple array */
    public void testSimpleStringArrayScenario() {
        try {
            String expectedXML =
                    "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                            "<StringInfo><array>Some Text 0</array>" +
                            "<array>Some Text 1</array>" +
                            "<array>Some Text 2</array>" +
                            "<array>Some Text 3</array></StringInfo>" +
                            "</ns1:TestComplexStringArrayScenario>";

            ArrayList propertyList = new ArrayList();

            String[] stringArray = new String[4];
            for (int i = 0; i < 4; i++) {
                stringArray[i] = "Some Text " + i;
            }
            propertyList.add("StringInfo");
            propertyList.add(stringArray);

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testComplexStringArrayScenario.org",
                              "TestComplexStringArrayScenario", "ns1"),
                    propertyList.toArray(), null);
            String actualXML = getStringXML(pullParser);


            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (XMLStreamException e) {
            fail("Error has occurred " + e);
        }


    }

    /** Test a simple array with null's inbetween */
    public void testSimpleStringArrayScenarioWithNulls() {
        try {
            String expectedXML =
                    "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                            "<StringInfo><array>Some Text 0</array>" +
                            "<array xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" +
                            "<array>Some Text 2</array>" +
                            "<array>Some Text 3</array></StringInfo>" +
                            "</ns1:TestComplexStringArrayScenario>";

            ArrayList propertyList = new ArrayList();

            String[] stringArray = new String[4];
            for (int i = 0; i < 4; i++) {
                stringArray[i] = "Some Text " + i;
            }
            stringArray[1] = null;

            propertyList.add("StringInfo");
            propertyList.add(stringArray);

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testComplexStringArrayScenario.org",
                              "TestComplexStringArrayScenario", "ns1"),
                    propertyList.toArray(), null);
            String actualXML = getStringXML(pullParser);


            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (XMLStreamException e) {
            fail("Error has occurred " + e);
        }


    }


    /** test the mixed content */
    public void testComplexStringArrayScenarioWithMixedContent() {
        try {
            String expectedXML =
                    "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                            "<Foo>Some Text</Foo>" +
                            "<Dependent>" +
                            "<Name>FooTwo</Name>" +
                            "<Age>25</Age>" +
                            "<Sex>Male</Sex>" +
                            "</Dependent>" +
                            "<StringInfo><array>Some Text 0</array>" +
                            "<array>Some Text 1</array>" +
                            "<array>Some Text 2</array>" +
                            "<array>Some Text 3</array></StringInfo>" +
                            "<Bar>Some More Text</Bar>" +
                            "</ns1:TestComplexStringArrayScenario>";

            ArrayList propertyList = new ArrayList();
            propertyList.add("Foo");
            propertyList.add("Some Text");
            propertyList.add(new QName("Dependent"));
            DummyADBBean dummyBean = new DummyADBBean();
            propertyList.add(dummyBean);

            String[] stringArray = new String[4];
            for (int i = 0; i < 4; i++) {
                stringArray[i] = "Some Text " + i;
            }
            propertyList.add("StringInfo");
            propertyList.add(stringArray);

            propertyList.add("Bar");
            propertyList.add("Some More Text");

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testComplexStringArrayScenario.org",
                              "TestComplexStringArrayScenario", "ns1"),
                    propertyList.toArray(),
                    null);
            String actualXML = getStringXML(pullParser);


            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (Exception e) {
            fail("Error has occurred " + e);
        }


    }

    /** Test a simple array with one element nil */
    public void testComplexStringArrayScenarioWithNull() {
        try {
            String expectedXML = "<ns1:TestComplexStringArrayScenario " +
                    "xmlns:ns1=\"http://testComplexStringArrayScenario.org\" " +
                    ">" +
                    "<StringInfo><array>Some Text 0</array>" +
                    "<array xsi:nil=\"true\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"></array>" +
                    "<array>Some Text 2</array>" +
                    "<array>Some Text 3</array></StringInfo>" +
                    "</ns1:TestComplexStringArrayScenario>";

            ArrayList propertyList = new ArrayList();

            String[] stringArray = new String[4];
            for (int i = 0; i < 4; i++) {
                if (i != 1) stringArray[i] = "Some Text " + i;
            }
            stringArray[1] = null;

            propertyList.add("StringInfo");
            propertyList.add(stringArray);

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testComplexStringArrayScenario.org",
                              "TestComplexStringArrayScenario", "ns1"),
                    propertyList.toArray(), null);
            String actualXML = getStringXML(pullParser);


            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (XMLStreamException e) {
            fail("Error has occurred " + e);
        }


    }

    /**
     * Test multiple unqulified attributes
     *
     * @throws XMLStreamException
     */
    public void testAttributes() throws XMLStreamException {

        String expectedXML =
                "<emp:Employee xmlns:emp=\"http://ec.org/software\" Attr2=\"Value 2\" " +
                        "Attr3=\"Value 3\" Attr1=\"Value 1\" Attr5=\"Value 5\" Attr4=\"Value 4\"></emp:Employee>";

        OMFactory factory = OMAbstractFactory.getOMFactory();
        QName elementQName = new QName("http://ec.org/software", "Employee", "emp");
        OMAttribute[] attribute = new OMAttribute[5];

        for (int i = 0; i < 5; i++) {
            attribute[i] = factory.createOMAttribute("Attr" + (i + 1), null, "Value " + (i + 1));
        }

        List omAttribList = new ArrayList();
        for (int i = 0; i < attribute.length; i++) {
            omAttribList.add(Constants.OM_ATTRIBUTE_KEY);
            omAttribList.add(attribute[i]);
        }


        String stringXML = getStringXML(new ADBXMLStreamReaderImpl(elementQName,
                                                                   null,
                                                                   omAttribList.toArray()));
        try {
            Document actualDom = newDocument(stringXML);
            Document expectedDocument = newDocument(expectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        }


    }

    /** A text only element */
    public void testElementText() {

        String expectedXML = "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\">" +
                "This is some Text for the element</ns1:testElementText>";
        try {
            ArrayList properties = new ArrayList();
            properties.add(ADBXMLStreamReader.ELEMENT_TEXT);
            properties.add("This is some Text for the element");

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testElementText.org", "testElementText", "ns1"),
                    properties.toArray(), null);

            String actualXML = getStringXML(pullParser);

            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error has occurred " + e);
        }
    }

/// todo Fails due to a bug in WSTX writer
//    /**
//     * Test multiple qualified attributes
//     * @throws XMLStreamException
//     */
//    public void testAttributesWithNamespaces() throws XMLStreamException {
//
//        String expectedXML = "<emp:Employee xmlns:emp=\"http://ec.org/software\" " +
//                "xmlns:attrNS=\"mailto:whoever@whatever.com\" attrNS:Attr2=\"Value 2\" " +
//                "attrNS:Attr3=\"Value 3\" attrNS:Attr1=\"Value 1\"\n" +
//                "              attrNS:Attr5=\"Value 5\" attrNS:Attr4=\"Value 4\"></emp:Employee>";
//
//        OMFactory factory = OMAbstractFactory.getOMFactory();
//        QName elementQName = new QName("http://ec.org/software", "Employee", "emp");
//        OMNamespace attrNS = factory.createOMNamespace("mailto:whoever@whatever.com", "attrNS");
//
//        // add some attributes with namespaces
//        OMAttribute[] attribute = new OMAttribute[5];
//        for (int i = 0; i < 5; i++) {
//            attribute[i] = factory.createOMAttribute("Attr" + (i + 1), attrNS, "Value " + (i + 1));
//        }
//
//        List omAttribList = new ArrayList();
//        for (int i = 0; i < attribute.length; i++) {
//            omAttribList.add(Constants.OM_ATTRIBUTE_KEY);
//            omAttribList.add(attribute[i]);
//        }
//        String stringXML = getStringXML(new ADBXMLStreamReaderImpl(elementQName,
//                null,
//                omAttribList.toArray()));
//        try {
//            Document actualDom = newDocument(stringXML);
//            Document expectedDocument = newDocument(expectedXML);
//            assertXMLEqual(actualDom, expectedDocument);
//        } catch (ParserConfigurationException e) {
//            fail("Exception in parsing documents " + e);
//        } catch (SAXException e) {
//            fail("Exception in parsing documents " + e);
//        } catch (IOException e) {
//            fail("Exception in parsing documents " + e);
//        }
//    }

    /** test for qualified attributes */
    public void testUnQualifiedAttributes() {

        String expectedXML =
                "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\" MyUnQualifiedAttribute=\"MyAttributeValue\">" +
                        "<ns2:QualifiedElement xmlns:ns2=\"http://testQElementText.org\">" +
                        "This is some Text for the element</ns2:QualifiedElement></ns1:testElementText>";
        try {
            ArrayList properties = new ArrayList();
            properties.add(new QName("http://testQElementText.org", "QualifiedElement", "ns2"));
            properties.add("This is some Text for the element");

            String[] attributes = new String[2];
            attributes[0] = "MyUnQualifiedAttribute";
            attributes[1] = "MyAttributeValue";


            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testElementText.org", "testElementText", "ns1"),
                    properties.toArray(),
                    attributes);

            String actualXML = getStringXML(pullParser);

            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (Exception e) {
            fail("Error has occurred " + e);
        }
    }

    /** test for base64 */
    public void testBase64EncodedText() {

        String textTobeSent = "33344MthwrrewrIOTEN)(&**^E(W)EW";

        String expectedXML = "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\">" +
                "<ns2:QualifiedElement xmlns:ns2=\"http://testQElementText.org\">" +
                Base64.encode(textTobeSent.getBytes()) +
                "</ns2:QualifiedElement></ns1:testElementText>";
        try {
            ArrayList properties = new ArrayList();
            properties.add(new QName("http://testQElementText.org", "QualifiedElement", "ns2"));
            properties.add(new DataHandler(new ByteArrayDataSource(textTobeSent.getBytes())));

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testElementText.org", "testElementText", "ns1"),
                    properties.toArray(),
                    null);

            String actualXML = getStringXML(pullParser);

            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (Exception e) {
            fail("Error has occurred " + e);
        }
    }

    /** test the qualified elements A qulified element has been associated with a namespace */
    public void testQualifiedElement() {

        String expectedXML = "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\">" +
                "<ns2:QualifiedElement xmlns:ns2=\"http://testQElementText.org\">" +
                "This is some Text for the element</ns2:QualifiedElement></ns1:testElementText>";
        try {
            ArrayList properties = new ArrayList();
            properties.add(new QName("http://testQElementText.org", "QualifiedElement", "ns2"));
            properties.add("This is some Text for the element");

            XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                    new QName("http://testElementText.org", "testElementText", "ns1"),
                    properties.toArray(),
                    null);

            String actualXML = getStringXML(pullParser);
            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        } catch (Exception e) {
            fail("Error has occurred " + e);
        }
    }

    /**
     * Util method to convert the pullstream to a string
     *
     * @param reader
     * @return
     */
    private String getStringXML(XMLStreamReader reader) throws XMLStreamException {
        //the returned pullparser starts at an Element rather than the start
        //document event. This is somewhat disturbing but since an ADBBean
        //denotes an XMLFragment, it is justifiable to keep the current event
        //at the Start-element rather than the start document
        //What it boils down to is that we need to wrap the reader in a
        //stream wrapper to get a fake start-document event

        StreamingOMSerializer ser = new StreamingOMSerializer();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(byteArrayOutputStream);
        ser.serialize(
                new StreamWrapper(reader),
                writer);
        writer.flush();
        return byteArrayOutputStream.toString();
    }

//     /**
//     * Util method to convert the pullstream to a string
//     * @param reader
//     * @return
//     */
//    private String getStringXML(XMLStreamReader reader) {
//        //the returned pullparser starts at an Element rather than the start
//        //document event. This is somewhat disturbing but since an ADBBean
//        //denotes an XMLFragment, it is justifiable to keep the current event
//        //at the Start-element rather than the start document
//        //What it boils down to is that we need to wrap the reader in a
//        //stream wrapper to get a fake start-document event
//        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(
//                new StreamWrapper(reader));
//        //stAXOMBuilder.setDoDebug(true);
//        OMElement omelement = stAXOMBuilder.getDocumentElement();
//        return omelement.toString();
//    }

    /**
     * Creates a DOM document from the string
     *
     * @param xml
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }
}
