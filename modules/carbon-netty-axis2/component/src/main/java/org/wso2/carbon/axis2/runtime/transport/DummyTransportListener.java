/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.axis2.runtime.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.internal.DataHolder;
import org.wso2.carbon.kernel.transports.CarbonTransport;

import java.net.SocketException;

/**
 * This is DummyTransportListener.
 *
 * @since 1.0.0
 */
public class DummyTransportListener implements TransportListener {
    private static final Logger logger = LoggerFactory.getLogger(DummyTransportListener.class);
    private ConfigurationContext configurationContext;
    private TransportInDescription transportInDescription;
    private int port = -1;

    @Override
    public void init(ConfigurationContext configurationContext, TransportInDescription transportInDescription)
            throws AxisFault {
        this.configurationContext = configurationContext;
        this.transportInDescription = transportInDescription;
    }

    @Override
    public void start() throws AxisFault {
        logger.info("HttpCarbonAxis2TransportListener is started");
    }

    @Override
    public void stop() throws AxisFault {
        logger.info("HttpCarbonAxis2TransportListener is stopped");
    }

    @Override
    public EndpointReference[] getEPRsForService(String s, String s1) throws AxisFault {
        // TODO : This is a temporary fix
        if (s1.contains(":")) {
            port = Integer.parseInt(s1.substring(s1.indexOf(":") + 1));
        }
        return HTTPTransportUtils.getEPRsForService(configurationContext, transportInDescription, s, s1, port);
    }

    @Override
    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    @Override
    public void destroy() {

    }
}
