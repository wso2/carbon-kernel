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
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;

@RunWith(SAAJTestRunner.class)
public class AttachmentTest extends Assert {
    @Validated @Test
    public void testStringAttachment() throws Exception {

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        AttachmentPart attachment = message.createAttachmentPart();
        String stringContent = "Update address for Sunny Skies " +
                "Inc., to 10 Upbeat Street, Pleasant Grove, CA 95439";

        attachment.setContent(stringContent, "text/plain");
        attachment.setContentId("update_address");
        message.addAttachmentPart(attachment);

        assertTrue(message.countAttachments() == 1);

        Iterator it = message.getAttachments();
        while (it.hasNext()) {
            attachment = (AttachmentPart)it.next();
            Object content = attachment.getContent();
            String id = attachment.getContentId();
            assertEquals(content, stringContent);
        }
        message.removeAllAttachments();
        assertTrue(message.countAttachments() == 0);
    }

    @Validated @Test
    public void testMultipleAttachments() throws Exception {

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage();

        AttachmentPart a1 = msg.createAttachmentPart(new DataHandler("<some_xml/>", "text/xml"));
        a1.setContentType("text/xml");
        msg.addAttachmentPart(a1);
        AttachmentPart a2 = msg.createAttachmentPart(new DataHandler("<some_xml/>", "text/xml"));
        a2.setContentType("text/xml");
        msg.addAttachmentPart(a2);
        AttachmentPart a3 = msg.createAttachmentPart(new DataHandler("text", "text/plain"));
        a3.setContentType("text/plain");
        msg.addAttachmentPart(a3);

        assertTrue(msg.countAttachments() == 3);

        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml");

        int nAttachments = 0;
        Iterator iterator = msg.getAttachments(mimeHeaders);
        while (iterator.hasNext()) {
            nAttachments++;
            AttachmentPart ap = (AttachmentPart)iterator.next();
            assertTrue(ap.equals(a1) || ap.equals(a2));
        }
        assertTrue(nAttachments == 2);
    }

    // Note: This test case fails with Sun's SAAJ implementation
    //       and can't be @Validated.
    @Test
    public void testBadAttSize() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();

        ByteArrayInputStream ins = new ByteArrayInputStream(new byte[5]);
        DataHandler dh = new DataHandler(new Src(ins, "text/plain"));
        AttachmentPart part = message.createAttachmentPart(dh);
        assertEquals("Size should match", 5, part.getSize());
    }

    class Src implements DataSource {
        InputStream m_src;
        String m_type;

        public Src(InputStream data, String type) {
            m_src = data;
            m_type = type;
        }

        public String getContentType() {
            return m_type;
        }

        public InputStream getInputStream() throws IOException {
            m_src.reset();
            return m_src;
        }

        public String getName() {
            return "Some-Data";
        }

        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException("I don't give output streams");
        }
    }

    @Validated @Test
    public void testClearContent() throws Exception {
        try {
            InputStream in1 = TestUtils.getTestFile("attach.xml");

            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage message = factory.createMessage();
            AttachmentPart ap = message.createAttachmentPart();
            MimeHeader mh = null;

            //Setting Mime Header
            ap.setMimeHeader("Content-Description", "some text");

            //Setting Content Id Header
            ap.setContentId("id@abc.com");

            //Setting Content
            ap.setContent(new StreamSource(in1), "text/xml");

            //Clearing Content
            ap.clearContent();

            try {

                //Getting Content
                InputStream is = (InputStream)ap.getContent();
                fail("Error: SOAPException should have been thrown");
            } catch (SOAPException e) {
                //Error thrown.(expected)
            }

            Iterator iterator = ap.getAllMimeHeaders();
            int cnt = 0;
            boolean foundHeader1 = false;
            boolean foundHeader2 = false;
            boolean foundDefaultHeader = false;
            while (iterator.hasNext()) {
                cnt++;
                mh = (MimeHeader)iterator.next();
                String name = mh.getName();
                String value = mh.getValue();
                if (name.equals("Content-Description") && value.equals("some text")) {
                    if (!foundHeader1) {
                        foundHeader1 = true;
                        //MimeHeaders do match for header1
                    } else {
                        fail("Error: Received the same header1 header twice");
                    }
                } else if (name.equalsIgnoreCase("Content-Id") && value.equals("id@abc.com")) {
                    if (!foundHeader2) {
                        foundHeader2 = true;
                        //MimeHeaders do match for header2
                    } else {
                        fail("Error: Received the same header2 header twice");
                    }
                } else if (name.equals("Content-Type") && value.equals("text/xml")) {
                    if (!foundDefaultHeader) {
                        foundDefaultHeader = true;
                        //MimeHeaders do match for default header
                    } else {
                        fail("Error: Received the same default header header twice");
                    }
                } else {
                    fail("Error: Received an invalid header");
                }
            }

            if (!(foundHeader1 && foundHeader2)) {
                fail("Error: did not receive both headers");
            }

        } catch (Exception e) {
            fail("Exception: " + e);
        }

    }

    @Validated @Test
    public void testGetContent() throws Exception {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage msg = factory.createMessage();
            AttachmentPart ap = msg.createAttachmentPart();
            Image image = ImageIO.read(TestUtils.getTestFileURL("attach.gif"));
            ap = msg.createAttachmentPart(image, "image/gif");

            //Getting Content should return an Image object
            Object o = ap.getContent();
            if (o != null) {
                if (o instanceof Image) {
                    //Image object was returned (ok)
                } else {
                    fail("Unexpected object was returned");
                }
            }
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    @Validated @Test
    public void testGetRawContents() {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage msg = factory.createMessage();
            AttachmentPart ap = msg.createAttachmentPart();
            ap = msg.createAttachmentPart();
            byte data1[] = null;
            data1 = ap.getRawContentBytes();

        } catch (SOAPException e) {
            //Caught expected SOAPException
        } catch (NullPointerException e) {
            //Caught expected NullPointerException
        } catch (Exception e) {
            fail();
        }
    }

    @Validated @Test
    public void testSetBase64Content() {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage msg = factory.createMessage();
            AttachmentPart ap = msg.createAttachmentPart();

            String urlString = "http://ws.apache.org/images/project-logo.jpg";
            if (isNetworkedResourceAvailable(urlString)) {
                URL url = new URL(urlString);
                DataHandler dh = new DataHandler(url);
                //Create InputStream from DataHandler's InputStream
                InputStream is = dh.getInputStream();

                byte buf[] = IOUtils.getStreamAsByteArray(is);
                //Setting Content via InputStream for image/jpeg mime type
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Base64.encode(buf, 0, buf.length, bos);
                buf = bos.toByteArray();
                InputStream stream = new ByteArrayInputStream(buf);
                ap.setBase64Content(stream, "image/jpeg");

                //Getting Content.. should return InputStream object
                InputStream r = ap.getBase64Content();
                if (r != null) {
                    if (r instanceof InputStream) {
                        //InputStream object was returned (ok)
                    } else {
                        fail("Unexpected object was returned");
                    }
                }
            }
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    private boolean isNetworkedResourceAvailable(String url) {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(1000);
                method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                        new DefaultHttpMethodRetryHandler(1, false));

        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                return false;
            }
            //byte[] responseBody = method.getResponseBody();
        } catch (HttpException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            method.releaseConnection();
        }
        return true;
    }
}