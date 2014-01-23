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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.AbstractTransportTest;
import org.apache.axis2.transport.CustomAxisConfigurator;
import org.apache.axis2.transport.UtilsTransportServer;

/**
 * Test case for {@link UDPListener} and {@link UDPSender}.
 */
public class UDPTest extends AbstractTransportTest {
    @Override
    protected UtilsTransportServer createServer() throws Exception {
        return new UtilsUDPServer();
    }
    
    public void testSoapOverUdpWithEchoService() throws Exception {
        Options options = new Options();
        options.setTo(new EndpointReference("udp://127.0.0.1:3333?contentType=text/xml+soap"));
        options.setAction(Constants.AXIS2_NAMESPACE_URI + "/echoOMElement");
        options.setUseSeparateListener(true);
        options.setTimeOutInMilliSeconds(Long.MAX_VALUE);

        ServiceClient serviceClient = new ServiceClient(getClientCfgCtx(), null);
        serviceClient.setOptions(options);
        // We need to set up the anonymous service Axis uses to get the response
        AxisService clientService = serviceClient.getServiceContext().getAxisService();
        clientService.addParameter(UDPConstants.PORT_KEY, 4444);
        clientService.addParameter(UDPConstants.CONTENT_TYPE_KEY, "text/xml+soap");
        OMElement response = serviceClient.sendReceive(createPayload());
        
        assertEchoResponse(response);
    }
    
    public ConfigurationContext getClientCfgCtx() throws Exception {
        ConfigurationContext cfgCtx =
            ConfigurationContextFactory.createConfigurationContext(new CustomAxisConfigurator());
        AxisConfiguration axisCfg = cfgCtx.getAxisConfiguration();
        axisCfg.engageModule("addressing");

        TransportInDescription trpInDesc = new TransportInDescription("udp");
        trpInDesc.setReceiver(new UDPListener());
        axisCfg.addTransportIn(trpInDesc);
        
        TransportOutDescription trpOutDesc = new TransportOutDescription("udp");
        trpOutDesc.setSender(new UDPSender());
        axisCfg.addTransportOut(trpOutDesc);
        
        return cfgCtx;
    }
}
