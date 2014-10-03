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
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.authorization.JDBCAuthorizationManager;
import org.wso2.carbon.utils.AuthenticationObserver;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible for updating the permission caches of the tenants in the system
 */
public class PermissionUpdater {
    private static final Log log = LogFactory.getLog(PermissionUpdater.class);

    private static final String PERM_TREE_TIMESTAMP_LOC =
            "/repository/components/org.wso2.carbon.user.mgt/updatedTime";
    private static final String PERM_TREE_TIMESTAMP_PROP = "timestamp";

    /**
     * Map containing the tenantId -> lastModifiedTime
     */
    private static ConcurrentHashMap<Integer, Long>
            permTreeModifiedTimeStampMap = new ConcurrentHashMap<Integer, Long>();

    private static CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    public PermissionUpdater() {
    }

    public static void update(int tenantId) {
        try {
            initializeRegistry(tenantId);
            RegistryService registryService = dataHolder.getRegistryService();
            AuthorizationManager authzManager = getAuthzManager(tenantId, registryService);
            if (authzManager instanceof JDBCAuthorizationManager) {
                if (log.isDebugEnabled()) {
                    log.debug("Updating  permission cache for tenant: " + tenantId);
                }
                ((JDBCAuthorizationManager) authzManager).populatePermissionTreeFromDB();
            }
            UserRegistry registry = registryService.getGovernanceSystemRegistry(tenantId);
            Long lastModifiedTime = System.currentTimeMillis();
            if (registry.resourceExists(PERM_TREE_TIMESTAMP_LOC)) {
                Resource resource = registry.get(PERM_TREE_TIMESTAMP_LOC);
                lastModifiedTime =
                        new Long(resource.getProperty(PERM_TREE_TIMESTAMP_PROP).trim());
            } else {
                Resource resource = registry.newResource();
                resource.setProperty(PERM_TREE_TIMESTAMP_PROP,
                                     String.valueOf(lastModifiedTime));
                registry.put(PERM_TREE_TIMESTAMP_LOC, resource);

            }
            permTreeModifiedTimeStampMap.put(tenantId, lastModifiedTime);
            log.info("Permission cache updated for tenant " + tenantId);
        } catch (Exception e) {
            log.error("Error when updating the permission cache for tenant : " + tenantId, e);
        }
    }

    private static AuthorizationManager getAuthzManager(int tenantId,
                                                        RegistryService registryService)
            throws UserStoreException, RegistryException {
        AuthorizationManager authznManager =
                ((RegistryRealm) registryService.getUserRealm(tenantId)).
                        getRealm().getAuthorizationManager();
        return authznManager;
    }

    public static void remove(int tenantId) {
        try {
            RegistryService registryService = dataHolder.getRegistryService();
            AuthorizationManager authzManager = getAuthzManager(tenantId, registryService);
            if (authzManager instanceof JDBCAuthorizationManager) {
                if (log.isDebugEnabled()) {
                    log.debug("Updating  permission cache for tenant: " + tenantId);
                }
                ((JDBCAuthorizationManager) authzManager).clearPermissionTree();
            }
            permTreeModifiedTimeStampMap.remove(tenantId);
        } catch (Exception e) {
            log.error("Error when clearing the permission cache for tenant : " + tenantId, e);
        }
    }

    /**
     * Checks whether a tenant's permission cache needs to be updated
     *
     * @param tenantId The ID of the tenant
     * @return true - if the tenant's cache needs update, false - otherwise
     * @throws RegistryException If an error occurs when retrieving Registry resources
     */
    public static boolean needsUpdating(int tenantId) throws Exception {
        RegistryService registryService = dataHolder.getRegistryService();
        UserRegistry registry = registryService.getGovernanceSystemRegistry(tenantId);

        if (!registry.resourceExists(PERM_TREE_TIMESTAMP_LOC)) {
            return false;
        }

        if (permTreeModifiedTimeStampMap.containsKey(tenantId)) {
            Resource resource = registry.get(PERM_TREE_TIMESTAMP_LOC);
            Long registryTimeStamp =
                    new Long(resource.getProperty(PERM_TREE_TIMESTAMP_PROP).trim());
            Long localTimeStamp = permTreeModifiedTimeStampMap.get(tenantId);

            // If the permission tree is updated in registry
            return (localTimeStamp < registryTimeStamp);
        }
        return true;
    }

    private static void initializeRegistry(int tenantId) throws CarbonException{
        BundleContext bundleContext = dataHolder.getBundleContext();
        if (bundleContext != null) {
            ServiceTracker tracker =
                    new ServiceTracker(bundleContext,
                            AuthenticationObserver.class.getName(), null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).startedAuthentication(tenantId);
                }
            }
            tracker.close();
        }
    }
}
