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

package org.apache.axis2.schema.soapencoding;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.soapencoding.Array;
import org.apache.axis2.databinding.types.soapencoding._double;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.List;

public class SoapEncodingTest extends TestCase {

    public void testSoapElement11() {

        Array array = new Array();
        array.setArrayTypeQName(new QName("http://schemas.xmlsoap.org/soap/encoding/", "double"));
        _double testDouble;
        for (int i = 0; i < 10; i++) {
            testDouble = new _double();
            testDouble.set_double(23.45);
            array.addObject(testDouble);
        }
        testSoapElement11(array);
        testSoapElement21(array);
    }

    public void testSoapElement12() {

        Array array = new Array();
        _double testDouble;
        for (int i = 0; i < 10; i++) {
            testDouble = new _double();
            testDouble.set_double(23.45);
            array.addObject(testDouble);
        }
        testSoapElement11(array);
        testSoapElement21(array);
    }

    public void testSoapElement13() {

        Array array = new Array();
        array.setArrayTypeQName(new QName("http://www.w3.org/2001/XMLSchema", "double"));
        org.apache.axis2.databinding.types.xsd._double testDouble;
        for (int i = 0; i < 10; i++) {
            testDouble = new org.apache.axis2.databinding.types.xsd._double();
            testDouble.set_double(23.45);
            array.addObject(testDouble);
        }
        testSoapElement12(array);
        testSoapElement22(array);
    }

    public void testSoapElement14() {

        Array array = new Array();
        org.apache.axis2.databinding.types.xsd._double testDouble;
        for (int i = 0; i < 10; i++) {
            testDouble = new org.apache.axis2.databinding.types.xsd._double();
            testDouble.set_double(23.45);
            array.addObject(testDouble);
        }
        testSoapElement12(array);
        testSoapElement22(array);
    }

    public void testSoapElement15() {

        Array array = new Array();
        array.setArrayTypeQName(new QName("http://apache.org/axis2/schema/soapencoding", "TestComplexType"));
        TestComplexType testComplexType;
        for (int i = 0; i < 10; i++) {
            testComplexType = new TestComplexType();
            testComplexType.setParam1("Param1");
            testComplexType.setParam2(2);
            array.addObject(testComplexType);
        }
        testSoapElement31(array);
    }

    public void testSoapElement16() {

        Array array = new Array();
        TestComplexType testComplexType;
        for (int i = 0; i < 10; i++) {
            testComplexType = new TestComplexType();
            testComplexType.setParam1("Param1");
            testComplexType.setParam2(2);
            array.addObject(testComplexType);
        }
        testSoapElement31(array);
    }

    private void testSoapElement11(Array array) {
        TestSoapElement1 testSoapElement1 = new TestSoapElement1();
        testSoapElement1.setTestSoapElement1(array);

        try {
            OMElement omElement = testSoapElement1.getOMElement(
                    TestSoapElement1.MY_QNAME, OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==>" + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            TestSoapElement1 result = TestSoapElement1.Factory.parse(xmlReader);
            List resultList = result.getTestSoapElement1().getObjectList();
            for (int i = 0; i < array.getObjectList().size(); i++) {
                assertEquals(((_double) resultList.get(i)).get_double(),
                        ((_double) array.getObjectList().get(i)).get_double(),0.001);
            }
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

     private void testSoapElement21(Array array) {
        TestSoapElement2 testSoapElement2 = new TestSoapElement2();
        testSoapElement2.setParam1("test param");
        testSoapElement2.setParam2(array);

        try {
            OMElement omElement = testSoapElement2.getOMElement(
                    TestSoapElement1.MY_QNAME, OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==>" + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            TestSoapElement2 result = TestSoapElement2.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),"test param");
            List resultList = result.getParam2().getObjectList();
            for (int i = 0; i < array.getObjectList().size(); i++) {
                assertEquals(((_double) resultList.get(i)).get_double(),
                        ((_double) array.getObjectList().get(i)).get_double(),0.001);
            }
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void testSoapElement12(Array array) {
        TestSoapElement1 testSoapElement1 = new TestSoapElement1();
        testSoapElement1.setTestSoapElement1(array);

        try {
            OMElement omElement = testSoapElement1.getOMElement(
                    TestSoapElement1.MY_QNAME, OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==>" + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            TestSoapElement1 result = TestSoapElement1.Factory.parse(xmlReader);
            List resultList = result.getTestSoapElement1().getObjectList();
            for (int i = 0; i < array.getObjectList().size(); i++) {
                assertEquals(((org.apache.axis2.databinding.types.xsd._double) resultList.get(i)).get_double(),
                        ((org.apache.axis2.databinding.types.xsd._double) array.getObjectList().get(i)).get_double(),0.001);
            }
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void testSoapElement22(Array array) {
        TestSoapElement2 testSoapElement2 = new TestSoapElement2();
        testSoapElement2.setParam1("test param");
        testSoapElement2.setParam2(array);

        try {
            OMElement omElement = testSoapElement2.getOMElement(
                    TestSoapElement1.MY_QNAME, OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==>" + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            TestSoapElement2 result = TestSoapElement2.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),"test param");
            List resultList = result.getParam2().getObjectList();
            for (int i = 0; i < array.getObjectList().size(); i++) {
                assertEquals(((org.apache.axis2.databinding.types.xsd._double) resultList.get(i)).get_double(),
                        ((org.apache.axis2.databinding.types.xsd._double) array.getObjectList().get(i)).get_double(),0.001);
            }
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void testSoapElement31(Array array) {
        TestSoapElement1 testSoapElement1 = new TestSoapElement1();
        testSoapElement1.setTestSoapElement1(array);

        try {
            OMElement omElement = testSoapElement1.getOMElement(
                    TestSoapElement1.MY_QNAME, OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==>" + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            TestSoapElement1 result = TestSoapElement1.Factory.parse(xmlReader);
            List resultList = result.getTestSoapElement1().getObjectList();
            TestComplexType testComplexType = null;
            for (int i = 0; i < resultList.size(); i++) {
                testComplexType = (TestComplexType) resultList.get(i);
                assertEquals(testComplexType.getParam1(),"Param1");
                assertEquals(testComplexType.getParam2(),2);
            }
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


}
