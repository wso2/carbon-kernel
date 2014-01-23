package org.wso2.carbon.user.core.tenant;


import java.io.Serializable;

public class TenantCacheEntry<T> implements Serializable {
    /**
     *  TODO
     */
    private static final long serialVersionUID = 1L;

    private transient T tenant = null;

    public TenantCacheEntry(T tenant) {
        this.tenant = tenant;
    }

    public T getTenant() {
        return tenant;
    }
}