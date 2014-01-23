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

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

@RunWith(SAAJTestRunner.class)
public class AttachmentSerializationTest extends Assert {

    public static final String MIME_MULTIPART_RELATED = "multipart/related";
    public static final String MIME_APPLICATION_DIME = "application/dime";
    public static final String NS_PREFIX = "jaxmtst";
    public static final String NS_URI = "http://www.jcommerce.net/soap/jaxm/TestJaxm";

    @Validated @Test
    public void testAttachments() throws Exception {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        int count = saveMsgWithAttachments(bais);
        assertEquals(count, 2);
    }

    public int saveMsgWithAttachments(OutputStream os) throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage msg = mf.createMessage();

        SOAPPart soapPart = msg.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPHeader header = envelope.getHeader();
        SOAPBody body = envelope.getBody();

        SOAPElement el = header.addHeaderElement(envelope.createName("field4", NS_PREFIX, NS_URI));

        SOAPElement el2 = el.addChildElement("field4b", NS_PREFIX);
        el2.addTextNode("field4value");

        el = body.addBodyElement(envelope.createName("bodyfield3", NS_PREFIX, NS_URI));
        el2 = el.addChildElement("bodyfield3a", NS_PREFIX);
        el2.addTextNode("bodyvalue3a");
        el2 = el.addChildElement("bodyfield3b", NS_PREFIX);
        el2.addTextNode("bodyvalue3b");
        el.addChildElement("datefield", NS_PREFIX);

        // First Attachment
        AttachmentPart ap = msg.createAttachmentPart();
        final String testText = "some attachment text...";
        ap.setContent(testText, "text/plain");
        msg.addAttachmentPart(ap);

        // Second attachment
        DataHandler dh = new DataHandler(TestUtils.getTestFileAsDataSource("axis2.jpg"));
        AttachmentPart ap2 = msg.createAttachmentPart(dh);
        ap2.setContentType("image/jpg");
        msg.addAttachmentPart(ap2);

        msg.saveChanges(); // This is only required with Sun's SAAJ implementation

        MimeHeaders headers = msg.getMimeHeaders();
        assertTrue(headers != null);
        String [] contentType = headers.getHeader("Content-Type");
        assertTrue(contentType != null);

        int i = 0;
        for (Iterator iter = msg.getAttachments(); iter.hasNext();) {
            AttachmentPart attachmentPart = (AttachmentPart)iter.next();
            final Object content = attachmentPart.getDataHandler().getContent();
            if (content instanceof String) {
                assertEquals(testText, (String)content);
            } else if (content instanceof FileInputStream) {

                // try to write to a File and check whether it is ok
                final FileInputStream fis = (FileInputStream)content;
                byte[] b = new byte[15000];
                final int lengthRead = fis.read(b);
                FileOutputStream fos =
                        new FileOutputStream(new File(System.getProperty("basedir", ".") + "/" +
                                "target/test-resources/atts-op" + (i++) + ".jpg"));
                fos.write(b, 0, lengthRead);
                fos.flush();
                fos.close();
            }
        }

        msg.writeTo(os);
        os.flush();
        return msg.countAttachments();
    }

    public int loadMsgWithAttachments(InputStream is) throws Exception {
        MimeHeaders headers = new MimeHeaders();
        headers.setHeader("Content-Type", MIME_MULTIPART_RELATED);
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage msg = mf.createMessage(headers, is);

        SOAPPart sp = msg.getSOAPPart();
        assertTrue(sp != null);

        SOAPEnvelope envelope = sp.getEnvelope();
        assertTrue(envelope != null);
        return msg.countAttachments();
    }
}
