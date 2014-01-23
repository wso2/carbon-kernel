/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.clustering;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.handlers.AbstractHandler;

/**
 *
 */
public class RequestBlockingHandler extends AbstractHandler {
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        // Handle blocking at gobal level
        ConfigurationContext cfgCtx = msgContext.getConfigurationContext();
        Boolean isBlockingAllRequests =
                (Boolean) cfgCtx.getProperty(ClusteringConstants.BLOCK_ALL_REQUESTS);
        AxisServiceGroup serviceGroup = msgContext.getAxisServiceGroup();

        // Handle blocking at service group level
        Boolean isBlockingServiceGroupRequests = Boolean.FALSE;
        if (serviceGroup != null) {
            Parameter blockingParam =
                    serviceGroup.getParameter(ClusteringConstants.BLOCK_ALL_REQUESTS);
            if (blockingParam != null) {
                isBlockingServiceGroupRequests = (Boolean) blockingParam.getValue();
            }
        }

        // Handle blocking at service level
        AxisService service = msgContext.getAxisService();
        Boolean isBlockingServiceRequests = Boolean.FALSE;
        if (service != null) {
            Parameter blockingParam =
                    service.getParameter(ClusteringConstants.BLOCK_ALL_REQUESTS);
            if (blockingParam != null) {
                isBlockingServiceRequests = (Boolean) blockingParam.getValue();
            }
        }

        if (isBlockingAllRequests != null && isBlockingAllRequests.booleanValue()) {

            // Allow only NodeManager service commit requests to pass through. Block all others
            AxisService axisService = msgContext.getAxisService();
            if (!axisService.getName().equals(ClusteringConstants.NODE_MANAGER_SERVICE)) {
                if (!msgContext.getAxisOperation().getName().getLocalPart().equals("commit")) {
                    throw new AxisFault("System is being reinitialized. " +
                                        "Please try again in a few seconds.");
                } else {
                    throw new AxisFault("NodeManager service cannot call any other " +
                                        "operation after calling prepare");
                }
            }
        } else if (isBlockingServiceGroupRequests.booleanValue()) {
            throw new AxisFault("This service group is being initialized or unloaded. " +
                                "Please try again in a few seconds.");
        } else if (isBlockingServiceRequests.booleanValue()) {
            throw new AxisFault("This service is being initialized. " +
                                "Please try again in a few seconds.");
        }
        return InvocationResponse.CONTINUE;
    }


    public boolean equals(Object obj) {
        if(obj instanceof RequestBlockingHandler){
            RequestBlockingHandler that = (RequestBlockingHandler) obj;
            HandlerDescription thisDesc = this.getHandlerDesc();
            HandlerDescription thatDesc = that.getHandlerDesc();
            if(thisDesc != null && thatDesc != null && thisDesc.getName().equals(thatDesc.getName())){
                return true;
            }
        }
        return false;
    }


    public int hashCode() {
        if(this.handlerDesc != null){
            return this.handlerDesc.hashCode();
        }
        return super.hashCode();
    }
}
