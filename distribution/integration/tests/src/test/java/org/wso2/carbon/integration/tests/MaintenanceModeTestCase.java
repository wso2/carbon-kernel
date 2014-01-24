/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.integration.tests;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.server.admin.service.ServerAdminMBean;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * Test for maintenance mode testing
 */

public class MaintenanceModeTestCase {

    private ServerAdminMBean serverAdmin;

    @BeforeMethod(groups = {"carbon.core"},
                  description = "Initializes the JMX connection & obtains the ServerAdmin MBean")
    public void init() throws Exception {
        JMXServiceURL url =
                new JMXServiceURL("service:jmx:rmi://localhost:11111/jndi/rmi://localhost:9999/jmxrmi");
        Map<String, Object> env = new HashMap<String, Object>();
        String[] creds = {"admin", "admin"};
        env.put(JMXConnector.CREDENTIALS, creds);
        JMXConnector cc = JMXConnectorFactory.connect(url, env);
        MBeanServerConnection mbsc = cc.getMBeanServerConnection();
        ObjectName mxbeanName =
                new ObjectName("org.wso2.carbon:type=ServerAdmin");
        serverAdmin = JMX.newMXBeanProxy(mbsc, mxbeanName, ServerAdminMBean.class);
    }

    @Test(groups = {"carbon.core"},
          description = "Puts the Carbon core server into maintenance mode")
    public void startMaintenance() throws Exception {
        serverAdmin.startMaintenance();
        assertFalse(ClientConnectionUtil.isPortOpen(9443),
                    "Port 9443 should be closed in maintenance mode, but is open");
        assertFalse(ClientConnectionUtil.isPortOpen(9763),
                    "Port 9763 should be closed in maintenance mode, but is open");
    }

    @Test(groups = {"carbon.core"}, dependsOnMethods = {"startMaintenance"},
          description = "Puts a server which was in maintenance mode back to normal mode")
    public void endMaintenance() throws Exception {
        serverAdmin.endMaintenance();
        ClientConnectionUtil.waitForPort(9443);
        assertTrue(ClientConnectionUtil.isPortOpen(9443),
                "Port 9443 should be open in normal mode, but is closed");
        ClientConnectionUtil.waitForPort(9763);
        assertTrue(ClientConnectionUtil.isPortOpen(9763),
                "Port 9763 should be open in maintenance mode, but is closed");
    }
}

