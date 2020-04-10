/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.user.core.tenant;

import java.util.List;

/**
 * Holds tenant's pagination related information.
 */
public class TenantSearchResult {

    private List<Tenant> tenantList;
    private int totalTenantCount;
    private int limit;
    private int offSet;
    private String filter;
    private String sortOrder;
    private String sortBy;

    public List<Tenant> getTenantList() {

        return tenantList;
    }

    public void setTenantList(List<Tenant> tenantList) {

        this.tenantList = tenantList;
    }

    public int getTotalTenantCount() {

        return totalTenantCount;
    }

    public void setTotalTenantCount(int totalTenantCount) {

        this.totalTenantCount = totalTenantCount;
    }

    public int getLimit() {

        return limit;
    }

    public void setLimit(int limit) {

        this.limit = limit;
    }

    public int getOffSet() {

        return offSet;
    }

    public void setOffSet(int offSet) {

        this.offSet = offSet;
    }

    public void setFilter(String filter) {

        this.filter = filter;
    }

    public String getFilter() {

        return filter;
    }

    public void setSortBy(String sortBy) {

        this.sortBy = sortBy;
    }

    public String getSortBy() {

        return sortBy;
    }

    public void setSortOrder(String sortOrder) {

        this.sortOrder = sortOrder;
    }

    public String getSortOrder() {

        return sortOrder;
    }
}
