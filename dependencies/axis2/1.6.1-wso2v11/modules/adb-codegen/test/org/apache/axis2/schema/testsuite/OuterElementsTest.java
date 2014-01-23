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

package org.apache.axis2.schema.testsuite;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

public class OuterElementsTest extends AbstractTest {

    public static final int NILLABLE_TRUE = 0;
    public static final int NILLABLE_FALSE = 1;

    public void testString() {
        String returnString;
        try {
            returnString = testString(null, NILLABLE_TRUE);
            assertEquals(returnString, null);
            returnString = testString("Test String", NILLABLE_TRUE);
            assertEquals(returnString, "Test String");
        } catch (Exception e) {
            fail();
        }

        try {
            returnString = testString(null, NILLABLE_FALSE);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnString = testString("Test String", NILLABLE_FALSE);
            assertEquals(returnString, "Test String");
        } catch (Exception e) {
            fail();
        }
    }

    private String testString(String innerElement, int type) throws Exception {
        OMElement omElement;
        String returnString = null;
        String omElementString;

        switch (type) {
            case NILLABLE_TRUE : {
                OuterTestString1 outerTestString = new OuterTestString1();
                outerTestString.setOuterTestString1(innerElement);
                omElement = outerTestString.getOMElement(OuterTestString1.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnString = OuterTestString1.Factory.parse(xmlReader).getOuterTestString1();
                break;
            }

            case NILLABLE_FALSE : {
                OuterTestString2 outerTestString = new OuterTestString2();
                outerTestString.setOuterTestString2(innerElement);
                omElement = outerTestString.getOMElement(OuterTestString2.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnString = OuterTestString2.Factory.parse(xmlReader).getOuterTestString2();
                break;

            }
        }
        return returnString;
    }

    public void testInt() {

         try {
            assertEquals(testInt(5, NILLABLE_TRUE), 5);
            assertEquals(testInt(Integer.MIN_VALUE, NILLABLE_TRUE), Integer.MIN_VALUE);
        } catch (Exception e) {
            fail();
        }

        try {
            assertEquals(testInt(5, NILLABLE_FALSE), 5);
        } catch (Exception e) {
            fail();
        }

        try {
            assertEquals(testInt(Integer.MIN_VALUE, NILLABLE_FALSE), Integer.MIN_VALUE);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    private int testInt(int innerElement, int type) throws Exception {
        OMElement omElement;
        int returnInt = 0;
        String omElementString;
        switch (type) {
            case NILLABLE_TRUE : {
                OuterTestInt1 outerTestInt = new OuterTestInt1();
                outerTestInt.setOuterTestInt1(innerElement);
                omElement = outerTestInt.getOMElement(OuterTestInt1.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnInt = OuterTestInt1.Factory.parse(xmlReader).getOuterTestInt1();
                break;
            }
            case NILLABLE_FALSE : {
                OuterTestInt2 outerTestInt = new OuterTestInt2();
                outerTestInt.setOuterTestInt2(innerElement);
                omElement = outerTestInt.getOMElement(OuterTestInt2.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnInt = OuterTestInt2.Factory.parse(xmlReader).getOuterTestInt2();
                break;
            }
        }
        return returnInt;
    }

    public void testAnyType() {
        Object returnObject;
        try {
            returnObject = testAnyType(null, NILLABLE_TRUE);
            assertTrue(isObjectsEqual(returnObject, null));
            returnObject = testAnyType(new Double(23.45), NILLABLE_TRUE);
            assertTrue(isObjectsEqual(returnObject, new Double(23.45)));
        } catch (Exception e) {
            fail();
        }

        try {
            returnObject = testAnyType(null, NILLABLE_FALSE);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testAnyType(new Double(23.45), NILLABLE_FALSE);
            assertTrue(isObjectsEqual(returnObject, new Double(23.45)));
        } catch (Exception e) {
            fail();
        }
    }

    private Object testAnyType(Object innerElement, int type) throws Exception {
        OMElement omElement;
        Object returnObject = null;
        String omElementString;

        switch (type) {
            case NILLABLE_TRUE : {
                OuterTestAnyType1 outerTestAnyType = new OuterTestAnyType1();
                outerTestAnyType.setOuterTestAnyType1(innerElement);
                omElement = outerTestAnyType.getOMElement(OuterTestAnyType1.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = OuterTestAnyType1.Factory.parse(xmlReader).getOuterTestAnyType1();
                break;
            }

            case NILLABLE_FALSE : {
                OuterTestAnyType2 outerTestAnyType = new OuterTestAnyType2();
                outerTestAnyType.setOuterTestAnyType2(innerElement);
                omElement = outerTestAnyType.getOMElement(OuterTestAnyType2.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = OuterTestAnyType2.Factory.parse(xmlReader).getOuterTestAnyType2();
                break;
            }
        }
        return returnObject;
    }

    public void testBookInformation() {
        BookInformation returnObject;
        try {
            returnObject = testBookInformation(null, NILLABLE_TRUE);
            assertTrue(isBookInformationObjectsEquals(returnObject, null));
            returnObject = testBookInformation(getBookInformation(), NILLABLE_TRUE);
            assertTrue(isBookInformationObjectsEquals(returnObject, getBookInformation()));
        } catch (Exception e) {
            fail();
        }

        try {
            returnObject = testBookInformation(null, NILLABLE_FALSE);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testBookInformation(getBookInformation(), NILLABLE_FALSE);
            assertTrue(isBookInformationObjectsEquals(returnObject, getBookInformation()));
        } catch (Exception e) {
            fail();
        }
    }

    private BookInformation testBookInformation(BookInformation innerElement, int type) throws Exception {
        OMElement omElement;
        BookInformation returnObject = null;
        String omElementString;

        switch (type) {
            case NILLABLE_TRUE : {
                OuterTestBookInformation1 outerTestBookInformation = new OuterTestBookInformation1();
                outerTestBookInformation.setOuterTestBookInformation1(innerElement);
                omElement = outerTestBookInformation.getOMElement(OuterTestBookInformation1.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = OuterTestBookInformation1.Factory.parse(xmlReader).getOuterTestBookInformation1();
                break;
            }

            case NILLABLE_FALSE : {
                OuterTestBookInformation2 outerTestBookInformation = new OuterTestBookInformation2();
                outerTestBookInformation.setOuterTestBookInformation2(innerElement);
                omElement = outerTestBookInformation.getOMElement(OuterTestBookInformation2.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = OuterTestBookInformation2.Factory.parse(xmlReader).getOuterTestBookInformation2();
                break;
            }
        }
        return returnObject;
    }


}
