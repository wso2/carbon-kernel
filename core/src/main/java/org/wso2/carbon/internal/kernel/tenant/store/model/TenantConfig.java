/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.internal.kernel.tenant.store.model;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * JAXB mapping for Tenant Config
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TenantConfig {

    @XmlElement(name = "Id", required = true)
    private String id;

    @XmlElement(name = "Domain", required = true)
    private String domain;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "CreatedDate")
    private Date createdDate;

    @XmlElement(name = "AdminUser", required = true)
    private AdminUserConfig adminUserConfig;

    @XmlElement(name = "Hierarchy", required = true)
    private HierarchyConfig hierarchyConfig;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate != null ? new Date(createdDate.getTime()) : null;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = new Date(createdDate.getTime());
    }

    public AdminUserConfig getAdminUserConfig() {
        return adminUserConfig;
    }

    public void setAdminUserConfig(AdminUserConfig adminUserConfig) {
        this.adminUserConfig = adminUserConfig;
    }

    public HierarchyConfig getHierarchyConfig() {
        return hierarchyConfig;
    }

    public void setHierarchyConfig(HierarchyConfig hierarchyConfig) {
        this.hierarchyConfig = hierarchyConfig;
    }
}
