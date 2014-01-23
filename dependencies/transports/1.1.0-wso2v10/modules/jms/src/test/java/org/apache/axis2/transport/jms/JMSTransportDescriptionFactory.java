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

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.testkit.axis2.TransportDescriptionFactory;
import org.apache.axis2.transport.testkit.name.Key;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.mockejb.jndi.MockContextFactory;

public class JMSTransportDescriptionFactory implements TransportDescriptionFactory {
    public static final String CONNECTION_FACTORY = "ConnectionFactory";
    public static final String QUEUE_CONNECTION_FACTORY = "QueueConnectionFactory";
    public static final String TOPIC_CONNECTION_FACTORY = "TopicConnectionFactory";
    
    private static final OMFactory factory = OMAbstractFactory.getOMFactory();
    
    private final boolean singleCF;
    private final boolean cfOnSender;
    private final int concurrentConsumers;
    private @Transient Context context;
    
    /**
     * Constructor.
     * @param singleCF True if a single connection factory for all types of destinations
     *                 (queues and topics) should be used. Otherwise, separate connection
     *                 factories will be configured.
     * @param cfOnSender Determine whether the connection factories (JMS providers)
     *                   should also be configured on the sender. This switch allows
     *                   us to build regression tests for SYNAPSE-448. 
     */
    public JMSTransportDescriptionFactory(boolean singleCF, boolean cfOnSender, int concurrentConsumers) {
        this.singleCF = singleCF;
        this.cfOnSender = cfOnSender;
        this.concurrentConsumers = concurrentConsumers;
    }

    @Setup @SuppressWarnings("unused")
    private void setUp(JMSTestEnvironment env, JNDIEnvironment jndiEnvironment) throws Exception {
        context = jndiEnvironment.getContext();
        ConnectionFactory connectionFactory = env.getConnectionFactory();
        if (singleCF) {
            context.bind(CONNECTION_FACTORY, connectionFactory);
        } else {
            context.bind(QUEUE_CONNECTION_FACTORY, connectionFactory);
            context.bind(TOPIC_CONNECTION_FACTORY, connectionFactory);
        }
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        if (singleCF) {
            context.unbind(CONNECTION_FACTORY);
        } else {
            context.unbind(QUEUE_CONNECTION_FACTORY);
            context.unbind(TOPIC_CONNECTION_FACTORY);
        }
    }
    
    @Key("singleCF")
    public boolean isSingleCF() {
        return singleCF;
    }

    @Key("cfOnSender")
    public boolean isCfOnSender() {
        return cfOnSender;
    }

    private OMElement createParameterElement(String name, String value) {
        OMElement element = factory.createOMElement(new QName("parameter"));
        element.addAttribute("name", name, null);
        if (value != null) {
            element.setText(value);
        }
        return element;
    }
    
    private void setupConnectionFactoryConfig(ParameterInclude trpDesc, String name, String connFactName, String type) throws AxisFault {
        OMElement element = createParameterElement(JMSConstants.DEFAULT_CONFAC_NAME, null);
        element.addChild(createParameterElement(Context.INITIAL_CONTEXT_FACTORY,
                MockContextFactory.class.getName()));
        element.addChild(createParameterElement(JMSConstants.PARAM_CONFAC_JNDI_NAME,
                connFactName));
        if (type != null) {
            element.addChild(createParameterElement(JMSConstants.PARAM_CONFAC_TYPE, type));
        }
        element.addChild(createParameterElement(JMSConstants.PARAM_CONCURRENT_CONSUMERS,
            Integer.toString(concurrentConsumers)));
        trpDesc.addParameter(new Parameter(name, element));
    }
    
    private void setupTransport(ParameterInclude trpDesc) throws AxisFault {
        if (singleCF) {
            // TODO: setting the type to "queue" is nonsense, but required by the transport (see SYNAPSE-439)
            setupConnectionFactoryConfig(trpDesc, "default", CONNECTION_FACTORY, null);
        } else {
            setupConnectionFactoryConfig(trpDesc, "queue", QUEUE_CONNECTION_FACTORY, "queue");
            setupConnectionFactoryConfig(trpDesc, "topic", TOPIC_CONNECTION_FACTORY, "topic");
        }
    }
    
    public TransportInDescription createTransportInDescription() throws Exception {
        TransportInDescription trpInDesc = new TransportInDescription(JMSListener.TRANSPORT_NAME);
        setupTransport(trpInDesc);
        trpInDesc.setReceiver(new JMSListener());
        return trpInDesc;
    }
    
    public TransportOutDescription createTransportOutDescription() throws Exception {
        TransportOutDescription trpOutDesc = new TransportOutDescription(JMSSender.TRANSPORT_NAME);
        if (cfOnSender) {
            setupTransport(trpOutDesc);
        }
        trpOutDesc.setSender(new JMSSender());
        return trpOutDesc;
    }
    
    public String getConnectionFactoryName(String destinationType) {
        return singleCF ? "default" : destinationType;
    }
    
    public String getConnectionFactoryJNDIName(String destinationType) {
        if (singleCF) {
            return CONNECTION_FACTORY;
        } else {
            return destinationType.equals("queue") ? QUEUE_CONNECTION_FACTORY
                                                   : TOPIC_CONNECTION_FACTORY;
        }
    }
}
