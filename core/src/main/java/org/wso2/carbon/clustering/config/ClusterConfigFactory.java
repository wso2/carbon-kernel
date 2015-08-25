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

package org.wso2.carbon.clustering.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;

import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class ClusterConfigFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConfigFactory.class);

    public static ClusterConfiguration build()
            throws ClusterConfigurationException {
        ClusterConfiguration clusterConfiguration = null;
        try {
            //TODO : get carbon repo from system property
            String clusterXmlLocation = System.getProperty("carbon.home") + File.separator +
                                        "repository" + File.separator + "conf" +
                                        File.separator + "cluster.xml";

            File file = new File(clusterXmlLocation);
            JAXBContext jaxbContext = JAXBContext.newInstance(ClusterConfiguration.class);

            // validate cluster.xml using the schema
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            StreamSource streamSource = new StreamSource();
            streamSource.setInputStream(Thread.currentThread().getContextClassLoader().
                    getResourceAsStream("cluster.xsd"));
            Schema schema = sf.newSchema(streamSource);

            // un-marshall and populate the cluster configuration instance
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            clusterConfiguration = (ClusterConfiguration) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            String msg = "Error while loading cluster configuration file";
            logger.error(msg, e);
            throw new ClusterConfigurationException(msg, e);
        }

        return clusterConfiguration;
    }
}
