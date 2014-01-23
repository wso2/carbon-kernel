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
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.mail.internet.ContentType;

import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.client.TestClient;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.name.Named;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;

@Name("jms")
public class JMSClient<T> implements TestClient {
    protected final JMSMessageFactory<T> jmsMessageFactory;
    
    private @Transient Connection connection;
    private @Transient Session session;
    private @Transient MessageProducer producer;
    private @Transient ContentTypeMode contentTypeMode;
    
    public JMSClient(JMSMessageFactory<T> jmsMessageFactory) {
        this.jmsMessageFactory = jmsMessageFactory;
    }

    @Named
    public JMSMessageFactory<T> getJmsMessageFactory() {
        return jmsMessageFactory;
    }

    @Setup @SuppressWarnings("unused")
    private void setUp(JMSTestEnvironment env, JMSChannel channel) throws Exception {
        Destination destination = channel.getDestination();
        ConnectionFactory connectionFactory = env.getConnectionFactory();
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(destination);
        contentTypeMode = channel.getContentTypeMode();
    }

    protected String doSendMessage(ClientOptions options, ContentType contentType, T message) throws Exception {
        Message jmsMessage = jmsMessageFactory.createMessage(session, message);
        if (contentTypeMode == ContentTypeMode.TRANSPORT) {
            jmsMessage.setStringProperty(BaseConstants.CONTENT_TYPE, contentType.toString());
        }
        producer.send(jmsMessage);
        return jmsMessage.getJMSMessageID();
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        producer.close();
        session.close();
        connection.close();
    }
    
    public ContentType getContentType(ClientOptions options, ContentType contentType) {
        return contentType;
    }
}
