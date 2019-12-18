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

package org.wso2.carbon.user.core.listener;

import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;

import java.util.List;
import java.util.Map;

/**
 * This allows an extension point to implement various additional operations before and after
 * actual user operation is done.
 */
public interface UserOperationEventListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Define any additional actions before actual authentication is happen
     *
     * @param userName         User name of User
     * @param credential       Credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreAuthenticate(String userName, Object credential,
                                     UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after actual authentication is happen
     *
     * @param userName         User name of User
     * @param authenticated    where user is authenticated or not
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostAuthenticate(String userName, boolean authenticated,
                                      UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before user is added.
     *
     * @param userName         User name of User
     * @param credential       Credential/password of the user
     * @param roleList         role list of user
     * @param claims           Properties of the user
     * @param profile          profile name of user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreAddUser(String userName, Object credential, String[] roleList,
                                Map<String, String> claims, String profile,
                                UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after user is added.
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostAddUser(String userName, Object credential, String[] roleList,
                                 Map<String, String> claims, String profile,
                                 UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before credential is updated by user
     *
     * @param userName         User name of User
     * @param newCredential    new credential/password of the user
     * @param oldCredential    Old credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreUpdateCredential(String userName, Object newCredential,
                                         Object oldCredential,
                                         UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after credential is updated by user
     *
     * @param userName         User name of User
     * @param credential
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before credential is updated by Admin
     *
     * @param userName         User name of User
     * @param newCredential    new credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
                                                UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after credential is updated by Admin
     *
     * @param userName         User name of User
     * @param credential
     * @param userStoreManager The underlying UserStoreManager  @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */

    public boolean doPostUpdateCredentialByAdmin(String userName, Object credential,
                                                 UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before user is deleted by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after user is deleted by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attribute is set by Admin
     *
     * @param userName         User name of User
     * @param claimURI         claim uri
     * @param claimValue       claim value
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue,
                                          String profileName, UserStoreManager userStoreManager)
            throws UserStoreException;


    /**
     * Defines any additional actions after user attribute is set by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attributes are set by Admin
     *
     * @param userName         User name of User
     * @param claims           claim uri and claim value map
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims,
                                           String profileName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after user attributes are set by Admin
     *
     * @param userName         User name of User
     * @param claims
     * @param profileName
     * @param userStoreManager The underlying UserStoreManager  @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims,
                                            String profileName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attributes are deleted by Admin
     *
     * @param userName         User name of User
     * @param claims           claim uri and claim value map
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
                                              UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after user attributes are deleted by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attribute is deleted by Admin
     *
     * @param userName         User name of User
     * @param claimURI         claim uri
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
                                             UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after user attribute is deleted by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManagern
     */
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before adding a role.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions,
                                UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after adding a role.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions,
                                 UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before adding an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userList         List of users.
     * @param permissions      permissions.
     * @param userStoreManager user store manager.
     * @throws UserStoreException UserStoreException.
     */
    default boolean doPreAddInternalRole(String roleName, String[] userList, Permission[] permissions,
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
    default boolean doPostAddInternalRole(String roleName, String[] userList, Permission[] permissions,
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
    default boolean doPreUpdateInternalRoleName(String roleName, String newRoleName,
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
    default boolean doPostUpdateInternalRoleName(String roleName, String newRoleName,
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
    default boolean doPreDeleteInternalRole(String roleName, UserStoreManager userStoreManager)
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
    default boolean doPostDeleteInternalRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions before deleting a role.
     *
     * @param roleName
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before deleting a role.
     *
     * @param roleName
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before updating a role name.
     *
     * @param roleName
     * @param newRoleName
     * @return
     * @throws UserStoreException
     */
    public boolean doPreUpdateRoleName(String roleName, String newRoleName,
                                       UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after updating a role name.
     *
     * @param roleName
     * @param newRoleName
     * @return
     * @throws UserStoreException
     */
    public boolean doPostUpdateRoleName(String roleName, String newRoleName,
                                        UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before updating a role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @return
     * @throws UserStoreException
     */
    public boolean doPreUpdateUserListOfRole(String roleName, String deletedUsers[],
                                             String[] newUsers, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after updating a role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @return
     * @throws UserStoreException
     */
    public boolean doPostUpdateUserListOfRole(String roleName, String deletedUsers[],
                                              String[] newUsers, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before updating role list of user.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                             String[] newRoles,
                                             UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after updating role list of user.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                              String[] newRoles,
                                              UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Pre listener for the get user claim value method.
     *
     * @param userName     username.
     * @param claim        claim uri.
     * @param profileName  profile name.
     * @param storeManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    default boolean doPreGetUserClaimValue(String userName, String claim, String profileName,
            UserStoreManager storeManager) throws UserStoreException {

        return true;
    }

    /**
     * Pre listener for the get user claim values method.
     *
     * @param userName     username.
     * @param claims       claims uris.
     * @param profileName  profile name.
     * @param claimMap     claims map
     * @param storeManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    default boolean doPreGetUserClaimValues(String userName, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager storeManager) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the get user claim value method.
     *
     * @param userName     username.
     * @param claim        claims uri.
     * @param claimValue   claim value.
     * @param profileName  profile name.
     * @param storeManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    default boolean doPostGetUserClaimValue(String userName, String claim, List<String> claimValue, String profileName,
            UserStoreManager storeManager) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for the get user claim values method.
     *
     * @param userName     username
     * @param claims       claims uri.
     * @param profileName  profile name.
     * @param claimMap     claim map.
     * @param storeManager User store manager.
     * @return False if error.
     * @throws UserStoreException User Store Exception.
     */
    default boolean doPostGetUserClaimValues(String userName, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager storeManager) throws UserStoreException {

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
    default boolean doPreGetUserList(String claimUri, String claimValue, final List<String> returnUserNameList,
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
    default boolean doPreGetUserList(Condition condition, String domain, String profileName, int limit, int offset, String sortBy, String
            sortOrder, UserStoreManager userStoreManager) throws UserStoreException {

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
    default boolean doPreGetUserList(String claimUri, String claimValue, int limit, int offset, final List<String>
            returnUserNameList, UserStoreManager userStoreManager) throws UserStoreException {

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
    default boolean doPostGetUserList(String claimUri, String claimValue, final List<String> returnValues,
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
    default boolean doPostGetUserList(String claimUri, String claimValue, final List<String> returnValues, int limit,
            int offset, UserStoreManager userStoreManager) throws UserStoreException {

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
    default boolean doPostGetUserList(Condition condition, String domain, String profileName, int limit, int offset,
            String sortBy, String sortOrder, String[] users, UserStoreManager
            userStoreManager) throws UserStoreException {

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
    default boolean doPostGetPaginatedUserList(String claimUri, String claimValue, final List<String> returnValues,
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
    default boolean doPostListUsers(String filter, int limit, int offset, final List<String> returnValues,
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
    default boolean doPostGetRoleListOfUser(String userName, String filter, String[] roleList,
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
    default boolean doPostGetUserListOfRole(String roleName, String[] userList, UserStoreManager userStoreManager)
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
    default boolean doPostUpdatePermissionsOfRole(String roleName, Permission[] permissions, UserStoreManager
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
    default boolean doPostGetRoleListOfUsers(String[] userNames, Map<String, List<String>> rolesOfUsersMap)
            throws UserStoreException {

        return true;
    }



    /**
     * Post listener for get role list of users.
     *
     * @param userNames        user names
     * @param rolesOfUsersMap  map of roles against users
     * @param userStoreManager User Store Manager.
     * @return false in case of error
     * @throws UserStoreException UserStoreException
     */
    default boolean doPostGetRoleListOfUsers(String[] userNames, Map<String, List<String>> rolesOfUsersMap,
            UserStoreManager userStoreManager) throws UserStoreException {

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
    default boolean doPostGetUsersClaimValues(String[] userNames, String[] claims, String profileName,
            UserClaimSearchEntry[] userClaimSearchEntries) throws UserStoreException {

        return true;
    }

    /**
     * Post listener for get claim values of users
     *
     * @param userNames              user names
     * @param claims                 claims
     * @param profileName            profile name
     * @param userClaimSearchEntries user claim search entries
     * @param userStoreManager       User Store Manager.
     * @return false in case of error
     * @throws UserStoreException UserStoreException
     */
    default boolean doPostGetUsersClaimValues(String[] userNames, String[] claims, String profileName,
            UserClaimSearchEntry[] userClaimSearchEntries, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

}
