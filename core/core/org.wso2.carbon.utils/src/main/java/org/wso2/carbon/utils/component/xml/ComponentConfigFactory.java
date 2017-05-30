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
package org.wso2.carbon.utils.component.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.component.xml.builder.ComponentConfigBuilder;
import org.wso2.carbon.utils.component.xml.builder.DeployerConfigBuilder;
import org.wso2.carbon.utils.component.xml.builder.HTTPGetRequestProcessorConfigBuilder;
import org.wso2.carbon.utils.component.xml.builder.ManagementPermissionsBuilder;
import org.wso2.carbon.utils.component.xml.config.ComponentConfig;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentConfigFactory implements ComponentConstants {
    private static Log log = LogFactory.getLog(ComponentConfigFactory.class);

    private static Map<String, ComponentConfigBuilder> defaultConfigBuilderMap = new ConcurrentHashMap<String, ComponentConfigBuilder>();
    
    private ComponentConfigFactory() {
        //disable external instantiation
    }

    // Statically initializing and registering common config builders.
    static {
        DeployerConfigBuilder deployerBuilder = new DeployerConfigBuilder();
        HTTPGetRequestProcessorConfigBuilder httpBuilder = new HTTPGetRequestProcessorConfigBuilder();
        ManagementPermissionsBuilder permisisonBuilder = new ManagementPermissionsBuilder();
        defaultConfigBuilderMap.put(deployerBuilder.getLocalNameOfComponentConfigElement(),
                deployerBuilder);
        defaultConfigBuilderMap
                .put(httpBuilder.getLocalNameOfComponentConfigElement(), httpBuilder);
        defaultConfigBuilderMap.put(permisisonBuilder.getLocalNameOfComponentConfigElement(),
                permisisonBuilder);
    }

    public static void addComponentBuilder(String configKey,
            ComponentConfigBuilder componentConfigBuilder) {
        defaultConfigBuilderMap.put(configKey, componentConfigBuilder);
    }

    @SuppressWarnings("unchecked")
    public static Component build(InputStream xmlStream) throws CarbonException {
        if (xmlStream == null) {
            throw new CarbonException("component.xml stream is null..");
        }

        StAXOMBuilder builder = null;
        Component component;
        try {
            builder = new StAXOMBuilder(xmlStream);
            OMElement document = builder.getDocumentElement();

            // Processing custom component builders if any,
            OMElement compBuilderEle = document.getFirstChildWithName(new QName(NS_WSO2CARBON,
                    ELE_COMPONENT_BUILDERS));
            if (compBuilderEle != null) {
                processCustomBuilders(compBuilderEle);
            }
            component = new Component();

            for (Iterator itr = document.getChildElements(); itr.hasNext();) {
                OMElement configElement = (OMElement) itr.next();
                String configKey = configElement.getLocalName();

                ComponentConfigBuilder configBuilder = defaultConfigBuilderMap.get(configKey);

                if (configBuilder == null) {
                    continue;
                }

                ComponentConfig[] componentConfig = configBuilder.build(configElement);
                if (componentConfig != null) {
                    component.addComponentConfig(configKey, componentConfig);
                }
            }
        } catch (Exception e) {
            String msg = "Failed to build component configuration.";
            log.error(msg, e);
            throw new CarbonException(msg, e);
        }

        return component;
    }

    @SuppressWarnings("unchecked")
    private static void processCustomBuilders(OMElement buildersElement) throws CarbonException {
        for (Iterator iterator = buildersElement.getChildrenWithName(new QName(NS_WSO2CARBON,
                ELE_COMPONENT_BUILDER)); iterator.hasNext();) {
            OMElement builderElement = (OMElement) iterator.next();

            // class element
            OMElement classElement = builderElement.getFirstChildWithName(new QName(NS_WSO2CARBON,
                    ELE_CLASS));
            String classString = null;
            if (classElement != null) {
                classString = classElement.getText().trim();
            }

            if (classString == null) {
                throw new CarbonException(
                        "Mandatory attribute custom component builder/name entry "
                                + "does not exist or is empty in the component.xml");
            }
            
            Class deployerClass = null;
            try {
                deployerClass = Class.forName(classString);
                ComponentConfigBuilder configBuilder = (ComponentConfigBuilder) deployerClass
                        .newInstance();
                defaultConfigBuilderMap.put(configBuilder.getLocalNameOfComponentConfigElement(),
                        configBuilder);
            } catch (Exception e) {
                throw new CarbonException(e);
            }

        }
    }
}
