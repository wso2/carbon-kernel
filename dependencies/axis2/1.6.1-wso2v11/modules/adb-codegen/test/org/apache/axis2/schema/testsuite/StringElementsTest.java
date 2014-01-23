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

public class StringElementsTest extends AbstractTest {

    public static final int MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST = 1;
    public static final int MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST = 2;
    public static final int MIN_EQUALS_ONE_NILLABLE_TRUE_TEST = 3;
    public static final int MIN_EQUALS_ONE_NILLABLE_FALSE_TEST = 4;

    public void testStringArray() {
        System.out.println("Test minOccurs 0 nillable true");
        String[] returnObject = null;

        try {
            returnObject = testStringArray(null, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{null}));
            returnObject = testStringArray(new String[]{null}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{null}));
            returnObject = testStringArray(new String[]{"test"}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{"test"}));
            returnObject = testStringArray(new String[]{"test", null}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{"test", null}));
        } catch (Exception e) {
            fail();
        }

        System.out.println("Test minOccurs = 0 nillable false");
        try {
            returnObject = testStringArray(null, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, null));
            returnObject = testStringArray(new String[]{null}, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, null));
            returnObject = testStringArray(new String[]{"test"}, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{"test"}));
            returnObject = testStringArray(new String[]{"test", null}, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{"test"}));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        System.out.println("Test minOccurs = 1 nillable true");
        try {
            returnObject = testStringArray(null, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{null}));
            returnObject = testStringArray(new String[]{null}, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{null}));
            returnObject = testStringArray(new String[]{"test"}, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{"test"}));
            returnObject = testStringArray(new String[]{"test", null}, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{"test", null}));
        } catch (Exception e) {
            fail();
        }

        System.out.println("Test minOccurs = 1 nillable false");

        try {
            returnObject = testStringArray(null, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testStringArray(new String[]{null}, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testStringArray(new String[]{"test", null}, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testStringArray(new String[]{"test"}, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, new String[]{"test"}));
        } catch (Exception e) {
            fail();
        }


    }

    private String[] testStringArray(String[] innerString, int type) throws Exception {

        String[] returnObject = null;
        OMElement omElement;
        String omElementString;

        switch (type) {
            case MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST : {
                TestString1 testString = new TestString1();
                testString.setTestValue(innerString);
                omElement = testString.getOMElement(TestString1.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestString1.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST : {
                TestString3 testString = new TestString3();
                testString.setTestValue(innerString);
                omElement = testString.getOMElement(TestString3.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestString3.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_TRUE_TEST : {
                TestString5 testString = new TestString5();
                testString.setTestValue(innerString);
                omElement = testString.getOMElement(TestString5.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestString5.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_FALSE_TEST : {
                TestString7 testString = new TestString7();
                testString.setTestValue(innerString);
                omElement = testString.getOMElement(TestString7.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestString7.Factory.parse(xmlReader).getTestValue();
                break;
            }

        }
        return returnObject;
    }

    public void testString() {

        String returnObject;
        System.out.println("Test minOccurs 0 nillable true");
        try {
            returnObject = testString(null, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertEquals(returnObject, null);
            returnObject = testString("Test", MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertEquals(returnObject, "Test");
        } catch (Exception e) {
            fail();
        }

        System.out.println("Test minOccurs = 0 nillable false");
        try {
            returnObject = testString(null, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertEquals(returnObject, null);
            returnObject = testString("Test", MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertEquals(returnObject, "Test");
        } catch (Exception e) {
            fail();
        }

        System.out.println("Test minOccurs = 1 nillable true");
        try {
            returnObject = testString(null, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertEquals(returnObject, null);
            returnObject = testString("Test", MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertEquals(returnObject, "Test");
        } catch (Exception e) {
            fail();
        }
        System.out.println("Test minOccurs = 1 nillable false");
        try {
            returnObject = testString(null, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testString("Test", MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            assertEquals(returnObject, "Test");
        } catch (Exception e) {
            fail();
        }

    }

    private String testString(String innerString, int type) throws Exception {

        String returnObject = null;
        OMElement omElement;
        String omElementString;

        switch (type) {
            case MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST : {
                TestString2 testString = new TestString2();
                testString.setTestValue(innerString);
                omElement = testString.getOMElement(TestString2.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestString2.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST : {
                TestString4 testString = new TestString4();
                testString.setTestValue(innerString);
                omElement = testString.getOMElement(TestString4.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestString4.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_TRUE_TEST : {
                TestString6 testString = new TestString6();
                testString.setTestValue(innerString);
                omElement = testString.getOMElement(TestString6.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestString6.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_FALSE_TEST : {
                TestString8 testString = new TestString8();
                testString.setTestValue(innerString);
                omElement = testString.getOMElement(TestString8.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestString8.Factory.parse(xmlReader).getTestValue();
                break;
            }

        }
        return returnObject;
    }

}
