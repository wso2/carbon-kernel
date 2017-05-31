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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @scr.component name="org.wso2.stratos.permission.update.PermissionUpdateServiceComponent"
 * immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class PermissionUpdateServiceComponent {

    private static Log log = LogFactory.getLog(PermissionUpdateServiceComponent.class);
    private static final long JOB_INTERVAL_SECS = 1 * 60;

    private static final ScheduledExecutorService permUpdater = Executors.newScheduledThreadPool(1);
    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                permUpdater.shutdownNow();
            }
        });
    }

    protected void activate(ComponentContext ctxt) {
        // register a listener for tenant creation events to sync their permission tree
        ctxt.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                new PermissionUpdateAxis2ConfigurationContextObserver(),
                                                null);

        // update super-tenant permission cache
        PermissionUpdater.update(MultitenantConstants.SUPER_TENANT_ID);

        // Start a scheduled task for updating the permission tree periodically if it is enabled in the
        // user-mgt.xml.
        try {
            if ("true".equals(dataHolder.getRealmService().getBootstrapRealmConfiguration().
                    getAuthorizationPropertyValue(
                            UserCoreConstants.RealmConfig.PROPERTY_UPDATE_PERM_TREE_PERIODICALLY))) {
                permUpdater.scheduleAtFixedRate(new PermissionUpdateTask(), JOB_INTERVAL_SECS,
                                                JOB_INTERVAL_SECS, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("Error when retrieving the realm service for enabling the permission updater " +
                      "task. Disabling the periodic permission update task", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("********************* PermissionUpdateServiceComponent is activated..************");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        permUpdater.shutdownNow();
        if (log.isDebugEnabled()) {
            log.debug("PermissionUpdateServiceComponent is deactivated.");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is set for PermissionUpdateServiceComponent.");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(null);
    }
}

