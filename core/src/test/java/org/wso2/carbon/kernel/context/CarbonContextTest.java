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
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.internal.context.CarbonConfigProviderImpl;
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
    private static final Path testDir = Paths.get("src", "test", "resources");

    @Test
    public void testCarbonContext() throws Exception {
        CarbonContext carbonContext = CarbonContext.getCurrentContext();
        Assert.assertEquals(carbonContext.getUserPrincipal(), null);
        Assert.assertEquals(carbonContext.getProperty("someProperty"), null);
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() throws Exception {
        Principal userPrincipal = () -> "test";
        String carbonContextPropertyKey = "KEY";
        Object carbonContextPropertyValue = "VALUE";
        setupCarbonConfig(Constants.DEFAULT_TENANT);
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getCurrentContext();
        try {
            privilegedCarbonContext.setUserPrincipal(userPrincipal);
            privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
            Assert.assertEquals(CarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal);
            Assert.assertEquals(CarbonContext.getCurrentContext().getProperty(carbonContextPropertyKey),
                    carbonContextPropertyValue);
        } finally {
            clearSystemProperties();
        }
        Assert.assertEquals(CarbonContext.getCurrentContext().getUserPrincipal(), null);
    }

    @Test(dependsOnMethods = "testPrivilegeCarbonContext")
    public void testCarbonContextFaultyScenario() throws Exception {
        Principal userPrincipal1 = () -> "test1";
        Principal userPrincipal2 = () -> "test2";

        try {
            clearSystemProperties();
            setupCarbonConfig(Constants.DEFAULT_TENANT);
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getCurrentContext();
            try {
                privilegedCarbonContext.setUserPrincipal(userPrincipal1);
                Assert.assertEquals(CarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal1);
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

    private class CarbonContextInvoker extends Thread {
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
                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getCurrentContext();
                Principal userPrincipal = () -> "test";
                privilegedCarbonContext.setUserPrincipal(userPrincipal);
                privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
                CarbonContext carbonContext = CarbonContext.getCurrentContext();
                Assert.assertEquals(carbonContext.getUserPrincipal(), userPrincipal);
                Assert.assertEquals(carbonContext.getProperty(carbonContextPropertyKey), carbonContextPropertyValue);
            } catch (Exception e) {
                throw new RuntimeException("Error while running CarbonContextInvoker multi thread test case", e);
            } finally {
                clearSystemProperties();
            }
            Assert.assertEquals(CarbonContext.getCurrentContext().getUserPrincipal(), null);
        }
    }

    private void clearSystemProperties() {
        PrivilegedCarbonContext.destroyCurrentContext();
        System.clearProperty(Constants.TENANT_NAME);
        System.clearProperty(org.wso2.carbon.utils.Constants.CARBON_HOME);
    }


    private void setupCarbonConfig(String tenantName) throws Exception {
        System.setProperty(org.wso2.carbon.utils.Constants.CARBON_HOME,
                           Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty(Constants.TENANT_NAME, tenantName);
        ConfigProvider configProvider = new CarbonConfigProviderImpl();
        CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);
    }
}
