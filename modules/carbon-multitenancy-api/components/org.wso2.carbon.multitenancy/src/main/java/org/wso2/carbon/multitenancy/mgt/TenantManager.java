/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.multitenancy.mgt;

import org.wso2.carbon.multitenancy.api.Tenant;
import org.wso2.carbon.multitenancy.api.TenantEvent;
import org.wso2.carbon.multitenancy.api.TenantStore;
import org.wso2.carbon.multitenancy.impl.CarbonTenant;
import org.wso2.carbon.multitenancy.internal.OSGiServiceHolder;

import java.util.Map;

/**
 * TODO
 */
public class TenantManager {
    private TenantStore<Tenant> tenantStore;

    public TenantManager(TenantStore<Tenant> tenantStore) {
        this.tenantStore = tenantStore;
    }

    public Tenant addTenant(Tenant tenant) throws Exception {
        return addTenantInternal(tenant);
    }

    public Tenant addTenant(String domain, Map<String, Object> props) throws Exception {
        Tenant tenant = new CarbonTenant(domain);
        tenant.setProperties(props);
        return addTenantInternal(tenant);
    }

    private Tenant addTenantInternal(Tenant tenant) throws Exception {
        tenantStore.addTenant(tenant);
        OSGiServiceHolder.getInstance().getTenantListeners()
                .forEach(tenantListener ->
                        tenantListener.notify(new TenantEvent(TenantEvent.ADDED, tenant.getDomain())));
        return tenant;
    }


    public Tenant deleteTenant(String tenantDomain) throws Exception {
        Tenant tenant = tenantStore.deleteTenant(tenantDomain);
        OSGiServiceHolder.getInstance().getTenantListeners()
                .forEach(tenantListener ->
                        tenantListener.notify(new TenantEvent(TenantEvent.REMOVED, tenant.getDomain())));
        return tenant;
    }
}
