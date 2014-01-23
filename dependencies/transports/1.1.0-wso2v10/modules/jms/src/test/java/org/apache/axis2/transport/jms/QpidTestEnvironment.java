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

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.qpid.AMQException;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.client.AMQDestination;
import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.client.transport.TransportConnection;
import org.apache.qpid.exchange.ExchangeDefaults;
import org.apache.qpid.framing.AMQShortString;
import org.apache.qpid.server.registry.ApplicationRegistry;
import org.apache.qpid.server.virtualhost.VirtualHost;

@Name("qpid")
public class QpidTestEnvironment extends JMSTestEnvironment {
    private VirtualHost virtualHost;
    
    @Setup @SuppressWarnings("unused")
    private void setUp() throws Exception {
        TransportConnection.createVMBroker(1);
        // null means the default virtual host
        virtualHost = ApplicationRegistry.getInstance(1).getVirtualHostRegistry().getVirtualHost(null);
    }

    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        TransportConnection.killVMBroker(1);
    }

    @Override
    protected AMQConnectionFactory createConnectionFactory() throws Exception {
        return new AMQConnectionFactory("vm://:1", "guest", "guest", "fred", "test");
    }

    @Override
    public Queue createQueue(String name) throws AMQException {
        QpidUtil.createQueue(virtualHost, ExchangeDefaults.DIRECT_EXCHANGE_NAME, name);
        return new AMQQueue(ExchangeDefaults.DIRECT_EXCHANGE_NAME, name);
    }

    @Override
    public Topic createTopic(String name) throws AMQException {
        QpidUtil.createQueue(virtualHost, ExchangeDefaults.TOPIC_EXCHANGE_NAME, name);
        return new AMQTopic(ExchangeDefaults.TOPIC_EXCHANGE_NAME, name);
    }

    @Override
    public void deleteDestination(Destination destination) throws Exception {
        QpidUtil.deleteQueue(virtualHost, ((AMQDestination)destination).getDestinationName().asString());
    }
}
