/*                                                                             
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.utils.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.SessionContextUtil;

import java.net.SocketException;

/**
 *
 */
public class GenericHttpTransportListener implements TransportListener {

    private ConfigurationContext configurationContext;
    private int port = -1;


	public GenericHttpTransportListener() {
	}

	public GenericHttpTransportListener(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void init(ConfigurationContext configContext,
                     TransportInDescription transportIn) throws AxisFault {
        this.configurationContext = configContext;
	    Parameter portParam = transportIn.getParameter("port");
	    if (portParam != null) {
		    this.port = Integer.parseInt(portParam.getValue().toString().trim());
	    }
    }

    public void start() throws AxisFault {
    }

    public void stop() throws AxisFault {
    }

    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        String serviceContextPath = configurationContext.getServiceContextPath();
        if (serviceContextPath == null) {
            throw new AxisFault(GenericHttpsTransportListener.class.getName() +
                                " Service Context path cannot be null");
        }
        return new EndpointReference("http://" + ip + ":" + port +
                                     serviceContextPath + "/" + serviceName);
    }

    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        try {
            if (ip == null) {
                ip = NetworkUtils.getLocalHostname();
            }
        } catch (SocketException e) {
            throw AxisFault.makeFault(e);
        }
        return new EndpointReference[]{getEPRForService(serviceName, ip)};
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        return SessionContextUtil.createSessionContext(messageContext);
    }

    public void destroy() {
        this.configurationContext = null;
    }
}
