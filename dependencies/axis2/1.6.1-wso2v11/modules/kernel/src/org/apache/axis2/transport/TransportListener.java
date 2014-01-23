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


package org.apache.axis2.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;

/**
 * Class TransportListener
 */
public interface TransportListener {

    String PARAM_PORT = "port";
    String HOST_ADDRESS = "hostname";

    void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
            throws AxisFault;

    void start() throws AxisFault;

    void stop() throws AxisFault;

    /**
     * @param serviceName
     * @param ip
     * @throws AxisFault
     * @deprecated Transport listener can expose more than EPRs. So this method should return an array of EPRs.
     *             Deprecating this method for now and please use getEPRsForServices instead.
     */
    EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault;

    /**
     * Get the endpoint references for a given service. These are the addresses that a client
     * can use to send requests to the given service through this transport.
     * 
     * @param serviceName TODO: this is actually not simply the service name!
     * @param ip The host name or IP address of the local host. The implementation should use
     *           this information instead of {@link java.net.InetAddress#getLocalHost()}.
     *           The value of this parameter may be <code>null</code>, in which case the
     *           implementation should use {@link org.apache.axis2.util.Utils#getIpAddress(
     *           org.apache.axis2.engine.AxisConfiguration)}.
     * @return an array of endpoint references for the given service
     * @throws AxisFault
     */
    EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault;

    /**
     * To get the sessionContext transport dependent manner. So that transport listener
     * can return its own implementation of session managment
     *
     * @param messageContext : MessageContext which has all the relavent data
     * @return SessionContext
     */
    SessionContext getSessionContext(MessageContext messageContext);
    
    void destroy();
}
