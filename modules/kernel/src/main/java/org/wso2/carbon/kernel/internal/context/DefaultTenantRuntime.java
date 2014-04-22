/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.kernel.internal.context;

import org.wso2.carbon.kernel.internal.tenant.DefaultTenant;
import org.wso2.carbon.kernel.internal.tenant.store.FileBasedTenantStore;
import org.wso2.carbon.kernel.region.TenantRegion;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantRuntime;
import org.wso2.carbon.kernel.tenant.store.TenantStore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultTenantRuntime implements TenantRuntime<Tenant> {

    private TenantStore<Tenant> tenantStore;

    private Map<String, Tenant> loadedTenants = new HashMap<>();

    //TODO maintain a tenant hierarchy in a map. This will be useful in the future.

    @Override
    public void init() throws Exception {
        //TODO implement a pluggable mechanism for tenant stores.
        tenantStore = new FileBasedTenantStore();
        tenantStore.init();
    }

    @Override
    public Tenant addTenant(Tenant tenant) throws Exception {
        return addTenantInternal(tenant);
    }

    @Override
    public Tenant addTenant(String domain, String name, String description, String adminUsername,
                            String adminUserEmailAddress, Map<String, String> props) throws Exception {
        return addTenant(domain, name, description, adminUsername, adminUserEmailAddress, props, "server",
                new String[0], -1);
    }

    @Override
    public Tenant addTenant(String domain, String name, String description, String adminUsername,
                            String adminUserEmailAddress, Map<String, String> props, String parentTenantDomain,
                            String[] childTenantDomains, int depthOfHierarchy) throws Exception {

        //TODO implement a pluggable mechanism for tenant implementations.
        Tenant tenant = new DefaultTenant();
        String id = UUID.randomUUID().toString();
        tenant.setID(id);
        tenant.setDomain(domain);
        tenant.setName(name);
        tenant.setDescription(description);
        tenant.setCreatedDate(new Date());
        tenant.setAdminUsername(adminUsername);
        tenant.setAdminUserEmailAddress(adminUserEmailAddress);
        tenant.setProperties(props);
        tenant.setRegion(new TenantRegion(id));
//        tenant.setParent(parentID);
//        tenant.setDepthOfHierarchy(depthOfHierarchy);

//        for (String childID : childIDs) {
//            tenant.addChild(childID);
//        }
        return addTenantInternal(tenant);
    }

    @Override
    public Tenant deleteTenant(String tenantDomain) throws Exception{
        //TODO Notify
        return tenantStore.deleteTenant(tenantDomain);
    }

    @Override
    public Tenant getTenant(String tenantDomain) throws Exception {
        if(loadedTenants.containsKey(tenantDomain)){
            return loadedTenants.get(tenantDomain);
        }
        // Loading the tenant.
        try {
            return loadTenant(tenantDomain);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @Override
    public Tenant persistTenant(Tenant tenant) throws Exception {
        tenantStore.persistTenant(tenant);
        //TODO Notify
        return tenant;
    }

    private Tenant addTenantInternal(Tenant tenant) throws Exception {
        //TODO do the validations here, domain, parent, children etc...
        tenantStore.persistTenant(tenant);
        loadedTenants.put(tenant.getDomain(), tenant);
        //TODO Notify
        return tenant;
    }

    private Tenant loadTenant(String tenantDomain) throws Exception {
        //TODO Notify
        Tenant loadedTenant = tenantStore.loadTenant(tenantDomain);
        loadedTenants.put(loadedTenant.getDomain(), loadedTenant);
        return loadedTenant;
    }

}
