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

import org.wso2.carbon.user.api.UserRealm;

import java.io.Serializable;

public class RealmCacheEntry implements Serializable {

    private static final long serialVersionUID = -5827839354382128293L;

    private UserRealm userRealm = null;

    public RealmCacheEntry(UserRealm userRealm) {
        this.userRealm = userRealm;
    }

    public UserRealm getUserRealm() {
        return userRealm;
    }

    public void setUserRealm(UserRealm userRealm) {
        this.userRealm = userRealm;
    }

}
