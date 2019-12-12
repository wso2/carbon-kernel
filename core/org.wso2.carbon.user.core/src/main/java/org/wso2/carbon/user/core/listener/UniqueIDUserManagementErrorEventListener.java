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
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.model.Condition;

import java.util.List;
import java.util.Map;

/**
 * This allows an extension point to implement various additional operations when there is a failure in any of the
 * User Management operations.
 */
public interface UniqueIDUserManagementErrorEventListener extends UserManagementErrorEventListener {

    /**
     * Defines any additional actions that need to be done when there is an authentication failure.
     *
     * @param errorCode              Error code.
     * @param errorMessage           Error Message.
     * @param preferredUserNameClaim Preferred username claim.
     * @param preferredUserNameValue Preferred username value.
     * @param credential             Relevant credential provided.
     * @param userStoreManager       UserStore Manager.
     * @return true if the handling succeeded.
     * @throws UserStoreException Exception that will be thrown during erroneous scenarios.
     */
    boolean onAuthenticateFailureWithID(String errorCode, String errorMessage, String preferredUserNameClaim,
            String preferredUserNameValue, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is an authentication failure.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error Message.
     * @param loginIdentifiers Login identifiers.
     * @param credential       Relevant credential provided.
     * @param userStoreManager UserStore Manager.
     * @return true if the handling succeeded.
     * @throws UserStoreException Exception that will be thrown during erroneous scenarios.
     */
    boolean onAuthenticateFailureWithID(String errorCode, String errorMessage, List<LoginIdentifier> loginIdentifiers,
            Object credential, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is an authentication failure.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error Message.
     * @param userID           user ID.
     * @param credential       Relevant credential provided.
     * @param userStoreManager UserStore Manager.
     * @return true if the handling succeeded.
     * @throws UserStoreException Exception that will be thrown during erroneous scenarios.
     */
    boolean onAuthenticateFailureWithID(String errorCode, String errorMessage, String userID, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines additional actions that need to be done when there is a failure while trying to add user.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message relevant to the the particular erroneous scenario.
     * @param userID           Name of the user.
     * @param credential       Relevant credential.
     * @param roleList         List of the roles added for this user.
     * @param claims           Claims added for the user.
     * @param profile          Profile of the User.
     * @param userStoreManager UserStore Manager.
     * @return true if the handling succeeded.
     * @throws UserStoreException Exception that would be thrown if there is an erroneous case.
     */
    boolean onAddUserFailureWithID(String errorCode, String errorMessage, String userID, Object credential,
            String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done whether there is a failure while trying to update
     * credential of a user.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message relevant particular scenario.
     * @param userID           Name of the user.
     * @param newCredential    New credential.
     * @param oldCredential    Old credential.
     * @param userStoreManager UserStore Manager
     * @return true if the handling succeeded.
     * @throws UserStoreException Exception that would be thrown if there is an un-expected error.
     */
    boolean onUpdateCredentialFailureWithID(String errorCode, String errorMessage, String userID, Object newCredential,
            Object oldCredential, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is failure while an update credential operation
     * is done by admin.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message relevant to particular scenario.
     * @param userID           Name of the user.
     * @param newCredential    New credential.
     * @param userStoreManager UserStore Manager
     * @return true if the handling succeeded.
     * @throws UserStoreException Exception that would be thrown if there is an un-expected error.
     */
    boolean onUpdateCredentialByAdminFailureWithID(String errorCode, String errorMessage, String userID,
            Object newCredential, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure while trying to delete a user.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message relevant to particular scenario.
     * @param userID           Name of the user.
     * @param userStoreManager User Store Manager
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that would be thrown if there is an un-expected error.
     */
    boolean onDeleteUserFailureWithID(String errorCode, String errorMessage, String userID,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done whether there is a failure while trying to set particular
     * user claim value.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message relevant to particular scenario.
     * @param userID           Name of the user.
     * @param claimURI         Claim URI.
     * @param claimValue       Relevant claim value.
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that would be thrown if there is an un-expected error.
     */
    boolean onSetUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID, String claimURI,
            String claimValue, String profileName, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure while trying to set user claim
     * values.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message relevant to particular scenario.
     * @param userID           Name of the user.
     * @param claims           Relevant claims.
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that would be thrown if there is an un-expected error.
     */
    boolean onSetUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userID,
            Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is failure while trying to delete the user claim
     * values.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param userID           Name of the user.
     * @param claims           Claims that are requested to be deleted.
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that would be thrown if there is an un-expected error.
     */
    boolean onDeleteUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userID, String[] claims,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be whether there is failure while trying to delete user claim value.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param userID           Name of the user.
     * @param claimURI         Claim URI that is requested to be deleted.
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of this method.
     */
    boolean onDeleteUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID, String claimURI,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done if there is a failure while trying to add a role.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param roleName         Name of the role..
     * @param userList         List of the users who are intended to assigned to the user.
     * @param permissions      Permission of the role.
     * @param userStoreManager User Store Manager
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of this method.
     */
    boolean onAddRoleFailureWithID(String errorCode, String errorMessage, String roleName, String[] userList,
            Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done if there is failure while trying to update user list of a
     * role.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param roleName         Name of the role.
     * @param deletedUsers     Deleted users.
     * @param newUsers         New users.
     * @param userStoreManager UserStore Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    boolean onUpdateUserListOfRoleFailureWithID(String errorCode, String errorMessage, String roleName,
            String deletedUsers[], String[] newUsers, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done if there is a failure while trying to update role of a user.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param userID           Name of the user.
     * @param deletedRoles     Deleted roles.
     * @param newRoles         New roles.
     * @param userStoreManager User Store Manager
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    boolean onUpdateRoleListOfUserFailureWithID(String errorCode, String errorMessage, String userID,
            String[] deletedRoles, String[] newRoles, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done if there is a failure while trying to get the user claim
     * value of a user.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param userID           Name of the user.
     * @param claim            Relevant claim that is retrieved.
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    boolean onGetUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID, String claim,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done if there is failure while trying to get the user claim
     * values of a user.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message
     * @param userID           Name of the user
     * @param claims           Relevant claim that is retrieved.
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    boolean onGetUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userID, String[] claims,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done if there is a failure retrieving user list.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param claim            Claim URI.
     * @param claimValue       Claim Value.
     * @param profileName      Name of the profile.
     * @param userStoreManager User Store Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    boolean onGetUserListFailureWithID(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException;

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
    boolean onGetUserListFailureWithID(String errorCode, String errorMessage, String claim, String claimValue,
            int limit, int offset, String profileName, UserStoreManager userStoreManager) throws UserStoreException;

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
    boolean onGetUserListFailureWithID(String errorCode, String errorMassage, Condition condition, String domain,
            String profileName, int limit, int offset, String sortBy, String sortOrder,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done if there is a failure retrieving the user.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param userID           user ID.
     * @param requestedClaims  Requested Claims.
     * @param profileName      Profile Name.
     * @param userStoreManager User Store Manager.
     * @return true if the handing succeeded.
     * @throws UserStoreException Exception that will be thrown during the execution of the method.
     */
    boolean onGetUserFailureWithID(String errorCode, String errorMessage, String userID, String[] requestedClaims,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done if there is a failure while updating permissions of a role.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param roleName         Name of the role.
     * @param permissions      Update permissions of the role.
     * @param userStoreManager User Store Manager.
     * @return true if handling succeeded.
     * @throws UserStoreException User Store Exception will be thrown if there is an issue during execution.
     */
    boolean onUpdatePermissionsOfRoleFailureWithID(String errorCode, String errorMessage, String roleName,
            Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException;

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
    boolean onGetPaginatedUserListFailureWithID(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException;

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
    boolean onListUsersFailureWithID(String errorCode, String errorMessage, String filter, int limit, int offset,
            UserStoreManager userStoreManager) throws UserStoreException;

}
