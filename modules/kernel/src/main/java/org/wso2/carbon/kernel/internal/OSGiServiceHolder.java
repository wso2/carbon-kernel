package org.wso2.carbon.kernel.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.region.BundleToRegionManager;
import org.wso2.carbon.kernel.region.RegionManager;

public class OSGiServiceHolder {
    private  static OSGiServiceHolder instance = new OSGiServiceHolder();

    private CarbonRuntime carbonRuntime;
    private BundleContext bundleContext;
    private RegionManager regionManager;

    public  static OSGiServiceHolder getInstance() {
        return instance;
    }

    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setRegionManager(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }
}
