package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.bundle.CollisionHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.context.PrivilegedTenantContext;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;

import java.util.Collection;
import java.util.HashSet;

public class RegionBundleCollisionHook implements CollisionHook {

    @Override
    public void filterCollisions(int i, Bundle bundle, Collection<Bundle> bundles) {

        Collection<Bundle> collidingBundles = new HashSet<>();

        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();

        Region kernelRegion = OSGiServiceHolder.getInstance().getKernelRegion();

        Region installRegion = PrivilegedTenantContext.getThreadLocalTenantContext().getRegion();

        if (regionManager != null) {
            for (Bundle currentBundle : bundles) {
                Region currentBundleRegion = regionManager.getRegion(currentBundle.getBundleId());
                //Check for tenant region collisions
                if ((installRegion != null)) {
                    if (currentBundleRegion.equals(installRegion)) {
                        collidingBundles.add(currentBundle);
                    }
                } else if (currentBundleRegion.equals(kernelRegion)) {//Check for kernel region bundle collisions
                    collidingBundles.add(currentBundle);
                }
            }
        }

        bundles.retainAll(collidingBundles);
    }
}
