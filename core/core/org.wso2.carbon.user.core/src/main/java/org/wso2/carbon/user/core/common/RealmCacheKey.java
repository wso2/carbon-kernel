/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.base.MultitenantConstants;

import java.io.Serializable;

public class RealmCacheKey implements Serializable {

    private static final long serialVersionUID = -2758605151199576047L;

    private String key;

    public RealmCacheKey(int tenantId, String realmName) {
        this.key = realmName +
                (tenantId == MultitenantConstants.SUPER_TENANT_ID ? 0 : tenantId);
    }

    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof RealmCacheKey)) {
            return false;
        }
        RealmCacheKey otherKey = (RealmCacheKey) otherObject;
        if (key.equals(otherKey.getKey())) {
            return true;
        }
        return false;
    }

    public String getKey() {
        return key;
    }

    public int hashCode() {
        return key.hashCode();
    }

}
