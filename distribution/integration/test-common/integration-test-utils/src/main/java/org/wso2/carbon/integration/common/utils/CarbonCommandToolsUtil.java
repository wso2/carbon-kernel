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

package org.wso2.carbon.integration.common.utils;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.ExtensionConstants;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.extensions.servers.utils.InputStreamHandler;
import org.wso2.carbon.automation.extensions.servers.utils.ServerLogReader;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class CarbonCommandToolsUtil {

    /**
     * This class has the method which using by carbon tools test cases
     */

    private static final Log log = LogFactory.getLog(CarbonCommandToolsUtil.class);
    private static int TIMEOUT = 180 * 1000; // Max time to wait
    private static String carbonHome = null;
    private static final long DEFAULT_START_STOP_WAIT_MS = 1000 * 60 * 5;
    private static ServerLogReader inputStreamHandler;
    private static final String SERVER_STARTUP_MESSAGE = "Mgt Console URL";

    /**
     * This method is to execute commands and reading the logs to find the expected string.
     *
     * @param directory      - Directory which has the file to be executed .
     * @param cmdArray       - Command array to be executed.
     * @param expectedString - Expected string in  the log.
     * @return boolean - true : Found the expected string , false : not found the expected string.
     * @throws java.io.IOException - Error while getting the command directory
     */
    public static boolean isScriptRunSuccessfully(String directory, String[] cmdArray,
                                                  String expectedString) throws IOException {
        boolean isFoundTheMessage = false;
        BufferedReader br = null;
        Process process = null;
        try {
            File commandDir = new File(directory);
            process = Runtime.getRuntime().exec(cmdArray, null, commandDir);
            String line;
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < TIMEOUT) {
                br = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                while ((line = br.readLine()) != null) {
                    log.info(line);
                    if (line.contains(expectedString)) {
                        log.info("found the string " + expectedString + " in line " + line);
                        isFoundTheMessage = true;
                        break;
                    }
                }
                if (isFoundTheMessage) {
                    break;
                }
            }
            return isFoundTheMessage;
        } catch (IOException ex) {
            log.error("Error when reading the InputStream when running shell script  " +
                      ex.getMessage(), ex);
            throw new IOException("Error when reading the InputStream when running shell script "
                                  + ex.getMessage(), ex);
        } finally {
            if (br != null) {
                br.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * This method is to execute commands and return the Process
     *
     * @param directory - Directory which has the file to be executed .
     * @param cmdArray  - Command array to be executed
     * @return Process - executed process
     * @throws java.io.IOException - Error while getting the execution directory
     */
    public static Process runScript(String directory, String[] cmdArray)
            throws IOException {

        Process process;
        try {
            File commandDir = new File(directory);
            process = Runtime.getRuntime().exec(cmdArray, null, commandDir);
            return process;
        } catch (IOException ex) {
            log.error("Error when reading the InputStream when running shell script " +
                      ex.getMessage(), ex);
            throw new IOException("Error when reading the InputStream when running shell script "
                                  + ex.getMessage(), ex);
        }
    }


    /**
     * This method to find multiple strings in same line in log
     *
     * @param stringArrayToFind - String array to find all the elements
     * @return boolean - true if found all the strings , false if not
     * @throws InterruptedException
     */
    public static boolean findMultipleStringsInLog(String[] stringArrayToFind)
            throws InterruptedException {
        boolean expectedStringFound = false;

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < TIMEOUT) {
            String message = inputStreamHandler.getOutput();
            for (String stringToFind : stringArrayToFind) {
                if (message.contains(stringToFind)) {
                    expectedStringFound = true;
                } else {
                    expectedStringFound = false;
                    break;
                }
            }
            if (expectedStringFound) {
                break;
            }

            if (expectedStringFound) {
                break;
            }
            Thread.sleep(500); // wait for 0.5 second to check the log again.
        }
        return expectedStringFound;
    }


    /**
     * This method to check whether server is up or not
     * This method wait for some time to check login status by checking the port and login
     * This will throw an exception if port is not open or couldn't login
     *
     * @param automationContext - AutomationContext
     * @return true: If server is up else false
     * @throws Exception  - Error while waiting for login
     */
    public static boolean isServerStartedUp(AutomationContext automationContext, int portOffset)
            throws Exception {

        //Waiting util a port is open, If couldn't open within given time this will throw an Exception
        ClientConnectionUtil.waitForPort(
                Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset,
                DEFAULT_START_STOP_WAIT_MS, false, automationContext.getInstance().getHosts().get("default"));

        //Waiting util login to the the server this will throw LoginAuthenticationExceptionException if fails
        ClientConnectionUtil.waitForLogin(automationContext);
        log.info("Server started successfully.");
        return true;
    }

    /**
     * This method is to check whether server is down or not
     *
     * @param automationContext - AutomationContext
     * @param portOffset        - port offset
     * @return boolean - if server is down true : else false
     */
    public static boolean isServerDown(AutomationContext automationContext, int portOffset)
            throws InterruptedException {
        boolean isPortOpen = true;
        long startTime = System.currentTimeMillis();
        // Looping the isPortOpen method, waiting for a while  to check the server is down or not
        while (isPortOpen && (System.currentTimeMillis() - startTime) < TIMEOUT) {
            isPortOpen = ClientConnectionUtil.isPortOpen(
                    Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset);
            if(isPortOpen){
                Thread.sleep(1000); // waiting 1 sec to check isPortOpen again
            }
        }
        return !isPortOpen;
    }

    /**
     * This method to check file has created or not
     *
     * @param filePathString - file path
     * @return - if file created true else false
     */
    public static boolean waitForFileCreation(String filePathString) {
        boolean isFileCreated = false;
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < TIMEOUT) {
            File createdFile = new File(filePathString);
            if (createdFile.exists() && !createdFile.isDirectory()) {
                isFileCreated = true;
                break;
            }
        }
        return isFileCreated;
    }

    /**
     * This method is to check running os is windows or not
     *
     * @return if current os is windows return true : else false
     */
    public static boolean isCurrentOSWindows() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return true;
        }
        return false;

    }

    /**
     * This method is to get carbon home.
     *
     * @param context - AutomationContext
     * @return - carbon home
     * @throws java.io.IOException - Error while setup carbon home from carbon zip file
     */
    public static String getCarbonHome(AutomationContext context)
            throws IOException {
        if (carbonHome != null) {
            return carbonHome;
        }
        String carbonZip = System.getProperty(FrameworkConstants.SYSTEM_PROPERTY_CARBON_ZIP_LOCATION);
        CarbonServerManager carbonServerManager = new CarbonServerManager(context);
        carbonHome = carbonServerManager.setUpCarbonHome(carbonZip);
        return carbonHome;
    }

    public static void serverShutdown(int portOffset,
                                      AutomationContext automationContext) throws Exception {
        long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
        log.info("Shutting down server..");
        boolean logOutSuccess = false;
        if (ClientConnectionUtil.isPortOpen(
                Integer.parseInt(ExtensionConstants.SERVER_DEFAULT_HTTPS_PORT))) {

            int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset;
            String url = automationContext.getContextUrls().getBackEndUrl();
            String backendURL = url.replaceAll("(:\\d+)", ":" + httpsPort);

            ServerAdminClient serverAdminServiceClient = new ServerAdminClient(backendURL,
                                                                               automationContext.getContextTenant().getTenantAdmin().getUserName(),
                                                                               automationContext.getContextTenant().getTenantAdmin().getPassword());

            serverAdminServiceClient.shutdown();
            while (System.currentTimeMillis() < time && !logOutSuccess) {
                logOutSuccess = isServerDown(automationContext, portOffset);
                // wait until server shutdown is completed
            }
            log.info("Server stopped successfully...");
        }

    }


    public static Process startServerUsingCarbonHome(String carbonHome, int portOffset,
                                                     AutomationContext automationContext,
                                                     String[] parameters) throws Exception {

        Process tempProcess;
        String scriptName = "wso2server";
        File commandDir = new File(carbonHome);
        String[] cmdArray;
        log.info("Starting server............. ");
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            commandDir = new File(carbonHome + File.separator + "bin");
            cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat", "-DportOffset=" + portOffset};
            cmdArray = mergePropertiesToCommandArray(parameters, cmdArray);
            tempProcess = Runtime.getRuntime().exec(cmdArray, null, commandDir);
        } else {
            cmdArray = new String[]{"sh", "bin/" + scriptName + ".sh", "-DportOffset=" + portOffset};
            cmdArray = mergePropertiesToCommandArray(parameters, cmdArray);
            tempProcess = Runtime.getRuntime().exec(cmdArray, null, commandDir);
        }
        int defaultHttpPort = Integer.parseInt(automationContext.getInstance().getPorts().get("http"));
        InputStreamHandler errorStreamHandler =
                new InputStreamHandler("errorStream", tempProcess.getErrorStream());
        inputStreamHandler = new ServerLogReader("inputStream", tempProcess.getInputStream());
        // start the stream readers
        inputStreamHandler.start();
        errorStreamHandler.start();
        ClientConnectionUtil.waitForPort(defaultHttpPort,
                                         DEFAULT_START_STOP_WAIT_MS, false,
                                         automationContext.getInstance().getHosts().get("default"));
        //wait until Mgt console url printed.
        long time = System.currentTimeMillis() + TIMEOUT;
        while (!inputStreamHandler.getOutput().contains(SERVER_STARTUP_MESSAGE) &&
               System.currentTimeMillis() < time) {
            // wait until server startup is completed
        }
        ClientConnectionUtil.waitForLogin(automationContext);
        log.info("Server started successfully.");
        return tempProcess;
    }

    private static String[] mergePropertiesToCommandArray(String[] parameters, String[] cmdArray) {
        if (parameters != null) {
            cmdArray = mergerArrays(cmdArray, parameters);
        }
        return cmdArray;
    }

    private static String[] mergerArrays(String[] array1, String[] array2) {
        return ArrayUtils.addAll(array1, array2);
    }


    public static void serverShutdown(String backendURL, String userName, String passWord,
                                      AutomationContext context, int portOffset) throws Exception {

        log.info("Shutting down server..");
        boolean logOutSuccess = false;
        ServerAdminClient serverAdminServiceClient =
                new ServerAdminClient(backendURL, userName, passWord);
        serverAdminServiceClient.shutdown();
        long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
        while (System.currentTimeMillis() < time && !logOutSuccess) {
            logOutSuccess = isServerDown(context, portOffset);
            // wait until server shutdown is completed
        }
        log.info("Server stopped successfully...");

    }


}
