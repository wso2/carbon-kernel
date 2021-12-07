package org.wso2.carbon.user.core.listener;

import org.wso2.carbon.user.api.RealmConfiguration;

// move this for tenant package, rename for TenantUserStoreCon
public interface UserStoreConfigurationListener {

    int getExecutionOrderId();

    boolean canExecutable();

    RealmConfiguration[] getSecondaryUserStoreRealmConfigurations(int tenantId);
}
