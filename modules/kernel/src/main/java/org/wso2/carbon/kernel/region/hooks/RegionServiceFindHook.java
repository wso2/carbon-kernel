package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.FindHook;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;

import java.util.Collection;
import java.util.HashSet;

public class RegionServiceFindHook implements FindHook {

    @Override
    public void find(BundleContext bundleContext, String s, String s2, boolean b,
                     Collection<ServiceReference<?>> serviceReferences) {

        Collection<ServiceReference> allowedReferences = new HashSet<>();

        Bundle currentBundle = bundleContext.getBundle();

        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();

        Region kernelRegion = OSGiServiceHolder.getInstance().getKernelRegion();

        Region currentBundleRegion = regionManager.getRegion(currentBundle.getBundleId());

        //skip filtering of bundles if the find is invoked from kernel region
        if (currentBundleRegion.equals(kernelRegion)) {
            return;
        }

        for (ServiceReference serviceReference : serviceReferences) {
            Region region = regionManager.getRegion(serviceReference.getBundle().getBundleId());
            if (currentBundleRegion.equals(region)) {
                allowedReferences.add(serviceReference);
            }
        }

        serviceReferences.retainAll(allowedReferences);
    }
}
