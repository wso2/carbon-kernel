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
    public boolean doPreAuthenticate(String userName, Object credential,
                                     UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated,
                                      UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList,
                                Map<String, String> claims, String profile, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList,
                                 Map<String, String> claims, String profile,
                                 UserStoreManager userStoreManager)
            throws UserStoreException {
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
    public boolean doPostUpdateCredentialByAdmin(String userName,
                                                 Object credential,
                                                 UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreDeleteUser(String userName,
                                   UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName,
                                    UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue,
                                          String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims,
                                           String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims,
                                            String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {
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
     * @param userList         List of users.
     * @param permissions      permissions.
     * @param userStoreManager user store manager.
     * @throws UserStoreException UserStoreException.
     */
    public boolean doPreAddInternalRole(String roleName, String[] userList, Permission[] permissions,
                                        UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before adding an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userIDs         List of users IDs.
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
     * @param userList         List of users.
     * @param permissions      permissions
     * @param userStoreManager user store manager.
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostAddInternalRole(String roleName, String[] userList, Permission[] permissions,
                                         UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions after adding an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userIDs         List of users IDs.
     * @param permissions      permissions
     * @param userStoreManager user store manager.
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostAddInternalRoleWithID(String roleName, String[] userIDs, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before updating an internal role name.
     *
     * @param roleName    current internal role Name.
     * @param newRoleName new internal role Name.
     * @throws org.wso2.carbon.user.core.UserStoreException UserStoreException
     */
    public boolean doPreUpdateInternalRoleName(String roleName, String newRoleName,
                                               UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions after updating an internal role name.
     *
     * @param roleName    current internal role name.
     * @param newRoleName new internal role name.
     * @throws org.wso2.carbon.user.core.UserStoreException UserStoreException.
     */
    public boolean doPostUpdateInternalRoleName(String roleName, String newRoleName,
                                                UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before deleting an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userStoreManager user store manager.
     * @throws org.wso2.carbon.user.core.UserStoreException UserStoreException
     */
    public boolean doPreDeleteInternalRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before deleting an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userStoreManager user store manager.
     * @throws org.wso2.carbon.user.core.UserStoreException UserStoreException
     */
    public boolean doPostDeleteInternalRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {

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
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {
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
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {
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
    public boolean doPreUpdateRoleName(String roleName, String newRoleName,
                                       UserStoreManager userStoreManager)
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
    public boolean doPostUpdateRoleName(String roleName, String newRoleName,
                                        UserStoreManager userStoreManager)
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
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers,
                                             String[] newUsers, UserStoreManager userStoreManager)
            throws UserStoreException {
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
    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers,
                                              String[] newUsers, UserStoreManager userStoreManager)
            throws UserStoreException {
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
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                             String[] newRoles, UserStoreManager userStoreManager)
            throws UserStoreException {
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
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                              String[] newRoles, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    public boolean doPreGetUserClaimValue(String userName, String claim, String profileName,
                                          UserStoreManager storeManager) throws UserStoreException {
        return true;
    }

    public boolean doPreGetUserClaimValueWithID(String userID, String claim, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    public boolean doPreGetUserClaimValues(String userName, String[] claims,
                                           String profileName, Map<String, String> claimMap, UserStoreManager storeManager) throws UserStoreException {
        return true;
    }

    public boolean doPreGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    public boolean doPostGetUserClaimValue(String userName, String claim, List<String> claimValue,
                                           String profileName, UserStoreManager storeManager) throws UserStoreException {
        return true;
    }

    public boolean doPostGetUserClaimValueWithID(String userID, String claim, List<String> claimValue,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    public boolean doPostGetUserClaimValues(String userName, String[] claims,
                                            String profileName, Map<String, String> claimMap, UserStoreManager storeManager) throws UserStoreException {
        return true;
    }

    public boolean doPostGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Pre listener for the get user list method.
     * @param claimUri Claim URI.
     * @param claimValue Value of the given claim URI.
     * @param returnUserNameList List of user names that this listiner will return.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPreGetUserList(String claimUri, String claimValue, final List<String> returnUserNameList,
                                    UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Pre listener for the get user list method.
     * @param claimUri Claim URI.
     * @param claimValue Value of the given claim URI.
     * @param returnUsersList List of users that this listiner will return.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPreGetUserListWithID(String claimUri, String claimValue, final List<User> returnUsersList,
            UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Pre listener for the get paginated  conditional user list method.
     *
     * @param condition        condition.
     * @param domain           user store domain.
     * @param profileName      profile name.
     * @param limit            number of search results.
     * @param offset           start index of the search.
     * @param sortBy           sort By attribute
     * @param sortOrder        sort order.
     * @param userStoreManager userStoreManager.
     * @throws UserStoreException UserStoreException
     */
    public boolean doPreGetUserList(Condition condition, String domain, String profileName, int limit, int offset, String sortBy, String
            sortOrder, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Pre listener for the get paginated  conditional user list method.
     *
     * @param condition        condition.
     * @param domain           user store domain.
     * @param profileName      profile name.
     * @param limit            number of search results.
     * @param offset           start index of the search.
     * @param sortBy           sort By attribute
     * @param sortOrder        sort order.
     * @param userStoreManager userStoreManager.
     * @throws UserStoreException UserStoreException
     */
    public boolean doPreGetUserListWithID(Condition condition, String domain, String profileName, int limit, int offset,
            String sortBy, String sortOrder, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }


    /**
     * Pre listener for the get paginated user list method.
     *
     * @param claimUri           Claim URI.
     * @param claimValue         Value of the given claim URI.
     * @param limit              No of search results.
     * @param offset             Start index of the search.
     * @param returnUserNameList List of user names that this listener will return.
     * @param userStoreManager   User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPreGetUserList(String claimUri, String claimValue, int limit, int offset, final List<String>
            returnUserNameList, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Pre listener for the get paginated user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param returnUsersList  List of users that this listener will return.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPreGetUserListWithID(String claimUri, String claimValue, int limit, int offset,
            final List<User> returnUsersList, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the get user list method.
     * @param claimUri Claim URI.
     * @param claimValue Value of the given claim URI.
     * @param returnValues Values to be returned.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostGetUserList(String claimUri, String claimValue, final List<String> returnValues,
                                     UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Post listener for the get user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param returnValues     Values to be returned.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the get user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param returnValues     Values to be returned.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostGetUserList(String claimUri, String claimValue, final List<String> returnValues, int limit,
                                     int offset, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the get user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param returnValues     Values to be returned.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, final List<User> returnValues,
            int limit, int offset, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the get user conditional list method.
     *
     * @param condition        condition.
     * @param domain           user store domain.
     * @param profileName      profile name.
     * @param limit            number of search results.
     * @param offset           start index of the search.
     * @param sortBy           sort by attribute.
     * @param sortOrder        sort order.
     * @param userStoreManager user store manager.
     * @param users            Filtered user list
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostGetUserList(Condition condition, String domain, String profileName, int limit, int offset,
                                     String sortBy, String sortOrder, String[] users, UserStoreManager
                                             userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the get user conditional list method.
     *
     * @param condition        condition.
     * @param domain           user store domain.
     * @param profileName      profile name.
     * @param limit            number of search results.
     * @param offset           start index of the search.
     * @param sortBy           sort by attribute.
     * @param sortOrder        sort order.
     * @param userStoreManager user store manager.
     * @param users            Filtered user list
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostGetUserListWithID(Condition condition, String domain, String profileName, int limit,
            int offset, String sortBy, String sortOrder, String[] users, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the get paginated user list method.
     * @param claimUri Claim URI.
     * @param claimValue Value of the given claim URI.
     * @param returnValues Values to be returned.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostGetPaginatedUserList(String claimUri, String claimValue, final List<String> returnValues,
                                     UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Post listener for the get paginated user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param returnValues     Values to be returned.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostGetPaginatedUserListWithID(String claimUri, String claimValue, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Post listener for the list paginated users method.
     *
     * @param filter           username filter.
     * @param limit            No of search results.
     * @param offset           start index of the search.
     * @param returnValues     Values to be returned.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostListUsers(String filter, int limit, int offset, final List<String> returnValues,
                                   UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the list paginated users method.
     *
     * @param filter           username filter.
     * @param limit            No of search results.
     * @param offset           start index of the search.
     * @param returnValues     Values to be returned.
     * @param userStoreManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostListUsersWithID(String filter, int limit, int offset, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Any additional tasks that need to be done after getting the role list of a user.
     *
     * @param userName         Name of the user.
     * @param filter           Relevant filter.
     * @param roleList         List of roles.
     * @param userStoreManager User Store Manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception
     */
    public boolean doPostGetRoleListOfUser(String userName, String filter, String[] roleList,
            UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Any additional tasks that need to be done after getting the role list of a user.
     *
     * @param userName         Name of the user.
     * @param filter           Relevant filter.
     * @param roleList         List of roles.
     * @param userStoreManager User Store Manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception
     */
    public boolean doPostGetRoleListOfUserWithID(String userName, String filter, String[] roleList,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Any additional tasks that need to be done after getting user list a role.
     *
     * @param roleName         Name of the role.
     * @param userList         List of users.
     * @param userStoreManager User Store Manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException UserStore Exception.
     */
    public boolean doPostGetUserListOfRole(String roleName, String[] userList, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    /**
     * Any additional tasks that need to be done after getting user list a role.
     *
     * @param roleName         Name of the role.
     * @param userList         List of users.
     * @param userStoreManager User Store Manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException UserStore Exception.
     */
    public boolean doPostGetUserListOfRoleWithID(String roleName, User[] userList, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Any additional tasks that need to be done after updating permissions of a role.
     *
     * @param roleName         Name of the role.
     * @param permissions      Permissions related with role.
     * @param userStoreManager User Store Manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    public boolean doPostUpdatePermissionsOfRole(String roleName, Permission[] permissions, UserStoreManager
            userStoreManager) throws UserStoreException {
        return true;
    }


    /**
     * Post listener for get role list of users.
     *
     * @param userNames       user names
     * @param rolesOfUsersMap map of roles against users
     * @return false in case of error
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostGetRoleListOfUsers(String[] userNames, Map<String, List<String>> rolesOfUsersMap)
            throws UserStoreException {

        return true;
    }

    /**
     * Post listener for get role list of users.
     *
     * @param userNames       user names
     * @param rolesOfUsersMap map of roles against users
     * @return false in case of error
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostGetRoleListOfUsersWithID(String[] userNames, Map<String, List<String>> rolesOfUsersMap)
            throws UserStoreException {

        return true;
    }

    /**
     * Post listener for get claim values of users
     *
     * @param userNames              user names
     * @param claims                 claims
     * @param profileName            profile name
     * @param userClaimSearchEntries user claim search entries
     * @return false in case of error
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostGetUsersClaimValues(String[] userNames, String[] claims, String profileName,
            UserClaimSearchEntry[] userClaimSearchEntries) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for get claim values of users
     *
     * @param userIDs                user names
     * @param claims                 claims
     * @param profileName            profile name
     * @param userClaimSearchEntries user claim search entries
     * @return false in case of error
     * @throws UserStoreException UserStoreException
     */
    public boolean doPostGetUsersClaimValuesWithID(String[] userIDs, String[] claims, String profileName,
            UserClaimSearchEntry[] userClaimSearchEntries) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreAuthenticateWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAuthenticateWithID(String userID, boolean authenticated, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPreAddUserWithID(String userID, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean doPostAddUserWithID(String userID, Object credential, String[] roleList, Map<String, String> claims,
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
}
