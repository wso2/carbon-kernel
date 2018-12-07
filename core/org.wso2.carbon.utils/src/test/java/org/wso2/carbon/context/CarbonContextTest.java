/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.context;

import org.eclipse.osgi.internal.framework.FilterImpl;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserRealmService;

import static org.mockito.Mockito.when;

/**
 * Test class for CarbonContext API usage.
 */
@Test(dependsOnGroups = {"org.wso2.carbon.utils.logging", "org.wso2.carbon.utils.base"})
public class CarbonContextTest extends BaseTest {

    @Test(groups = {"org.wso2.carbon.context"})
    public void testCarbonContext() throws Exception {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        Assert.assertEquals(carbonContext.getUsername(), null);
        Assert.assertEquals(carbonContext.getApplicationName(), null);
        Assert.assertEquals(carbonContext.getTenantDomain(), null);
        Assert.assertEquals(carbonContext.getTenantId(), -1);
        Assert.assertEquals(carbonContext.getUserRealm(), null);
        Assert.assertEquals(carbonContext.getCarbonContextDataHolder(),
                CarbonContextDataHolder.getThreadLocalCarbonContextHolder());
    }

    @Test(groups = {"org.wso2.carbon.context"}, dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() throws Exception {
        try {
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
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }


    @Test(groups = {"org.wso2.carbon.context"}, dependsOnMethods = "testPrivilegeCarbonContext")
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

    @Test(groups = {"org.wso2.carbon.context"}, dependsOnMethods = "testTenantCarbonContext1")
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


    @Test(groups = {"org.wso2.carbon.context"}, dependsOnMethods = "testTenantCarbonContext2")
    public void testMultiThreadedCarbonContextInvocation() throws Exception {
        for (int id = 1; id <= 10; id++) {
            CarbonContextInvoker invoker = new CarbonContextInvoker(id, "ccTenantDomain" + id);
            invoker.start();
            while (invoker.isAlive()) {
                Thread.sleep(100);
            }
        }
    }

    private static class CarbonContextInvoker extends Thread {
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

    @Test(groups = {"org.wso2.carbon.context"}, dependsOnMethods = "testMultiThreadedCarbonContextInvocation")
    public void testPrivilegedCarbonContext2() throws Exception {
        try {
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            String tenantDomain = "test222";
            int tenantID = 222;
            String applicationName = "testApp222";
            String username = "testUser222";

            UserRealm userRealm = Mockito.mock(UserRealm.class);
            Registry registry = Mockito.mock(Registry.class);

            privilegedCarbonContext.setTenantDomain(tenantDomain);
            privilegedCarbonContext.setTenantId(tenantID);
            privilegedCarbonContext.setApplicationName(applicationName);
            privilegedCarbonContext.setUsername(username);
            privilegedCarbonContext.setUserRealm(userRealm);
            privilegedCarbonContext.setRegistry(RegistryType.SYSTEM_CONFIGURATION, registry);
            privilegedCarbonContext.setRegistry(RegistryType.SYSTEM_GOVERNANCE, registry);
            privilegedCarbonContext.setRegistry(RegistryType.USER_CONFIGURATION, registry);
            privilegedCarbonContext.setRegistry(RegistryType.USER_GOVERNANCE, registry);
            privilegedCarbonContext.setRegistry(RegistryType.LOCAL_REPOSITORY, registry);

            OSGiDataHolder dataHolder = OSGiDataHolder.getInstance();

            RegistryService registryService = Mockito.mock(RegistryService.class);
            dataHolder.setRegistryService(registryService);

            when(registryService.getConfigSystemRegistry(tenantID)).thenReturn(registry);
            when(registryService.getConfigUserRegistry(username, tenantID)).thenReturn(registry);
            when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
            when(registryService.getGovernanceUserRegistry(username, tenantID)).thenReturn(registry);
            when(registryService.getLocalRepository(tenantID)).thenReturn(registry);

            Assert.assertEquals(privilegedCarbonContext.getRegistry(RegistryType.SYSTEM_CONFIGURATION), registry);
            Assert.assertEquals(privilegedCarbonContext.getRegistry(RegistryType.SYSTEM_GOVERNANCE), registry);
            Assert.assertEquals(privilegedCarbonContext.getRegistry(RegistryType.USER_CONFIGURATION), registry);
            Assert.assertEquals(privilegedCarbonContext.getRegistry(RegistryType.USER_GOVERNANCE), registry);
            Assert.assertEquals(privilegedCarbonContext.getRegistry(RegistryType.LOCAL_REPOSITORY), registry);

            Assert.assertEquals(privilegedCarbonContext.getUserRealm(), userRealm);
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(groups = {"org.wso2.carbon.context"}, dependsOnMethods = "testPrivilegedCarbonContext2")
    public void testPrivilegedCarbonContext3() throws Exception {
        try {
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            String tenantDomain = "test333";
            int tenantID = 333;
            String applicationName = "testApp333";
            String username = "testUser333";

            UserRealmService userRealmService = Mockito.mock(UserRealmService.class);

            OSGiDataHolder dataHolder = OSGiDataHolder.getInstance();
            dataHolder.setUserRealmService(userRealmService);

            privilegedCarbonContext.setTenantDomain(tenantDomain, true);
            privilegedCarbonContext.setTenantId(tenantID, true);
            privilegedCarbonContext.setApplicationName(applicationName);
            privilegedCarbonContext.setUsername(username);

            Assert.assertEquals(privilegedCarbonContext.getTenantDomain(true), tenantDomain);
            Assert.assertEquals(privilegedCarbonContext.getTenantId(true), tenantID);
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(groups = {"org.wso2.carbon.context"}, expectedExceptions = NullPointerException.class,
            dependsOnMethods = "testPrivilegedCarbonContext3")
    public void testPrivilegedCarbonContext4() throws Exception {
        try {
            mockBundleContext();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            String tenantDomain = "test444";
            int tenantID = 444;
            String applicationName = "testApp444";
            String username = "testUser444";

            privilegedCarbonContext.setTenantDomain(tenantDomain);
            privilegedCarbonContext.setTenantId(tenantID);
            privilegedCarbonContext.setApplicationName(applicationName);
            privilegedCarbonContext.setUsername(username);
            privilegedCarbonContext.getOSGiService(CarbonContext.class, null);
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(groups = {"org.wso2.carbon.context"}, expectedExceptions = NullPointerException.class,
            dependsOnMethods = "testPrivilegedCarbonContext4")
    public void testPrivilegedCarbonContext5() throws Exception {
        try {
            mockBundleContext();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            String tenantDomain = "test555";
            int tenantID = 555;
            String applicationName = "testApp555";
            String username = "testUser555";

            privilegedCarbonContext.setTenantDomain(tenantDomain);
            privilegedCarbonContext.setTenantId(tenantID);
            privilegedCarbonContext.setApplicationName(applicationName);
            privilegedCarbonContext.setUsername(username);
            privilegedCarbonContext.getOSGiServices(CarbonContext.class, null);
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(groups = {"org.wso2.carbon.context"}, expectedExceptions = NullPointerException.class,
            dependsOnMethods = "testPrivilegedCarbonContext5")
    public void testCarbonContext2() throws Exception {
        mockBundleContext();
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        carbonContext.getOSGiService(CarbonContext.class, null);
    }

    @Test(groups = {"org.wso2.carbon.context"}, expectedExceptions = NullPointerException.class,
            dependsOnMethods = "testCarbonContext2")
    public void testCarbonContext3() throws Exception {
        mockBundleContext();
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        carbonContext.getOSGiServices(CarbonContext.class, null);
    }

    private void mockBundleContext() throws InvalidSyntaxException {
        String filter = "(objectClass=" + CarbonContext.class.getName() + ")";
        BundleContext bundleContext = Mockito.mock(BundleContext.class);

        when(bundleContext.createFilter(filter)).thenReturn(FilterImpl.newInstance(filter));

        OSGiDataHolder dataHolder = OSGiDataHolder.getInstance();
        dataHolder.setBundleContext(bundleContext);
    }
}
