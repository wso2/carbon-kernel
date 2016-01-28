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
package org.wso2.carbon.axis2.runtime.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ListenerManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.Axis2ServiceManager;
import org.wso2.carbon.axis2.runtime.bridge.CarbonAxis2Bridge;
import org.wso2.carbon.messaging.CarbonMessageProcessor;

/**
 * Service component to consume CarbonTransport instance which has been registered as an OSGi service
 * by Carbon Kernel.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.axis2.runtime.internal.Axis2NettyInitializerComponent",
        immediate = true
)
public class Axis2NettyInitializerComponent {
    private static final Logger logger = LoggerFactory.getLogger(Axis2NettyInitializerComponent.class);
    private static final String CHANNEL_ID_KEY = "channel.id";

    /**
     * This is the activation method of Axis2NettyInitializerComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        logger.info("Axis2NettyInitializerComponent is activated");

        ListenerManager listenerManager = new ListenerManager();
        listenerManager.init(DataHolder.getInstance().getConfigurationContext());
        listenerManager.start();

        bundleContext.registerService(CarbonMessageProcessor.class, new Axis2CarbonMessageProcessor(), null);
    }

    /**
     * This is the deactivation method of Axis2NettyInitializerComponent. This will be called when this component
     * is being stopped or references are un-satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        logger.info("Axis2NettyInitializerComponent is deactivated");
    }

    @Reference(
            name = "axis2-service-manager",
            service = Axis2ServiceManager.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeAxis2ServiceManager"
    )
    protected void addAxis2ServiceManager(Axis2ServiceManager axis2ServiceManager) {
        ConfigurationContext configurationContext = DataHolder.getInstance().getConfigurationContext();
        if (configurationContext != null) {
            try {
                AxisService axisService = axis2ServiceManager.getAxisService();
                configurationContext.deployService(axisService);
            } catch (AxisFault axisFault) {
                logger.error("Failed to create an Axis2 service with given class '{}'",
                        axis2ServiceManager.getImplementationClass().getName(), axisFault);
            }
        }
    }

    protected void removeAxis2ServiceManager(Axis2ServiceManager axis2ServiceManager) {
        //TODO: Unregister service group + service form the Configuration Context
    }

    @Reference(
            name = "carbon-axis2-bridge",
            service = CarbonAxis2Bridge.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonAxis2Bridge"
    )
    protected void addCarbonAxis2Bridge(CarbonAxis2Bridge carbonAxis2Bridge) {
        logger.info("======================addCarbonAxis2Bridge");
    }

    protected void removeCarbonAxis2Bridge(CarbonAxis2Bridge carbonAxis2Bridge) {
        logger.info("======================removeCarbonAxis2Bridge");
    }
}
