package org.wso2.carbon.kernel.internal.startupresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * StartupServiceCache caches all the startup services against the component name given in the
 * ${@link org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener} and interface name of the services.
 *
 * @since 5.2.0
 */
public class StartupServiceCache {
    private static final Logger logger = LoggerFactory.getLogger(StartupServiceCache.class);

    private static StartupServiceCache serviceCacheInstance = new StartupServiceCache();
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
    public synchronized void update(String componentName, Class interfaceName, Object serviceInstance) {
        logger.debug("Updating StartupServiceCache, componentName={}, interfaceName={}, serviceInstance={}",
                componentName, interfaceName.getName(), serviceInstance);

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

    /**
     * This method provides the list of OSGi services that were reported by the {@code componentName}
     *
     * @param componentName name of the reporter component
     * @return a list of reported OSGi service names
     */
    public List<String> getServiceList(String componentName) {
        return Optional.ofNullable(componentMap.get(componentName))
                .orElse(Collections.emptyMap())
                .keySet()
                .stream()
                .collect(Collectors.toList());
    }
}
