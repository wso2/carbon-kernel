/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.kernel.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.transports.TransportManager;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Unit test class for org.wso2.carbon.kernel.utils.MBeanRegistrator.
 *
 * @since 5.1.0
 */
public class MBeanRegistratorTest {

    private static Log log = LogFactory.getLog(MBeanRegistratorTest.class);
    private static int initialMBeanCount;

    @Test()
    public void testRegisterMBean() {

        MBeanServer mBeanServer = MBeanManagementFactory.getMBeanServer();
        initialMBeanCount = mBeanServer.getMBeanCount();

        MBeanRegistrator.registerMBean(new TransportManager());
        Assert.assertTrue(mBeanServer.getMBeanCount() == initialMBeanCount + 1);

        String className = new TransportManager().getClass().getName();
        if (className.indexOf('.') != -1) {
            className = className.substring(className.lastIndexOf('.') + 1);
        }

        String objectName = Constants.SERVER_PACKAGE + ":type=" + className;
        try {
            Assert.assertNotNull(mBeanServer.getMBeanInfo(new ObjectName(objectName)));
        } catch (MalformedObjectNameException | InstanceNotFoundException | IntrospectionException |
                ReflectionException e) {
            log.error("Error when retrieving mBean Inforation", e);
        }
    }

    @Test(dependsOnMethods = {"testRegisterMBean"}, expectedExceptions = RuntimeException.class)
    public void testMBeanAlreadyExists() throws RuntimeException {
        MBeanRegistrator.registerMBean(new TransportManager());
    }

    @Test(dependsOnMethods = {"testMBeanAlreadyExists"}, expectedExceptions = RuntimeException.class)
    public void testMBeanNotCompliant() throws RuntimeException {
        MBeanRegistrator.registerMBean(new CarbonServerInfo());
    }

    @Test(dependsOnMethods = {"testMBeanNotCompliant"})
    public void testUnregisterAllMBeans() {
        MBeanServer mBeanServer = MBeanManagementFactory.getMBeanServer();
        MBeanRegistrator.unregisterAllMBeans();
        Assert.assertTrue(mBeanServer.getMBeanCount() == initialMBeanCount);
    }
}
