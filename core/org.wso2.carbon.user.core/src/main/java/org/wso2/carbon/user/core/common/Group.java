/*
 *  Copyright (c) (2020-2021), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.List;

/**
 * Group class which can be used to represent a set of users.
 */
public class Group extends Entity {

    private static final long serialVersionUID = -6157030956831929121L;
    private String createdDate;
    private String lastModifiedDate;
    private String location;

    /**
     * Represents the constructor.
     */
    public Group() {

        super();
    }

    /**
     * Represents the constructor.
     *
     * @param groupID group ID.
     */
    public Group(String groupID) {

        super(groupID);
    }

    /**
     * Represents the constructor.
     *
     * @param groupID   group ID.
     * @param groupName group name.
     */
    public Group(String groupID, String groupName) {

        super(groupID, groupName);
    }

    /**
     * Represents the constructor.
     *
     * @param groupID         group ID.
     * @param groupName       group name.
     * @param displayName     display name.
     * @param tenantDomain    tenant domain.
     * @param userStoreDomain user store domain.
     * @param claims          claims.
     */
    public Group(String groupID, String groupName, String displayName, String tenantDomain, String userStoreDomain,
            List<Claim> claims) {

        super(groupID, groupName, displayName, tenantDomain, userStoreDomain, claims);
    }

    /**
     * Get group ID.
     *
     * @return group ID.
     */
    public String getGroupID() {

        return super.getId();
    }

    /**
     * Set group ID.
     *
     * @param groupID group ID.
     */
    public void setGroupID(String groupID) {

        super.setId(groupID);
    }

    /**
     * Get group name.
     *
     * @return group name.
     */
    public String getGroupName() {

        return super.getName();
    }

    /**
     * Set group name.
     *
     * @param groupName group name.
     */
    public void setGroupName(String groupName) {

        super.setName(groupName);
    }

    /**
     * Get group created date.
     *
     * @return Group created date.
     */
    public String getCreatedDate() {

        return createdDate;
    }

    /**
     * Set group created date.
     *
     * @param createdDate Group created date.
     */
    public void setCreatedDate(String createdDate) {

        this.createdDate = createdDate;
    }

    /**
     * Get group last modified date.
     *
     * @return Group last modified date.
     */
    public String getLastModifiedDate() {

        return lastModifiedDate;
    }

    /**
     * Set group last modified date.
     *
     * @param lastModifiedDate Group last modified date.
     */
    public void setLastModifiedDate(String lastModifiedDate) {

        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * Get group location.
     *
     * @return Group location.
     */
    public String getLocation() {

        return location;
    }

    /**
     * Set group location.
     *
     * @param location Group location.
     */
    public void setLocation(String location) {

        this.location = location;
    }
}
