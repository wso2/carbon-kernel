/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.tests.carbontools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.telnet.TelnetClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.Assert.assertNotEquals;

/**
 * This class is to test -DosgiConsole command by checking the active components
 */
public class OSGICommandDosgiConsoleTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(OSGICommandDosgiConsoleTestCase.class);
    private static int telnetPort = 2000;
    private TelnetClient telnet = new TelnetClient();
    private ArrayList<String> arrList = new ArrayList<String>();
    private ArrayList<String> activeList = new ArrayList<String>();
    private HashMap<String, String> serverPropertyMap = new HashMap<String, String>();
    private MultipleServersManager manager = new MultipleServersManager();
    private PrintStream out;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        // to start the server from a different port offset
        serverPropertyMap.put("-DportOffset", "1");
        // start with OSGI component service
        serverPropertyMap.put("-DosgiConsole", Integer.toString(telnetPort));
        AutomationContext autoCtx = new AutomationContext();
        CarbonTestServerManager server = new CarbonTestServerManager(
                autoCtx, System.getProperty("carbon.zip"), serverPropertyMap);
        manager.startServers(server);
    }

    @AfterClass(alwaysRun = true)
    public void stopServers() throws Exception {
        disconnect();  // telnet disconnection
        manager.stopAllServers();
    }

    @Test(groups = "wso2.all", description = "Identifying and storing active OSGI components")
    public void testOSGIActiveComponents() throws Exception {
        telnet.connect(InetAddress.getLocalHost().getHostAddress(), telnetPort);
        telnet.setSoTimeout(10000);
        ArrayList<String> arr = retrieveActiveComponentsList("ls");
        for (int x = 0; x < arr.size(); x++) {
            activeList.add(arrList.get(x).split("\t")[3]);
        }
        assertNotEquals(activeList.size(), 0, "Active components not detected in server startup.");
    }

    private ArrayList<String> retrieveActiveComponentsList(String command) throws IOException {
        writeInputCommand(command);
        try {
            readResponseToFindActiveComponents();
        } catch (SocketTimeoutException e) {
            log.error("Socket timeout Exception " + e);
        }
        return arrList;
    }

    private void writeInputCommand(String value) throws UnsupportedEncodingException {
        out = new PrintStream(telnet.getOutputStream(), true, "UTF-8");
        out.println(value);
        out.flush();
        log.info(value);
    }

    private void readResponseToFindActiveComponents() throws IOException {
        InputStream in = telnet.getInputStream();
        BufferedReader inBuff = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String inputLine;
        while ((inputLine = inBuff.readLine()) != null) {
            if (inputLine.contains("Active")) {  // filtering active components
                arrList.add(inputLine);
                log.info(inputLine);
                break;
            }
        }
        inBuff.close();
        out.close();
    }

    private void disconnect() {
        try {
            telnet.disconnect();
        } catch (IOException e) {
            log.error("Error occurred while telnet disconnection " + e);
        }
    }
}
