/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.user.core.common;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This will manage the relationship between the user unique id in the system against the unique id in the user store.
 */
public class UserUniqueIDManger {

    private static final String USER_ID_CLAIM = "http://wso2.org/claims/identity/uuid";

    /**
     * Add new user and create a unique user id for that user.
     * @param username Username in the user store.
     * @return User object with unique user id.
     */
    public User addUser(String username, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        Map<String, String> claims = new HashMap<>();
        String uniqueId = generateUniqueId();
        claims.put(USER_ID_CLAIM, uniqueId);

        userStoreManager.setUserClaimValues(username, claims, profileName);

        User user = new User();
        user.setUserID(uniqueId);
        user.setUsername(username);
        user.setUserStoreDomain(getUserStoreDomainName(userStoreManager));

        return user;
    }

    /**
     * Get user from unique id.
     * @param uniqueId User's unique id.
     * @return User object if user presents for the unique id. Null otherwise.
     */
    public User getUser(String uniqueId, String profile, UserStoreManager userStoreManager) throws UserStoreException {

        String[] usernames = userStoreManager.getUserList(USER_ID_CLAIM, uniqueId, profile);

        if (usernames.length > 1) {
            throw new UserStoreException("More than one user presents with the same user unique id.");
        }

        if (usernames.length == 0) {
            return null;
        }

        User user = new User();
        user.setUserID(uniqueId);
        user.setUsername(usernames[0]);

        return user;
    }

    /**
     * Get user's unique id from the claims.
     * @param username Username of the user.
     * @param profile Profile name of the user.
     * @param userStoreManager User store manger to use.
     * @return User's unique id.
     */
    public String getUniqueId(String username, String profile, UserStoreManager userStoreManager)
            throws UserStoreException {

        String userUniqueId = userStoreManager.getUserClaimValue(username, USER_ID_CLAIM, profile);
        return userUniqueId;
    }

    /**
     * Check whether the user exists using the user id.
     * @param uniqueId user id.
     * @return True if user exists.
     */
    public boolean checkUserExist(String uniqueId, String profile, UserStoreManager userStoreManager)
            throws UserStoreException {

        String[] usernames = userStoreManager.getUserList(USER_ID_CLAIM, uniqueId, profile);

        if (usernames.length > 1) {
            throw new UserStoreException("More than one user presents with the same user unique id.");
        }

        return usernames.length != 0;
    }

    protected String generateUniqueId() {

        return UUID.randomUUID().toString();
    }

    private String getUserStoreDomainName(UserStoreManager userStoreManager) {

        String domainNameProperty;
        domainNameProperty = userStoreManager.getRealmConfiguration()
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        if (StringUtils.isEmpty(domainNameProperty)) {
            domainNameProperty = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
        return domainNameProperty;
    }

}
