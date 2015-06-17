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

package org.wso2.carbon.registry.core.jdbc.realm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.tenant.Tenant;

import java.util.Date;
import java.util.Map;

/**
 * Registry wrapper of the user store admin. This will provide registry level validation for some of
 * the functions in user store management.
 */
@Deprecated
@SuppressWarnings("unused")
public class RegistryUserStoreManager implements UserStoreManager {

    private UserRealm coreRealm;
    private static final Log log = LogFactory.getLog(RegistryUserStoreManager.class);

    /**
     * Constructs the Registry User Store Admin
     *
     * @param coreRealm the realm which got wrapped by the registry realm.
     */
    public RegistryUserStoreManager(UserRealm coreRealm) {
        this.coreRealm = coreRealm;
    }

    /**
     * Authenticate the user
     *
     * @param userName   the user name.
     * @param credential the credential to authenticate.
     *
     * @return true if the authenticate successful, false otherwise.
     * @throws UserStoreException throws if the operation failed.
     */
    public boolean authenticate(String userName, Object credential) throws UserStoreException {
        return getUserStoreManager().authenticate(userName, credential);
    }

//    /**
//     * Method to add role.
//     *
//     * @param roleName    the role name.
//     * @param userList    the users list to add to the role.
//     * @param permissions the permission list corresponding to the users list
//     *
//     * @throws UserStoreException throws if the operation failed.
//     */
//    public void addRole(String roleName, String[] userList, Permission[] permissions, boolean isSharedRole)
//            throws org.wso2.carbon.user.api.UserStoreException {
//        getUserStoreManager().addRole(roleName, userList, permissions, isSharedRole);
//    }

    /**
     * Method to add user.
     *
     * @param userName    the user name.
     * @param credential  the credential to authenticate.
     * @param roleList    the role list.
     * @param claims      the claims list corresponding to the roles list.
     * @param profileName the profile name.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void addUser(String userName, Object credential, String[] roleList,
                        Map<String, String> claims, String profileName) throws UserStoreException {
        this.addUser(userName, credential, roleList, claims, profileName, false);
    }

    /**
     * Method to add user.
     *
     * @param userName              the user name.
     * @param credential            the credential of the user.
     * @param roleList              the roles list the users belong to
     * @param claims                associated with of the user
     * @param profileName           the name of the profile
     * @param requirePasswordChange whether the password change required.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void addUser(String userName, Object credential, String[] roleList,
                        Map<String, String> claims, String profileName,
                        boolean requirePasswordChange) throws UserStoreException {

        getUserStoreManager().addUser(userName, credential, roleList, claims, profileName,
                requirePasswordChange);
    }

    /**
     * Delete role.
     *
     * @param roleName the role name to deleted.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void deleteRole(String roleName) throws UserStoreException {
        getUserStoreManager().deleteRole(roleName);
    }

   /**
     * Update Role Name.
     *
     * @param roleName the role name to update
     * @param newRoleName the new role name
     * @throws UserStoreException throws if the operation failed.
     */
    public void updateRoleName(String roleName, String newRoleName) throws UserStoreException {
        getUserStoreManager().updateRoleName(roleName, newRoleName);
    }

    /**
     * This method is to check whether multiple profiles are allowed with a particular user-store.
     * For an example, currently, JDBC user store supports multiple profiles and where as ApacheDS
     * does not allow.
     *
     * @return boolean
     */
    public boolean isMultipleProfilesAllowed() {
        return true;
    }

    /**
     * Delete user.
     *
     * @param userName the user name
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void deleteUser(String userName) throws UserStoreException {
        getUserStoreManager().deleteUser(userName);

    }

    /**
     * Delete all the user management related data of the tenant
     *
     * @throws UserStoreException
     */
    public void deleteUMTenantData(int tenantId) throws UserStoreException {

    }

    public void addRole(String roleName, String[] userList,
                        org.wso2.carbon.user.api.Permission[] permissions)
            throws org.wso2.carbon.user.api.UserStoreException {
        addRole(roleName, userList, permissions, false);

    }

    /**
     * Delete the user claim value.
     *
     * @param userName    the user name.
     * @param claimURI    the claim uri string.
     * @param profileName the profile name.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void deleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {
        getUserStoreManager().deleteUserClaimValue(userName, claimURI, profileName);
    }

    /**
     * Delete the user claim values
     *
     * @param userName    the user name
     * @param claims      the claims to delete.
     * @param profileName the profile name.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void deleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {
        getUserStoreManager().deleteUserClaimValues(userName, claims, profileName);
    }

    /**
     * Set a claim value
     *
     * @param userName    the user name.
     * @param claimURI    the claim uri.
     * @param claimValue  the claim value.
     * @param profileName the profile name.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void setUserClaimValue(String userName, String claimURI, String claimValue,
                                  String profileName) throws UserStoreException {
        getUserStoreManager().deleteUserClaimValue(userName, claimURI, profileName);
    }

    /**
     * Set the user claim values.
     *
     * @param userName    the user name.
     * @param claims      the claims
     * @param profileName the profile name.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void setUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {
        getUserStoreManager().setUserClaimValues(userName, claims, profileName);

    }

    /**
     * Update the credential.
     *
     * @param userName      the user name.
     * @param newCredential the new credential.
     * @param oldCredential the old credential.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void updateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {
        getUserStoreManager().updateCredential(userName, newCredential, oldCredential);
    }

    /**
     * Update the credential by an admin
     *
     * @param userName      the user name
     * @param newCredential the credential
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void updateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {
        getUserStoreManager().updateCredentialByAdmin(userName, newCredential);

    }

    /**
     * Update the role list of a user.
     *
     * @param userName     the user name.
     * @param deletedRoles the deleted roles.
     * @param newRoles     the new role list
     *
     * @throws UserStoreException if the operation failed.
     */
    public void updateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        getUserStoreManager().updateRoleListOfUser(userName, deletedRoles, newRoles);

    }

    /**
     * Update the user list of a role
     *
     * @param roleName     the role name.
     * @param deletedUsers the deleted users.
     * @param newUsers     the new users.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void updateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {
        getUserStoreManager().updateUserListOfRole(roleName, deletedUsers, newUsers);
    }

    /**
     * Method to return the the hybrid roles.
     *
     * @return array of hybrid roles.
     * @throws UserStoreException throws if the operation failed.
     */
    public String[] getHybridRoles() throws UserStoreException {
        return getUserStoreManager().getHybridRoles();
    }

    @Override
    public String[] getAllSecondaryRoles() throws UserStoreException {
        return getUserStoreManager().getAllSecondaryRoles();
    }

    /**
     * Method to return the user list of a role.
     *
     * @param roleName the role name.
     *
     * @return array of users
     * @throws UserStoreException throws if the operation failed.
     */
    public String[] getUserListOfRole(String roleName) throws UserStoreException {
        return getUserStoreManager().getUserListOfRole(roleName);
    }

    /**
     * Get all profile names.
     *
     * @return array of profile names.
     * @throws UserStoreException throws if the operation failed.
     */
    public String[] getAllProfileNames() throws UserStoreException {
        return getUserStoreManager().getAllProfileNames();

    }

    /**
     * Get the role names.
     *
     * @return the array of roles.
     * @throws UserStoreException throws if the operation failed.
     */
    public String[] getRoleNames() throws UserStoreException {
        return getUserStoreManager().getRoleNames();

    }

    @Override
    public String[] getRoleNames(boolean b) throws UserStoreException {
       return getUserStoreManager().getRoleNames(b);
    }

    /**
     * Get profile names.
     *
     * @param userName the user name.
     *
     * @return the array of profile names
     * @throws UserStoreException throws if the operation failed.
     */
    public String[] getProfileNames(String userName) throws UserStoreException {
        return getUserStoreManager().getProfileNames(userName);

    }

    /**
     * Get the claim value of a user.
     *
     * @param userName    the user name.
     * @param claim       the claim
     * @param profileName the profile name
     *
     * @return the claim value of the user.
     * @throws UserStoreException throws if the operation failed.
     */
    public String getUserClaimValue(String userName, String claim, String profileName)
            throws UserStoreException {
        return getUserStoreManager().getUserClaimValue(userName, claim, profileName);

    }

    /**
     * Method to get the user claim values.
     *
     * @param userName    the user name.
     * @param claims      the claims
     * @param profileName the profile name.
     *
     * @return the user claim values.
     * @throws UserStoreException throws if the operation failed.
     */
    public Map<String, String> getUserClaimValues(String userName, String[] claims,
                                                  String profileName) throws UserStoreException {
        return getUserStoreManager().getUserClaimValues(userName, claims, profileName);

    }

    /**
     * Method to get the role list of user.
     *
     * @param userName user name.
     *
     * @return the role list of a user.
     * @throws UserStoreException throws if the operation failed.
     */
    public String[] getRoleListOfUser(String userName) throws UserStoreException {
        return getUserStoreManager().getRoleListOfUser(userName);

    }

    /**
     * Method to check whether the role exist or not.
     *
     * @param roleName the role name.
     *
     * @return true, if the role exist. false, otherwise.
     * @throws UserStoreException throws if the operation failed.
     */
    @Override
    public boolean isExistingRole(String roleName) throws UserStoreException {
        return getUserStoreManager().isExistingRole(roleName);
    }

    /**
     * Method to check whether the user exist or not.
     *
     * @param userName the user name.
     *
     * @return true, if the user exist, false otherwise
     * @throws UserStoreException throws if the operation failed.
     */
    public boolean isExistingUser(String userName) throws UserStoreException {
        return getUserStoreManager().isExistingUser(userName);
    }

    /**
     * Method to list the
     *
     * @param filter       the filter to list the users.
     * @param maxItemLimit maximum item limit
     *
     * @return the filtered users list
     * @throws UserStoreException throws if the operation failed.
     */
    public String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {
        return getUserStoreManager().listUsers(filter, maxItemLimit);
    }

    /**
     * Method to get the user id.
     *
     * @param userName the user name.
     *
     * @return the user id
     * @throws UserStoreException throws if the operation failed.
     */
    public int getUserId(String userName) throws UserStoreException {
        return getUserStoreManager().getUserId(userName);
    }

    /**
     * Method to get the tenant id of the current user store manager instance.
     *
     * @return the tenant id
     * @throws UserStoreException throws if the operation failed.
     */
    public int getTenantId() throws UserStoreException {
        return getUserStoreManager().getTenantId();
    }

    public Map<String, String> getProperties(org.wso2.carbon.user.api.Tenant tenant)
            throws org.wso2.carbon.user.api.UserStoreException {
        //TODO: Method implementation
        return null;
    }

    /**
     * Method to get the tenant id from a given user name.
     *
     * @param userName the user name.
     *
     * @return the tenant id.
     * @throws UserStoreException throws if the operation failed.
     */
    public int getTenantId(String userName) throws UserStoreException {
        return getUserStoreManager().getTenantId(userName);
    }

    /**
     * Method to check whether the registry is readonly or not.
     *
     * @return true, if the user store manager is read only, false otherwise.
     * @throws UserStoreException throws if the operation failed.
     */
    public boolean isReadOnly() throws UserStoreException {
        return coreRealm.getUserStoreManager().isReadOnly();
    }

    /**
     * Method to get the user store manager.
     *
     * @return the user store manager.
     * @throws UserStoreException throws if the operation failed.
     */
    private UserStoreManager getUserStoreManager()
            throws org.wso2.carbon.user.core.UserStoreException {
        return coreRealm.getUserStoreManager();
    }

    /**
     * Method to get the user claim values.
     *
     * @param userName    the user name.
     * @param profileName the profile name.
     *
     * @return the claim values of the user.
     * @throws UserStoreException throw if the operation failed.
     */
    public Claim[] getUserClaimValues(String userName, String profileName)
            throws UserStoreException {
        return coreRealm.getUserStoreManager().getUserClaimValues(userName, profileName);
    }

    /**
     * Method to get the password expiration time.
     *
     * @param userName the user name.
     *
     * @return the password expiration time.
     * @throws UserStoreException throw if the operation failed.
     */
    public Date getPasswordExpirationTime(String userName) throws UserStoreException {
        return coreRealm.getUserStoreManager().getPasswordExpirationTime(userName);
    }

    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
        return this.coreRealm.getRealmConfiguration().getUserStoreProperties();
    }

    public void addRememberMe(String userName, String token)
            throws org.wso2.carbon.user.api.UserStoreException {
        coreRealm.getUserStoreManager().addRememberMe(userName, token);
    }

    public boolean isValidRememberMeToken(String userName, String token)
            throws org.wso2.carbon.user.api.UserStoreException {
       return coreRealm.getUserStoreManager().isValidRememberMeToken(userName, null);
    }

    @Override
    public ClaimManager getClaimManager() throws org.wso2.carbon.user.api.UserStoreException {
          return    coreRealm.getUserStoreManager().getClaimManager();
    }

    @Override
    public boolean isSCIMEnabled() throws org.wso2.carbon.user.api.UserStoreException {
        return coreRealm.getUserStoreManager().isSCIMEnabled();
    }

    public boolean isBulkImportSupported() throws UserStoreException {
        return coreRealm.getUserStoreManager().isBulkImportSupported();
    }

    @Override
    public String[] getUserList(String claim, String claimValue, String profileName)
                                                throws UserStoreException {
        return coreRealm.getUserStoreManager().getUserList(claim, claimValue, profileName);
    }

    @Override
    public UserStoreManager getSecondaryUserStoreManager() {
        try {
            return getUserStoreManager().getSecondaryUserStoreManager();
        } catch (UserStoreException e) {
           log.error("Error occurred while obtaining SecondaryUserStoreManager .!" + e.getMessage());
           return null;
        }
    }

    @Override
    public UserStoreManager getSecondaryUserStoreManager(String s) {
        try {
            return getUserStoreManager().getSecondaryUserStoreManager(s);
        } catch (UserStoreException e) {
            log.error("Error occurred while obtaining SecondaryUserStoreManager for " + s + ". " + e.getMessage());
            return null;
        }

    }

    @Override
    public void addSecondaryUserStoreManager(String s, UserStoreManager userStoreManager) {
        try {
            getUserStoreManager().addSecondaryUserStoreManager(s,userStoreManager);
        } catch (UserStoreException e) {
            log.error("Error occurred while adding a SecondaryUserStoreManager of " + s + ". " + e.getMessage());
        }
    }

    @Override
    public void setSecondaryUserStoreManager(UserStoreManager userStoreManager) {
        try {
            getUserStoreManager().setSecondaryUserStoreManager(userStoreManager);
        } catch (UserStoreException e) {
            log.error("Error occurred while setting a SecondaryUserStoreManager " + e.getMessage());
        }
    }
/*
    public RoleDTO[] getRoleNamesWithDomain() throws UserStoreException {
        return getUserStoreManager().getRoleNamesWithDomain();
    }
*/
    public RealmConfiguration getRealmConfiguration() {
        try {
            return getUserStoreManager().getRealmConfiguration();
        } catch (UserStoreException e) {
            log.error("Error occurred while getting Realm Configuration " + e.getMessage());
        }
        return null;
    }


	public Properties getDefaultUserStoreProperties() {
		return null;
	}

	@Override
	public void addRole(String roleName, String[] userList,
	                    org.wso2.carbon.user.api.Permission[] permissions, boolean isSharedRole)
	                                                                                            throws org.wso2.carbon.user.api.UserStoreException {
		getUserStoreManager().addRole(roleName, userList, permissions, isSharedRole);
	}

	@Override
    public boolean isExistingRole(String roleName, boolean isShared)
                                                                    throws org.wso2.carbon.user.api.UserStoreException {
	    return false;
    }

}
