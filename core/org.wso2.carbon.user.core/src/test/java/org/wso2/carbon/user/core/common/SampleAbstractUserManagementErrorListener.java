/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.Map;

/**
 * This is a dummy implementation of class {@link AbstractUserManagementErrorListener} for testing purposes.
 */
public class SampleAbstractUserManagementErrorListener extends AbstractUserManagementErrorListener {
    private int authenticationFailureCount = 0;
    private int addUserFailureCount = 0;
    private int updateCredentialFailureCount = 0;
    private int updateCredentialByAdminFailureCount = 0;
    private int deleteUserFailureCount = 0;
    private int setUserClaimValueFailureCount = 0;
    private int setUserClaimValuesFailureCount = 0;
    private int deleteUserClaimValuesFailureCount = 0;
    private int deleteUserClaimValueFailureCount = 0;
    private int addRoleFailureCount = 0;
    private int deleteRoleFailureCount = 0;
    private int updateRoleNameFailureCount = 0;
    private int updateUserRoleListFailureCount = 0;
    private int updateRoleListOfUserFailureCount = 0;
    private int getUserClaimValueFailureCount = 0;
    private int getUserClaimValuesFailureCount = 0;
    private int getUserListFailureCount = 0;

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public boolean onAuthenticateFailure(String errorCode, String errorMessage, String userName, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException {

        authenticationFailureCount++;
        super.onAuthenticateFailure(errorCode, errorMessage, userName, credential, userStoreManager);
        return true;
    }

    @Override
    public boolean onAddUserFailure(String errorCode, String errorMessage, String userName, Object credential,
            String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager) throws
            UserStoreException {

        addUserFailureCount++;
        super.onAddUserFailure(errorCode, errorMessage, userName, credential, roleList, claims, profile,
                userStoreManager);
        return true;
    }

    @Override
    public boolean onUpdateCredentialFailure(String errorCode, String errorMessage, String userName,
            Object newCredential, Object oldCredential, UserStoreManager userStoreManager) throws UserStoreException {

        updateCredentialFailureCount++;
        super.onUpdateCredentialFailure(errorCode, errorMessage, userName, newCredential, oldCredential,
                userStoreManager);
        return true;
    }

    @Override
    public boolean onUpdateCredentialByAdminFailure(String errorCode, String errorMessage, String userName,
            Object newCredential, UserStoreManager userStoreManager) throws UserStoreException {

        updateCredentialByAdminFailureCount++;
        super.onUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential, userStoreManager);
        return true;
    }

    @Override
    public boolean onDeleteUserFailure(String errorCode, String errorMessage, String userName,
            UserStoreManager userStoreManager) throws UserStoreException {

        deleteUserFailureCount++;
        super.onDeleteUserFailure(errorCode, errorMessage, userName, userStoreManager);
        return true;
    }

    @Override
    public boolean onSetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claimURI,
            String claimValue, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        setUserClaimValueFailureCount++;
        super.onSetUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, claimValue, profileName,
                userStoreManager);
        return true;
    }

    @Override
    public boolean onSetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        setUserClaimValuesFailureCount++;
        super.onSetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, userStoreManager);
        return true;
    }

    @Override
    public boolean onDeleteUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            String[] claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        deleteUserClaimValuesFailureCount++;
        super.onDeleteUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, userStoreManager);
        return true;
    }

    @Override
    public boolean onDeleteUserClaimValueFailure(String errorCode, String errorMessage, String userName,
            String claimURI, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        deleteUserClaimValueFailureCount++;
        super.onDeleteUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, profileName, userStoreManager);
        return true;
    }

    @Override
    public boolean onAddRoleFailure(String errorCode, String errorMessage, String roleName, String[] userList,
            Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {

        addRoleFailureCount++;
        super.onAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions, userStoreManager);
        return true;
    }

    @Override
    public boolean onDeleteRoleFailure(String errorCode, String errorMessage, String roleName,
            UserStoreManager userStoreManager) throws UserStoreException {

        deleteRoleFailureCount++;
        super.onDeleteRoleFailure(errorCode, errorMessage, roleName, userStoreManager);
        return true;
    }

    @Override
    public boolean onUpdateRoleNameFailure(String errorCode, String errorMessage, String roleName, String
            newRoleName, UserStoreManager userStoreManager) throws UserStoreException {

        updateRoleNameFailureCount++;
        super.onUpdateRoleNameFailure(errorCode, errorMessage, roleName, newRoleName, userStoreManager);
        return true;
    }

    @Override
    public boolean onUpdateUserListOfRoleFailure(String errorCode, String errorMessage, String roleName,
            String[] deletedUsers, String[] newUsers, UserStoreManager userStoreManager) throws UserStoreException {

        updateUserRoleListFailureCount++;
        super.onUpdateUserListOfRoleFailure(errorCode, errorMessage, roleName, deletedUsers, newUsers,
                userStoreManager);
        return true;
    }

    @Override
    public boolean onUpdateRoleListOfUserFailure(String errorCode, String errorMessage, String userName,
            String[] deletedRoles, String[] newRoles, UserStoreManager userStoreManager) throws UserStoreException {

        updateRoleListOfUserFailureCount++;
        super.onUpdateRoleListOfUserFailure(errorCode, errorMessage, userName, deletedRoles, newRoles,
                userStoreManager);
        return true;
    }

    @Override
    public boolean onGetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claim,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        getUserClaimValueFailureCount++;
        super.onGetUserClaimValueFailure(errorCode, errorMessage, userName, claim, profileName, userStoreManager);
        return true;
    }

    @Override
    public boolean onGetUserClaimValuesFailure(String errorCode, String errorMessage, String userName, String[] claims,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        getUserClaimValuesFailureCount++;
        super.onGetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, userStoreManager);
        return true;
    }

    @Override
    public boolean onGetUserListFailure(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        getUserListFailureCount++;
        super.onGetUserListFailure(errorCode, errorMessage, claim, claimValue, profileName, userStoreManager);
        return true;
    }

    /**
     * To get authentication failure count.
     *
     * @return authentication failure count.
     */
    public int getAuthenticationFailureCount() {
        return authenticationFailureCount;
    }

    /**
     * To get update credential failure count.
     *
     * @return UpdateCredential failure count.
     */
    public int getUpdateCredentialFailureCount() {
        return updateCredentialFailureCount;
    }

    /**
     * To get add user failure count.
     *
     * @return Add User failure count.
     */
    public int getAddUserFailureCount() {
        return addUserFailureCount;
    }

    /**
     * To get update credential by admin failure count.
     *
     * @return Update Credential by admin failure count.
     */
    public int getUpdateCredentialByAdminFailureCount() {
        return updateCredentialByAdminFailureCount;
    }

    /**
     * To get delete user failure count.
     *
     * @return Delete User Failure Count.
     */
    public int getDeleteUserFailureCount() {
        return deleteUserFailureCount;
    }

    /**
     * To get failure count while setting the user claim value.
     *
     * @return Set User Claim Failure Count.
     */
    public int getSetUserClaimValueFailureCount() {
        return setUserClaimValueFailureCount;
    }

    /**
     * To get failure count while setting user claim values.
     *
     * @return Failure count while setting user claim values.
     */
    public int getSetUserClaimValuesFailureCount() {
        return setUserClaimValuesFailureCount;
    }

    /**
     * To get failure count while deleting user claim values.
     *
     * @return Failure count while deleting user claim values.
     */
    public int getDeleteUserClaimValuesFailureCount() {
        return deleteUserClaimValuesFailureCount;
    }

    /**
     * To get failure count while delete user claim value.
     *
     * @return Failure count while deleting user claim value.
     */
    public int getDeleteUserClaimValueFailureCount() {
        return deleteUserClaimValueFailureCount;
    }

    /**
     * To get failure count while adding role.
     *
     * @return Failure count while adding role.
     */
    public int getAddRoleFailureCount() {
        return addRoleFailureCount;
    }

    /**
     * To get failure count while deleting role .
     *
     * @return Failure count while deleting role.
     */
    public int getDeleteRoleFailureCount() {
        return deleteRoleFailureCount;
    }

    /**
     * To get failure count while updating role name.
     *
     * @return Failure count while updating role name.
     */
    public int getUpdateRoleNameFailureCount() {
        return updateRoleNameFailureCount;
    }

    /**
     * To get failure count while updating user role list.
     *
     * @return Failure count while updating user role list.
     */
    public int getUpdateUserRoleListFailureCount() {
        return updateUserRoleListFailureCount;
    }

    /**
     * To get failure count while updating role list of user.
     *
     * @return Failure count while updating role list of user.
     */
    public int getUpdateRoleListOfUserFailureCount() {
        return updateRoleListOfUserFailureCount;
    }

    /**
     * To get failure count while getting user claim value.
     *
     * @return Failure count while getting user claim value.
     */
    public int getGetUserClaimValueFailureCount() {
        return getUserClaimValueFailureCount;
    }

    /**
     * To get failure count while getting user claim values.
     *
     * @return Failure count while getting user claim values.
     */
    public int getGetUserClaimValuesFailureCount() {
        return getUserClaimValuesFailureCount;
    }

    /**
     * To get failure count while getting user list.
     *
     * @return Failure count while getting user list.
     */
    public int getGetUserListFailureCount() {
        return getUserListFailureCount;
    }
}
