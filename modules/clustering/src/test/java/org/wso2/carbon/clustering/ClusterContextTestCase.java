package org.wso2.carbon.clustering;


import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.membership.listener.CustomMembershipListener;

import java.net.InetSocketAddress;
import java.util.UUID;

public class ClusterContextTestCase extends BaseTest {

    private ClusterContext clusterContext;
    private ClusterMember clusterMember;
    private CustomMembershipListener membershipListener;

    @BeforeTest
    public void setup() throws ClusterConfigurationException {
        clusterMember = new ClusterMember(UUID.randomUUID().toString(),
                                          new InetSocketAddress("127.0.0.0", 4000));
        membershipListener = new CustomMembershipListener();
        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.
                setClusterConfigurationXMLLocation(getTestResourceFile("cluster-00.xml").
                        getAbsolutePath());
        clusterConfiguration.build();
        clusterContext = new ClusterContext(clusterConfiguration);
    }

    @Test
    public void testAddMember() {
        clusterContext.addMember(clusterMember);
        Assert.assertEquals(clusterContext.getPrimaryClusterMembers().get(0), clusterMember);
    }

    @Test(dependsOnMethods = {"testAddMember"})
    public void testRemoveMember() {
        clusterContext.removeMember(clusterMember);
        Assert.assertEquals(clusterContext.getPrimaryClusterMembers().size(), 0);
    }

    @Test
    public void testAddMembershipListener() {
        clusterContext.addMembershipListener(membershipListener);
        Assert.assertEquals(clusterContext.getMembershipListeners().get(0), membershipListener);
    }

    @Test (dependsOnMethods = {"testAddMembershipListener"})
    public void testRemoveMembershipListener() {
        clusterContext.removeMembershipListener(membershipListener);
        Assert.assertEquals(clusterContext.getMembershipListeners().size(), 0);
    }


}
