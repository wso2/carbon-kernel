package org.wso2.carbon.user.core.tenant;

import java.io.Serializable;

/**
 * Model class of Tenant Unique id cache key.
 */
public class TenantUniqueIDKey implements Serializable {

    private static final long serialVersionUID = 5955431181622617223L;
    private String tenantUniqueID;

    public TenantUniqueIDKey(String tenantUniqueID) {

        this.tenantUniqueID = tenantUniqueID;
    }

    public String getTenantUniqueID() {

        return tenantUniqueID;
    }

    @Override
    public boolean equals(Object otherObject) {

        if (!(otherObject instanceof TenantUniqueIDKey)) {
            return false;
        }

        TenantUniqueIDKey uniqueIDKey = (TenantUniqueIDKey) otherObject;

        if (tenantUniqueID != null && !tenantUniqueID.equals(uniqueIDKey.getTenantUniqueID())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        return tenantUniqueID.hashCode();
    }
}
