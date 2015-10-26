/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.internal.transports;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.carbon.kernel.transports.TransportManager;

import java.util.Map;

/**
 * OSGi declarative services component which handled registration & uregistration of Carbon transports.
 */
@Component(
        name = "org.wso2.carbon.kernel.internal.transports.TransportServiceComponent",
        immediate = true
)
public class TransportServiceComponent {

    private TransportManager transportManager = new TransportManager();

    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(TransportManager.class, transportManager, null);

        // Registering transport management command provider implementation. This allows users to manage
        // transports via the OSGi console.
        bundleContext.registerService(CommandProvider.class.getName(),
                new TransportMgtCommandProvider(transportManager), null);
    }

    @Reference(
            name = "carbon.transport",
            service = CarbonTransport.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterTransport"
    )
    protected void registerTransport(CarbonTransport transport, Map<String, ?> ref) {
        transportManager.registerTransport(transport);
    }

    protected void unregisterTransport(CarbonTransport transport, Map<String, ?> ref) {
        transportManager.unregisterTransport(transport);
    }
}
