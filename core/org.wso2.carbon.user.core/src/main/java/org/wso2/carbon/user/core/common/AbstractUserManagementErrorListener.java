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
import org.wso2.carbon.user.core.listener.UniqueIDUserManagementErrorEventListener;
import org.wso2.carbon.user.core.listener.UserManagementErrorEventListener;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Map;

/**
 * Abstract implementation of UserManagementErrorEventListener.
 */
public class AbstractUserManagementErrorListener implements UniqueIDUserManagementErrorEventListener {

    @Override
    public int getExecutionOrderId() {
        return 0;
    }

    @Override
    public boolean isEnable() {
        return false;
    }

    @Override
    public boolean onAuthenticateFailure(String errorCode, String errorMessage, String userName, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onAddUserFailure(String errorCode, String errorMessage, String userName, Object credential,
            String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    @Override
    public boolean onUpdateCredentialFailure(String errorCode, String errorMessage, String userName,
            Object newCredential, Object oldCredential, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onUpdateCredentialByAdminFailure(String errorCode, String errorMessage, String userName,
            Object newCredential, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onDeleteUserFailure(String errorCode, String errorMessage, String userName,
            UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onSetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claimURI,
            String claimValue, String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onSetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    @Override
    public boolean onDeleteUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            String[] claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onDeleteUserClaimValueFailure(String errorCode, String errorMessage, String userName,
            String claimURI, String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onAddRoleFailure(String errorCode, String errorMessage, String roleName, String[] userList,
            Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onDeleteRoleFailure(String errorCode, String errorMessage, String roleName,
            UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onUpdateRoleNameFailure(String errorCode, String errorMessage, String roleName, String newRoleName,
            UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onUpdateUserListOfRoleFailure(String errorCode, String errorMessage, String roleName,
            String[] deletedUsers, String[] newUsers, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onUpdateRoleListOfUserFailure(String errorCode, String errorMessage, String userName,
            String[] deletedRoles, String[] newRoles, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onGetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claim,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onGetUserClaimValuesFailure(String errorCode, String errorMessage, String userName, String[] claims,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean onGetUserListFailure(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions that need to be done if there is a failure retrieving user list.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param claim            Claim URI
     * @param claimValue       Claim Value
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    public boolean onGetUserListFailure(String errorCode, String errorMessage, String claim, String claimValue, int
            limit, int offset, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions that need to be done if there is a failure on retrieving conditional user list.
     *
     * @param errorCode        Error code.
     * @param errorMassage     Error Message.
     * @param domain           user store domain.
     * @param profileName      profile name.
     * @param limit            number of search results.
     * @param offset           start index of the search.
     * @param sortBy           sort by attribute.
     * @param sortOrder        sort order.
     * @param userStoreManager user store domain.
     * @throws UserStoreException UserStoreException
     */
    public boolean onGetUserListFailure(String errorCode, String errorMassage, Condition condition, String domain,
                                        String profileName, int limit, int offset, String sortBy, String sortOrder,
                                        UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onUpdatePermissionsOfRoleFailure(String errorCode, String errorMessage, String roleName,
            Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions that need to be done if there is a failure retrieving paginated user list.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param claim            Claim URI
     * @param claimValue       Claim Value
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    boolean onGetPaginatedUserListFailure(String errorCode, String errorMessage, String claim, String claimValue,
                                          String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    /**
     * Defines any additional actions that need to be done if there is a failure retrieving paginated user list.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param filter           Username filter.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param userStoreManager User Store Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    boolean onListUsersFailure(String errorCode, String errorMessage, String filter, int limit, int offset,
                               UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onAuthenticateFailureWithID(String errorCode, String errorMessage, String userID, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onAddUserFailureWithID(String errorCode, String errorMessage, String userID, Object credential,
            String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onUpdateCredentialFailureWithID(String errorCode, String errorMessage, String userID,
            Object newCredential, Object oldCredential, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onUpdateCredentialByAdminFailureWithID(String errorCode, String errorMessage, String userID,
            Object newCredential, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onDeleteUserFailureWithID(String errorCode, String errorMessage, String userID,
            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onSetUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID,
            String claimURI, String claimValue, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onSetUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userID,
            Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onDeleteUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userID,
            String[] claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onDeleteUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID,
            String claimURI, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onAddRoleFailureWithID(String errorCode, String errorMessage, String roleName, String[] userList,
            Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onUpdateUserListOfRoleFailureWithID(String errorCode, String errorMessage, String roleName,
            String[] deletedUsers, String[] newUsers, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onUpdateRoleListOfUserFailureWithID(String errorCode, String errorMessage, String userID,
            String[] deletedRoles, String[] newRoles, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onGetUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID, String claim,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onGetUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userID,
            String[] claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onGetUserListFailureWithID(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onUpdatePermissionsOfRoleFailureWithID(String errorCode, String errorMessage, String roleName,
            Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }
}
