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
import org.wso2.carbon.logging.correlation.ConfigObserver;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigAttribute;
import org.wso2.carbon.logging.correlation.CorrelationLogService;
import org.wso2.carbon.logging.correlation.mgt.ConfigMapHolder;
import org.wso2.carbon.logging.correlation.mgt.CorrelationLogConfig;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogConstants;
import org.wso2.carbon.utils.MBeanRegistrar;

import java.util.HashMap;
import java.util.Map;

/**
 * The correlation log manager class. This class reads correlation log configurations from the Carbon.xml file and the
 * JMX endpoint and manage all service implementation by dispatching the configurations..
 */
@Component(immediate = true)
public class CorrelationLogManager implements ConfigObserver {
    private static Log log = LogFactory.getLog(CorrelationLogManager.class);

    private Map<String, CorrelationLogService> serviceMap = new HashMap<>();
    private String objectName = null;

    public CorrelationLogManager() {
        // Initializes the root configurations. These are shared/common for all the service implementations.
        CorrelationLogConfigAttribute[] attributes = new CorrelationLogConfigAttribute[] {
                new CorrelationLogConfigAttribute(
                        CorrelationLogConstants.ENABLE,
                        "Enable correlation logs", Boolean.class.getName(), false),
                new CorrelationLogConfigAttribute(CorrelationLogConstants.COMPONENTS,
                        "Components to enable logs", String.class.getName(), ""),
                new CorrelationLogConfigAttribute(CorrelationLogConstants.DENIED_THREADS,
                        "Threads which are ignored from correlation logs", String.class.getName(),
                        String.join(",", CorrelationLogConstants.DEFAULT_DENIED_THREADS))
        };

        ConfigMapHolder.getInstance().onConfigUpdated(this);
        createAndRegisterMBean(null, attributes);
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
        log.debug("Correlation log service '" + service + "' loaded.");
        serviceMap.put(service.getName(), service);
        if (service.getConfigDescriptor() != null) {
            createAndRegisterMBean(service.getName(), service.getConfigDescriptor());
        }
        // Assumption: The each service component only based on the root configs and its own configs.
        updateComponentConfig(service);
    }

    protected void unsetCorrelationLogService(CorrelationLogService service) {
        serviceMap.remove(service.getName());
    }

    /**
     * Notifies each service implementation once a configuration is changed in the config map.
     *
     * @param component Component name
     * @param key Field name
     * @param value New value
     */
    @Override
    public void configUpdated(String component, String key, Object value) {
        for (Map.Entry<String, CorrelationLogService> entry : serviceMap.entrySet()) {
            updateComponentConfig(entry.getValue());
        }
    }

    /**
     * Merges the root config with the component specific configs and notifies the service component.
     *
     * @param service
     */
    private void updateComponentConfig(CorrelationLogService service) {
        // Get root config, append component-wise configs on top of that and invoke the components.
        Map<String, Object> componentSpecificConfigMap = new HashMap<>();
        for (String component : new String[] { null, service.getName() }) {
            Map<String, Object> map = ConfigMapHolder.getInstance().getConfigMap(component);
            if (map != null) {
                componentSpecificConfigMap.putAll(map);
            }
        }
        service.reconfigure(componentSpecificConfigMap);
    }

    /**
     * Create a dynamic MBean based on the given attributes and register it. These MBeans are created for the root
     * component as well as other service service components.
     *
     * @param componentName Name of the component
     * @param attributes Attribute list
     */
    private void createAndRegisterMBean(String componentName, CorrelationLogConfigAttribute[] attributes) {
        // Create dynamic MBean via the given attributes. This will copy the defaults to the config map.
        CorrelationLogConfig mbean = new CorrelationLogConfig(componentName, attributes);
        // Override the defaults from the configs defined in the carbon.xml file.
        overrideConfigsFromFile(componentName, attributes);

        // Register MBean.
        try {
            String objectName = getObjectName();
            if (componentName != null) {
                objectName += ",name=" + componentName;
            }
            MBeanRegistrar.registerMBean(mbean, objectName);
            log.debug("Registered correlation log MBean for " + componentName + " component.");
        } catch (Exception e) {
            String msg = "Could not register Correlation Log " + componentName + " MBean";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Load default configurations from the Carbon.xml file.
     */
    private void overrideConfigsFromFile(String componentName, CorrelationLogConfigAttribute[] attributes) {
        log.debug("Overriding configurations from the carbon.xml file.");
        boolean isRootConfig = componentName == null;
        for (CorrelationLogConfigAttribute attr : attributes) {
            String path = CorrelationLogConstants.CONFIG_ROOT
                    + (isRootConfig ? "." : ".componentConfigs." + componentName + ".")
                    + attr.getName();
            String value = ServerConfiguration.getInstance().getFirstProperty(path);
            if (value != null) {
                // If value is null default will be effective.
                if (Boolean.class.getName().equals(attr.getType())) {
                    ConfigMapHolder.getInstance().setConfig(componentName, attr.getName(), Boolean.parseBoolean(value));
                } else {
                    ConfigMapHolder.getInstance().setConfig(componentName, attr.getName(), value);
                }
            }
        }
    }

    /**
     * Get object name for the MBean.
     *
     * @return The object name
     */
    private String getObjectName() {
        if (objectName == null) {
            String serverPackage = ServerConfiguration.getInstance().getFirstProperty("Package");
            if (serverPackage == null) {
                serverPackage = "wso2";
            }
            String className = CorrelationLogConfig.class.getName();
            if (className.indexOf('.') != -1) {
                className = className.substring(className.lastIndexOf('.') + 1);
            }
            objectName = serverPackage + ":type=" + className;
        }
        return objectName;
    }
}
