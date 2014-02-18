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
import org.w3c.dom.Element;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;

public class ClusterConfigurationTestCase extends BaseTest {

    private ClusterConfiguration clusterConfiguration;
    private String clusterXMLLocation;

    @BeforeTest
    public void setup() {
        clusterXMLLocation = getTestResourceFile("cluster-00.xml").getAbsolutePath();
        clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setClusterConfigurationXMLLocation(clusterXMLLocation);
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

    @Test (dependsOnMethods = {"testBuildClusterConfiguration"})
    public void testClusteringEnabled() {
        Object obj = clusterConfiguration.getElement("cluster");
        if (obj instanceof Element) {
            Element rootElement = (Element) obj;
            boolean isEnabled = Boolean.parseBoolean(rootElement.getAttribute("enable"));
            Assert.assertTrue(isEnabled);
        }
    }

    @Test
    public void testClusteringAgentType() throws ClusterConfigurationException {
        boolean isHazelCast = clusterConfiguration.shouldInitialize("hazelcast");
        Assert.assertTrue(isHazelCast);
    }
}
