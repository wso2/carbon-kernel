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
import org.wso2.carbon.multitenancy.api.Tenant;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Test class for CarbonContext API usage.
 *
 * @since 5.0.0
 */
public class CarbonContextTest {
    private static final String TENANT_DOMAIN = "test";
    private static final String TENANT_PROPERTY = "testProperty";

    @Test
    public void testCarbonContext() {
        CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
        Assert.assertEquals(carbonContext.getTenant(), null);
        Assert.assertEquals(carbonContext.getUserPrincipal(), null);
        Assert.assertEquals(carbonContext.getProperty("someProperty"), null);
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() {
        Principal userPrincipal = () -> "test";
        TenantSupplier tenantSupplier = () -> new Tenant(TENANT_DOMAIN);
        String tenantPropertyValue = "testValue";
        Map<String, Object> properties = new HashMap<>();
        properties.put(TENANT_PROPERTY, tenantPropertyValue);
        String carbonContextPropertyKey = "KEY";
        Object carbonContextPropertyValue = "VALUE";
        PrivilegedCarbonContext privilegedCarbonContext =
                (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);

        try {
            privilegedCarbonContext.setTenant(tenantSupplier);
            privilegedCarbonContext.setUserPrincipal(userPrincipal);
            privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getDomain(), TENANT_DOMAIN);
            Tenant tenant = PrivilegedCarbonContext.getCurrentContext().getTenant();
            tenant.setProperties(properties);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getProperty(TENANT_PROPERTY),
                    tenantPropertyValue);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getProperty(carbonContextPropertyKey),
                    carbonContextPropertyValue);
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }

        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), null);
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testMultiThreadedCarbonContextInvocation() {
        IntStream.range(1, 10)
                .forEach(id ->
                        {
                            CarbonContextInvoker invoker = new CarbonContextInvoker("tenantDomain" + id,
                                    "tenantPropertyKey" + id, "tenantPropertyVal" + id, "ccPropertyKey" + id,
                                    "ccPropertyVal" + id);
                            invoker.start();
                        }
                );
    }

    class CarbonContextInvoker extends Thread {
        String tenantDomain;
        String tenantPropertyKey;
        String tenantPropertyValue;
        String carbonContextPropertyKey;
        Object carbonContextPropertyValue;

        CarbonContextInvoker(String tenantDomain, String tenantPropertyKey, String tenantPropertyValue,
                             String carbonContextPropertyKey, Object carbonContextPropertyValue) {
            this.tenantDomain = tenantDomain;
            this.tenantPropertyKey = tenantPropertyKey;
            this.tenantPropertyValue = tenantPropertyValue;
            this.carbonContextPropertyKey = carbonContextPropertyKey;
            this.carbonContextPropertyValue = carbonContextPropertyValue;
        }

        @Override
        public void run() {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);

            Principal userPrincipal = () -> "test";
            TenantSupplier tenantSupplier = () -> new Tenant(tenantDomain);
            Map<String, Object> properties = new HashMap<>();
            properties.put(tenantPropertyKey, tenantPropertyValue);
            try {
                privilegedCarbonContext.setTenant(tenantSupplier);
                privilegedCarbonContext.setUserPrincipal(userPrincipal);
                privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);

                CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
                Assert.assertEquals(carbonContext.getTenant().getDomain(), tenantDomain);
                Tenant tenant = PrivilegedCarbonContext.getCurrentContext().getTenant();
                tenant.setProperties(properties);
                Assert.assertEquals(carbonContext.getTenant().getProperty(tenantPropertyKey), tenantPropertyValue);
                Assert.assertEquals(carbonContext.getUserPrincipal(), userPrincipal);
                Assert.assertEquals(carbonContext.getProperty(carbonContextPropertyKey), carbonContextPropertyValue);
            } finally {
                PrivilegedCarbonContext.destroyCurrentContext();
            }

            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), null);
        }
    }


    @Test(dependsOnMethods = "testMultiThreadedCarbonContextInvocation")
    public void testCarbonContextFaultyScenario1() {
        String tenantDomain1 = "tenant1";
        String tenantDomain2 = "tenant2";
        TenantSupplier tenantSupplier1 = () -> new Tenant(tenantDomain1);
        TenantSupplier tenantSupplier2 = () -> new Tenant(tenantDomain2);
        try {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            try {
                privilegedCarbonContext.setTenant(tenantSupplier1);
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getDomain(), tenantDomain1);
                privilegedCarbonContext.setTenant(tenantSupplier2);
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("Trying to override the current tenant " + tenantDomain1 +
                        " to " + tenantDomain2));
            }
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(dependsOnMethods = "testCarbonContextFaultyScenario1")
    public void testCarbonContextFaultyScenario2() {
        Principal userPrincipal1 = () -> "test1";
        Principal userPrincipal2 = () -> "test2";
        TenantSupplier tenantSupplier1 = () -> new Tenant(TENANT_DOMAIN);

        try {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);
            try {
                privilegedCarbonContext.setTenant(tenantSupplier1);
                privilegedCarbonContext.setUserPrincipal(userPrincipal1);
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getDomain(),
                        TENANT_DOMAIN);
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal1);
                privilegedCarbonContext.setUserPrincipal(userPrincipal2);
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("Trying to override the already available user principal " +
                        "from " + userPrincipal1.toString() + " to " + userPrincipal2.toString()));
            }
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(dependsOnMethods = "testCarbonContextFaultyScenario2")
    public void testSystemTenantDomainCarbonContextPopulation1() {
        String tenantDomain = "test-sys-domain";
        System.setProperty(CarbonContextUtils.TENANT_DOMAIN, tenantDomain);
        CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
        Assert.assertEquals(carbonContext.getTenant().getDomain(), tenantDomain);
        TenantSupplier tenantSupplier = () -> new Tenant(TENANT_DOMAIN);
        PrivilegedCarbonContext privilegedCarbonContext =
                (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
        privilegedCarbonContext.setTenant(tenantSupplier);
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getDomain(), tenantDomain);

        System.clearProperty(CarbonContextUtils.TENANT_DOMAIN);
    }
}
