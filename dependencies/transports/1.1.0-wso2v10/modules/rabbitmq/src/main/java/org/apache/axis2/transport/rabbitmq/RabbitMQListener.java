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


import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.base.AbstractTransportListenerEx;

/**
 * The RabbitMQ AMQP Transport listener implementation. Creates {@link ServiceTaskManager} instances
 * for each service requesting exposure over AMQP, and stops these if they are undeployed / stopped.
 * A service indicates a AMQP Connection factory definition by name, which would be defined in the
 * RabbitMQListener on the axis2.xml, and this provides a way to reuse common configuration between
 * services, as well as to optimize resources utilized
 */
public class RabbitMQListener extends AbstractTransportListenerEx<RabbitMQEndpoint> {

    /** The ConnectionFactoryManager which centralizes the management of defined factories */
    private ConnectionFactoryManager connectionFactoryManager;

    @Override
    protected void doInit() throws AxisFault {
        connectionFactoryManager = new ConnectionFactoryManager(getTransportInDescription());
        log.info("RabbitMQ AMQP Transport Receiver initialized...");
    }

    @Override
    protected RabbitMQEndpoint createEndpoint() {
        return new RabbitMQEndpoint(this, workerPool);
    }


    /**
     * Listen for AMQP messages on behalf of the given service
     *
     * @param endpoint the Axis service for which to listen for messages
     */
    @Override
    protected void startEndpoint(RabbitMQEndpoint endpoint) throws AxisFault {
        ServiceTaskManager stm = endpoint.getServiceTaskManager();
        stm.start();
    }

    /**
     * Stops listening for messages for the service thats undeployed or stopped
     *
     * @param endpoint the service that was undeployed or stopped
     */
    @Override
    protected void stopEndpoint(RabbitMQEndpoint endpoint) {
        ServiceTaskManager stm = endpoint.getServiceTaskManager();
        stm.stop();
    }

    /**
     * Return the connection factory name for this service. If this service
     * refers to an invalid factory or defaults to a non-existent default
     * factory, this returns null
     *
     * @param service the AxisService
     * @return the ConnectionFactory to be used, or null if reference is invalid
     */
    public ConnectionFactory getConnectionFactory(AxisService service) {
        Parameter conFacParam = service.getParameter(RabbitMQConstants.RABBITMQ_CON_FAC);
        if (conFacParam != null) {
            return connectionFactoryManager.getAMQPConnectionFactory((String) conFacParam.getValue());
        }
        return null;
    }

    @Override
    public void pause() throws AxisFault {
        //TODO Implement me
    }


    @Override
    public void resume() throws AxisFault {
        //TODO Implement me
    }
}
