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
package org.wso2.carbon.kernel.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;
import org.wso2.carbon.kernel.internal.config.YAMLBasedConfigProvider;
import org.wso2.carbon.kernel.internal.context.CarbonRuntimeFactory;
import org.wso2.carbon.kernel.internal.logging.LoggingConfiguration;

import java.util.Map;

/**
 * The core service component responsible for configuring logging framework and
 * initializing the carbon configuration.
 *
 * @since 5.0.0
 */

@Component(
        name = "org.wso2.carbon.kernel.internal.CarbonCoreDSComponent",
        immediate = true
)
public class CarbonCoreDSComponent {
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfiguration.class);
    private LoggingConfiguration loggingConfiguration = LoggingConfiguration.getInstance();

    /**
     * This is the activation method of CarbonCoreDSComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of the carbon core bundle used service registration, etc.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        // 1) Find to initialize the Carbon configuration provider
        CarbonConfigProvider configProvider = new YAMLBasedConfigProvider();

        // 2) Creates the CarbonRuntime instance using the Carbon configuration provider.
        CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);

        // 3) Register CarbonRuntime instance as an OSGi bundle.
        bundleContext.registerService(CarbonRuntime.class.getName(), carbonRuntime, null);
    }

    /**
     * This is the deactivation method of DeploymentEngineComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        DataHolder.getInstance().setBundleContext(null);
    }

    /**
     * The is a dependency of CarbonCoreDSComponent for ManagedService registration. This is the bind method
     * and it will be called when ManagedService instance is registered and it satisfy the policy defined.
     *
     * @param managedService the managedService instance used for configuring the logging framework
     * @param properties     the properties of the ManagedService service registration used for checking the service.pid
     */
    @Reference(
            name = "config.admin.managed.service",
            service = ManagedService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterLoggingConfig"
    )
    protected void registerLoggingConfig(ManagedService managedService, Map<String, ?> properties) {
        String pid = (String) properties.get(Constants.SERVICE_PID);
        // check if the service is the one that should be configured
        if ((pid == null) || (!Constants.LOGGING_CONFIG_PID.equals(pid))) {
            return;
        }
        try {
            loggingConfiguration.register(managedService);
        } catch (Throwable e) {
            logger.error("Cannot load logging configuration", e);
        }
    }

    /**
     * This is the unbind method for the above reference that gets called for ManagedService instance un-registrations.
     *
     * @param managedService the managedService instance that is un-registered. this is not used currently in
     *                       this method.
     */
    protected void unRegisterLoggingConfig(ManagedService managedService, Map<String, ?> properties) {
        String pid = (String) properties.get(Constants.SERVICE_PID);
        // check for logging config service pid
        if ((pid == null) || (!Constants.LOGGING_CONFIG_PID.equals(pid))) {
            return;
        }
        loggingConfiguration.unregister(managedService);
    }
}
