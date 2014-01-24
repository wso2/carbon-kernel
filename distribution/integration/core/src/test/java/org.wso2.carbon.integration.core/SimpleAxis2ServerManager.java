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
package org.wso2.carbon.integration.core;

import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.utils.ServerConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class SimpleAxis2ServerManager {

    private static Process process;
    private static Thread runnable;
    private static String originalUserDir = null;

    private final static String SERVER_STARTUP_MESSAGE = "[SimpleAxisServer] Starting";
    private final static String SERVER_SHUTDOWN_MESSAGE = "Shutting down SimpleAxisServer";
    private final static long DEFAULT_START_STOP_WAIT_MS = 1000 * 60 * 4;


    public synchronized static void startServer()
            throws ServerConfigurationException {
        if (process != null) { // An instance of the server is running
            return;
        }
        Process tempProcess;
        try {
            String carbonHome = FrameworkSettings.CARBON_HOME;
            System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
            originalUserDir = System.getProperty("user.dir");
            System.setProperty("user.dir", carbonHome);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            String temp;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                tempProcess = Runtime.getRuntime().exec(new String[]{"bat", "samples/axis2Server/axis2server.bat"},
                        null, new File(carbonHome));
            } else {
                tempProcess = Runtime.getRuntime().exec(new String[]{"sh", "samples/axis2Server/axis2server.sh", "test"},
                        null, new File(carbonHome));
            }
             Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        shutdown();
                    } catch (Exception ignored) {

                    }
                }
            });
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(tempProcess.getInputStream()));
            long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
            while ((temp = reader.readLine()) != null && System.currentTimeMillis() < time) {
                System.out.println(temp);
                if (temp.contains(SERVER_STARTUP_MESSAGE)) {
                    runnable = new Thread() {
                        public void run() {
                            try {
                                String temp;
                                while ((temp = reader.readLine()) != null) {
                                    System.out.println(temp);
                                }
                            } catch (Exception ignore) {

                            }
                        }
                    };
                    runnable.start();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to start server", e);
        }
        process = tempProcess;
        System.out.println("Successfully started Axis2Server server. Returning...");
    }

    public synchronized static void shutdown() throws Exception {
        
        if (process != null) {
            try {
                String temp;
                process.destroy();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
                while ((temp = reader.readLine()) != null && System.currentTimeMillis() < time) {
                    if (temp.contains(SERVER_SHUTDOWN_MESSAGE)) {
                        break;
                    }
                }

            } catch (IOException ignored) {
            }
            try {
                runnable.interrupt();
            } catch (Exception ignored) {
            }
            runnable = null;
            process = null;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            System.clearProperty(ServerConstants.CARBON_HOME);
            System.setProperty("user.dir", originalUserDir);
        }
    }
}
