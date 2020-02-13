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

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Map;

/**
 * Represents the user.
 *
 * @since 4.6.0
 */
public class User extends Entity {

    private static final long serialVersionUID = -8811345359211553015L;
    private String preferredUsername;
    private Map<String, String> attributes;

    public User() {

        super();
    }

    public User(String userID) {

        super(userID);
    }

    public User(String userID, String username, String preferredUsername) {

        super(userID, username);
        this.preferredUsername = preferredUsername;
    }

    public User(String userID, String username, String preferredUsername, String displayName, String tenantDomain,
                String userStoreDomain, Map<String, String> attributes) {

        super(userID, username, displayName, tenantDomain, userStoreDomain);
        this.attributes = attributes;
        this.preferredUsername = preferredUsername;
    }

    public String getUserID() {

        return super.getId();
    }

    public void setUserID(String userID) {

        super.setId(userID);
    }

    public String getUsername() {

        return super.getName();
    }

    public void setUsername(String username) {

        super.setName(username);
    }

    public String getDomainQualifiedUsername() {

        return UserCoreUtil.addDomainToName(super.getName(), super.getUserStoreDomain());
    }

    public String getFullQualifiedUsername() {

        String domainQualifiedUsername = getDomainQualifiedUsername();
        return UserCoreUtil.addTenantDomainToEntry(domainQualifiedUsername, super.getTenantDomain());
    }

    public Map<String, String> getAttributes() {

        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {

        this.attributes = attributes;
    }

    public String getPreferredUsername() {

        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {

        this.preferredUsername = preferredUsername;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof User) {
            return this.getFullQualifiedUsername().equals(((User) obj).getFullQualifiedUsername());
        }

        return false;
    }

    @Override
    public int hashCode() {

        return this.getFullQualifiedUsername().hashCode();
    }
}
