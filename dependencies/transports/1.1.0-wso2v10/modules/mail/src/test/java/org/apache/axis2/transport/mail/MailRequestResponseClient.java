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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.client.RequestResponseTestClient;
import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MailRequestResponseClient extends MailClient implements RequestResponseTestClient<byte[],byte[]> {
    private static final Log log = LogFactory.getLog(MailRequestResponseClient.class);
    
    private @Transient MailChannel channel;
    private @Transient Store store;
    
    public MailRequestResponseClient(MessageLayout layout) {
        super(layout);
    }
    
    @Setup @SuppressWarnings("unused")
    private void setUp(MailTestEnvironment env, MailChannel channel) throws MessagingException {
        this.channel = channel;
        Session session = channel.getReplySession();
        session.setDebug(log.isTraceEnabled());
        store = session.getStore(env.getProtocol());
        MailTestEnvironment.Account sender = channel.getSender();
        store.connect(sender.getLogin(), sender.getPassword());
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws MessagingException {
        store.close();
    }
    
    public IncomingMessage<byte[]> sendMessage(ClientOptions options, ContentType contentType, byte[] message) throws Exception {
        String msgId = sendMessage(contentType, message);
        Message reply = waitForReply(msgId);
        Assert.assertNotNull("No response received", reply);
        Assert.assertEquals(channel.getSender().getAddress(),
                            ((InternetAddress)reply.getRecipients(Message.RecipientType.TO)[0]).getAddress());
        Assert.assertEquals(channel.getRecipient().getAddress(),
                            ((InternetAddress)reply.getFrom()[0]).getAddress());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        reply.getDataHandler().writeTo(baos);
        return new IncomingMessage<byte[]>(new ContentType(reply.getContentType()), baos.toByteArray());
    }
    
    private Message waitForReply(String msgId) throws Exception {
        Thread.yield();
        Thread.sleep(100);
        
        Message reply = null;
        boolean replyNotFound = true;
        int retryCount = 50;
        while (replyNotFound) {
            log.debug("Checking for response ... with MessageID : " + msgId);
            reply = getMessage(msgId);
            if (reply != null) {
                replyNotFound = false;
            } else {
                if (retryCount-- > 0) {
                    Thread.sleep(100);
                } else {
                    break;
                }
            }
        }
        return reply;
    }

    private Message getMessage(String requestMsgId) throws Exception {
        MimeMessage response = null;
        Folder folder = store.getFolder(MailConstants.DEFAULT_FOLDER);
        folder.open(Folder.READ_WRITE);
        Message[] msgs = folder.getMessages();
        log.debug(msgs.length + " messages in mailbox");
        loop: for (Message m : msgs) {
            MimeMessage mimeMessage = (MimeMessage)m;
            String[] inReplyTo = mimeMessage.getHeader(MailConstants.MAIL_HEADER_IN_REPLY_TO);
            log.debug("Found message " + mimeMessage.getMessageID() + " in reply to " + Arrays.toString(inReplyTo));
            if (inReplyTo != null && inReplyTo.length > 0) {
                for (int j=0; j<inReplyTo.length; j++) {
                    if (requestMsgId.equals(inReplyTo[j])) {
                        log.debug("Identified message " + mimeMessage.getMessageID() + " as the response to " + requestMsgId + "; retrieving it from the store");
                        // We need to create a copy so that we can delete the original and close the folder
                        response = new MimeMessage(mimeMessage);
                        log.debug("Flagging message " + mimeMessage.getMessageID() + " for deletion");
                        mimeMessage.setFlag(Flags.Flag.DELETED, true);
                        break loop;
                    }
                }
            }
            log.warn("Don't know what to do with message " + mimeMessage.getMessageID() + "; skipping");
        }
        folder.close(true);
        return response;
    }
}
