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
package org.wso2.carbon.utils.component.xml.builder;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.component.xml.ComponentConstants;
import org.wso2.carbon.utils.component.xml.config.ComponentConfig;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeployerConfigBuilder extends ComponentConfigBuilder {

    public ComponentConfig[] build(OMElement deployersElement) throws CarbonException {
        List<DeployerConfig> deployerConfigList = new ArrayList<DeployerConfig>();
        for (Iterator iterator = deployersElement.getChildrenWithName(
                new QName(NS_WSO2CARBON, ELE_DEPLOYER)); iterator.hasNext();) {
            OMElement deployerElement = (OMElement) iterator.next();

            //directory element
            OMElement directoryElement = deployerElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, ELE_DIRECTORY));
            String directory = null;
            if (directoryElement != null) {
                directory = directoryElement.getText().trim();
            }

            if (directory == null) {
                throw new CarbonException("Mandatory attribute deployer/directory entry " +
                        "does not exist or is empty in the component.xml");
            }

            //extension element
            OMElement extensionElement = deployerElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, ELE_EXTENSION));
            String extension = null;
            if (extensionElement != null) {
                extension = extensionElement.getText().trim();
            }

/*         Removing to enable Dir based deployment and making the extention element optional
            if (extension == null)
                throw new CarbonException("Mandatory attribute deployer/extension entry " +
                        "does not exist or is empty in the component.xml");*/
            
            //class element
            OMElement classElement = deployerElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, ELE_CLASS));
            String classString = null;
            if (classElement != null) {
                classString = classElement.getText().trim();
            }
            
            if (classString == null) {
                throw new CarbonException("Mandatory attribute deployer/class entry " +
                        "does not exist or is empty in the component.xml");
            }
            
            DeployerConfig deployerConfig = new DeployerConfig();
            deployerConfig.setClassStr(classString);
            deployerConfig.setDirectory(directory);
            deployerConfig.setExtension(extension);
            
            deployerConfigList.add(deployerConfig);
        }

        if(deployerConfigList.size() == 0){
            return null;
        } else {
            DeployerConfig[] deployerConfigs = new DeployerConfig[deployerConfigList.size()];
            return deployerConfigList.toArray(deployerConfigs);
        }
    }

    public String getLocalNameOfComponentConfigElement() {
        return ComponentConstants.DEPLOYER_CONFIG;
    }
    
    
}
