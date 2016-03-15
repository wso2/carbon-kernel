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
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.utils.OSGiTestUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * JMXOSGiTest class is to test the CarbonJMX
 *
 * @since 5.1.0
 */
@Listeners(org.ops4j.pax.exam.testng.listener.PaxExam.class)
@ExamReactorStrategy(org.ops4j.pax.exam.spi.reactors.PerClass.class)
public class JMXOSGiTest {

    private static final Logger logger = LoggerFactory.getLogger(JMXOSGiTest.class);

    @Configuration
    public Option[] createConfiguration() {
        OSGiTestUtils.setupOSGiTestEnvironment();
        copyCarbonYAML();
        return OSGiTestUtils.getDefaultPaxOptions();
    }

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Test
    public void testMBeanRegistration() throws Exception {
        JMXCustom test = new JMXCustom();
        ObjectName mbeanName = new ObjectName("org.wso2.carbon.osgi.jmx:type=JMXCustom");
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.registerMBean(test, mbeanName);

        Assert.assertTrue(mBeanServer.isRegistered(mbeanName), "MBean is not registered");
    }

    /**
     * Replace the existing carbon.yml file with populated carbon.yml file.
     */
    private static void copyCarbonYAML() {
        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        try {
            carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "jmx", "carbon.yml");
            Files.copy(carbonYmlFilePath, Paths.get(System.getProperty("carbon.home"), "conf", "carbon.yml"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to copy the carbon.yml file", e);
        }
    }
}
