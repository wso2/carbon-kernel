/*
 * Copyright 2015 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.kernel.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.transports.TransportManager;

import java.text.DecimalFormat;

/**
 * The Startup Listener service component will be notified after the server has completed startup finalization.
 * We expect that server has completed startup finalization after the TransportManager service has registered.
 *
 * @since 5.0.0
 */

@Component(
        name = "org.wso2.carbon.kernel.internal.CarbonCoreStartupListener",
        immediate = true
)
public class CarbonCoreStartupListener {
    private static final Logger logger = LoggerFactory.getLogger(CarbonCoreStartupListener.class);

    /**
     * This is the activation method of CarbonCoreStartupListener. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of the carbon core bundle used service registration, etc.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

        //Starting up all the transport means the server has completed startup finalization
        double startTime = Long.parseLong(System.getProperty(org.wso2.carbon.kernel.Constants.START_TIME));
        double startupTime = (System.currentTimeMillis() - startTime) / 1000;

        DecimalFormat decimalFormatter = new DecimalFormat("#,##0.000");
        logger.info("WSO2 Carbon started in " + Double.valueOf(decimalFormatter.format(startupTime)) + " sec");
    }

    /**
     * This is the deactivation method of CarbonCoreStartupListener. This will be called when this component
     * is being stopped or references are unsatisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
    }

    /**
     * The is a dependency of CarbonCoreStartupListener for TransportManager registration.
     *
     * @param transportManager the transportManager service instance
     */
    @Reference(
            name = "transport.manager.service",
            service = TransportManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterTransportManager"
    )
    protected void registerTransportManager(TransportManager transportManager) {

    }

    protected void unRegisterTransportManager(TransportManager transportManager) {

    }

}
