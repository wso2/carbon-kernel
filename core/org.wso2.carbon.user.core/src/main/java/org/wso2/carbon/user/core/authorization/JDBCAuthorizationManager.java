/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.user.core.authorization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.internal.UMListenerServiceComponent;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;

public class JDBCAuthorizationManager implements AuthorizationManager {

    private DataSource dataSource = null;
    private PermissionTree permissionTree = null;
    private AuthorizationCache authorizationCache = null;
    private UserRealm userRealm = null;
    private RealmConfiguration realmConfig = null;
    private boolean caseInSensitiveAuthorizationRules;
    private String cacheIdentifier;
    private int tenantId;
    /**
     * The root node of the tree
     */
    private static Log log = LogFactory.getLog(JDBCAuthorizationManager.class);
    private static boolean debug = log.isDebugEnabled();

    public JDBCAuthorizationManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm,
            Integer tenantId) throws UserStoreException {

        authorizationCache = AuthorizationCache.getInstance();
        if(!"true".equals(realmConfig.getAuthorizationManagerProperty(UserCoreConstants.
                                        RealmConfig.PROPERTY_AUTHORIZATION_CACHE_ENABLED))){
            authorizationCache.disableCache();
        }

        if(!"true".equals(realmConfig.getAuthorizationManagerProperty(UserCoreConstants.
                                        RealmConfig.PROPERTY_CASE_SENSITIVITY))){
            caseInSensitiveAuthorizationRules = true;
        }

        String userCoreCacheIdentifier = realmConfig.getUserStoreProperty(UserCoreConstants.
                        RealmConfig.PROPERTY_USER_CORE_CACHE_IDENTIFIER);

        if(userCoreCacheIdentifier != null && userCoreCacheIdentifier.trim().length() > 0){
            cacheIdentifier =  userCoreCacheIdentifier;
        } else {
            cacheIdentifier = UserCoreConstants.DEFAULT_CACHE_IDENTIFIER;
        }

        dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
        if (dataSource == null) {
            dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
            properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
        }
        this.permissionTree = new PermissionTree(cacheIdentifier, tenantId, dataSource);
        this.realmConfig = realmConfig;
        this.userRealm = realm;
        this.tenantId = tenantId;
        if (log.isDebugEnabled()) {
            log.debug("The jdbcDataSource being used by JDBCAuthorizationManager :: "
                    + dataSource.hashCode());
        }
        this.populatePermissionTreeFromDB();
        this.addInitialData();
    }

    public boolean isRoleAuthorized(String roleName, String resourceId, String action) throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.isRoleAuthorized(roleName, resourceId, action, this)) {
                return false;
            }
        }

        roleName = modify(roleName);
        resourceId = modify(resourceId);
        action = modify(action);

        permissionTree.updatePermissionTree();
        SearchResult sr = permissionTree.getRolePermission(roleName, PermissionTreeUtil
                .actionToPermission(action), null, null, PermissionTreeUtil
                .toComponenets(resourceId));


        if(log.isDebugEnabled()) {
            if (!sr.getLastNodeAllowedAccess()){
                log.debug(roleName + " role is not Authorized to perform "+ action + " on " + resourceId);
            }
        }
        
        return sr.getLastNodeAllowedAccess();
    }

    public boolean isUserAuthorized(String userName, String resourceId, String action)
            throws UserStoreException {

        if (CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(userName)) {
            return true;
        }

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.isUserAuthorized(userName, resourceId, action, this)) {
                return false;
            }
        }

        String unModifiedUser = userName;

        userName = modify(userName);
        resourceId = modify(resourceId);
        action = modify(action);

        try {
            Boolean userAllowed = authorizationCache.isUserAuthorized(cacheIdentifier,
                                                            tenantId, userName, resourceId, action);
            if(log.isDebugEnabled()){
                if(userAllowed != null && !userAllowed) {
                    log.debug("Authorization cache hit. " +
                            userName + " user is not Authorized to perform "+ action +
                                                                               " on " + resourceId);
                }
            }
            
			if (userAllowed != null) {
				return userAllowed;
			}
			
        } catch (AuthorizationCacheException e) {
            // Entry not found in the cache. Just continue.
        }

        if(log.isDebugEnabled()){
            log.debug("Authorization cache miss for username : " + userName + " resource " + resourceId
                                           + " action : " + action);
        }

        permissionTree.updatePermissionTree();

        //following is related with user permission, and it is not hit in the current flow.
		SearchResult sr =
		                  permissionTree.getUserPermission(userName,
		                                                   PermissionTreeUtil.actionToPermission(action),
		                                                   null, null,
		                                                   PermissionTreeUtil.toComponenets(resourceId));
		if (sr.getLastNodeAllowedAccess()) {
			authorizationCache.addToCache(cacheIdentifier, tenantId, userName, resourceId, action, true);
			return true;
		}


        boolean userAllowed = false;
        String[] allowedRoles = modify(getAllowedRolesForResource(resourceId, action));
        
        
        if(allowedRoles != null && allowedRoles.length > 0){
            if(log.isDebugEnabled()) {
                log.debug("Roles which have permission for resource : " + resourceId + " action : " + action);
                for(String allowedRole : allowedRoles){
                    log.debug("Role :  " + allowedRole);
                }
            }

            AbstractUserStoreManager manager = (AbstractUserStoreManager) userRealm.getUserStoreManager();
            for (String role : allowedRoles) {
                if (manager.isUserInRole(unModifiedUser, role)) {
                    if(log.isDebugEnabled()) {
                        log.debug( unModifiedUser + " user is in role :  " + role);
                    }
                    userAllowed = true;
                    break;
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug( unModifiedUser + " user is not in role :  " + role);
                    }
                }
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug("No roles have permission for resource : " + resourceId + " action : " + action);    
            }
        }

        //need to add the authorization decision taken by role based permission
        authorizationCache.addToCache(cacheIdentifier, this.tenantId, userName, resourceId, action,
                                                                                        userAllowed);
        
        if(log.isDebugEnabled()){
            if(!userAllowed) {
                log.debug(userName + " user is not Authorized to perform "+ action + " on " + resourceId);
            }
        }

        return userAllowed;
    }

    public String[] getAllowedRolesForResource(String resourceId, String action)
            throws UserStoreException {

		resourceId = modify(resourceId);
		action = modify(action);
		TreeNode.Permission permission = PermissionTreeUtil.actionToPermission(action);
		permissionTree.updatePermissionTree();
		SearchResult sr =
		                  permissionTree.getAllowedRolesForResource(null,
		                                                            null,
		                                                            permission,
		                                                            PermissionTreeUtil.toComponenets(resourceId));
		
		if(debug) {
			log.debug("Allowed roles for the ResourceID: " + resourceId + " Action: " + action);
			String[] roles = sr.getAllowedEntities().toArray(new String[sr.getAllowedEntities().size()]);
			for(String role : roles) {
				log.debug("role: " + role);
			}
		}

        return sr.getAllowedEntities().toArray(new String[sr.getAllowedEntities().size()]);
    }

    public String[] getExplicitlyAllowedUsersForResource(String resourceId, String action)
            throws UserStoreException {

		resourceId = modify(resourceId);
		action = modify(action);
		TreeNode.Permission permission = PermissionTreeUtil.actionToPermission(action);
		permissionTree.updatePermissionTree();
		SearchResult sr =
		                  permissionTree.getAllowedUsersForResource(null,
		                                                            null,
		                                                            permission,
		                                                            PermissionTreeUtil.toComponenets(resourceId));
		
		if(debug) {
			log.debug("Explicityly allowed roles for the ResourceID: " + resourceId + " Action: " + action);
			String[] roles = sr.getAllowedEntities().toArray(new String[sr.getAllowedEntities().size()]);
			for(String role : roles) {
				log.debug("role: " + role);
			}
		}

        return sr.getAllowedEntities().toArray(new String[sr.getAllowedEntities().size()]);
    }

    public String[] getDeniedRolesForResource(String resourceId, String action)
            throws UserStoreException {

		resourceId = modify(resourceId);
		action = modify(action);
		TreeNode.Permission permission = PermissionTreeUtil.actionToPermission(action);
		permissionTree.updatePermissionTree();
		SearchResult sr =
		                  permissionTree.getDeniedRolesForResource(null,
		                                                           null,
		                                                           permission,
		                                                           PermissionTreeUtil.toComponenets(resourceId));
		return sr.getDeniedEntities().toArray(new String[sr.getAllowedEntities().size()]);
    }

    public String[] getExplicitlyDeniedUsersForResource(String resourceId, String action)
            throws UserStoreException {

		resourceId = modify(resourceId);
		action = modify(action);
		TreeNode.Permission permission = PermissionTreeUtil.actionToPermission(action);
		permissionTree.updatePermissionTree();
		SearchResult sr =
		                  permissionTree.getDeniedUsersForResource(null,
		                                                           null,
		                                                           permission,
		                                                           PermissionTreeUtil.toComponenets(resourceId));
		return sr.getDeniedEntities().toArray(new String[sr.getAllowedEntities().size()]);
    }

    public String[] getAllowedUIResourcesForUser(String userName, String permissionRootPath)
            throws UserStoreException {

		permissionRootPath = modify(permissionRootPath);
		List<String> lstPermissions = new ArrayList<String>();
		List<String> resourceIds = getUIPermissionId();
		if (resourceIds != null) {
			for (String resourceId : resourceIds) {
				if (isUserAuthorized(userName, resourceId,CarbonConstants.UI_PERMISSION_ACTION)) 
				{
					if (permissionRootPath == null) {
						lstPermissions.add(resourceId);
					} else {
						if (resourceId.contains(permissionRootPath)) {
							lstPermissions.add(resourceId);
						}
					}
				}//authorization check up
			}//loop over resource list 
		}//resource ID checkup
        

		String[] permissions = lstPermissions.toArray(new String[lstPermissions.size()]);
		String[] optimizedList = UserCoreUtil.optimizePermissions(permissions);
		
		if(debug) {
			log.debug("Allowed UI Resources for User: " + userName + " in permissionRootPath: " +
			          permissionRootPath);
			for(String resource : optimizedList) {
				log.debug("Resource: " + resource);
			}
		}
		
		return optimizedList;
    }

    public void authorizeRole(String roleName, String resourceId, String action)
            throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.authorizeRole(roleName, resourceId, action, this)) {
                return;
            }
        }

        if (resourceId == null || action == null) {
            log.error("Invalid data provided at authorization code");
            throw new UserStoreException("Invalid data provided");
        }
        roleName = modify(roleName);
        resourceId = modify(resourceId);
        action = modify(action);
        addAuthorizationForRole(roleName,resourceId, action, UserCoreConstants.ALLOW, true);
    }

    public void denyRole(String roleName, String resourceId, String action)
            throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.denyRole(roleName, resourceId, action, this)) {
                return;
            }
        }

        if (resourceId == null || action == null) {
            log.error("Invalid data provided at authorization code");
            throw new UserStoreException("Invalid data provided");
        }
        roleName = modify(roleName);
        resourceId = modify(resourceId);
        action = modify(action);
        addAuthorizationForRole(roleName, resourceId, action, UserCoreConstants.DENY, true);
    }

    public void authorizeUser(String userName, String resourceId, String action)
            throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.authorizeUser(userName, resourceId, action, this)) {
                return;
            }
        }

        if (resourceId == null || action == null) {
            log.error("Invalid data provided at authorization code");
            throw new UserStoreException("Invalid data provided");
        }
        userName = modify(userName);
        resourceId = modify(resourceId);
        action = modify(action);
        addAuthorizationForUser(userName, resourceId, action, UserCoreConstants.ALLOW, true);
    }

    public void denyUser(String userName, String resourceId, String action)
            throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.denyUser(userName, resourceId, action, this)) {
                return;
            }
        }

        if (resourceId == null || action == null) {
            log.error("Invalid data provided at authorization code");
            throw new UserStoreException("Invalid data provided");
        }

        userName = modify(userName);
        resourceId = modify(resourceId);
        action = modify(action);

        addAuthorizationForUser(userName, resourceId, action, UserCoreConstants.DENY, true);
    }

    public void clearResourceAuthorizations(String resourceId) throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearResourceAuthorizations(resourceId, this)) {
                return;
            }
        }
        resourceId = modify(resourceId);
        /**
         * Need to clear authz cache when resource authorization is cleared.
         */
        authorizationCache.clearCacheByTenant(this.tenantId);

        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            DatabaseUtil.updateDatabase(dbConnection,
                    DBConstants.ON_DELETE_PERMISSION_UM_ROLE_PERMISSIONS_SQL, resourceId, tenantId);
            DatabaseUtil.updateDatabase(dbConnection,
                    DBConstants.ON_DELETE_PERMISSION_UM_USER_PERMISSIONS_SQL, resourceId, tenantId);
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_PERMISSION_SQL,
                    resourceId, tenantId);
            permissionTree.clearResourceAuthorizations(resourceId);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public void clearRoleAuthorization(String roleName, String resourceId, String action)
            throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearRoleAuthorization(roleName, resourceId, action, this)) {
                return;
            }
        }

        roleName = modify(roleName);
        resourceId = modify(resourceId);
        action = modify(action);

        /*need to clear tenant authz cache once role authorization is removed, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);
        
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            String domain = UserCoreUtil.extractDomainFromName(roleName);
            if (domain != null) {
				domain = domain.toUpperCase();
			}
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_ROLE_PERMISSION_SQL,
                    UserCoreUtil.removeDomainFromName(roleName), resourceId, action, tenantId, tenantId, tenantId, domain);
            permissionTree.clearRoleAuthorization(roleName, resourceId, action);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public void clearUserAuthorization(String userName, String resourceId, String action)
            throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearUserAuthorization(userName, resourceId, action, this)) {
                return;
            }
        }

        userName = modify(userName);
        resourceId = modify(resourceId);
        action = modify(action);

        this.authorizationCache.clearCacheEntry(cacheIdentifier, tenantId, userName, resourceId,
                                                                                        action);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            int permissionId = this.getPermissionId(dbConnection, resourceId, action);
            if (permissionId == -1) {
                this.addPermissionId(dbConnection, resourceId, action);                
            }
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_USER_PERMISSION_SQL,
                    userName, resourceId, action, tenantId, tenantId);
            permissionTree.clearUserAuthorization(userName, resourceId, action);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void clearRoleActionOnAllResources(String roleName, String action)
            throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearRoleActionOnAllResources(roleName, action, this)) {
                return;
            }
        }
        
        roleName = modify(roleName);
        action = modify(action);

        /*need to clear tenant authz cache once role authorization is removed, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            permissionTree.clearRoleAuthorization(roleName, action);
            String domain = UserCoreUtil.extractDomainFromName(roleName);
			if (domain != null) {
				domain = domain.toUpperCase();
			}
            DatabaseUtil.updateDatabase(dbConnection,
                    DBConstants.DELETE_ROLE_PERMISSIONS_BASED_ON_ACTION, UserCoreUtil.removeDomainFromName(roleName),
                    action, tenantId, tenantId, tenantId, domain);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }
    
    public void clearRoleAuthorization(String roleName) throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearRoleAuthorization(roleName, this)) {
                return;
            }
        }

        roleName = modify(roleName);

        /*need to clear tenant authz cache once role authorization is removed, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            permissionTree.clearRoleAuthorization(roleName);
            String domain = UserCoreUtil.extractDomainFromName(roleName);
            if (domain != null) {
				domain = domain.toUpperCase();
			}
            DatabaseUtil.updateDatabase(dbConnection,
                    DBConstants.ON_DELETE_ROLE_DELETE_PERMISSION_SQL, UserCoreUtil.removeDomainFromName(roleName),
                    tenantId, tenantId, domain);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void clearUserAuthorization(String userName) throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearUserAuthorization(userName, this)) {
                return;
            }
        }

        userName = modify(userName);

        this.authorizationCache.clearCacheByTenant(tenantId);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            permissionTree.clearUserAuthorization(userName);
            DatabaseUtil.updateDatabase(dbConnection,
                    DBConstants.ON_DELETE_USER_DELETE_PERMISSION_SQL, userName, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }

    }

    public  void resetPermissionOnUpdateRole(String roleName, String newRoleName)
            throws UserStoreException {

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.resetPermissionOnUpdateRole(roleName, newRoleName, this)) {
                return;
            }
        }
        
        roleName = modify(roleName);
        newRoleName = modify(newRoleName);

        /*need to clear tenant authz cache when role is updated, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);
        
        String sqlStmt = DBConstants.UPDATE_UM_ROLE_NAME_PERMISSION_SQL;
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for update role name is null");
        }
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            permissionTree.updateRoleNameInCache(roleName, newRoleName);
            String domain = UserCoreUtil.extractDomainFromName(newRoleName);
            newRoleName = UserCoreUtil.removeDomainFromName(newRoleName);
            roleName = UserCoreUtil.removeDomainFromName(roleName);
            if (domain != null) {
				domain = domain.toUpperCase();
			}
            DatabaseUtil.updateDatabase(dbConnection, sqlStmt, newRoleName, roleName,tenantId, tenantId, domain);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void addAuthorization(String subject, String resourceId, String action,
                                    boolean authorized, boolean isRole) throws UserStoreException {
        short allow = 0;
        if(authorized){
            allow = UserCoreConstants.ALLOW;
        }
        if(isRole){
            addAuthorizationForRole(subject, resourceId, action, allow, false);
        } else {
            addAuthorizationForUser(subject, resourceId, action, allow, false);            
        }
    }
    
    private  void addAuthorizationForRole(String roleName, String resourceId, String action,
            short allow, boolean updateCache) throws UserStoreException {

        /*need to clear tenant authz cache once role authorization is added, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            int permissionId = this.getPermissionId(dbConnection, resourceId, action);
            if (permissionId == -1) {
                this.addPermissionId(dbConnection, resourceId, action);
                permissionId = this.getPermissionId(dbConnection, resourceId, action);
            }
            String domain = UserCoreUtil.extractDomainFromName(roleName);
            if (domain != null) {
                domain = domain.toUpperCase();
            }
            //check if system role
            boolean isSystemRole = UserCoreUtil.isSystemRole(roleName, this.tenantId, this.dataSource);

            if(isSystemRole){
                domain = UserCoreConstants.SYSTEM_DOMAIN_NAME;
            } else if(domain == null){
                // assume as primary domain
                domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
            }

            DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_ROLE_PERMISSION_SQL,
                    UserCoreUtil.removeDomainFromName(roleName), resourceId, action,
                    tenantId, tenantId, tenantId, domain);

			if (log.isDebugEnabled()) {
				log.debug("Adding permission Id: " + permissionId + " to the role: "
						+ UserCoreUtil.removeDomainFromName(roleName) + " of tenant: " + tenantId
						+ " of domain: " + domain + " to resource: " + resourceId);
			}
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.ADD_ROLE_PERMISSION_SQL,
                    permissionId, UserCoreUtil.removeDomainFromName(roleName), allow,
                    tenantId, tenantId, domain);
            
            if(updateCache){
                if (allow == UserCoreConstants.ALLOW) {
                    permissionTree.authorizeRoleInTree(roleName, resourceId, action, true);
                } else {
                    permissionTree.denyRoleInTree(roleName, resourceId, action, true);
                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    private void addAuthorizationForUser(String userName, String resourceId, String action,
            short allow, boolean updateCache) throws UserStoreException {
        /*need to clear tenant authz cache once role authorization is removed, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);
        
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            int permissionId = this.getPermissionId(dbConnection, resourceId, action);
            if (permissionId == -1) {
                this.addPermissionId(dbConnection, resourceId, action);
                permissionId = this.getPermissionId(dbConnection, resourceId, action);
            }
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_USER_PERMISSION_SQL,
                    userName, resourceId, action, tenantId, tenantId);
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.ADD_USER_PERMISSION_SQL,
                    permissionId, userName, allow, tenantId);
            if(updateCache){
                if (allow == UserCoreConstants.ALLOW) {
                    permissionTree.authorizeUserInTree(userName, resourceId, action, true);
                } else {
                    permissionTree.denyUserInTree(userName, resourceId, action, true);
                    authorizationCache.clearCacheEntry(cacheIdentifier, tenantId, userName, resourceId,
                                                                                                action);
                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    private List<String> getUIPermissionId() throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;        
        ResultSet rs = null;
        List<String> resourceIds = new ArrayList<String>(); 
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(DBConstants.GET_PERMISSION_SQL);
            prepStmt.setString(1, CarbonConstants.UI_PERMISSION_ACTION);
            prepStmt.setInt(2, tenantId);

            rs = prepStmt.executeQuery();
            if(rs != null){
                while(rs.next()) {
                    resourceIds.add(rs.getString(1));
                }
            }
            return resourceIds;
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }
    
    
    private int getPermissionId(Connection dbConnection, String resourceId, String action)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int value = -1;
        try {
            prepStmt = dbConnection.prepareStatement(DBConstants.GET_PERMISSION_ID_SQL);
            prepStmt.setString(1, resourceId);
            prepStmt.setString(2, action);
            prepStmt.setInt(3, tenantId);

            rs = prepStmt.executeQuery();
            if (rs.next()) {
                value = rs.getInt(1);
            }
            return value;
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    private void addPermissionId(Connection dbConnection, String resourceId, String action)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(DBConstants.ADD_PERMISSION_SQL);
            prepStmt.setString(1, resourceId);
            prepStmt.setString(2, action);
            prepStmt.setInt(3, tenantId);
            int count = prepStmt.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("Executed querry is " + DBConstants.ADD_PERMISSION_SQL
                        + " and number of updated rows :: " + count);
            }
        } catch (SQLException e) {
            log.error("Error! " + e.getMessage(), e);
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    private Connection getDBConnection() throws SQLException {
        Connection dbConnection = dataSource.getConnection();
        dbConnection.setAutoCommit(false);
        return dbConnection;
    }

    public  void populatePermissionTreeFromDB() throws UserStoreException {
        permissionTree.updatePermissionTreeFromDB();
    }

    /**
     * This method will unload all permission data loaded from a database. This method is useful in a lazy loading
     * scenario.
     */
    public void clearPermissionTree() {
        this.permissionTree.clear();
        this.authorizationCache.clearCache();
    }
    
    public int getTenantId() throws UserStoreException {
        return tenantId;
    }

    /**
     *
     * @param name
     * @return
     */
    private String modify(String name){
        if(caseInSensitiveAuthorizationRules && name != null){
            return name.toLowerCase();
        }
        return name;
    }

    /**
     *
     * @param names
     * @return
     */
    private String[] modify(String[] names){
        if(caseInSensitiveAuthorizationRules && names != null){
            List<String> list = new ArrayList<String>();
            for(String name : names){
                list.add(name.toLowerCase());
            }
            return list.toArray(new String[list.size()]);
        }
        return names;
    }

    private void addInitialData() throws UserStoreException {
        String mgtPermissions = realmConfig
                .getAuthorizationManagerProperty(UserCoreConstants.RealmConfig.PROPERTY_EVERYONEROLE_AUTHORIZATION);
        if (mgtPermissions != null) {
            String everyoneRole = realmConfig.getEveryOneRoleName();
            String[] resourceIds = mgtPermissions.split(",");
            for (String resourceId : resourceIds) {
                if (!this.isRoleAuthorized(everyoneRole, resourceId,
                        CarbonConstants.UI_PERMISSION_ACTION)) {
                    this.authorizeRole(everyoneRole, resourceId,
                            CarbonConstants.UI_PERMISSION_ACTION);
                }
            }
        }

        mgtPermissions = realmConfig
                .getAuthorizationManagerProperty(UserCoreConstants.RealmConfig.PROPERTY_ADMINROLE_AUTHORIZATION);
        if (mgtPermissions != null) {
            String[] resourceIds = mgtPermissions.split(",");
            String adminRole = realmConfig.getAdminRoleName();
            for (String resourceId : resourceIds) {
                if (!this.isRoleAuthorized(adminRole, resourceId,
                                           CarbonConstants.UI_PERMISSION_ACTION)) {
                    /* check whether admin role created in primary user store or as a hybrid role.
                     * if primary user store, & if not read only &/or if read ldap groups false,
                     * it is a hybrid role.
                     */
                    // as internal roles are created, role name must be appended with internal domain name
                    if (userRealm.getUserStoreManager().isReadOnly()) {
                        String readLDAPGroups = realmConfig.getUserStoreProperties().get(
                                LDAPConstants.READ_LDAP_GROUPS);
                        if (readLDAPGroups != null) {
                            if (!(Boolean.parseBoolean(readLDAPGroups))) {
                                this.authorizeRole(UserCoreConstants.INTERNAL_DOMAIN +
                                                   CarbonConstants.DOMAIN_SEPARATOR +
                                                   UserCoreUtil.removeDomainFromName(adminRole),
                                                   resourceId, CarbonConstants.UI_PERMISSION_ACTION);
                                return;
                            }
                        } else {
                            this.authorizeRole(UserCoreConstants.INTERNAL_DOMAIN +
                                               CarbonConstants.DOMAIN_SEPARATOR +
                                               UserCoreUtil.removeDomainFromName(adminRole),
                                               resourceId, CarbonConstants.UI_PERMISSION_ACTION);
                            return;
                        }
                    }
                    //if role is in external primary user store, prefix admin role with domain name
                    adminRole = UserCoreUtil.addDomainToName(adminRole, realmConfig.getUserStoreProperty(
                    UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
                    this.authorizeRole(adminRole, resourceId, CarbonConstants.UI_PERMISSION_ACTION);
                }
            }
        }
    }

	@Override
	public String[] normalizeRoles(String[] roles) {
		if (roles != null && roles.length > 0) {
			int index = 0;
			List<String> normalizedRoles = new ArrayList<String>();
			for (String role : roles) {
				if ((index = role.indexOf(UserCoreConstants.TENANT_DOMAIN_COMBINER.toLowerCase())) >= 0) {
					normalizedRoles.add(role.substring(0, index));
				} else {
					normalizedRoles.add(role);
				}
			}
			return normalizedRoles.toArray(new String[normalizedRoles.size()]);
		}
		return roles;
	}
}
