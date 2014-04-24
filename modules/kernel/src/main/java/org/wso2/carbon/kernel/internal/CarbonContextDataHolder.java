package org.wso2.carbon.kernel.internal;


import org.wso2.carbon.kernel.region.Region;

public class CarbonContextDataHolder {

    private Region region;

    private static ThreadLocal<CarbonContextDataHolder> currentContextHolder =
            new ThreadLocal<CarbonContextDataHolder>() {
        protected CarbonContextDataHolder initialValue() {
            return new CarbonContextDataHolder();
        }
    };

    public static CarbonContextDataHolder getThreadLocalCarbonContextHolder() {
        return currentContextHolder.get();
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public static void destroyCurrentCarbonContextHolder() {
        currentContextHolder.remove();
    }
}
