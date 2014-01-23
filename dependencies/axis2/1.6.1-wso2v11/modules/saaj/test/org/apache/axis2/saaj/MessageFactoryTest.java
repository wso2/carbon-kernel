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

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 
 */
@RunWith(SAAJTestRunner.class)
public class MessageFactoryTest extends XMLAssert {
    private MessageFactory mf = null;

    @Before
    public void setUp() throws Exception {
        mf = MessageFactory.newInstance();
    }

    @Validated @Test
    public void testCreateMessage() {
        try {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

            SOAPMessage msg1 = mf.createMessage();
            msg1.writeTo(baos1);

            MimeHeaders headers = new MimeHeaders();
            headers.addHeader("Content-Type", "text/xml");

            // Create SOAPMessage from MessageFactory object using InputStream
            SOAPMessage msg2 = mf.createMessage(headers,
                                                new ByteArrayInputStream(
                                                        baos1.toString().getBytes()));
            if (msg2 == null) {
                fail();
            }
            msg2.writeTo(baos2);

            if (!(baos1.toString().equals(baos2.toString()))) {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Validated @Test
    public void testCreateMessage2() {
        try {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

            final String XML_STRING =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                            "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                            " <soapenv:Header>\n" +
                            "  <shw:Hello xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
                            "    <shw:Myname>Tony</shw:Myname>\n" +
                            "  </shw:Hello>\n" +
                            " </soapenv:Header>\n" +
                            " <soapenv:Body>\n" +
                            "<shw:Address shw:t='test' xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
                            "<shw:City>GENT</shw:City>\n" +
                            "</shw:Address>\n" +
                            "</soapenv:Body>\n" +
                            "</soapenv:Envelope>";

            MimeHeaders headers = new MimeHeaders();
            headers.addHeader("Content-Type", "text/xml");

            SOAPMessage msg1 =
                    mf.createMessage(headers, new ByteArrayInputStream(XML_STRING.getBytes()));
            msg1.writeTo(baos1);

            // Create SOAPMessage from MessageFactory object using InputStream
            SOAPMessage msg2 = mf.createMessage(headers,
                                                new ByteArrayInputStream(
                                                        baos1.toString().getBytes()));
            if (msg2 == null) {
                fail();
            }
            msg2.writeTo(baos2);

            this.assertXMLEqual(baos1.toString(), baos2.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Validated @Test
    public void testNewInstane() {
        try {
            MessageFactory mf = MessageFactory.newInstance();
            assertNotNull(mf);
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();

            SOAPMessage msg1 = mf.createMessage();
            msg1.writeTo(baos1);

            MimeHeaders headers = new MimeHeaders();
            headers.addHeader("Content-Type", "text/xml");

        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    @Validated @Test
    public void testParseMTOMMessage() throws Exception {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", TestUtils.MTOM_TEST_MESSAGE_CONTENT_TYPE);
        InputStream in = TestUtils.getTestFile(TestUtils.MTOM_TEST_MESSAGE_FILE);
        SOAPMessage message = mf.createMessage(headers, in);
        SOAPPart soapPart = message.getSOAPPart();
        assertEquals("<0.urn:uuid:F02ECC18873CFB73E211412748909308@apache.org>",
                soapPart.getContentId());
        
        // Check the xop:Include element. Note that SAAJ doesn't resolve xop:Includes!
        SOAPElement textElement =
                (SOAPElement)soapPart.getEnvelope().getElementsByTagName("text").item(0);
        assertNotNull(textElement);
        Node xopIncludeNode = textElement.getChildNodes().item(0);
        assertTrue(xopIncludeNode instanceof SOAPElement);
        AttachmentPart ap = message.getAttachment((SOAPElement)xopIncludeNode);
        assertEquals("<1.urn:uuid:F02ECC18873CFB73E211412748909349@apache.org>",
                ap.getContentId());
        
        // Now check the attachments
        Iterator attachments = message.getAttachments();
        assertTrue(attachments.hasNext());
        ap = (AttachmentPart)attachments.next();
        assertEquals("<1.urn:uuid:F02ECC18873CFB73E211412748909349@apache.org>",
                ap.getContentId());
        assertFalse(attachments.hasNext());
    }

    @Validated @Test
    public void testParseSwAMessage() throws Exception {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type",
                "multipart/related; " +
                "boundary=MIMEBoundaryurn_uuid_E3F7CE4554928DA89B1231365678616; " +
                "type=\"text/xml\"; " +
                "start=\"<0.urn:uuid:E3F7CE4554928DA89B1231365678617@apache.org>\"");
        InputStream in = TestUtils.getTestFile("SwAmessage.bin");
        SOAPMessage message = mf.createMessage(headers, in);
        SOAPPart soapPart = message.getSOAPPart();
        assertEquals("<0.urn:uuid:E3F7CE4554928DA89B1231365678617@apache.org>",
                soapPart.getContentId());
        Iterator attachments = message.getAttachments();
        assertTrue(attachments.hasNext());
        AttachmentPart ap = (AttachmentPart)attachments.next();
        assertEquals("<urn:uuid:E3F7CE4554928DA89B1231365678347>",
                ap.getContentId());
        assertFalse(attachments.hasNext());
    }
}
