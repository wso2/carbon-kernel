package org.wso2.carbon.clustering.internal;


import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarbonClusterBundleActivator {
    private static Logger logger = LoggerFactory.getLogger(CarbonClusterBundleActivator.class);

    public void start(BundleContext bundleContext) {
        logger.debug("Activating carbon cluster bundle");
        DataHolder.getInstance().setBundleContext(bundleContext);
    }

    public void stop(BundleContext bundleContext) {
        logger.debug("Stopping carbon cluster bundle");
        DataHolder.getInstance().setBundleContext(null);
    }
}
