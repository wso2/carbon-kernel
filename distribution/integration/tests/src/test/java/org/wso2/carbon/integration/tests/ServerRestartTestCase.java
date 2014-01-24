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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;

import java.io.IOException;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for gracefully & forcefully restarting Carbon servers
 */
public class ServerRestartTestCase {

    private static final Log log = LogFactory.getLog(ServerShutdownTestCase.class);
    private static final long TIMEOUT = 5 * 60000;

    @Test(groups = {"carbon.core.graceful.restart.test"})
    public void testGracefulServerRestart() throws java.lang.Exception {
        log.info("Testing server graceful restart...");
        int portOffset = 121;
        CarbonTestServerManager serverManager = startServerForRestartTest(portOffset);
        try {
            int httpsPort = 9443 + portOffset;
            ClientConnectionUtil.waitForPort(httpsPort);
            ServerAdminClient serverAdmin = LoginLogoutUtil.getServerAdminClient(portOffset);
            assertTrue(serverAdmin.restartGracefully());
            Thread.sleep(5000); //This sleep should be there, since we have to give some time for 
                                //the server to initiate restart. Otherwise, "waitForPort" call 
                                //might happen before server initiate restart.
            ClientConnectionUtil.waitForPort(httpsPort, TIMEOUT, true);
            Thread.sleep(5000);
        } finally {
            if (serverManager != null) {
                serverManager.stopServer();
            }
        }
    }

    @Test(groups = {"carbon.core.restart.test"})
    public void testServerRestart() throws java.lang.Exception {
        log.info("Testing server restart...");
        int portOffset = 122;
        CarbonTestServerManager serverManager = startServerForRestartTest(portOffset);
        try {
            int httpsPort = 9443 + portOffset;
            ClientConnectionUtil.waitForPort(httpsPort);
            ServerAdminClient serverAdmin = LoginLogoutUtil.getServerAdminClient(portOffset);
            assertTrue(serverAdmin.restart());
            Thread.sleep(5000);
            ClientConnectionUtil.waitForPort(httpsPort, TIMEOUT, true);
            Thread.sleep(5000);
        } finally {
            if (serverManager != null) {
                serverManager.stopServer();
            }
        }
    }

    private CarbonTestServerManager startServerForRestartTest(int portOffset) throws IOException {
        CarbonTestServerManager server = new CarbonTestServerManager(portOffset);
        server.startServer();
        return server;
    }
}
