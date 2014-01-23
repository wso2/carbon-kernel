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

package org.apache.axis2.databinding.types.soapencoding;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.databinding.ADBException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.List;


public class ArrayTest extends TestCase {

    public void testArray1() {

        ArrayE arrayElement = new ArrayE();
        Array array = new Array();

        _int testInt;
        for (int i = 1; i < 6; i++) {
            testInt = new _int();
            testInt.set_int(i);
            array.addObject(testInt);
        }

        array.setArrayTypeQName(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/","int"));
        arrayElement.setArray(array);

        try {
            OMElement omElement = arrayElement.getOMElement(ArrayE.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            java.lang.String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            ArrayE result = ArrayE.Factory.parse(xmlReader);
            List resultList = result.getArray().getObjectList();
            for (int i = 1; i < 6; i++) {
               assertEquals(((_int)resultList.get(i-1)).get_int(),i);
            }
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }

    public void testArray2() {

        ArrayE arrayElement = new ArrayE();
        Array array = new Array();

        Date testDate;
        for (int i = 1; i < 6; i++) {
            testDate = new Date();
            testDate.setDate(new java.util.Date());
            array.addObject(testDate);
        }

        array.setArrayTypeQName(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/","date"));
        arrayElement.setArray(array);

        try {
            OMElement omElement = arrayElement.getOMElement(ArrayE.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            java.lang.String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            ArrayE result = ArrayE.Factory.parse(xmlReader);
            List resultList = result.getArray().getObjectList();
            assertEquals(resultList.size(),5);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testArray3() {

        ArrayE arrayElement = new ArrayE();
        Array array = new Array();

        _int testInt;
        for (int i = 1; i < 6; i++) {
            testInt = new _int();
            testInt.set_int(i);
            array.addObject(testInt);
        }

        arrayElement.setArray(array);

        try {
            OMElement omElement = arrayElement.getOMElement(ArrayE.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            java.lang.String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            ArrayE result = ArrayE.Factory.parse(xmlReader);
            List resultList = result.getArray().getObjectList();
            for (int i = 1; i < 6; i++) {
               assertEquals(((_int)resultList.get(i-1)).get_int(),i);
            }
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }

    public void testArray4() {

        ArrayE arrayElement = new ArrayE();
        Array array = new Array();

        Date testDate;
        for (int i = 1; i < 6; i++) {
            testDate = new Date();
            testDate.setDate(new java.util.Date());
            array.addObject(testDate);
        }

        arrayElement.setArray(array);

        try {
            OMElement omElement = arrayElement.getOMElement(ArrayE.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            java.lang.String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            ArrayE result = ArrayE.Factory.parse(xmlReader);
            List resultList = result.getArray().getObjectList();
            assertEquals(resultList.size(),5);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testArray5() {


        try {
            java.lang.String omElementString = "<ns1:Array xmlns:ns1=\"http://schemas.xmlsoap.org/soap/encoding/\" \n" +
                    "           xmlns:s1=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "           ns1:arrayType=\"s1:ur-type[5]\">\n" +
                    "    <ns1:int>1</ns1:int>\n" +
                    "    <ns1:int>2</ns1:int>\n" +
                    "    <ns1:int>3</ns1:int>\n" +
                    "    <ns1:int>4</ns1:int>\n" +
                    "    <ns1:int>5</ns1:int>\n" +
                    "</ns1:Array>";
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            ArrayE result = ArrayE.Factory.parse(xmlReader);
            List resultList = result.getArray().getObjectList();
            for (int i = 1; i < 6; i++) {
               assertEquals(((_int)resultList.get(i-1)).get_int(),i);
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

    public void testArray6() {

        ArrayE arrayElement = new ArrayE();
        Array array = new Array();
        array.addObject(null);

        _int testInt;
        for (int i = 1; i < 6; i++) {
            testInt = new _int();
            testInt.set_int(i);
            array.addObject(testInt);
        }

        array.setArrayTypeQName(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/","int"));
        arrayElement.setArray(array);

        try {
            OMElement omElement = arrayElement.getOMElement(ArrayE.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            java.lang.String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(omElementString.getBytes()));
            ArrayE result = ArrayE.Factory.parse(xmlReader);
            List resultList = result.getArray().getObjectList();
            for (int i = 1; i < 6; i++) {
               assertEquals(((_int)resultList.get(i)).get_int(),i);
            }
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }


}
