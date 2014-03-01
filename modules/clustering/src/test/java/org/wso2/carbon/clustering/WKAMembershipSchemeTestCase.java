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
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.config.membership.scheme.WKAMember;
import org.wso2.carbon.clustering.config.membership.scheme.WKASchemeConfig;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.message.CustomClusterMessage;
import org.wso2.carbon.clustering.message.CustomMemberClusterMessage;

import java.util.ArrayList;
import java.util.List;


public class WKAMembershipSchemeTestCase extends MembershipSchemeBaseTest {

    @BeforeTest
    public void setup() throws ClusterConfigurationException {
        setupMembershipScheme("cluster-03.xml", "cluster-04.xml");
    }

    @Test(groups = {"wso2.carbon.clustering"}, description = "test wka scheme with two members")
    public void testWKAMembershipScheme()
            throws ClusterInitializationException, ClusterConfigurationException {
        initializeMembershipScheme();
        int noOfMembers = getNoOfMembers();
        Assert.assertEquals(noOfMembers, 2);

        ClusterConfiguration clusterConfiguration = getClusterContext().getClusterConfiguration();
        Object membershipScheme = clusterConfiguration.
                getMembershipSchemeConfiguration().getMembershipScheme();
        Assert.assertEquals(ClusterUtil.getMembershipScheme(clusterConfiguration),
                            membershipScheme.toString());
        WKASchemeConfig wkaSchemeConfig = (WKASchemeConfig) membershipScheme;
        List<WKAMember> wkaMembers = wkaSchemeConfig.getWkaMembers();
        List<ClusterMember> clusterMembers = ClusterUtil.getWellKnownMembers(clusterConfiguration);

        for (int i = 0; i < wkaMembers.size(); i++) {
            Assert.assertTrue(clusterMembers.get(i).getHostName().
                    equals(wkaMembers.get(i).getHost()));
        }

    }


    @Test(groups = {"wso2.carbon.clustering"}, description = "test send message with wka scheme",
          dependsOnMethods = {"testWKAMembershipScheme"})
    public void testSendMessage() throws MessageFailedException {
        CarbonCluster carbonCluster = getClusterService();
        CustomClusterMessage clusterMessage = new CustomClusterMessage("WKAMessage");
        carbonCluster.sendMessage(clusterMessage);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            //ignore
        }
        Assert.assertEquals(clusterMessage.getExecutedMsg(), "WKAMessageExecuted");
    }

    @Test(groups = {"wso2.carbon.clustering"},
          description = "test send message to specific member in wka scheme",
          dependsOnMethods = {"testSendMessage"})
    public void testSendMessageToMember() throws MessageFailedException {
        CarbonCluster carbonCluster = getClusterService();
        List<ClusterMember> clusterMembers = carbonCluster.getMembers();
        List<ClusterMember> membersToSend = new ArrayList<>();
        for (ClusterMember member : clusterMembers) {
            if (member.getPort() == 4004) {
                membersToSend.add(member);
                break;
            }
        }
        if (!membersToSend.isEmpty()) {
            CustomMemberClusterMessage clusterMessage =
                    new CustomMemberClusterMessage("WKAMemberMessage");
            carbonCluster.sendMessage(clusterMessage, membersToSend);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //ignore
            }

            // try again, if the message was not delivered during the given sleep time
            if ("WKAMessageExecuted".equals(clusterMessage.getExecutedMsg())) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
            // if still not delivered then fail the test
            if ("WKAMessageExecuted".equals(clusterMessage.getExecutedMsg())) {
                Assert.fail("Message is not sent/executed with given time delay of 15 seconds");
            }
            Assert.assertEquals(clusterMessage.getExecutedMsg(), "WKAMemberMessageExecuted");
        } else {
            Assert.fail("Members to send list is empty");
        }
    }

    @AfterTest
    public void shutdownNodes() {
        terminate();
    }
}
