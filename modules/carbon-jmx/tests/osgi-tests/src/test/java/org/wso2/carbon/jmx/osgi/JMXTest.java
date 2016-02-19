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
package org.wso2.carbon.jmx.osgi;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JMXTest {
    @Test
    public void testMBeanRegistration() throws Exception {
        JMXSample test = new JMXSample();
        ObjectName mbeanName = new ObjectName("org.wso2.carbon.osgi.jmx:type=JMXSample");
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.registerMBean(test, mbeanName);

        Assert.assertTrue(mBeanServer.isRegistered(mbeanName), "MBean is not registered");
    }

    @Test(dependsOnMethods = {"testMBeanRegistration"})
    public void testAccessMBean() throws Exception {

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:7700/jndi/rmi://localhost:7800/jmxrmi");
        Map<String, Object> environment = new HashMap<>();
        String[] credentials = {"admin", "password"};
        environment.put(JMXConnector.CREDENTIALS, credentials);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, environment);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

        ObjectName mbeanName = new ObjectName("org.wso2.carbon.osgi.jmx:type=JMXSample");
        JMXSampleMBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, JMXSampleMBean.class, true);

        Assert.assertEquals(mbeanProxy.getCount(), 0, "Count is not zero");

        mbeanProxy.setCount(500);
        Assert.assertEquals(mbeanProxy.getCount(), 500, "Count is not 500");

        mbeanProxy.reset();
        Assert.assertEquals(mbeanProxy.getCount(), 0, "Count is not reset");
    }
}
