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
import org.wso2.carbon.osgi.utils.OSGiTestUtils;
import org.wso2.carbon.sample.deployer.mgt.DeployerManager;
import org.wso2.carbon.sample.deployer.mgt.DeployerServicesListener;
import org.wso2.carbon.sample.runtime.mgt.RuntimeManager;
import org.wso2.carbon.sample.runtime.mgt.RuntimeServicesListener;
import org.wso2.carbon.sample.startuporder.OrderResolverMonitor;
import org.wso2.carbon.sample.transport.mgt.TransportManager;
import org.wso2.carbon.sample.transport.mgt.TransportServicesListener;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * A test strategy to test and verify the startup order resolving for both intra-component and inter-component
 * dependencies.
 * <p>
 * In here the test will verify that three different components are started in an expected order.
 * 1. Runtime-Mgt
 * 2. Deployment-Mgt
 * 3. Transport-Mgt
 * <p>
 * Runtime-Mgt declares a dependency on Deployment-Mgt that it should be started only when runtime-service is
 * registered. And Deployment-Mgt declares a dependency on Transport-Mgt that it should be started only when
 * deployment-service is registered.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SampleStartupOrderResolverOSGiTest {

    @Inject
    private RuntimeManager runtimeManager;

    @Inject
    private DeployerManager deployerManager;

    @Inject
    private TransportManager transportManager;


    @Configuration
    public Option[] createConfiguration() {
        OSGiTestUtils.setupOSGiTestEnvironment();

        Option[] options = CoreOptions.options(
                mavenBundle().artifactId("org.wso2.carbon.sample.runtime.mgt").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.runtime.mss").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.deployer.mgt").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.dbs.deployer").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.mgt").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.http").groupId(
                        "org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.order.resolver").groupId(
                        "org.wso2.carbon").versionAsInProject()
        );

        return OSGiTestUtils.getDefaultPaxOptions(options);
    }

    @Test
    public void testSampleStartupOrderResolving() {
        Assert.assertNotNull(deployerManager, "DeployerManager Service cannot be null");
        Assert.assertNotNull(transportManager, "TransportManager Service cannot be null");
        Assert.assertNotNull(runtimeManager, "RuntimeManager Service cannot be null");
        OrderResolverMonitor orderResolverMonitor = OrderResolverMonitor.getInstance();
        Assert.assertNotNull(orderResolverMonitor, "Order Resolver Monitor instance cannot be null");

        int runtimeListenerInvocation = orderResolverMonitor.
                getListenerInvocationOrder(RuntimeServicesListener.class.getName());
        Assert.assertEquals(runtimeListenerInvocation, 1);
        int deploymentListenerInvocation = orderResolverMonitor.
                getListenerInvocationOrder(DeployerServicesListener.class.getName());
        Assert.assertEquals(deploymentListenerInvocation, 2);
        int transportListenerInvocation = orderResolverMonitor.
                getListenerInvocationOrder(TransportServicesListener.class.getName());
        Assert.assertEquals(transportListenerInvocation, 3);

        orderResolverMonitor.clearInvocationCounter();
    }
}
