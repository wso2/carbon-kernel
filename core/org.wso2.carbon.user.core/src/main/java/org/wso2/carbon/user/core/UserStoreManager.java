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
package org.wso2.carbon.user.core;

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.tenant.Tenant;

import java.util.Date;
import java.util.Map;

/**
 * The interface to read data from a user store.
 * <p/>
 * Implement this interface in your UserStoreManager class and add the class to the class path.
 * Provide the class name in the configuration file and the framework will pick the new code that
 * reads user information from the store.
 */
public interface UserStoreManager extends org.wso2.carbon.user.api.UserStoreManager {

    /**
     * Given the user name and a credential object, the implementation code must validate whether
     * the user is authenticated.
     *
     * @param userName   The user name
     * @param credential The credential of a user
     * @return If the value is true the provided credential match with the user name. False is
     * returned for invalid credential, invalid user name and mismatching credential with
     * user name.
     * @throws UserStoreException An unexpected exception has occured
     */
    boolean authenticate(String userName, Object credential) throws UserStoreException;

    /**
     * Retrieves a list of user names upto a maximum limit
     *
     * @param filter       The string to filter out user
     * @param maxItemLimit The max item limit. If -1 then system maximum limit will be used. If the
     *                     given value is greater than the system configured max limit it will be resetted to
     *                     the system configured max limit.
     * @return An arry of user names
     * @throws UserStoreException
     */
    String[] listUsers(String filter, int maxItemLimit) throws UserStoreException;

    /**
     * Checks whether the user is in the user store
     *
     * @param userName The user name
     * @return Returns true if user name is found else returns false.
     * @throws UserStoreException
     */
    boolean isExistingUser(String userName) throws UserStoreException;

    /**
     * Checks whether the role name is in the user store
     *
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    boolean isExistingRole(String roleName) throws UserStoreException;


    /**
     * Get all role names
     *
     * @return An array of all role names
     * @throws UserStoreException
     */
    String[] getRoleNames() throws UserStoreException;

    /**
     * Get all role names
     *
     * @return An array of all role names
     * @throws UserStoreException
     */
    String[] getRoleNames(boolean noHybridRoles) throws UserStoreException;

    /**
     * Get all profile names
     *
     * @param userName The user name
     * @return An array of profile names the user has.
     * @throws UserStoreException
     */
    String[] getProfileNames(String userName) throws UserStoreException;

    /**
     * Get roles of a user.
     *
     * @param userName The user name
     * @return An array of role names that user belongs.
     * @throws UserStoreException
     */
    String[] getRoleListOfUser(String userName) throws UserStoreException;

    /**
     * Get Users for the role.
     * @param roleName Name of the role.
     * @return Array of usernames.
     * @throws UserStoreException
     */
    String[] getUserListOfRole(String roleName) throws UserStoreException;

    /**
     * Get user claim value in the profile.
     *
     * @param userName    The user name
     * @param claim       The claim URI
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return The value
     * @throws UserStoreException
     */
    String getUserClaimValue(String userName, String claim, String profileName)
            throws UserStoreException;

    /**
     * Get user claim values in the profile.
     *
     * @param userName    The user name
     * @param claims      The claim URI
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return A map containing name value pairs
     * @throws UserStoreException
     */
    Map<String, String> getUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException;

    /**
     * Get all claim values of the user in the profile.
     *
     * @param userName    The user name
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return An array of claims
     * @throws UserStoreException
     */
    Claim[] getUserClaimValues(String userName, String profileName) throws UserStoreException;

    /**
     * Get all the profile names in the system
     *
     * @return An array of all profile names
     * @throws UserStoreException
     */
    String[] getAllProfileNames() throws UserStoreException;

    /**
     * @return
     * @throws UserStoreException
     */
    boolean isReadOnly() throws UserStoreException;

    /**
     * Add a user to the user store.
     *
     * @param userName    User name of the user
     * @param credential  The credential/password of the user
     * @param roleList    The roles that user belongs
     * @param claims      Properties of the user
     * @param profileName TODO
     * @throws UserStoreException
     */
    void addUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                 String profileName) throws UserStoreException;

    /**
     * Add a user to the user store.
     * @param userName              User name of the user
     * @param credential            The credential/password of the user
     * @param roleList              The roles that user belongs
     * @param claims                Properties of the user
     * @param profileName           The name of the profile where claims should be added
     * @param requirePasswordChange Require the password change within next 24 hours
     * @throws UserStoreException
     */
    void addUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                 String profileName, boolean requirePasswordChange) throws UserStoreException;

    /**
     * Update the credential/password of the user
     *
     * @param userName      The user name
     * @param newCredential The new credential/password
     * @param oldCredential The old credential/password
     * @throws UserStoreException
     */
    void updateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException;

    /**
     * Update credential/password by the admin of another user
     *
     * @param userName      The user name
     * @param newCredential The new credential
     * @throws UserStoreException
     */
    void updateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException;

    /**
     * Delete the user with the given user name
     *
     * @param userName The user name
     * @throws UserStoreException
     */
    void deleteUser(String userName) throws UserStoreException;

    /**
     * Delete the role with the given role name
     *
     * @param roleName The role name
     * @throws UserStoreException
     */
    void deleteRole(String roleName) throws UserStoreException;

    /**
     * Update user list of the role.
     * @param roleName     The role name to be updated
     * @param deletedUsers The array of user names to be deleted
     * @param newUsers     The array of of user names to be added
     * @throws UserStoreException
     */
    void updateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException;

    /**
     * Update role list of the user.
     * @param userName     The user name of the user where role list is updated
     * @param deletedRoles The array of role names to be added
     * @param newRoles     The array of role names to be added
     * @throws UserStoreException
     */
    void updateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException;

    /**
     * Set a single user claim value
     *
     * @param userName    The user name
     * @param claimURI    The claim URI
     * @param claimValue  The value
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException
     */
    void setUserClaimValue(String userName, String claimURI, String claimValue, String profileName)
            throws UserStoreException;

    /**
     * Set many user claim values
     *
     * @param userName    The user name
     * @param claims      Map of claim URIs against values
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException
     */
    void setUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException;

    /**
     * Delete a single user claim value
     *
     * @param userName    The user name
     * @param claimURI    Name of the claim
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException
     */
    void deleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException;

    /**
     * Delete many user claim values.
     *
     * @param userName    The user name
     * @param claims      URIs of the claims to be deleted.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException
     */
    void deleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException;

    /**
     * Get all hybrid roles.
     * @return Array of hybrid roles.
     * @throws UserStoreException
     */
    String[] getHybridRoles() throws UserStoreException;

    /**
     * Get all roles in the secondary user store.
     * @return Array of roles.
     * @throws UserStoreException
     */
    String[] getAllSecondaryRoles() throws UserStoreException;

    /**
     * Get password expiration time.
     * @param username The user name
     * @return Expiration date.
     * @throws UserStoreException
     */
    Date getPasswordExpirationTime(String username) throws UserStoreException;

    /**
     * Get user id from the username.
     * @param username The user name
     * @return Integer user id.
     * @throws UserStoreException
     */
    int getUserId(String username) throws UserStoreException;

    /**
     * This method works only if the tenant is super tenant. If the realm is not super tenant's this
     * method should throw exception
     *
     * @param username Username of the user.
     * @return Tenant id as integer.
     * @throws UserStoreException
     */
    int getTenantId(String username) throws UserStoreException;

    /**
     * this will get the tenant id associated with the user store manager
     *
     * @return the tenant id of the authorization manager
     * @throws UserStoreException if the operation failed
     */
    int getTenantId() throws UserStoreException;

    /**
     * Get user store properties.
     * @param tenant @see Tenant object.
     * @return Hash map of user store properties.
     * @throws UserStoreException
     */
    Map<String, String> getProperties(Tenant tenant) throws UserStoreException;

    /**
     * Update the role name of given role
     *
     * @param roleName Name of the existing role.
     * @param newRoleName New name that the role should be changed.
     * @throws UserStoreException
     */
    void updateRoleName(String roleName, String newRoleName) throws UserStoreException;

    /**
     * Specified whether current user store supports bulk import.
     *
     * @return <code>true</code> if bulk import supported, else <code>false<code>.
     */
    boolean isBulkImportSupported() throws UserStoreException;

    /**
     * Retrieves a list of user names for given user claim value
     *
     * @param claim       claim uri
     * @param claimValue  claim value
     * @param profileName profile name, can be null. If null the default profile is considered.
     * @return An array of user names
     * @throws UserStoreException if the operation failed
     */
    String[] getUserList(String claim, String claimValue, String profileName)
            throws UserStoreException;

    /**
     * Get secondary user store manager associated with this user store manager. This should return the immediate
     * secondary user store manger that is in the linked list.
     * @return @see UserStoreManager.
     */
    UserStoreManager getSecondaryUserStoreManager();

    /**
     * Set secondary user store manager associated with this user store manager. This should be the immediate
     * secondary user store manger that is in the linked list.
     * @param userStoreManager @see UserStoreManager
     */
    void setSecondaryUserStoreManager(UserStoreManager userStoreManager);

    /**
     * Get the secondary user store manger associated for the given domain from the given name.
     * @param userDomain Name of the user store domain.
     * @return @see UserStoreManager
     */
    UserStoreManager getSecondaryUserStoreManager(String userDomain);

    /**
     * Add a secondary user store manger under the given domain name.
     * @param userDomain Domain name.
     * @param userStoreManager Parent user store manger object.
     */
    void addSecondaryUserStoreManager(String userDomain, UserStoreManager userStoreManager);

    /**
     * Default implementation for get users count with claims.
     *
     * @param claimUri claim uri
     * @param filterValueWithDomain filter value with domain
     * @return count users
     * @throws UserStoreException UserStoreException
     */
    default long getUserCountWithClaims(String claimUri, String filterValueWithDomain) throws UserStoreException {

        throw new UserStoreException("Operation is not supported");
    }

    /**
     * Default implementation for count roles in user stores.
     *
     * @param filter The string to filter out roles
     * @return count roles
     * @throws UserStoreException UserStoreException
     */
    default long countRoles(String filter) throws UserStoreException {

        throw new UserStoreException("Operation is not supported");
    }

    /**
     * Get the RealmConfiguration belonging to this user store
     *
     * @return RealmConfiguration
     */
    RealmConfiguration getRealmConfiguration();
}
