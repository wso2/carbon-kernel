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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.server.extensions.*;
import org.wso2.carbon.server.util.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class Main {
    private static Log log = LogFactory.getLog(Main.class);

    /**
     * Launch the Carbon server.
     * 1) Process and set system properties
     * 2) Invoke extensions.
     * 3) Launch OSGi framework.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
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
        //setting default WSO2CarbonProfile as the running Profile if no other Profile is given as an argument
        if (System.getProperty(LauncherConstants.PROFILE_ID) == null){
            System.setProperty(LauncherConstants.PROFILE_ID, LauncherConstants.DEFAULT_CARBON_PROFILE);
        }
        processCmdLineArgs(args);

        // set WSO2CarbonProfile as worker if workerNode=true present
        if ((System.getProperty(LauncherConstants.WORKER_NODE) != null) && (System.getProperty(LauncherConstants.WORKER_NODE).equals("true"))) {
            File profileDir = new File( Utils.getCarbonComponentRepo() + File.separator + LauncherConstants.WORKER_PROFILE);
               /*
                *   Better check profile directory is present or not otherwise osgi will hang
                * */
            if (!profileDir.exists()) {
                log.fatal("OSGi runtime " + LauncherConstants.WORKER_PROFILE + " profile not found");
                throw new RuntimeException(LauncherConstants.WORKER_PROFILE + " profile not found");
            }
            System.setProperty(LauncherConstants.PROFILE, LauncherConstants.WORKER_PROFILE);
        }
        //setting default WSO2CarbonProfile as the running Profile if no other Profile is given as an argument
        if (System.getProperty(LauncherConstants.PROFILE) == null){
            System.setProperty(LauncherConstants.PROFILE, LauncherConstants.DEFAULT_CARBON_PROFILE);
        }
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
        //TODO Read extensions from the carbon.xml and execute them - Sameera.

        //converting jars found under components/lib and putting them in components/dropins dir
        new DefaultBundleCreator().perform();
        new SystemBundleExtensionCreator().perform();
        new Log4jPropFileFragmentBundleCreator().perform();
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
        String[] cmd = {"bash", "-c", "echo $PPID"};
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
            log.error(e.getMessage(), e);
        }

        String pid = new String(bo);
        if (pid.length() != 0) {
            BufferedWriter out = null;
            try {
                FileWriter writer = new FileWriter(carbonHome + File.separator + "wso2carbon.pid");
                out = new BufferedWriter(writer);
                out.write(pid);
            } catch (IOException e) {
                log.warn("Cannot write wso2carbon.pid file");
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
}




