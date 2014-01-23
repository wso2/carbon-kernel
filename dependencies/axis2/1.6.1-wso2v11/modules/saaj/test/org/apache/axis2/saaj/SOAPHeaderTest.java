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

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;

@RunWith(SAAJTestRunner.class)
public class SOAPHeaderTest extends Assert {
    private MessageFactory mf = null;
    private SOAPMessage msg = null;
    private SOAPPart sp = null;
    private SOAPEnvelope envelope = null;
    private SOAPHeader hdr = null;
    private SOAPHeaderElement she1 = null;
    private SOAPHeaderElement she2 = null;

    @Validated @Test
    public void testAddHeaderElements() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage =
                javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPEnvelope soapEnv =
                soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();
        try {
            header.addChildElement("ebxmlms1");
        } catch (Exception e) {
            assertTrue(e instanceof SOAPException);
        }

        assertTrue(header.addChildElement("ebxmlms1", "ns-prefix",
                                          "http://test.apache.org") instanceof SOAPHeaderElement);
        ((SOAPHeaderElement)header.getFirstChild()).addTextNode("test add");


        assertTrue(header.addHeaderElement(
                soapEnv.createName("ebxmlms2", "ns-prefix", "http://test2.apache.org")) != null);
        assertTrue(header.addHeaderElement(
                new PrefixedQName("http://test3.apache.org", "ebxmlms3", "ns-prefix")) != null);


        SOAPHeaderElement firstChild = (SOAPHeaderElement)header.getFirstChild();
        assertEquals("ebxmlms1", firstChild.getLocalName());
        assertEquals("ns-prefix", firstChild.getPrefix());
        assertEquals("http://test.apache.org", firstChild.getNamespaceURI());

        SOAPHeaderElement secondChild = (SOAPHeaderElement)firstChild.getNextSibling();
        assertEquals("ebxmlms2", secondChild.getLocalName());
        assertEquals("ns-prefix", secondChild.getPrefix());
        assertEquals("http://test2.apache.org", secondChild.getNamespaceURI());

        SOAPHeaderElement lastChild = (SOAPHeaderElement)header.getLastChild();
        assertEquals("ebxmlms3", lastChild.getLocalName());
        assertEquals("ns-prefix", lastChild.getPrefix());
        assertEquals("http://test3.apache.org", lastChild.getNamespaceURI());

        SOAPHeaderElement fourthChild = (SOAPHeaderElement)lastChild.getPreviousSibling();
        assertEquals("ebxmlms2", fourthChild.getLocalName());
        assertEquals("ns-prefix", fourthChild.getPrefix());
        assertEquals("http://test2.apache.org", fourthChild.getNamespaceURI());

        Iterator it = header.getChildElements();
        int numOfHeaderElements = 0;
        while (it.hasNext()) {
            Object o = it.next();
            assertTrue(o instanceof SOAPHeaderElement);
            SOAPHeaderElement el = (SOAPHeaderElement)o;
            String lName = el.getLocalName();
            assertTrue(lName.equals("ebxmlms" + ++numOfHeaderElements));
        }
        assertEquals(3, numOfHeaderElements);
    }

    @Validated @Test
    public void testHeaders() {
        try {
            // Create message factory and SOAP factory
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPFactory soapFactory = SOAPFactory.newInstance();

            // Create a message
            SOAPMessage message = messageFactory.createMessage();

            // Get the SOAP header from the message and
            //  add headers to it
            SOAPHeader header = message.getSOAPHeader();

            String nameSpace = "ns";
            String nameSpaceURI = "http://gizmos.com/NSURI";

            Name order =
                    soapFactory.createName("orderDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement orderHeader = header.addHeaderElement(order);
            orderHeader.setActor("http://gizmos.com/orders");

            Name shipping =
                    soapFactory.createName("shippingDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement shippingHeader =
                    header.addHeaderElement(shipping);
            shippingHeader.setActor("http://gizmos.com/shipping");

            Name confirmation =
                    soapFactory.createName("confirmationDesk", nameSpace,
                                           nameSpaceURI);
            SOAPHeaderElement confirmationHeader =
                    header.addHeaderElement(confirmation);
            confirmationHeader.setActor("http://gizmos.com/confirmations");

            Name billing =
                    soapFactory.createName("billingDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement billingHeader = header.addHeaderElement(billing);
            billingHeader.setActor("http://gizmos.com/billing");

            // Add header with mustUnderstand attribute
            Name tName =
                    soapFactory.createName("Transaction", "t",
                                           "http://gizmos.com/orders");

            SOAPHeaderElement transaction = header.addHeaderElement(tName);
            transaction.setMustUnderstand(true);
            transaction.addTextNode("5");

            // Get the SOAP body from the message but leave
            // it empty
            SOAPBody body = message.getSOAPBody();

            message.saveChanges();

            // Display the message that would be sent
            //System.out.println("\n----- Request Message ----\n");
            //message.writeTo(System.out);

            // Look at the headers
            Iterator allHeaders = header.examineAllHeaderElements();

            while (allHeaders.hasNext()) {
                SOAPHeaderElement headerElement =
                        (SOAPHeaderElement)allHeaders.next();
                Name headerName = headerElement.getElementName();
                //System.out.println("\nHeader name is " +
                //                   headerName.getQualifiedName());
                //System.out.println("Actor is " + headerElement.getActor());
                //System.out.println("mustUnderstand is " +
                //                   headerElement.getMustUnderstand());
            }
        } catch (Exception e) {
            fail("Enexpected Exception " + e);
        }
    }

    @Before
    public void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
        sp = msg.getSOAPPart();
        envelope = sp.getEnvelope();
        hdr = envelope.getHeader();
    }

    @Validated @Test
    public void testExamineHeader() {
        SOAPHeaderElement she = null;

        try {
            she1 = hdr.addHeaderElement(envelope.createName("foo1", "f1", "foo1-URI"));
            she1.setActor("actor-URI");
            Iterator iterator = hdr.examineAllHeaderElements();
            int cnt = 0;
            while (iterator.hasNext()) {
                cnt++;
                she = (SOAPHeaderElement)iterator.next();
                if (!she.equals(she1)) {
                    fail("SOAP Header Elements do not match");
                }
            }

            if (cnt != 1) {
                fail("SOAPHeaderElement count mismatch: expected 1, received " + cnt);
            }

            iterator = hdr.examineAllHeaderElements();
            if (!iterator.hasNext()) {
                fail("no elements in iterator - unexpected");
            }

        } catch (Exception e) {
            fail("Unexpected Exception: " + e);
        }
    }

    @Validated @Test
    public void testAddNotUnderstoodHeaderElement() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage =
                javax.xml.soap.MessageFactory.newInstance(
                        SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

        javax.xml.soap.SOAPEnvelope soapEnv =
                soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();

        SOAPElement soapElement = header.addNotUnderstoodHeaderElement(
                new QName("http://foo.org", "foo", "f"));

        assertNotNull(soapElement);
        Name name = soapElement.getElementName();
        String uri = name.getURI();
        String localName = name.getLocalName();
        assertEquals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, uri);
        //Validate the LocalName which must be NotUnderstood
        assertEquals("NotUnderstood", localName);
    }


    @Validated @Test
    public void testAddUpgradeHeaderElement() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage =
                javax.xml.soap.MessageFactory.newInstance(
                        SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

        javax.xml.soap.SOAPEnvelope soapEnv =
                soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();

        // create a list of supported URIs.
        ArrayList supported = new ArrayList();
        supported.add(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
        supported.add(SOAPConstants.URI_NS_SOAP_ENVELOPE);

        SOAPElement soapElement = header.addUpgradeHeaderElement(supported.iterator());
        assertNotNull(soapElement);
        Name name = soapElement.getElementName();
        String uri = name.getURI();
        String localName = name.getLocalName();

        //Validate the URI which must be SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
        assertTrue(uri.equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE));
        assertTrue(localName.equals("Upgrade"));
    }

    @Validated @Test
    public void testExamineHeaderElements() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage =
                javax.xml.soap.MessageFactory.newInstance(
                        SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

        javax.xml.soap.SOAPEnvelope soapEnv =
                soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();

        SOAPHeaderElement soapHeaderElement =
                header.addHeaderElement(envelope.createName("foo1", "f1", "foo1-URI"));

        Iterator iterator = null;
        soapHeaderElement.setRole("role-URI");

        iterator = header.examineHeaderElements("role1-URI");

        int count = 0;
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        assertEquals(0, count);
    }

    @Validated @Test
    public void testExamineHeaderElements2() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage =
                javax.xml.soap.MessageFactory.newInstance().createMessage();

        javax.xml.soap.SOAPEnvelope soapEnv =
                soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();
        SOAPHeaderElement soapHeaderElement = null;

        try {
            // Add some soap header elements
            SOAPElement se = header.addHeaderElement(
                    envelope.createName("Header1", "prefix", "http://myuri"))
                    .addTextNode("This is Header1");
            soapHeaderElement = (SOAPHeaderElement)se;
            soapHeaderElement.setMustUnderstand(true);

            se = header.addHeaderElement(
                    envelope.createName("Header2", "prefix", "http://myuri"))
                    .addTextNode("This is Header2");
            soapHeaderElement = (SOAPHeaderElement)se;
            soapHeaderElement.setMustUnderstand(false);

            se = header.addHeaderElement(
                    envelope.createName("Header3", "prefix", "http://myuri"))
                    .addTextNode("This is Header3");
            soapHeaderElement = (SOAPHeaderElement)se;
            soapHeaderElement.setMustUnderstand(true);

            se = header.addHeaderElement(
                    envelope.createName("Header4", "prefix", "http://myuri"))
                    .addTextNode("This is Header4");
            soapHeaderElement = (SOAPHeaderElement)se;
            soapHeaderElement.setMustUnderstand(false);

            Iterator iterator = header.examineAllHeaderElements();

            //validating Iterator count .... should be 4");
            int cnt = 0;
            while (iterator.hasNext()) {
                cnt++;
                soapHeaderElement = (SOAPHeaderElement)iterator.next();
            }
            assertEquals(cnt, 4);
            iterator = header.examineHeaderElements(SOAPConstants.URI_SOAP_ACTOR_NEXT);
            cnt = 0;
            while (iterator.hasNext()) {
                cnt++;
                soapHeaderElement = (SOAPHeaderElement)iterator.next();
            }
            assertEquals(cnt, 0);
        } catch (Exception e) {
            fail("Unexpected Exception: " + e);
        }
    }

    @Validated @Test
    public void testQNamesOnHeader() {
        SOAPHeaderElement headerElement = null;
        try {
            //SOAP1.1 and SOAP1.2 requires all HeaderElements to be namespace qualified
            //Try adding HeaderElement with unqualified QName not belonging to any namespace
            //(expect SOAPException)
            headerElement = hdr.addHeaderElement(envelope.createName("Transaction"));
            fail("Did not throw expected SOAPException");
        } catch (SOAPException e) {
            //Did throw expected SOAPException
        } catch (Exception e) {
            fail("Unexpected Exception: " + e.getMessage());
        }
    }
    
    @Validated @Test
    public void testAppendChild() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPHeader soapHeader = message.getSOAPHeader();
        
        assertEquals(0, soapHeader.getChildNodes().getLength());
        assertFalse(soapHeader.getChildElements().hasNext());
        
        Document doc = soapHeader.getOwnerDocument();        
        String namespace = "http://example.com";
        String localName = "GetLastTradePrice";
        Element getLastTradePrice = doc.createElementNS(namespace, localName);        
        Element symbol = doc.createElement("symbol");
        symbol.setAttribute("foo", "bar");
        getLastTradePrice.appendChild(symbol);
        org.w3c.dom.Text def = doc.createTextNode("DEF");
        symbol.appendChild(def);
                        
        soapHeader.appendChild(getLastTradePrice);
                
        assertEquals(1, soapHeader.getChildNodes().getLength());
        Iterator iter = soapHeader.getChildElements();
        assertTrue(iter.hasNext()); 
        Object obj = iter.next();
        // must be SOAPHeaderElement
        assertTrue(obj instanceof SOAPHeaderElement);  
        SOAPElement soapElement = (SOAPElement)obj;
        assertEquals(namespace, soapElement.getNamespaceURI());
        assertEquals(localName, soapElement.getLocalName());
        
        iter = soapElement.getChildElements();
        assertTrue(iter.hasNext()); 
        obj = iter.next();
        assertTrue(obj instanceof SOAPElement);  
        soapElement = (SOAPElement)obj;
        assertEquals(null, soapElement.getNamespaceURI());
        assertEquals("symbol", soapElement.getLocalName());
        assertFalse(iter.hasNext());
        
        iter = soapElement.getChildElements();
        assertTrue(iter.hasNext()); 
        obj = iter.next();
        assertTrue(obj instanceof Text);  
        Text text = (Text)obj;
        assertEquals("DEF", text.getData());
        assertFalse(iter.hasNext());   
    }
}