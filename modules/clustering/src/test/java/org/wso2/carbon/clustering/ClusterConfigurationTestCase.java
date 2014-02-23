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
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

public class ClusterConfigurationTestCase extends BaseTest {

    private ClusterConfiguration clusterConfiguration;

    @BeforeTest
    public void setup() {
        String clusterXMLLocation = getTestResourceFile("cluster-00.xml").getAbsolutePath();
        clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setClusterConfigurationXMLLocation(clusterXMLLocation);
    }

    @Test
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

    @Test
    public void testClusterXmlValidation2()
            throws ParserConfigurationException, IOException, SAXException {
        Source xmlFile = new StreamSource(new File(".." + File.separator + ".." + File.separator +
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

    @Test
    public void testBuildClusterConfiguration() throws ClusterConfigurationException {
        ClusterConfiguration sampleClusterConfiguration = new ClusterConfiguration();
        sampleClusterConfiguration.setClusterConfigurationXMLLocation("fake/path");
        try {
            sampleClusterConfiguration.build();
        } catch (ClusterConfigurationException e) {
            Assert.assertTrue(e.getMessage().
                    contains("Error while building cluster configuration"));
        }
        clusterConfiguration.build();
    }

    @Test(dependsOnMethods = {"testBuildClusterConfiguration"})
    public void testClusteringEnabled() {
        boolean isEnabled = Boolean.parseBoolean(clusterConfiguration.getFirstProperty("Enable"));
        Assert.assertTrue(isEnabled);
    }

    @Test
    public void testClusteringAgentType() throws ClusterConfigurationException {
        boolean isHazelCast = clusterConfiguration.shouldInitialize("hazelcast");
        Assert.assertTrue(isHazelCast);
    }
}
