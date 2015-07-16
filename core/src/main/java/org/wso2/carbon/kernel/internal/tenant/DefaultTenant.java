/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.kernel.internal.tenant;

import org.wso2.carbon.kernel.tenant.TenantContainerBase;
import org.wso2.carbon.kernel.tenant.Tenant;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class DefaultTenant extends TenantContainerBase implements Tenant {

    private String id;
    private String domain;
    private String name;
    private String description;
    private Date createdDate;
    private String adminUsername;
    private String adminUserEmailAddress;
    private Map<String, String> properties;

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public String getAdminUsername() {
        return adminUsername;
    }

    @Override
    public String getAdminUserEmailAddress() {
        return adminUserEmailAddress;
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public void setID(String id) {
        this.id = id;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    @Override
    public void setAdminUserEmailAddress(String emailAddress) {
        this.adminUserEmailAddress = emailAddress;
    }

    @Override
    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }

    @Override
    public void setProperties(Map<String, String> props) {
        this.properties = props;
    }
}
