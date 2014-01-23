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

import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

@RunWith(SAAJTestRunner.class)
public class SOAPFaultDetailTest extends Assert {
    private String xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                    " <soapenv:Body>" +
                    "  <soapenv:Fault>" +
                    "   <faultcode>soapenv:Server.generalException</faultcode>" +
                    "   <faultstring></faultstring>" +
                    "   <detail>" +
                    "    <tickerSymbol xsi:type=\"xsd:string\">MACR</tickerSymbol>" +
                    "   <ns1:exceptionName xmlns:ns1=\"http://xml.apache.org/axis2/\">test.wsdl.faults.InvalidTickerFaultMessage</ns1:exceptionName>" +
                    "   </detail>" +
                    "  </soapenv:Fault>" +
                    " </soapenv:Body>" +
                    "</soapenv:Envelope>";

    // TODO: check why this fails with Sun's SAAJ implementation
    @Test
    public void testDetails() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage smsg =
                mf.createMessage(new MimeHeaders(), new ByteArrayInputStream(xmlString.getBytes()));
        SOAPBody body = smsg.getSOAPBody();
        //smsg.writeTo(System.out);
        SOAPFault fault = body.getFault();
        fault.addDetail();
        javax.xml.soap.Detail d = fault.getDetail();
        Iterator i = d.getDetailEntries();
        while (i.hasNext()) {
            DetailEntry entry = (DetailEntry)i.next();
            String name = entry.getElementName().getLocalName();
            if ("tickerSymbol".equals(name)) {
                assertEquals("the value of the tickerSymbol element didn't match",
                             "MACR", entry.getValue());
            } else if ("exceptionName".equals(name)) {
                assertEquals("the value of the exceptionName element didn't match",
                             "test.wsdl.faults.InvalidTickerFaultMessage", entry.getValue());
            } else {
                assertTrue("Expecting details element name of 'tickerSymbol' or " +
                        "'expceptionName' - I found :" + name, false);
            }
        }
    }
}
