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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.model;

import org.wso2.carbon.user.api.User;

/**
 * Default implementation for the user.
 * @since 4.4.5
 */
public class UserImpl implements User {

    private String username;
    private String userId;
    private int tenantId;
    private String domainName;

    public UserImpl() {
        super();
    }

    public UserImpl(String id, String username) {
        this.userId = id;
        this.username = username;
    }

    @Override
    public void setUserName(String name) {
        this.username = name;
    }

    @Override
    public void setId(String id) {
        this.userId = id;
    }

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public void setTenantId(int id) {
        this.tenantId = id;
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }
}
