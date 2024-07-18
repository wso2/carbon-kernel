/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.user.core.common;

import java.util.Collections;
import java.util.List;

/**
 * Class to model paginated username search result for unique id user stores.
 */
public class UniqueIDPaginatedUsernameSearchResult {

    private List<String> users;
    private PaginatedSearchResult paginatedSearchResult;

    // This variable is set only when users.length = 0. When filtered user count is zero for a given user store, it is
    // required to know how many users skipped in that user store to identify the start index of next user store.
    private int skippedUserCount;

    public List<String> getUsers() {

        if (users == null) {
            return Collections.emptyList();
        }
        return users;
    }

    public void setUsers(List<String> users) {

        this.users = users;
    }

    public int getSkippedUserCount() {

        return skippedUserCount;
    }

    public void setSkippedUserCount(int skippedUserCount) {

        this.skippedUserCount = skippedUserCount;
    }

    public PaginatedSearchResult getPaginatedSearchResult() {

        return paginatedSearchResult;
    }

    public void setPaginatedSearchResult(PaginatedSearchResult paginatedSearchResult) {

        this.paginatedSearchResult = paginatedSearchResult;
    }
}
