/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.utils.deployment;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of utility methods which are used by the Ghost Deployer
 */
public class GhostDeployerUtils {

    private static final String PARAMETER_VALUE_TRUE = "true";

	private static Log log = LogFactory.getLog(GhostDeployerUtils.class);

    public static final String GHOST_DEPLOYMENT = "GhostDeployment";
    public static final String ENABLED = GHOST_DEPLOYMENT + ".Enabled";
    public static final String PARTIAL_UPDATE_MODE = GHOST_DEPLOYMENT + ".PartialUpdate";

    // Map of ghost services which are currently being converted into actual services
    private static final String TRANSIT_GHOST_SERVICE_MAP = "TransitGhostServiceMap";

    private static final String SERVICE_ARCHIVE_FILE_NAME = "ServiceArchiveFileName";
    
    private GhostDeployerUtils() {
        //disable external instantiation
    }

    /**
     * Removes the given ghost service and deploys the actual service. Service file name is
     * extracted from the Ghost Service and then the actual service is deployed. Finally the
     * actual service is returned.
     *
     * @param axisConfig - AxisConfiguration instance
     * @param ghostService - Existing Ghost service
     * @return - newly deployed real service
     * @throws org.apache.axis2.AxisFault - On errors while removing existing service
     */
    public static AxisService deployActualService(AxisConfiguration axisConfig,
                                                  AxisService ghostService) throws AxisFault {
        AxisService newService = null;
        /**
         * There can be multiple requests for the same ghost service depending on the level
         * of concurrency. Therefore we have to synchronize on the ghost service instance.
         */
        String serviceName = ghostService.getName();
        synchronized (serviceName.intern()) {
            // there can be situations in which the actual service is already deployed and
            // available in the axisConfig
            AxisService axisConfigService = axisConfig.getService(serviceName);
            if (axisConfigService == null) {
                return null;
            }
            Parameter actualGhostParam = axisConfigService
                    .getParameter(CarbonConstants.GHOST_SERVICE_PARAM);
            if (actualGhostParam == null || "false".equals(actualGhostParam.getValue())) {
                // if the service from axisConfig is not a ghost, return it
                newService = axisConfigService;
            } else {
                GhostDeployer ghostDeployer = getGhostDeployer(axisConfig);
                if (ghostDeployer == null) {
                    return null;
                }
                DeploymentFileData dfd = ghostDeployer.getFileData(axisConfigService
                        .getFileName().getPath());
                if (dfd != null) {
                    // remove the existing service
                    log.info("Removing Ghost Service and loading actual service : " + serviceName);
                    AxisServiceGroup existingSG = (AxisServiceGroup) axisConfigService.getParent();
                    existingSG.addParameter(CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, PARAMETER_VALUE_TRUE);

                    // Add all services in the group to the ghost list
                    Map<String, AxisService> transitGhostList =
                            getTransitGhostServicesMap(axisConfig);
                    for (Iterator<AxisService> servicesItr = existingSG.getServices() ;
                         servicesItr.hasNext() ;) {
                        AxisService service = servicesItr.next();
                        transitGhostList.put(service.getName(), service);
                    }

                    String serviceGroupName = existingSG.getServiceGroupName();
                    if (axisConfig.getServiceGroup(serviceGroupName) != null) {
                        axisConfig.removeServiceGroup(serviceGroupName);
                    }

                    if (axisConfig.getService(serviceName) != null) {
                        axisConfig.removeService(serviceName);
                    }
                    // deploy the new service
                    dfd.deploy();
                    newService = axisConfig.getService(serviceName);

                    // Remove all services in the new group from the ghost list
                    AxisServiceGroup newSG = (AxisServiceGroup) newService.getParent();
                    for (Iterator<AxisService> servicesItr = newSG.getServices() ;
                         servicesItr.hasNext() ;) {
                        AxisService service = servicesItr.next();
                        transitGhostList.remove(service.getName());
                    }
                }
            }
            updateLastUsedTime(newService);
        }
        return newService;
    }

    /**
     * Updates the last used timestamp of the given service. A new Paramter is created if it
     * doesn't already exists..
     *
     * @param service - AxisService instance
     */
    public static void updateLastUsedTime(AxisService service) {
        if (service == null) {
            return;
        }
        try {
            Parameter lastUsageParam = service
                    .getParameter(CarbonConstants.SERVICE_LAST_USED_TIME);
            if (lastUsageParam == null) {
                lastUsageParam = new Parameter();
                lastUsageParam.setName(CarbonConstants.SERVICE_LAST_USED_TIME);
                service.addParameter(lastUsageParam);
            }
            lastUsageParam.setValue(System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Error while updating " + CarbonConstants.SERVICE_LAST_USED_TIME +
                    " parameter in service : " + service.getName(), e);
        }
    }

    /**
     * Get GhostDeployer which is stored as a parameter in the AxisConfiguration
     *
     * @param axisConfig - AxisConfiguration instance
     * @return - GhostDeployer instance if found
     */
    public static GhostDeployer getGhostDeployer(AxisConfiguration axisConfig) {
        GhostDeployer ghostDeployer = null;
        Parameter param = axisConfig.getParameter(CarbonConstants.GHOST_DEPLOYER);
        if (param != null) {
            return (GhostDeployer) param.getValue();
        }
        return ghostDeployer;
    }

    /**
     * Read the "Enabled" property under "GhostDeployment" config from the carbon.xml and return the boolean value
     *
     * @return - true if the property is set to true. otherwise false
     */
    public static boolean isGhostOn() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String ghostOn = serverConfig.getFirstProperty(ENABLED);
        if (ghostOn != null && Boolean.parseBoolean(ghostOn)) {
            return true;
        }
        return false;
    }

    /**
     * Read the "PartialUpdate" property under "GhostDeployment" config from the carbon.xml and return the boolean value
     *
     * @return - true if the property is set to true. otherwise false
     */
    public static boolean isPartialUpdateEnabled() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String partialUpdateOn = serverConfig.getFirstProperty(PARTIAL_UPDATE_MODE);
        if (partialUpdateOn != null && Boolean.parseBoolean(partialUpdateOn)) {
            return true;
        }
        return false;
    }

    /**
     * Get the map of services which are in transit. A particular service will be in this
     * map from the time the ghost service is removed from AxisConfig and the actual service is
     * deployed..
     *
     * @param axisConfig - AxisConfiguration instance
     * @return returns a map of strings which are the names of the services
     * @throws org.apache.axis2.AxisFault - on error while getting or setting map
     */
    public static synchronized Map<String, AxisService> getTransitGhostServicesMap(
            AxisConfiguration axisConfig) throws AxisFault {
        Parameter param = axisConfig.getParameter(TRANSIT_GHOST_SERVICE_MAP);
        Map<String, AxisService> transitMap = null;
        if (param != null) {
            transitMap = (Map<String, AxisService>) param.getValue();
        }
        if (transitMap == null) {
            transitMap = new HashMap<String, AxisService>();
            axisConfig.addParameter(TRANSIT_GHOST_SERVICE_MAP, transitMap);
        }
        return transitMap;
    }

    /**
     * When a ghost service is removed from the axisConfig and the corresponding actual service is
     * deployed, there's a time interval in which there's no AxisService in the AxisConfiguration
     * for the particular service name. Within this interval, if a request comes in to the service,
     * we have to somehow dispatch the service.
     * Within the above mentioned time interval, service is kept in a map. This method checks
     * whether the relevant service is available in that map and if it is found, returns the
     * name of the service.
     * IMPORTANT : This method assumes that the service name can be figured out using the To EPR of
     * the MessageContext. But it's only valid for HTTP/S transport. For other transports like
     * JMS, this won't work.
     *
     * @param msgCtx - MessageContext for the current request
     * @return - Service  if found, else null
     * @throws AxisFault - on errors while calling Axis2 APIs
     */
    public static AxisService dispatchServiceFromTransitGhosts(MessageContext msgCtx) throws AxisFault {
        AxisService actualService = null;
        // get the map of ghost services which are being redeployed..
        Map<String, AxisService> transitGhostMap = getTransitGhostServicesMap(msgCtx
                .getConfigurationContext().getAxisConfiguration());

        EndpointReference toEPR = msgCtx.getTo();
        if (toEPR != null) {
            String filePart = toEPR.getAddress();

            // Get the service/operation part from the request URL
            String serviceOpPart = Utils.getServiceAndOperationPart(filePart,
                    msgCtx.getConfigurationContext().getServiceContextPath());

            if (serviceOpPart != null) {
                // First remove anything after .
                int index = serviceOpPart.indexOf('.');
                if (index != -1) {
                    serviceOpPart = serviceOpPart.substring(0, index);
                }
                /**
                 * Split the serviceOpPart from '/' and add part by part and check whether we have
                 * a service. This is because we are supporting hierarchical services. We can't
                 * decide the service name just by looking at the request URL.
                 */
                String[] parts = serviceOpPart.split("/");
                String tmpServiceName = "";
                int count = 0;

                /**
                 * To avoid performance issues if an incorrect URL comes in with a long service name
                 * including lots of '/' separated strings, we limit the hierarchical depth to 10
                 */
                while (actualService == null && count < parts.length &&
                        count < Constants.MAX_HIERARCHICAL_DEPTH) {
                    tmpServiceName = count == 0 ? tmpServiceName + parts[count] :
                            tmpServiceName + "/" + parts[count];
                    if (transitGhostMap.containsKey(tmpServiceName)) {
                        actualService = transitGhostMap.get(tmpServiceName);
                    }
                    count++;
                }
            }
        }
        return actualService;
    }

    /**
     * When a ghost service is removed from the AxisConfiguration, name of that service is kept
     * temporary in a map. This method waits until the provided service name is removed from that
     * map. In other words, it waits until the actual service is deployed. After the actual
     * service is deployed, it is safe to forward the request further..
     *
     * @param serviceName - name of the service
     * @param axisConfig - current axisConfig instance
     * @throws AxisFault - on errors while reading ghost map
     */
    public static void waitForServiceToLeaveTransit(String serviceName, AxisConfiguration axisConfig)
            throws AxisFault {
        Map<String, AxisService> transitGhostMap = getTransitGhostServicesMap(axisConfig);
        while (transitGhostMap.containsKey(serviceName)) {
            // wait until the service is removed from ghost map
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                // ignored
            }
        }
    }

    /**
     * Checks if the given Axis service is a ghost service.
     *
     * @param axisService - The AxisService object to be checked if it's a ghost service
     * @return - true if the service is a ghost service
     */
    public static boolean isGhostService(AxisService axisService) {
        if (axisService == null) {
            return false;
        }
        Parameter ghostParam = axisService.getParameter(CarbonConstants.GHOST_SERVICE_PARAM);
        return ghostParam != null && PARAMETER_VALUE_TRUE.equals(ghostParam.getValue());
    }

    /**
     * Creates the Ghost ServiceGroup by reading the ghost metadata file. The created object will
     * contain the basic metadata of the service group which are enough to dispatch a request.
     *
     * @param axisConfig - AxisConfiguration instance
     * @param ghostFile - Ghost metadata file
     * @param originalFile - URL of the original service artifact
     * @return - AxisServiceGroup object which is created
     */
    public static AxisServiceGroup createGhostServiceGroup(AxisConfiguration axisConfig,
                                                    File ghostFile, URL originalFile) {
        OMElement serviceGroupElm;
        AxisServiceGroup ghostGroup;
        synchronized (axisConfig) {
            try {
                InputStream xmlInputStream = new FileInputStream(ghostFile);
                serviceGroupElm = new StAXOMBuilder(xmlInputStream).getDocumentElement();
            } catch (Exception e) {
                log.error("Error while parsing ghost XML file : " + ghostFile.getAbsolutePath());
                return null;
            }
            // create service group
            ghostGroup = new AxisServiceGroup(axisConfig);
            ghostGroup.setServiceGroupName(serviceGroupElm
                    .getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_NAME)));

            try {
                // create services
                for (Iterator itr = serviceGroupElm
                        .getChildrenWithLocalName(CarbonConstants.GHOST_SERVICE); itr.hasNext();) {
                    OMElement serviceElm = (OMElement) itr.next();
                    AxisService ghostService = new AxisService(serviceElm
                            .getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_NAME)));
                    ghostService.addParameter(new Parameter(CarbonConstants
                            .GHOST_SERVICE_PARAM, PARAMETER_VALUE_TRUE));
                    // set the service type
                    String serviceType = serviceElm
                            .getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_SERVICE_TYPE));
                    if (serviceType != null) {
                        ghostService.addParameter(new Parameter(ServerConstants.SERVICE_TYPE,
                                serviceType));
                    }
                    // set security scenario
                    String secScenario = serviceElm
                            .getAttributeValue(new QName(CarbonConstants.
                                                                 GHOST_ATTR_SECURITY_SCENARIO));
                    if (secScenario != null) {
                        ghostService.addParameter(new Parameter(CarbonConstants
                                .GHOST_ATTR_SECURITY_SCENARIO, secScenario));
                    }
                    if (originalFile != null) {
                        ghostService.setFileName(originalFile);
                    } else {
                        ghostService.setFileName(new URL("file:" +
                                                         serviceGroupElm.getAttributeValue
                                                           (new QName(SERVICE_ARCHIVE_FILE_NAME))));
                    }

                    // Add operations to ghost service
                    OMElement operationsElm = serviceElm
                            .getFirstChildWithName(new QName(CarbonConstants.
                                                                     GHOST_SERVICE_OPERATIONS));
                    if (operationsElm != null) {
                        for (Iterator operationItr = operationsElm.getChildren();
                             operationItr.hasNext();) {
                            OMElement opElm = (OMElement) operationItr.next();
                            AxisOperation newOp = AxisOperationFactory.getOperationDescription(opElm
                                    .getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_MEP)));
                            String ns = opElm.getNamespace() == null ? "" :
                                    opElm.getNamespace().getNamespaceURI();
                            newOp.setName(new QName(ns, opElm.getLocalName()));
                            ghostService.addOperation(newOp);
                        }
                    }

                    // Add endpoints to ghost service
                    OMElement endpointsElm = serviceElm
                            .getFirstChildWithName(new QName(CarbonConstants.
                                                                     GHOST_SERVICE_ENDPOINTS));
                    if (endpointsElm != null) {
                        for (Iterator endpointItr = endpointsElm.getChildren();
                             endpointItr.hasNext();) {
                            OMElement epElm = (OMElement) endpointItr.next();
                            AxisEndpoint axisEndpoint = new AxisEndpoint();
                            // set a dummy binding to the endpoint
                            axisEndpoint.setBinding(new AxisBinding());
                            axisEndpoint.setName(epElm.getLocalName());
                            ghostService.addEndpoint(epElm.getLocalName(), axisEndpoint);
                        }
                    }
                    if (axisConfig.getService(ghostService.getName()) == null) {
                        ghostGroup.addService(ghostService);
                    }
                }
                ghostGroup.addParameter(new Parameter(CarbonConstants.
                                                        GHOST_SERVICE_PARAM, PARAMETER_VALUE_TRUE));
            } catch (Exception e) {
                log.error("Error while creating Ghost Service from Ghost File : " +
                        ghostFile.getAbsolutePath(), e);
            }
        }
        return ghostGroup;
    }

    /**
     * Creates the ghost file for the current service group. Basically this ghost file contains
     * basic metadata about the service group. Service name, operations, service type, MEP and
     * all endpoints are stored for each service found under the given service group.
     *
     * @param serviceGroup - Service group to be serialized
     * @param axisConfig - AxisConfiguration instance
     * @param dfd - Absolute path to the service artifact
     */
    public static void serializeServiceGroup(AxisServiceGroup serviceGroup,
                                             AxisConfiguration axisConfig, DeploymentFileData dfd) {
        String servicePath = dfd.getAbsolutePath();
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        // first create service group element
        OMElement serviceGroupEle = omFactory
                .createOMElement(new QName(CarbonConstants.GHOST_SERVICE_GROUP));
        serviceGroupEle.addAttribute(CarbonConstants.GHOST_ATTR_NAME,
                serviceGroup.getServiceGroupName(), null);

        serviceGroupEle.addAttribute(SERVICE_ARCHIVE_FILE_NAME, dfd.getFile().getName(), null);

        // we have to serialize all services
        for (Iterator<AxisService> services = serviceGroup.getServices(); services.hasNext();) {
            AxisService service = services.next();
            // service element
            OMElement serviceEle = omFactory
                    .createOMElement(new QName(CarbonConstants.GHOST_SERVICE));
            serviceGroupEle.addChild(serviceEle);
            serviceEle.addAttribute(CarbonConstants.GHOST_ATTR_NAME, service.getName(), null);

            // get the service type
            String serviceType = null;
            Parameter serviceTypeParam = service.getParameter(ServerConstants.SERVICE_TYPE);
            if (serviceTypeParam != null) {
                serviceType = (String) serviceTypeParam.getValue();
            }
            if (serviceType != null) {
                serviceEle.addAttribute(CarbonConstants.GHOST_ATTR_SERVICE_TYPE, serviceType, null);
            }

            // operations
            OMElement operations = omFactory
                    .createOMElement(new QName(CarbonConstants.GHOST_SERVICE_OPERATIONS));
            serviceEle.addChild(operations);
            for (Iterator<AxisOperation> ops = service.getOperations(); ops.hasNext();) {
                AxisOperation axisOperation = ops.next();
                OMElement opElement = omFactory.createOMElement(axisOperation.getName());
                opElement.addAttribute(CarbonConstants.GHOST_ATTR_MEP,
                        axisOperation.getMessageExchangePattern(), null);
                operations.addChild(opElement);
            }

            // endpoints
            OMElement endpoints = omFactory
                    .createOMElement(new QName(CarbonConstants.GHOST_SERVICE_ENDPOINTS));
            serviceEle.addChild(endpoints);
            for (AxisEndpoint endpoint : service.getEndpoints().values()) {
                endpoints.addChild(omFactory.createOMElement(new QName(endpoint.getName())));
            }
        }

        // Now create a ghostFile and serialize the created OMElement
        String ghostMetafileDirPath = CarbonUtils.getGhostMetafileDir(axisConfig);
        if (ghostMetafileDirPath == null) {
            return;
        }
        String ghostPath = ghostMetafileDirPath +
                File.separator + CarbonConstants.GHOST_SERVICES_FOLDER;
        File ghostFolder = new File(ghostPath);
        if (!ghostFolder.exists() && !ghostFolder.mkdir()) {
            log.error("Error while creating ghostServices folder at : " + ghostPath);
            return;
        }

        FileOutputStream fos = null;
        try {
            File axisConfigRepoPathUrlToFile = new File(axisConfig.getRepository().getPath());
            //Add file seperator at the end of absolute path, to avoid getting an underscore in the beginning of
            // ghost file name.
            String axisConfigRepoPath = axisConfigRepoPathUrlToFile.getAbsolutePath().concat(File.separator);
            String axisConfigRepoPathToUnix = GhostDeployer.separatorsToUnix(axisConfigRepoPath);
            String ghostFileName = calculateGhostFileName(servicePath, axisConfigRepoPathToUnix);

            if (ghostFileName == null) {
                log.error("Ghost file name is null. Actual service path : " + servicePath);
                return;
            }
            File serviceFile = new File(ghostPath + File.separator + ghostFileName);
            fos = new FileOutputStream(serviceFile);
            serviceGroupEle.serialize(fos);
            fos.flush();
        } catch (Exception e) {
            log.error("Error while serializing OMElement for Ghost Service", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Error while closing the file output stream", e);
                }
            }
        }
    }

    /**
     * Calculate the ghost file name using the original file name. This should be unique across
     * all service types and hierarchical services.
     *
     * @param fileName - original file name
     * @param repoPath - path of the axis2 repository
     * @return - derived ghost file name
     */
    public static String calculateGhostFileName(String fileName, String repoPath) {
        String ghostFileName = null;
        //since in Windows env, filename & repopath get two formats.
        fileName = GhostDeployer.separatorsToUnix(fileName);
        if (fileName != null && fileName.startsWith(repoPath)) {
            // first drop the repo path
            ghostFileName = fileName.substring(repoPath.length());
            // then remove the extension
            if (ghostFileName.lastIndexOf('.') != -1) {
                ghostFileName = ghostFileName.substring(0, ghostFileName.lastIndexOf('.'));
            }
            // adjust the path for windows..
            if (File.separatorChar == '\\') {
                ghostFileName = ghostFileName.replace('\\', '/');
            }
            // replace '/' with '_'
            ghostFileName = ghostFileName.replace('/', '_');
            // ghost file is always an XML
            ghostFileName += ".xml";
        }
        return ghostFileName;
    }

    /**
     * Creates a File instance for the ghost service according to the original file name..
     *
     * @param fileName - original service file name
     * @param axisConfig - AxisConfiguration instance
     * @return - File instance created
     */
    public static File getGhostFile(String fileName, AxisConfiguration axisConfig) {
        String ghostMetafilesDirPath = CarbonUtils.getGhostMetafileDir(axisConfig);
        File axisConfigRepoPathUrlToFile = new File(axisConfig.getRepository().getPath());
        //Add file seperator at the end of absolute path, to avoid getting an underscore in the beginning of
        // ghost file name.
        String axisConfigRepoPath = axisConfigRepoPathUrlToFile.getAbsolutePath().concat(File.separator);
        //since in Windows env, filename & repopath get two formats
        String axisConfigRepoPathToUnix = GhostDeployer.separatorsToUnix(axisConfigRepoPath);
        String ghostFileName = calculateGhostFileName(fileName, axisConfigRepoPathToUnix);

        if (ghostMetafilesDirPath != null && ghostFileName != null) {
            return new File(ghostMetafilesDirPath + File.separator +
                    CarbonConstants.GHOST_SERVICES_FOLDER + File.separator +
                    ghostFileName);
        }
        return null;
    }

    /**
     * Adds the given service group to the transit map
     *
     * @param serviceGroup - to be added to transit map
     * @param axisConfig - current axis configuration
     * @throws AxisFault - on error
     */
    public static void addServiceGroupToTransitMap(AxisServiceGroup serviceGroup,
                                            AxisConfiguration axisConfig)
            throws AxisFault {
        Map<String, AxisService> transitGhostList =
                getTransitGhostServicesMap(axisConfig);
        for (Iterator<AxisService> servicesItr = serviceGroup.getServices();
             servicesItr.hasNext(); ) {
            AxisService service = servicesItr.next();
            transitGhostList.put(service.getName(), service);
        }
    }

    /**
     * Removes given service group from transit map
     *
     * @param serviceGroup - to be removed from transit map
     * @param axisConfig - current axis configuration
     * @throws AxisFault - on error
     */
    public static void removeServiceGroupFromTransitMap(AxisServiceGroup serviceGroup,
                                                 AxisConfiguration axisConfig)
            throws AxisFault {
        Map<String, AxisService> transitGhostList =
                getTransitGhostServicesMap(axisConfig);
        for (Iterator<AxisService> servicesItr = serviceGroup.getServices();
             servicesItr.hasNext(); ) {
            AxisService service = servicesItr.next();
            transitGhostList.remove(service.getName());
        }
    }

    public static void deployGhostArtifacts(AxisConfiguration axisConfig)
            throws DeploymentException {
        // load the ghost service group
        File[] ghostMetaArtifacts;
        File ghostMetafilesDir = new File(CarbonUtils.getGhostMetafileDir(axisConfig) + File.separator +
                                          CarbonConstants.GHOST_SERVICES_FOLDER);
        boolean doDeploy = true;
        if (ghostMetafilesDir.exists()) {
            ghostMetaArtifacts = ghostMetafilesDir.listFiles();
        } else {
            return;
        }

        for (File ghostFile : ghostMetaArtifacts) {
            if (!ghostFile.getPath().endsWith(".svn")) {
                try {
                    AxisServiceGroup ghostServiceGroup = GhostDeployerUtils.createGhostServiceGroup(
                            axisConfig, ghostFile, null);
                    Map<String, AxisService> transitGhostList =
                            getTransitGhostServicesMap(axisConfig);
                    if (axisConfig.getServiceGroup(ghostServiceGroup.getServiceGroupName()) == null) {

                        for (AxisService service : transitGhostList.values()) {
                            if (ghostServiceGroup.getService(service.getName()) != null) {
                                doDeploy = false;
                                break;
                            }
                        }
                        if (doDeploy) {
                            for (Iterator<AxisService> servicesItr = ghostServiceGroup.getServices();
                                 servicesItr.hasNext(); ) {
                                AxisService axisService = servicesItr.next();
                                synchronized (axisService.getName().intern()) {
                                    if (axisConfig.getService(axisService.getName()) != null) {
                                        doDeploy = false;
                                        break;
                                    }
                                }
                            }
                            if (doDeploy && ghostServiceGroup.getServices().hasNext() &&
                                axisConfig.getServiceGroup(ghostServiceGroup.
                                        getServiceGroupName()) == null) {
                                log.info("Adding Ghost service group : " +
                                         ghostServiceGroup.getServiceGroupName());
                                axisConfig.addServiceGroup(ghostServiceGroup);
                            }
                        }
                    }
                } catch (Exception e) {
                    String msg = "Error while loading the Ghost Service : " +
                                 ghostFile.getAbsolutePath();
                    log.error(msg, e);
                    throw new DeploymentException(msg, e);
                }
            }
        }
    }
}
