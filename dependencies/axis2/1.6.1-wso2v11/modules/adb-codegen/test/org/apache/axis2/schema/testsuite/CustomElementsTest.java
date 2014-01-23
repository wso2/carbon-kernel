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


public class CustomElementsTest extends AbstractTest {

    public static final int MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST = 1;
    public static final int MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST = 2;
    public static final int MIN_EQUALS_ONE_NILLABLE_TRUE_TEST = 3;
    public static final int MIN_EQUALS_ONE_NILLABLE_FALSE_TEST = 4;

    public void testCustomArray() {

        BookInformation[] returnObject;
        System.out.println("minOccurs = 0 and nillable true");
        try {
            returnObject = testCustomArray(null, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{null}));
            returnObject = testCustomArray(new BookInformation[]{null}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{null}));
            returnObject = testCustomArray(new BookInformation[]{getBookInformation()}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{getBookInformation()}));
            returnObject = testCustomArray(new BookInformation[]{getBookInformation(), null}, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{getBookInformation(), null}));
        } catch (Exception e) {
            fail();
        }
        System.out.println("minOccurs = 0 and nillable false");
        try {
            returnObject = testCustomArray(null, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, null));
            returnObject = testCustomArray(new BookInformation[]{null}, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, null));
            returnObject = testCustomArray(new BookInformation[]{getBookInformation()}, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{getBookInformation()}));
            returnObject = testCustomArray(new BookInformation[]{getBookInformation(), null}, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{getBookInformation()}));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        System.out.println("minOccurs = 1 and nillable true");
        try {
            returnObject = testCustomArray(null, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{null}));
            returnObject = testCustomArray(new BookInformation[]{null}, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{null}));
            returnObject = testCustomArray(new BookInformation[]{getBookInformation()}, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{getBookInformation()}));
            returnObject = testCustomArray(new BookInformation[]{getBookInformation(), null}, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{getBookInformation(), null}));
        } catch (Exception e) {
            fail();
        }

        System.out.println("minOccurs = 1 and nillable false");
        try {
            returnObject = testCustomArray(null, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testCustomArray(new BookInformation[]{null}, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testCustomArray(new BookInformation[]{getBookInformation(), null}, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testCustomArray(new BookInformation[]{getBookInformation()}, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            assertTrue(assertArrayEqual(returnObject, new BookInformation[]{getBookInformation()}));
        } catch (Exception e) {
            fail();
        }


    }

    private BookInformation[] testCustomArray(BookInformation[] innerElement, int type) throws Exception {
        OMElement omElement;
        BookInformation[] returnObject = null;
        String omElementString;
        switch (type) {
            case MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST : {
                TestBookInformation1 testBookInformation = new TestBookInformation1();
                testBookInformation.setTestValue(innerElement);
                omElement = testBookInformation.getOMElement(TestBookInformation1.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestBookInformation1.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST : {
                TestBookInformation3 testBookInformation = new TestBookInformation3();
                testBookInformation.setTestValue(innerElement);
                omElement = testBookInformation.getOMElement(TestBookInformation3.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestBookInformation3.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_TRUE_TEST : {
                TestBookInformation5 testBookInformation = new TestBookInformation5();
                testBookInformation.setTestValue(innerElement);
                omElement = testBookInformation.getOMElement(TestBookInformation5.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestBookInformation5.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_FALSE_TEST : {
                TestBookInformation7 testBookInformation = new TestBookInformation7();
                testBookInformation.setTestValue(innerElement);
                omElement = testBookInformation.getOMElement(TestBookInformation7.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestBookInformation7.Factory.parse(xmlReader).getTestValue();
                break;
            }
        }
        return returnObject;
    }

    public void testCustom() {
        BookInformation returnObject;
        System.out.println("minOccurs = 0 and nillable true");
        try {
            returnObject = testCustom(null, MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(isBookInformationObjectsEquals(returnObject, null));
            returnObject = testCustom(getBookInformation(), MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST);
            assertTrue(isBookInformationObjectsEquals(returnObject, getBookInformation()));
        } catch (Exception e) {
            fail();
        }
        System.out.println("minOccurs = 0 and nillable false");
        try {
            returnObject = testCustom(null, MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(isBookInformationObjectsEquals(returnObject, null));
            returnObject = testCustom(getBookInformation(), MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST);
            assertTrue(isBookInformationObjectsEquals(returnObject, getBookInformation()));
        } catch (Exception e) {
            fail();
        }
        System.out.println("minOccurs = 1 and nillable true");
        try {
            returnObject = testCustom(null, MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(isBookInformationObjectsEquals(returnObject, null));
            returnObject = testCustom(getBookInformation(), MIN_EQUALS_ONE_NILLABLE_TRUE_TEST);
            assertTrue(isBookInformationObjectsEquals(returnObject, getBookInformation()));
        } catch (Exception e) {
            fail();
        }
        System.out.println("minOccurs = 1 and nillable false");
        try {
            returnObject = testCustom(null, MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        try {
            returnObject = testCustom(getBookInformation(), MIN_EQUALS_ONE_NILLABLE_FALSE_TEST);
            assertTrue(isBookInformationObjectsEquals(returnObject, getBookInformation()));
        } catch (Exception e) {
            fail();
        }

    }

    private BookInformation testCustom(BookInformation innerElement, int type) throws Exception {
        OMElement omElement;
        BookInformation returnObject = null;
        String omElementString;
        switch (type) {
            case MIN_EQUALS_ZERO_NILLABLE_TRUE_TEST : {
                TestBookInformation2 testBookInformation = new TestBookInformation2();
                testBookInformation.setTestValue(innerElement);
                omElement = testBookInformation.getOMElement(TestBookInformation2.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestBookInformation2.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ZERO_NILLABLE_FALSE_TEST : {
                TestBookInformation4 testBookInformation = new TestBookInformation4();
                testBookInformation.setTestValue(innerElement);
                omElement = testBookInformation.getOMElement(TestBookInformation4.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestBookInformation4.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_TRUE_TEST : {
                TestBookInformation6 testBookInformation = new TestBookInformation6();
                testBookInformation.setTestValue(innerElement);
                omElement = testBookInformation.getOMElement(TestBookInformation6.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestBookInformation6.Factory.parse(xmlReader).getTestValue();
                break;
            }
            case MIN_EQUALS_ONE_NILLABLE_FALSE_TEST : {
                TestBookInformation8 testBookInformation = new TestBookInformation8();
                testBookInformation.setTestValue(innerElement);
                omElement = testBookInformation.getOMElement(TestBookInformation8.MY_QNAME, OMAbstractFactory.getSOAP12Factory());
                omElementString = omElement.toStringWithConsume();
                System.out.println("OMElement ==> " + omElementString);
                XMLStreamReader xmlReader =
                        StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
                returnObject = TestBookInformation8.Factory.parse(xmlReader).getTestValue();
                break;
            }
        }
        return returnObject;
    }


}
