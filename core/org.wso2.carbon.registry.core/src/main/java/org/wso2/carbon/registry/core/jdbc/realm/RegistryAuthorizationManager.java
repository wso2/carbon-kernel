/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.jdbc.realm;

import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.HashMap;
import java.util.Map;

/**
 * The Registry wrapper for the authorization manager.
 */
public class RegistryAuthorizationManager implements AuthorizationManager {

    private UserRealm coreRealm;
    private Map<String, String> pathMap;

    public String computePathOnMount(final String path) {
        for (Map.Entry<String, String> e : pathMap.entrySet()) {
            if (path.startsWith(e.getKey())) {
                return e.getValue() + path.substring(e.getKey().length());
            }
        }
        return path;
    }

    /**
     * Construct the registry authorization manager
     * 
     * @param coreRealm
     *            the realm to wrap.
     */
    public RegistryAuthorizationManager(UserRealm coreRealm) {
        this.coreRealm = coreRealm;
        pathMap = new HashMap<String, String>();
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        for (Mount mount : registryContext.getMounts()) {
            pathMap.put(mount.getPath(), mount.getTargetPath());
        }
    }

    /**
     * Clear user authorization for a given resource and action.
     * 
     * @param userName
     *            the user name.
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public void clearUserAuthorization(String userName, String resourceId, String action)
            throws UserStoreException {
        /*RealmConfiguration realmConfig = coreRealm.getRealmConfiguration();
        String systemUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        String adminUser = realmConfig.getAdminUserName();

        if (userName.equals(systemUser) || userName.equals(adminUser)) {

            String msg = "Could not change authorizations of the system defined user: " + userName;
            throw new UserStoreException(msg, false);
        }*/
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        getAuthorizationManager().clearUserAuthorization(userName, unchrootedResourceId, action);
    }

    /**
     * Clear the role authorization for a given resource id and action.
     * 
     * @param roleName
     *            the role name.
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public void clearRoleAuthorization(String roleName, String resourceId, String action)
            throws UserStoreException {
        /*RealmConfiguration realmConfig = coreRealm.getRealmConfiguration();
        String adminRoleName = realmConfig.getAdminRoleName();

        if (roleName.equals(adminRoleName)) {

            String msg = "Could not change authorizations of the system defined role: " + roleName;
            throw new UserStoreException(msg, false);
        }*/
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        getAuthorizationManager().clearRoleAuthorization(roleName, unchrootedResourceId, action);
    }

    /**
     * Authorize user for an action on a resource.
     * 
     * @param userName
     *            the user name.
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public void authorizeUser(String userName, String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        getAuthorizationManager().authorizeUser(userName, unchrootedResourceId, action);
    }

    /**
     * Authorize role for an action on a resource.
     * 
     * @param roleName
     *            the role name.
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public void authorizeRole(String roleName, String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        getAuthorizationManager().authorizeRole(roleName, unchrootedResourceId, action);
    }
/*
    @Override
    public void authorizeRole(String s, Permission[] permissions) throws UserStoreException {
     getAuthorizationManager().authorizeRole(s, permissions);
    }
*/
    /**
     * clear the resource authorizations.
     * 
     * @param resourceId
     *            the resource id.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public void clearResourceAuthorizations(String resourceId) throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        getAuthorizationManager().clearResourceAuthorizations(unchrootedResourceId);
    }

    /**
     * Check whether the user is authorized to do an action on a resource.
     * 
     * @param userName
     *            the user name.
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @return true, if the user is authorized, false otherwise.
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public boolean isUserAuthorized(String userName, String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        return getAuthorizationManager().isUserAuthorized(userName, unchrootedResourceId, action);
    }

    /**
     * Check whether the role is authorized do an action on a resource.
     * 
     * @param roleName
     *            the role name.
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @return true, if the role is authorized, false otherwise.
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public boolean isRoleAuthorized(String roleName, String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        return getAuthorizationManager().isRoleAuthorized(roleName, unchrootedResourceId, action);
    }

    /**
     * Get allowed roles for a resource to do an action.
     * 
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @return the allowed roles.
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public String[] getAllowedRolesForResource(String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        return getAuthorizationManager().getAllowedRolesForResource(unchrootedResourceId, action);
    }

    /**
     * Deny role to do an action on a resource.
     * 
     * @param roleName
     *            the role name.
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public void denyRole(String roleName, String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        getAuthorizationManager().denyRole(roleName, unchrootedResourceId, action);
    }

    /**
     * Deny user to do an action on a resource.
     * 
     * @param userName
     *            the user name.
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public void denyUser(String userName, String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        getAuthorizationManager().denyUser(userName, unchrootedResourceId, action);
    }

    /**
     * Get denied roles for a resource to do an action.
     * 
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @return the denied roles array.
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public String[] getDeniedRolesForResource(String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        return getAuthorizationManager().getDeniedRolesForResource(unchrootedResourceId, action);
    }

    /**
     * Get the explicitly allowed users for a resource.
     * 
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @return the denied roles array.
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public String[] getExplicitlyAllowedUsersForResource(String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        return getAuthorizationManager().getExplicitlyAllowedUsersForResource(unchrootedResourceId,
                action);
    }

    /**
     * Get the explicitly denied users for a resource.
     * 
     * @param resourceId
     *            the resource id.
     * @param action
     *            the action.
     * 
     * @return the denied roles array.
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public String[] getExplicitlyDeniedUsersForResource(String resourceId, String action)
            throws UserStoreException {
        String unchrootedResourceId = computePathOnMount(RegistryUtils.getUnChrootedPath(resourceId));
        return getAuthorizationManager().getExplicitlyDeniedUsersForResource(unchrootedResourceId,
                action);
    }

    /**
     * Get the explicitly denied users for a resource.
     * 
     * @param roleName
     *            the role name.
     * @param action
     *            the action.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    public void clearRoleActionOnAllResources(String roleName, String action)
            throws UserStoreException {
        getAuthorizationManager().clearRoleActionOnAllResources(roleName, action);
    }

    /**
     * Get the allowed UI resources for a user.
     * 
     * @param userName
     *            the user name.
     * 
     * @return an array of users.
     * @throws UserStoreException
     *             if the operation failed.
     */
    public String[] getAllowedUIResourcesForUser(String userName, String permissionRootPath)
            throws UserStoreException {
        return getAuthorizationManager().getAllowedUIResourcesForUser(userName, permissionRootPath);
    }

    /**
     * Clear the role authorization.
     * 
     * @param roleName
     *            the role name.
     * 
     * @throws UserStoreException
     *             if the operation failed.
     */
    public void clearRoleAuthorization(String roleName) throws UserStoreException {
        getAuthorizationManager().clearRoleAuthorization(roleName);

    }

    /**
     * Clear the user authorization.
     * 
     * @param userName
     *            the user name.
     * 
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public void clearUserAuthorization(String userName) throws UserStoreException {
        getAuthorizationManager().clearUserAuthorization(userName);
    }

    /**
     * Get the core authorization manager.
     * 
     * @return the authorization manager.
     * @throws UserStoreException
     *             throws if the operation failed.
     */
    private AuthorizationManager getAuthorizationManager() throws UserStoreException {
        return coreRealm.getAuthorizationManager();
    }


    /**
     * this will get the tenant id associated with the user authorization manager
     *
     * @return the tenant id of the authorization manager
     * @throws UserStoreException if the operation failed
     */
    public int getTenantId() throws UserStoreException {
        return getAuthorizationManager().getTenantId();
    }

    /**
     * this will reset the permission of the renamed role
     *
     * @param roleName    existing role name.
     * @param newRoleName new role name.
     * 
     * @throws UserStoreException if the operation failed
     */
    public void resetPermissionOnUpdateRole(String roleName, String newRoleName)
            throws UserStoreException {
        getAuthorizationManager().resetPermissionOnUpdateRole(roleName, newRoleName);        
    }

	@Override
    public String[] normalizeRoles(String[] roles) {
	    return roles;
    }

}
