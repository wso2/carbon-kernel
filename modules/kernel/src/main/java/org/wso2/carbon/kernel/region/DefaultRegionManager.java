package org.wso2.carbon.kernel.region;

import org.osgi.framework.BundleException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DefaultRegionManager implements RegionManager {

    private final Map<Long, Region> bundleToRegionMap = new HashMap<>();
    private final Map<String, Region> tenantToRegionMap = new HashMap<>();


    public void associateBundleWithRegion(long bundleId, Region region) throws BundleException {
        Region r = this.bundleToRegionMap.get(bundleId);
        if (r != null && r != region) {
            throw new BundleException("Bundle : " + bundleId + " is already associated with region" +
                                      " : " + r, BundleException.INVALID_OPERATION);
        }
        bundleToRegionMap.put(bundleId, region);
    }


    public void dissociateBundleFromRegion(long bundleId) {
        bundleToRegionMap.remove(bundleId);
    }


    public boolean isBundleAssociatedWithRegion(long bundleId, Region region) {
        return bundleToRegionMap.get(bundleId) == region;
    }


    public Set<Long> getBundleIds(Region region) {
        Set<Long> bundleIds = new HashSet<Long>();

        for (Map.Entry<Long, Region> entry : bundleToRegionMap.entrySet()) {
            if (entry.getValue() == region) {
                bundleIds.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(bundleIds);
    }


    public void clear() {
        bundleToRegionMap.clear();
    }

    public Region getRegion(long bundleId) {
        return bundleToRegionMap.get(bundleId);
    }

    @Override
    public Region getRegion(String tenantDomain) {
        return tenantToRegionMap.get(tenantDomain);
    }

    @Override
    public void associateTenantWithRegion(String tenantDomain, Region region) {
        tenantToRegionMap.put(tenantDomain, region);
    }

    public void dissociateRegion(Region region) {
        Iterator<Map.Entry<Long, Region>> iterator = bundleToRegionMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Region> entry = iterator.next();
            if (entry.getValue().equals(region)) {
                iterator.remove();
            }
        }
    }
}
