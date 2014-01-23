/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.osgi.tx;

import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.osgi.framework.BundleContext;

/**
 * Default HttpListener that synch with the underlying listerner frameworks.
 * This implemenation uses org.osgi.service.http.port property to find the port.
 * <p/>
 * At the moment this will assume the underlying evn is Equinox. if the prior property is not set
 * this will default to 80.
 */
public class HttpListener implements TransportListener {

    private BundleContext context;

    private ConfigurationContext configCtx;

    public HttpListener(BundleContext context) {
        this.context = context;
    }

    public void init(ConfigurationContext configCtx, TransportInDescription transprtIn)
            throws AxisFault {
        this.configCtx = configCtx;
    }

    public void start() throws AxisFault {
        //ignore
    }

    public void stop() throws AxisFault {
        //ignore
    }

    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return calculateEndpoint("http", serviceName, ip);
    }

    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        return new EndpointReference[]{calculateEndpoint("http", serviceName, ip)};
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        //ignore for the moment. This should be able to take the HttpService and attached to it.
        return null;
    }

    public void destroy() {

    }

    private EndpointReference calculateEndpoint(String protocol, String serviceName, String ip) {
        String portS = context.getProperty("org.osgi.service.http.port");
        int port = 80;
        if (portS != null && portS.length() != 0) {
            try {
                port = Integer.parseInt(portS);
            } catch (NumberFormatException e) {//ignore
            }
        }
        String servicePath = configCtx.getServicePath();
        if (servicePath.startsWith("/")) {
            servicePath = servicePath.substring(1);
        }
        String contextRoot = configCtx.getContextRoot();
        if (contextRoot.equals("/") || contextRoot == null) {
            contextRoot = ""; 
        }
        return new EndpointReference(protocol + "://" + ip + ":" + port + contextRoot + "/" +
                                     servicePath + "/" + serviceName + "/");
    }
}
