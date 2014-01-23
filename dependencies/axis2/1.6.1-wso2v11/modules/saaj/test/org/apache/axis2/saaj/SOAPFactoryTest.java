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

package org.apache.axis2.saaj;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import java.util.Iterator;


/**
 * 
 */
@RunWith(SAAJTestRunner.class)
public class SOAPFactoryTest extends Assert {
    private static final Log log = LogFactory.getLog(SOAPFactoryTest.class);

    @Validated @Test
    public void testCreateDetail() {
        try {
            SOAPFactory sf = SOAPFactory.newInstance();
            if (sf == null) {
                fail("SOAPFactory was null");
            }
            Detail d = sf.createDetail();
            if (d == null) {
                fail("Detail was null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception " + e);
        }
    }

    @Validated @Test
    public void testCreateElement() {
        try {
            //SOAPFactory sf = SOAPFactory.newInstance();
            SOAPFactory sf = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            if (sf == null) {
                fail("createElementTest4() could not create SOAPFactory object");
            }
            //Create QName object with localName=MyName1,prefix=MyPrefix1, uri=MyUri1
            QName name = new QName("MyUri1", "MyName1", "MyPrefix1");
            SOAPElement se = sf.createElement(name);
            assertNotNull(se);
            name = se.getElementQName();
            String localName = name.getLocalPart();
            String prefix = name.getPrefix();
            String uri = name.getNamespaceURI();
            if (localName == null) {
                fail("localName is null (expected MyName1)");
            } else if (!localName.equals("MyName1")) {
                fail("localName is wrong (expected MyName1)");
            } else if (prefix == null) {
                fail("prefix is null (expected MyPrefix1)");
            } else if (!prefix.equals("MyPrefix1")) {
                fail("prefix is wrong (expected MyPrefix1)");
            } else if (uri == null) {
                fail("uri is null (expected MyUri1)");
            } else if (!uri.equals("MyUri1")) {
                fail("uri is wrong (expected MyUri1)");
            }
        }
        catch (Exception e) {
            fail();
        }
    }

    @Validated @Test
    public void testCreateElement2() {
        try {
            SOAPFactory sf = SOAPFactory.newInstance();
            //SOAPFactory sf = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            if (sf == null) {
                fail("could not create SOAPFactory object");
            }
            log.info("Create a DOMElement");
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element de = document.createElementNS("http://MyNamespace.org/", "MyTag");
            //Calling SOAPFactory.createElement(org.w3c.dom.Element)
            SOAPElement se = sf.createElement(de);
            if (!de.getNodeName().equals(se.getNodeName()) || !de.getNamespaceURI().equals(
                    se.getNamespaceURI())) {
                //Node names are not equal
                fail("Got: <URI=" + se.getNamespaceURI() + ", PREFIX=" +
                        se.getPrefix() + ", NAME=" + se.getNodeName() + ">" +
                        "Expected: <URI=" + de.getNamespaceURI() + ", PREFIX=" +
                        de.getPrefix() + ", NAME=" + de.getNodeName() + ">");
            }
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    @Validated @Test
    public void testCreateElement3() {
        try {
            SOAPFactory factory = SOAPFactory.newInstance();
            if (factory == null) {
                fail("createFaultTest1() could not create SOAPFactory object");
            }
            SOAPFault sf = factory.createFault();
            if (sf == null) {
                fail("createFault() returned null");
            } else if (!(sf instanceof SOAPFault)) {
                fail("createFault() did not create a SOAPFault object");
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Validated @Test
    public void testCreateElement4() {
        try {
            SOAPFactory sf = SOAPFactory.newInstance();
            if (sf == null) {
                fail("createElementTest6() could not create SOAPFactory object");
            }
            QName qname = new QName("http://MyNamespace.org/", "MyTag");
            SOAPElement se1 = sf.createElement(qname);
            //Create second SOAPElement from first SOAPElement
            SOAPElement se2 = sf.createElement(se1);
            //commented to support jdk 1.4 build
            //    		if(!se1.isEqualNode(se2) && !se1.isSameNode(se2)) {
            //    			fail("The SOAPElement's are not equal and not the same (unexpected)");
            //    		}
            if (!se1.getNodeName().equals(se2.getNodeName()) || !se1.getNamespaceURI().equals(
                    se2.getNamespaceURI())) {
                fail("Got: <URI=" + se1.getNamespaceURI() + ", PREFIX=" +
                        se1.getPrefix() + ", NAME=" + se1.getNodeName() + ">" +
                        "Expected: <URI=" + se2.getNamespaceURI() + ", PREFIX=" +
                        se2.getPrefix() + ", NAME=" + se2.getNodeName() + ">");
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Validated @Test
    public void testCreateFault() {
        try {
            SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            //SOAPFactory factory = SOAPFactory.newInstance();
            SOAPFault sf = factory.createFault("This is the fault reason.",
                                               SOAPConstants.SOAP_RECEIVER_FAULT);
            assertNotNull(sf);
            assertTrue(sf instanceof SOAPFault);
            QName fc = sf.getFaultCodeAsQName();
            //Expect FaultCode="+SOAPConstants.SOAP_RECEIVER_FAULT
            Iterator i = sf.getFaultReasonTexts();
            if (i == null) {
                log.info("Call to getFaultReasonTexts() returned null iterator");
            }
            String reason = "";
            while (i.hasNext()) {
                reason += (String)i.next();
            }
            assertNotNull(reason);
            assertTrue(reason.indexOf("This is the fault reason.") > -1);
            assertTrue(fc.equals(SOAPConstants.SOAP_RECEIVER_FAULT));
        } catch (SOAPException e) {
            fail("Caught unexpected SOAPException");
        }
    }

    // TODO: check why this fails with Sun's SAAJ implementation
    @Test
    public void testCreateFault1() {
        try {
            //SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPFactory factory = SOAPFactory.newInstance();
            SOAPFault sf = factory.createFault("This is the fault reason.",
                                               SOAPConstants.SOAP_RECEIVER_FAULT);
            assertNotNull(sf);
            QName fc = sf.getFaultCodeAsQName();
            Iterator i = sf.getFaultReasonTexts();

            String reason = "";
            while (i.hasNext()) {
                reason += (String)i.next();
            }
            log.info("Actual ReasonText=" + reason);
            assertNotNull(reason);
            assertTrue(reason.indexOf("This is the fault reason.") > -1);
            assertTrue(fc.equals(SOAPConstants.SOAP_RECEIVER_FAULT));
        } catch (SOAPException e) {
            //Caught expected SOAPException
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    /** for soap 1.1 */
    @Validated @Test
    public void testSOAPFaultException1() {
        try {
            SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPFault fault = factory.createFault("This is the fault reason.",
                                                  new QName("http://MyNamespaceURI.org/",
                                                            "My Fault Code"));
        } catch (UnsupportedOperationException e) {
            //Caught expected UnsupportedOperationException
        } catch (SOAPException e) {
            //Caught expected SOAPException
        } catch (IllegalArgumentException e) {
            //Caught expected IllegalArgumentException
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    /** for soap 1.2 */
    @Validated @Test
    public void testSOAPFaultException2() {
        try {
            SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPFault sf = factory.createFault("This is the fault reason.",
                                               new QName("http://MyNamespaceURI.org/",
                                                         "My Fault Code"));
            fail("Did not throw expected SOAPException");
        } catch (UnsupportedOperationException e) {
            //Caught expected UnsupportedOperationException
        } catch (SOAPException e) {
            //Caught expected SOAPException
        } catch (IllegalArgumentException e) {
            //Caught expected IllegalArgumentException
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }
}
