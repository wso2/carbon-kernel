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

package org.wso2.carbon.user.core.model;

import org.wso2.carbon.user.core.common.User;

import java.util.Map;

/**
 * Model class used to represent user with attributes.
 */
public class UniqueIDUserClaimSearchEntry {

    private User user;
    private Map<String, String> claims;

    private UserClaimSearchEntry userClaimSearchEntry;

    public User getUser() {

        return user;
    }

    public void setUser(User user) {

        this.user = user;
    }

    public Map<String, String> getClaims() {

        return claims;
    }

    public void setClaims(Map<String, String> claims) {

        this.claims = claims;
    }

    public UserClaimSearchEntry getUserClaimSearchEntry() {

        return userClaimSearchEntry;
    }

    public void setUserClaimSearchEntry(UserClaimSearchEntry userClaimSearchEntry) {

        this.userClaimSearchEntry = userClaimSearchEntry;
    }
}
