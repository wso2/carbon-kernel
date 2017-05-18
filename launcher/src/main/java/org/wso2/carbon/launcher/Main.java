/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.launcher;

import org.wso2.carbon.launcher.config.CarbonLaunchConfig;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.wso2.carbon.launcher.Constants.CARBON_HOME;
import static org.wso2.carbon.launcher.Constants.DEFAULT_PROFILE;
import static org.wso2.carbon.launcher.Constants.ExitCodes;
import static org.wso2.carbon.launcher.Constants.LAUNCH_PROPERTIES_FILE;
import static org.wso2.carbon.launcher.Constants.LOG_LEVEL_WARN;
import static org.wso2.carbon.launcher.Constants.PAX_DEFAULT_SERVICE_LOG_LEVEL;
import static org.wso2.carbon.launcher.Constants.PAX_LOGGING_PROPERTIES_FILE;
import static org.wso2.carbon.launcher.Constants.PAX_LOGGING_PROPERTY_FILE_KEY;
import static org.wso2.carbon.launcher.Constants.PAX_LOG_SERVICE_RANKING_LEVEL;
import static org.wso2.carbon.launcher.Constants.PROFILE;
import static org.wso2.carbon.launcher.Constants.RUNTIME_PATH;

/**
 * Starts a Carbon server instance.
 *
 * @since 5.0.0
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * @param args arguments
     */
    public static void main(String[] args) {

        //get the starting time to calculate the server startup time duration
        if (System.getProperty(Constants.START_TIME) == null) {
            System.setProperty(Constants.START_TIME, System.currentTimeMillis() + "");
        }

        // 1) Process command line arguments.
        processCmdLineArgs(args);

        // 2) Initialize and/or verify System properties
        initAndVerifySysProps();

        // 3) Load the Carbon start configuration
        CarbonLaunchConfig config = loadCarbonLaunchConfig();

        CarbonServer carbonServer = new CarbonServer(config);

        // 4) Register a shutdown hook to stop the server
        registerShutdownHook(carbonServer);

        // 5) Write pid to carbon.pid file
        writePID(System.getProperty(RUNTIME_PATH));

        // 6) Start Carbon server.
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
    private static CarbonLaunchConfig loadCarbonLaunchConfig() {
        File launchPropFile = Utils.getLaunchConfigDirectory().resolve(LAUNCH_PROPERTIES_FILE).toFile();

        if (launchPropFile.exists()) {
            logger.log(Level.FINE, "Loading the Carbon launch configuration from the file " +
                    launchPropFile.getAbsolutePath());

            return new CarbonLaunchConfig(launchPropFile);
        } else {

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Loading the Carbon launch configuration from the launch.properties file " +
                        "in the classpath");
            }

            return new CarbonLaunchConfig();
        }
    }

    /**
     * Registers a new virtual-machine shutdown hook.
     *
     * @param carbonServer Carbon server to stop
     */
    private static void registerShutdownHook(final CarbonServer carbonServer) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                carbonServer.stop();
            }
        });
    }

    /**
     * Initialize and verify system properties.
     */
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

        // Set log level for Pax logger to WARN and log service ranking to maximum value.
        System.setProperty(PAX_DEFAULT_SERVICE_LOG_LEVEL, LOG_LEVEL_WARN);
        System.setProperty(PAX_LOG_SERVICE_RANKING_LEVEL, String.valueOf(Integer.MAX_VALUE));

        Path paxLoggingPropertiesFile = Paths.get(carbonHome, "conf", "etc", PAX_LOGGING_PROPERTIES_FILE);
        if (paxLoggingPropertiesFile.toFile().exists()) {
            System.setProperty(PAX_LOGGING_PROPERTY_FILE_KEY, paxLoggingPropertiesFile.toAbsolutePath().toString());
            logger.log(Level.FINE, "Setting pax logging properties file path to : " +
                                   paxLoggingPropertiesFile.toAbsolutePath().toString());
        } else {
            String msg = PAX_LOGGING_PROPERTIES_FILE + " should be available to start the server";
            logger.log(Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Process command line arguments and set corresponding system properties.
     *
     * @param args cmd line args
     */
    public static void processCmdLineArgs(String[] args) {
        // Set the System properties
        Arrays.asList(args)
                .stream()
                .filter(arg -> arg.startsWith("-D"))
                .forEach(arg -> {
                    int indexOfEq = arg.indexOf('=');
                    String property;
                    String value;
                    if (indexOfEq != -1) {
                        property = arg.substring(2, indexOfEq);
                        value = arg.substring(indexOfEq + 1);
                    } else {
                        property = arg.substring(2);
                        value = "true";
                    }
                    System.setProperty(property, value);
                });
    }

    /**
     * Write the process ID of this process to the file.
     *
     * @param runtimePath wso2.runtime.path sys property value.
     */
    private static void writePID(String runtimePath) {

        String[] cmd = {"bash", "-c", "echo $PPID"};
        Process p;
        String pid = "";
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            //Ignored. We might be invoking this on a Window platform. Therefore if an error occurs
            //we simply ignore the error.
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(),
                StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            pid = builder.toString();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        if (pid.length() != 0) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Paths.get(runtimePath, "runtime.pid").toString()),
                    StandardCharsets.UTF_8))) {
                writer.write(pid);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Cannot write runtime.pid file");
            }
        }
    }
}
