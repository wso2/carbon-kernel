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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.core.CarbonThreadFactory;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component(name = "org.wso2.stratos.permission.update.PermissionUpdateServiceComponent", immediate = true)
public class PermissionUpdateServiceComponent {

    private static Log log = LogFactory.getLog(PermissionUpdateServiceComponent.class);
    private static final long JOB_INTERVAL_SECS = 1 * 60;

    private static final ScheduledExecutorService permUpdater =
            Executors.newScheduledThreadPool(1, new CarbonThreadFactory(new ThreadGroup("PermissionUpdaterThread")));
    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                permUpdater.shutdownNow();
            }
        });
    }

    @Activate
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

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        permUpdater.shutdownNow();
        if (log.isDebugEnabled()) {
            log.debug("PermissionUpdateServiceComponent is deactivated.");
        }
    }

    @Reference(name = "registry.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is set for PermissionUpdateServiceComponent.");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
    }

    @Reference(name = "config.context.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(null);
    }
}

