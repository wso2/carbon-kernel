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

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.logging.correlation.mgt.CorrelationLogConfig;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogConstants;
import org.wso2.carbon.logging.correlation.CorrelationLogService;
import org.wso2.carbon.logging.correlation.ConfigObserver;
import org.wso2.carbon.utils.MBeanRegistrar;
import org.wso2.carbon.utils.xml.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The correlation log manager class. This class reads configs from the Carbon.xml and the JMX endpoint and manage all
 * service implementation with current configurations.
 */
@Component(immediate = true)
public class CorrelationLogManager implements ConfigObserver {
    private Map<String, CorrelationLogService> serviceMap = new HashMap<>();
    private Map<String, Object> properties;
    private CorrelationLogConfig config;
    private boolean isDefaultLoaded = false;

    private ServerConfigurationService serverConfigurationService;

    public CorrelationLogManager() {
        // Register the MBean required for JMX. The notifier will be invoked once each field updated via JMX.
        config = new CorrelationLogConfig();
        config.registerObserver(this);
        MBeanRegistrar.registerMBean(config);
    }

    @Activate
    protected void activate(ComponentContext context) {
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        serviceMap = null;
    }

    /**
     * Get references of the correlation log server implementations.
     *
     * @param service
     */
    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MULTIPLE,
            unbind = "unsetCorrelationLogService"
    )
    protected void setCorrelationLogService(CorrelationLogService service) {
        serviceMap.put(service.getName(), service);
        // If the defaults are already loaded, configure the current implementation with configs. If not they will be
        // configured once the defaults are loaded.
        if (isDefaultLoaded) {
            service.reconfigure(properties);
        }
    }

    protected void unsetCorrelationLogService(CorrelationLogService service) {
        serviceMap.remove(service.getName());
    }

    /**
     * Get the reference of the server configuration service.
     *
     * @param serverConfigurationService
     */
    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MANDATORY,
            unbind = "unsetServerConfigurationService"
    )
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
        loadDefaults();
        // Configure all referenced service implementations with the defaults.
        configureServiceImpls();
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = null;
    }

    /**
     * Accepts the field name and the new value on all the configuration changes via the JMX endpoint.
     *
     * @param key Field name
     * @param value New value
     */
    @Override
    public void notify(String key, Object value) {
        // Update the properties map and configure each service implementation with updated values.
        properties.put(key, value);
        configureServiceImpls();
    }

    /**
     * Iterate and configure all the referenced service implementations.
     */
    private void configureServiceImpls() {
        // Notify each service implementation with configurations.
        for (Map.Entry<String, CorrelationLogService> serviceEntry : serviceMap.entrySet()) {
            serviceEntry.getValue().reconfigure(properties);
        }
    }

    /**
     * Load default configurations from the Carbon.xml file.
     */
    private void loadDefaults() {
        properties = new HashMap<>();
        // Enable
        String enabled = this.serverConfigurationService.getFirstProperty(CorrelationLogConstants.CONFIG_ENABLE);
        properties.put(CorrelationLogConstants.ENABLE, !StringUtils.isEmpty(enabled) && Boolean.parseBoolean(enabled));
        // Components
        String components = this.serverConfigurationService.getFirstProperty(CorrelationLogConstants.CONFIG_COMPONENTS);
        properties.put(CorrelationLogConstants.COMPONENTS, !StringUtils.isEmpty(components) ? components : "");
        // Blacklisted threads
        String blacklistedThreads = this.serverConfigurationService
                .getFirstProperty(CorrelationLogConstants.CONFIG_BLACKLISTED_THREADS);
        properties.put(CorrelationLogConstants.BLACKLISTED_THREADS,
                !StringUtils.isEmpty(blacklistedThreads) ? blacklistedThreads : "");
        // Log all methods
        String logAllMethods = this.serverConfigurationService
                .getFirstProperty(CorrelationLogConstants.CONFIG_LOG_ALL_METHODS);
        properties.put(CorrelationLogConstants.LOG_ALL_METHODS,
                !StringUtils.isEmpty(logAllMethods) && Boolean.parseBoolean(logAllMethods));
        isDefaultLoaded = true;
    }
}
