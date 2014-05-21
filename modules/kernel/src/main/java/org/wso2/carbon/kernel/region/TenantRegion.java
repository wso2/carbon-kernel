package org.wso2.carbon.kernel.region;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TenantRegion implements Region {

    private String id;
    private Map<Long, Bundle> tenantBundles = new ConcurrentHashMap<>();

    public TenantRegion(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void addBundle(Bundle bundle) throws BundleException {
        tenantBundles.put(bundle.getBundleId(), bundle);
        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();
        if (regionManager != null) {
            regionManager.associateBundleWithRegion(bundle.getBundleId(), this);
        }
    }

    @Override
    public void removeBundle(Bundle bundle) {
        if (tenantBundles.containsKey(bundle.getBundleId())) {
            tenantBundles.remove(bundle.getBundleId());
            RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();
            if (regionManager != null) {
                regionManager.dissociateBundleFromRegion(bundle.getBundleId());
            }
        }
    }

    @Override
    public Collection<Bundle> getBundles() {
        return tenantBundles.values();
    }
}
