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
package org.wso2.carbon.sample.transport.mgt;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.sample.startuporder.OrderResolverMonitor;


/**
 * Sample Transport Services Listener class.
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.sample.transport.mgt.TransportServicesListener",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = {
                "capability-name=org.wso2.carbon.sample.transport.mgt.Transport",
                "component-key=carbon-sample-transport-mgt"
        }
)
public class TransportServicesListener implements RequiredCapabilityListener {
    private static final Logger logger = LoggerFactory.getLogger(TransportServicesListener.class);
    private BundleContext bundleContext;

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        logger.info("All required services are available for : " + this.getClass().getName());
        bundleContext.registerService(TransportManager.class, new TransportManager(), null);
        OrderResolverMonitor.getInstance().listenerInvoked(TransportServicesListener.class.getName());
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Deactivate
    public void deactivate(BundleContext bundleContext) {

    }
}
