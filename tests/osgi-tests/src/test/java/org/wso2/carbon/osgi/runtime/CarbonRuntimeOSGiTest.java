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
package org.wso2.carbon.osgi.runtime;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.internal.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.CarbonRuntime;

import javax.inject.Inject;

/**
 * CarbonRuntimeOSGiTest class is to test the availability and the functionality of the Carbon Runtime Service
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CarbonRuntimeOSGiTest {

    private static final String CARBON_RUNTIME_SERVICE = CarbonRuntime.class.getName();

    @Inject
    private BundleContext bundleContext;

    @Test
    public void testCarbonRuntimeService() {

        ServiceReference reference = bundleContext.getServiceReference(CARBON_RUNTIME_SERVICE);
        Assert.assertNotNull(reference, "Carbon Runtime Service Reference is null");

        CarbonRuntime carbonRuntime = (CarbonRuntime) bundleContext.getService(reference);
        Assert.assertNotNull(carbonRuntime, "Carbon Runtime Service is null");

        CarbonConfiguration carbonConfiguration = carbonRuntime.getConfiguration();
        Assert.assertNotNull(carbonConfiguration, "Carbon Configuration is null");
    }

    @Test (dependsOnMethods = { "testCarbonRuntimeService" })
    public void testCarbonConfiguration(){

        //TODO - write
//        getCarbonConfiguration().
    }

    /**
     * @return Carbon Configuration reference
     */
    private CarbonConfiguration getCarbonConfiguration() {
        ServiceReference reference = bundleContext.getServiceReference(CARBON_RUNTIME_SERVICE);
        CarbonRuntime carbonRuntime = (CarbonRuntime) bundleContext.getService(reference);
        return carbonRuntime.getConfiguration();
    }
}
