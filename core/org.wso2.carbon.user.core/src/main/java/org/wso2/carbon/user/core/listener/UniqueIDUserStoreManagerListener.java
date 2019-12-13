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

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.LoginIdentifier;

import java.util.List;
import java.util.Map;

public interface UniqueIDUserStoreManagerListener extends UserStoreManagerListener {

    /**
     * Given the login identifiers and a credential object, the implementation code must
     * validate whether the user is authenticated.
     *
     * @param preferredUserNameClaim Preferred username claim.
     * @param preferredUserNameValue Preferred username value.
     * @param credential             The credential of a user.
     * @param userStoreManager       The underlying UserStoreManager.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean authenticateWithID(String preferredUserNameClaim, String preferredUserNameValue, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Given the login identifiers and a credential object, the implementation code must
     * validate whether the user is authenticated.
     *
     * @param loginIdentifiers Login identifiers.
     * @param credential       The credential of a user.
     * @param userStoreManager The underlying UserStoreManager.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean authenticateWithID(List<LoginIdentifier> loginIdentifiers, Object credential,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Add a user to the user store.
     *
     * @param userName         User name of the user.
     * @param credential       The credential/password of the user.
     * @param roleList         The roles that user belongs.
     * @param claims           Properties of the user.
     * @param profileName      The name of the profile.
     * @param userStoreManager The underlying UserStoreManager.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean addUserWithID(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Update the credential/password of the user.
     *
     * @param userID           The user ID.
     * @param newCredential    The new credential/password.
     * @param oldCredential    The old credential/password.
     * @param userStoreManager The underlying UserStoreManager.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean updateCredentialWithID(String userID, Object newCredential, Object oldCredential,
            UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Update credential/password by the admin of another user.
     *
     * @param userID           The user ID.
     * @param newCredential    The new credential.
     * @param userStoreManager The underlying UserStoreManager.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean updateCredentialByAdminWithID(String userID, Object newCredential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Delete the user with the given user name.
     *
     * @param userID           The user ID.
     * @param userStoreManager The underlying UserStoreManager.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    boolean deleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException;

}
