/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.core.init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.core.security.CarbonJMXAuthenticator;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ManagementFactory;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.ServerException;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * This class is responsible for managing the JMX RMI Server
 */
public class JMXServerManager {
    private static Log log = LogFactory.getLog(JMXServerManager.class);

    private java.rmi.registry.Registry rmiRegistry;
    private JMXConnectorServer         jmxConnectorServer;

    private static final String START_RMI_SERVER      = "StartRMIServer";
    private static final String JMX_HOST_NAME         = "HostName";
    private static final String JMX_RMI_REGISTRY_PORT = "RMIRegistryPort";
    private static final String JMX_RMI_SERVER_PORT   = "RMIServerPort";

    private JMXConfig jmxProperties = new JMXConfig();

    /**
     * The method to start JMX service.
     *
     * @throws ServerException If an error occurs while starting the RMI server
     */
    public void startJMXService() throws ServerException {

        //File path for the jmx config file.
        String filePath = CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "jmx.xml";
        boolean startJMXServer = false;

        File jmxConfigFile = new File(filePath);

        //Check whether jmx.xml file exists
        if (jmxConfigFile.exists()) {
            //Read jmx.xml file.
            parseJMXConfigXML(filePath);
            startJMXServer = jmxProperties.isStartServer();
            if (!startJMXServer) {
                return;
            }
        }

        int rmiRegistryPort = jmxProperties.getRmiRegistryPort();
        if (rmiRegistryPort == -1) {
            throw new RuntimeException("RMIRegistry port has not been properly defined in the " +
                                           "jmx.xml or carbon.xml files");
        }
        MBeanServer mbs = ManagementFactory.getMBeanServer();
        String jmxURL;
        try {
            try {
                rmiRegistry = LocateRegistry.createRegistry(rmiRegistryPort);
            } catch (Throwable ignored) {
                log.error("Could not create the RMI local registry", ignored);
            }

            String hostName;
            //If 'startRMIServer' element in jmx.xml file set to true and 'HostName' element
            // value that file is not null.
            if (startJMXServer && jmxProperties.getHostName() != null) {
                hostName = jmxProperties.getHostName();//Set hostname value from jmx.xml file.
            } else { //Else
                hostName = NetworkUtils.getLocalHostname();
            }
            // Create an RMI connector and start it
            int rmiServerPort = jmxProperties.getRmiServerPort();
            if (rmiServerPort != -1) {
                jmxURL = "service:jmx:rmi://" + hostName + ":" +
                    rmiServerPort + "/jndi/rmi://" + hostName + ":" +
                    rmiRegistryPort + "/jmxrmi";

            } else {
                jmxURL = "service:jmx:rmi:///jndi/rmi://" +
                    hostName + ":" + rmiRegistryPort + "/jmxrmi";
            }
            JMXServiceURL url = new JMXServiceURL(jmxURL);

            // Security credentials are included in the env Map
            HashMap<String, CarbonJMXAuthenticator> env =
                new HashMap<String, CarbonJMXAuthenticator>();
            env.put(JMXConnectorServer.AUTHENTICATOR, new CarbonJMXAuthenticator());
            jmxConnectorServer =
                JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            jmxConnectorServer.start();
            log.info("JMX Service URL  : " + jmxURL);
        } catch (Exception e) {
            String msg = "Could not initialize RMI server";
            log.error(msg, e);
        }
    }

    public void stopJmxService() {
        try {
            if (jmxConnectorServer != null) {
                jmxConnectorServer.stop();
                try {
                    UnicastRemoteObject.unexportObject(rmiRegistry, true); // Stop the RMI registry
                } catch (java.rmi.NoSuchObjectException ignored) {
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while stopping JMXConnectorServer", e);
        }
    }

    /**
     * This method is to get values from jmx.xml file.
     *
     * @param jmxXmlPath File path
     */
    private void parseJMXConfigXML(String jmxXmlPath) {
        /**
         * Parse the following file
         *
         <JMX xmlns="http://wso2.org/projects/carbon/jmx.xml">
         <StartRMIServer>true</StartRMIServer>
         <HostName>localhost</HostName>
         <RMIRegistryPort>9995</RMIRegistryPort>
         <RMIServerPort>1112</RMIServerPort>
         </JMX>
         *
         */
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getLocalHostname();
        } catch (SocketException ignored) {
        }
        boolean startServer = false;
        int rmiRegistryPort;
        int rmiServerPort;
        try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(jmxXmlPath));

            Node jmx = doc.getElementsByTagName("JMX").item(0);
            if (jmx == null) {
                throw new RuntimeException("JMX element not found");
            }

            Element jmxEle = (Element) jmx;

            if (jmxEle.getElementsByTagName(START_RMI_SERVER).getLength() > 0) {
                Node startRMIServer = jmxEle.getElementsByTagName(START_RMI_SERVER).item(0);
                if (startRMIServer != null) {
                    Node item = startRMIServer.getChildNodes().item(0);
                    if (item != null) {
                        startServer = Boolean.parseBoolean(item.getNodeValue().trim());
                        if (!startServer) {
                            jmxProperties.setStartServer(false);
                            return;  // If RMI server is not to be started, then we can simply return
                        }
                    }
                }
            }

            if (jmxEle.getElementsByTagName(JMX_HOST_NAME).getLength() > 0) {
                Node item = jmxEle.getElementsByTagName(JMX_HOST_NAME).item(0);
                if (item != null) {
                    item = item.getChildNodes().item(0);
                    if (item != null) {
                        hostName = item.getNodeValue().trim();
                    }
                }
            }

            rmiRegistryPort = getPort(JMX_RMI_REGISTRY_PORT, jmxEle);
            rmiServerPort = getPort(JMX_RMI_SERVER_PORT, jmxEle);

            jmxProperties.setHostName(hostName);
            jmxProperties.setStartServer(startServer);
            jmxProperties.setRmiRegistryPort(rmiRegistryPort);
            jmxProperties.setRmiServerPort(rmiServerPort);
        } catch (Throwable t) {
            log.fatal("Failed to parse jmx.xml", t);
        }

    }

    private int getPort(String elementTagName, Element jmxEle) {
        String port = "-1";
        if (jmxEle.getElementsByTagName(elementTagName).getLength() > 0) {
            Node item = jmxEle.getElementsByTagName(elementTagName).item(0);
            if (item != null) {
                item = item.getChildNodes().item(0);
                if (item != null) {
                    port = item.getNodeValue().trim();
                    if (port.startsWith("${")) {
                        port = Integer.toString(CarbonUtils.getPortFromServerConfig(port));
                    }
                }
            }
        }
        return Integer.parseInt(port);
    }

    /**
     * Inner class for set/get jmx configuration values.
     */
    private static class JMXConfig {
        private String hostName;

        public void setStartServer(boolean startServer) {
            this.startServer = startServer;
        }

        private boolean startServer;
        private int     rmiRegistryPort;
        private int     rmiServerPort;

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public boolean isStartServer() {
            return startServer;
        }

        public int getRmiRegistryPort() {
            return rmiRegistryPort;
        }

        public void setRmiRegistryPort(int rmiRegistryPort) {
            this.rmiRegistryPort = rmiRegistryPort;
        }

        public int getRmiServerPort() {
            return rmiServerPort;
        }

        public void setRmiServerPort(int rmiServerPort) {
            this.rmiServerPort = rmiServerPort;
        }
    }
}
