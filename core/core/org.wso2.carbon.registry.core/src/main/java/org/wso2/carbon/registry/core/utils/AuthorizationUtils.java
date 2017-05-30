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

package org.wso2.carbon.registry.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

/**
 * Set of utilities related to authorization functionality.
 */
public class AuthorizationUtils {

    private static final Log log = LogFactory.getLog(AuthorizationUtils.class);

    /**
     * Method to authorize a given resource path for a given action.
     *
     * @param resourcePath the resource path.
     * @param action       the action.
     *
     * @return whether the user is authorized or not.
     * @throws RegistryException if the operation failed.
     */
    public static boolean authorize(String resourcePath, String action) throws RegistryException {
        UserRealm userRealm = CurrentSession.getUserRealm();
        String userName = CurrentSession.getUser();
        try {
            if (!userRealm.getAuthorizationManager()
                    .isUserAuthorized(userName, resourcePath, action)) {
                return false;
            }
        } catch (UserStoreException e) {
            String msg = "Could not check authorization. \nCaused by " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        return true;
    }

    /**
     * Method to set authorizations for an anonymous user.
     *
     * @param path      the resource path.
     * @param userRealm the user realm
     *
     * @throws RegistryException if the operation failed.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static void setAnonAuthorization(String path, UserRealm userRealm)
            throws RegistryException {

        throw new UnsupportedOperationException("This method is no longer supported");
        
        /*if (userRealm == null) {
            return;
        }

        try {
            AuthorizationManager accessControlAdmin = userRealm.getAuthorizationManager();
            RealmConfiguration realmConfig;
            try {
                realmConfig = userRealm.getRealmConfiguration();
            } catch (UserStoreException e) {
                String msg = "Failed to retrieve realm configuration.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            String anonymousUserName = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;

            accessControlAdmin.authorizeUser(anonymousUserName, path, ActionConstants.GET);
            accessControlAdmin.denyUser(anonymousUserName, path, ActionConstants.PUT);
            accessControlAdmin.denyUser(anonymousUserName, path, ActionConstants.DELETE);
            accessControlAdmin.denyUser(anonymousUserName, path, AccessControlConstants.AUTHORIZE);

            String everyoneRole = realmConfig.getEveryOneRoleName();

            accessControlAdmin.authorizeRole(everyoneRole, path, ActionConstants.GET);
            accessControlAdmin.denyRole(everyoneRole, path, ActionConstants.PUT);
            accessControlAdmin.denyRole(everyoneRole, path, ActionConstants.DELETE);
            accessControlAdmin.denyRole(everyoneRole, path, AccessControlConstants.AUTHORIZE);

        } catch (UserStoreException e) {
            String msg = "Could not set authorizations for the " + path + ". \nCaused by: "
                    + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg);
        }*/
    }

    /**
     * Method to clear authorizations for a given resource path.
     *
     * @param resourcePath the resource path.
     *
     * @throws RegistryException if the operation failed.
     */
    public static void clearAuthorizations(String resourcePath) throws RegistryException {

        UserRealm userRealm = CurrentSession.getUserRealm();

        try {
            userRealm.getAuthorizationManager().clearResourceAuthorizations(resourcePath);

        } catch (UserStoreException e) {

            String msg = "Could not clear authorizations. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Method to copy authorizations from one path to another.
     *
     * @param sourcePath the source path (where to copy from)
     * @param targetPath the target path (where to copy to)
     *
     * @throws RegistryException if the operation failed.
     */
    public static void copyAuthorizations(String sourcePath, String targetPath)
            throws RegistryException {

        UserRealm userRealm = CurrentSession.getUserRealm();
        try {
            if (sourcePath != null && targetPath != null) {
                clearAuthorizations(targetPath);
                if (RegistryUtils.getParentPath(sourcePath).equals(
                        RegistryUtils.getParentPath(targetPath))) {
                    String[] actions = {ActionConstants.GET, ActionConstants.PUT,
                            ActionConstants.DELETE, AccessControlConstants.AUTHORIZE};
                    for (String action : actions) {
                        // Authorize required roles
                        String[] roles = userRealm.getAuthorizationManager().
                                getAllowedRolesForResource(sourcePath, action);
                        if (roles != null && roles.length > 0) {
                            for (String role : roles) {
                                if (!userRealm.getAuthorizationManager().isRoleAuthorized(
                                        role, targetPath, action)) {
                                    userRealm.getAuthorizationManager().authorizeRole(role,
                                            targetPath, action);
                                }
                            }
                        }
                        // Deny required roles
                        roles = userRealm.getAuthorizationManager().getDeniedRolesForResource(
                                sourcePath, action);
                        if (roles != null && roles.length > 0) {
                            for (String role : roles) {
                                if (userRealm.getAuthorizationManager().isRoleAuthorized(
                                        role, targetPath, action)) {
                                    userRealm.getAuthorizationManager().denyRole(role,
                                            targetPath, action);
                                }
                            }
                        }
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = "Could not copy authorizations to the " + targetPath + ". \nCaused by: "
                    + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg);
        }
    }

    /**
     * Method to deny anonymous authorizations to the given path.
     *
     * @param path      the path.
     * @param userRealm the user realm to use.
     *
     * @throws RegistryException if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static void denyAnonAuthorization(String path, UserRealm userRealm)
            throws RegistryException {

        throw new UnsupportedOperationException("This method is no longer supported");

        /*if (userRealm == null) {
            return;
        }

        try {
            AuthorizationManager accessControlAdmin = userRealm.getAuthorizationManager();
            RealmConfiguration realmConfig;
            try {
                realmConfig = userRealm.getRealmConfiguration();
            } catch (UserStoreException e) {
                String msg = "Failed to retrieve realm configuration.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            String anonymousUserName = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;

            accessControlAdmin.denyUser(anonymousUserName, path, ActionConstants.GET);
            accessControlAdmin.denyUser(anonymousUserName, path, ActionConstants.PUT);
            accessControlAdmin.denyUser(anonymousUserName, path, ActionConstants.DELETE);
            accessControlAdmin.denyUser(anonymousUserName, path, AccessControlConstants.AUTHORIZE);

            String everyoneRole = realmConfig.getEveryOneRoleName();

            accessControlAdmin.denyRole(everyoneRole, path, ActionConstants.GET);
            accessControlAdmin.denyRole(everyoneRole, path, ActionConstants.PUT);
            accessControlAdmin.denyRole(everyoneRole, path, ActionConstants.DELETE);
            accessControlAdmin.denyRole(everyoneRole, path, AccessControlConstants.AUTHORIZE);

        } catch (UserStoreException e) {
            String msg = "Could not clear authorizations for the " + path + ". \nCaused by: "
                    + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg);
        }*/
    }

    /**
     * Method to set authorizations to the root path.
     *
     * @param rootPath  the root path.
     * @param userRealm the user realm to use.
     *
     * @throws RegistryException if the operation failed.
     */
    public static void setRootAuthorizations(String rootPath, UserRealm userRealm)
            throws RegistryException {

        if (userRealm == null) {
            return;
        }

        try {
            AuthorizationManager accessControlAdmin = userRealm.getAuthorizationManager();
            RealmConfiguration realmConfig;
            try {
                realmConfig = userRealm.getRealmConfiguration();
            } catch (UserStoreException e) {
                String msg = "Failed to retrieve realm configuration.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }

            String adminRoleName = realmConfig.getAdminRoleName();
            String everyoneRoleName = realmConfig.getEveryOneRoleName();

            if (!accessControlAdmin.isRoleAuthorized(adminRoleName, rootPath,
                    ActionConstants.GET)) {
                accessControlAdmin.authorizeRole(adminRoleName, rootPath,
                        ActionConstants.GET);
            }
            if (!accessControlAdmin.isRoleAuthorized(adminRoleName, rootPath,
                    ActionConstants.PUT)) {
                accessControlAdmin.authorizeRole(adminRoleName, rootPath,
                        ActionConstants.PUT);
            }
            if (!accessControlAdmin.isRoleAuthorized(adminRoleName, rootPath,
                    ActionConstants.DELETE)) {
                accessControlAdmin.authorizeRole(adminRoleName, rootPath,
                        ActionConstants.DELETE);
            }
            if (!accessControlAdmin.isRoleAuthorized(adminRoleName, rootPath,
                    AccessControlConstants.AUTHORIZE)) {
                accessControlAdmin.authorizeRole(adminRoleName, rootPath,
                        AccessControlConstants.AUTHORIZE);
            }
            if (!accessControlAdmin.isRoleAuthorized(everyoneRoleName, rootPath,
                    ActionConstants.GET)) {
                accessControlAdmin.authorizeRole(everyoneRoleName, rootPath,
                        ActionConstants.GET);
            }

        } catch (UserStoreException e) {
            String msg = "Could not set authorizations for the root. \nCaused by: "
                    + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg);
        }
    }

    /**
     * Populates all necessary users, roles and authorizations related user store. Note that the
     * authorizations related to resource store is not populated by this method.
     *
     * @param realm Realm for which data has to be populated
     *
     * @throws UserStoreException if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static void populateUserStore(UserRealm realm) throws UserStoreException {

        throw new UnsupportedOperationException("This method is no longer used");

        /*if (realm == null) {
            return;
        }

        UserStoreManager userStoreAdmin = realm.getUserStoreManager();
        AuthorizationManager accessControlAdmin = realm.getAuthorizationManager();

        RealmConfiguration realmConfig = realm.getRealmConfiguration();
        String adminRoleName = realmConfig.getAdminRoleName();

        if (!userStoreAdmin.isExistingRole(adminRoleName)) {
            userStoreAdmin.addRole(adminRoleName, null, null);
        }

        // adding the admin role and granting permissions are two independent
        // tasks. There can be
        // scenarios where the admin role exists without any permissions (e.g.
        // admin role defined
        // in external user store)
        if (!accessControlAdmin.isRoleAuthorized(adminRoleName,
                AccessControlConstants.USER_RESOURCE, AccessControlConstants.ADD)) {
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.USER_RESOURCE, AccessControlConstants.ADD);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.USER_RESOURCE, AccessControlConstants.READ);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.USER_RESOURCE, AccessControlConstants.EDIT);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.USER_RESOURCE, AccessControlConstants.DELETE);

            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.ROLE_RESOURCE, AccessControlConstants.ADD);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.ROLE_RESOURCE, AccessControlConstants.READ);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.ROLE_RESOURCE, AccessControlConstants.EDIT);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.ROLE_RESOURCE, AccessControlConstants.DELETE);

            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.USER_PERMISSION_RESOURCE, AccessControlConstants.READ);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.USER_PERMISSION_RESOURCE, AccessControlConstants.ADD);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.USER_PERMISSION_RESOURCE, AccessControlConstants.EDIT);
            accessControlAdmin.authorizeRole(adminRoleName,
                    AccessControlConstants.USER_PERMISSION_RESOURCE, AccessControlConstants.DELETE);
        }
        String everyoneRoleName = realmConfig.getEveryOneRoleName();
        if (!userStoreAdmin.isExistingRole(everyoneRoleName)) {
            userStoreAdmin.addRole(everyoneRoleName, null, null);
        }

        if (!accessControlAdmin.isRoleAuthorized(everyoneRoleName,
                AccessControlConstants.USER_RESOURCE, AccessControlConstants.READ)) {
            accessControlAdmin.authorizeRole(everyoneRoleName,
                    AccessControlConstants.USER_RESOURCE, AccessControlConstants.READ);
            accessControlAdmin.authorizeRole(everyoneRoleName,
                    AccessControlConstants.ROLE_RESOURCE, AccessControlConstants.READ);
            accessControlAdmin.authorizeRole(everyoneRoleName,
                    AccessControlConstants.USER_PERMISSION_RESOURCE, AccessControlConstants.READ);
        }*/
    }

    /**
     * Path of a resource given to the Registry interface may contain extensions to refer meta data
     * about resources. But we always store the authorization for resources against the pure
     * resource path, stored in the ARTIFACTS table. This methods extracts that pure resource path
     * from a given path.
     *
     * @param resourcePath A path string, which may contain extensions
     *
     * @return pure resource path for the given path
     */
    public static String getAuthorizationPath(String resourcePath) {

        // if the user has permission to the current version, he will get
        // permission to all
        // previous versions of the same resource

        String preparedPath = resourcePath;
        if (resourcePath.indexOf('?') > 0) {
            preparedPath = resourcePath.split("\\?")[0];
        } else if (resourcePath.indexOf(RegistryConstants.URL_SEPARATOR) > 0) {
            preparedPath = resourcePath.split("\\;")[0];
        }

        if (preparedPath.equals(RegistryConstants.ROOT_PATH)) {
            return preparedPath;

        } else {

            if (!preparedPath.startsWith(RegistryConstants.ROOT_PATH)) {
                preparedPath = RegistryConstants.ROOT_PATH + preparedPath;
            }

            if (preparedPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                preparedPath = preparedPath.substring(0, preparedPath.length() - 1);
            }
        }

        return preparedPath;
    }

    /**
     * Adds listener to intercept authorizeRole and clearRoleActionOnAllResources operations. This
     * method must only be used inside the registry kernel and its fragments.
     *
     * @param executionId   an identifier which determines the order in which this listener is
     *                      called.
     * @param path          the path to which authorizations should be granted on the repository.
     * @param permission    the corresponding UI permission
     * @param executeAction the execute action used by the User Management bundle
     * @param actions       the actions which we authorize this role for the given path
     *
     * @see org.wso2.carbon.user.core.AuthorizationManager#authorizeRole(String, String, String)
     * @see org.wso2.carbon.user.core.AuthorizationManager#clearRoleActionOnAllResources(String,
     *      String)
     */
    @SuppressWarnings("unused")
    // Used outside the registry kernel.
    public static void addAuthorizeRoleListener(int executionId, String path, String permission,
                                                String executeAction, String[] actions) {
        RegistryCoreServiceComponent.addAuthorizeRoleListener(executionId, path, permission,
                executeAction, actions);
    }

    /**
     * Adds listener to intercept authorizeRole and clearRoleActionOnAllResources operations. This
     * method must only be used inside the registry kernel and its fragments.
     *
     * @param executionId   an identifier which determines the order in which this listener is
     *                      called.
     * @param path          the path to which authorizations should be granted on the repository.
     * @param permission    the corresponding UI permission
     * @param executeAction the execute action used by the User Management bundle
     *
     * @see org.wso2.carbon.user.core.AuthorizationManager#authorizeRole(String, String, String)
     * @see org.wso2.carbon.user.core.AuthorizationManager#clearRoleActionOnAllResources(String,
     *      String)
     */
    @SuppressWarnings("unused")
    // Used outside the registry kernel.
    public static void addAuthorizeRoleListener(int executionId, String path, String permission,
                                                String executeAction) {
        RegistryCoreServiceComponent.addAuthorizeRoleListener(executionId, path, permission,
                executeAction, null);
    }
}
