package org.wso2.carbon.kernel.region.hook;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.hooks.bundle.EventHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class RegionBundleEventHook implements EventHook {
    private static final Logger logger = LoggerFactory.getLogger(RegionBundleEventHook.class);

    @Override
    public void event(BundleEvent bundleEvent, Collection<BundleContext> bundleContexts) {
        logger.info("Executing bundle event hook on : {}", bundleContexts);
    }
}
