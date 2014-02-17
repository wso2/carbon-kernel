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
import org.wso2.carbon.clustering.agent.CustomClusteringAgent;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;

public class ClusteringAgentTestCase extends BaseTest {

    private CustomClusteringAgent clusteringAgent;
    private ClusterContext clusterContext;

    @BeforeTest
    public void setup() throws ClusterConfigurationException {
        String clusterXMLLocation = getTestResourceFile("cluster-02.xml").getAbsolutePath();
        clusteringAgent = new CustomClusteringAgent();
        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setClusterConfigurationXMLLocation(clusterXMLLocation);
        clusterConfiguration.build();
        clusterContext = new ClusterContext(clusterConfiguration);
    }

    @Test
    public void testInitializeClusteringAgent() throws ClusterInitializationException {
        clusteringAgent.init(clusterContext);
        Assert.assertTrue(clusteringAgent.isInitialized());
    }

    @Test(dependsOnMethods = {"testInitializeClusteringAgent"})
    public void testShutDownClusteringAgent() {
        clusteringAgent.shutdown();
    }

}
