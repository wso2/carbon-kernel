package org.wso2.carbon.kernel.region;


import org.osgi.framework.BundleException;

public interface RegionManager {

    public void associateBundleWithRegion(long bundleId, Region region) throws BundleException;

    public Region getRegion(long bundleId);
}
