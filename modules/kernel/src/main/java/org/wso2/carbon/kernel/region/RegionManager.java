package org.wso2.carbon.kernel.region;


import org.osgi.framework.BundleException;

public interface RegionManager {

    public void associateBundleWithRegion(long bundleId, Region region) throws BundleException;

    public void dissociateRegion(Region region);

    public Region getRegion(long bundleId);
}
