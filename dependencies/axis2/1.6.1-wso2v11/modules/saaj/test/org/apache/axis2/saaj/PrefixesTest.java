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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

@RunWith(SAAJTestRunner.class)
public class PrefixesTest extends Assert {
    @Validated @Test
    public void testAddingPrefixesForChildElements() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        SOAPElement el1 = sb.addBodyElement(se.createName("element1",
                                                          "prefix1",
                                                          "http://www.sun.com"));
        el1.addChildElement(se.createName("element2",
                                          "prefix2",
                                          "http://www.apache.org"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);

        String xml = new String(baos.toByteArray());
        assertTrue(xml.indexOf("prefix1") != -1);
        assertTrue(xml.indexOf("prefix2") != -1);
        assertTrue(xml.indexOf("http://www.sun.com") != -1);
        assertTrue(xml.indexOf("http://www.apache.org") != -1);
    }

    @Validated @Test
    public void testAttribute() throws Exception {
        String soappacket =
                "<soapenv:Envelope xmlns:soapenv =\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                        "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                        "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                        "   <soapenv:Body>\n" +
                        "       <t:helloworld t:name=\"test\" xmlns:t='http://test.org/Test'>Hello</t:helloworld>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>";

        SOAPMessage msg =
                MessageFactory.newInstance().createMessage(new MimeHeaders(),
                                                           new ByteArrayInputStream(
                                                                   soappacket.getBytes()));
        SOAPBody body = msg.getSOAPPart().getEnvelope().getBody();
        validateBody(body.getChildElements());
    }

    private void validateBody(Iterator iter) {
        while (iter.hasNext()) {
            final Object obj = iter.next();
            if (obj instanceof Text) {
                //System.out.println("\n- Text Ignored.");
            } else {
                final SOAPElement soapElement = (SOAPElement)obj;
                final Iterator attIter = soapElement.getAllAttributes();
                while (attIter.hasNext()) {
                    final Name name = (Name)attIter.next();
                    assertEquals("test", soapElement.getAttributeValue(name));
                    assertEquals("t", name.getPrefix());
                    assertEquals("t:name", name.getQualifiedName());
                    assertEquals("name", name.getLocalName());
                    assertEquals("http://test.org/Test", name.getURI());
                }

                final Iterator childElementIter = soapElement.getChildElements();
                if (childElementIter == null) return;
                validateBody(childElementIter);
            }
        }
    }

    @Validated @Test
    public void testAttrPrifix() {
        try {
            MessageFactory fac = MessageFactory.newInstance();

            SOAPMessage msg = fac.createMessage();
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
            SOAPHeader header = msg.getSOAPHeader();

            Name name = env.createName("Local", "pre1", "http://test1");
            SOAPElement local = header.addChildElement(name);

            Name name2 = env.createName("Local1", "pre1", "http://test1");
            SOAPElement local2 = local.addChildElement(name2);

            Name aName = env.createName("attrib", "pre1", "http://test1");
            local2.addAttribute(aName, "value");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);

            String xml = new String(baos.toByteArray());

            assertTrue(xml.indexOf("xmlns:http://test1") == -1);
            assertTrue(xml.indexOf("pre1:attrib=\"value\"") > 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
