/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.context.api;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CarbonContextTest {
    private static final int TENANT_ID = 12345;
    private static final String TENANT_DOMAIN = "test";

    @Test
    public void testCarbonContext() {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        Assert.assertEquals(carbonContext.getTenantId(), Constants.INVALID_TENANT_ID);
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() {
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Assert.assertEquals(privilegedCarbonContext.getTenantId(), Constants.INVALID_TENANT_ID);

        privilegedCarbonContext.setTenantId(TENANT_ID);
        privilegedCarbonContext.setTenantDomain(TENANT_DOMAIN);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), TENANT_ID);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);

        PrivilegedCarbonContext.destroyCurrentContext();
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(),
                Constants.INVALID_TENANT_ID);
    }

    @Test(dependsOnMethods = "testPrivilegeCarbonContext")
    public void testCarbonContextTenantFlow1() {
        int tenantId1 = 1234;
        String tenantDomain1 = "tenant1";
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(TENANT_ID);
        privilegedCarbonContext.setTenantDomain(TENANT_DOMAIN);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), TENANT_ID);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext tenant1CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenant1CarbonContext.setTenantId(tenantId1);
            tenant1CarbonContext.setTenantDomain(tenantDomain1);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantId1);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), TENANT_ID);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);
    }

    @Test(dependsOnMethods = "testCarbonContextTenantFlow1")
    public void testCarbonContextTenantFlow2() {
        int tenantId1 = 1234;
        String tenantDomain1 = "tenant1";
        int tenantId2 = 4321;
        String tenantDomain2 = "tenant2";

        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(TENANT_ID);
        privilegedCarbonContext.setTenantDomain(TENANT_DOMAIN);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), TENANT_ID);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext tenant1CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenant1CarbonContext.setTenantId(tenantId1);
            tenant1CarbonContext.setTenantDomain(tenantDomain1);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantId1);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), TENANT_ID);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext tenant2CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenant2CarbonContext.setTenantId(tenantId2);
            tenant2CarbonContext.setTenantDomain(tenantDomain2);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantId2);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain2);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), TENANT_ID);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);
    }

    @Test(dependsOnMethods = "testCarbonContextTenantFlow2")
    public void testCarbonContextTenantFlow3() {
        int tenantId1 = 1234;
        String tenantDomain1 = "tenant1";
        int tenantId2 = 4321;
        String tenantDomain2 = "tenant2";

        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(TENANT_ID);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), TENANT_ID);

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext tenant1CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenant1CarbonContext.setTenantId(tenantId1);
            tenant1CarbonContext.setTenantDomain(tenantDomain1);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantId1);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);

            PrivilegedCarbonContext.startTenantFlow();
            try {
                PrivilegedCarbonContext tenant2CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                tenant2CarbonContext.setTenantId(tenantId2);
                tenant2CarbonContext.setTenantDomain(tenantDomain2);
                Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantId2);
                Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                        tenantDomain2);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantId1);
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), TENANT_ID);
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);
    }
}
