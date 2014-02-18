package org.wso2.carbon.clustering;

import org.wso2.carbon.clustering.agent.CustomClusteringAgent;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;


public class MembershipSchemeBaseTest extends BaseTest{
    private CustomClusteringAgent clusteringAgent1;
    private CustomClusteringAgent clusteringAgent2;
    private ClusterContext clusterContext1;
    private ClusterContext clusterContext2;


    public void setupMembershipScheme(String instance1xml, String instance2xml)
            throws ClusterConfigurationException {
        String clusterXMLLocation1 = getTestResourceFile(instance1xml).getAbsolutePath();
        clusteringAgent1 = new CustomClusteringAgent();
        ClusterConfiguration clusterConfiguration1 = new ClusterConfiguration();
        clusterConfiguration1.setClusterConfigurationXMLLocation(clusterXMLLocation1);
        clusterConfiguration1.build();
        clusterContext1 = new ClusterContext(clusterConfiguration1);

        String clusterXMLLocation2 = getTestResourceFile(instance2xml).getAbsolutePath();
        clusteringAgent2 = new CustomClusteringAgent();
        ClusterConfiguration clusterConfiguration2 = new ClusterConfiguration();
        clusterConfiguration2.setClusterConfigurationXMLLocation(clusterXMLLocation2);
        clusterConfiguration2.build();
        clusterContext2 = new ClusterContext(clusterConfiguration2);
    }

    public void initializeMembershipScheme() throws ClusterInitializationException {
        clusteringAgent1.init(clusterContext1);
        clusteringAgent2.init(clusterContext2);
    }

    public int getNoOfMembers() {
        return clusteringAgent2.getAliveMemberCount();
    }

    public void terminate() {
        clusteringAgent1.shutdown();
        clusteringAgent2.shutdown();
    }
}
