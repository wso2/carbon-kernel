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

import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.xml.sax.SAXException;

import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class BaseTest {

    protected String testDir = "src" + File.separator + "test" + File.separator;
    protected String testResourceDir = testDir + "resources";

    /**
     * Basedir for all file I/O.
     */
    public static String basedir;

    static {
        basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
    }

    public BaseTest() {
        testDir = new File(basedir, testDir).getAbsolutePath();
        testResourceDir = new File(basedir, testResourceDir).getAbsolutePath();
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    public ClusterConfiguration buildClusterConfig(String clusterXmlLocation)
            throws ClusterConfigurationException {
        ClusterConfiguration clusterConfiguration = null;
        try {
            File file = new File(clusterXmlLocation);
            JAXBContext jaxbContext = JAXBContext.newInstance(ClusterConfiguration.class);

            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(new File(testResourceDir + File.separator +
                                                  "xsd" + File.separator + "cluster.xsd"));

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            clusterConfiguration = (ClusterConfiguration) unmarshaller.unmarshal(file);
        } catch (JAXBException | SAXException e) {
            String msg = "Error while building cluster configuration";
            throw new ClusterConfigurationException(msg, e);
        }

        return clusterConfiguration;
    }
}
