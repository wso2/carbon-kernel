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
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.test.CarbonContextInvoker;
import org.wso2.carbon.context.test.InMemoryTenantStore;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.context.CarbonContext;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantStore;
import org.wso2.carbon.kernel.tenant.exception.TenantStoreException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.utils.OSGiTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * CarbonContextOSGiTest class is to test the functionality of the Carbon Context API.
 *
 * @since 5.1.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CarbonContextOSGiTest {
    private static final String TEST_TENANT_DOMAIN1 = "test.tenant.domain";
    private static final String TEST_TENANT_DOMAIN2 = "test.tenant.domain.2";

    private static final Logger logger = LoggerFactory.getLogger(CarbonContextOSGiTest.class);

    @Inject
    private TenantStore<Tenant> tenantStore;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        System.setProperty(Constants.TENANT_DOMAIN, TEST_TENANT_DOMAIN1);
        OSGiTestUtils.setupOSGiTestEnvironment();
        OSGiTestUtils.copyCarbonYAML();
        copyCarbonTenantXML();
        Option[] options = CoreOptions.options(
                mavenBundle().artifactId("carbon-context-test-artifact").groupId(
                        "org.wso2.carbon").versionAsInProject()
        );

        return OSGiTestUtils.getDefaultPaxOptions(options);
    }

    @Test
    public void testCarbonContext() {
        CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
        Assert.assertEquals(carbonContext.getServerTenant().getDomain(), TEST_TENANT_DOMAIN1);
        Assert.assertEquals(carbonContext.getUserPrincipal(), null);
        Assert.assertEquals(carbonContext.getProperty("someProperty"), null);
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
        PrivilegedCarbonContext privilegedCarbonContext =
                (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getServerTenant().getDomain(),
                TEST_TENANT_DOMAIN1);

        try {
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
        }

        Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getUserPrincipal(), null);
    }

    @Test(dependsOnMethods = "testPrivilegeCarbonContext")
    public void testCarbonContextFaultyScenario() {
        Principal userPrincipal1 = () -> "test1";
        Principal userPrincipal2 = () -> "test2";

        try {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(PrivilegedCarbonContext.getCurrentContext().getServerTenant().getDomain(),
                    TEST_TENANT_DOMAIN1);
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
        }
    }


    @Test(dependsOnMethods = "testCarbonContextFaultyScenario")
    public void testCustomBundle() {
        String tenantPropertyKey = "tenantPropertyKey";
        String tenantPropertyValue = "tenantPropertyValue";
        String carbonContextPropertyKey = "carbonContextPropertyKey";
        Object carbonContextPropertyValue = "carbonContextPropertyValue";
        String userPrincipalName = "userPrincipalName";
        CarbonContextInvoker carbonContextInvoker = new CarbonContextInvoker(tenantPropertyKey, tenantPropertyValue,
                carbonContextPropertyKey, carbonContextPropertyValue, userPrincipalName);
        carbonContextInvoker.invoke();

        CarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext();
        Tenant tenant = carbonContext.getServerTenant();

        Assert.assertEquals(tenant.getProperty(tenantPropertyKey), tenantPropertyValue);
        Assert.assertEquals(carbonContext.getProperty(carbonContextPropertyKey), carbonContextPropertyValue);
        Assert.assertEquals(carbonContext.getUserPrincipal().getName(), userPrincipalName);
    }

    @Test(dependsOnMethods = "testCustomBundle")
    public void testTenantStore() throws TenantStoreException {
        String tenantPropertyKey = "tenantPropertyKey";
        String tenantPropertyValue = "tenantPropertyValue";
        Tenant tenant = new Tenant(TEST_TENANT_DOMAIN2);
        tenant.setProperty(tenantPropertyKey, tenantPropertyValue);
        InMemoryTenantStore inMemoryTenantStore = (InMemoryTenantStore) tenantStore;
        inMemoryTenantStore.addTenant(tenant);
        Tenant loadedTenant = inMemoryTenantStore.loadTenant(TEST_TENANT_DOMAIN2);
        Assert.assertEquals(loadedTenant, tenant);
        Assert.assertEquals(loadedTenant.getDomain(), TEST_TENANT_DOMAIN2);
        Assert.assertEquals(loadedTenant.getProperty(tenantPropertyKey), tenantPropertyValue);
    }

    private static void copyCarbonTenantXML() {
        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        try {
            carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "runtime", "tenant.xml");
            Files.copy(carbonYmlFilePath, Paths.get(System.getProperty("carbon.home"), "data", "tenant",
                    "tenant.xml"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to copy the tenant.xml file", e);
        }
    }
}
