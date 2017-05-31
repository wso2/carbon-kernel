/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core;

public interface AuthorizationManager extends org.wso2.carbon.user.api.AuthorizationManager {

    /**
     * Checks for user authorization
     *
     * @param userName   The user name
     * @param resourceId Resource Id String
     * @param action     The action user is trying to perform
     * @return Returns true when user is authorized to perform the action on the
     * resource and false otherwise.
     * @throws UserStoreException
     */
    boolean isUserAuthorized(String userName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Checks for role authorization.
     *
     * @param roleName   The role name
     * @param resourceId Resource Id String
     * @param action     The action the role is trying to perform
     * @return Returns true when the role is authorized to perform the action on
     * the resource and false otherwise
     * @throws UserStoreException
     */
    boolean isRoleAuthorized(String roleName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Retrieves a list of users allowed to perform the given action on the
     * resource
     *
     * @param resourceId Resource Id String
     * @param action     The action that is allowed to perform
     * @return Returns a list of users allowed to perform the given action on
     * the resource
     * @throws UserStoreException
     * @deprecated
     */
    String[] getExplicitlyAllowedUsersForResource(String resourceId, String action)
            throws UserStoreException;

    /**
     * Retrieves a list of roles allowed to perform the given action on the
     * resource
     *
     * @param resourceId Resource Id String
     * @param action     The action that is allowed to perform
     * @return Returns a list of roles allowed to perform the given action on
     * the resource
     * @throws UserStoreException
     */
    String[] getAllowedRolesForResource(String resourceId, String action)
            throws UserStoreException;

    String[] getDeniedRolesForResource(String resourceId, String action)
            throws UserStoreException;

    /**
     * @param resourceId
     * @param action
     * @return
     * @throws UserStoreException
     * @deprecated
     */
    String[] getExplicitlyDeniedUsersForResource(String resourceId, String action)
            throws UserStoreException;

    /**
     * Grants authorizations to a user to perform an action on a resource.
     *
     * @param userName   The user name
     * @param resourceId Resource identification string
     * @param action     The action granted to the user
     * @throws UserStoreException
     * @deprecated
     */
    void authorizeUser(String userName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Grants authorizes to a role to perform an action on a resource.
     *
     * @param roleName   The role name
     * @param resourceId Resource identification string
     * @param action     The action granted to the role
     * @throws UserStoreException
     */
    void authorizeRole(String roleName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Deny authorizations to a user to perform an action on a resource.
     *
     * @param userName   The user name
     * @param resourceId Resource identification string
     * @param action     The action granted to the user
     * @throws UserStoreException
     * @deprecated
     */
    void denyUser(String userName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Deny authorizations to a role to perform an action on a resource.
     *
     * @param roleName   The role name
     * @param resourceId Resource identification string
     * @param action     The action granted to the role
     * @throws UserStoreException
     */
    void denyRole(String roleName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Deletes an already granted authorization of a user.
     *
     * @param userName   The user name
     * @param resourceId Resource identification string
     * @param action     The action granted
     * @throws UserStoreException
     * @deprecated
     */
    void clearUserAuthorization(String userName, String resourceId, String action)
            throws UserStoreException;

    /**
     * @param userName
     * @throws UserStoreException
     * @deprecated
     */
    void clearUserAuthorization(String userName) throws UserStoreException;

    /**
     * Deletes an already granted authorization of a role.
     *
     * @param roleName   The role name
     * @param resourceId Resource identification string
     * @param action     The action granted
     * @throws UserStoreException
     */
    void clearRoleAuthorization(String roleName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Deletes the role's right to perform the action on all resources.
     *
     * @param roleName The role name
     * @param action   The action granted
     * @throws UserStoreException
     */
    void clearRoleActionOnAllResources(String roleName, String action)
            throws UserStoreException;

    /**
     * Used when deleting roles.
     *
     * @param roleName
     * @throws UserStoreException
     */
    void clearRoleAuthorization(String roleName) throws UserStoreException;

    /**
     * Deletes all granted authorization on a resource.
     *
     * @param resourceId Resource identification string
     * @throws UserStoreException
     */
    void clearResourceAuthorizations(String resourceId) throws UserStoreException;

    /**
     * Returns the complete set of UI resources allowed for User.
     *
     * @param userName
     * @return
     * @throws UserStoreException
     */
    String[] getAllowedUIResourcesForUser(String userName, String permissionRootPath)
            throws UserStoreException;


    /**
     * this will get the tenant id associated with the user authorization manager
     *
     * @return the tenant id of the authorization manager
     * @throws UserStoreException if the operation failed
     */
    int getTenantId() throws UserStoreException;


    /**
     * this will reset the permission of the renamed role
     *
     * @param roleName
     * @param newRoleName
     */
    void resetPermissionOnUpdateRole(String roleName, String newRoleName)
            throws UserStoreException;

    /**
     * This method should be used to remove the distinguished names from the
     * roles and to do any other normalization activity
     *
     * @param roles
     * @return
     */
    String[] normalizeRoles(String[] roles);

}
