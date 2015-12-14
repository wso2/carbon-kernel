/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.osgi.startupresolver;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.utils.OSGiTestUtils;
import org.wso2.carbon.sample.transport.mgt.TransportManager;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * This test case will test the dynamic capability registrations and then the listener implementation that waits for
 * the capabilities. In this test, we are testing both static and dynamic ways of registering a capability.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class DynamicCapabilityOSGiTest {

    @Inject
    TransportManager transportManager;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    /**
     * This configuration registers the capability "org.wso2.carbon.sample.transport.mgt.Transport" statically using
     * Provider-Capability header with "org.wso2.carbon.sample.transport.http" bundle and also dynamically registers
     * the same capability with the implementation of @see TransportServiceCapabilityProvider in
     * "org.wso2.carbon.sample.transport.custom" bundle.
     *
     * @return the bundle configurations that will be used for this test case.
     */
    @Configuration
    public Option[] createConfiguration() {
        OSGiTestUtils.setupOSGiTestEnvironment();

        Option[] options = CoreOptions.options(
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.mgt").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.http").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.custom").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.jms").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.order.resolver").groupId(
                        "org.wso2.carbon").versionAsInProject()
        );

        return OSGiTestUtils.getDefaultPaxOptions(options);
    }

    @Test
    public void testCoordinationWithDynamicAndStaticCapability() {
        int expectedTransportCount = 6;
        int actualTransportCount = transportManager.getTransportCount();
        Assert.assertEquals(actualTransportCount, expectedTransportCount, "Transport count is not correct");
    }
}
