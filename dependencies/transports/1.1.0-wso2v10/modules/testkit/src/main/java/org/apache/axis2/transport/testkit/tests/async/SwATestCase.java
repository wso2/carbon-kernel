/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.tests.async;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.message.XMLMessage;
import org.apache.axis2.transport.testkit.name.Name;

@Name("AsyncSwA")
public class SwATestCase extends AsyncMessageTestCase<XMLMessage> {
    private static final Random random = new Random();
    
    private byte[] attachmentContent;
    private String contentID;
    
    public SwATestCase(AsyncChannel channel, AsyncTestClient<XMLMessage> client, AsyncEndpoint<XMLMessage> endpoint, Object... resources) {
        super(channel, client, endpoint, XMLMessage.Type.SWA.getContentType(), "UTF-8", resources);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        attachmentContent = new byte[8192];
        random.nextBytes(attachmentContent);
        contentID = UIDGenerator.generateContentId();
    }

    @Override
    protected XMLMessage prepareMessage() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement payload = factory.createOMElement(new QName("root"));
        Attachments attachments = new Attachments();
        attachments.addDataHandler(contentID, new DataHandler(new ByteArrayDataSource(attachmentContent, "application/octet-stream")));
        return new XMLMessage(payload, XMLMessage.Type.SWA, attachments);
    }

    @Override
    protected void checkMessageData(XMLMessage expected, XMLMessage actual) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Attachments attachments = actual.getAttachments();
        DataHandler dataHandler = attachments.getDataHandler(contentID);
        assertNotNull(dataHandler);
        dataHandler.writeTo(baos);
        assertTrue(Arrays.equals(attachmentContent, baos.toByteArray()));
    }
}
