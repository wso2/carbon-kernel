package org.wso2.carbon.kernel.region.hook;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.bundle.FindHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class RegionBundleFindHook implements FindHook {
    private static final Logger logger = LoggerFactory.getLogger(RegionBundleFindHook.class);

    @Override
    public void find(BundleContext bundleContext, Collection<Bundle> bundles) {
        logger.info("Executing bundle find hook on : {}", bundles);
    }
}
