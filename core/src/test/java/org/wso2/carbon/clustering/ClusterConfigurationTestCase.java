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

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class ClusterConfigurationTestCase extends BaseTest {

    private ClusterConfiguration clusterConfiguration;

    @BeforeTest
    public void setup() throws ClusterConfigurationException {

    }

    @Test(groups = {"wso2.carbon.clustering"}, description = "sample cluster.xml validation")
    public void testClusterXmlValidation1()
            throws ParserConfigurationException, IOException, SAXException {
        Source xmlFile = new StreamSource(getTestResourceFile("xsd" + File.separator +
                "cluster.xml"));
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(getTestResourceFile("xsd" + File.separator +
                "cluster.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }

    @Test(groups = {"wso2.carbon.clustering"}, description = "distribution cluster.xml validation")
    public void testClusterXmlValidation2()
            throws ParserConfigurationException, IOException, SAXException {
        Source xmlFile = new StreamSource(new File(".." + File.separator +
                "distribution" + File.separator + "carbon-home" +
                File.separator + "repository" + File.separator +
                "conf" + File.separator + "cluster.xml"));
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(getTestResourceFile("xsd" + File.separator +
                "cluster.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }

    @Test(groups = {"wso2.carbon.clustering"},
            description = "test cluster configuration population")
    public void testBuildClusterConfiguration() throws ClusterConfigurationException {
        try {
            buildClusterConfig("fake/path");
        } catch (ClusterConfigurationException e) {
            Assert.assertTrue(e.getMessage().
                    contains("Error while building cluster configuration"));
        }
        String clusterXMLLocation = getTestResourceFile("cluster-00.xml").getAbsolutePath();
        clusterConfiguration = buildClusterConfig(clusterXMLLocation);
    }

    @Test(groups = {"wso2.carbon.clustering"}, description = "test clustering enable/disabled",
            dependsOnMethods = {"testBuildClusterConfiguration"})
    public void testClusteringEnabled() {
        boolean isEnabled = clusterConfiguration.isEnabled();
        Assert.assertTrue(isEnabled);
    }

    @Test(groups = {"wso2.carbon.clustering"}, description = "test clustering agent type")
    public void testClusteringAgentType() throws ClusterConfigurationException {
        Assert.assertEquals("hazelcast", clusterConfiguration.getAgent());
    }
}
