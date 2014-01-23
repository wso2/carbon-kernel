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
import java.util.Arrays;


public class IntElementsTest extends AbstractTest {

    public static final int MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST = 1;
    public static final int MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST = 2;
    public static final int MIN_EQUALS_ONE_NILLABLE_TRUE_TEST = 3;
    public static final int MIN_EQUALS_ONE_NILLABLE_FALSE_TEST = 4;

    public void testIntArray() {
        int[] returnObject;
        System.out.println("minOccurs = 0 nillable true");
        try {
            returnObject = testIntArray(null, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{Integer.MIN_VALUE}));
            returnObject = testIntArray(new int[]{5}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{5}));
            returnObject = testIntArray(new int[]{Integer.MIN_VALUE}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{Integer.MIN_VALUE}));
            returnObject = testIntArray(new int[]{Integer.MIN_VALUE,5}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{Integer.MIN_VALUE,5}));
        } catch (Exception e) {
            fail();
        }
        System.out.println("minOccurs = 0 nillable false");
        try {
            returnObject = testIntArray(null, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(Arrays.equals(returnObject, null));
            returnObject = testIntArray(new int[]{5}, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{5}));
            returnObject = testIntArray(new int[]{Integer.MIN_VALUE}, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(Arrays.equals(returnObject, null));
        } catch (Exception e) {
            fail();
        }
        System.out.println("minOccurs = 1 nillable true");
        try {
            returnObject = testIntArray(null, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{Integer.MIN_VALUE}));
            returnObject = testIntArray(new int[]{5}, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{5}));
            returnObject = testIntArray(new int[]{Integer.MIN_VALUE}, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{Integer.MIN_VALUE}));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        System.out.println("minOccurs = 1 nillable false");
        try {
            returnObject = testIntArray(null, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testIntArray(new int[]{Integer.MIN_VALUE}, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testIntArray(new int[]{5}, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            assertTrue(Arrays.equals(returnObject, new int[]{5}));
        } catch (Exception e) {
            fail();
        }
    }

    private int[] testIntArray(int[] innerElement, int type) throws Exception {
        OMElement omElement;
        int[] returnObject = null;
        String omElementString;
        switch (type) {
            case MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST : {
                TestInt1 testInt = new TestInt1();
                testInt.setTestValue(innerElement);
                omElement = testInt.getOMElement(TestInt1.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestInt1.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST : {
                TestInt3 testInt = new TestInt3();
                testInt.setTestValue(innerElement);
                omElement = testInt.getOMElement(TestInt3.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestInt3.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_TRUE_TEST : {
                TestInt5 testInt = new TestInt5();
                testInt.setTestValue(innerElement);
                omElement = testInt.getOMElement(TestInt5.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestInt5.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_FALSE_TEST : {
                TestInt7 testInt = new TestInt7();
                testInt.setTestValue(innerElement);
                omElement = testInt.getOMElement(TestInt7.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestInt7.Factory.parse(xmlReader).getTestValue();
                break;
            }
        }
        return returnObject;
    }


    public void testInt() {

        System.out.println("min =0 and nillable true test");
        try {
            assertEquals(testInt(1, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST), 1);
            assertEquals(testInt(Integer.MIN_VALUE, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST), Integer.MIN_VALUE);
        } catch (Exception e) {
            fail();
        }

        try {
            assertEquals(testInt(1, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST), 1);
            assertEquals(testInt(Integer.MIN_VALUE,MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST),Integer.MIN_VALUE);
        } catch (Exception e) {
            fail();
        }

        try {
            assertEquals(testInt(1, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST), 1);
            assertEquals(testInt(Integer.MIN_VALUE,MIN_EQUALS_ONE_NILLABLE_TRUE_TEST),Integer.MIN_VALUE);
        } catch (Exception e) {
            fail();
        }


        try {
            assertEquals(testInt(1, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST), 1);
        } catch (Exception e) {
            fail();
        }

        try {
            assertEquals(testInt(Integer.MIN_VALUE, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST), Integer.MIN_VALUE);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

    }

    public int testInt(int innerElement, int type) throws Exception {
        OMElement omElement;
        int returnInt = 0;
        String omElementString;

        switch (type) {
            case MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST : {
                TestInt2 testInt = new TestInt2();
                testInt.setTestValue(innerElement);
                omElement = testInt.getOMElement(TestInt2.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnInt = TestInt2.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST : {
                TestInt4 testInt = new TestInt4();
                testInt.setTestValue(innerElement);
                omElement = testInt.getOMElement(TestInt4.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnInt = TestInt4.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_TRUE_TEST : {
                TestInt6 testInt = new TestInt6();
                testInt.setTestValue(innerElement);
                omElement = testInt.getOMElement(TestInt6.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnInt = TestInt6.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_FALSE_TEST : {
                TestInt8 testInt = new TestInt8();
                testInt.setTestValue(innerElement);
                omElement = testInt.getOMElement(TestInt8.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnInt = TestInt6.Factory.parse(xmlReader).getTestValue();
                break;
            }
        }
        return returnInt;
    }
}
