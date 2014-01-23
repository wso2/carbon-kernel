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

package org.apache.axis2.transport.testkit.axis2.endpoint;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.UtilsTransportServer;
import org.apache.axis2.transport.testkit.axis2.TransportDescriptionFactory;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;

/**
 * Resource maintaining the {@link ConfigurationContext} for {@link AxisTestEndpoint}
 * instances. This class provides the Axis2 server environment.
 * <p>
 * Dependencies:
 * <dl>
 *   <dt>{@link TransportDescriptionFactory} (1)</dt>
 *   <dd>Used to create transport descriptions.</dd>
 *   <dt>{@link AxisTestEndpointContextConfigurator} (0..*)</dt>
 *   <dd>Used to configure the transport.</dd>
 * </dl>
 */
public class AxisTestEndpointContext {
    public static final AxisTestEndpointContext INSTANCE = new AxisTestEndpointContext();
    
    private @Transient TransportInDescription trpInDesc;
    private @Transient UtilsTransportServer server;
    
    private AxisTestEndpointContext() {}
    
    @Setup @SuppressWarnings("unused")
    private void setUp(TransportDescriptionFactory tdf, AxisTestEndpointContextConfigurator[] configurators) throws Exception {
        
        server = new UtilsTransportServer();
        
        TransportOutDescription trpOutDesc = tdf.createTransportOutDescription();
        trpInDesc = tdf.createTransportInDescription();
        server.addTransport(trpInDesc, trpOutDesc);
        
        for (AxisTestEndpointContextConfigurator configurator : configurators) {
            configurator.setupTransport(trpInDesc, trpOutDesc);
        }
        
        ConfigurationContext cfgCtx = server.getConfigurationContext();
        
        cfgCtx.setContextRoot("/");
        cfgCtx.setServicePath("services");
        
        AxisConfiguration axisConfiguration = server.getAxisConfiguration();
        
        server.start();
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        server.stop();
    }
    
    public AxisConfiguration getAxisConfiguration() {
        return server.getAxisConfiguration();
    }

    public TransportListener getTransportListener() {
        return trpInDesc.getReceiver();
    }
    
    public String getTransportName() {
        return trpInDesc.getName();
    }

    public String getEPR(AxisService service) throws AxisFault {
        EndpointReference[] endpointReferences =
            trpInDesc.getReceiver().getEPRsForService(service.getName(), "localhost");
        return endpointReferences != null && endpointReferences.length > 0
                            ? endpointReferences[0].getAddress() : null;
    }
}
