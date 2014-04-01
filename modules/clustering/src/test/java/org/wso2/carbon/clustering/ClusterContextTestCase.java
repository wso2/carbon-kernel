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
import org.wso2.carbon.clustering.internal.ClusterContext;
import org.wso2.carbon.clustering.membership.listener.CustomMembershipListener;

import java.util.UUID;

import static org.testng.Assert.*;


public class ClusterContextTestCase extends BaseTest {

    private ClusterContext clusterContext;
    private ClusterMember clusterMember;
    private CustomMembershipListener membershipListener;

    @BeforeTest
    public void setup() throws ClusterConfigurationException {
        clusterMember = new ClusterMember("127.0.0.0", 4000);
        clusterMember.setId(UUID.randomUUID().toString());
        membershipListener = new CustomMembershipListener();
        ClusterConfiguration clusterConfiguration = buildClusterConfig(
                getTestResourceFile("cluster-00.xml"). getAbsolutePath());
        clusterContext = new ClusterContext(clusterConfiguration);
    }

    @Test (groups = {"wso2.carbon.clustering"}, description = "test add cluster member")
    public void testAddMember() {
        clusterContext.addMember(clusterMember);
        assertEquals(clusterContext.getClusterMembers().get(0), clusterMember);
    }

    @Test(groups = {"wso2.carbon.clustering"}, description = "test remove cluster member",
          dependsOnMethods = {"testAddMember"})
    public void testRemoveMember() {
        clusterContext.removeMember(clusterMember);
        assertEquals(clusterContext.getClusterMembers().size(), 0);
    }

    @Test (groups = {"wso2.carbon.clustering"},
           description = "test add cluster membership listener")
    public void testAddMembershipListener() {
        clusterContext.addMembershipListener(membershipListener);
        assertEquals(clusterContext.getMembershipListeners().get(0), membershipListener);
    }

    @Test (groups = {"wso2.carbon.clustering"},
           description = "test remove cluster membership listener",
           dependsOnMethods = {"testAddMembershipListener"})
    public void testRemoveMembershipListener() {
        clusterContext.removeMembershipListener(membershipListener);
        assertEquals(clusterContext.getMembershipListeners().size(), 0);
    }


}
