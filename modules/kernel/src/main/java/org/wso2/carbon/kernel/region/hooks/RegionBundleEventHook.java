package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.hooks.bundle.EventHook;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.context.CarbonContext;
import org.wso2.carbon.kernel.context.PrivilegedTenantContext;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;

import java.util.Collection;
import java.util.HashSet;

public class RegionBundleEventHook implements EventHook {

    @Override
    public void event(BundleEvent bundleEvent, Collection<BundleContext> bundleContexts) {

        if (bundleEvent.getType() == BundleEvent.INSTALLED) {
            bundleInstalled(bundleEvent.getBundle());
        }

        Collection<BundleContext> allowedBundleContexts = new HashSet<>();

        Bundle bundle = bundleEvent.getBundle();
        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();

        Region kernelRegion = OSGiServiceHolder.getInstance().getKernelRegion();

        Region currentBundleRegion = regionManager.getRegion(bundle.getBundleId());

        for (BundleContext bundleContext : bundleContexts) {
            Region region = regionManager.getRegion(bundleContext.getBundle().getBundleId());
            if ((currentBundleRegion != null && currentBundleRegion.equals(region)) ||
                kernelRegion.equals(region)) {
                allowedBundleContexts.add(bundleContext);
            }
        }

        bundleContexts.retainAll(allowedBundleContexts);
    }


    private void bundleInstalled(Bundle eventBundle) {
        try {
            Region installRegion = PrivilegedTenantContext.getThreadLocalTenantContext().getRegion();
            if (installRegion != null) {
                installRegion.addBundle(eventBundle);
            } else {
                Region kernelRegion = OSGiServiceHolder.getInstance().getKernelRegion();

                if (kernelRegion != null) {
                    kernelRegion.addBundle(eventBundle);
                }
            }
        } catch (BundleException e) {
            e.printStackTrace();
        }
    }
}
