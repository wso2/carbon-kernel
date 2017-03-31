/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.internal.startupresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StartupServiceCache caches all the startup services against the component name given in the
 * ${@link org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener} and interface name of the services.
 *
 * @since 5.2.0
 */
public class StartupServiceCache {
    private static final Logger logger = LoggerFactory.getLogger(StartupServiceCache.class);

    private static StartupServiceCache serviceCacheInstance = new StartupServiceCache();

    /*
    The internal map contains interface name (OSGi service class) against a list of implementation objects. The outer
    map has the mapping between the component name and the internal map.
     */
    private Map<String, Map<String, List<Object>>> componentMap = new HashMap<>();

    public static StartupServiceCache getInstance() {
        return serviceCacheInstance;
    }

    private StartupServiceCache() {
    }

    /**
     * This method updates the StartupServiceCache with the provided information.
     *
     * @param componentName name of the reporting component
     * @param interfaceName name of the OSGi service interface
     * @param serviceInstance OSGi service instance
     */
    public void update(String componentName, Class interfaceName, Object serviceInstance) {
        logger.debug("Updating StartupServiceCache, componentName={}, interfaceName={}, serviceInstance={}",
                componentName, interfaceName.getName(), serviceInstance);

        synchronized (componentMap) {
            Map<String, List<Object>> componentServicesMap = componentMap.get(componentName);
            if (componentServicesMap == null) {
                logger.debug("Creating a Component Services Map for component {}", componentName);
                componentServicesMap = new HashMap<>();
                componentMap.put(componentName, componentServicesMap);
            }

            List<Object> services = componentServicesMap.get(interfaceName.getName());
            if (services == null) {
                logger.debug("Creating a Service Instance List for interface {} in component {}",
                        interfaceName, componentName);
                services = new ArrayList<>();
                componentServicesMap.put(interfaceName.getName(), services);
            }

            if (services.indexOf(serviceInstance) == -1) {
                services.add(serviceInstance);
            }
        }
    }

    /**
     * This method provides a map of OSGi services and service count for the given {@code componentName}
     *
     * @param componentName name of the reporter component
     * @return a list of reported OSGi service names
     */
    public Map<String, Long> getAvailableService(String componentName) {
        synchronized (componentMap) {
            Map<String, List<Object>> availableServices = componentMap.get(componentName);
            if (availableServices == null) {
                return Collections.emptyMap();
            }

            Map<String, Long> availableServiceCounts = new HashMap<>();
            for (Map.Entry<String, List<Object>> entry : availableServices.entrySet()) {
                availableServiceCounts.put(entry.getKey(), Long.valueOf(entry.getValue().size()));
            }
            return availableServiceCounts;
        }
    }
}
