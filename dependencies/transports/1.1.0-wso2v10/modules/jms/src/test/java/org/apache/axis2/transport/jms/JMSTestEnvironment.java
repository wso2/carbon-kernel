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
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.axis2.transport.testkit.name.Key;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

@Key("broker")
public abstract class JMSTestEnvironment {
    private @Transient ConnectionFactory connectionFactory;
    
    @Setup @SuppressWarnings("unused")
    private void setUp() throws Exception {
        connectionFactory = createConnectionFactory();
    }
    
    protected abstract ConnectionFactory createConnectionFactory() throws Exception;
    
    public Destination createDestination(String destinationType, String name) throws Exception {
        if (destinationType.equals(JMSConstants.DESTINATION_TYPE_TOPIC)) {
            return createTopic(name);
        } else {
            return createQueue(name);
        }
    }

    public abstract Queue createQueue(String name) throws Exception;
    public abstract Topic createTopic(String name) throws Exception;
    
    public abstract void deleteDestination(Destination destination) throws Exception;
    
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
