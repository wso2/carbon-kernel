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

package org.wso2.carbon.user.core;

import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The interface to read data from a user store.
 * <p>
 * Implement this interface in your UserStoreManager class and add the class to the class path.
 * Provide the class name in the configuration file and the framework will pick the new code that
 * reads user information from the store.
 * </p>
 */
public interface UniqueIDUserStoreManager extends UserStoreManager {

    /**
     * Given the preferred user name and a credential object, the implementation code must
     * validate whether the user is authenticated.
     *
     * @param preferredUserNameClaim The preferred user name property.
     * @param preferredUserNameValue The preferred user name value.
     * @param credential             The credential of a user.
     * @param profileName            profile name.
     * @return authenticated result.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    AuthenticationResult authenticateWithID(String preferredUserNameClaim, String preferredUserNameValue,
            Object credential, String profileName) throws UserStoreException;

    /**
     * Given the login identifiers and a credential object, the implementation code must
     * validate whether the user is authenticated.
     *
     * @param loginIdentifiers The login identifiers list that can be used to identify the user.
     * @param domain           User store domain.
     * @param credential       The credential of a user.
     * @return authenticated result.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    AuthenticationResult authenticateWithID(List<LoginIdentifier> loginIdentifiers, String domain, Object credential)
            throws UserStoreException;

    /**
     * Given the user ID and a credential object, the implementation code must validate whether
     * the user is authenticated.
     *
     * @param userID     The user ID.
     * @param domain     User store domain.
     * @param credential The credential of a user.
     * @return authenticated result.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    AuthenticationResult authenticateWithID(String userID, String domain, Object credential) throws UserStoreException;

    /**
     * Retrieves users upto a maximum limit that matches the user name filter.
     *
     * @param filter       The string to filter out user.
     * @param maxItemLimit The max item limit. If -1 then system maximum limit will be used. If the
     *                     given value is greater than the system configured max limit it will be resetted to
     *                     the system configured max limit.
     * @return An array of users.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    List<User> listUsersWithID(String filter, int maxItemLimit) throws UserStoreException;

    /**
     * Retrieves the user of the given user ID.
     *
     * @param userID          user ID.
     * @param requestedClaims Requested Claims.
     * @param profileName     Profile Name.
     * @return the user.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    User getUserWithID(String userID, String[] requestedClaims, String profileName) throws UserStoreException;

    /**
     * Checks whether the user is in the user store.
     *
     * @param userID The user ID.
     * @return Returns true if user ID is found else returns false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean isExistingUserWithID(String userID) throws UserStoreException;

    /**
     * Get all profile names.
     *
     * @param userID The user ID.
     * @return An array of profile names the user has.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    String[] getProfileNamesWithID(String userID) throws UserStoreException;

    /**
     * Get roles of a user.
     *
     * @param userID The user ID.
     * @return An array of role names that user belongs.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    List<String> getRoleListOfUserWithID(String userID) throws UserStoreException;

    /**
     * Get user list of role.
     *
     * @param roleName role name.
     * @return An array of users that belongs to the given role.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    List<User> getUserListOfRoleWithID(String roleName) throws UserStoreException;

    /**
     * Get user list of role.
     *
     * @param roleName     role name.
     * @param filter       filter.
     * @param maxItemLimit max user count.
     * @return An array of users that belongs to the given role.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    List<User> getUserListOfRoleWithID(String roleName, String filter, int maxItemLimit) throws UserStoreException;

    /**
     * Get user claim value in the profile.
     *
     * @param userID      The user ID.
     * @param claim       The claim URI.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return The claim value.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    String getUserClaimValueWithID(String userID, String claim, String profileName) throws UserStoreException;

    /**
     * Get user claim values in the profile.
     *
     * @param userID      The user ID.
     * @param claims      The claim URI.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return A map containing name value pairs.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    Map<String, String> getUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException;

    /**
     * Get all claim values of the user in the profile.
     *
     * @param userID      The user ID.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return An array of claims.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    List<Claim> getUserClaimValuesWithID(String userID, String profileName) throws UserStoreException;

    /**
     * Add a user to the user store.
     *
     * @param userName    User name of the user.
     * @param credential  The credential/password of the user.
     * @param roleList    The roles that user belongs.
     * @param claims      Properties of the user.
     * @param profileName profile name.
     * @return added user.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    User addUserWithID(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName) throws UserStoreException;

    /**
     * Delete the user with the given user name.
     *
     * @param userID The user ID.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void deleteUserWithID(String userID) throws UserStoreException;

    /**
     * Set a single user claim value.
     *
     * @param userID      The user ID.
     * @param claimURI    The claim URI.
     * @param claimValue  The value.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void setUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName)
            throws UserStoreException;

    /**
     * Set many user claim values.
     *
     * @param userID      The user ID.
     * @param claims      Map of claim URIs against values.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void setUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName)
            throws UserStoreException;

    /**
     * Retrieves a list of users for given user claim value.
     *
     * @param claim       claim uri.
     * @param claimValue  claim value.
     * @param profileName profile name, can be null. If null the default profile is considered.
     * @return An array of users.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    List<User> getUserListWithID(String claim, String claimValue, String profileName) throws UserStoreException;

    /**
     * Update the credential/password of the user.
     *
     * @param userID        The user ID.
     * @param newCredential The new credential/password.
     * @param oldCredential The old credential/password.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void updateCredentialWithID(String userID, Object newCredential, Object oldCredential) throws UserStoreException;

    /**
     * Update credential/password by the admin of another user.
     *
     * @param userID        The user ID.
     * @param newCredential The new credential.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void updateCredentialByAdminWithID(String userID, Object newCredential) throws UserStoreException;

    /**
     * Adds a role to the system.
     *
     * @param roleName     The role name.
     * @param userIDList   the list of the user IDs.
     * @param permissions  The permissions of the role.
     * @param isSharedRole Whether the added role is a shared role or not.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void addRoleWithID(String roleName, String[] userIDList, Permission[] permissions, boolean isSharedRole)
            throws UserStoreException;

    /**
     * Update the user list of a given role.
     *
     * @param roleName       role name.
     * @param deletedUserIDs deleted users.
     * @param newUserIDs     new user IDs.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void updateUserListOfRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs)
            throws UserStoreException;

    /**
     * Update the role list of a given user.
     *
     * @param userID       user ID.
     * @param deletedRoles deleted roles.
     * @param newRoles     new roles.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void updateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles) throws UserStoreException;

    /**
     * Delete a single user claim value.
     *
     * @param userID      The user ID.
     * @param claimURI    Name of the claim.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void deleteUserClaimValueWithID(String userID, String claimURI, String profileName) throws UserStoreException;

    /**
     * Delete many user claim values.
     *
     * @param userID      The user ID.
     * @param claims      URIs of the claims to be deleted.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    void deleteUserClaimValuesWithID(String userID, String[] claims, String profileName) throws UserStoreException;

    /**
     * Method to get the password expiration time.
     *
     * @param userID the user ID.
     * @return the password expiration time.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    Date getPasswordExpirationTimeWithID(String userID) throws UserStoreException;

    /**
     * Checks whether the user is in the given role.
     *
     * @param userID   user ID.
     * @param roleName role name.
     * @return Returns true if user ID is in the role else returns false.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean isUserInRoleWithID(String userID, String roleName) throws UserStoreException;

    /**
     * Retrieves a list of paginated user names.
     *
     * @param filter The string to filter out user.
     * @param limit  No of search results. If the given value is greater than the system configured max limit
     *               it will be reset to the system configured max limit.
     * @param offset Start index of the user search.
     * @return An array of user names.
     * @throws UserStoreException User Store Exception.
     */
    List<User> listUsersWithID(String filter, int limit, int offset) throws UserStoreException;

    /**
     * Retrieves a list of paginated user names from user claims.
     *
     * @param claim       Claim URI. If the claim uri is domain qualified, search the users respective user store. Else
     *                    search recursively.
     * @param claimValue  Claim value.
     * @param profileName User profile name.
     * @param limit       No of search results. If the given value is greater than the system configured max limit
     *                    it will be reset to the system configured max limit.
     * @param offset      Start index of the user search.
     * @return An array of user names.
     * @throws UserStoreException User Store Exception.
     */
    List<User> getUserListWithID(String claim, String claimValue, String profileName, int limit, int offset)
            throws UserStoreException;

    /**
     * Retrieves a list of paginated user names conditionally.
     *
     * @param condition   Conditional filter.
     * @param domain      User Store Domain.
     * @param profileName User profile name.
     * @param limit       No of search results. If the given value is greater than the system configured max limit
     *                    it will be reset to the system configured max limit.
     * @param offset      Start index of the user search.
     * @return An array of user names.
     * @throws UserStoreException User Store Exception.
     */
    List<User> getUserListWithID(Condition condition, String domain, String profileName, int limit, int offset,
            String sortBy, String sortOrder) throws UserStoreException;

    /**
     * Get claim values of users.
     *
     * @param userIDs User IDs.
     * @param claims  Required claims.
     * @return User claim search entry set.
     * @throws UserStoreException User Store Exception.
     */
    List<UniqueIDUserClaimSearchEntry> getUsersClaimValuesWithID(List<String> userIDs, List<String> claims, String profileName)
            throws UserStoreException;

    /**
     * Get roles of a users.
     *
     * @param userIDs user IDs.
     * @return A map contains a list of role names each user belongs.
     * @throws UserStoreException User Store Exception.
     */
    Map<String, List<String>> getRoleListOfUsersWithID(List<String> userIDs) throws UserStoreException;

}
