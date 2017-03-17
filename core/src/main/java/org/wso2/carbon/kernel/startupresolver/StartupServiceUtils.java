package org.wso2.carbon.kernel.startupresolver;

import org.wso2.carbon.kernel.internal.startupresolver.StartupServiceCache;

/**
 * A Utility which provides a way to update the startup service cache.
 *
 * @since 5.2.0
 */
public class StartupServiceUtils {

    private StartupServiceUtils() {
        throw new AssertionError("Instantiating utility class...");
    }

    /**
     * All the components that are using startup order resolver functionality should call this method once those
     * components receive a reference to a dependent OSGi service. This method will update the internal service cache
     * of the StartupOrderResolver.
     *
     * @param componentName name of the reporting component
     * @param interfaceName name of the OSGi service interface
     * @param serviceInstance OSGi service instance
     */
    public static void updateServiceCache(String componentName, Class interfaceName, Object serviceInstance)  {
        StartupServiceCache.getInstance().update(componentName, interfaceName, serviceInstance);
    }
}
