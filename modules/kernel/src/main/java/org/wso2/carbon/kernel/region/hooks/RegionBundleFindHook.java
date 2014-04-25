package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.hooks.bundle.FindHook;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.context.CarbonContext;
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

        for (Bundle bundle : bundles) {
            Region region = regionManager.getRegion(bundleContext.getBundle().getBundleId());
            if ((currentBundleRegion != null && currentBundleRegion.equals(region)) ||
                kernelRegion.equals(region)) {
                allowedBundles.add(bundle);
            }
        }

        bundles.retainAll(allowedBundles);
    }
}
