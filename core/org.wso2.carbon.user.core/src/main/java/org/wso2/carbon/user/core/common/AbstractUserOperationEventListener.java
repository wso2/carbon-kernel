/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UniqueIDUserOperationEventListener;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class AbstractUserOperationEventListener implements UniqueIDUserOperationEventListener {

    @Override
    public int getExecutionOrderId() {
        return 0;
    }

    @Override
    public boolean doPreAuthenticate(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before adding a role.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions after adding a role.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before adding an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userIDs          List of users IDs.
     * @param permissions      permissions.
     * @param userStoreManager user store manager.
     * @throws UserStoreException UserStoreException.
     */
    public boolean doPreAddInternalRoleWithID(String roleName, String[] userIDs, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions after adding an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userIDs          List of users IDs.
     * @param permissions      permissions
     * @param userStoreManager user store manager.
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostAddInternalRoleWithID(String roleName, String[] userIDs, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before deleting a role.
     *
     * @param roleName
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before deleting a role.
     *
     * @param roleName
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before updating a role name.
     *
     * @param roleName
     * @param newRoleName
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPreUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions after updating a role name.
     *
     * @param roleName
     * @param newRoleName
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPostUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before updating a role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions after updating a role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Define any additional actions before updating role list of user.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Define any additional actions after updating role list of user.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreGetUserClaimValueWithID(String userID, String claim, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetUserClaimValueWithID(String userID, String claim, List<String> claimValue,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreGetUserListWithID(String claimUri, String claimValue, final List<User> returnUsersList,
            UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreGetUserListWithID(Condition condition, String domain, String profileName, int limit, int offset,
            String sortBy, String sortOrder, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreGetUserListWithID(String claimUri, String claimValue, int limit, int offset,
            final List<User> returnUsersList, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, final List<User> returnValues, int limit,
            int offset, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(Condition condition, String domain, String profileName, int limit,
            int offset, String sortBy, String sortOrder, List<User> users, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreGetUserWithID(String userID, String[] requestedClaims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetUserWithID(String userID, String[] requestedClaims, String profileName, User user,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetPaginatedUserListWithID(String claimUri, String claimValue, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostListUsersWithID(String filter, int limit, int offset, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetRoleListOfUserWithID(String userName, String filter, String[] roleList,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetUserListOfRoleWithID(String roleName, List<User> userList,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetUsersClaimValuesWithID(List<String> userIDs, List<String> claims, String profileName,
            List<UniqueIDUserClaimSearchEntry> userClaimSearchEntries, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreAuthenticateWithID(String preferredUserNameClaim, String preferredUserNameValue,
            Object credential, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAuthenticateWithID(String preferredUserNameClaim, String preferredUserNameValue,
            AuthenticationResult authenticationResult, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreAuthenticateWithID(List<LoginIdentifier> loginIdentifiers, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAuthenticateWithID(List<LoginIdentifier> loginIdentifiers,
            AuthenticationResult authenticationResult, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreAuthenticateWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAuthenticateWithID(String userID, AuthenticationResult authenticationResult,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreAddUserWithID(String userID, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAddUserWithID(User user, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostUpdateCredentialWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdminWithID(String userID, Object newCredential,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdminWithID(String userID, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreDeleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostDeleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostSetUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValuesWithID(String userID, String[] claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValuesWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValueWithID(String userID, String claimURI, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreAddRoleWithID(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAddRoleWithID(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreUpdateUserListOfRoleWithID(String roleName, String[] deletedUsers, String[] newUsers,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostUpdateUserListOfRoleWithID(String roleName, String[] deletedUsers, String[] newUsers,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostGetRoleListOfUsersWithID(List<String> userIDs, Map<String, List<String>> rolesOfUsersMap,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

}
