package org.wso2.carbon.launcher;

import org.osgi.framework.BundleContext;

/**
 * Notify the framework startup event
 *
 * @since 5.0.0
 */
public interface FrameworkStartupHook {

    /**
     * Notify after the framework system bundle starts.
     *
     * @param systemBundleContext the bundle context of the system bundle.
     */
    void systemBundleStarted(BundleContext systemBundleContext);
}
