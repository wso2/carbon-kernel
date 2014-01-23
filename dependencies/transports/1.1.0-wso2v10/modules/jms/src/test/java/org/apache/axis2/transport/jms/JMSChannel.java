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

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.testkit.axis2.AxisServiceConfigurator;
import org.apache.axis2.transport.testkit.name.Key;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;

public abstract class JMSChannel implements AxisServiceConfigurator {
    private final String name;
    private final String destinationType;
    private final ContentTypeMode contentTypeMode;
    protected @Transient JMSTestEnvironment env;
    protected @Transient Context context;
    private @Transient String destinationName;
    private @Transient String jndiName;
    private @Transient Destination destination;
    private @Transient String connectionFactoryName;
    private @Transient String connectionFactoryJNDIName;
    
    public JMSChannel(String name, String destinationType, ContentTypeMode contentTypeMode) {
        this.name = name;
        this.destinationType = destinationType;
        this.contentTypeMode = contentTypeMode;
    }
    
    public JMSChannel(String destinationType, ContentTypeMode contentTypeMode) {
        this(null, destinationType, contentTypeMode);
    }
    
    protected String buildDestinationName(String direction, String destinationType) {
        StringBuilder destinationName = new StringBuilder();
        if (name != null) {
            destinationName.append(name);
            destinationName.append(Character.toUpperCase(direction.charAt(0)));
            destinationName.append(direction.substring(1));
        } else {
            destinationName.append(direction);
        }
        destinationName.append(destinationType == JMSConstants.DESTINATION_TYPE_QUEUE ? 'Q' : 'T');
        return destinationName.toString();
    }
    
    protected String buildJndiName(String direction, String destinationType) {
        return "jms/" + buildDestinationName(direction, destinationType);
    }
    
    @Setup @SuppressWarnings("unused")
    private void setUp(JMSTestEnvironment env, JNDIEnvironment jndiEnvironment, JMSTransportDescriptionFactory tdf) throws Exception {
        this.env = env;
        context = jndiEnvironment.getContext();
        destinationName = buildDestinationName("request", destinationType);
        jndiName = buildJndiName("request", destinationType);
        destination = env.createDestination(destinationType, destinationName);
        context.bind(jndiName, destination);
        connectionFactoryName = tdf.getConnectionFactoryName(destinationType);
        connectionFactoryJNDIName = tdf.getConnectionFactoryJNDIName(destinationType);
    }

    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        context.unbind(jndiName);
        env.deleteDestination(destination);
    }

    @Key("destType")
    public String getDestinationType() {
        return destinationType;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public Destination getDestination() {
        return destination;
    }

    @Key("contentTypeMode")
    public ContentTypeMode getContentTypeMode() {
        return contentTypeMode;
    }

    public int getMessageCount() throws JMSException {
        Connection connection = env.getConnectionFactory().createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueBrowser browser = session.createBrowser((Queue)destination);
            int count = 0;
            for (Enumeration<?> e = browser.getEnumeration(); e.hasMoreElements(); e.nextElement()) {
                count++;
            }
            return count;
        } finally {
            connection.close();
        }
    }

    public EndpointReference getEndpointReference() throws Exception {
        return new EndpointReference(
                "jms:/" + jndiName + "?transport.jms.DestinationType=" + destinationType +
                "&java.naming.factory.initial=org.mockejb.jndi.MockContextFactory" +
                "&transport.jms.ConnectionFactoryJNDIName=" + connectionFactoryJNDIName +
                "&" + JMSConstants.CONTENT_TYPE_PROPERTY_PARAM + "=Content-Type");
    }

    public void setupService(AxisService service, boolean isClientSide) throws Exception {
        service.addParameter(JMSConstants.PARAM_JMS_CONFAC, connectionFactoryName);
        service.addParameter(JMSConstants.PARAM_DEST_TYPE, destinationType);
        service.addParameter(JMSConstants.PARAM_DESTINATION, jndiName);
    }
}
