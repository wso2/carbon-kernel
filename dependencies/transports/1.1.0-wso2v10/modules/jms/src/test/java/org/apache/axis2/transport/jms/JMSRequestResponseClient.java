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

package org.apache.axis2.transport.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.client.RequestResponseTestClient;
import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;

public class JMSRequestResponseClient<T> extends JMSClient<T> implements RequestResponseTestClient<T,T> {
    private @Transient Destination replyDestination;
    private @Transient Connection replyConnection;
    private @Transient Session replySession;

    public JMSRequestResponseClient(JMSMessageFactory<T> jmsMessageFactory) {
        super(jmsMessageFactory);
    }
    
    @Setup @SuppressWarnings("unused")
    private void setUp(JMSTestEnvironment env, JMSRequestResponseChannel channel) throws Exception {
        replyDestination = channel.getReplyDestination();
        ConnectionFactory connectionFactory = env.getConnectionFactory();
        replyConnection = connectionFactory.createConnection();
        replyConnection.start();
        replySession = replyConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
    
    public IncomingMessage<T> sendMessage(ClientOptions options, ContentType contentType, T message) throws Exception {
        String correlationId = doSendMessage(options, contentType, message);
        MessageConsumer consumer = replySession.createConsumer(replyDestination, "JMSCorrelationID = '" + correlationId + "'");
        try {
            Message replyMessage = consumer.receive(8000);
            return new IncomingMessage<T>(new ContentType(replyMessage.getStringProperty("Content-Type")),
                                          jmsMessageFactory.parseMessage(replyMessage));
        } finally {
            consumer.close();
        }
    }

    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        replySession.close();
        replyConnection.close();
    }
}
