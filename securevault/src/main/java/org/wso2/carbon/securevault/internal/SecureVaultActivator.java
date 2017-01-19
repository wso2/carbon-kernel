package org.wso2.carbon.securevault.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class SecureVaultActivator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        SecureVaultDataHolder.getInstance().setBundleContext(bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        SecureVaultDataHolder.getInstance().setBundleContext(null);
    }
}
