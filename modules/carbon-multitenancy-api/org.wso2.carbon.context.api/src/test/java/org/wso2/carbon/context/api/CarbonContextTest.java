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

import javax.security.auth.Subject;

public class CarbonContextTest {
    private static final String TENANT_DOMAIN = "test";

    @Test
    public void testCarbonContext() {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        Assert.assertEquals(carbonContext.getTenantDomain(), null);
        Assert.assertEquals(carbonContext.getSubject(), null);
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() {
        Subject subject = new Subject();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), null);

        privilegedCarbonContext.setTenantDomain(TENANT_DOMAIN);
        privilegedCarbonContext.setSubject(subject);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getSubject(), subject);

        PrivilegedCarbonContext.destroyCurrentContext();
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), null);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getSubject(), null);
    }

    @Test(dependsOnMethods = "testPrivilegeCarbonContext")
    public void testCarbonContextTenantFlow1() {
        String tenantDomain = "tenant1";
        Subject subject = new Subject();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantDomain(TENANT_DOMAIN);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext tenantCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenantCarbonContext.setTenantDomain(tenantDomain);
            tenantCarbonContext.setSubject(subject);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getSubject(), subject);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);
    }

    @Test(dependsOnMethods = "testCarbonContextTenantFlow1")
    public void testCarbonContextTenantFlow2() {
        String tenantDomain1 = "tenant1";
        Subject subject1 = new Subject();
        String tenantDomain2 = "tenant2";
        Subject subject2 = new Subject();

        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantDomain(TENANT_DOMAIN);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext tenant1CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenant1CarbonContext.setTenantDomain(tenantDomain1);
            tenant1CarbonContext.setSubject(subject1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getSubject(), subject1);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext tenant2CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenant2CarbonContext.setTenantDomain(tenantDomain2);
            tenant2CarbonContext.setSubject(subject2);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain2);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getSubject(), subject2);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);
    }

    @Test(dependsOnMethods = "testCarbonContextTenantFlow2")
    public void testCarbonContextTenantFlow3() {
        String tenantDomain1 = "tenant1";
        Subject subject1 = new Subject();
        String tenantDomain2 = "tenant2";
        Subject subject2 = new Subject();

        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantDomain(TENANT_DOMAIN);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext tenant1CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenant1CarbonContext.setTenantDomain(tenantDomain1);
            tenant1CarbonContext.setSubject(subject1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getSubject(), subject1);

            PrivilegedCarbonContext.startTenantFlow();
            try {
                PrivilegedCarbonContext tenant2CarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                tenant2CarbonContext.setTenantDomain(tenantDomain2);
                tenant2CarbonContext.setSubject(subject2);
                Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain2);
                Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getSubject(), subject2);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getSubject(), subject1);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), TENANT_DOMAIN);
    }
}
