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
import org.w3c.dom.Document;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class ClusterConfigurationTestCase extends BaseTest {

    private ClusterConfiguration clusterConfiguration;
    private String clusterXMLLocation;

    @BeforeTest
    public void setup() {
        clusterXMLLocation = getTestResourceFile("cluster-01.xml").getAbsolutePath();
        clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setClusterConfigurationXMLLocation(clusterXMLLocation);
    }

    @Test
    public void testClusteringEnabled()
            throws ParserConfigurationException, IOException, SAXException {
        File xmlFile = new File(clusterXMLLocation);
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);

        boolean isEnabled = Boolean.parseBoolean(doc.getDocumentElement().
                getAttribute("enable"));

        Assert.assertTrue(isEnabled);
    }

    @Test
    public void testClusteringAgentType() throws ClusterConfigurationException {
        boolean isHazelCast = clusterConfiguration.shouldInitialize("hazelcast");
        Assert.assertTrue(isHazelCast);
    }
}
