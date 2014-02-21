/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.Utils;
import org.wso2.carbon.base.exception.ConfigurationInitializationException;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClusterConfiguration which holds the static information of the cluster. This is will be build
 * and populated using the cluster.xml
 */
public class ClusterConfiguration {


    private static final Logger logger = LoggerFactory.getLogger(ClusterConfiguration.class);

    private Map<String, List<Object>> configuration = new HashMap<>();
    private boolean isInitialized = false;

    private String configurationXMLLocation = System.getProperty("carbon.home") + File.separator +
                                              "repository" + File.separator + "conf" +
                                              File.separator + "cluster.xml";


    public void setClusterConfigurationXMLLocation(String configurationXMLLocation) {
        this.configurationXMLLocation = configurationXMLLocation;
    }

    /**
     * This initializes the server configuration. This method should only be
     * called once, for successive calls, it will be checked.
     *
     * @throws ClusterConfigurationException if the operation failed.
     */
    public synchronized void build() throws ClusterConfigurationException {
        if (isInitialized) {
            return;
        }

        InputStream xmlInputStream = null;
        try {
            xmlInputStream = Utils.parseXmlConfiguration(configurationXMLLocation);
            Utils.buildXmlConfiguration(xmlInputStream, configuration);
            isInitialized = true;
        } catch (ConfigurationInitializationException e) {
            throw new ClusterConfigurationException("Error while building cluster configuration",
                                                    e);
        } finally {
            if (xmlInputStream != null) {
                try {
                    xmlInputStream.close();
                } catch (IOException e) {
                    logger.warn("Cannot close input stream", e);
                }
            }
        }
    }

    /**
     * There can be multiple objects with the same key. This will return the
     * first String from them
     *
     * @param key the search key
     * @return value corresponding to the given key
     */
    public String getFirstProperty(String key) {
        List<Object> valueList = configuration.get(key);
        if (valueList == null || !(valueList.get(0) instanceof String)) {
            return null;
        }
        return (String) valueList.get(0);
    }

    public List<Object> getElement(String key) {
        List<Object> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value;
    }

    /**
     * This will return whether cluster agent should be initialized by checking the cluster
     * "agentType" attribute in cluster xml with the registered value
     *
     * @param agentType the value of the registered agent type to check
     * @return true if registered agentType match the cluster.xml property value
     * @throws ClusterConfigurationException on error while reading the value
     */
    public boolean shouldInitialize(String agentType) throws ClusterConfigurationException {
        boolean initialize = false;
        try {
            File xmlFile = new File(configurationXMLLocation);
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList nodeList = doc.getDocumentElement().getChildNodes();

            for (int count = 0; count <= nodeList.getLength(); count++) {
                Node node = nodeList.item(count);
                if (node.getNodeType() == Node.ELEMENT_NODE &&
                    node.getNodeName().equals(ClusteringConstants.CLUSTER_AGENT) &&
                    node.getTextContent().equals(agentType)) {
                    build();
                    initialize = true;
                    break;
                }
            }
        } catch (Exception e) {
            String msg = "Error while loading cluster configuration file";
            logger.error(msg, e);
            throw new ClusterConfigurationException(msg, e);
        }
        return initialize;
    }
}
