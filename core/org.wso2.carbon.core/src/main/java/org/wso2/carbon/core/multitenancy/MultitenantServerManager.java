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
package org.wso2.carbon.core.multitenancy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.CarbonThreadFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles server startup & iniatialization when a multitenant deployment is available
 */
public class MultitenantServerManager {

    private static final Log log = LogFactory.getLog(MultitenantServerManager.class);
    private static final ScheduledExecutorService tenantCleanupExec = Executors
            .newScheduledThreadPool(1, new CarbonThreadFactory(new ThreadGroup("tenantCleanupThread")));
    private static final int TENANT_CLEANUP_PERIOD_SECS = 60;
    private static final int DEFAULT_TENANT_IDLE_MINS = 30;
    private static long tenantIdleTimeMillis;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tenantCleanupExec.shutdownNow();
            }
        });
        tenantIdleTimeMillis =
                Long.parseLong(System.getProperty(MultitenantConstants.TENANT_IDLE_TIME,
                                                  String.valueOf(DEFAULT_TENANT_IDLE_MINS)))*
                60 * 1000;
    }

    /**
     * Start multitenant deployment
     *
     * @param configCtx - super tenant config context
     * @throws AxisFault If an error occurs while doing tenant specific deployments
     */
    public void start(ConfigurationContext configCtx) throws Exception {
        // schedule the tenant cleanup task
        TenantCleanupTask tenantCleanupTask = new TenantCleanupTask();
        tenantCleanupExec.scheduleAtFixedRate(tenantCleanupTask,
                TENANT_CLEANUP_PERIOD_SECS,
                TENANT_CLEANUP_PERIOD_SECS, TimeUnit.SECONDS);
    }

    private static class TenantCleanupTask implements Runnable {

        private TenantCleanupTask() {
        }

        public void run() {
            try {
                TenantAxisUtils.cleanupTenants(tenantIdleTimeMillis);
            } catch (Throwable e) {
                log.error("Error occurred while executing tenant cleanup", e);
            }
        }
    }

    public void cleanup() {
        tenantCleanupExec.shutdownNow();
    }

    /*private static final ExecutorService exec = Executors.newCachedThreadPool();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
                exec.shutdownNow();
            }
        });
    }

    private void initTenantDeployment(final ConfigurationContext mainConfigCtx)
            throws Exception {

        RealmService realmService = CarbonCoreServiceComponent.getRealmService();
        Tenant[] tenants = realmService.getTenantManager().getAllTenants();
        for (final Tenant tenant : tenants) {
            // Create per tenant config contexts
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        TenantUtils.createTenantConfigurationContext(mainConfigCtx, tenant.getDomain(),
                                                                     tenant.getId(), bundleContext);
                    } catch (Throwable e) {
                        log.error("Error occurred while creating AxisConfiguration for tenant " +
                                  tenant.getDomain() + "[" + tenant.getId() + "]");
                    }
                }
            };
            exec.execute(runnable);
        }
    }*/
}
