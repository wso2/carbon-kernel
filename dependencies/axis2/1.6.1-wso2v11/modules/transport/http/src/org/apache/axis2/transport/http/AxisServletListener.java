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

package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link TransportListener} implementation for {@link AxisServlet}. There will be one instance
 * of this class for each protocol (HTTP and/or HTTPS) accepted by the servlet.
 */
public class AxisServletListener implements TransportListener {

    private static final Log log = LogFactory.getLog(AxisServletListener.class);
    
    /**
     * The URL scheme which can be either <tt>http</tt> or <tt>https</tt>. This is the same as the
     * transport name.
     */
    private String scheme;
    
    /**
     * The port number. <code>-1</code> means that the port number will be autodetected.
     */
    private int port;
    
    private ConfigurationContext configurationContext;
    private TransportInDescription transportInDescription;

    public void init(ConfigurationContext configurationContext,
                     TransportInDescription transportInDescription) throws AxisFault {
        this.configurationContext = configurationContext;
        this.transportInDescription = transportInDescription;
        scheme = transportInDescription.getName();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new AxisFault(AxisServletListener.class.getName() + " can only be used for http or https");
        }
        Parameter param = transportInDescription.getParameter(PARAM_PORT);
        if (param != null) {
            try {
                port = Integer.parseInt((String) param.getValue());
            } catch (NumberFormatException ex) {
                throw new AxisFault("Invalid port number");
            }
        } else {
            port = -1;
        }
    }

    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }

    public void start() throws AxisFault {
    }

    public void stop() throws AxisFault {
    }

    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        if (port == -1) {
            throw new AxisFault("Port number for transport " + scheme + " has not yet been detected");
        }
        return HTTPTransportUtils.getEPRsForService(configurationContext, transportInDescription,
                serviceName, ip, port);
    }

    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return getEPRsForService(serviceName, ip)[0];
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        HttpServletRequest req = (HttpServletRequest) messageContext.getProperty(
                HTTPConstants.MC_HTTP_SERVLETREQUEST);
        SessionContext sessionContext =
                (SessionContext) req.getSession(true).getAttribute(
                        Constants.SESSION_CONTEXT_PROPERTY);
        String sessionId = req.getSession().getId();
        if (sessionContext == null) {
            sessionContext = new SessionContext(null);
            sessionContext.setCookieID(sessionId);
            req.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,
                                          sessionContext);
        }
        messageContext.setSessionContext(sessionContext);
        messageContext.setProperty(AxisServlet.SESSION_ID, sessionId);
        return sessionContext;
    }

    public void destroy() {
    }

}
