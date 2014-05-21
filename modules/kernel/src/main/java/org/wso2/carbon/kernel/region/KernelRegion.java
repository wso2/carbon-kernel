package org.wso2.carbon.kernel.region;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KernelRegion implements Region {

    private String id;
    private Map<Long, Bundle> kernelBundles = new ConcurrentHashMap<>();

    public KernelRegion(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void addBundle(Bundle bundle) throws BundleException {
        kernelBundles.put(bundle.getBundleId(), bundle);
        RegionManager regionManager = OSGiServiceHolder.getInstance().getRegionManager();
        if (regionManager != null) {
            regionManager.associateBundleWithRegion(bundle.getBundleId(), this);
        }
    }

    @Override
    public void removeBundle(Bundle bundle) {
        if (kernelBundles.containsKey(bundle.getBundleId())) {
            kernelBundles.remove(bundle.getBundleId());
        }
    }

    @Override
    public Collection<Bundle> getBundles() {
        return kernelBundles.values();
    }
}
