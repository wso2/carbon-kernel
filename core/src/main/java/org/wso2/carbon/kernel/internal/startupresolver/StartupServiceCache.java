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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StartupServiceCache caches all the startup services against the component name.
 * Component name is taken from ${@link org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener}
 * and interface name of the services.
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
    private Map<String, Map<String, Long>> componentMap = new HashMap<>();

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
     */
    public void update(String componentName, Class interfaceName) {
        logger.debug("Updating StartupServiceCache, componentName={}, interfaceName={}.",
                componentName, interfaceName.getName());

        synchronized (componentMap) {
            Map<String, Long> componentServicesMap = componentMap.get(componentName);
            if (componentServicesMap == null) {
                logger.debug("Creating a Component Services Map for component {}", componentName);
                componentServicesMap = new HashMap<>();
                componentMap.put(componentName, componentServicesMap);
            }

            Long serviceCount = componentServicesMap.get(interfaceName.getName());
            if (serviceCount == null) {
                logger.debug("Creating a Service Instance List for interface {} in component {}",
                        interfaceName, componentName);
                serviceCount = 1L;
            } else {
                serviceCount++;
            }
            componentServicesMap.put(interfaceName.getName(), serviceCount);
        }
    }

    /**
     * This method provides a map of OSGi services and service count for the given {@code componentName}.
     *
     * @param componentName name of the reporter component
     * @return a list of reported OSGi service names
     */
    public Map<String, Long> getAvailableService(String componentName) {
        synchronized (componentMap) {
            Map<String, Long> availableServices = componentMap.get(componentName);
            if (availableServices == null) {
                return Collections.emptyMap();
            }
            return availableServices.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            stringLongEntry -> Long.valueOf(stringLongEntry.getValue())));
        }
    }
}
