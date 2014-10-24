/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.application.deployer.persistence;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.config.RegistryConfig;
import org.wso2.carbon.application.deployer.internal.AppDeployerServiceComponent;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.synchronization.RegistrySynchronizer;
import org.wso2.carbon.roles.mgt.ServerRoleConstants;
import org.wso2.carbon.roles.mgt.ServerRoleUtils;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;


public class CarbonAppPersistenceManager {

    private AxisConfiguration axisConfig;
    private Registry localRegistry;
    private Registry configRegistry;
    private Registry governanceRegistry;
    private Registry rootRegistry;

    private static final Log log = LogFactory.getLog(CarbonAppPersistenceManager.class);

    public CarbonAppPersistenceManager(AxisConfiguration axisConfig) throws CarbonException {
        this.axisConfig = axisConfig;
        try {
            PrivilegedCarbonContext carbonCtx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            localRegistry = (Registry) carbonCtx.getRegistry(RegistryType.LOCAL_REPOSITORY);
            configRegistry = (Registry) carbonCtx.getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            governanceRegistry = (Registry) carbonCtx.getRegistry(RegistryType.SYSTEM_GOVERNANCE);

            // get the root registry for the current tenant from RegistryService
            rootRegistry = AppDeployerServiceComponent.getRegistryService().getRegistry(
                    CarbonConstants.REGISTRY_SYSTEM_USERNAME, carbonCtx.getTenantId());
        } catch (Exception e) {
            log.error("Error while retrieving config registry from Axis configuration", e);
        }
        if (configRegistry == null) {
            throw new CarbonException("Configuration Registry is not available");
        }
    }


    /**
     * Reads the hash value property of the given cApp from registry..
     *
     * @param appNameWithVersion - cApp name with version
     * @return - hash value of the capp artifact
     * @throws CarbonException -
     */
    public String getHashValue(String appNameWithVersion) throws CarbonException {
        try {
            String appResourcePath = AppDeployerConstants.APPLICATIONS + appNameWithVersion;
            //if the app exists in the configRegistry, read the property..
            if (configRegistry.resourceExists(appResourcePath)) {
                Resource app = configRegistry.get(appResourcePath);
                return app.getProperty(AppDeployerConstants.HASH_VALUE);
            }
        } catch (RegistryException e) {
            String msg = "Unable to read hash value of the Application : " + appNameWithVersion
                    + ". Registry transactions failed.";
            log.error(msg, e);
            throw new CarbonException(msg, e);
        }
        return null;
    }

    /**
     * Delete the specified cApp from registry if already exists
     *
     * @param appNameWithVersion - application name with  version
     * @throws Exception - on registry transaction error
     */
    public void deleteApplication(String appNameWithVersion) throws Exception {
        try {
            String appResourcePath = AppDeployerConstants.APPLICATIONS + appNameWithVersion;
            //if the app exists in the configRegistry, delete it..
            if (configRegistry.resourceExists(appResourcePath)) {
                configRegistry.delete(appResourcePath);
            }
        } catch (RegistryException e) {
            String msg = "Unable to delete the Application : " + appNameWithVersion
                    + ". Registry transactions failed.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    /**
     * Removes all registry collections, resources and associations introduced through this
     * Capp artifact.
     *
     * @param registryConfig - RegistryConfig instance
     */
    public void removeArtifactResources(RegistryConfig registryConfig) {
        try {
            // remove collections
            List<RegistryConfig.Collection> collections = registryConfig.getCollections();
            for (RegistryConfig.Collection col : collections) {
                Registry reg = getRegistryInstance(col.getRegistryType());
                if (reg != null && reg.resourceExists(col.getPath())) {
                    reg.delete(col.getPath());
                }
            }
            // remove dumps
            List<RegistryConfig.Dump> dumps = registryConfig.getDumps();
            for (RegistryConfig.Dump dump : dumps) {
                Registry reg = getRegistryInstance(dump.getRegistryType());
                if (reg != null && reg.resourceExists(dump.getPath())) {
                    reg.delete(dump.getPath());
                }
            }

            if (configRegistry.resourceExists(AppDeployerConstants.REG_PATH_MAPPING + registryConfig.getAppName())) {
                Resource pathMappingResource = configRegistry.
                        get(AppDeployerConstants.REG_PATH_MAPPING + registryConfig.getAppName());
                OMElement pathMappingElement = AXIOMUtil.stringToOM(new String((byte[])pathMappingResource.getContent()));

                // remove resources
                List<RegistryConfig.Resourse> resources = registryConfig.getResources();
                for (RegistryConfig.Resourse res : resources) {
                    String fileName = res.getFileName();
                    Registry reg = getRegistryInstance(res.getRegistryType());
                    String resourcePath = AppDeployerUtils.computeResourcePath(res.getPath(), fileName);
                    AXIOMXPath axiomxPath = new AXIOMXPath("//resource[@path='" + resourcePath + "']");
                    OMElement resourceElement = (OMElement) axiomxPath.selectSingleNode(pathMappingElement);
                    String actualResourcePath;
                    if (resourceElement != null) {
                        OMElement targetElement = resourceElement.getFirstChildWithName(
                                new QName(AppDeployerConstants.REG_PATH_MAPPING_RESOURCE_TARGET));
                        actualResourcePath = targetElement.getText();
                    } else {
                        actualResourcePath = resourcePath;
                    }

                    if (reg != null && reg.resourceExists(actualResourcePath)) {
                        reg.delete(actualResourcePath);
                    } else {
                        String mediaType = res.getMediaType();
                        if (mediaType == null) {
                            mediaType = MediaTypesUtils.getMediaType(fileName);
                        }
                        if (AppDeployerConstants.REG_GAR_MEDIATYPE.equals(mediaType)) {
                            String garName = fileName.substring(0, fileName.lastIndexOf("."));
                            String garMappingResourcePath = AppDeployerConstants.REG_GAR_PATH_MAPPING + garName;
                            if (configRegistry.resourceExists(garMappingResourcePath)) {
                                Resource garMappingResource = configRegistry.get(garMappingResourcePath);
                                OMElement garMappingElement = AXIOMUtil.
                                        stringToOM(new String((byte[]) garMappingResource.getContent()));
                                axiomxPath = new AXIOMXPath("//gar[@path='" + resourcePath + "']");
                                OMElement garElement = (OMElement) axiomxPath.selectSingleNode(garMappingElement);
                                Iterator<OMElement> garTargetElements = garElement.
                                        getChildrenWithLocalName(AppDeployerConstants.REG_GAR_PATH_MAPPING_RESOURCE_TARGET);
                                while (garTargetElements.hasNext()) {
                                    String targetPath = garTargetElements.next().getText();
                                    if (reg.resourceExists(targetPath)) {
                                        reg.delete(targetPath);
                                    }
                                }
                                configRegistry.delete(garMappingResourcePath);
                            }
                        }
                    }
                }
            }
            // remove associations
            List<RegistryConfig.Association> associations = registryConfig.getAssociations();
            for (RegistryConfig.Association association : associations) {
                Registry reg = getRegistryInstance(association.getRegistryType());
                if (reg != null) {
                    reg.removeAssociation(association.getSourcePath(),
                            association.getTargetPath(), association.getAssociationType());
                }
            }
        } catch (RegistryException e) {
            log.error("Error while removing registry resources of the artifact : " +
                    registryConfig.getParentArtifactName());
        } catch (Exception e) {
            log.error("Error while reading path mapping file for : " +
                    registryConfig.getParentArtifactName());
        }
    }

    /**
     * Writes all registry contents (resources, collections and associations) of the given
     * artifact to the registry.
     *
     * @param regConfig - Artifact instance
     * @throws Exception - on registry errors
     */
    public void writeArtifactResources(RegistryConfig regConfig) throws Exception {
        // write collections
        List<RegistryConfig.Collection> collections = regConfig.getCollections();
        for (RegistryConfig.Collection col : collections) {
            Registry reg = getRegistryInstance(col.getRegistryType());
            String dirPath = regConfig.getExtractedPath() + File.separator +
                    AppDeployerConstants.RESOURCES_DIR + File.separator + col.getDirectory();

            // check whether the collection dir exists
            File file = new File(dirPath);
            if (!file.exists()) {
                log.error("Specified collection directory not found at : " + dirPath);
                continue;
            }
            if (reg != null) {
                RegistrySynchronizer.checkIn((UserRegistry) reg, dirPath,
                        col.getPath(), true, true);
            }
        }

        // write resources
        List<RegistryConfig.Resourse> resources = regConfig.getResources();
        for (RegistryConfig.Resourse resource : resources) {
            Registry reg = getRegistryInstance(resource.getRegistryType());
            String filePath = regConfig.getExtractedPath() + File.separator +
                    AppDeployerConstants.RESOURCES_DIR + File.separator + resource.getFileName();

            // check whether the file exists
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("Specified file to be written as a resource is " +
                        "not found at : " + filePath);
                continue;
            }
            if (reg != null) {
                String resourcePath = AppDeployerUtils.computeResourcePath(resource.getPath(),
                        resource.getFileName());
				String actualResourcePath = writeFromFile(
						reg,
						file,
						resourcePath,
						resource.getMediaType() != null ? resource.getMediaType() : 
						AppDeployerUtils.readMediaType(regConfig.getExtractedPath(),
						resource.getFileName()));
                if (actualResourcePath != null && !resourcePath.equals(actualResourcePath)) {
                    Resource pathMappingResource = configRegistry.
                            get(AppDeployerConstants.REG_PATH_MAPPING + regConfig.getAppName());
                    OMElement element = AXIOMUtil.stringToOM(new String((byte[]) pathMappingResource.getContent()));
                    OMFactory factory = OMAbstractFactory.getOMFactory();
                    OMElement resourceElement = factory.createOMElement(
                            new QName(AppDeployerConstants.REG_PATH_MAPPING_RESOURCE));
                    resourceElement.addAttribute(factory.createOMAttribute(
                            AppDeployerConstants.REG_PATH_MAPPING_RESOURCE_ATTR_PATH, null, resourcePath));
                    OMElement targetElement = factory.createOMElement(
                            new QName(AppDeployerConstants.REG_PATH_MAPPING_RESOURCE_TARGET));
                    targetElement.setText(actualResourcePath);
                    resourceElement.addChild(targetElement);
                    element.addChild(resourceElement);
                    pathMappingResource.setContent(element.toString());
                    configRegistry.put(AppDeployerConstants.REG_PATH_MAPPING +
                            regConfig.getAppName(), pathMappingResource);
                }
            }
        }

        // write dumps
        List<RegistryConfig.Dump> dumps = regConfig.getDumps();
        for (RegistryConfig.Dump dump : dumps) {
            Registry reg = getRegistryInstance(dump.getRegistryType());
            String filePath = regConfig.getExtractedPath() + File.separator +
                    AppDeployerConstants.RESOURCES_DIR + File.separator + dump.getDumpFileName();

            // check whether the file exists
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("Specified file to be written as a dump is " +
                        "not found at : " + filePath);
                continue;
            }
            // .dump file is a zip, so create a ZipInputStream
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            zis.getNextEntry();
            Reader reader = new InputStreamReader(zis);
            if (reg != null) {
                reg.restore(dump.getPath(), reader);
            }
        }

        // write associations
        List<RegistryConfig.Association> associations = regConfig.getAssociations();
        for (RegistryConfig.Association association : associations) {
            Registry reg = getRegistryInstance(association.getRegistryType());
            try {
                if (reg != null) {
                    reg.addAssociation(association.getSourcePath(),
                            association.getTargetPath(), association.getAssociationType());
                }
            } catch (RegistryException e) {
                log.error("Error while adding the association. Source path : " + association
                        .getSourcePath() + " Target path : " + association.getTargetPath());
            }
        }
    }

    /**
     * Write the file content as a registry resource to the given path
     *
     * @param reg          - correct registry instance
     * @param file         - file to be written
     * @param registryPath - path to write the resource
     * @param mediaType    - media type of the resource to be added
     *
     * @return String resourcePath - actual resource path
     */
    public String writeFromFile(Registry reg, File file, String registryPath, String mediaType) {
        // convert the file content into bytes and then encode it as a string
        byte[] content = getBytesFromFile(file);
        if (content == null) {
            log.error("Error while writing file content into Registry. File content is null..");
            return null;
        }

        String resourcePath = null;
        try {
            reg.beginTransaction();
            String fileName = file.getName();
            if (mediaType == null) {
                mediaType = MediaTypesUtils.getMediaType(fileName);
            }

            if (AppDeployerConstants.REG_GAR_MEDIATYPE.equals(mediaType)) {
                try {
                    String garName = fileName.substring(0, fileName.lastIndexOf("."));
                    String garMappingResourcePath = AppDeployerConstants.REG_GAR_PATH_MAPPING + garName;
                    Resource gar = configRegistry.newResource();
                    gar.setUUID(UUID.randomUUID().toString());
                    gar.setContent("<gar_mapping/>");
                    configRegistry.put(garMappingResourcePath, gar);
                } catch (Exception e) {
                    log.error("Error in adding gar mapping file for " + fileName, e);
                }
            }

            Resource resource = reg.newResource();
            resource.setContent(content);
            resource.setMediaType(mediaType);
            resourcePath = reg.put(registryPath, resource);
            reg.commitTransaction();
        } catch (RegistryException e) {
            try {
                reg.rollbackTransaction();
            } catch (RegistryException e1) {
                log.error("Error while transaction rollback", e1);
            }
            log.error("Error while checking in resource to path: " + registryPath +
                    " from file: " + file.getAbsolutePath(), e);
        }
        return resourcePath;
    }

    /**
     * Create resource path mapping file in the registry.
     * This is only used in GovernanceRegistry server role
     *
     * @param appName  Governance Registry Application Name
     */
    public void createResourcePathMappingFile(String appName) {
        try {
            String pathMappingResourcePath = AppDeployerConstants.REG_PATH_MAPPING + appName;
            Resource resource = configRegistry.newResource();
            resource.setUUID(UUID.randomUUID().toString());
            resource.setContent("<path_mapping/>");
            configRegistry.put(pathMappingResourcePath, resource);
        } catch (Exception e) {
            log.error("Error in creating resource path mapping for carbon app " + appName, e);
        }
    }

    /**
     * Delete resource path mapping file in the registry.
     * This is only used in GovernanceRegistry server role
     *
     * @param appName  Governance Registry Application Name
     */
    public void deleteResourcePathMappingFile(String appName) {
        try {
            String pathMappingResourcePath = AppDeployerConstants.REG_PATH_MAPPING + appName;
            configRegistry.delete(pathMappingResourcePath);
        } catch (Exception e) {
            log.error("Error in creating resource path mapping for carbon app " + appName, e);
        }
    }


    /**
     * Persits the registry config file to registry
     *
     * @param artifactPath - registry path of the "registry/resource" artifact
     * @param regConfig - RegistryConfig instance
     * @throws Exception - on registry errors
     */
    public void persistRegConfig(String artifactPath, RegistryConfig regConfig)
            throws Exception {
        if (regConfig == null) {
            return;
        }
        Resource resource = configRegistry.newResource();
        File regConfigXml = new File(regConfig.getExtractedPath() +
                File.separator + regConfig.getConfigFileName());
        resource.setContentStream(new FileInputStream(regConfigXml));
        configRegistry.put(artifactPath + AppDeployerConstants.REG_CONFIG_XML , resource);
    }

    /**
     * Loads the registry config stream for the given artifact path and builds a RegistryConfig
     * instance.
     *
     * @param artifactPath - registry path of the "registry/resource" artifact
     * @return - RegistryConfig instance built
     * @throws Exception - on registry errors
     */
    public RegistryConfig loadRegistryConfig(String artifactPath) throws Exception {
        return loadRegistryConfig(artifactPath, null);
    }

    /**
     * Loads the registry config stream for the given artifact path and builds a RegistryConfig
     * instance.
     *
     * @param artifactPath - registry path of the "registry/resource" artifact
     * @param appName - carbon application name
     * @return - RegistryConfig instance built
     * @throws Exception - on registry errors
     */
    public RegistryConfig loadRegistryConfig(String artifactPath, String appName) throws Exception {
        RegistryConfig regConfig = null;
        String regConfigPath = artifactPath + AppDeployerConstants.REG_CONFIG_XML;
        if (configRegistry.resourceExists(regConfigPath)) {
            Resource artifactResource = configRegistry.get(regConfigPath);
            InputStream xmlStream = artifactResource.getContentStream();
            if (xmlStream != null) {
                regConfig = AppDeployerUtils.populateRegistryConfig(
                        new StAXOMBuilder(xmlStream).getDocumentElement());
            }
        }
        regConfig.setAppName(appName);
        return regConfig;
    }
    
    /**
     * Checks whether default server roles in carbon.xml are overridden through the UI
     * @return - true if modified, else false
     */
    public boolean areRolesOverridden() {
        String defaultPath = ServerRoleUtils.getRegistryPath(ServerRoleConstants.DEFAULT_ROLES_ID);

        try {
            if (configRegistry.resourceExists(defaultPath)) {
                Resource defResource = configRegistry.get(defaultPath);
                if (ServerRoleConstants.MODIFIED_TAG_TRUE.equals(defResource
                        .getProperty(ServerRoleConstants.MODIFIED_TAG))) {
                    return true;
                }
            }
        } catch (RegistryException e) {
            log.error("Error while reading server role resources", e);
        }
        return false;
    }

    /**
     * Reads the server roles which are stored in registry
     * @param roleType - default or custom
     * @return - list of roles
     */
    public List<String> readServerRoles(String roleType) {
        String rolesPath = ServerRoleUtils.getRegistryPath(roleType);
        List<String> roles = new ArrayList<String>();

        try {
            if (configRegistry.resourceExists(rolesPath)) {
                Resource resource = configRegistry.get(rolesPath);
                List<String> rolesRead = resource.getPropertyValues(roleType);
                if (rolesRead != null) {
                    return rolesRead;
                }
            }
        } catch (RegistryException e) {
            log.error("Error while reading server role resources", e);
        }
        return roles;
    }

    /**
     * Returns the contents of the file in a byte array.
     *
     * @param file - file to convert
     * @return - byte array
     */
    private byte[] getBytesFromFile(File file) {
        InputStream is = null;
        byte[] bytes = null;
        try {
            is = new FileInputStream(file);
            long length = file.length();
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
                log.error("File " + file.getName() + "is too large.");
            }

            // byte array to keep the data
            bytes = new byte[(int) length];
            int offset = 0;
            int numRead;
            try {
                while (offset < bytes.length
                        && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                    offset += numRead;
                }
            } catch (IOException e) {
                log.error("Error in reading data", e);
            }

            if (offset < bytes.length) {
                log.error("Could not completely read file " + file.getName());
            }
        } catch (FileNotFoundException e) {
            log.error("Expected file: " + file.getName() + " Not found", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                log.error("Error in closing the stream", e);
            }
        }
        return bytes;
    }

    /**
     * Returns the correct registry instance according to the given registry type
     *
     * @param registryType - type string
     * @return - Registry instance
     */
    private Registry getRegistryInstance(String registryType) {
        Registry registry = null;
        // we use the rootRegistry if registryType is not found. this is to make it 
        // backward compatible with CStudio 1.0
        if (registryType == null || "".equals(registryType)) {
            registry = rootRegistry;
        } else if (RegistryConfig.LOCAL_REGISTRY.equals(registryType)) {
            registry = localRegistry;
        } else if (RegistryConfig.CONFIG_REGISTRY.equals(registryType)) {
            registry = configRegistry;
        } else if (RegistryConfig.GOVERNANCE_REGISTRY.equals(registryType)) {
            registry = governanceRegistry;
        }
        return registry;
    }

}
