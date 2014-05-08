package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.ListenerHook;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;

import java.util.Collection;
import java.util.Map;

public class RegionServiceEventHook implements EventListenerHook {

    @Override
    public void event(ServiceEvent serviceEvent,
                      Map<BundleContext, Collection<ListenerHook.ListenerInfo>>
                              bundleContextCollectionMap) {

        Bundle finderBundle = serviceEvent.getServiceReference().getBundle();

        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();

        Region kernelRegion = OSGiServiceHolder.getInstance().getKernelRegion();

        Region finderBundleRegion = regionManager.getRegion(finderBundle.getBundleId());

        for (BundleContext bundleContext : bundleContextCollectionMap.keySet()) {
            Region region = regionManager.getRegion(bundleContext.getBundle().getBundleId());
            //Check for the bundle region is not part of finder bundle region or kernel region
            if (!((finderBundleRegion != null && finderBundleRegion.equals(region)) ||
                kernelRegion.equals(region))) {
                bundleContextCollectionMap.remove(bundleContext);
            }
        }
    }
}
