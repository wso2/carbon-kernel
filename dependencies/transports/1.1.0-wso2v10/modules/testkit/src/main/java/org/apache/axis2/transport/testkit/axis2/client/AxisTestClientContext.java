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

package org.apache.axis2.transport.testkit.axis2.client;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.CustomAxisConfigurator;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.testkit.axis2.TransportDescriptionFactory;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;

/**
 * Resource maintaining the {@link ConfigurationContext} for {@link AxisTestClient}
 * instances.
 * <p>
 * Dependencies:
 * <dl>
 *   <dt>{@link TransportDescriptionFactory} (1)</dt>
 *   <dd>Used to create transport descriptions.</dd>
 *   <dt>{@link AxisTestClientContextConfigurator} (0..*)</dt>
 *   <dd>Used to determine whether a transport listener is required and to
 *       configure the transport.</dd>
 * </dl>
 */
public class AxisTestClientContext {
    public static final AxisTestClientContext INSTANCE = new AxisTestClientContext();
    
    private @Transient TransportSender sender;
    private @Transient ConfigurationContext cfgCtx;
    private @Transient ListenerManager listenerManager;
    
    private AxisTestClientContext() {}
    
    @Setup @SuppressWarnings("unused")
    private void setUp(TransportDescriptionFactory tdf, AxisTestClientContextConfigurator[] configurators) throws Exception {
        cfgCtx = ConfigurationContextFactory.createConfigurationContext(new CustomAxisConfigurator());
        AxisConfiguration axisCfg = cfgCtx.getAxisConfiguration();

        TransportOutDescription trpOutDesc = tdf.createTransportOutDescription();
        axisCfg.addTransportOut(trpOutDesc);
        sender = trpOutDesc.getSender();
        sender.init(cfgCtx, trpOutDesc);
        
        boolean useListener = false;
        for (AxisTestClientContextConfigurator configurator : configurators) {
            if (configurator.isTransportListenerRequired()) {
                useListener = true;
                break;
            }
        }
        
        TransportInDescription trpInDesc;
        if (useListener) {
            trpInDesc = tdf.createTransportInDescription();
        } else {
            trpInDesc = null;
        }
        
        for (AxisTestClientContextConfigurator configurator : configurators) {
            configurator.setupTransport(trpInDesc, trpOutDesc);
        }
        
        if (useListener) {
            listenerManager = new ListenerManager();
            listenerManager.init(cfgCtx);
            cfgCtx.setTransportManager(listenerManager);
            listenerManager.addListener(trpInDesc, false);
            listenerManager.start();
        }
    }
    
    public TransportSender getSender() {
        return sender;
    }

    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        sender.stop();
        if (listenerManager != null) {
            listenerManager.stop();
            listenerManager.destroy();
        }
        cfgCtx.terminate();
    }

    public ConfigurationContext getConfigurationContext() {
        return cfgCtx;
    }
}
