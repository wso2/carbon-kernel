/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core;

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.tenant.Tenant;

public class TenantTestUtil {
    public static Tenant[] createTenant(RealmConfiguration realmConfig) throws Exception {
        Tenant[] arr = new Tenant[3];

        Tenant t1 = new Tenant();
        t1.setAdminName("admin");
        t1.setDomain("domain1");
        t1.setEmail("tenant1@domain1.com");
        t1.setRealmConfig(realmConfig);
        arr[0] = t1;

        Tenant t2 = new Tenant();
        t2.setAdminName("admin1");
        t2.setDomain("domain2");
        t2.setEmail("tenant2@domain2.com");
        t2.setRealmConfig(realmConfig);
        arr[1] = t2;

        Tenant t3 = new Tenant();
        t3.setAdminName("admin2");
        t3.setDomain("domain3");
        t3.setEmail("tenant3@domain3.com");
        t3.setRealmConfig(realmConfig);
        arr[2] = t3;

        return arr;
    }
}
