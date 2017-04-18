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
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.sample.transport.mgt.TransportManager;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

/**
 * This test case will test the dynamic capability registrations and then the listener implementation that waits for
 * the capabilities.
 *
 * @since 5.2.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class PendingCapabilityDelayedOSGiTest {

    @Inject
    private BundleContext bundleContext;

    /**
     * This configuration registers Runtime, Deployer and Transport Capabilities.
     * org.wso2.carbon.sample.transport.custom3.CustomTransportServiceCapabilityProvider registers three instances of
     * org.wso2.carbon.sample.transport.custom3.Custom3Transport with two seconds delay. Hence the server startup
     * should be delayed until all the Transports are available.
     *
     * @return the bundle configurations that will be used for this test case.
     */
    @Configuration
    public Option[] createConfiguration() {
        return new Option[] {
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.transport.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(
                        maven().artifactId("org.wso2.carbon.sample.transport.file").groupId("org.wso2.carbon")
                                .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.runtime.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.runtime.mss").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.deployer.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.dbs.deployer").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.order.resolver").groupId("org.wso2.carbon")
                        .versionAsInProject())
        };
    }

    /**
     * This test will wait 10 seconds until the TransportManager server is available. Ideally the TransportManager
     * should be available after four seconds.
     */
    @Test
    public void testDelayedPendingCapabilityRegistration() {
        ServiceReference reference = null;
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {

            }
            reference = bundleContext.getServiceReference(TransportManager.class);
            if (reference != null) {
                break;
            }
        }
        Assert.assertNotNull(reference, "Service reference of TransportManager should not be null");
    }
}
