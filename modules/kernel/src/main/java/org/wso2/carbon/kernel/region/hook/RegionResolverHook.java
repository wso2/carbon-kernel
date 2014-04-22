package org.wso2.carbon.kernel.region.hook;

import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.BundleToRegionManager;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantRuntime;

import java.util.ArrayList;
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
        Region tenantRegion = getTenantRegion(bundleRequirement.getRevision());
        Collection<BundleCapability> allowed = getAllowedCapabilities(bundleCapabilities,
                                                                      tenantRegion);
        bundleCapabilities.retainAll(allowed);
    }

    private Collection<BundleCapability> getAllowedCapabilities(
            Collection<BundleCapability> bundleCapabilities, Region tenantRegion) {
        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();

        Collection<BundleCapability> allowedCapabilities = new HashSet<>();
        for (BundleCapability bundleCapability : bundleCapabilities) {
            if (tenantRegion.equals(regionManager.getRegion(bundleCapability.getRevision().
                    getBundle().getBundleId()))) {
                allowedCapabilities.add(bundleCapability);
            }
        }

        return allowedCapabilities;
    }

    @Override
    public void end() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private Region getTenantRegion(BundleRevision revision) {
        Region region = null;
        CarbonRuntime carbonRuntime = OSGiServiceHolder.getInstance().getCarbonRuntime();

        if (carbonRuntime != null) {
            Bundle bundle = revision.getBundle();
            RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();
            region = regionManager.getRegion(bundle.getBundleId());
        }
        return region;
    }
}
