package org.wso2.carbon.security.keystore.dao;

import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class DAOUtils {

    public static String getTenantUUID(int tenantId) throws SecurityConfigException {

        // Super tenant does not have a tenant UUID. Therefore, set a hard coded value.
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            // Set a hard length of 32 characters for super tenant ID.
            // This is to avoid the database column length constraint violation.
            // TODO: shouldn't the length be 36 ?
            return String.format("%1$-32d", tenantId);
        }

        // TODO: getTenant also seems to throw a runtime exception if tenant does not exist. Figure out a way to use that, or catch that and convert to SecurityConfigException
        if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            Tenant tenant = IdentityTenantUtil.getTenant(tenantId);
            return tenant.getTenantUniqueID();
        }

        throw new SecurityConfigException("Invalid tenant id: " + tenantId);
    }
}
