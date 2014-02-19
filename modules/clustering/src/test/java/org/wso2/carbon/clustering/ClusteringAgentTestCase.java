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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.clustering.agent.CustomClusteringAgent;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.membership.listener.CustomMembershipListener;

import java.net.InetSocketAddress;
import java.util.UUID;

public class ClusteringAgentTestCase extends BaseTest {

    private CustomClusteringAgent clusteringAgent;
    private ClusterContext clusterContext;
    private CustomMembershipListener membershipListener;

    @BeforeTest
    public void setup() throws ClusterConfigurationException {
        String clusterXMLLocation = getTestResourceFile("cluster-00.xml").getAbsolutePath();
        clusteringAgent = new CustomClusteringAgent();
        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setClusterConfigurationXMLLocation(clusterXMLLocation);
        clusterConfiguration.build();
        clusterContext = new ClusterContext(clusterConfiguration);
        membershipListener = new CustomMembershipListener();
    }

    @Test
    public void testInitializeClusteringAgent() throws ClusterInitializationException {
        clusteringAgent.init(clusterContext);
        Assert.assertTrue(clusteringAgent.isInitialized());
    }

    @Test (dependsOnMethods = {"testInitializeClusteringAgent"})
    public void testMembershipListener() {
        clusterContext.addMembershipListener(membershipListener);
        ClusterMember clusterMember = new ClusterMember(UUID.randomUUID().toString(),
                                          new InetSocketAddress("127.0.0.0", 4500));
        clusterContext.addMember(clusterMember);
        String addedMember = clusterMember.getHostName() + ":" + clusterMember.getPort();
        Assert.assertEquals(membershipListener.getAddedMember(), addedMember);

        clusterContext.removeMember(clusterMember);
        Assert.assertEquals(membershipListener.getRemovedMember(), addedMember);
    }

    @AfterTest
    public void shutdownAgent() {
        clusteringAgent.shutdown();
    }
}
