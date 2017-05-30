/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.core.internal.permission.update;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This Job handles updating of tenant permissions. It will periodically get the list of active
 * tenants, and check whether the permission of each tenant needs to be updated. If the permission
 * tree of a tenant needs to be updated, it will create a Task of type
 * <code>TenantPermissionUpdateTask</code>, and submit it to an ExecutorService which will
 * execute those tasks which update tenant permissions.
 */
public class PermissionUpdateTask implements Runnable {
    private static final Log log = LogFactory.getLog(PermissionUpdateTask.class);
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private String isRunning = "false";

    /**
     * An instance of this task will be created per active tenant, if that tenant's permission
     * should be updated.
     */
    private static class TenantPermissionUpdateTask implements Runnable {

        private int tenantId;

        private TenantPermissionUpdateTask(int tenantId) {
            this.tenantId = tenantId;
        }

        public void run() {
            PermissionUpdater.update(tenantId);
        }
    }

    public void run() {
        synchronized (isRunning) {
            if (isRunning.equals("true")) {
                return;
            }
        }
        isRunning = "true";

        if (log.isDebugEnabled()) {
            log.debug("Periodic task of updating permission cache is started.");
        }

        try {
            updateSuperTenantPermissions();
            updateTenantPermissions();
        } catch (Exception e) {
            log.error("Error when obtaining the tenant's govovernance registry instance to update " +
                      "the permission cache", e);
        } finally {
            isRunning = "false";
        }
    }

    private void updateSuperTenantPermissions() throws Exception {
        if (PermissionUpdater.needsUpdating(MultitenantConstants.SUPER_TENANT_ID)) {
            threadPool.submit(new TenantPermissionUpdateTask(MultitenantConstants.SUPER_TENANT_ID));
        }
    }

    private void updateTenantPermissions() throws Exception {
        ConfigurationContext mainServerConfigContext =
                CarbonCoreDataHolder.getInstance().getMainServerConfigContext();
        List<Tenant> tenantList = TenantAxisUtils.getActiveTenants(mainServerConfigContext);
        Tenant[] tenants = tenantList.toArray(new Tenant[tenantList.size()]);
        for (Tenant tenant : tenants) {
            if (!tenant.isActive()) {
                continue;
            }
            int tenantId = tenant.getId();
            if (PermissionUpdater.needsUpdating(tenantId)) {
                threadPool.submit(new TenantPermissionUpdateTask(tenantId));
            }
        }
    }
}

