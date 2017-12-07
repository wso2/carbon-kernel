/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.osgi.startupresolver;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.sample.transport.mgt.TransportManager;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

/**
 * This test case will test the skipping of service tracking in the Startup Order Resolver.
 *
 * @since 5.2.5
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SkipServiceTrackingOSGiTest {

    @Inject
    TransportManager transportManager;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    /**
     * This configuration registers the capability "org.wso2.carbon.sample.transport.mgt.Transport" statically using
     * Provider-Capability header with "org.wso2.carbon.sample.transport.http" bundle and also dynamically registers the
     * same capability with the implementation of @see TransportServiceCapabilityProvider in
     * "org.wso2.carbon.sample.transport.custom" bundle.
     *
     * @return the bundle configurations that will be used for this test case.
     */
    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.transport.mgt").groupId("org.wso2.carbon")
                                          .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.transport.ftp").groupId("org.wso2.carbon")
                                          .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.order.resolver").groupId("org.wso2.carbon")
                                          .versionAsInProject())
        };
    }

    @Test
    public void testSkipServiceTracking() {
        int expectedTransportCount = 2;
        int actualTransportCount = transportManager.getTransportCount();
        Assert.assertEquals(actualTransportCount, expectedTransportCount, "Transport count is not correct.");
    }
}
