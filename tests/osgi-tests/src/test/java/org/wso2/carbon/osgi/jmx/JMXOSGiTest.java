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
package org.wso2.carbon.osgi.jmx;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;
import org.wso2.carbon.osgi.test.util.container.CarbonContainerFactory;
import org.wso2.carbon.osgi.test.util.container.options.CarbonDistributionConfigurationFileReplacementOption;

import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * JMXOSGiTest class is to test the CarbonJMX.
 *
 * @since 5.1.0
 */
@Listeners(org.ops4j.pax.exam.testng.listener.PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class JMXOSGiTest {

    private static final Logger logger = LoggerFactory.getLogger(JMXOSGiTest.class);
    @Inject
    private CarbonServerInfo carbonServerInfo;

    /**
     * Replace the existing carbon.yml file with populated carbon.yml file.
     */
    private CarbonDistributionConfigurationFileReplacementOption copyCarbonYAMLOption() {
        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "jmx", "carbon.yml");

        return new CarbonDistributionConfigurationFileReplacementOption(carbonYmlFilePath,
                Paths.get("conf", "carbon.yml"));
    }

    @Configuration
    public Option[] createConfiguration() {
        List<Option> optionList = OSGiTestConfigurationUtils.getConfiguration();
        optionList.add(copyCarbonYAMLOption());
        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testMBeanRegistration() throws Exception {
        JMXCustom test = new JMXCustom();
        ObjectName mbeanName = new ObjectName("org.wso2.carbon.osgi.jmx:type=JMXCustom");
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.registerMBean(test, mbeanName);

        Assert.assertTrue(mBeanServer.isRegistered(mbeanName), "MBean is not registered");
    }
}
