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

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

//    private static Log log = LogFactory.getLog(Main.class);
    // TODO handle restarts and error handling here.

    /**
     * @param args arguments
     */
    public static void main(String[] args) {

        try {
            log.addHandler(BootstrapLogManager.getDefaultHandler());
            log.addHandler(BootstrapConsoleManager.getDefaultHandler());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error occurred while reading log4j.properties file");
            e.printStackTrace();
        }

        // 1) Initialize and/or verify System properties
        initAndVerifySysProps();

        // 2) Initialize logging.
        //TODO

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
            if(restart) {
                // Here in this implementation we do not simply restart the OSGi framework. This could lead up to
                //  memory leaks. Hence we do a complete JVM level restart. Exit state 121 is a special value.
                //  Once the startup script receives this value, it restarts the JVM with the same arguments.
                System.exit(121);
            } else {
                System.exit(0);
            }
        } catch (Throwable e) {
            // We need to invoke the stop method of the CarbonServer to allow the server to cleanup itself.
            carbonServer.stop();
            // TODO add proper logging
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static CarbonLaunchConfig<String, String> loadCarbonLaunchConfig() {
        String launchPropFilePath = Utils.getRepositoryConfDir() + File.separator + "osgi" +
                File.separator + LAUNCH_PROPERTIES_FILE;
        File launchPropFile = new File(launchPropFilePath);

        if (launchPropFile.exists()) {
            return new CarbonLaunchConfig<String, String>(launchPropFile);
        } else {
            return new CarbonLaunchConfig<String, String>();
        }
    }

    private static void registerShutdownHook(final CarbonServer carbonServer) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    carbonServer.stop();
                } catch (Throwable e) {
                    System.exit(-1);
                }
            }
        });
    }

    private static void initAndVerifySysProps() {
        String carbonHome = System.getProperty(CARBON_HOME);
        if (carbonHome == null || carbonHome.length() == 0) {
            throw new RuntimeException("carbon.home system property must be set before starting the server");
        }

        String profileName = System.getProperty(PROFILE);
        if (profileName == null || profileName.length() == 0) {
            System.setProperty(PROFILE, DEFAULT_PROFILE);
        }

        System.setProperty(LOGGING_DEFAULT_SERVICE_NAME, PAX_LOGGING_LEVEL);
        System.setProperty(BUNDLE_CONFIG_LOCATION, Utils.getRepositoryConfDir());
    }
}
