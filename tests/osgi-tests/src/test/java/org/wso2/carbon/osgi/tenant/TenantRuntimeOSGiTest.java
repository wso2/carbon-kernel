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
package org.wso2.carbon.osgi.tenant;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.test.CarbonContextInvoker;
import org.wso2.carbon.context.test.InMemoryTenantStore;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.context.CarbonContext;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantRuntime;
import org.wso2.carbon.kernel.tenant.TenantStore;
import org.wso2.carbon.kernel.tenant.exception.TenantStoreException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.utils.OSGiTestUtils;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Test case to test tenant runtime operations and custom tenant store implementation.
 *
 * @since 5.1.0
 */
public class TenantRuntimeOSGiTest {

    private static final String TEST_TENANT_DOMAIN1 = "test.tenant.domain";
    private static final String TEST_TENANT_DOMAIN2 = "test.tenant.domain.2";

    @Inject
    private TenantStore<Tenant> tenantStore;

    @Inject
    private CarbonRuntime carbonRuntime;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        OSGiTestUtils.setupOSGiTestEnvironment();
        OSGiTestUtils.copyCarbonYAML();
        OSGiTestUtils.copyCarbonTenantXML();
        Option[] options = CoreOptions.options(
                mavenBundle().artifactId("carbon-context-test-artifact").groupId(
                        "org.wso2.carbon").versionAsInProject()
        );
        return OSGiTestUtils.getDefaultPaxOptions(options);
    }

    @Test
    public void testTenantRuntime() {
        Assert.assertNotNull(carbonRuntime.getTenantRuntime());
        TenantRuntime tenantRuntime = carbonRuntime.getTenantRuntime();
        Assert.assertEquals(tenantRuntime.getLoadedTenants().size(), 0);
    }

    @Test(dependsOnMethods = "testTenantRuntime")
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

    @Test(dependsOnMethods = "testTenantRuntime")
    public void testTenantStore1() throws TenantStoreException {
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
        inMemoryTenantStore.deleteTenant(TEST_TENANT_DOMAIN2);
        Assert.assertEquals(inMemoryTenantStore.loadTenant(TEST_TENANT_DOMAIN2), null);
    }


    @Test(dependsOnMethods = "testTenantStore1")
    public void testTenantStore2() throws TenantStoreException {
        try {
            String tenantPropertyKey = "tenantPropertyKey";
            String tenantPropertyValue = "tenantPropertyValue";
            Tenant tenant = new Tenant(TEST_TENANT_DOMAIN2);
            tenant.setProperty(tenantPropertyKey, tenantPropertyValue);
            InMemoryTenantStore inMemoryTenantStore = (InMemoryTenantStore) tenantStore;
            inMemoryTenantStore.addTenant(tenant);

            PrivilegedCarbonContext.destroyCurrentContext();
            System.setProperty(Constants.TENANT_DOMAIN, TEST_TENANT_DOMAIN2);
            Tenant currentTenant = PrivilegedCarbonContext.getCurrentContext().getServerTenant();
            Assert.assertEquals(currentTenant.getDomain(), TEST_TENANT_DOMAIN2);
            Assert.assertEquals(currentTenant.getProperty(tenantPropertyKey), tenantPropertyValue);
        } finally {
            System.clearProperty(Constants.TENANT_DOMAIN);
        }
    }
}
