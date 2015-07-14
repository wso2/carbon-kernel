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
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;
import org.wso2.carbon.integration.tests.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationConstants;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.Assert.assertNotEquals;

/**
* Test -DosgiConsole command by checking the active components
*/
public class OSGICommandDosgiConsoleTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(OSGICommandDosgiConsoleTestCase.class);
    private int telnetPort = 2000;
    private TelnetClient telnet = new TelnetClient();
    private ArrayList<String> arrList = new ArrayList<String>();
    private ArrayList<String> activeList = new ArrayList<String>();
    private HashMap<String, String> serverPropertyMap = new HashMap<String, String>();
    private PrintStream out;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        // to start the server from a different port offset
        int portOffset = 1;
        serverPropertyMap.put("-DportOffset", Integer.toString(portOffset));
        // start with OSGI component service
        serverPropertyMap.put("-DosgiConsole", Integer.toString(telnetPort));

        if (!CarbonTestServerManager.isServerRunning()) {
            CarbonTestServerManager.start(serverPropertyMap);
        } else {
            CarbonTestServerManager.stop();
            CarbonTestServerManager.start(serverPropertyMap);
        }
    }

    @Test(groups = "carbon.core", description = "Identifying active OSGI components")
    public void testOSGIActiveComponents()
            throws CarbonToolsIntegrationTestException, IOException {

        telnet.connect(InetAddress.getLocalHost().getHostAddress(), telnetPort);
        telnet.setSoTimeout((int) CarbonIntegrationConstants.DEFAULT_WAIT_MS);
        ArrayList<String> arr = retrieveActiveComponentsList("ls");//run ls command to list bundles
        for (int x = 0; x < arr.size(); x++) {
            activeList.add(arrList.get(x).split("\t")[3]);
        }
        assertNotEquals(activeList.size(), 0, "Active components not detected in server startup.");
    }

    private ArrayList<String> retrieveActiveComponentsList(String command)
            throws CarbonToolsIntegrationTestException {
        writeInputCommand(command);
        readResponseToFindActiveComponents();
        return arrList;
    }

    private void writeInputCommand(String value) throws CarbonToolsIntegrationTestException {
        try {
            out = new PrintStream(telnet.getOutputStream(), true, "UTF-8");
            out.println(value);
        } catch (UnsupportedEncodingException ex) {
            log.error("Unsupported encoding UTF-8", ex);
            throw new CarbonToolsIntegrationTestException("Unsupported encoding UTF-8 ", ex);
        } finally {
            if (out != null) {
                out.flush();
            }
        }
        log.info(value);
    }

    private void readResponseToFindActiveComponents() throws CarbonToolsIntegrationTestException {
        InputStream in = telnet.getInputStream();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                if (inputLine.contains("Active")) {  // filtering active components
                    arrList.add(inputLine);
                    log.info(inputLine);
                    break;
                }
            }

        } catch (IOException ex) {
            log.error("Error while reading input stream ", ex);
            throw new CarbonToolsIntegrationTestException("Error while reading input stream ", ex);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.warn("Error when closing BufferedReader  ", e);
                }
            }
        }
    }

    private void disconnect() {
        try {
            telnet.disconnect();
        } catch (IOException e) {
            log.error("Error occurred while telnet disconnection " + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void stopServers() throws Exception {
        try {
            if (out != null) {
                out.close();
            }
            disconnect();  // telnet disconnection
        }finally {
            CarbonTestServerManager.stop();
        }

    }
}
