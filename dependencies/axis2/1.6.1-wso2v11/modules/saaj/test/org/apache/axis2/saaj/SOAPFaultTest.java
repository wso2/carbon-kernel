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
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Locale;

@RunWith(SAAJTestRunner.class)
public class SOAPFaultTest extends Assert {
    public void _testSOAPFaultWithDetails() throws Exception {
        /* We are trying to generate the following SOAPFault

        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:cwmp="http://cwmp.com">
         <soapenv:Header>
          <cwmp:ID soapenv:mustUnderstand="1">HEADERID-786767comm
          8</cwmp:ID>
         </soapenv:Header>
         <soapenv:Body>
          <soapenv:Fault>
           <faultcode>Client</faultcode>
           <faultstring>CWMP fault</faultstring>
           <faultactor>http://gizmos.com/order</faultactor>
           <detail>
            <cwmp:Fault>
             <cwmp:FaultCode>This is the fault code</cwmp:FaultCode>
             <cwmp:FaultString>Fault Message</cwmp:FaultString>
             <cwmp:Message>This is a test fault</cwmp:FaultString>
            </cwmp:Fault>
           </detail>
          </soapenv:Fault>
         </soapenv:Body>
        </soapenv:Envelope>

        */

        MessageFactory fac = MessageFactory.newInstance();

        //Create the response to the message
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPHeader header = envelope.getHeader();
        Name idName = envelope.createName("ID", "cwmp", "http://cwmp.com");
        SOAPHeaderElement id = header.addHeaderElement(idName);
        id.setMustUnderstand(true);
        id.addTextNode("HEADERID-7867678");

        //Create the SOAPFault object
        SOAPFault fault = body.addFault();
        fault.setFaultCode("Client");
        fault.setFaultString("CWMP fault");
        fault.setFaultActor("http://gizmos.com/order");

        assertEquals("Client", fault.getFaultCode());
        assertEquals("CWMP fault", fault.getFaultString());
        assertEquals("http://gizmos.com/order", fault.getFaultActor());

        //Add Fault Detail information
        Detail faultDetail = fault.addDetail();
        Name cwmpFaultName = envelope.createName("Fault", "cwmp", "http://cwmp.com");
        DetailEntry faultDetailEntry = faultDetail.addDetailEntry(cwmpFaultName);
        SOAPElement e = faultDetailEntry.addChildElement("FaultCode");

        e.addTextNode("This is the fault code");
        SOAPElement e2 = faultDetailEntry.addChildElement(envelope.createName("FaultString",
                                                                              "cwmp",
                                                                              "http://cwmp.com"));
        e2.addTextNode("Fault Message");

        SOAPElement e3 = faultDetailEntry.addChildElement("Message");
        e3.addTextNode("This is a test fault");

        soapMessage.saveChanges();

        // ------------------- Validate the contents -------------------------------------
        final Detail detail = fault.getDetail();
        final Iterator detailEntryIter = detail.getDetailEntries();
        boolean foundFirst = false;
        boolean foundSecond = false;
        boolean foundThird = false;
        while (detailEntryIter.hasNext()) {
            final DetailEntry detailEntry = (DetailEntry)detailEntryIter.next();
            final Iterator childElementsIter = detailEntry.getChildElements();
            while (childElementsIter.hasNext()) {
                final SOAPElement soapElement = (SOAPElement)childElementsIter.next();
                if (soapElement.getTagName().equals("FaultCode") &&
                        soapElement.getValue().equals("This is the fault code")) {
                    foundFirst = true;
                }
                if (soapElement.getTagName().equals("cwmp:FaultString") &&
                        soapElement.getValue().equals("Fault Message")) {
                    foundSecond = true;
                }
                if (soapElement.getTagName().equals("Message") &&
                        soapElement.getValue().equals("This is a test fault")) {
                    foundThird = true;
                }
            }
        }
        assertTrue(foundFirst && foundSecond && foundThird);
        // ------------------------------------------------------------------------------

        // Test whether the fault is being serialized properly
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapMessage.writeTo(baos);
        String xml = new String(baos.toByteArray());

        assertTrue(xml.indexOf("<faultcode>Client</faultcode>") != -1);
        assertTrue(xml.indexOf("<faultstring>CWMP fault</faultstring>") != -1);
        assertTrue(xml.indexOf("<faultactor>http://gizmos.com/order</faultactor>") != -1);
    }

    @Validated @Test
    public void testAddDetailsTwice() throws Exception {
        MessageFactory fac = MessageFactory.newInstance();

        //Create the response to the message
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();

        body.addFault().addDetail();
        try {
            body.getFault().addDetail();
            fail("Expected Exception did not occur");
        } catch (SOAPException e) {
            assertTrue(true);
        }
    }

    public void _testQuick() throws Exception {
        MessageFactory msgfactory = MessageFactory.newInstance();
        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPMessage outputmsg = msgfactory.createMessage();
        String valueCode = "faultcode";
        String valueString = "faultString";
        SOAPFault fault = outputmsg.getSOAPPart().getEnvelope().getBody().addFault();
        fault.setFaultCode(valueCode);
        fault.setFaultString(valueString);
        Detail detail = fault.addDetail();
        detail.addDetailEntry(factory.createName("Hello"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (outputmsg.saveRequired()) {
            outputmsg.saveChanges();
        }
        outputmsg.writeTo(baos);
        String xml = new String(baos.toByteArray());
        assertTrue(xml.indexOf("Hello") != -1);
    }

    @Validated @Test
    public void testFaults() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPFactory soapFactory = SOAPFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();
        SOAPBody body = message.getSOAPBody();
        SOAPFault fault = body.addFault();

        Name faultName =
                soapFactory.createName("Client", "",
                                       SOAPConstants.URI_NS_SOAP_ENVELOPE);
        fault.setFaultCode(faultName);

        fault.setFaultString("Message does not have necessary info");
        fault.setFaultActor("http://gizmos.com/order");

        Detail detail = fault.addDetail();

        Name entryName =
                soapFactory.createName("order", "PO",
                                       "http://gizmos.com/orders/");
        DetailEntry entry = detail.addDetailEntry(entryName);
        entry.addTextNode("Quantity element does not have a value");

        Name entryName2 =
                soapFactory.createName("confirmation", "PO",
                                       "http://gizmos.com/confirm");
        DetailEntry entry2 = detail.addDetailEntry(entryName2);
        entry2.addTextNode("Incomplete address: " + "no zip code");

        message.saveChanges();
        //message.writeTo(System.out);

        // Now retrieve the SOAPFault object and
        // its contents, after checking to see that
        // there is one
        if (body.hasFault()) {
            SOAPFault newFault = body.getFault();

            // Get the qualified name of the fault code
            assertNotNull(newFault.getFaultCodeAsName());
            assertNotNull(newFault.getFaultString());
            assertNotNull(newFault.getFaultActor());
            Detail newDetail = newFault.getDetail();

            if (newDetail != null) {
                Iterator entries = newDetail.getDetailEntries();

                while (entries.hasNext()) {
                    DetailEntry newEntry = (DetailEntry)entries.next();
                    String value = newEntry.getValue();
                    assertNotNull(value);
                }
            }
        }
    }

    @Validated @Test
    public void testGetFaultActor() throws Exception {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPFault sf = msg.getSOAPBody().addFault();
        sf.setFaultActor("/faultActorURI");
        sf.setFaultActor("/faultActorURI2");
        String result = sf.getFaultActor();

        if (!result.equals("/faultActorURI2")) {
            fail("Fault Actor not properly set");
        }
    }

    @Validated @Test
    public void testGetFaultString() throws Exception {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPFault sf = msg.getSOAPBody().addFault();

        sf.setFaultString("1st Fault String");
        sf.setFaultString("2nd Fault String");
        String result = sf.getFaultString();

        if (!result.equals("2nd Fault String")) {
            fail("Fault String not properly set");
        }
    }

    @Validated @Test
    public void testAppendSubCode() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        QName qname = new QName("http://example.com", "myfault1", "flt1");
        soapFault.appendFaultSubcode(qname);

        QName qname2 = new QName("http://example2.com", "myfault2", "flt2");
        soapFault.appendFaultSubcode(qname2);

        QName qname3 = new QName("http://example3.com", "myfault3", "flt3");
        soapFault.appendFaultSubcode(qname3);

        soapMessage.saveChanges();

        Iterator faultSubCodes = soapFault.getFaultSubcodes();
        assertNotNull(faultSubCodes);
    }

    @Validated @Test
    public void testAppendFaultSubCode() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();

        QName expected1 = new QName("http://example.com", "myfault1", "flt1");
        QName expected2 = new QName("http://example.com", "myfault2", "flt2");
        boolean found1 = false;
        boolean found2 = false;

        //Appending fault Subcode
        sf.appendFaultSubcode(expected1);
        //Appending a second fault Subcode
        sf.appendFaultSubcode(expected2);

        //Getting FaultSubCodes from SOAPFault
        Iterator i = sf.getFaultSubcodes();
        int j = 0;
        while (i.hasNext()) {
            Object o = i.next();
            if (o != null && o instanceof QName) {
                QName actual = (QName)o;
                if (actual.equals(expected1)) {
                    if (!found1) {
                        found1 = true;
                        //System.out.println("Subcode= '"+actual+"'");
                    } else {
                        //System.out.println("Received a duplicate Subcode :'"+actual+"'");
                    }
                } else if (actual.equals(expected2)) {
                    if (!found2) {
                        found2 = true;
                        //System.out.println("Subcode= '"+actual+"'");
                    } else {
                        //System.out.println("Received a duplicate Subcode :'"+actual+"'");
                    }
                }
            }
            j++;
        }
        if (j < 1) {
            fail("No Subcode was returned");
        }
        if (j > 2) {
            fail("More than two Subcodes were returned");
        }
        if (!found1) {
            fail("The following Subcode was not received: '" + expected1 + "'");
        }
        if (!found2) {
            fail("The following Subcode was not received: '" + expected2 + "'");
        }
    }

    public void _testGetFaultReasonTexts() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        soapFault.addFaultReasonText("myReason", new Locale("en"));
        soapFault.addFaultReasonText("de-myReason", new Locale("de"));
        soapFault.addFaultReasonText("si-myReason", new Locale("si"));
        soapMessage.saveChanges();
        Iterator reasonTexts = soapFault.getFaultReasonTexts();
        while (reasonTexts.hasNext()) {
            String reasonText = (String)reasonTexts.next();
            assertNotNull(reasonText);
        }
    }

    public void _testAddFaultReasonText1() throws Exception {
        MessageFactory fac = MessageFactory.newInstance();
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        soapFault.addFaultReasonText("myReason", Locale.ENGLISH);
        soapMessage.saveChanges();
    }

    @Validated @Test
    public void testAddFaultReasonText2() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        soapFault.addFaultReasonText("myReason", new Locale("en"));
        soapFault.addFaultReasonText("de-myReason", new Locale("de"));
        soapFault.addFaultReasonText("si-myReason", new Locale("si"));
        soapMessage.saveChanges();
    }

    @Validated @Test
    public void testAddFaultReasonText3() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();
        
        String expected = "Its my fault again";
        boolean found = false;
        sf.addFaultReasonText("Its my fault", Locale.ENGLISH);
        sf.addFaultReasonText(expected, Locale.ENGLISH);
        Iterator i = sf.getFaultReasonTexts();
        int j = 0;
        while (i.hasNext()) {
            Object o = i.next();
            if (o != null && o instanceof String) {
                String actual = (String)o;
                if (actual.equals(expected)) {
                    if (!found) {
                        found = true;
                    }
                }
            }
            j++;
        }
        if (j < 1) {
            fail("No reason text was returned");
        }
        if (j > 1) {
            fail("More than one reason text was returned");
        }
        if (!found) {
            fail("The following Reason text was not received: '" + expected + "'");
        }
    }

    @Validated @Test
    public void testAddFaultReasonText4() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();
        
        String expected1 = "Its my fault";
        String expected2 = "Its my fault again";

        boolean found1 = false;
        boolean found2 = false;
        sf.addFaultReasonText(expected1, Locale.UK);
        sf.addFaultReasonText(expected2, Locale.ENGLISH);
        Iterator i = sf.getFaultReasonTexts();
        int j = 0;
        while (i.hasNext()) {
            Object o = i.next();
            if (o != null && o instanceof String) {
                String actual = (String)o;
                if (actual.equals(expected1)) {
                    if (!found1) {
                        found1 = true;
                    }
                } else if (actual.equals(expected2)) {
                    if (!found2) {
                        found2 = true;
                    }
                }
            }
            j++;
        }
        if (j < 1) {
            fail("No reason text was returned");
        }
        if (j > 2) {
            fail("More than two reason texts were returned");
        }
        if (!found1) {
            fail("The following Reason text was not received: '" + expected1 + "'");
        }
        if (!found2) {
            fail("The following Reason text was not received: '" + expected2 + "'");
        }
    }

    @Validated @Test
    public void testSetFaultRole() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        soapFault.setFaultRole("test");
        soapMessage.saveChanges();
    }

    @Validated @Test
    public void testSetFaultNode() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        soapFault.setFaultNode("test");
        soapMessage.saveChanges();
    }

    public void _testGetFaultReasonText() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        soapFault.addFaultReasonText("myReason", new Locale("en"));
        soapFault.addFaultReasonText("de-myReason", new Locale("de"));
        soapFault.addFaultReasonText("si-myReason", new Locale("si"));
        soapMessage.saveChanges();

        String faultReasonText = soapFault.getFaultReasonText(new Locale("si"));
        assertNotNull(faultReasonText);
        faultReasonText = soapFault.getFaultReasonText(new Locale("ja"));
        assertNull(faultReasonText);
    }


    public void _testGetFaultCodeAsQName() throws Exception {
        //MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        MessageFactory fac = MessageFactory.newInstance();

        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        soapFault.addFaultReasonText("myReason", new Locale("en"));
        soapFault.setFaultCode("mycode");
        soapMessage.saveChanges();

        QName qname = soapFault.getFaultCodeAsQName();
        assertNotNull(qname);
    }

    @Validated @Test
    public void testHasDetail() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        //MessageFactory fac = MessageFactory.newInstance();

        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPFault soapFault = body.addFault();
        Detail detail = soapFault.addDetail();
        detail.setAttribute("test", "myvalue");
        soapMessage.saveChanges();
    }

    @Validated @Test
    public void testFaultReasonLocales() throws Exception  {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        //SOAPFault sf = body.addFault();
        
        Locale expected1 = Locale.ENGLISH;
        Locale expected2 = Locale.UK;
        Locale expected3 = Locale.GERMAN;
        boolean found1 = false;
        boolean found2 = false;
        boolean found3 = false;

        SOAPFault sf = body.addFault(SOAPConstants.SOAP_RECEIVER_FAULT, "Its my fault1", expected1);
        sf.addFaultReasonText("Its my fault1", expected1);
        sf.addFaultReasonText("Its my fault2", expected2);
        sf.addFaultReasonText("Its my fault3", expected3);
        Iterator i = sf.getFaultReasonLocales();

        int localeCount = 0;
        while (i.hasNext()) {
            localeCount++;
            i.next();
        }

        i = sf.getFaultReasonLocales();
        int j = 0;
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Locale) {
                Locale actual = (Locale)o;
                if (actual != null) {
                    if (actual.equals(expected1)) {
                        if (!found1) {
                            found1 = true;
                        }
                    } else if (actual.equals(expected2)) {
                        if (!found2) {
                            found2 = true;
                        }
                    } else if (actual.equals(expected3)) {
                        if (!found3) {
                            found3 = true;
                        }
                    }
                }
            }
            j++;
        }
        if (j < 1) {
            fail("No reason text was returned");
        }
        if (j > 3) {
            fail("More than 3 Locales were returned");
        }
        if (!found1) {
            fail("The following Locale was not received: '" + expected1 + "'");
        }
        if (!found2) {
            fail("The following Locale was not received: '" + expected2 + "'");
        }
        if (!found3) {
            fail("The following Locale was not received: '" + expected3 + "'");
        }
    }

    @Validated @Test
    public void testFaultStringLocale() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        //MessageFactory fac = MessageFactory.newInstance();
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();

        //Setting fault string with no Locale
        sf.setFaultString("this is the fault string");
        Locale result = sf.getFaultStringLocale();
        assertNotNull(result);
    }

    @Validated @Test
    public void testFaultStringLocale2() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        //MessageFactory fac = MessageFactory.newInstance();
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();

        sf.setFaultString("this is the fault string");
        Locale result = sf.getFaultStringLocale();
        assertNotNull(result);
        assertTrue(result.equals(Locale.getDefault()));
    }

    @Validated @Test
    public void testSetFaultStringLocale() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        //MessageFactory fac = MessageFactory.newInstance();
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();

        Locale expected = Locale.ENGLISH;
        sf.setFaultString("this is the fault string", expected);
        Locale result = sf.getFaultStringLocale();
        assertNotNull(result);
        assertTrue(result.equals(expected));
    }

    // TODO: fix this test: it uses a fault code with unbound prefix
    @Test
    public void testFaultCodeWithPrefix1() throws Exception {
        MessageFactory fac = MessageFactory.newInstance();
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();

        String prefix = "wso2";
        sf.setFaultCode(prefix + ":Server");
        String result = sf.getFaultCode();

        assertNotNull(result);
        assertEquals(prefix + ":Server", result);
    }

    // TODO: fix this test: it uses a fault code with unbound prefix
    @Test
    public void testFaultCodeWithPrefix2() throws Exception {
        MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();

        String prefix = "wso2";
        sf.setFaultCode(prefix + ":Server");
        String result = sf.getFaultCode();

        assertNotNull(result);
        assertEquals(prefix + ":Server", result);
    }

    @Validated @Test
    public void testSetGetFaultCodeAsName1() throws Exception {
        SOAPFactory fac = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPFault sf = fac.createFault();

        Name name = fac.createName("myfault", "flt", "http://example.com");
        sf.setFaultCode(name);
        
        Name name2 = sf.getFaultCodeAsName();            
        assertNotNull(name2);
        assertEquals(name.getLocalName(), name2.getLocalName());
        assertEquals(name.getPrefix(), name2.getPrefix());
        assertEquals(name.getURI(), name2.getURI());
        
        QName name3 = sf.getFaultCodeAsQName();            
        assertNotNull(name3);
        assertEquals(name.getLocalName(), name3.getLocalPart());
        assertEquals(name.getPrefix(), name3.getPrefix());
        assertEquals(name.getURI(), name3.getNamespaceURI());
    }

    @Validated @Test
    public void testSetGetFaultCodeAsName2() throws Exception {
        QName qname = SOAPConstants.SOAP_SENDER_FAULT;
        SOAPFactory fac = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        Name name = fac.createName(qname.getLocalPart(),
                                   qname.getPrefix(), qname.getNamespaceURI());
        SOAPFault sf = fac.createFault();
        sf.setFaultCode(name);
        
        Name name2 = sf.getFaultCodeAsName();            
        assertNotNull(name2);
        assertEquals(name.getLocalName(), name2.getLocalName());
        assertEquals(name.getPrefix(), name2.getPrefix());
        assertEquals(name.getURI(), name2.getURI());
        
        QName name3 = sf.getFaultCodeAsQName();            
        assertNotNull(name3);
        assertEquals(name.getLocalName(), name3.getLocalPart());
        assertEquals(name.getPrefix(), name3.getPrefix());
        assertEquals(name.getURI(), name3.getNamespaceURI());
    }

    @Validated @Test
    public void testSetGetFaultCodeAsQName1() throws Exception {
        SOAPFactory fac = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPFault sf = fac.createFault();

        QName name = new QName("http://example.com", "myfault", "flt");
        sf.setFaultCode(name);
        
        QName name2 = sf.getFaultCodeAsQName();
        assertNotNull(name2);
        assertEquals(name.getLocalPart(), name2.getLocalPart());
        assertEquals(name.getPrefix(), name2.getPrefix());
        assertEquals(name.getNamespaceURI(), name2.getNamespaceURI());
        
        Name name3 = sf.getFaultCodeAsName();
        assertNotNull(name3);
        assertEquals(name.getLocalPart(), name3.getLocalName());
        assertEquals(name.getPrefix(), name3.getPrefix());
        assertEquals(name.getNamespaceURI(), name3.getURI());
    }

    @Validated @Test
    public void testSetGetFaultCodeAsQName2() throws Exception {
        QName name = SOAPConstants.SOAP_SENDER_FAULT;
        SOAPFactory fac = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPFault sf = fac.createFault();
        sf.setFaultCode(name);
        
        QName name2 = sf.getFaultCodeAsQName();
        assertNotNull(name2);
        assertEquals(name.getLocalPart(), name2.getLocalPart());
        assertEquals(name.getPrefix(), name2.getPrefix());
        assertEquals(name.getNamespaceURI(), name2.getNamespaceURI());
        
        Name name3 = sf.getFaultCodeAsName();
        assertNotNull(name3);
        assertEquals(name.getLocalPart(), name3.getLocalName());
        assertEquals(name.getPrefix(), name3.getPrefix());
        assertEquals(name.getNamespaceURI(), name3.getURI());
    }

    @Validated @Test
    public void testFault12Defaults() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = messageFactory.createMessage();
        SOAPBody body = message.getSOAPBody();
        SOAPFault fault = body.addFault();
        assertNotNull(fault.getFaultCodeAsQName());
        assertNotNull(fault.getFaultString());
    }

    @Validated @Test
    public void testFault11Defaults() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();
        SOAPBody body = message.getSOAPBody();
        SOAPFault fault = body.addFault();

        assertNotNull(fault.getFaultCodeAsQName());
        assertNotNull(fault.getFaultString());
    }


}
