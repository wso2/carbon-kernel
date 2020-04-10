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

import java.io.Serializable;

/**
 * Model class of Tenant Unique id cache key.
 */
public class TenantUniqueIDKey implements Serializable {

    private static final long serialVersionUID = 5955431181622617223L;
    private String tenantUniqueID;

    public TenantUniqueIDKey(String tenantUniqueID) {

        this.tenantUniqueID = tenantUniqueID;
    }

    public String getTenantUniqueID() {

        return tenantUniqueID;
    }

    @Override
    public boolean equals(Object otherObject) {

        if (!(otherObject instanceof TenantUniqueIDKey)) {
            return false;
        }

        TenantUniqueIDKey uniqueIDKey = (TenantUniqueIDKey) otherObject;

        if (tenantUniqueID != null && !tenantUniqueID.equals(uniqueIDKey.getTenantUniqueID())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        return tenantUniqueID.hashCode();
    }
}
