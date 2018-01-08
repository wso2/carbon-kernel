/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.api;

/**
 * Manager to handle username and user id mapping.
 */
public interface UserIdManager {

    /**
     * Retrieve username from user's user id.
     * @param userId User id of the user.
     * @return Username as a string.
     */
    String getUsernameFromUserId(String userId);

    /**
     * Retrieve user id from user's username.
     * @param username Username of the user.
     * @return User id as a string.
     */
    String getUserIdFromUsername(String username);

    /**
     * Add an user id for user that does not currently have an user id.
     * @param username Username of the user.
     * @param userId id of the user.
     */
    void addUserIdForUsername(String username, String userId);
}
