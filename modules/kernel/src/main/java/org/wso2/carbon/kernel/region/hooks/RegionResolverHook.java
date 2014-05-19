package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;

import java.util.Collection;
import java.util.HashSet;

public class RegionResolverHook implements ResolverHook {

    @Override
    public void filterResolvable(Collection<BundleRevision> bundleRevisions) {
        //TODO : Implement this
    }

    @Override
    public void filterSingletonCollisions(BundleCapability bundleCapability,
                                          Collection<BundleCapability> bundleCapabilities) {
        //TODO : Implement this
    }

    @Override
    public void filterMatches(BundleRequirement bundleRequirement,
                              Collection<BundleCapability> bundleCapabilities) {
        Collection<BundleCapability> allowed = getAllowedCapabilities(bundleCapabilities,
                                                                      bundleRequirement);
        bundleCapabilities.retainAll(allowed);
    }

    @Override
    public void end() {
        //TODO : Implement this
    }

    private Collection<BundleCapability> getAllowedCapabilities(
            Collection<BundleCapability> bundleCapabilities, BundleRequirement bundleRequirement) {

        Collection<BundleCapability> allowedCapabilities = new HashSet<>();

        Bundle currentBundle = bundleRequirement.getRevision().getBundle();
        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();

        Region kernelRegion = OSGiServiceHolder.getInstance().getKernelRegion();

        Region currentBundleRegion = regionManager.getRegion(currentBundle.getBundleId());

        for (BundleCapability bundleCapability : bundleCapabilities) {
            Region region = regionManager.getRegion(bundleCapability.getRevision().
                    getBundle().getBundleId());
            if ((currentBundleRegion != null && currentBundleRegion.equals(region)) ||
                kernelRegion.equals(region)) {
                allowedCapabilities.add(bundleCapability);
            }
        }
        return allowedCapabilities;
    }

}
