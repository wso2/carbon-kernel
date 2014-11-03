/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.utils;


import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.*;

/**
 * A collection of useful utility methods
 */
public class CarbonUtils {

    private static final String REPOSITORY = "repository";
    
	private static final String TRUE = "true";
	private static Log log = LogFactory.getLog(CarbonUtils.class);
    private static boolean isServerConfigInitialized;

    public static boolean isAdminConsoleEnabled() {
        boolean enableAdminConsole = false;
        String enableAdminConsoleProp =
                ServerConfiguration.getInstance().getFirstProperty("Management.EnableConsole");
        if (enableAdminConsoleProp != null) {
            enableAdminConsole = Boolean.valueOf(enableAdminConsoleProp);
        }
        return enableAdminConsole;
    }

    /**
     * Get the port corresponding to a particular transport
     *
     * @param configContextService The OSGi ConfigurationContextService
     * @param transport            The transport
     * @return The port corresponding to the <code>transport</code>
     * @throws IllegalStateException If no port is associated with the transport, or if this method
     *                               has been called before the ListenerManager has started
     */
    public static int getTransportPort(ConfigurationContextService configContextService,
                                       String transport) {
        return getTransportPort(configContextService.getServerConfigContext(), transport);
    }

    /**
     * Get the port corresponding to a particular transport
     *
     * @param configContext The Axis2 ConfigurationContext
     * @param transport     The transport
     * @return The port corresponding to the <code>transport</code>
     * @throws IllegalStateException If no port is assocaited with the transport, or if this method
     *                               has been called before the ListenerManager has started
     */
    public static int getTransportPort(ConfigurationContext configContext, String transport) {
        return getTransportPort(configContext.getAxisConfiguration(), transport);
    }

    /**
     * Get the proxy port corresponding to a particular transport
     *
     * @param configContext The Axis2 ConfigurationContext
     * @param transport     The transport
     * @return The proxy port corresponding to the <code>transport</code>. -1 if the proxy port is not defined
     * @throws IllegalStateException If no port is assocaited with the transport, or if this method
     *                               has been called before the ListenerManager has started
     */
    public static int getTransportProxyPort(ConfigurationContext configContext, String transport) {
        return getTransportProxyPort(configContext.getAxisConfiguration(), transport);
    }

    /**
     * Get the port corresponding to a particular transport
     *
     * @param axisConfig The AxisConfiguration
     * @param transport  The transport
     * @return The port corresponding to the <code>transport</code>
     * @throws IllegalStateException If no port is assocaited with the transport, or if this method
     *                               has been called before the ListenerManager has started
     */
    public static int getTransportPort(AxisConfiguration axisConfig, String transport) {
        String transportPort = System.getProperty(transport + "Port");
        if (transportPort == null) {
            if(axisConfig == null) return -1; // Server not yet started
            TransportInDescription transportIn = axisConfig.getTransportIn(transport);
            if(transportIn == null) return -1; // Transport not yet started
            Parameter portParam = transportIn.getParameter("port");
            if (portParam != null) {
                transportPort = (String) portParam.getValue();
            } else {
                throw new IllegalStateException(transport + " port has not been set yet. " +
                        "Most probably the ListenerManager has not" +
                        " been started yet or the this transport does not " +
                        "have a port associated with it. You can wait until " +
                        "the org.apache.axis2.engine.ListenerManager OSGi " +
                        "service is available & retry.");
            }
        }
        return Integer.parseInt(transportPort);
    }

    /**
     * Get the proxy port corresponding to a particular transport
     *
     * @param axisConfig The AxisConfiguration
     * @param transport  The transport
     * @return The proxy port corresponding to the <code>transport</code>.
     *         -1 if this parameter is not defined
     * @throws IllegalStateException If no port is assocaited with the transport, or if this method
     *                               has been called before the ListenerManager has started
     */
    public static int getTransportProxyPort(AxisConfiguration axisConfig, String transport) {
        String transportPort;
        if(axisConfig == null) return -1; // Server not yet started
        TransportInDescription transportIn = axisConfig.getTransportIn(transport);
        if(transportIn == null) return -1; // Transport not yet started
        Parameter proxyPortParam = axisConfig.getTransportIn(transport).getParameter("proxyPort");
        if (proxyPortParam != null) {
            transportPort = (String) proxyPortParam.getValue();
            return Integer.parseInt(transportPort);
        }
        return -1;
    }

    public static String getAxis2Xml() {
        String axis2XML = ServerConfiguration.getInstance().
                getFirstProperty("Axis2Config.ConfigurationFile");
        if (axis2XML == null) {
            axis2XML = System.getProperty(Constants.AXIS2_CONF);
        }
        return axis2XML;
    }

    public static String getServerXml() {
        String carbonXML =
                System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        /* if user set the system property telling where is the configuration directory*/
        if (carbonXML == null) {
            return getCarbonConfigDirPath() + File.separator + "carbon.xml";
        }
        return carbonXML + File.separator + "carbon.xml";
    }

    public static String getCarbonHome() {
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = System.getenv(CarbonConstants.CARBON_HOME_ENV);
            System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
        }
        return carbonHome;
    }

	public static String getCarbonCatalinaHome() {
		String carbonCatalinaHomePath = System.getProperty(ServerConstants.CARBON_CATALINA_HOME);
		if (carbonCatalinaHomePath == null) {
			carbonCatalinaHomePath = System.getenv(CarbonConstants.CARBON_CATALINA_DIR_PATH_ENV);
			if (carbonCatalinaHomePath == null) {
				return getCarbonHome() + File.separator + "lib" + File.separator + "tomcat"
						+ File.separator + "work" + File.separator + "Catalina";
			}
		}
		return carbonCatalinaHomePath;

	}
	
    public static String getCarbonTenantsDirPath() {
        String carbonTenantsDirPath = System.getProperty(ServerConstants.CARBON_TENANTS_DIR_PATH);
        if (carbonTenantsDirPath == null) {
            carbonTenantsDirPath = System.getenv(CarbonConstants.CARBON_TENANTS_DIR_PATH_ENV);
        }
        if (carbonTenantsDirPath == null) {
            carbonTenantsDirPath = getCarbonHome() + File.separator + REPOSITORY +
                    File.separator + "tenants";
        }
        return carbonTenantsDirPath;
    }

    public static String getEtcCarbonConfigDirPath() {
        return getCarbonConfigDirPath() + File.separator + "etc";
    }
    
    public static String getCarbonSecurityConfigDirPath() {
        return getCarbonConfigDirPath() + File.separator + "security";
    }
    

    public static String getCarbonConfigDirPath() {
        String carbonConfigDirPath = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        if (carbonConfigDirPath == null) {
            carbonConfigDirPath = System.getenv(CarbonConstants.CARBON_CONFIG_DIR_PATH_ENV);
            if (carbonConfigDirPath == null) {
                return getCarbonHome() + File.separator + REPOSITORY + File.separator + "conf";
            }
        }
        return carbonConfigDirPath;
    }

    public static String getCarbonLogsPath() {
        String carbonLogsPath = System.getProperty(ServerConstants.CARBON_LOGS_PATH);
        if (carbonLogsPath == null) {
            carbonLogsPath = System.getenv(CarbonConstants.CARBON_LOGS_PATH_ENV);
            if (carbonLogsPath == null) {
                return getCarbonHome() + File.separator + REPOSITORY + File.separator + "logs";
            }
        }
        return carbonLogsPath;
    }

    public static String getComponentsRepo() {
        String componentsRepo = System.getProperty(ServerConstants.COMPONENT_REP0);
        if (componentsRepo == null) {
            componentsRepo = System.getenv(CarbonConstants.COMPONENT_REP0_ENV);
            if (componentsRepo == null) {
                return getCarbonHome() + File.separator + REPOSITORY + File.separator +
                        "components" + File.separator + "plugins";
            }
        }
        return componentsRepo;
    }

    public static String getCarbonOSGiDropinsDir() {
        return getCarbonHome() + File.separator + REPOSITORY + File.separator +
                "components" + File.separator + "dropins";
    }

    public static String getAxis2Repo() {
        String axis2Repo = System.getProperty(ServerConstants.AXIS2_REPO);
        if (axis2Repo == null) {
            axis2Repo = System.getenv(CarbonConstants.AXIS2_REPO_ENV);
        }
        return axis2Repo;
    }

    public static String getRegistryXMLPath() {
        String carbonHome = CarbonUtils.getCarbonHome();
        String configPath = null;
        if (carbonHome != null) {
            if (System.getProperty(ServerConstants.REGISTRY_XML_PATH) == null) {
                configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "registry.xml";
            } else {
                configPath = System.getProperty(ServerConstants.REGISTRY_XML_PATH);
            }
        }
        return configPath;
    }

    public static String getUserMgtXMLPath() {
        String carbonHome = CarbonUtils.getCarbonHome();
        String configPath = null;
        if (carbonHome != null) {
            if (System.getProperty(ServerConstants.USER_MGT_XML_PATH) == null) {
                configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "user-mgt.xml";
            } else {
                configPath = System.getProperty(ServerConstants.USER_MGT_XML_PATH);
            }
        }
        return configPath;
    }

    /**
     * Check whther the specified Strin corresponds to a URL
     *
     * @param location The String to be checked
     * @return true - if <code>location</code> is a URL, false - otherwise
     */
    public static boolean isURL(String location) {
        try {
            new URL(location);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Get the last updated time of the service artifact corresponding to AxisServiceGroup
     * serviceGroup
     *
     * @param serviceGroup The service group whose last updated time we need to get
     * @return The last updated time of the service artifact corresponding to AxisServiceGroup
     *         serviceGroup. Will return null if a Axis2 URL repository is in use.
     */
    public static Long lastUpdatedTime(AxisServiceGroup serviceGroup) {
        String axis2Repo = ServerConfiguration.getInstance().
                getFirstProperty(ServerConfiguration.AXIS2_CONFIG_REPO_LOCATION);
        if (isURL(axis2Repo)) { // We do not support retrieving the timestamps of URLs
            return null;
        }

        Long lastUpdated = null;
        String fileName = "";
        for (Iterator<AxisService> serviceIter = serviceGroup.getServices(); serviceIter.hasNext();) {
            AxisService axisService = serviceIter.next();
            URL fn = axisService.getFileName();
            if (fn != null) {
                fileName = fn.getPath();
            }
            if ((fileName != null) && (fileName.trim().length() != 0)) {
                File file = new File(fileName);
                lastUpdated = file.lastModified();
                break;
            }
        }
        return lastUpdated;
    }

    /**
     * Get the MD5 hash value of the service artifact corresponding to AxisServiceGroup
     * serviceGroup
     *
     * @param serviceGroup The service group whose last updated time we need to get
     * @return The MD5 hash of the service artifact corresponding to AxisServiceGroup
     *         serviceGroup. Will return null if a Axis2 URL repository is in use.
     */
    public static String computeServiceHash(AxisServiceGroup serviceGroup) {
        String axis2Repo = ServerConfiguration.getInstance().
                getFirstProperty(ServerConfiguration.AXIS2_CONFIG_REPO_LOCATION);
        if (isURL(axis2Repo)) { // We do not support retrieving the timestamps of URLs
            return null;
        }

        // If there is a parameter to always force a new service addtion, we don't need hash
        // TODO - Fix handleExistingServiceInit method and remove this parameter
        Parameter param = serviceGroup.getParameter(CarbonConstants.FORCE_EXISTING_SERVICE_INIT);
        if (param != null && TRUE.equals(param.getValue().toString())) {
            return null;
        }

        String hashValue = null;
        String fileName = "";
        for (Iterator<AxisService> serviceIter = serviceGroup.getServices(); serviceIter.hasNext();) {
            AxisService axisService = serviceIter.next();
            URL fn = axisService.getFileName();
            if (fn != null) {
                fileName = fn.getPath();
            }
            if ((fileName != null) && (fileName.trim().length() != 0)) {
                File file = new File(fileName);
                try {
                    hashValue = getMD5(getBytesFromFile(file));
                } catch (CarbonException ignore) {
                    // If some error occures in calculating the hash, just ignore it..
                    if (log.isDebugEnabled()) {
                        log.debug("Error occured in calculating the hash", ignore);
                    }
                }
                break;
            }
        }
        return hashValue;
    }
    
    /**
     * 
     * @return absolute path to carbon tmp directory
     */
    public static String getTmpDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * There's a separate tmp space for each tenant under java temp dir. This method can be used
     * to get the absolute path to current tenant's temp location. If the sub directories are not
     * found already, those are created.
     *
     * @param axisConfig - AxisConfiguration instance of the current tenant
     * @return - absolute path to tmp tenant space
     */
    public static String getTenantTmpDirPath(AxisConfiguration axisConfig) {
        String tenantTmpDirPath = null;
        String javaTempDir = System.getProperty("java.io.tmpdir");
        if (javaTempDir != null) {
            String tmpPath =  javaTempDir + File.separator + "tenants";
            if (createDir(tmpPath)) {
                CarbonContext carbonCtxHolder = PrivilegedCarbonContext
                        .getThreadLocalCarbonContext();
                if (carbonCtxHolder != null) {
                	int tempTenantId = carbonCtxHolder.getTenantId();
                    // use 0 for super tenant
                	if (tempTenantId == -1 ||
                            tempTenantId == MultitenantConstants.SUPER_TENANT_ID) {
                        tempTenantId = 0;
                	}
                    tmpPath += File.separator + tempTenantId;
                    if (createDir(tmpPath)) {
                        tenantTmpDirPath = tmpPath;
                    }
                }
            }
        }
        return tenantTmpDirPath;
    }

    private static boolean createDir(String path) {
        File tmpFile = new File(path);
        if (!tmpFile.exists() && !tmpFile.mkdir()) {
            log.error("Error while creating directory at : " + path);
            return false;
        }
        return true;
    }

    /**
     * Get the port of the command listener. A server socket is opened on this port to listen
     * to incoming commands
     *
     * @return The command listener port
     */
    public static int getCommandListenerPort() {
        int serverPort = -1;
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String port = serverConfig.getFirstProperty("Ports.CommandListener");
        if (port == null) {
            port = serverConfig.getFirstProperty("CommandListener.Port");
        }
        if (port != null) {
            serverPort = Integer.parseInt(port);
        }
        return serverPort;
    }

    /**
     * Register a faulty service
     *
     * @param artifactPath         Path of the faulty service artifact
     * @param serviceType          Service type of the faulty service
     * @param configurationContext ConfigurationContext
     * @throws AxisFault If an error occurs while creating the service type Axis2 parameter
     */
    public static void registerFaultyService(String artifactPath,
                                             String serviceType,
                                             ConfigurationContext configurationContext)
            throws AxisFault {
        String repository = configurationContext.getAxisConfiguration().getRepository().getPath();
        String serviceName = artifactPath;
        if (File.separatorChar == '\\') {
            serviceName = serviceName.replace('\\', '/');
            repository = repository.replace('\\', '/');
        }
        if (serviceName.endsWith("/")) {
            serviceName = serviceName.substring(0, serviceName.length() - 1);
        }
        if (repository.endsWith("/")) {
            repository = repository.substring(0, repository.length() - 1);
        }
        serviceName = serviceName.substring(repository.length() + 1);
        serviceName = serviceName.substring(serviceName.indexOf('/') + 1);

        int slashIndex = serviceName.lastIndexOf('/');
        int dotIndex = serviceName.lastIndexOf('.');
        if (dotIndex != -1 && (dotIndex > slashIndex)) {
            serviceName = serviceName.substring(0, dotIndex);
        }

        AxisService service = new AxisService(serviceName);
        if (serviceType != null) {
            Parameter serviceTypeParam = new Parameter(ServerConstants.SERVICE_TYPE,
                    serviceType);
            service.addParameter(serviceTypeParam);
        }

        Map<String, AxisService> faultyServicesMap =
                (Map<String, AxisService>) configurationContext.
                        getPropertyNonReplicable(CarbonConstants.FAULTY_SERVICES_MAP);
        if (faultyServicesMap == null) {
            faultyServicesMap = new HashMap<String, AxisService>();
            configurationContext.setNonReplicableProperty(CarbonConstants.FAULTY_SERVICES_MAP,
                    faultyServicesMap);
        }
        faultyServicesMap.put(artifactPath, service);
    }

    /**
     * Get a faulty service
     *
     * @param serviceName          Name of the faulty service
     * @param configurationContext ConfigurationContext
     * @return AxisService object corresponding to the <code>serviceName</code>
     */
    public static AxisService getFaultyService(String serviceName,
                                               ConfigurationContext configurationContext) {
        Map<String, AxisService> faultyServicesMap =
                (Map<String, AxisService>) configurationContext.
                        getPropertyNonReplicable(CarbonConstants.FAULTY_SERVICES_MAP);
        if (faultyServicesMap != null) {
            return faultyServicesMap.get(serviceName);
        }
        return null;
    }

    public static boolean isRemoteRegistry() throws Exception {
        ServerConfiguration serverConfig = getServerConfiguration();
        String isRemoteRegistry =
                serverConfig.getFirstProperty("Registry.Type");
        return (isRemoteRegistry != null && isRemoteRegistry.equalsIgnoreCase("remote"));
    }

    public static String getCarbonRepository() {
        ServerConfiguration serverConfig = getServerConfiguration();
        return serverConfig.getFirstProperty("Axis2Config.RepositoryLocation"); //TODO: Change to Carbon.Repository in carbon.xml
    }

    public static ServerConfiguration getServerConfiguration() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        if (!isServerConfigInitialized) {
            String serverXml = CarbonUtils.getServerXml();
            File carbonXML = new File(serverXml);
            InputStream inSXml = null;
            try {
                inSXml = new FileInputStream(carbonXML);
                serverConfig.init(inSXml);
                isServerConfigInitialized = true;
            } catch (Exception e) {
                log.error("Cannot read file " + serverXml, e);
            } finally {
                if (inSXml != null) {
                    try {
                        inSXml.close();
                    } catch (IOException e) {
                        log.warn("Cannot close file " + serverXml, e);
                    }
                }
            }
        }
        return serverConfig;
    }

    /**
     * Check if this is a ChildNode or Not
     *
     * @return true if the instance is a child node in multiple instance scenario
     */
    public static boolean isMasterNode() {
        if (TRUE.equals(System.getProperty("instance")) && TRUE.equals(System.getProperty("master"))) {
            return true;
        }
        return false;
    }

    /**
     * Check if this is an Instance started by a Java exec
     *
     * @return true if this is an instance started by Java exec
     */
    public static boolean isChildNode() {
        if (TRUE.equals(System.getProperty("instance"))) {
            return true;
        }
        return false;
    }

    /**
     * Check whether this is the multiple Instance scenario- means started the server with -n arg
     *
     * @return true if the server started with -n argument
     */
    public static boolean isMultipleInstanceCase() {
        if (System.getProperty("instances.value") != null) {
            return true;
        }
        return false;
    }

    /**
     * Check whether this is the a node which should readonly
     *
     * @return true if the server started with -n argument
     */
    public static boolean isReadOnlyNode() {
        if (isChildNode() && !isMasterNode()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the contents of the file in a byte array.
     *
     * @param file the file the to read
     * @return the content of the file
     * @throws CarbonException throws if the operation failed.
     */
    public static byte[] getBytesFromFile(File file) throws CarbonException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);

            long length = file.length();
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
                throw new CarbonException("File to read is too large. File name: " +
                        file.getName() + ". File length limit: " + Integer.MAX_VALUE);
            }

            // byte array to keep the data
            byte[] bytes = new byte[(int) length];

            int numRead;
            int offset = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            if (offset < bytes.length) {
                throw new CarbonException("Couldn't read the entire file. " +
                        "File name: " + file.getName());
            }

            return bytes;   

        } catch (FileNotFoundException e) {
            throw new CarbonException("Error creating the file input stream", e);

        } catch (IOException e) {
            throw new CarbonException("Error in reading file. File name: " + file.getName(), e);

        } finally {
            try {
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                log.warn("Error in closing the file input stream. " +
                        "File name: " + e.getMessage(), e);
            }
        }
    }

    public static String getMD5(byte[] content) {
        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            log.error("Error while generating the MD5", e);
            return null;
        }
        m.update(content, 0, content.length);

        return new BigInteger(1, m.digest()).toString(16);
    }

    /**
     * Is this Carbon server running in standalone mode?
     *
     * @return true is this Carbon server is running in standalone mode, false - if it is running
     *         as a webapp within another servlet container
     */
    public static boolean isRunningInStandaloneMode() {
        return System.getProperty(ServerConstants.STANDALONE_MODE, "false").equalsIgnoreCase(TRUE);
    }

    /**
     * Should we use a Registry based repo instead of a FileSystem based one?
     *
     * @return true - if registry based repo should be used system-wide
     */
    public static boolean useRegistryBasedRepository() {
        return TRUE.equalsIgnoreCase(System.getProperty("carbon.use.registry.repo"));
    }

    public static void checkSecurity() {
        CarbonBaseUtils.checkSecurity();
    }

    public static void checkSecurity(List<String> allowedClasses) {
        //CarbonBaseUtils.checkSecurity();
        //TODO should remove this method
    }

    public static void checkSecurity(Map<String, String> allowedMethods) {
        CarbonBaseUtils.checkSecurity(allowedMethods);
    }

    public static String getBackendHttpPort(ConfigurationContext configurationContext) throws Exception {
        String httpPort;

        httpPort = ServerConfiguration.getInstance().getFirstProperty(CarbonConstants.REGISTRY_HTTP_PORT);

        if (httpPort == null) {
            httpPort = (String) configurationContext.getAxisConfiguration().getTransportIn("http")
                    .getParameter("port").getValue();
        }

        return httpPort;
    }

    public static String getServerURL(ServerConfigurationService serverConfig,
                                      ConfigurationContext configCtx) {
        ServerConfigurationService serverConfigToProcess = (serverConfig == null)? getServerConfiguration() : serverConfig ;

        return getServerURL(serverConfigToProcess.getFirstProperty(CarbonConstants.SERVER_URL), 
                            configCtx);
    }

    public static String getServerURL(ServletContext servletContext, HttpSession httpSession,
                                      ConfigurationContext configCtx) {
        String url;
        Object obj = httpSession.getAttribute(CarbonConstants.SERVER_URL);
        if (obj instanceof String) {
            // Server URL is present in the servlet session
            url = (String) obj;
        } else {
            url = (String) servletContext.getAttribute(CarbonConstants.SERVER_URL);
        }
        return getServerURL(url, configCtx);
    }

    private static String getServerURL(String url, ConfigurationContext configCtx) {
        String carbonMgtParam = "${carbon.management.port}";
        String mgtTransport = getManagementTransport();
        String returnUrl = url;
		if (returnUrl.indexOf(carbonMgtParam) != -1) {
            String httpsPort =
                    CarbonUtils.getTransportPort(configCtx, mgtTransport) + "";
            returnUrl = returnUrl.replace(carbonMgtParam, httpsPort);
        }
        if (returnUrl.indexOf("${carbon.context}") != -1) {
            String context = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
            if (context.equals("/")) {
                context = "";
            }
            returnUrl = returnUrl.replace("${carbon.context}", context);
        }
        return returnUrl;
    }

    /**
     * @return The transport on which the management functionality is available
     */
    public static String getManagementTransport() {
        String mgtConsoleTransport =
                ServerConfiguration.getInstance().getFirstProperty("ManagementTransport");
        if (mgtConsoleTransport == null || mgtConsoleTransport.startsWith("${")) {
            mgtConsoleTransport = "https";
        }
        return mgtConsoleTransport;
    }

    /**
     * Returns a copy of the provided array. Same as the JDK 1.6 Arrays.copyOf() method
     *
     * @param original The original array
     * @param <T> Type of objects in the original array
     * @return Copy of the provided array
     */
    public static <T> T[] arrayCopyOf(T[] original) {
        if(original == null) {
            return null;
        }
        Class newType = original.getClass();
        int newLength = original.length;
        T[] copy = (newType == Object[].class)
                   ? (T[]) new Object[newLength]
                   : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,  newLength);
        return copy;
    }

    /**
     * This is to read the port values defined in other config files, which are overridden
     * from those in carbon.xml.
     * @param property
     * @return
     */
    public static int getPortFromServerConfig(String property) {
        String port;
        int portNumber = -1;
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        // The following condition deals with ports specified to be read from carbon.xml.
        // Ports are specified as templates: eg ${Ports.EmbeddedLDAP.LDAPServerPort},
        if (indexOfStartingChars < property.indexOf("${") &&
            (indexOfStartingChars = property.indexOf("${")) != -1 &&
            (indexOfClosingBrace = property.indexOf('}')) != -1) { // Is this template used?

            String portTemplate = property.substring(indexOfStartingChars + 2,
                                                     indexOfClosingBrace);

            port = serverConfiguration.getFirstProperty(portTemplate);

            if (port != null) {
                portNumber = Integer.parseInt(port);
            }

        }
        String portOffset = System.getProperty("portOffset",
                                               serverConfiguration.getFirstProperty("Ports.Offset"));
        //setting up port offset properties as system global property which allows this
        //to available at the other context as required (fix 2011-11-30)
        System.setProperty("portOffset", portOffset);
        return portOffset == null? portNumber : portNumber + Integer.parseInt(portOffset);
    }

    /**
     * Checks whether the given AxisService is an Admin Service or a Hidden Service
     *
     * @param service - AxisService instance
     * @return - filtered service or not
     */
    public static boolean isFilteredOutService(AxisService service) {
        String adminParamValue =
                (String) service.getParameterValue(CarbonConstants.ADMIN_SERVICE_PARAM_NAME);
        String hiddenParamValue =
                (String) service.getParameterValue(CarbonConstants.HIDDEN_SERVICE_PARAM_NAME);
        if (adminParamValue != null && adminParamValue.length() != 0) {
            if (Boolean.parseBoolean(adminParamValue.trim())) {
                return true;
            }
        } else if (hiddenParamValue != null && hiddenParamValue.length() != 0) {
            if (Boolean.parseBoolean(hiddenParamValue.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRunningOnLocalTransportMode(){
        return System.getProperty(CarbonConstants.LOCAL_TRANSPORT_MODE_ENABLED) != null;
    }

    /**
     * Reads the AAR services dir from the Axis config. if it is null, returns the default value
     * used in Carbon
     *
     * @param axisConfig - AxisConfiguration instance
     * @return - services dir name
     */
    public static String getAxis2ServicesDir(AxisConfiguration axisConfig) {
        String servicesDir = "axis2services";
        String serviceDirPara = (String)
                axisConfig.getParameterValue(DeploymentConstants.SERVICE_DIR_PATH);
        if (serviceDirPara != null) {
            servicesDir = serviceDirPara;
        }
        return servicesDir;
    }

     /**
     * This is a utility method which can be used to set security headers in a service client. This method
     * will create authorization header according to basic security protocol. i.e. encodeBase64(username:password)
     * and put it in a HTTP header with name "Authorization".
     * @param userName User calling the service.
     * @param password Password of the user.
     * @param rememberMe <code>true</code> if UI asks to persist remember me cookie.
     * @param serviceClient The service client used in the communication.
     */
    public static void setBasicAccessSecurityHeaders(String userName, String password, boolean rememberMe,
                                                     ServiceClient serviceClient) {

        String userNamePassword = userName + ":" + password;
        String encodedString = Base64Utils.encode(userNamePassword.getBytes());

        String authorizationHeader = "Basic " + encodedString;

        List<Header> headers = new ArrayList<Header>();

        Header authHeader = new Header("Authorization", authorizationHeader);
        headers.add(authHeader);

        if (rememberMe) {
            Header rememberMeHeader = new Header("RememberMe", TRUE);
            headers.add(rememberMeHeader);
        }
      
        serviceClient.getOptions().setProperty(HTTPConstants.HTTP_HEADERS, headers);
    }

    /**
     * This is a utility method which can be used to set security headers in the message context. This method
     * will create authorization header according to basic security protocol. i.e. encodeBase64(username:password)
     * and put it in a HTTP header with name "Authorization".
     * @param userName User calling the service.
     * @param password Password of the user.
     * @param rememberMe <code>true</code> if UI asks to persist remember me cookie.
     * @param msgContext The MessageContext of the message.
     */

    public static void setBasicAccessSecurityHeaders(String userName, String password, boolean rememberMe,
                                                     MessageContext msgContext) throws AxisFault {

        String userNamePassword = userName + ":" + password;
        String encodedString = Base64Utils.encode(userNamePassword.getBytes());

        String authorizationHeader = "Basic " + encodedString;

        List<Header> headers = new ArrayList<Header>();

        Header authHeader = new Header("Authorization", authorizationHeader);
        headers.add(authHeader);

        if (rememberMe) {
            Header rememberMeHeader = new Header("RememberMe", TRUE);
            headers.add(rememberMeHeader);
        }

        msgContext.getOptions().setProperty(HTTPConstants.HTTP_HEADERS, headers);
    }

     /**
     * This is a utility method which can be used to set security headers in a service client. This method
     * will create authorization header according to basic security protocol. i.e. encodeBase64(username:password)
     * and put it in a HTTP header with name "Authorization".
     * @param userName User calling the service.
     * @param password Password of the user.
     * @param serviceClient The service client used in the communication.
     */
    public static void setBasicAccessSecurityHeaders(String userName, String password, ServiceClient serviceClient) {

        setBasicAccessSecurityHeaders(userName, password, false, serviceClient);
    }

    /**
     * Adds the cApp deployer config into the original DeployerConfig array..
     * This is to fix : https://wso2.org/jira/browse/CARBON-13598
     *
     * @param originalConfigs - original DeployerConfig array
     * @param axisConfig - AxisConfiguration instance to get tenant id
     * @return - new array which includes cApp deployer
     */
    public static DeployerConfig[] addCappDeployer(DeployerConfig[] originalConfigs,
                                                   AxisConfiguration axisConfig) {
        // create a new deployer config for cApps
        DeployerConfig appDeployerConfig = new DeployerConfig();
        // set cApp deployer class, car extension
        appDeployerConfig.setClassStr("org.wso2.carbon.application.deployer.CappAxis2Deployer");
        appDeployerConfig.setExtension("car");
        // setting the deployment direcotry as CARBON_HOME/repository/carbonapps/<tenant>
        String appsRepo = CarbonUtils.getCAppDeploymentDirPath(axisConfig);
        appDeployerConfig.setDirectory(appsRepo);

        // create a new configs array by including the dep deployer cofig
        DeployerConfig[] newDepConfigs = new DeployerConfig[originalConfigs.length + 1];
        System.arraycopy(originalConfigs, 0, newDepConfigs, 0, originalConfigs.length);
        newDepConfigs[newDepConfigs.length - 1] = appDeployerConfig;
        return newDepConfigs;
    }

    /**
     * This is method returns deployment dir for carbon apps for current tenant.
     *
     * @param axisConfig - AxisConfiguration instance of the current tenant
     * @return - absolute path to tenant capp deployment space
     */
    public static String getCAppDeploymentDirPath(AxisConfiguration axisConfig) {
        return axisConfig.getRepository().getPath() + File.separator + "carbonapps";
//        String tenantCAppDirPath = null;
//        String cAppDirPath = System.getProperty("carbon.home") + File.separator +
//                             "repository" + File.separator + "carbonapps";
//        CarbonContext carbonContext = PrivilegedCarbonContext
//                .getCurrentContext(axisConfig);
//        if (carbonContext != null) {
//            int tenantId = carbonContext.getTenantId();
//            // use 0 for super tenant
//            if (tenantId == -1 || tenantId == MultitenantConstants.SUPER_TENANT_ID) {
//                tenantId = 0;
//            }
//            String tmpPath = cAppDirPath +  File.separator + tenantId;
//            if (createDir(tmpPath)) {
//                tenantCAppDirPath = tmpPath;
//            }
//        }
//        return tenantCAppDirPath;
    }

    /**
     *
     * @param xmlConfiguration InputStream that carries xml configuration
     * @return returns a InputStream that has evaluated system variables in input
     * @throws CarbonException
     */
    public static InputStream replaceSystemVariablesInXml(InputStream xmlConfiguration) throws CarbonException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(xmlConfiguration);
        } catch (Exception e) {
            throw new CarbonException("Error in building Document", e);
        }
        NodeList nodeList = null;
        if (doc != null) {
            nodeList = doc.getElementsByTagName("*");
        }
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                resolveLeafNodeValue(nodeList.item(i));
            }
        }
        return toInputStream(doc);
    }

    public static void resolveLeafNodeValue(Node node) {
        if (node != null) {
            Element element = (Element) node;
            NodeList childNodeList = element.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                Node chileNode = childNodeList.item(j);
                if (!chileNode.hasChildNodes()) {
                    String nodeValue = resolveSystemProperty(chileNode.getTextContent());
                    childNodeList.item(j).setTextContent(nodeValue);
                } else {
                    resolveLeafNodeValue(chileNode);
                }
            }
        }
    }

    public static String replaceSystemVariablesInXml(String xmlConfiguration) throws CarbonException {
        InputStream in = replaceSystemVariablesInXml(new ByteArrayInputStream(xmlConfiguration.getBytes()));
        try {
            xmlConfiguration = IOUtils.toString(in);
        } catch (IOException e) {
            throw new CarbonException("Error in converting InputStream to String");
        }
        return xmlConfiguration;
    }


    public static String resolveSystemProperty(String text) {
		int indexOfStartingChars = -1;
		int indexOfClosingBrace;

		// The following condition deals with properties.
		// Properties are specified as ${system.property},
		// and are assumed to be System properties
		while (indexOfStartingChars < text.indexOf("${")
				&& (indexOfStartingChars = text.indexOf("${")) != -1
				&& (indexOfClosingBrace = text.indexOf('}')) != -1) { // Is a
																		// property
																		// used?
			String sysProp = text.substring(indexOfStartingChars + 2,
					indexOfClosingBrace);
			String propValue = System.getProperty(sysProp);
			if (propValue != null) {
				text = text.substring(0, indexOfStartingChars) + propValue
						+ text.substring(indexOfClosingBrace + 1);
			}
			if (sysProp.equals("carbon.home") && propValue != null
					&& propValue.equals(".")) {

				text = new File(".").getAbsolutePath() + File.separator + text;

			}
		}
		return text;
	}

    /**
     *
     * @param doc  the DOM.Document to be converted to InputStream.
     * @return Returns InputStream.
     * @throws CarbonException
     */
	public static InputStream toInputStream(Document doc) throws CarbonException {
        InputStream in;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(doc);
            Result result = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, result);
            in = new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerException e) {
            throw new CarbonException("Error in transforming DOM to InputStream", e);
        }
        return in;
    }
	

    private static final boolean isWorkerNode = Boolean.parseBoolean(System.getProperty("workerNode"));

    /**
     * Check if the current server is a worker node
     * In clustering setup some servers are run as worker nodes and some as management nodes
     * @return
     */
    public static boolean isWorkerNode() {
        return isWorkerNode;
    }

    private static final boolean isOptimized = Boolean.parseBoolean(System.getProperty("optimize"));

   /**
    * Check if the current server is optimized or not.
    * This mode is more suitable to run Carbon kernel on resource constrained environments.
    * @return isOptimized
    */
     public static boolean isOptimized() {
        return isOptimized;
     }

    /**
     * utility method to check whether deployment synchronizer enabled.
     * @return
     */
    public static boolean isDepSyncEnabled() {
        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        String value = serverConfiguration.getFirstProperty("DeploymentSynchronizer.Enabled");
        if(JavaUtils.isTrueExplicitly(value)) {
            return true;
        }
        return false;
    }

    public static String getGhostMetafileDir(AxisConfiguration axisConfig) {
        String ghostMetaFileDirPath = axisConfig.getRepository().getPath() +
                                         File.separator + "ghostmetafiles";
        File ghostMetaFileDir = new File(ghostMetaFileDirPath);
        if (!ghostMetaFileDir.exists() && !ghostMetaFileDir.mkdir()) {
            log.error("Error while creating tenant temporary directory at : " + ghostMetaFileDirPath);
            return null;
        }
        return ghostMetaFileDir.getPath();
    }


	/**
	 * Returns the proxy context path value specified in the carbon.xml.(Duplicated Util Method)
	 *
	 * @param isWorkerNode If isWorkerNode is true then this method returns the proxy context path of the
	 *                     corresponding worker node. If the worker proxy context path is not specified, this method
	 *                     returns the value specified for the proxy context path.
	 * @return the proxy context path value.
	 */
	public static String getProxyContextPath(boolean isWorkerNode) {
		String proxyContextPath = "";

		if (isWorkerNode) {
			proxyContextPath = getProxyContextPathValue(ServerConstants.WORKER_PROXY_CONTEXT_PATH);
			if ("".equals(proxyContextPath)) {
				proxyContextPath = getProxyContextPathValue(ServerConstants.PROXY_CONTEXT_PATH);
			}
		} else {
			proxyContextPath = getProxyContextPathValue(ServerConstants.PROXY_CONTEXT_PATH);
		}

		if(log.isDebugEnabled()){
			log.debug("Proxy context path : " + proxyContextPath);
		}
		return proxyContextPath;
	}

    /**
     * @param className name of the class to build the deployer
     * @return Deployer
     */

    public static Deployer getDeployer(String className) throws Exception {
        Deployer deployer;
        try {
            Class deployerClass = Class.forName(className);
            deployer = (Deployer) deployerClass.newInstance();

        } catch (ClassNotFoundException e) {
            String msg = "Deployer class not found ";
            log.error(msg, e);
            throw new Exception(msg, e);
        } catch (InstantiationException e) {
            String msg = "Cannot create new deployer instance";
            log.error(msg, e);
            throw new Exception(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Error creating deployer";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        return deployer;
    }

	/**
	 * Retrieves the proxy context path from the ServerConfiguration and process it before returning. (Duplicated Util Method)
	 *
	 * @param key Property key
	 * @return the processed proxy context path.
	 */
	private static String getProxyContextPathValue(String key) {
		String proxyContextPath = ServerConfiguration.getInstance().getFirstProperty(key);

		if (proxyContextPath == null || proxyContextPath.length() == 0 | "/".equals(proxyContextPath)) {
			proxyContextPath = "";
		} else {
			proxyContextPath = proxyContextPath.trim();
			if (!proxyContextPath.startsWith("/")) {
				proxyContextPath = "/" + proxyContextPath;
			}
		}
		return proxyContextPath;
	}
}
