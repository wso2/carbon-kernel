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
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.sample.deployer.mgt.DeployerManager;
import org.wso2.carbon.sample.runtime.mgt.RuntimeManager;
import org.wso2.carbon.sample.transport.mgt.TransportManager;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

/**
 * zero provide-capability
 * one provide-capability
 * multiple provide-capability.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class StartupCoordinatorOSGiTest {

    @Inject
    private RuntimeManager runtimeManager;

    @Inject
    private DeployerManager deployerManager;

    @Inject
    private TransportManager transportManager;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[] {
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.runtime.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.runtime.mss").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.runtime.jar").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.runtime.bps").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.runtime.webapp").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.runtime.custom").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.deployer.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.dbs.deployer").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.transport.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.transport.http").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.transport.jms").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven().artifactId("org.wso2.carbon.sample.order.resolver").groupId("org.wso2.carbon")
                        .versionAsInProject())
        };
    }

    @Test
    public void testCoordinationWithZeroServices() {
        Assert.assertNotNull(deployerManager, "DeployerManager Service is null");

        int expectedDeployerCount = 1;
        int actualDeployerCount = deployerManager.getDeployerCount();
        Assert.assertEquals(actualDeployerCount, expectedDeployerCount, "Deployer count is not correct");
    }

    @Test
    public void testCoordinationWithOneService() {
        Assert.assertNotNull(transportManager, "TransportManager Service is null");

        int expectedTransportCount = 3;
        int actualTransportCount = transportManager.getTransportCount();
        Assert.assertEquals(actualTransportCount, expectedTransportCount, "Transport count is not correct");
    }

    @Test
    public void testCoordinationWithMultipleService() {
        Assert.assertNotNull(runtimeManager, "RuntimeManager Service is null");

        int expectedRuntimeCount = 7;
        int actualRuntimeCount = runtimeManager.getRuntimeCount();
        Assert.assertEquals(actualRuntimeCount, expectedRuntimeCount, "Runtime count is not correct");
    }
}
