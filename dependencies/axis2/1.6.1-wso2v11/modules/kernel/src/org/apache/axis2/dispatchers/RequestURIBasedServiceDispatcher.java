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

package org.apache.axis2.dispatchers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class RequestURIBasedServiceDispatcher extends AbstractServiceDispatcher {

    public static final String NAME = "RequestURIBasedServiceDispatcher";
    private static final Log log = LogFactory.getLog(RequestURIBasedServiceDispatcher.class);

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();
        if (toEPR != null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(messageContext.getLogIDString() +
                        " Checking for Service using target endpoint address : " +
                        toEPR.getAddress());
            }
            String filePart = toEPR.getAddress();
            ConfigurationContext configurationContext = messageContext.getConfigurationContext();

            //Get the service/operation part from the request URL
            String serviceOpPart = Utils.getServiceAndOperationPart(filePart,
                    messageContext.getConfigurationContext().getServiceContextPath());

            if (serviceOpPart != null) {
            	
                AxisConfiguration registry =
                        configurationContext.getAxisConfiguration();

                /**
                 * Split the serviceOpPart from '/' and add part by part and check whether we have
                 * a service. This is because we are supporting hierarchical services. We can't
                 * decide the service name just by looking at the request URL.
                 */
                AxisService axisService = null;
                String[] parts = serviceOpPart.split("/");
                String serviceName = "";
                int count = 0;

                /**
                 * To avoid performance issues if an incorrect URL comes in with a long service name
                 * including lots of '/' separated strings, we limit the hierarchical depth to 10
                 */
                while (axisService == null && count < parts.length &&
                        count < Constants.MAX_HIERARCHICAL_DEPTH) {
                    serviceName = count == 0 ? serviceName + parts[count] :
                            serviceName + "/" + parts[count];
                    axisService = registry.getService(serviceName);
                    count++;
                }

                // If the axisService is not null we get the binding that the request came to add
                // add it as a property to the messageContext
                if (axisService != null) {
                    Map endpoints = axisService.getEndpoints();
                    if (endpoints != null) {
                        if (endpoints.size() == 1) {
                            messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME,
                                    endpoints.get(
            								axisService.getEndpointName()));
            			} else {
                            String[] temp = serviceName.split("/");
                            int periodIndex = temp[temp.length - 1].lastIndexOf('.');
                            if (periodIndex != -1) {
                                String endpointName
                                        = temp[temp.length - 1].substring(periodIndex + 1);
                                messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME,
                                        endpoints.get(endpointName));
                            } else {
                                 messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME,
                                        endpoints.get(axisService.getEndpointName()));
                            }
            			}
            		}
            	}

            	return axisService;
            } else {
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug(messageContext.getLogIDString() +
                            " Attempted to check for Service using target endpoint URI, but the service fragment was missing");
                }
                return null;
            }
        } else {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(messageContext.getLogIDString() +
                        " Attempted to check for Service using null target endpoint URI");
            }
            return null;
        }
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
