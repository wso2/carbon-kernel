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
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.util.Hashtable;

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
            name = "carbon-transport",
            service = CarbonTransport.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonTransport"
    )
    protected void addCarbonTransport(CarbonTransport carbonTransport) {
        if ("axis2-http".equals(carbonTransport.getId())) {
            DataHolder.getInstance().setCarbonTransport(carbonTransport);
            Hashtable<String, String> httpInitParams = new Hashtable<>();
            httpInitParams.put(CHANNEL_ID_KEY, carbonTransport.getId());
            Axis2NettyInitializer axis2NettyInitializer = new Axis2NettyInitializer();

            BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
            if (bundleContext != null) {
                bundleContext.registerService(CarbonNettyServerInitializer.class,
                        axis2NettyInitializer, httpInitParams);
            }
        }
    }

    protected void removeCarbonTransport(CarbonTransport carbonTransport) {
        if ("axis2-http".equals(carbonTransport.getId())) {
            //TODO : un-initialize the the netty transport
            DataHolder.getInstance().setCarbonTransport(null);
        }
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
                AxisService axisService = AxisService.createService(axis2ServiceManager
                        .getImplementationClass().getName(), configurationContext.getAxisConfiguration());
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
}
