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
package org.wso2.carbon.integration.tests.integration.test.servers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.HashMap;

/**
 * Prepares the Carbon server for test runs, starts the server, and stops the server after
 * test runs
 */
public class CarbonTestServerManager {

    private static final Log log = LogFactory.getLog(CarbonTestServerManager.class);
    private static TestServerManager testServerInstance = null;
    private static CarbonTestServerManager instance = new CarbonTestServerManager();
    private static boolean isServerRunning = false;

    private CarbonTestServerManager() {
        try {
            testServerInstance = new TestServerManager(new AutomationContext("CARBON", TestUserMode.SUPER_TENANT_ADMIN));
            testServerInstance.getCommands().put("-Dsetup", "");
        } catch (XPathExpressionException e) {
            log.error(e);
        }
    }

    public static void start(int portOffset) throws Exception {
        if (isServerRunning) {
            throw new Exception("Server already Running..");
        }
        deleteDatabases();
        testServerInstance.getCommands().put("-DportOffset", String.valueOf(portOffset));
        testServerInstance.startServer();
        isServerRunning = true;
    }

    public static void start(HashMap<String, String> serverPropertyMap) throws Exception {
        if (isServerRunning) {
            throw new Exception("Server already Running..");
        }
        deleteDatabases();
        testServerInstance.getCommands().clear();
        testServerInstance.getCommands().put("-Dsetup", "");
        testServerInstance.getCommands().putAll(serverPropertyMap);
        testServerInstance.startServer();
        isServerRunning = true;
    }

    public static void stop() throws AutomationFrameworkException {
        testServerInstance.stopServer();
        isServerRunning = false;
    }

    public static void reStart() throws AutomationFrameworkException {
        testServerInstance.restartGracefully();
        isServerRunning = true;
    }

    public static String getCarbonHome() {
        return testServerInstance.getCarbonHome();
    }

    public static boolean isServerRunning() {
        return isServerRunning;
    }

    private static void deleteDatabases() throws Exception {
        if (CarbonTestServerManager.getCarbonHome() == null || CarbonTestServerManager.getCarbonHome().isEmpty()) {
            return;
        }
        File dbDir = new File(CarbonTestServerManager.getCarbonHome() + File.separator + "repository"
                              + File.separator + "database");
        final File[] files = dbDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        Thread.sleep(1000);
    }

}
