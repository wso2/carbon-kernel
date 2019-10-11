/*
 *
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.wso2.carbon.user.core.listener;

import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.Map;

/**
 * This allows an extension point to implement various additional operations before and after
 * actual user operation is done.
 */
public interface UniqueIDUserOperationEventListener extends UserOperationEventListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Define any additional actions before actual authentication is happen
     *
     * @param userID           User name of User
     * @param credential       Credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreAuthenticateWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after actual authentication is happen
     *
     * @param userID           User name of User
     * @param authenticated    where user is authenticated or not
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostAuthenticateWithID(String userID, boolean authenticated, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before user is added.
     *
     * @param userID           User name of User
     * @param credential       Credential/password of the user
     * @param roleList         role list of user
     * @param claims           Properties of the user
     * @param profile          profile name of user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreAddUserWithID(String userID, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after user is added.
     *
     * @param userID           User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostAddUserWithID(String userID, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before credential is updated by user
     *
     * @param userID           User name of User
     * @param newCredential    new credential/password of the user
     * @param oldCredential    Old credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after credential is updated by user
     *
     * @param userID           User name of User
     * @param credential       user credentials
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostUpdateCredentialWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before credential is updated by Admin
     *
     * @param userID           User name of User
     * @param newCredential    new credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreUpdateCredentialByAdminWithID(String userID, Object newCredential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after credential is updated by Admin
     *
     * @param userID           User name of User
     * @param credential       user credentials
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */

    boolean doPostUpdateCredentialByAdminWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before user is deleted by Admin
     *
     * @param userID           User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreDeleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user is deleted by Admin
     *
     * @param userID           User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostDeleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before user attribute is set by Admin
     *
     * @param userID           User name of User
     * @param claimURI         claim uri
     * @param claimValue       claim value
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreSetUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user attribute is set by Admin
     *
     * @param userID           User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostSetUserClaimValueWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before user attributes are set by Admin
     *
     * @param userID           User name of User
     * @param claims           claim uri and claim value map
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user attributes are set by Admin
     *
     * @param userID           User name of User
     * @param claims           user claims
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before user attributes are deleted by Admin
     *
     * @param userID           User name of User
     * @param claims           claim uri and claim value map
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreDeleteUserClaimValuesWithID(String userID, String[] claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user attributes are deleted by Admin
     *
     * @param userID           User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostDeleteUserClaimValuesWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attribute is deleted by Admin
     *
     * @param userID           User name of User
     * @param claimURI         claim uri
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreDeleteUserClaimValueWithID(String userID, String claimURI, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user attribute is deleted by Admin
     *
     * @param userID           User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostDeleteUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before adding a role.
     *
     * @param roleName         role names
     * @param userList         user List
     * @param permissions      permissions
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreAddRoleWithID(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after adding a role.
     *
     * @param roleName         role names
     * @param userList         user List
     * @param permissions      permissions
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostAddRoleWithID(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before updating a role.
     *
     * @param roleName     role names
     * @param deletedUsers deleted user IDs
     * @param newUsers     new user IDs
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreUpdateUserListOfRoleWithID(String roleName, String deletedUsers[], String[] newUsers,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after updating a role.
     *
     * @param roleName     role names
     * @param deletedUsers deleted user IDs
     * @param newUsers     new user IDs
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostUpdateUserListOfRoleWithID(String roleName, String deletedUsers[], String[] newUsers,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before updating role list of user.
     *
     * @param userID           user ID
     * @param deletedRoles     deleted roles
     * @param newRoles         new roles
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPreUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after updating role list of user.
     *
     * @param userID           user ID
     * @param deletedRoles     deleted roles
     * @param newRoles         new roles
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean doPostUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) throws UserStoreException;
}
