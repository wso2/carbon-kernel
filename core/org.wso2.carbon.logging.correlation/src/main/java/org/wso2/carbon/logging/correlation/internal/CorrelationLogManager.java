/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.logging.correlation.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigurable;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigurator;
import org.wso2.carbon.logging.correlation.bean.CorrelationLogComponentConfig;
import org.wso2.carbon.logging.correlation.bean.CorrelationLogConfig;
import org.wso2.carbon.logging.correlation.bean.ImmutableCorrelationLogConfig;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogConstants;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogHolder;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogUtil;

/**
 * The correlation log manager class. This class reads correlation log configurations from the Carbon.xml file and the
 * JMX endpoint and manage all service implementation by dispatching the configurations..
 */
@Component(
        immediate = true,
        service = CorrelationLogConfigurator.class)
public class CorrelationLogManager implements CorrelationLogConfigurator {
    private static Log log = LogFactory.getLog(CorrelationLogManager.class);

    private CorrelationLogConfig config;
    private boolean systemEnableCorrelationLogs;

    public CorrelationLogManager() {
        // Load root configurations from the carbon.xml file.
        this.config = loadRootConfigurations();
    }

    @Activate
    protected void activate(ComponentContext context) {
        log.debug("CorrelationLogManager component activated.");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("CorrelationLogManager component deactivated.");
    }

    /**
     * This invokes when a new service implementation is loaded into the runtime.
     *
     * @param service
     */
    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MULTIPLE,
            unbind = "unsetCorrelationLogService"
    )
    protected void setCorrelationLogService(CorrelationLogConfigurable service) {
        log.debug("Get reference of service implementation '" + service.getName() + "'");
        // Load component-specific configurations from the carbon.xml file.
        config.getComponentConfigs()
                .put(service.getName(), loadComponentSpecificConfigs(service.getName()));
        // Create an immutable configuration object and send it to the service.
        service.onConfigure(getComponentSpecificConfiguration(service.getName()));
        CorrelationLogHolder.getInstance().addCorrelationLogConfigurableService(service);
    }

    protected void unsetCorrelationLogService(CorrelationLogConfigurable service) {
        // Remove configuration of the service.
        config.getComponentConfigs().remove(service.getName());
        CorrelationLogHolder.getInstance().removeCorrelationLogConfigurableService(service.getName());
    }

    /**
     * Returns a clone of the <code>CorrelationLogConfig</code> object.
     *
     * @return
     */
    @Override
    public CorrelationLogConfig getConfiguration() {
        return config.clone();
    }

    /**
     * Override the current <code>CorrelationLogConfig</code> object with new configurations.
     *
     * @param correlationLogConfig
     */
    @Override
    public void updateConfiguration(CorrelationLogConfig correlationLogConfig) {
        log.debug("Correlation log configurations are modified.");
        this.config = correlationLogConfig;
    }

    /**
     * Returns root configuration from carbon.xml file.
     *
     * @return
     */
    private CorrelationLogConfig loadRootConfigurations() {
        boolean enable = Boolean.parseBoolean(
                ServerConfiguration.getInstance().getFirstProperty(CorrelationLogConstants.CONFIG_PATH_ENABLE));
        String[] components = CorrelationLogUtil.toArray(
                ServerConfiguration.getInstance().getFirstProperty(CorrelationLogConstants.CONFIG_PATH_COMPONENTS));
        String deniedThreadsList =
                ServerConfiguration.getInstance().getFirstProperty(CorrelationLogConstants.CONFIG_PATH_DENIED_THREADS);
        String[] deniedThreads = deniedThreadsList != null ?
                CorrelationLogUtil.toArray(deniedThreadsList) : CorrelationLogConstants.DEFAULT_DENIED_THREADS;

        boolean systemEnable = Boolean.parseBoolean(
                System.getProperty(CorrelationLogConstants.CORRELATION_LOGS_SYS_PROPERTY));
        if (systemEnable) {
            enable = true;
            systemEnableCorrelationLogs = true;
            String systemDeniedThreads = System.getProperty(CorrelationLogConstants.DENIED_THREADS_SYS_PROPERTY);
            deniedThreads = (systemDeniedThreads != null) ?
                    CorrelationLogUtil.toArray(systemDeniedThreads) : CorrelationLogConstants.DEFAULT_DENIED_THREADS;
            log.debug("Correlation log configuration enabled from the System parameter");
            CorrelationLogHolder.getInstance().setSystemEnabledCorrelationLogs(true);
        } else {
            log.debug("Correlation log configurations are loaded from the carbon.xml file.");
        }
        return new CorrelationLogConfig(enable, components, deniedThreads);
    }

    /**
     * Returns component-specific configuration from the carbon.xml file.
     *
     * @param componentName Component name
     * @return
     */
    private CorrelationLogComponentConfig loadComponentSpecificConfigs(String componentName) {
        String path = CorrelationLogConstants.CONFIG_PATH_COMPONENT_CONFIGS + "." + componentName + ".logAllMethods";
        boolean logAllMethods = Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty(path));
        log.debug("Component-specific configurations for '" + componentName + "' loaded from the carbon.xml file.");
        return new CorrelationLogComponentConfig(logAllMethods);
    }

    /**
     * Returns component-specific configuration from the configuration object.
     * @param componentName
     * @return
     */
    private ImmutableCorrelationLogConfig getComponentSpecificConfiguration(String componentName) {
        // Build component specific immutable configuration object
        return new ImmutableCorrelationLogConfig(
                systemEnableCorrelationLogs || (config.isEnable() &&
                        CorrelationLogUtil.isComponentAllowed(componentName, config.getComponents())),
                config.getDeniedThreads(),
                config.getComponentConfigs().get(componentName).isLogAllMethods());
    }
}
