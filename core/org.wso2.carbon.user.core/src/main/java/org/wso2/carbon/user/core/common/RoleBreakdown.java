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

public class RoleBreakdown {

    private String[] roles;
    private Integer[] tenantIds;

    private String[] sharedRoles;
    private Integer[] sharedTenantIDs;

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public Integer[] getTenantIds() {
        return tenantIds;
    }

    public void setTenantIds(Integer[] tenantIds) {
        this.tenantIds = tenantIds;
    }

    public String[] getSharedRoles() {
        return sharedRoles;
    }

    public void setSharedRoles(String[] sharedRoles) {
        this.sharedRoles = sharedRoles;
    }

    public Integer[] getSharedTenantIDs() {
        return sharedTenantIDs;
    }

    public void setSharedTenantIDs(Integer[] sharedTenantIDs) {
        this.sharedTenantIDs = sharedTenantIDs;
    }

}
