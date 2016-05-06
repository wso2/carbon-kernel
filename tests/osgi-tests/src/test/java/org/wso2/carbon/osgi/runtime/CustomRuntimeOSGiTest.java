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
package org.wso2.carbon.osgi.runtime;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;
import org.wso2.carbon.sample.runtime.mgt.RuntimeManager;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * A test strategy to test and verify the custom pluggable Runtime's.
 * <p>
 * In here the test will verify that multiple(2) different runtimes get registered on carbon server.
 * <p>
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CustomRuntimeOSGiTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    private RuntimeManager runtimeManager;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = new ArrayList<>();
        optionList.add(mavenBundle().artifactId("org.wso2.carbon.sample.runtime.mgt").groupId("org.wso2.carbon")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.wso2.carbon.sample.runtime.mss").groupId("org.wso2.carbon")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.wso2.carbon.sample.runtime.jar").groupId("org.wso2.carbon")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.wso2.carbon.sample.order.resolver").groupId("org.wso2.carbon")
                .versionAsInProject());

        optionList = OSGiTestConfigurationUtils.getConfiguration(optionList, null);
        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testSampleRuntimes() {
        // evaluating number of runtime's registered
        Assert.assertEquals(runtimeManager.getRuntimeCount(), 2);
    }

}
