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
import org.wso2.carbon.utils.component.xml.config.HTTPGetRequestProcessorConfig;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HTTPGetRequestProcessorConfigBuilder extends ComponentConfigBuilder {

    public ComponentConfig[] build(OMElement configElement) throws CarbonException {
        List<HTTPGetRequestProcessorConfig> getRequestProcessorConfigList =
                new ArrayList<HTTPGetRequestProcessorConfig>();
        for (Iterator iterator = configElement.getChildrenWithName(
                new QName(NS_WSO2CARBON, ELE_PROCESSOR)); iterator.hasNext();) {
            OMElement processorElement = (OMElement) iterator.next();

            //item element
            OMElement itemElement = processorElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, ELE_ITEM));
            String item = null;
            if (itemElement != null) {
                item = itemElement.getText().trim();
            }

            if (item == null) {
                throw new CarbonException("Mandatory element processor/item entry " +
                        "does not exist or is empty in the component.xml");
            }

            //class element
            OMElement classElement = processorElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, ELE_CLASS));
            String className = null;
            if (classElement != null) {
                className = classElement.getText().trim();
            }

            if (className == null) {
                throw new CarbonException("Mandatory element processor/class entry " +
                        "does not exist or is empty in the component.xml");
            }
            
            HTTPGetRequestProcessorConfig getRequestProcessorConfig =
                    new HTTPGetRequestProcessorConfig();
            getRequestProcessorConfig.setItem(item);
            getRequestProcessorConfig.setClassName(className);
            getRequestProcessorConfigList.add(getRequestProcessorConfig);
        }

        if(getRequestProcessorConfigList.size() == 0){
            return null;
        } else {
            HTTPGetRequestProcessorConfig[] getRequestProcessorConfigs =
                    new HTTPGetRequestProcessorConfig[getRequestProcessorConfigList.size()];
            return getRequestProcessorConfigList.toArray(getRequestProcessorConfigs);
        }
    }
    
    public String getLocalNameOfComponentConfigElement() {
        return ComponentConstants.HTTP_GET_REQUEST_PROCESSORS;
    }
}
