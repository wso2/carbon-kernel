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
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.message.CustomClusterMessage;

import java.util.ArrayList;
import java.util.List;


public class MulitcastMembershipSchemeTestCase extends MembershipSchemeBaseTest {


    @BeforeTest
    public void setup() throws ClusterConfigurationException {
        setupMembershipScheme("cluster-01.xml", "cluster-02.xml");
    }
    @Test (groups = {"wso2.carbon.clustering"},
           description = "test multicast scheme with two members")
    public void testMulticastMembershipScheme() throws ClusterInitializationException {
        initializeMembershipScheme();
        int noOfMembers = getNoOfMembers();
        Assert.assertEquals(noOfMembers, 2);
    }

    @Test (groups = {"wso2.carbon.clustering"},
           description = "test send message with multicast scheme",
           dependsOnMethods = {"testMulticastMembershipScheme"})
    public void testSendMessage() throws MessageFailedException {
        CarbonCluster carbonCluster = getClusterService();
        CustomClusterMessage clusterMessage = new CustomClusterMessage("MulticastMessage");
        carbonCluster.sendMessage(clusterMessage);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            //ignore
        }
        Assert.assertEquals(clusterMessage.getExecutedMsg(), "MulticastMessageExecuted");
    }

    @Test (groups = {"wso2.carbon.clustering"},
           description = "test send message to specific member in multicast scheme",
           dependsOnMethods = {"testSendMessage"})
    public void testSendMessageToMember() throws MessageFailedException {
        CarbonCluster carbonCluster = getClusterService();
        List<ClusterMember> clusterMembers = carbonCluster.getMembers();
        List<ClusterMember> membersToSend = new ArrayList<>();
        for (ClusterMember member : clusterMembers) {
            if (member.getPort() == 4002) {
                membersToSend.add(member);
                break;
            }
        }
        if (!membersToSend.isEmpty()) {
            CustomClusterMessage clusterMessage = new CustomClusterMessage("MulticastMemberMessage");
            carbonCluster.sendMessage(clusterMessage, membersToSend);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                //ignore
            }
            Assert.assertEquals(clusterMessage.getExecutedMsg(), "MulticastMemberMessageExecuted");
        } else {
            Assert.fail("Members to send list is empty");
        }
    }

    @AfterTest
    public void shutdownNodes() {
        terminate();
    }
}
