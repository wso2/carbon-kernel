/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.launcher;

import org.wso2.carbon.launcher.bootstrapLogging.BootstrapConsoleManager;
import org.wso2.carbon.launcher.bootstrapLogging.BootstrapLogManager;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.wso2.carbon.launcher.utils.Constants.*;

/**
 * Starts a Carbon server instance.
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * @param args arguments
     */
    public static void main(String[] args) {

        // 1) Initialize logging.
        bootstrapLogging();

        // 2) Initialize and/or verify System properties
        initAndVerifySysProps();

        // 3) Load the Carbon start configuration
        CarbonLaunchConfig<String, String> config = loadCarbonLaunchConfig();

        CarbonServer carbonServer = new CarbonServer(config);

        // 4) Register a shutdown hook to stop the server
        registerShutdownHook(carbonServer);

        // 5) Start Carbon server.
        try {
            // This method launches the OSGi framework, loads all the bundles and starts Carbon server completely.
            carbonServer.start();

            // Checking whether a server restart is required.
            boolean restart = Boolean.parseBoolean(System.getProperty("carbon.server.restart"));
            if (restart) {
                // Here in this implementation we do not simply restart the OSGi framework. This could lead up to
                //  memory leaks. Hence we do a complete JVM level restart. Exit state 121 is a special value.
                //  Once the startup script receives this value, it restarts the JVM with the same arguments.
                System.exit(ExitCodes.RESTART_ACTION);
            } else {
                System.exit(ExitCodes.SUCCESSFUL_TERMINATION);
            }
        } catch (Throwable e) {
            // We need to invoke the stop method of the CarbonServer to allow the server to cleanup itself.
            carbonServer.stop();

            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(ExitCodes.UNSUCCESSFUL_TERMINATION);
        }
    }

    /**
     * Loads Carbon launch configuration from the launch.properties file.
     *
     * @return CarbonLaunchConfig
     */
    private static CarbonLaunchConfig<String, String> loadCarbonLaunchConfig() {
        String launchPropFilePath = Utils.getLaunchConfigDir() + File.separator + LAUNCH_PROPERTIES_FILE;
        File launchPropFile = new File(launchPropFilePath);

        if (launchPropFile.exists()) {
            logger.log(Level.FINE, "Loading the Carbon launch configuration from the file " + launchPropFile.getAbsolutePath());

            return new CarbonLaunchConfig<String, String>(launchPropFile);
        } else {

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Loading the Carbon launch configuration from the launch.properties file in the classpath");
            }

            return new CarbonLaunchConfig<String, String>();
        }
    }

    private static void registerShutdownHook(final CarbonServer carbonServer) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                carbonServer.stop();
            }
        });
    }

    private static void initAndVerifySysProps() {
        String carbonHome = System.getProperty(CARBON_HOME);
        if (carbonHome == null || carbonHome.length() == 0) {
            String msg = "carbon.home system property must be set before starting the server";
            logger.log(Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }

        String profileName = System.getProperty(PROFILE);
        if (profileName == null || profileName.length() == 0) {
            System.setProperty(PROFILE, DEFAULT_PROFILE);
        }

        // Set log level for Pax logger to WARN.
        System.setProperty(PAX_DEFAULT_SERVICE_LOG_LEVEL, LOG_LEVEL_WARN);
    }

    private static void bootstrapLogging() {
        try {
            logger.addHandler(BootstrapLogManager.getDefaultHandler());
            logger.addHandler(BootstrapConsoleManager.getDefaultHandler());
        } catch (IOException e) {
            // Following log may never get printed if logging is not properly initialized. Hence the sending the error
            //  message to the standard out.
            e.printStackTrace();
            logger.log(Level.SEVERE, "Could not initialize logging", e);
            throw new RuntimeException(e);
        }
    }
}
