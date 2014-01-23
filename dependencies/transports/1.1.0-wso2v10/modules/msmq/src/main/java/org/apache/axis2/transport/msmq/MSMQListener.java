/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.msmq;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.AbstractTransportListenerEx;
import org.apache.axis2.transport.msmq.MSMQConnectionManager.ConnectionType;

/**
 * The revamped MSMQ Transport listener implementation. Creates {@link ServiceTaskManager} instances
 * for each service requesting exposure over MSMQ, and stops these if they are undeployed / stopped.
 * <p>
 * A service indicates a MSMQ client connection  definition by name, which would be defined in the
 * MSMQListener on the axis2.xml, and this provides a way to reuse common configuration between
 * services, as well as to optimize resources utilized
 * All Destinations / MSMQ connection  objects used MUST be pre-created or already available 
 */
public class MSMQListener extends AbstractTransportListenerEx<MSMQEndpoint> {
    public static final String TRANSPORT_NAME = MSMQConstants.TRANSPORT_NAME;
    
    @Override
    protected MSMQEndpoint createEndpoint() {
        return new MSMQEndpoint(this, workerPool);
    }

    /**
     * Listen for MSMQ messages on behalf of the given service
     *
     * @param service the Axis service for which to listen for messages
     */
    @Override
    protected void startEndpoint(MSMQEndpoint endpoint) throws AxisFault {
        // if the queue is not there create a queue with default parameters
        // service name and start listening on it
        String destinationQueue =MSMQConnectionManager.getReceiverQueueFullName(endpoint.getServiceName()); 
        ServiceTaskManager stm = endpoint.getServiceTaskManager();
        stm.setDestinationQueue(destinationQueue);
        stm.start();
        // just wait three seconds to start the poll task
        for (int i = 0; i < 3; i++) {
            if(stm.getActiveTaskCount() > 0){
            	if (log.isDebugEnabled()) {
            		log.info("Started to listen on destination : " + destinationQueue);
            	}
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                 //ignore the exception
            }
        }
        log.warn("Polling tasks on destinatination :" + destinationQueue + " has not yet started" +
                "after 3 seconds");
    }
    
    
    /**
     * Stops listening for messages for the service thats undeployed or stopped
     *
     * @param service the service that was undeployed or stopped
     */
    @Override
    protected void stopEndpoint(MSMQEndpoint endpoint) {
    	 ServiceTaskManager stm = endpoint.getServiceTaskManager();
         stm.stop();
         if (log.isDebugEnabled()) {
        	 log.info("Stopped listening for MSMQ messages to service : " + endpoint.getServiceName());
         }
    }

    /**
     * Pause the listener - Stop accepting/processing new messages, but continues processing existing
     * messages until they complete. This helps bring an instance into a maintenence mode
     * @throws AxisFault on error
     */
    @Override
    public void pause() throws AxisFault {
        //throw new AxisFault("Method not implemented: pause");
        // TODO
    }

    /**
     * Resume the lister - Brings the lister into active mode back from a paused state
     * @throws AxisFault on error
     */
    @Override
    public void resume() throws AxisFault {
        //throw new AxisFault("Method not implemented: resume");
        // TODO
    }

    @Override
    public void maintenenceShutdown(long milliSeconds) throws AxisFault {
       // throw new AxisFault("Method not implemented: maintenceShutdown");
        // TODO
    }

	@Override
    protected void doInit() throws AxisFault {
		MSMQConnectionManager.init(getTransportInDescription(),ConnectionType.RECIVER);
    }

	
}
