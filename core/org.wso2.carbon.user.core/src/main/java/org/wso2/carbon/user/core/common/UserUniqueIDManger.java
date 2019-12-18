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
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.constants.UserCoreClaimConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This will manage the relationship between the user unique id in the system against the unique id in the user store.
 */
public class UserUniqueIDManger {

    /**
     * Add new user and create a unique user id for that user.
     * @param username Username in the user store.
     * @return User object with unique user id.
     */
    public User addUser(String username, String profileName, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        Map<String, String> claims = new HashMap<>();
        String uniqueId = generateUniqueId();

        claims.put(UserCoreClaimConstants.USER_ID_CLAIM_URI, uniqueId);

        userStoreManager.setUserClaimValues(username, claims, profileName);

        User user = new User();
        user.setUserID(uniqueId);
        user.setUsername(username);
        user.setUserStoreDomain(userStoreManager.getMyDomainName());

        return user;
    }

    /**
     * Get user from unique id.
     * @param uniqueId User's unique id.
     * @return User object if user presents for the unique id. Null otherwise.
     */
    public User getUser(String uniqueId, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        String[] usernames = userStoreManager.getUserList(UserCoreClaimConstants.USER_ID_CLAIM_URI, uniqueId, null);

        if (usernames.length > 1) {
            throw new UserStoreException("More than one user presents with the same user unique id.");
        }

        if (usernames.length == 0) {
            return null;
        }

        User user = new User();
        user.setUserID(uniqueId);
        user.setUsername(usernames[0]);
        user.setUserStoreDomain(userStoreManager.getMyDomainName());

        return user;
    }

    /**
     * Get list of uesr's from the provided user id list.
     * @param userIds List of user ids'
     * @param userStoreManager User store manger.
     * @return List of users.
     */
    public List<User> getUsers(List<String> userIds, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        List<User> users = new ArrayList<>();
        for (String userId : userIds) {
            User user = userStoreManager.getUserWithID(userId, new String[0], null);
            users.add(user);
        }

        return users;
    }

    /**
     * Get user's unique id from the claims.
     * @param username Username of the user.
     * @param userStoreManager User store manger to use.
     * @return User's unique id.
     */
    public String getUniqueId(String username, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        return userStoreManager.getUserClaimValues(username, new String[]{UserCoreClaimConstants.USER_ID_CLAIM_URI},
                null).get(UserCoreClaimConstants.USER_ID_CLAIM_URI);
    }

    /**
     * Check whether the user exists using the user id.
     * @param uniqueId user id.
     * @return True if user exists.
     */
    public boolean checkUserExist(String uniqueId, String profile, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        String[] usernames = userStoreManager.getUserList(UserCoreClaimConstants.USER_ID_CLAIM_URI, uniqueId, profile);
        if (usernames.length > 1) {
            throw new UserStoreException("More than one user presents with the same user unique id.");
        }

        return usernames.length != 0;
    }

    /**
     * Get paginated user list from paginated search result.
     * @param paginatedSearchResult Paginated search result.
     * @param userStoreManager User store manger instance.
     * @return @see UniqueIDPaginatedSearchResult
     */
    public UniqueIDPaginatedSearchResult listUsers(PaginatedSearchResult paginatedSearchResult,
                                                   AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        UniqueIDPaginatedSearchResult uniqueIDPaginatedSearchResult = new UniqueIDPaginatedSearchResult();
        List<User> users = new ArrayList<>();
        for (String username : paginatedSearchResult.getUsers()) {
            User user = new User();
            String uniqueId = getUniqueId(username, userStoreManager);
            if (StringUtils.isEmpty(uniqueId)) {
                user = addUser(username, null, userStoreManager);
            } else {
                user.setUserID(uniqueId);
            }
            user.setUsername(username);
            users.add(user);
        }
        uniqueIDPaginatedSearchResult.setUsers(users);
        uniqueIDPaginatedSearchResult.setSkippedUserCount(paginatedSearchResult.getSkippedUserCount());
        return uniqueIDPaginatedSearchResult;
    }

    /**
     * Get list of user's from array of user names.
     * @param listUsers List of user names.
     * @param userStoreManager User store manger instance.
     * @return List of @see User objects.
     */
    public List<User> listUsers(String[] listUsers, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        return listUsers(Arrays.asList(listUsers), userStoreManager);
    }

    /**
     * Get list of user's from list of user names.
     * @param listUsers List of user names.
     * @param userStoreManager User store manger instance.
     * @return List of @see User objects.
     */
    public List<User> listUsers(List<String> listUsers, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        List<User> users = new ArrayList<>();
        for (String username : listUsers) {
            User user = new User();
            String uniqueId = getUniqueId(username, userStoreManager);
            user.setUsername(username);
            user.setUserID(uniqueId);
            user.setUserStoreDomain(userStoreManager.getMyDomainName());
            users.add(user);
        }

        return users;
    }

    /**
     * Generate an unique identifier.
     * @return String representation of the unique identifier.
     */
    protected String generateUniqueId() {

        return UUID.randomUUID().toString();
    }
}
