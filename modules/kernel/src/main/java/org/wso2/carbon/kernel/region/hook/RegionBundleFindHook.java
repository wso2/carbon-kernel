package org.wso2.carbon.kernel.region.hook;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.bundle.FindHook;

import java.util.Collection;


public class RegionBundleFindHook implements FindHook {
    @Override
    public void find(BundleContext bundleContext, Collection<Bundle> bundles) {

    }
}
