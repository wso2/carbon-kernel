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
package org.wso2.carbon.context;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.utils.ServerConstants;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for CarbonContext API usage.
 */
public class CarbonContextTest {
    private static final Path testDir = Paths.get("src", "test");

    @Test
    public void testCarbonContext() throws Exception {
        System.setProperty(ServerConstants.CARBON_HOME, testDir.toString());
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        Assert.assertEquals(carbonContext.getUsername(), null);
        Assert.assertEquals(carbonContext.getApplicationName(), null);
        Assert.assertEquals(carbonContext.getTenantDomain(), null);
        Assert.assertEquals(carbonContext.getTenantId(), -1);
        Assert.assertEquals(carbonContext.getUserRealm(), null);
        Assert.assertEquals(carbonContext.getCarbonContextDataHolder(),
                CarbonContextDataHolder.getThreadLocalCarbonContextHolder());
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() throws Exception {
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = "test";
        int tenantID = 123;
        String applicationName = "testApp";
        String username = "testUser";

        privilegedCarbonContext.setTenantDomain(tenantDomain);
        privilegedCarbonContext.setTenantId(tenantID);
        privilegedCarbonContext.setApplicationName(applicationName);
        privilegedCarbonContext.setUsername(username);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantID);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getApplicationName(), applicationName);
        Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getUsername(), username);
        PrivilegedCarbonContext.destroyCurrentContext();
    }


    @Test(dependsOnMethods = "testPrivilegeCarbonContext")
    public void testTenantCarbonContext1() throws Exception {
        try {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                String tenantDomain = "testTenant1";
                int tenantID = 5551;
                String applicationName = "testTenant1App";
                String username = "testTenant1User";

                privilegedCarbonContext.setTenantDomain(tenantDomain);
                privilegedCarbonContext.setTenantId(tenantID);
                privilegedCarbonContext.setApplicationName(applicationName);
                privilegedCarbonContext.setUsername(username);
                Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain);
                Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantID);
                Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getApplicationName(), applicationName);
                Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getUsername(), username);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

            testSecondTenantFlow();
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(dependsOnMethods = "testTenantCarbonContext1")
    public void testTenantCarbonContext2() throws Exception {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext1 = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            String tenantDomain1 = "testTenant1";
            int tenantID1 = 5551;
            String applicationName1 = "testTenant1App";
            String username1 = "testTenant1User";

            privilegedCarbonContext1.setTenantDomain(tenantDomain1);
            privilegedCarbonContext1.setTenantId(tenantID1);
            privilegedCarbonContext1.setApplicationName(applicationName1);
            privilegedCarbonContext1.setUsername(username1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantID1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getApplicationName(), applicationName1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getUsername(), username1);

            testSecondTenantFlow();

            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantID1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getApplicationName(), applicationName1);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getUsername(), username1);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    private void testSecondTenantFlow() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext2 =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext();
            String tenantDomain2 = "testTenant2";
            int tenantID2 = 5552;
            String applicationName2 = "testTenant2App";
            String username2 = "testTenant2User";

            privilegedCarbonContext2.setTenantDomain(tenantDomain2);
            privilegedCarbonContext2.setTenantId(tenantID2);
            privilegedCarbonContext2.setApplicationName(applicationName2);
            privilegedCarbonContext2.setUsername(username2);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), tenantDomain2);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantID2);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getApplicationName(), applicationName2);
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getUsername(), username2);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    @Test(dependsOnMethods = "testTenantCarbonContext2")
    public void testMultiThreadedCarbonContextInvocation() throws Exception {
        for (int id = 1; id <= 10; id++) {
            CarbonContextInvoker invoker = new CarbonContextInvoker(id, "ccTenantDomain" + id);
            invoker.start();
            while (invoker.isAlive()) {
                Thread.sleep(00);
            }
        }
    }

    private class CarbonContextInvoker extends Thread {
        int tenantID;
        String tenantDomain;

        CarbonContextInvoker(int tenantID, String tenantDomain) {
            this.tenantID = tenantID;
            this.tenantDomain = tenantDomain;
        }

        @Override
        public void run() {
            try {
                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantId(tenantID);
                privilegedCarbonContext.setTenantDomain(tenantDomain);
                CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
                Assert.assertEquals(carbonContext.getTenantId(), tenantID);
                Assert.assertEquals(carbonContext.getTenantDomain(), tenantDomain);
            } catch (Exception e) {
                throw new RuntimeException("Error while running CarbonContextInvoker multi thread test case", e);
            } finally {
                PrivilegedCarbonContext.destroyCurrentContext();
            }
            Assert.assertEquals(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), null);
        }
    }

}
