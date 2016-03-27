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
package org.wso2.carbon.kernel.context;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;
import org.wso2.carbon.kernel.internal.DataHolder;
import org.wso2.carbon.kernel.internal.config.YAMLBasedConfigProvider;
import org.wso2.carbon.kernel.internal.context.CarbonRuntimeFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.stream.IntStream;

/**
 * Test class for CarbonContext API usage.
 *
 * @since 5.0.0
 */
public class CarbonContextTest {
    private static final String TENANT_NAME = "tenant.name";
    private static final Path testDir = Paths.get("src", "test", "resources");

    @Test
    public void testCarbonContext() throws Exception {
        CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
        Assert.assertEquals(carbonContext.getTenant(), Constants.DEFAULT_TENANT);
        Assert.assertEquals(carbonContext.getUserPrincipal(), null);
        Assert.assertEquals(carbonContext.getProperty("someProperty"), null);
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() throws Exception {
        Principal userPrincipal = () -> "test";
        String carbonContextPropertyKey = "KEY";
        Object carbonContextPropertyValue = "VALUE";
        setupCarbonConfig(Constants.DEFAULT_TENANT);
        PrivilegedCarbonContext privilegedCarbonContext =
                (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(),
                Constants.DEFAULT_TENANT);
        try {
            privilegedCarbonContext.setUserPrincipal(userPrincipal);
            privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getProperty(carbonContextPropertyKey),
                    carbonContextPropertyValue);
        } finally {
            clearSystemProperties();
        }
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), null);
    }

    @Test(dependsOnMethods = "testPrivilegeCarbonContext")
    public void testCarbonContextFaultyScenario() throws Exception {
        Principal userPrincipal1 = () -> "test1";
        Principal userPrincipal2 = () -> "test2";

        try {
            clearSystemProperties();
            setupCarbonConfig(Constants.DEFAULT_TENANT);
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), Constants.DEFAULT_TENANT);
            try {
                privilegedCarbonContext.setUserPrincipal(userPrincipal1);
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal1);
                privilegedCarbonContext.setUserPrincipal(userPrincipal2);
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("Trying to override the already available user principal " +
                        "from " + userPrincipal1.toString() + " to " + userPrincipal2.toString()));
            }
        } finally {
            clearSystemProperties();
        }
    }

    @Test(dependsOnMethods = "testCarbonContextFaultyScenario")
    public void testSystemTenantDomainCarbonContextPopulation() throws Exception {
        try {
            String tenant = "test-sys-domain";
            setupCarbonConfig(tenant);
            CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(carbonContext.getTenant(), tenant);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), tenant);
        } finally {
            clearSystemProperties();
        }
    }

    @Test(dependsOnMethods = "testSystemTenantDomainCarbonContextPopulation")
    public void testMultiThreadedCarbonContextInvocation() throws Exception {
        setupCarbonConfig(Constants.DEFAULT_TENANT);
        IntStream.range(1, 10)
                .forEach(id ->
                        {
                            CarbonContextInvoker invoker = new CarbonContextInvoker("ccPropertyKey" + id,
                                    "ccPropertyVal" + id);
                            invoker.start();
                        }
                );
    }

    class CarbonContextInvoker extends Thread {
        String carbonContextPropertyKey;
        Object carbonContextPropertyValue;

        CarbonContextInvoker(String carbonContextPropertyKey, Object carbonContextPropertyValue) {
            this.carbonContextPropertyKey = carbonContextPropertyKey;
            this.carbonContextPropertyValue = carbonContextPropertyValue;
        }

        @Override
        public void run() {
            try {
                PrivilegedCarbonContext.destroyCurrentContext();
                PrivilegedCarbonContext privilegedCarbonContext =
                        (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getTenant(), Constants.DEFAULT_TENANT);
                Principal userPrincipal = () -> "test";
                privilegedCarbonContext.setUserPrincipal(userPrincipal);
                privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
                CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
                Assert.assertEquals(carbonContext.getUserPrincipal(), userPrincipal);
                Assert.assertEquals(carbonContext.getProperty(carbonContextPropertyKey), carbonContextPropertyValue);
            } catch (Exception e) {
                throw new RuntimeException("Error while running CarbonContextInvoker multi thread test case", e);
            } finally {
                clearSystemProperties();
            }
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), null);
        }
    }

    private void clearSystemProperties() {
        PrivilegedCarbonContext.destroyCurrentContext();
        System.clearProperty(TENANT_NAME);
        System.clearProperty(Constants.CARBON_HOME);
    }


    private void setupCarbonConfig(String tenantName) throws Exception {
        System.setProperty(Constants.CARBON_HOME, Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty(TENANT_NAME, tenantName);
        CarbonConfigProvider configProvider = new YAMLBasedConfigProvider();
        CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);
        DataHolder.getInstance().setCarbonRuntime(carbonRuntime);
    }
}
