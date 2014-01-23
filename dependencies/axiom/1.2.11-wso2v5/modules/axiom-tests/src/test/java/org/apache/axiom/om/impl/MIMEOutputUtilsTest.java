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

package org.apache.axiom.om.impl;

import junit.framework.TestCase;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPFactory;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MIMEOutputUtilsTest extends TestCase {
    byte[] buffer;
    byte[] byteArray = new byte[] { 13, 56, 65, 32, 12, 12, 7, -3, -2, -1,
            98 };

    protected void setUp() throws Exception {
        super.setUp();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        ByteArrayOutputStream outStream;
        String boundary;

        OMOutputFormat omOutput = new OMOutputFormat();
        boundary = omOutput.getMimeBoundary();
        omOutput.setSOAP11(false);

        String contentType = omOutput.getContentType();
        DataHandler dataHandler;
        dataHandler = new DataHandler(new ByteArrayDataSource(byteArray));
        OMText textData = factory.createOMText(dataHandler, true);
        assertNotNull(textData.getContentID());

        DataHandler dataHandler2 = new DataHandler(
                "Apache Software Foundation", "text/plain");
        OMText text = factory.createOMText(dataHandler2, true);
        assertNotNull(text.getContentID());
        outStream = new ByteArrayOutputStream();
        outStream.write(("Content-Type: " + contentType).getBytes());
        outStream.write(new byte[] { 13, 10 });
        outStream.write(new byte[] { 13, 10 });

        MIMEOutputUtils.startWritingMime(outStream, boundary);
        MimeBodyPart part1 = MIMEOutputUtils.createMimeBodyPart(textData
                .getContentID(), (DataHandler) textData.getDataHandler());
        MIMEOutputUtils.writeBodyPart(outStream, part1, boundary);
        MimeBodyPart part2 = MIMEOutputUtils.createMimeBodyPart(text
                .getContentID(), (DataHandler) text.getDataHandler());
        MIMEOutputUtils.writeBodyPart(outStream, part2, boundary);
        MIMEOutputUtils.finishWritingMime(outStream);
        buffer = outStream.toByteArray();
    }

    public void testMIMEWriting() throws IOException, MessagingException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(buffer);
        Properties props = new Properties();
        javax.mail.Session session = javax.mail.Session
                .getInstance(props, null);
        MimeMessage mimeMessage = new MimeMessage(session, inStream);
        DataHandler dh = mimeMessage.getDataHandler();
        MimeMultipart multiPart = new MimeMultipart(dh.getDataSource());
        MimeBodyPart mimeBodyPart0 = (MimeBodyPart) multiPart.getBodyPart(0);
        Object object0 = mimeBodyPart0.getContent();
        assertNotNull(object0);
        MimeBodyPart mimeBodyPart1 = (MimeBodyPart) multiPart.getBodyPart(1);
        Object object1 = mimeBodyPart1.getContent();
        assertNotNull(object1);
        assertEquals(multiPart.getCount(), 2);
    }

    public void testWriteSOAPWithAttachmentsMessage() throws IOException {
//    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
// 
//        OMOutputFormat omOutput = new OMOutputFormat();
//        omOutput.setCharSetEncoding(OMConstants.DEFAULT_CHAR_SET_ENCODING);
//        omOutput.setSOAP11(false);
//        omOutput.setDoingSWA(true);
//        
//        StringWriter stringWriter = new StringWriter();
//        stringWriter.write("Apache Axis2");
//        DataHandler dataHandler = new DataHandler(
//                "Apache Software Foundation", "text/plain");
//        HashMap map = new HashMap();
//        map.put("uuid_dsjkjkda",dataHandler);
//        byteArrayOutputStream.write((omOutput.getContentType()+"\n").getBytes());
//        MIMEOutputUtils.writeSOAPWithAttachmentsMessage(stringWriter,byteArrayOutputStream,map,omOutput);
//        System.out.println(byteArrayOutputStream.toString());
    }
}