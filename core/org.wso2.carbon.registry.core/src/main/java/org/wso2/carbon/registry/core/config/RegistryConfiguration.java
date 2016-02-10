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
package org.wso2.carbon.registry.core.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is to initialize, validate the registry related configurations in carbon.xml. This
 * method of storing registry configuration is deprecated from WSO2 Carbon 2.0.2
 */
@Deprecated
public class RegistryConfiguration {

    private static final Log log = LogFactory.getLog(RegistryConfiguration.class);

    /**
     * The registry configuration root element name.
     */
    public static final String REGISTRY_CONFIG = "Registry";

    /**
     * The element name to store registry type. Possible values are "remote" and "embedded".
     */
    public static final String TYPE = "Type";

    /**
     * The element name to store the registry username. Requires if the "Type" is selected as
     * "remote".
     */
    public static final String USERNAME = "Username";

    /**
     * The element name to store the registry password. Requires if the "Type" is selected as
     * "remote".
     */
    public static final String PASSWORD = "Password";

    /**
     * The element name to store the registry url. Requires if the "Type" is selected as "remote".
     */
    public static final String URL = "Url";

    /**
     * The element name to store the registry root. The registry chroot to be mounted.
     */
    public static final String REGISTRY_ROOT = "registryRoot";

    /**
     * The element name to store whether the . The registry chroot to be mounted.
     */
    public static final String READ_ONLY = "ReadOnly";

    /**
     * The system username of the registry.
     */
    public static final String SYSTEM_USER_NAME = "RegistrySystemUserName";

    /**
     * The value of the registry type field to mount a remote registry.
     */
    public static final String REMOTE_REGISTRY = "remote";

    /**
     * The value of the registry type field to use the embedded registry.
     */
    public static final String EMBEDDED_REGISTRY = "embedded";

    private Map<String, String> registryConfiguration = new HashMap<String, String>();

    /**
     * Initialize the registry configuration located in carbon.xml provided the carbon.xml path as
     * an argument.
     *
     * @param carbonXMLPath the path of the carbon.xml
     *
     * @throws RegistryException throws if the construction failed
     */
    public RegistryConfiguration(String carbonXMLPath) throws RegistryException {
        InputStream inSXml = null;
        try {
            File carbonXML = new File(carbonXMLPath);
            inSXml = new FileInputStream(carbonXML);
            OMElement config = new StAXOMBuilder(inSXml).getDocumentElement();

            OMElement registryConfig = config.getFirstChildWithName(new QName(
                    "http://wso2.org/projects/carbon/carbon.xml", REGISTRY_CONFIG));

            if (registryConfig != null) {

                for (Iterator childElements = registryConfig.getChildElements();
                     childElements.hasNext();) {
                    OMElement element = (OMElement) childElements.next();
                    registryConfiguration.put(element.getLocalName(), element.getText());

                }

                //Default to embedded configuration
            }

            if (registryConfiguration.get(TYPE) == null) {
                registryConfiguration.put(TYPE, EMBEDDED_REGISTRY);
            }

            validate();

        } catch (Exception e) {
            throw new RegistryException("Error occurred while initialization", e);
        } finally {
            if (inSXml != null) {
                try {
                    inSXml.close();
                } catch (IOException e) {
                    log.error("Failed to close the stream", e);
                }
            }
        }
    }

    /**
     * Validate the registry configuration.
     *
     * @throws RegistryException throws if the validation failed.
     */
    private void validate() throws RegistryException {

        registryConfiguration.put(SYSTEM_USER_NAME, CarbonConstants.REGISTRY_SYSTEM_USERNAME);

        String type = registryConfiguration.get(TYPE);
        if (type == null || type.trim().length() == 0) {
            type = EMBEDDED_REGISTRY;
        }
        if (REMOTE_REGISTRY.equals(type)) {
            if (registryConfiguration.get(URL) == null) {
                throw new RegistryException("URL not given");
            }
            if (registryConfiguration.get(REGISTRY_ROOT) != null &&
                    !registryConfiguration.get(REGISTRY_ROOT).startsWith("/")) {
                log.error("Invalid Registry Configuration : CHROOT must start with a /");
                throw new RegistryException(
                        "Invalid Registry Configuration : CHROOT must start with a /");
            }
        } else if (!EMBEDDED_REGISTRY.equals(type)) {
            throw new RegistryException("Unkown type");
        }
    }

    /**
     * Get a configuration value.
     *
     * @param key the configuration key.
     *
     * @return the configuration value.
     */
    public String getValue(String key) {
        return registryConfiguration.get(key);
    }

    /**
     * Get the registry type.
     *
     * @return the registry type. possible values "embedded", "remote"
     */
    public String getRegistryType() {
        return registryConfiguration.get(RegistryConfiguration.TYPE);
    }

}
