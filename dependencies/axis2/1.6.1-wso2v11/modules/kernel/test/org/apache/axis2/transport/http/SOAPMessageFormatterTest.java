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
package org.apache.axis2.transport.http;

import java.io.ByteArrayOutputStream;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import junit.framework.TestCase;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;

public class SOAPMessageFormatterTest extends TestCase {
    public void testMM7() throws Exception {
        SOAPMessageFormatter formatter = new SOAPMessageFormatter();
        MessageContext mc = new MessageContext();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(factory.getDefaultEnvelope());
        mc.setDoingSwA(true);
        Attachments attachments = mc.getAttachmentMap();
        attachments.addDataHandler("cid1@test.org", new DataHandler("test1", "text/plain"));
        attachments.addDataHandler("cid2@test.org", new DataHandler("test2", "text/plain"));
        mc.setProperty(Constants.Configuration.MM7_COMPATIBLE, true);
        OMOutputFormat format = new OMOutputFormat();
        format.setDoingSWA(true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        formatter.writeTo(mc, format, baos, true);
        
        MimeMultipart mp = new MimeMultipart(new ByteArrayDataSource(baos.toByteArray(), format.getContentType()));
        assertEquals(2, mp.getCount());
        BodyPart bp = mp.getBodyPart(0);
        assertEquals("<" + format.getRootContentId() + ">", bp.getHeader("Content-ID")[0]);
        bp = mp.getBodyPart(1);
        Object content = bp.getContent();
        assertTrue(content instanceof MimeMultipart);
        MimeMultipart inner = (MimeMultipart)content;
        assertEquals(2, inner.getCount());
        bp = inner.getBodyPart(0);
        assertEquals("<cid1@test.org>", bp.getHeader("Content-ID")[0]);
        assertEquals("test1", bp.getContent());
        bp = inner.getBodyPart(1);
        assertEquals("<cid2@test.org>", bp.getHeader("Content-ID")[0]);
        assertEquals("test2", bp.getContent());
    }
}
