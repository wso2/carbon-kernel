/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.server;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.server.extensions.*;
import org.wso2.carbon.server.util.Utils;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.config.mapper.ConfigParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;

public class Main {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Main.class.getName());
    private static final String CARBON_PROPERTIES = "carbon.properties";
    private static final String CONF_DIRECTORY_PATH = "carbon.config.dir.path";

    /**
     * Launch the Carbon server.
     * 1) Process and set system properties
     * 2) Invoke extensions.
     * 3) Launch OSGi framework.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        //Setting pax-logging configurations
        String confPath = System.getProperty(LauncherConstants.CARBON_CONFIG_DIR_PATH);
        System.setProperty(LauncherConstants.PAX_DEFAULT_SERVICE_LOG_LEVEL, LauncherConstants.LOG_LEVEL_WARN);
        System.setProperty(LauncherConstants.PAX_LOGGING_PROPERTY_FILE_KEY, confPath + File.separator +
                "etc" + File.separator + LauncherConstants.PAX_LOGGING_PROPERTIES_FILE);

        //Setting Carbon Home
        if (System.getProperty(LauncherConstants.CARBON_HOME) == null) {
            System.setProperty(LauncherConstants.CARBON_HOME, ".");
        }
        System.setProperty(LauncherConstants.AXIS2_HOME, System.getProperty(LauncherConstants.CARBON_HOME));

        //To keep track of the time taken to start the Carbon server.
        System.setProperty("wso2carbon.start.time", System.currentTimeMillis() + "");
        if (System.getProperty("carbon.instance.name") == null) {
            InetAddress addr;
            String ipAddr;
            String hostName;
            try {
                addr = InetAddress.getLocalHost();
                ipAddr = addr.getHostAddress();
                hostName = addr.getHostName();
            } catch (UnknownHostException e) {
                ipAddr = "localhost";
                hostName = "127.0.0.1";
            }
            String uuId = UUID.randomUUID().toString();
            String timeStamp = System.currentTimeMillis() + "";
            String carbon_instance_name = timeStamp + "_" + hostName + "_" + ipAddr + "_" + uuId;
            System.setProperty("carbon.instance.name", carbon_instance_name);

        }
        writePID(System.getProperty(LauncherConstants.CARBON_HOME));
        processCmdLineArgs(args);

        // set WSO2CarbonProfile as worker if workerNode=true present
        if ((System.getProperty(LauncherConstants.WORKER_NODE) != null) &&
                ("true".equals(System.getProperty(LauncherConstants.WORKER_NODE))) &&
                System.getProperty(LauncherConstants.PROFILE) == null) {
            File profileDir =
                    new File(Utils.getCarbonComponentRepo() + File.separator + LauncherConstants.WORKER_PROFILE);
            /*
             *   Better check profile directory is present or not otherwise osgi will hang
             * */
            if (!profileDir.exists()) {
                logger.log(Level.SEVERE, "OSGi runtime " + LauncherConstants.WORKER_PROFILE + " profile not found");
                throw new RuntimeException(LauncherConstants.WORKER_PROFILE + " profile not found");
            }
            System.setProperty(LauncherConstants.PROFILE, LauncherConstants.WORKER_PROFILE);
        }
        //setting default WSO2CarbonProfile as the running Profile if no other Profile is given as an argument
        if (System.getProperty(LauncherConstants.PROFILE) == null) {
            System.setProperty(LauncherConstants.PROFILE, LauncherConstants.DEFAULT_CARBON_PROFILE);
        }
        handleConfiguration();
        addSystemProperties();
        invokeExtensions();
        launchCarbon();
    }

    /**
     * Process command line arguments and set corresponding system properties.
     *
     * @param args cmd line args
     */
    public static void processCmdLineArgs(String[] args) {

        String cmd = null;
        int index = 0;

        // Set the System properties
        for (String arg : args) {
            index++;
            if (arg.startsWith("-D")) {
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
            } else if (arg.toUpperCase().endsWith(LauncherConstants.COMMAND_HELP)) {
                Utils.printUsages();
                System.exit(0);
            } else if (arg.toUpperCase().endsWith(LauncherConstants.COMMAND_CLEAN_REGISTRY)) {
                // sets the system property marking a registry cleanup
                System.setProperty("carbon.registry.clean", "true");
            } else {
                if (cmd == null) {
                    cmd = arg;
                }
            }
        }
    }

    /**
     * Invoke the extensions specified in the carbon.xml
     */
    public static void invokeExtensions() {
        //converting jars found under components/lib and putting them in components/dropins dir
        new DefaultBundleCreator().perform();
        new SystemBundleExtensionCreator().perform();
        new LibraryFragmentBundleCreator().perform();

        //Add bundles in the dropins directory to the bundles.info file.
        new DropinsBundleDeployer().perform();

        //copying patched jars to components/plugins dir
        new PatchInstaller().perform();

        //rewriting the eclipse.ini file
        new EclipseIniRewriter().perform();
    }

    /**
     * Launch the Carbon Server.
     */
    public static void launchCarbon() {

        CarbonLauncher carbonLauncher = new CarbonLauncher();
        carbonLauncher.launch();
    }

    /**
     * Write the process ID of this process to the file
     *
     * @param carbonHome carbon.home sys property value.
     */
    private static void writePID(String carbonHome) {

        byte[] bo = new byte[100];
        String[] cmd = {"sh", "-c", "echo $PPID"};
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            //ignored. We might be invoking this on a Window platform. Therefore if an error occurs
            //we simply ignore the error.
            return;
        }

        try {
            int bytes = p.getInputStream().read(bo);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        String pid = new String(bo);
        if (pid.length() != 0) {
            BufferedWriter out = null;
            try {
                FileWriter writer = new FileWriter(carbonHome + File.separator + "wso2carbon.pid");
                out = new BufferedWriter(writer);
                out.write(pid);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Cannot write wso2carbon.pid file");
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private static void handleConfiguration() {

        String resourcesDir = System.getProperty(LauncherConstants.CARBON_NEW_CONFIG_DIR_PATH);

        String configFilePath = System.getProperty(LauncherConstants.DEPLOYMENT_CONFIG_FILE_PATH);
        if (StringUtils.isEmpty(configFilePath)) {
            configFilePath = System.getProperty(LauncherConstants.CARBON_CONFIG_DIR_PATH) + File.separator +
                    ConfigParser.UX_FILE_PATH;
        }

        String outputDir = System.getProperty(LauncherConstants.CARBON_HOME);
        try {
            ConfigParser.parse(configFilePath, resourcesDir, outputDir);
        } catch (ConfigParserException e) {
            logger.log(Level.SEVERE, "Error while performing configuration changes", e);
            System.exit(1);
        }
    }

    /**
     * Reading carbon.properties file and setting system properties.
     */
    private static void addSystemProperties(){
        java.util.Properties properties = new java.util.Properties();
        String filePath = System.getProperty(CONF_DIRECTORY_PATH) + File.separator + CARBON_PROPERTIES;
        File file = new File(filePath);

        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while reading the file '" + filePath + "'.", e);
                System.exit(1);
            }
        } else {
            logger.log(Level.WARNING, "The file '" + filePath + "' does not exist.");
        }

        java.util.Set<Object> keys = properties.keySet();
        for (Object key: keys)  {
            System.setProperty((String)key, (String)properties.get(key));
        }
        // Set the javax.xml.bind.JAXBContext
        // To fix issue in creating JAXBContext instance in JDK11,
        // It uses the default factory class as “com.sun.xml.internal.bind.v2 ContextFactory".
        // But in the Java 11 runtime, this class is not found.
        System.setProperty("javax.xml.bind.JAXBContextFactory", "com.sun.xml.bind.v2.ContextFactory");
    }
}
