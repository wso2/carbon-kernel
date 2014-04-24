package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;

import java.util.Collection;
import java.util.HashSet;


public class RegionResolverHook implements ResolverHook {
    private static final Logger logger = LoggerFactory.getLogger(RegionResolverHook.class);

    @Override
    public void filterResolvable(Collection<BundleRevision> bundleRevisions) {
        logger.info("Resolvable bundle collections : " + bundleRevisions);
    }

    @Override
    public void filterSingletonCollisions(BundleCapability bundleCapability,
                                          Collection<BundleCapability> bundleCapabilities) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void filterMatches(BundleRequirement bundleRequirement,
                              Collection<BundleCapability> bundleCapabilities) {
        logger.info("Requirement bundle : " + bundleRequirement.getRevision());
        Collection<BundleCapability> allowed = getAllowedCapabilities(bundleCapabilities,
                                                                      bundleRequirement);
        bundleCapabilities.retainAll(allowed);
    }

    @Override
    public void end() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private Collection<BundleCapability> getAllowedCapabilities(
            Collection<BundleCapability> bundleCapabilities, BundleRequirement bundleRequirement) {

        Collection<BundleCapability> allowedCapabilities = new HashSet<>();
        CarbonRuntime carbonRuntime = OSGiServiceHolder.getInstance().getCarbonRuntime();

        if (carbonRuntime != null) {
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
        }
        return allowedCapabilities;
    }

}
