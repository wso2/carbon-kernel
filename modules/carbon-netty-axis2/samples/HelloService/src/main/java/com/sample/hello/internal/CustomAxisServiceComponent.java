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
package com.sample.hello.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service component to consume CarbonTransport instance which has been registered as an OSGi service
 * by Carbon Kernel.
 *
 * @since 1.0.0
 */
@Component(
        name = "CustomAxisServiceComponent",
        immediate = true
)
public class CustomAxisServiceComponent {
    private static final Logger logger = LoggerFactory.getLogger(CustomAxisServiceComponent.class);

    /**
     * This is the activation method of CustomAxisServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        logger.info("CustomAxisServiceComponent is activated");

        try {
            AxisService axisService = AxisService.createService("com.sample.hello.HelloService",
                    DataHolder.getInstance().getConfigurationContext().getAxisConfiguration());
            bundleContext.registerService(AxisService.class, axisService, null);
        } catch (AxisFault axisFault) {
            logger.error("Failed to create Axis2 service", axisFault);
        }

    }

    /**
     * This is the deactivation method of CustomAxisServiceComponent. This will be called when this component
     * is being stopped or references are un-satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        logger.info("CustomAxisServiceComponent is deactivated");
    }

    @Reference(
            name = "axis2-config-context-service",
            service = ConfigurationContext.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonAxis2Service"
    )
    protected void setCarbonAxis2Service(ConfigurationContext configurationContext) {
        DataHolder.getInstance().setConfigurationContext(configurationContext);
    }

    protected void unsetCarbonAxis2Service(ConfigurationContext configurationContext) {
        DataHolder.getInstance().setConfigurationContext(null);
    }
}
