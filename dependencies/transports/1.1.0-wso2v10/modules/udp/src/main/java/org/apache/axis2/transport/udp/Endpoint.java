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
package org.apache.axis2.transport.udp;

import java.net.SocketException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.datagram.DatagramEndpoint;
import org.apache.axis2.util.Utils;

/**
 * UDP endpoint description.
 */
public class Endpoint extends DatagramEndpoint {
    private int port;
    private int maxPacketSize;
    
    public int getPort() {
        return port;
    }
    
	public int getMaxPacketSize() {
        return maxPacketSize;
    }

    @Override
    public String getDescription() {
        return "*:" + port;
    }

    @Override
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
        port = ParamUtils.getOptionalParamInt(params, UDPConstants.PORT_KEY, -1);
        if (port == -1) {
            return false;
        }
        maxPacketSize = ParamUtils.getOptionalParamInt(params, UDPConstants.MAX_PACKET_SIZE_KEY,
                UDPConstants.DEFAULT_MAX_PACKET_SIZE);
        return super.loadConfiguration(params);
    }

	@Override
    public EndpointReference[] getEndpointReferences(AxisService service, String ip) throws AxisFault {
	    if (ip == null) {
	        try {
	            ip = Utils.getIpAddress(getListener().getConfigurationContext().getAxisConfiguration());
	        } catch (SocketException ex) {
	            throw new AxisFault("Unable to determine the host's IP address", ex);
	        }
	    }
	    StringBuilder epr = new StringBuilder("udp://");
	    epr.append(ip);
	    epr.append(':');
	    epr.append(getPort());
	    // If messages are predispatched to a service, then WS-Addressing will be used and we
	    // need to include the service path in the EPR.
	    if (getService() == null) {
	        epr.append('/');
	        epr.append(getConfigurationContext().getServiceContextPath());
            epr.append('/');
	        epr.append(service.getName());
	    }
	    epr.append("?contentType=");
	    epr.append(getContentType());
        return new EndpointReference[] { new EndpointReference(epr.toString()) };
    }
}
