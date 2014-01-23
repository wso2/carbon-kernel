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

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.jms.iowrappers.BytesMessageInputStream;
import org.apache.axis2.transport.jms.iowrappers.BytesMessageOutputStream;
import org.apache.axis2.transport.testkit.endpoint.InOutEndpoint;
import org.apache.axis2.transport.testkit.endpoint.InOutEndpointSupport;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Name("mock")
public class MockEchoEndpoint extends InOutEndpointSupport implements InOutEndpoint, ExceptionListener {
    static Log log = LogFactory.getLog(MockEchoEndpoint.class);
    
    private @Transient Connection connection;
    private @Transient Connection replyConnection;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(JMSTestEnvironment env, JMSRequestResponseChannel channel) throws Exception {
        Destination destination = channel.getDestination();
        Destination replyDestination = channel.getReplyDestination();
        connection = env.getConnectionFactory().createConnection();
        connection.setExceptionListener(this);
        connection.start();
        replyConnection = env.getConnectionFactory().createConnection();
        replyConnection.setExceptionListener(this);
        final Session replySession = replyConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final MessageProducer producer = replySession.createProducer(replyDestination);
        MessageConsumer consumer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE).createConsumer(destination);
        consumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                try {
                    log.info("Message received: ID = " + message.getJMSMessageID());
                    Message reply;
                    if (message instanceof BytesMessage) {
                        reply = replySession.createBytesMessage();
                        IOUtils.copy(new BytesMessageInputStream((BytesMessage)message), new BytesMessageOutputStream((BytesMessage)reply));
                    } else if (message instanceof TextMessage) {
                        reply = replySession.createTextMessage();
                        ((TextMessage)reply).setText(((TextMessage)message).getText());
                    } else {
                        // TODO
                        throw new UnsupportedOperationException("Unsupported message type");
                    }
                    reply.setJMSCorrelationID(message.getJMSMessageID());
                    reply.setStringProperty(BaseConstants.CONTENT_TYPE, message.getStringProperty(BaseConstants.CONTENT_TYPE));
                    producer.send(reply);
                    log.info("Message sent: ID = " + reply.getJMSMessageID());
                } catch (Throwable ex) {
                    fireEndpointError(ex);
                }
            }
        });
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        connection.close();
        replyConnection.close();
    }

    public void onException(JMSException ex) {
        log.error("Exception received by JMS exception listener", ex);
        fireEndpointError(ex);
    }
}
