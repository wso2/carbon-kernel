/*
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
 */

package org.wso2.carbon.user.core.listener;

import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;

import java.util.List;
import java.util.Map;

/**
 * This allows an extension point to implement various additional operations before and after
 * actual user operation is done.
 */
public interface UniqueIDUserOperationEventListener extends UserOperationEventListener {

    /**
     * Defines any additional actions before adding an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userIDs          List of users IDs.
     * @param permissions      permissions.
     * @param userStoreManager user store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPreAddInternalRoleWithID(String roleName, String[] userIDs, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after adding an internal role.
     *
     * @param roleName         Internal Role Name.
     * @param userIDs          List of users IDs.
     * @param permissions      permissions.
     * @param userStoreManager user store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPostAddInternalRoleWithID(String roleName, String[] userIDs, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before getting user claim value.
     *
     * @param userID           user ID.
     * @param claim            claim uri.
     * @param profileName      profile name.
     * @param userStoreManager user store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPreGetUserClaimValueWithID(String userID, String claim, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before getting user claim values.
     *
     * @param userID           user ID.
     * @param claims           claim uris.
     * @param profileName      profile name.
     * @param claimMap         claims map.
     * @param userStoreManager user store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPreGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after getting user claim value.
     *
     * @param userID           user ID.
     * @param claim            claim uri.
     * @param claimValue       claim value.
     * @param profileName      profile name.
     * @param userStoreManager user store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPostGetUserClaimValueWithID(String userID, String claim, List<String> claimValue, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after getting user claim values.
     *
     * @param userID           user ID.
     * @param claims           claim uris.
     * @param profileName      profile name.
     * @param claimMap         claims map.
     * @param userStoreManager user store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPostGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Pre listener for the get user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param returnUsersList  List of users that this listiner will return.
     * @param userStoreManager User store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPreGetUserListWithID(String claimUri, String claimValue, final List<User> returnUsersList,
            UserStoreManager userStoreManager) throws UserStoreException;

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
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPreGetUserListWithID(Condition condition, String domain, String profileName, int limit, int offset,
            String sortBy, String sortOrder, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Pre listener for the get paginated user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param returnUsersList  List of users that this listener will return.
     * @param userStoreManager User store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPreGetUserListWithID(String claimUri, String claimValue, int limit, int offset,
            final List<User> returnUsersList, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Post listener for the get user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param returnValues     Values to be returned.
     * @param userStoreManager User store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPostGetUserListWithID(String claimUri, String claimValue, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Post listener for the get user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param returnValues     Values to be returned.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param userStoreManager User store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPostGetUserListWithID(String claimUri, String claimValue, final List<User> returnValues, int limit,
            int offset, UserStoreManager userStoreManager) throws UserStoreException;

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
    boolean doPostGetUserListWithID(Condition condition, String domain, String profileName, int limit, int offset,
            String sortBy, String sortOrder, List<User> users, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Pre listener for the get user method.
     *
     * @param userID           user ID.
     * @param requestedClaims  Requested claims.
     * @param profileName      Profile name.
     * @param userStoreManager user store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPreGetUserWithID(String userID, String[] requestedClaims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Post listener for the get user method.
     *
     * @param userID           user ID.
     * @param requestedClaims  Requested claims.
     * @param profileName      Profile name.
     * @param user             User.
     * @param userStoreManager user store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPostGetUserWithID(String userID, String[] requestedClaims, String profileName, User user,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Post listener for the get paginated user list method.
     *
     * @param claimUri         Claim URI.
     * @param claimValue       Value of the given claim URI.
     * @param returnValues     Values to be returned.
     * @param userStoreManager User store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPostGetPaginatedUserListWithID(String claimUri, String claimValue, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Post listener for the list paginated users method.
     *
     * @param filter           username filter.
     * @param limit            No of search results.
     * @param offset           start index of the search.
     * @param returnValues     Values to be returned.
     * @param userStoreManager User store manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException User Store Exception.
     */
    boolean doPostListUsersWithID(String filter, int limit, int offset, final List<User> returnValues,
            UserStoreManager userStoreManager) throws UserStoreException;

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
    boolean doPostGetRoleListOfUserWithID(String userName, String filter, String[] roleList,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Any additional tasks that need to be done after getting user list a role.
     *
     * @param roleName         Name of the role.
     * @param userList         List of users.
     * @param userStoreManager User Store Manager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException UserStore Exception.
     */
    boolean doPostGetUserListOfRoleWithID(String roleName, List<User> userList, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Post listener for get claim values of users.
     *
     * @param userIDs                user names.
     * @param claims                 claims.
     * @param profileName            profile name.
     * @param userClaimSearchEntries user claim search entries.
     * @param userStoreManager       The underlying UserStoreManager.
     * @return false in case of error.
     * @throws UserStoreException UserStoreException.
     */
    boolean doPostGetUsersClaimValuesWithID(List<String> userIDs, List<String> claims, String profileName,
            List<UniqueIDUserClaimSearchEntry> userClaimSearchEntries, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before actual authentication is happen.
     *
     * @param preferredUserNameClaim Preferred username claim.
     * @param preferredUserNameValue Preferred username value.
     * @param credential             Credential/password of the user.
     * @param userStoreManager       The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreAuthenticateWithID(String preferredUserNameClaim, String preferredUserNameValue, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after actual authentication is happen.
     *
     * @param preferredUserNameClaim Preferred username claim.
     * @param preferredUserNameValue Preferred username value.
     * @param authenticationResult   Authentication Result.
     * @param userStoreManager       The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostAuthenticateWithID(String preferredUserNameClaim, String preferredUserNameValue,
            AuthenticationResult authenticationResult, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before actual authentication is happen.
     *
     * @param loginIdentifiers Login identifiers.
     * @param credential       Credential/password of the user.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreAuthenticateWithID(List<LoginIdentifier> loginIdentifiers, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after actual authentication is happen.
     *
     * @param loginIdentifiers     Login identifiers.
     * @param authenticationResult Authentication Result.
     * @param userStoreManager     The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostAuthenticateWithID(List<LoginIdentifier> loginIdentifiers, AuthenticationResult authenticationResult,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before actual authentication is happen.
     *
     * @param userID           User ID.
     * @param credential       Credential/password of the user.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreAuthenticateWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after actual authentication is happen.
     *
     * @param userID               User ID.
     * @param authenticationResult Authentication Result.
     * @param userStoreManager     The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostAuthenticateWithID(String userID, AuthenticationResult authenticationResult,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before user is added.
     *
     * @param userID           User ID of User.
     * @param credential       Credential/password of the user.
     * @param roleList         role list of user.
     * @param claims           Properties of the user.
     * @param profile          profile name of user.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreAddUserWithID(String userID, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after user is added.
     *
     * @param user             User.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostAddUserWithID(User user, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before credential is updated by user.
     *
     * @param userID           User ID of User.
     * @param newCredential    new credential/password of the user.
     * @param oldCredential    Old credential/password of the user.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after credential is updated by user.
     *
     * @param userID           User ID of User.
     * @param credential       user credentials.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostUpdateCredentialWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before credential is updated by Admin.
     *
     * @param userID           User ID of User.
     * @param newCredential    new credential/password of the user.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreUpdateCredentialByAdminWithID(String userID, Object newCredential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after credential is updated by Admin.
     *
     * @param userID           User ID of User.
     * @param credential       user credentials.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */

    boolean doPostUpdateCredentialByAdminWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before user is deleted by Admin.
     *
     * @param userID           User ID of User.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreDeleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user is deleted by Admin.
     *
     * @param userID           User ID of User.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostDeleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before user attribute is set by Admin.
     *
     * @param userID           User ID of User.
     * @param claimURI         claim uri.
     * @param claimValue       claim value.
     * @param profileName      user profile name.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreSetUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user attribute is set by Admin.
     *
     * @param userID           User ID of User.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostSetUserClaimValueWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before user attributes are set by Admin.
     *
     * @param userID           User ID of User.
     * @param claims           claim uri and claim value map.
     * @param profileName      user profile name.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user attributes are set by Admin.
     *
     * @param userID           User ID of User.
     * @param claims           user claims.
     * @param profileName      user profile name.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before user attributes are deleted by Admin.
     *
     * @param userID           User ID of User.
     * @param claims           claim uri and claim value map.
     * @param profileName      user profile name.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreDeleteUserClaimValuesWithID(String userID, String[] claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user attributes are deleted by Admin.
     *
     * @param userID           User ID of User.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostDeleteUserClaimValuesWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attribute is deleted by Admin.
     *
     * @param userID           User ID of User.
     * @param claimURI         claim uri.
     * @param profileName      user profile name.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreDeleteUserClaimValueWithID(String userID, String claimURI, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after user attribute is deleted by Admin.
     *
     * @param userID           User ID of User.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostDeleteUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before adding a role.
     *
     * @param roleName         role names.
     * @param userList         user List.
     * @param permissions      permissions.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreAddRoleWithID(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after adding a role.
     *
     * @param roleName         role names.
     * @param userList         user List.
     * @param permissions      permissions.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostAddRoleWithID(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before updating a role.
     *
     * @param roleName     role names.
     * @param deletedUsers deleted user IDs.
     * @param newUsers     new user IDs.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreUpdateUserListOfRoleWithID(String roleName, String deletedUsers[], String[] newUsers,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after updating a role.
     *
     * @param roleName     role names.
     * @param deletedUsers deleted user IDs.
     * @param newUsers     new user IDs.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostUpdateUserListOfRoleWithID(String roleName, String deletedUsers[], String[] newUsers,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before updating role list of user.
     *
     * @param userID           user ID.
     * @param deletedRoles     deleted roles.
     * @param newRoles         new roles.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPreUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after updating role list of user.
     *
     * @param userID           user ID.
     * @param deletedRoles     deleted roles.
     * @param newRoles         new roles.
     * @param userStoreManager The underlying UserStoreManager.
     * @return true if handling succeeds, otherwise false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean doPostUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Post listener for get role list of users.
     *
     * @param userIDs          user IDs.
     * @param rolesOfUsersMap  map of roles against users
     * @param userStoreManager The underlying UserStoreManager.
     * @return false in case of error
     * @throws UserStoreException UserStoreException
     */
    boolean doPostGetRoleListOfUsersWithID(List<String> userIDs, Map<String, List<String>> rolesOfUsersMap,
            UserStoreManager userStoreManager) throws UserStoreException;

}
