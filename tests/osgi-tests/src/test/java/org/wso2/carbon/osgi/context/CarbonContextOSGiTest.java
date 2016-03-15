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
package org.wso2.carbon.osgi.context;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.context.CarbonContext;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.exception.TenantStoreException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.utils.OSGiTestUtils;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

/**
 * CarbonContextOSGiTest class is to test the functionality of the Carbon Context API.
 *
 * @since 5.1.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CarbonContextOSGiTest {
    private static final String TEST_TENANT_DOMAIN = "test.tenant.domain";

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        System.setProperty(Constants.TENANT_DOMAIN, TEST_TENANT_DOMAIN);
        OSGiTestUtils.setupOSGiTestEnvironment();
        OSGiTestUtils.copyCarbonYAML();
        OSGiTestUtils.copyCarbonTenantXML();
        return OSGiTestUtils.getDefaultPaxOptions();
    }

    @Test
    public void testCarbonContext() {
        try {
            CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(carbonContext.getServerTenant().getDomain(), TEST_TENANT_DOMAIN);
            Assert.assertEquals(carbonContext.getUserPrincipal(), null);
            Assert.assertEquals(carbonContext.getProperty("someProperty"), null);
        } finally {
            System.clearProperty(Constants.TENANT_DOMAIN);
        }
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() throws TenantStoreException {
        Principal userPrincipal = () -> "test";
        String tenantPropertyTestKey = "testKey";
        String tenantPropertyTestValue = "testValue";
        Map<String, Object> properties = new HashMap<>();
        properties.put(tenantPropertyTestKey, tenantPropertyTestValue);
        String carbonContextPropertyKey = "KEY";
        Object carbonContextPropertyValue = "VALUE";

        try {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getServerTenant().getDomain(),
                    TEST_TENANT_DOMAIN);
            privilegedCarbonContext.setUserPrincipal(userPrincipal);
            privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
            Tenant tenant = PrivilegedCarbonContext.getCurrentContext().getServerTenant();
            tenant.setProperties(properties);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getServerTenant().
                    getProperty(tenantPropertyTestKey), tenantPropertyTestValue);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal);
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getProperty(carbonContextPropertyKey),
                    carbonContextPropertyValue);
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
            System.clearProperty(Constants.TENANT_DOMAIN);
        }
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), null);
    }

    @Test(dependsOnMethods = "testPrivilegeCarbonContext")
    public void testCarbonContextFaultyScenario() {
        PrivilegedCarbonContext.destroyCurrentContext();
        System.setProperty(Constants.TENANT_DOMAIN, TEST_TENANT_DOMAIN);
        Principal userPrincipal1 = () -> "test1";
        Principal userPrincipal2 = () -> "test2";
        try {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getServerTenant().getDomain(),
                    TEST_TENANT_DOMAIN);
            try {
                privilegedCarbonContext.setUserPrincipal(userPrincipal1);
                Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal1);
                privilegedCarbonContext.setUserPrincipal(userPrincipal2);
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("Trying to override the already available user principal " +
                        "from " + userPrincipal1.toString() + " to " + userPrincipal2.toString()));
            }
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
            System.clearProperty(Constants.TENANT_DOMAIN);
        }
    }

}
