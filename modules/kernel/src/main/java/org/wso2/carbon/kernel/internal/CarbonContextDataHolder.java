package org.wso2.carbon.kernel.internal;


public class CarbonContextDataHolder {

    private String tenantDomain;

    private static ThreadLocal<CarbonContextDataHolder> currentContextHolder =
            new ThreadLocal<CarbonContextDataHolder>() {
        protected CarbonContextDataHolder initialValue() {
            return new CarbonContextDataHolder();
        }
    };

    public static CarbonContextDataHolder getThreadLocalCarbonContextHolder() {
        return currentContextHolder.get();
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public static void destroyCurrentCarbonContextHolder() {
        currentContextHolder.remove();
    }
}
