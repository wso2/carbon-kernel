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
package org.wso2.carbon.registry.core.jdbc.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Utilities for configuring services.
 */
public final class ServiceConfigUtil {

    private static final Log log = LogFactory.getLog(ServiceConfigUtil.class);

    private ServiceConfigUtil() {
    }

    /**
     * Method to obtain configuration file content as a String.
     *
     * @return configuration file XML as a String
     * @throws RegistryException if the operation fails.
     */
    public static String getConfigFile() throws RegistryException {
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        if (carbonHome != null) {
            OMElement omElement = null;
            String configPath = CarbonUtils.getCarbonHome()+File.separator+"repository"+File.separator+"resources" + File.separator +"services-config.xml";

            try {
                File configFile = new File(configPath);
                if (configFile.exists()) {
                    InputStream stream = new FileInputStream(configFile);
                    StAXOMBuilder builder = new StAXOMBuilder(stream);
                    omElement = builder.getDocumentElement();
                }
            } catch (FileNotFoundException e) {
                log.error("The services configuration file was not found at: " + configPath);
            } catch (XMLStreamException e) {
                log.error("The configuration file does not contain well formed XML", e);
            }
            return (omElement != null) ? omElement.toString() : null;
        } else {
            String msg = "carbon.home system property is not set. It is required to to derive " +
                    "the path of the Services configuration file (services-config.xml).";
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    /**
     * Utility method to persist service configuration.
     *
     * @param registry the registry instance to use.
     *
     * @throws Exception if an error occurs.
     */
    public static void addConfig(Registry registry) throws Exception {

        Resource config = new ResourceImpl();
        String path =  RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH + "service";
        if (RegistryUtils.systemResourceShouldBeAdded(registry, path)) {
            if (registry.getRegistryContext() != null && registry.getRegistryContext().isClone()) {
                return;
            }
            String serviceConfig = getConfigFile();
            if (serviceConfig != null) {
                config.setContent(RegistryUtils.encodeString(serviceConfig));
            }
            registry.put(path, config);
        }

    }
     /**
     * Utility method to persist service configuration schema content.
     *
     * @param registry the registry instance to use.
     *
     * @throws Exception if an error occurs.
     */
    public static void addConfigSchema(Registry registry) throws Exception {

        Resource config = new ResourceImpl();
        String path = RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH + "service-schema";
        if (RegistryUtils.systemResourceShouldBeAdded(registry, path)) {
            if (registry.getRegistryContext() != null && registry.getRegistryContext().isClone()) {
                return;
            }
            String serviceConfig = getConfigSchemaFile();
            if (serviceConfig != null) {
                config.setContent(RegistryUtils.encodeString(serviceConfig));
            }
            registry.put(path, config);
        }

    }
    /**
     * Method to obtain services config schema file content as a String.
     *
     * @return service configuration schema file XML as a String
     * @throws RegistryException if the operation fails.
     */
    public static String getConfigSchemaFile() throws RegistryException {
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        if (carbonHome != null) {
            OMElement omElement = null;
            String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                "services-config.xsd";
            try {
                File configFile = new File(configPath);
                if (configFile.exists()) {
                    InputStream stream = new FileInputStream(configFile);
                    StAXOMBuilder builder = new StAXOMBuilder(stream);
                    omElement = builder.getDocumentElement();
                }
            } catch (FileNotFoundException e) {
                log.error("The services configuration schema file was not found at: " + configPath);
            } catch (XMLStreamException e) {
                log.error("The configuration schema file does not contain well formed XML", e);
            }
            return (omElement != null) ? omElement.toString() : null;
        } else {
            String msg = "carbon.home system property is not set. It is required to to derive " +
                    "the path of the Services configuration file (services-config.xml).";
            log.error(msg);
            throw new RegistryException(msg);
        }
    }
}
