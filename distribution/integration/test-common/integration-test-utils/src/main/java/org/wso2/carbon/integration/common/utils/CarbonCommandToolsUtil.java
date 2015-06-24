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
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.extensions.ExtensionConstants;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.extensions.servers.utils.InputStreamHandler;
import org.wso2.carbon.automation.extensions.servers.utils.ServerLogReader;
import org.wso2.carbon.integration.common.bean.DataSourceBean;
import org.wso2.carbon.integration.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Provides utility methods for carbon tools automation
 */
public class CarbonCommandToolsUtil {

    private static final Log log = LogFactory.getLog(CarbonCommandToolsUtil.class);
    private static String carbonHomePath = null;
    private static ServerLogReader serverLogStreamHandler;

    /**
     * This method is to execute commands and reading the logs to find the expected string.
     *
     * @param directory      - Directory which has the file to be executed .
     * @param cmdArray       - Command array to be executed.
     * @param expectedString - Expected string in  the log.
     * @return boolean - true : Found the expected string , false : not found the expected string.
     * @throws CarbonToolsIntegrationTestException - Error while getting the command directory
     */
    public static boolean isScriptRunSuccessfully(String directory, String[] cmdArray,
                                                  String expectedString)
            throws CarbonToolsIntegrationTestException {

        boolean isFoundTheMessage = false;
        BufferedReader br = null;
        Process process = null;
        for (String cmd : cmdArray) {
            System.out.print(cmd + " ");
        }
        try {
            File commandDir = new File(directory);
            process = Runtime.getRuntime().exec(cmdArray, null, commandDir);
            String line;
            long startTime = System.currentTimeMillis();
            while (!isFoundTheMessage && (System.currentTimeMillis() - startTime) < CarbonIntegrationConstants.DEFAULT_WAIT_MS) {

                br = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                while ((line = br.readLine()) != null) {
                    log.info(line);
                    if (line.contains(expectedString)) {
                        log.info("found the string expected string" + expectedString + ", in line " + line);
                        isFoundTheMessage = true;
                        break;
                    }
                }
            }
            return isFoundTheMessage;
        } catch (IOException ex) {
            log.error("Error when reading the InputStream when running shell script ", ex);
            throw new CarbonToolsIntegrationTestException("Error when reading the InputStream when " +
                                                          "running shell script ", ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("Error when closing BufferedReader  ", e);
                }
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
     * @throws CarbonToolsIntegrationTestException - Error while getting the execution directory
     */
    public static Process runScript(String directory, String[] cmdArray)
            throws CarbonToolsIntegrationTestException {
        try {
            final int portOffset = getPortOffsetFromStringArray(cmdArray);

            File commandDir = new File(directory);
            Process process = Runtime.getRuntime().exec(cmdArray, null, commandDir);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        CarbonCommandToolsUtil.serverShutdown(portOffset);
                    } catch (Exception e) {
                        log.warn("Error while server shutdown ..", e);
                    }
                }
            });
            return process;

        } catch (IOException ex) {
            log.error("Error when reading the InputStream when running shell script ", ex);
            throw new CarbonToolsIntegrationTestException("Error when reading the InputStream when " +
                                                          "running shell script ", ex);
        }
    }

    /**
     * This method to find multiple strings in same line in log
     *
     * @param stringArrayToFind - String array to find all the elements
     * @return boolean - true if found all the strings , false if not
     */
    public static boolean findMultipleStringsInLog(String[] stringArrayToFind) {
        boolean expectedStringFound = false;

        long startTime = System.currentTimeMillis();
        while (!expectedStringFound && (System.currentTimeMillis() - startTime) < CarbonIntegrationConstants.DEFAULT_WAIT_MS) {
            String message = serverLogStreamHandler.getOutput();
            for (String stringToFind : stringArrayToFind) {
                if (message.contains(stringToFind)) {
                    expectedStringFound = true; // Continue until find all the strings in this line
                } else {
                    expectedStringFound = false;
                    break; // break the loop and search the stringToFind in new line
                }
            }
            try {
                Thread.sleep(500); // wait for 0.5 second to check the log again.
            } catch (InterruptedException e) {
                log.warn("Exception while waiting for log message");
            }
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
     * @throws CarbonToolsIntegrationTestException - Error while waiting for login
     */
    public static boolean isServerStartedUp(AutomationContext automationContext, int portOffset)
            throws CarbonToolsIntegrationTestException {
        try {
            //Waiting util a port is open, If couldn't open within given time this will throw an Exception
            ClientConnectionUtil.waitForPort(
                    Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset,
                    CarbonIntegrationConstants.DEFAULT_WAIT_MS, false,
                    automationContext.getInstance().getHosts().get("default"));

            //Waiting util login to the the server this will throw LoginAuthenticationExceptionException if fails
            ClientConnectionUtil.waitForLogin(automationContext);
            log.info("Server started successfully.");
            return true;
        } catch (Exception e) {
            log.error("Error while waiting for login ", e);
            throw new CarbonToolsIntegrationTestException("Error while waiting for login ", e);
        }

    }

    /**
     * This method is to check whether server is down or not
     *
     * @param portOffset - port offset
     * @return boolean - if server is down true : else false
     */
    public static boolean isServerDown(int portOffset) {
        boolean isPortOpen = true;
        long startTime = System.currentTimeMillis();

        // Looping the isPortOpen method, waiting for a while  to check the server is down or not
        while (isPortOpen && (System.currentTimeMillis() - startTime) < CarbonIntegrationConstants.DEFAULT_WAIT_MS) {
            isPortOpen = ClientConnectionUtil.isPortOpen(
                    Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset);
            if (isPortOpen) {
                try {
                    Thread.sleep(500); // waiting 0.5 sec to check isPortOpen again
                } catch (InterruptedException e) {
                    log.warn("Thread interrupted while waiting for the port");
                }
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
        while ((System.currentTimeMillis() - startTime) < CarbonIntegrationConstants.DEFAULT_WAIT_MS) {
            File createdFile = new File(filePathString);
            if (createdFile.exists() && !createdFile.isDirectory()) {
                isFileCreated = true;
                break;
            }
        }
        return isFileCreated;
    }

    /**
     * Provides current operating system
     *
     * @return if current os is windows return true : else false
     */
    public static String getCurrentOperatingSystem() {
        return System.getProperty(FrameworkConstants.SYSTEM_PROPERTY_OS_NAME).toLowerCase();
    }

    /**
     * provides carbon home after extracting the pack.
     *
     * @param context - AutomationContext
     * @return - carbon home
     * @throws CarbonToolsIntegrationTestException - Error while setup carbon home from carbon zip file
     */
    public static String getCarbonHome(AutomationContext context)
            throws CarbonToolsIntegrationTestException {
        try {
            if (carbonHomePath != null) {
                return carbonHomePath;
            }

            String carbonZip = System.getProperty(FrameworkConstants.SYSTEM_PROPERTY_CARBON_ZIP_LOCATION);
            CarbonServerManager carbonServerManager = new CarbonServerManager(context);
            carbonHomePath = carbonServerManager.setUpCarbonHome(carbonZip);
            return carbonHomePath;

        } catch (IOException ex) {
            log.error("Extracting the pack and getting the carbon home failed", ex);
            throw new CarbonToolsIntegrationTestException("Extracting the pack and getting the " +
                                                          "carbon home failed", ex);
        }
    }

    /**
     * This method is to shutdown carbon server
     *
     * @param portOffset - port offset
     * @throws CarbonToolsIntegrationTestException - Error occurred while shutdown the server
     */
    public static void serverShutdown(int portOffset) throws CarbonToolsIntegrationTestException {
        long time = System.currentTimeMillis() + CarbonIntegrationConstants.DEFAULT_WAIT_MS;
        log.info("Shutting down server..");
        boolean isLogoutSuccess = false;

        try {
            AutomationContext automationContext = new AutomationContext();
            if (ClientConnectionUtil.isPortOpen(
                    Integer.parseInt(ExtensionConstants.SERVER_DEFAULT_HTTPS_PORT))) {

                int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset;
                String url = automationContext.getContextUrls().getBackEndUrl();
                String backendURL = url.replaceAll("(:\\d+)", ":" + httpsPort);

                ServerAdminClient serverAdminServiceClient =
                        new ServerAdminClient(
                                backendURL,
                                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                automationContext.getSuperTenant().getTenantAdmin().getPassword());

                serverAdminServiceClient.shutdown();
                while (System.currentTimeMillis() < time && !isLogoutSuccess) {
                    isLogoutSuccess = isServerDown(portOffset);
                    // wait until server shutdown is completed
                }
                log.info("Server stopped successfully...");
            }
        } catch (Exception ex) {
            log.error("Error while shutting down the server using default credentials", ex);
            throw new CarbonToolsIntegrationTestException("Error while shutting down the server using " +
                                                          "default credentials ", ex);
        }

    }

    /**
     * This method is to start a carbon server with a port offset and startup arguments
     *
     * @param carbonHome - carbon home
     * @param portOffset - port offset
     * @param parameters - startup arguments
     * @return Process - process of the started carbon
     * @throws CarbonToolsIntegrationTestException - Throws if server startup fails
     */
    public static Process startServerUsingCarbonHome(
            String carbonHome, int portOffset, String[] parameters)
            throws CarbonToolsIntegrationTestException {
        Process tempProcess;
        String scriptName = "wso2server";
        final int serverStartUpPortOffset = portOffset;
        try {
            AutomationContext automationContext = new AutomationContext();
            File commandDir = new File(carbonHome);
            String[] cmdArray;
            log.info("Starting server............. ");

            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                commandDir = new File(carbonHome + File.separator + "bin");
                cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat", "-DportOffset=" + serverStartUpPortOffset};
                cmdArray = mergePropertiesToCommandArray(parameters, cmdArray);
                tempProcess = Runtime.getRuntime().exec(cmdArray, null, commandDir);
            } else {
                cmdArray = new String[]{"sh", "bin/" + scriptName + ".sh", "-DportOffset=" + serverStartUpPortOffset};
                cmdArray = mergePropertiesToCommandArray(parameters, cmdArray);
                tempProcess = Runtime.getRuntime().exec(cmdArray, null, commandDir);
            }

            int defaultHttpPort = Integer.parseInt(automationContext.getInstance().getPorts().get("http"));

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        CarbonCommandToolsUtil.serverShutdown(serverStartUpPortOffset);
                    } catch (Exception e) {
                        log.error("Error while server shutdown ..", e);
                    }
                }
            });

            InputStreamHandler errorStreamHandler =
                    new InputStreamHandler("errorStream", tempProcess.getErrorStream());
            serverLogStreamHandler = new ServerLogReader("inputStream", tempProcess.getInputStream());

            // start the stream readers
            serverLogStreamHandler.start();
            errorStreamHandler.start();
            ClientConnectionUtil.waitForPort(defaultHttpPort, CarbonIntegrationConstants.DEFAULT_WAIT_MS, false,
                                             automationContext.getInstance().getHosts().get("default"));

            //wait until Mgt console url printed
            long time = System.currentTimeMillis() + CarbonIntegrationConstants.DEFAULT_WAIT_MS;
            while (!serverLogStreamHandler.getOutput().contains(CarbonIntegrationConstants.SERVER_STARTUP_MESSAGE) &&
                   System.currentTimeMillis() < time) {
                // wait until server startup is completed
            }
            log.info("Server started successfully.");
            return tempProcess;
        } catch (XPathExpressionException ex) {
            log.error("Error while starting the server using carbon home", ex);
            throw new CarbonToolsIntegrationTestException("Error while starting the server using carbon home", ex);
        } catch (IOException e) {
            log.error("Server startup folder not found", e);
            throw new CarbonToolsIntegrationTestException("Server startup folder not found", e);
        }
    }

    /**
     * This method is to merge two arrays together
     *
     * @param parameters - Server startup arguments
     * @param cmdArray   - Server startup command
     * @return - merged array
     */
    private static String[] mergePropertiesToCommandArray(String[] parameters, String[] cmdArray) {
        return ArrayUtils.addAll(cmdArray, parameters);
    }

    /**
     * This method is to shutdown carbon server using admin credentials
     *
     * @param backendURL - server backend url
     * @param userName   - admin username
     * @param password   - admin password
     * @param portOffset - port offset
     * @throws CarbonToolsIntegrationTestException - Error while server shutting down
     */
    public static void serverShutdown(String backendURL, String userName, String password,
                                      int portOffset)
            throws CarbonToolsIntegrationTestException {

        try {
            log.info("Shutting down server..");
            boolean logOutSuccess = false;

            ServerAdminClient serverAdminServiceClient =
                    new ServerAdminClient(backendURL, userName, password);

            serverAdminServiceClient.shutdown();
            long time = System.currentTimeMillis() + CarbonIntegrationConstants.DEFAULT_WAIT_MS;

            while (System.currentTimeMillis() < time && !logOutSuccess) {
                // wait until server shutdown is completed
                logOutSuccess = isServerDown(portOffset);
            }
            log.info("Server stopped successfully...");
        } catch (Exception ex) {
            log.error("Error while shutting down the server using default credentials", ex);
            throw new CarbonToolsIntegrationTestException("Error while shutting down the " +
                                                          "server using default credentials", ex);
        }
    }


    /**
     * Get the data source information from the automation configuration file
     *
     * @param dataSourceName - Data source name given in the configuration file
     * @return DataSourceInformation - Information about the data source.
     * @throws XPathExpressionException - Throws if an exception occurred when getting data for configuration file
     */
    public static DataSourceBean getDataSourceInformation(String dataSourceName)
            throws XPathExpressionException {

        AutomationContext automationContext =
                new AutomationContext(CarbonIntegrationConstants.PRODUCT_GROUP,
                                      CarbonIntegrationConstants.INSTANCE,
                                      ContextXpathConstants.SUPER_TENANT,
                                      ContextXpathConstants.SUPER_ADMIN);

        String URL = automationContext.getConfigurationValue(String.format(
                CarbonIntegrationConstants.CONTEXT_XPATH_DATA_SOURCE + "/url", dataSourceName));

        String userName = automationContext.getConfigurationValue(String.format(
                CarbonIntegrationConstants.CONTEXT_XPATH_DATA_SOURCE + "/username", dataSourceName));

        char[] passWord = automationContext.getConfigurationValue(String.format(
                CarbonIntegrationConstants.CONTEXT_XPATH_DATA_SOURCE + "/password", dataSourceName)).toCharArray();

        String driverClassName = automationContext.getConfigurationValue(String.format(
                CarbonIntegrationConstants.CONTEXT_XPATH_DATA_SOURCE + "/driverClassName", dataSourceName));

        return new DataSourceBean(URL, userName, passWord, driverClassName);
    }

    /**
     * Get the -DportOffset value from command array
     * @param commandStringArray - command array
     * @return int - port offset
     */
    private static int getPortOffsetFromStringArray(String[] commandStringArray) {

        int portOffset = 0;
        for (String commandString : commandStringArray) {
            if (commandString.contains(ExtensionConstants.PORT_OFFSET_COMMAND)) {
                String portOffsetString = commandString.split("=")[1];
                portOffset = Integer.parseInt(portOffsetString);
                break;
            }
        }
        return portOffset;
    }

}
