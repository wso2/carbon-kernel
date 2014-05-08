package org.wso2.carbon.kernel.region;

import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.tenant.Tenant;

public class RegionUtils {

    public static Region getTenantRegion(String tenantDomain) {
        Region region = null;
        try {
            Tenant tenant = OSGiServiceHolder.getInstance().getCarbonRuntime().
                    getTenantRuntime().getTenant(tenantDomain);
            region = tenant.getRegion();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return region;
    }
}
