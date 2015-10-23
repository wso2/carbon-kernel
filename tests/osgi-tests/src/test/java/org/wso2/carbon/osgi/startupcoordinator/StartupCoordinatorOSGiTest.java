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
package org.wso2.carbon.osgi.startupcoordinator;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.osgi.util.Utils;
import org.wso2.carbon.sample.deployer.mgt.DeployerManager;
import org.wso2.carbon.sample.runtime.mgt.RuntimeManager;
import org.wso2.carbon.sample.transport.mgt.TransportManager;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * zero provide-capability
 * one provide-capability
 * multiple provide-capability.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class StartupCoordinatorOSGiTest {
    private static final Logger logger = LoggerFactory.getLogger(StartupCoordinatorOSGiTest.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private DeployerManager deployerManager;

    @Configuration
    public Option[] createConfiguration() {
        Utils.setCarbonHome();
        Utils.setupMavenLocalRepo();

        Option[] options = CoreOptions.options(
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.mgt").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.http").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.deployer.mgt").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.runtime.mgt").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.runtime.mss").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.runtime.jar").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.runtime.bps").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.runtime.webapp").groupId(
                        "org.wso2.carbon").versionAsInProject()
        );

        return Utils.getDefaultPaxOptions(options);
    }

    @Test
    public void testCoordinationWithZeroServices() {
        Assert.assertNotNull(deployerManager, "DeployerManager Service is null");

        int expectedTransportCount = 0;
        int actualTransportCount = deployerManager.getDeployerCount();
        Assert.assertEquals(actualTransportCount, expectedTransportCount, "Deployer count is not correct");
    }

    @Test
    public void testCoordinationWithOneService() {
        ServiceReference<TransportManager> reference = bundleContext.getServiceReference(TransportManager.class);
        Assert.assertNotNull(reference, "TransportManager Service Reference is null");

        TransportManager transportManager = bundleContext.getService(reference);
        Assert.assertNotNull(transportManager, "TransportManager Service is null");

        int expectedTransportCount = 1;
        int actualTransportCount = transportManager.getTransportCount();
        Assert.assertEquals(actualTransportCount, expectedTransportCount, "Transport count is not correct");
    }

    @Test
    public void testCoordinationWithMultipleService() {
        ServiceReference<RuntimeManager> reference = bundleContext.getServiceReference(RuntimeManager.class);
        Assert.assertNotNull(reference, "RuntimeManager Service Reference is null");

        RuntimeManager runtimeManager = bundleContext.getService(reference);
        Assert.assertNotNull(runtimeManager, "RuntimeManager Service is null");

        int expectedTransportCount = 4;
        int actualTransportCount = runtimeManager.getRuntimeCount();
        Assert.assertEquals(actualTransportCount, expectedTransportCount, "Runtime count is not correct");
    }
}
