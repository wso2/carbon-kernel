/*
* Copyright 2015 WSO2, Inc. http://www.wso2.org
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.osgi;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantRuntime;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TenantRuntimeOSGiTest {

    private static final String CARBON_RUNTIME_SERVICE = CarbonRuntime.class.getName();
    private CarbonRuntime carbonRuntime = null;

    @Inject
    private BundleContext bundleContext;

    //TODO - validate the best practise
    @Inject
    private CarbonRuntime carbonRuntime22;


    @Test
    public void testTenantRuntimeServiceStatus() {

        //get the service reference and check it's available
        ServiceReference reference = bundleContext.getServiceReference(CARBON_RUNTIME_SERVICE);
        Assert.assertNotNull(reference, "Carbon Runtime Service Reference is null");

        //get carbon runtime service and check it's available
        CarbonRuntime carbonRuntime = (CarbonRuntime) bundleContext.getService(reference);
        Assert.assertNotNull(carbonRuntime, "Carbon Runtime Service is null");

        //check tenant runtime service from carbon runtime and check it's available
        TenantRuntime tenantRuntime = carbonRuntime.getTenantRuntime();
        Assert.assertNotNull(tenantRuntime, "Tenant Runtime is null");

    }

    @Test(dependsOnMethods = { "testTenantRuntimeServiceStatus" })
    public void testCreateTenant() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        //check creation of a tenant and availability after creation
        Tenant tenant = getTenantRuntimeReference().addTenant("test.com", "test", "sample tenant", "testAdmin",
                "testadmin@test.com", map);
        Assert.assertNotNull(tenant, "Tenant test.com creation failure");
    }

    @Test(dependsOnMethods = { "testCreateTenant" })
    public void testGetTenant() throws Exception {

        //check the availability of a created tenant
        Tenant tenant = getTenantRuntimeReference().getTenant("test.com");
        Assert.assertNotNull(tenant, "Tenant test.com error getting tenant");

        Assert.assertEquals(tenant.getAdminUsername(), "testAdmin", "Tenant admin user name not found");
        Assert.assertEquals(tenant.getProperties().get("key"), "value", "Tenant property value not found");
        Assert.assertEquals(tenant.getProperty("key"), "value", "Tenant property value not found");
    }

    @Test(dependsOnMethods = { "testGetTenant" })
    public void testDeleteTenant() throws Exception {

        //TODO - At the moment tenant deletion is not implemented
        //check deletion of a tenant
        //getTenantRuntimeReference().deleteTenant("test.com");

        //Assert.assertNull(getTenantRuntimeReference().getTenant("test.com"), "Tenant test.com error deleting tenant");

    }

    /**
     * @return tenant runtime service reference
     */
    private TenantRuntime getTenantRuntimeReference() {
        ServiceReference reference = bundleContext.getServiceReference(CARBON_RUNTIME_SERVICE);
        CarbonRuntime carbonRuntime = (CarbonRuntime) bundleContext.getService(reference);
        return carbonRuntime.getTenantRuntime();
    }
}
