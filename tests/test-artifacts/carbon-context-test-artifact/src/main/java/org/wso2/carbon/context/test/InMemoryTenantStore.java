/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.context.test;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantStore;
import org.wso2.carbon.kernel.tenant.exception.TenantStoreException;

import java.util.HashMap;
import java.util.Map;

/**
 * A sample implementation of a tenant store based on in memory.
 *
 * @since 5.1.0
 */
@Component(
        name = "org.wso2.carbon.context.test.InMemoryTenantStore",
        service = TenantStore.class,
        immediate = true
)
public class InMemoryTenantStore implements TenantStore {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryTenantStore.class);

    private Map<String, Tenant> tenantStore = new HashMap<>();

    @Activate
    protected void start(BundleContext bundleContext) {

    }

    @Override
    public void init() throws TenantStoreException {
        logger.info("Initializing InMemoryTenantStore");
        tenantStore.put("test.tenant.domain", new Tenant("test.tenant.domain"));
    }

    @Override
    public Tenant loadTenant(String tenantDomain) throws TenantStoreException {
        logger.info("Loading tenant with domain : " + tenantDomain + " from InMemoryTenantStore");
        return tenantStore.get(tenantDomain);
    }

    @Override
    public void addTenant(Tenant tenant) throws TenantStoreException {
        logger.info("Adding tenant with domain : " + tenant.getDomain() + " from InMemoryTenantStore");
        tenantStore.put(tenant.getDomain(), tenant);
    }

    @Override
    public Tenant deleteTenant(String tenantDomain) throws TenantStoreException {
        logger.info("Removing tenant with domain : " + tenantDomain + " from InMemoryTenantStore");
        return tenantStore.remove(tenantDomain);
    }
}
