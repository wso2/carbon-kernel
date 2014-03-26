package org.wso2.carbon.extensions.internal;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CarbonExtensionsBundleActivator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        DataHolder.getInstance().setBundleContext(bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        DataHolder.getInstance().setBundleContext(null);
    }
}
