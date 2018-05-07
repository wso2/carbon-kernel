/*
 * Copyright 2018 WSO2, Inc. (http://wso2.com)
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

import org.wso2.carbon.user.core.model.UserClaimSearchEntry;

import java.util.List;
import java.util.Map;

/**
 * This interface provides the pagination support of user operations.
 */
public interface PaginatedUserStoreManager {

    /**
     * Get claim values of users.
     *
     * @param userNames User names
     * @param claims    Required claims
     * @return User claim search entry set
     * @throws UserStoreException
     */
    UserClaimSearchEntry[] getUsersClaimValues(String[] userNames, String[] claims, String profileName)
            throws UserStoreException;


    /**
     * Get roles of a users.
     *
     * @param userNames user names
     * @return A map contains a list of role names each user belongs.
     * @throws UserStoreException
     */
    Map<String, List<String>> getRoleListOfUsers(String[] userNames) throws UserStoreException;
}
