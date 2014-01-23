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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;

@Name("ActiveMQ")
public class ActiveMQTestEnvironment extends JMSTestEnvironment {
    private static final String BROKER_NAME = "test";
    
    private @Transient BrokerService broker;
    
    @Setup @SuppressWarnings("unused")
    private void setUp() throws Exception {
        broker = new BrokerService();
        broker.setBrokerName(BROKER_NAME);
        broker.setDataDirectory("target/activemq-data");
        broker.setPersistenceAdapter(new MemoryPersistenceAdapter());
        broker.start();
    }

    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        broker.stop();
    }

    @Override
    protected ActiveMQConnectionFactory createConnectionFactory() throws Exception {
        return new ActiveMQConnectionFactory("vm://" + BROKER_NAME);
    }


    @Override
    public Queue createQueue(String name) {
        return new ActiveMQQueue(name);
    }

    @Override
    public Topic createTopic(String name) {
        return new ActiveMQTopic(name);
    }

    @Override
    public void deleteDestination(Destination destination) throws Exception {
    }
}
