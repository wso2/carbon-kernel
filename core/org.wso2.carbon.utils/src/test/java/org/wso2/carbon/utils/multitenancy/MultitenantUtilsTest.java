/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.utils.multitenancy;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class MultitenantUtilsTest {


    @Test(groups = {"org.wso2.carbon.utils.logging"},
            description = "")
    public void testGetTenantAwareUsername() {
        assertEquals(MultitenantUtils.getTenantAwareUsername("admin"), "admin", "Failed to result expected TenantAwareUsername");
        assertEquals(MultitenantUtils.getTenantAwareUsername("admin@carbon.super"), "admin", "Failed to result expected TenantAwareUsername");
        assertEquals(MultitenantUtils.getTenantAwareUsername("admin@wso2.com"), "admin", "Failed to result expected TenantAwareUsername");
        assertEquals(MultitenantUtils.getTenantAwareUsername("user@abc.com@t2.com"), "user@abc.com", "Failed to result expected TenantAwareUsername");
    }
}
