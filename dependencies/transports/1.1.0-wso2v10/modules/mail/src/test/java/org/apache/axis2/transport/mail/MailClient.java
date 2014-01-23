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

package org.apache.axis2.transport.mail;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.client.TestClient;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.name.Named;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

@Name("javamail")
public abstract class MailClient implements TestClient {
    private final MessageLayout layout;
    private @Transient MailChannel channel;
    private @Transient Session session;
    
    public MailClient(MessageLayout layout) {
        this.layout = layout;
    }

    @Named
    public MessageLayout getLayout() {
        return layout;
    }

    @Setup @SuppressWarnings("unused")
    private void setUp(MailTestEnvironment env, MailChannel channel) throws Exception {
        Properties props = new Properties();
        props.putAll(env.getOutProperties());
        session = Session.getInstance(props);
        this.channel = channel;
    }

    public ContentType getContentType(ClientOptions options, ContentType contentType) {
        return contentType;
    }

    protected String sendMessage(ContentType contentType, byte[] message) throws Exception {
        String msgId = UUIDGenerator.getUUID();
        MimeMessage msg = new MimeMessage(session);
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(channel.getRecipient().getAddress()));
        msg.setFrom(new InternetAddress(channel.getSender().getAddress()));
        msg.setSentDate(new Date());
        msg.setHeader(MailConstants.MAIL_HEADER_MESSAGE_ID, msgId);
        msg.setHeader(MailConstants.MAIL_HEADER_X_MESSAGE_ID, msgId);
        DataHandler dh = new DataHandler(new ByteArrayDataSource(message, contentType.toString()));
        layout.setupMessage(msg, dh);
        Transport.send(msg);
        return msgId;
    }
}
