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
package org.apache.axis2.transport.base;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.WSDL2Constants;

/**
 * Describes a protocol specific endpoint. This might be a TCP/UDP port, a mail account,
 * a JMS destination, etc. Typically, a protocol specific endpoint is mapped to a
 * service.
 */
public abstract class ProtocolEndpoint {
    private AbstractTransportListenerEx<?> listener;
    /** Axis2 service */
    private AxisService service;
    
    // This is called only by AbstractTransportListenerEx and must have package access
    void init(AbstractTransportListenerEx<?> listener, AxisService service) {
        this.listener = listener;
        this.service = service;
    }
    
    public final AbstractTransportListenerEx<?> getListener() {
        return listener;
    }

    public final AxisService getService() {
        return service;
    }

    /**
     * Get the name of the service to which messages received by this endpoint are pre-dispatched.
     * 
     * @return the name of the service, or <code>null</code> if message are not pre-dispatched
     */
    public final String getServiceName() {
        return service == null ? null : service.getName();
    }
    
    /**
     * Get the Axis2 configuration context. This is a convenience method that can be used by
     * subclasses to get the {@link ConfigurationContext} object from the listener.
     * 
     * @return the configuration context
     */
    protected final ConfigurationContext getConfigurationContext() {
        return listener.getConfigurationContext();
    }

    /**
     * Configure the endpoint based on the provided parameters.
     * If no relevant parameters are found, the implementation should
     * return <code>false</code>. An exception should only be thrown if there is an
     * error or inconsistency in the parameters.
     * 
     * @param params The source of the parameters to configure the
     *               endpoint. If the parameters are defined on
     *               a service, this will be an {@link AxisService}
     *               instance.
     * @return <code>true</code> if the parameters contained the required configuration
     *         information and the endpoint has been configured, <code>false</code> if
     *         the no configuration for the endpoint is present in the parameters
     * @throws AxisFault if configuration information is present, but there is an
     *         error or inconsistency in the parameters
     */
    public abstract boolean loadConfiguration(ParameterInclude params) throws AxisFault;
    
    /**
     * Get the endpoint references for this protocol endpoint.
     *
     * @param service The service to build the EPR for. If {@link #getService()} returns
     *                a non null value, then it has the same value as this parameter, which
     *                is never null.
     * @param ip The host name or IP address of the local host. The implementation should use
     *           this information instead of {@link java.net.InetAddress#getLocalHost()}.
     *           The value of this parameter may be <code>null</code>, in which case the
     *           implementation should use {@link org.apache.axis2.util.Utils#getIpAddress(
     *           org.apache.axis2.engine.AxisConfiguration)}.
     * @return an array of endpoint references
     * @throws AxisFault
     * 
     * @see org.apache.axis2.transport.TransportListener#getEPRsForService(String, String)
     */
    public abstract EndpointReference[] getEndpointReferences(AxisService service, String ip) throws AxisFault;

    /**
     * Get a short description of this endpoint suitable for inclusion in log messages.
     * 
     * @return a short description of the endpoint
     */
    // TODO: we should implement this method in all derived transports and make it abstract here
    public String getDescription() {
        return toString();
    }
    
    public MessageContext createMessageContext() throws AxisFault {
        MessageContext msgContext = listener.createMessageContext();
        
        if (service != null) {
            msgContext.setAxisService(service);
    
            // find the operation for the message, or default to one
            Parameter operationParam = service.getParameter(BaseConstants.OPERATION_PARAM);
            QName operationQName = (
                operationParam != null ?
                    BaseUtils.getQNameFromString(operationParam.getValue()) :
                    BaseConstants.DEFAULT_OPERATION);
    
            AxisOperation operation = service.getOperation(operationQName);
            if (operation != null) {
                msgContext.setAxisOperation(operation);
                msgContext.setAxisMessage(
                        operation.getMessage(WSDL2Constants.MESSAGE_LABEL_IN));
                msgContext.setSoapAction("urn:" + operation.getName().getLocalPart());
            }
        }
        return msgContext;
    }
}
