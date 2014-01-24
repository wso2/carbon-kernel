/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;

import java.io.IOException;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for gracefully & forcefully shutting down Carbon servers
 */
public class ServerShutdownTestCase {
    private static final Log log = LogFactory.getLog(ServerShutdownTestCase.class);

    @Test(groups = {"carbon.core.graceful.shutdown.test"})
    public void testGracefulServerShutdown() throws java.lang.Exception {
        log.info("Testing graceful server shutdown...");
        int portOffset = 35;
        startServerForShutdownTest(portOffset);
        int httpsPort = 9443 + portOffset;
        ClientConnectionUtil.waitForPort(httpsPort);
        ServerAdminClient serverAdmin = LoginLogoutUtil.getServerAdminClient(portOffset);
        serverAdmin.shutdownGracefully();
        Thread.sleep(5000);
        assertFalse(ClientConnectionUtil.isPortOpen(httpsPort),
                "Port " + httpsPort + " shouldn't be open when the server is gracefully shutting down");
        int httpPort = 9763 + portOffset;
        assertFalse(ClientConnectionUtil.isPortOpen(httpPort),
                "Port " + httpPort+ " shouldn't be open when the server is gracefully shutting down");
    }

    @Test(groups = {"carbon.core.shutdown.test"})
    public void testServerShutdown() throws java.lang.Exception {
        log.info("Testing server shutdown...");
        int portOffset = 37;
        startServerForShutdownTest(portOffset);
        int httpsPort = 9443 + portOffset;
        ClientConnectionUtil.waitForPort(httpsPort);
        ServerAdminClient serverAdmin = LoginLogoutUtil.getServerAdminClient(portOffset);
        assertTrue(serverAdmin.shutdown());
        Thread.sleep(5000);
        assertFalse(ClientConnectionUtil.isPortOpen(httpsPort),
                "Port " + httpsPort +" shouldn't be open when the server is shutting down");
        int httpPort = 9763 + portOffset;
        assertFalse(ClientConnectionUtil.isPortOpen(httpPort),
                "Port " + httpPort + " shouldn't be open when the server is shutting down");
    }

    private void startServerForShutdownTest(int portOffset) throws IOException {
        CarbonTestServerManager server = new CarbonTestServerManager(portOffset);
        server.startServer();
    }
}
