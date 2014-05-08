package org.wso2.carbon.kernel.region;

import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.tenant.Tenant;

public class RegionUtils {

    public static Region getTenantRegion(String tenantDomain) throws Exception {
        Tenant tenant = OSGiServiceHolder.getInstance().getCarbonRuntime().
                getTenantRuntime().getTenant(tenantDomain);
        return tenant.getRegion();
    }
}
