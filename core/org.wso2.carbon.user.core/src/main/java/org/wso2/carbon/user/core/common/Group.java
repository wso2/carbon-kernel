/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.claim.Claim;

import java.util.List;
import java.util.Map;

/**
 * Represents a Group.
 */
public class Group extends Entity {

    private static final long serialVersionUID = -6157030956831929121L;
    private List<Permission> permissions;

    public Group(String groupID) {

        super(groupID);
    }

    public Group(String groupID, String groupName) {

        super(groupID, groupName);
    }

    public Group(String groupID, String groupName, String displayName, String tenantDomain, String userStoreDomain,
                 List<Claim> claims) {

        super(groupID, groupName, displayName, tenantDomain, userStoreDomain, claims);
    }

    public Group(String groupID, String groupName, String displayName, String tenantDomain, String userStoreDomain,
                 List<Claim> claims, List<Permission> permissions) {

        super(groupID, groupName, displayName, tenantDomain, userStoreDomain, claims);
        this.permissions = permissions;
    }

    public String getGroupID() {

        return super.getId();
    }

    public void setGroupID(String groupID) {

        super.setId(groupID);
    }

    public String getGroupName() {

        return super.getName();
    }

    public void setGroupName(String groupName) {

        super.setName(groupName);
    }

    public List<Permission> getPermissions() {

        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {

        this.permissions = permissions;
    }
}
