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
 *
 */

package org.wso2.carbon.user.core.common;

import java.io.Serializable;
import java.util.Map;

/**
 * Abstract implementation of Groups and Users.
 */
public class AbstractEntity implements Serializable {

    private static final long serialVersionUID = -9038468040609754623L;
    private String id;
    private String displayName;
    private String tenantDomain;
    private String userStoreDomain;
    private Map<String, String> attributes;

    public AbstractEntity(String id) {

        this.id = id;
    }

    public AbstractEntity(String id, String displayName) {

        this.id = id;
        this.displayName = displayName;
    }

    public AbstractEntity(String id, String displayName, String tenantDomain,
                          String userStoreDomain, Map<String, String> attributes) {

        this.id = id;
        this.displayName = displayName;
        this.tenantDomain = tenantDomain;
        this.userStoreDomain = userStoreDomain;
        this.attributes = attributes;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    public void setUserStoreDomain(String userStoreDomain) {

        this.userStoreDomain = userStoreDomain;
    }

    public Map<String, String> getAttributes() {

        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {

        this.attributes = attributes;
    }
}
