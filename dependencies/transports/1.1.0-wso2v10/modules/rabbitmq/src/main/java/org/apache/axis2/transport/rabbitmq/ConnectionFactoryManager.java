/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.rabbitmq;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Class managing a set of {@link ConnectionFactory} objects.
 */
public class ConnectionFactoryManager {
    private final Map<String, ConnectionFactory> connectionFactories =
            new HashMap<String, ConnectionFactory>();

    /**
     * Construct a Connection factory manager for the RabbitMQ transport sender or receiver
     * @param description
     */
    public ConnectionFactoryManager(ParameterInclude description) {
        loadConnectionFactoryDefinitions(description);
    }

    /**
     * Get the connection factory that matches the given name, i.e. referring to
     * the same underlying connection factory. Used by the RabbitMQSender to determine if already
     * available resources should be used for outgoing messages. If no factory instance is
     * found then a new one will be created and added to the connection factory map
     *
     * @param props a Map of connection factory properties and name
     * @return the connection factory or null if no connection factory compatible
     *         with the given properties exists
     */
    public ConnectionFactory getAMQPConnectionFactory(Hashtable<String, String> props) {
        ConnectionFactory connectionFactory = null;
        String hostName = props.get(RabbitMQConstants.SERVER_HOST_NAME);
        String portValue = props.get(RabbitMQConstants.SERVER_PORT);
        String hostAndPort = hostName + ":" + portValue;
        connectionFactory = connectionFactories.get(hostAndPort);

        if (connectionFactory == null) {
            com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
            if (hostName != null && !hostName.equals("")) {
                factory.setHost(hostName);
            } else {
                throw new AxisRabbitMQException("Host name is not correctly defined");
            }
            int port = Integer.parseInt(portValue);
            if (port > 0) {
                factory.setPort(port);
            }
            String userName = props.get(RabbitMQConstants.SERVER_USER_NAME);

            if (userName != null && !userName.equals("")) {
                factory.setUsername(userName);
            }

            String password = props.get(RabbitMQConstants.SERVER_PASSWORD);

            if (password != null && !password.equals("")) {
                factory.setPassword(password);
            }
            String virtualHost = props.get(RabbitMQConstants.SERVER_VIRTUAL_HOST);

            if (virtualHost != null && !virtualHost.equals("")) {
                factory.setVirtualHost(virtualHost);
            }
            connectionFactory = new ConnectionFactory(hostAndPort, factory);
            connectionFactories.put(connectionFactory.getName(), connectionFactory);
        }

        return connectionFactory;
    }


    /**
     * Get the AMQP connection factory with the given name.
     *
     * @param connectionFactoryName the name of the AMQP connection factory
     * @return the AMQP connection factory or null if no connection factory with
     *         the given name exists
     */
    public ConnectionFactory getAMQPConnectionFactory(String connectionFactoryName) {
        return connectionFactories.get(connectionFactoryName);
    }

    /**
     * Create ConnectionFactory instances for the definitions in the transport configuration,
     * and add these into our collection of connectionFactories map keyed by name
     *
     * @param trpDesc the transport description for RabbitMQ AMQP
     */
    private void loadConnectionFactoryDefinitions(ParameterInclude trpDesc) {
        for (Parameter parameter : trpDesc.getParameters()) {
            ConnectionFactory amqpConFactory = new ConnectionFactory(parameter);
            connectionFactories.put(amqpConFactory.getName(), amqpConFactory);
        }
    }
}
