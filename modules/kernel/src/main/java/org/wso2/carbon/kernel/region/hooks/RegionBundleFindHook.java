package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.bundle.FindHook;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;

import java.util.Collection;
import java.util.HashSet;

public class RegionBundleFindHook implements FindHook {

    @Override
    public void find(BundleContext bundleContext, Collection<Bundle> bundles) {
        Collection<Bundle> allowedBundles = new HashSet<>();

        Bundle currentBundle = bundleContext.getBundle();
        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();

        Region kernelRegion = OSGiServiceHolder.getInstance().getKernelRegion();

        Region currentBundleRegion = regionManager.getRegion(currentBundle.getBundleId());

        //skip filtering of bundles if the find is invoked from kernel region
        if (currentBundleRegion.equals(kernelRegion)) {
            return;
        }

        for (Bundle bundle : bundles) {
            Region region = regionManager.getRegion(bundle.getBundleId());
            if (currentBundleRegion.equals(region)) {
                allowedBundles.add(bundle);
            }
        }

        bundles.retainAll(allowedBundles);
    }
}
