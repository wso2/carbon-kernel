package org.wso2.carbon.kernel.region;


import org.osgi.framework.BundleException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BundleToRegionManager implements RegionManager {

    private final Map<Long, Region> bundleToRegion = new HashMap<>();


    public void associateBundleWithRegion(long bundleId, Region region) throws BundleException {
        Region r = this.bundleToRegion.get(bundleId);
        if (r != null && r != region) {
            throw new BundleException("Bundle '" + bundleId + "' is already associated with region " +
                                      "'" + r + "'", BundleException.INVALID_OPERATION);
        }
        this.bundleToRegion.put(bundleId, region);
    }


    public void dissociateBundleFromRegion(long bundleId) {
        this.bundleToRegion.remove(bundleId);
    }


    public boolean isBundleAssociatedWithRegion(long bundleId, Region region) {
        return this.bundleToRegion.get(bundleId) == region;
    }


    public Set<Long> getBundleIds(Region region) {
        Set<Long> bundleIds = new HashSet<Long>();

        for (Map.Entry<Long, Region> entry : this.bundleToRegion.entrySet()) {
            if (entry.getValue() == region) {
                bundleIds.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(bundleIds);
    }


    public void clear() {
        this.bundleToRegion.clear();
    }

    public Region getRegion(long bundleId) {
        return this.bundleToRegion.get(bundleId);
    }

    public void dissociateRegion(Region region) {
        Iterator<Map.Entry<Long, Region>> iterator = this.bundleToRegion.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Region> entry = iterator.next();
            if (entry.getValue() == region) {
                iterator.remove();
            }
        }
    }
}
