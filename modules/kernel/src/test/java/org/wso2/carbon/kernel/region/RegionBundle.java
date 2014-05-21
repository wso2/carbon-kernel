package org.wso2.carbon.kernel.region;

import org.eclipse.osgi.framework.internal.core.BundleHost;

public class RegionBundle extends BundleHost {

    private long bundleId;
    public RegionBundle(long bundleId) {
        super(null, null);
        this.bundleId = bundleId;
    }

    @Override
    public long getBundleId() {
        return bundleId;
    }
}
