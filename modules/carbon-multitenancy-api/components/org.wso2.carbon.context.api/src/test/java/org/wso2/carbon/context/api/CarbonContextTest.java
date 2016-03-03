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
import org.wso2.carbon.multitenancy.impl.CarbonTenant;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javax.security.auth.Subject;

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
        Assert.assertEquals(carbonContext.getSubject(), null);
        Assert.assertEquals(carbonContext.getProperty("someProperty"), null);
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() {
        Subject subject = new Subject();
        Tenant tenant = new CarbonTenant(TENANT_DOMAIN);
        String tenantPropertyValue = "testValue";
        Map<String, Object> properties = new HashMap<>();
        properties.put(TENANT_PROPERTY, tenantPropertyValue);
        tenant.setProperties(properties);
        String carbonContextPropertyKey = "KEY";
        Object carbonContextPropertyValue = "VALUE";
        PrivilegedCarbonContext privilegedCarbonContext =
                (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);

        try {
            privilegedCarbonContext.setTenant(tenant);
            privilegedCarbonContext.setSubject(subject);
            privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getDomain(), TENANT_DOMAIN);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getProperty(TENANT_PROPERTY),
                    tenantPropertyValue);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getSubject(), subject);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getProperty(carbonContextPropertyKey),
                    carbonContextPropertyValue);
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }

        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getSubject(), null);
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

            Subject subject = new Subject();
            Tenant tenant = new CarbonTenant(tenantDomain);
            Map<String, Object> properties = new HashMap<>();
            properties.put(tenantPropertyKey, tenantPropertyValue);
            tenant.setProperties(properties);
            try {
                privilegedCarbonContext.setTenant(tenant);
                privilegedCarbonContext.setSubject(subject);
                privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);

                CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
                Assert.assertEquals(carbonContext.getTenant().getDomain(), tenantDomain);
                Assert.assertEquals(carbonContext.getTenant().getProperty(tenantPropertyKey), tenantPropertyValue);
                Assert.assertEquals(carbonContext.getSubject(), subject);
                Assert.assertEquals(carbonContext.getProperty(carbonContextPropertyKey), carbonContextPropertyValue);
            } finally {
                PrivilegedCarbonContext.destroyCurrentContext();
            }

            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getSubject(), null);
        }
    }


    @Test(dependsOnMethods = "testMultiThreadedCarbonContextInvocation")
    public void testCarbonContextFaultyScenario1() {
        Tenant tenant1 = new CarbonTenant(TENANT_DOMAIN);
        String tenantDomain2 = "tenant2";
        Tenant tenant2 = new CarbonTenant(tenantDomain2);
        try {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            try {
                privilegedCarbonContext.setTenant(tenant1);
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getDomain(), TENANT_DOMAIN);
                privilegedCarbonContext.setTenant(tenant2);
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("Trying to override the current tenant " + TENANT_DOMAIN +
                        " to " + tenantDomain2));
            }
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(dependsOnMethods = "testCarbonContextFaultyScenario1")
    public void testCarbonContextFaultyScenario2() {
        Subject subject1 = new Subject();
        Subject subject2 = new Subject();
        Tenant tenant = new CarbonTenant(TENANT_DOMAIN);

        try {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), null);
            try {
                privilegedCarbonContext.setTenant(tenant);
                privilegedCarbonContext.setSubject(subject1);
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant().getDomain(),
                        TENANT_DOMAIN);
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getSubject(), subject1);
                privilegedCarbonContext.setSubject(subject2);
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("Trying to override the already available subject " +
                        subject1.toString() + " to" + subject2.toString()));
            }
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }
}
