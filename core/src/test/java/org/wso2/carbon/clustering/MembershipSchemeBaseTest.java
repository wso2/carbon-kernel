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

import org.wso2.carbon.clustering.agent.CustomClusteringAgent;
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.internal.DataHolder;
import org.wso2.carbon.internal.clustering.CarbonCluster;
import org.wso2.carbon.internal.clustering.ClusterContext;


public class MembershipSchemeBaseTest extends BaseTest{
    private CustomClusteringAgent clusteringAgent1;
    private CustomClusteringAgent clusteringAgent2;
    private ClusterContext clusterContext1;
    private ClusterContext clusterContext2;
    private CarbonCluster carbonCluster;


    public void setupMembershipScheme(String instance1xml, String instance2xml)
            throws ClusterConfigurationException {
        String clusterXMLLocation1 = getTestResourceFile(instance1xml).getAbsolutePath();
        clusteringAgent1 = new CustomClusteringAgent();
        ClusterConfiguration clusterConfiguration1 = buildClusterConfig(clusterXMLLocation1);
        clusterContext1 = new ClusterContext(clusterConfiguration1);

        String clusterXMLLocation2 = getTestResourceFile(instance2xml).getAbsolutePath();
        clusteringAgent2 = new CustomClusteringAgent();
        ClusterConfiguration clusterConfiguration2 = buildClusterConfig(clusterXMLLocation2);
        clusterContext2 = new ClusterContext(clusterConfiguration2);
    }

    public void initializeMembershipScheme() throws ClusterInitializationException {
        clusteringAgent1.init(clusterContext1);
        clusteringAgent2.init(clusterContext2);

        carbonCluster = new CarbonCluster(clusteringAgent1);
        DataHolder.getInstance().setCarbonCluster(carbonCluster);
        DataHolder.getInstance().setClusterContext(clusterContext1);
    }

    public int getNoOfMembers() {
        return clusteringAgent2.getAliveMemberCount();
    }

    public CarbonCluster getClusterService() {
        return carbonCluster;
    }

    public ClusterContext getClusterContext() {
        return clusterContext1;
    }

    public void terminate() {
        clusteringAgent1.shutdown();
        clusteringAgent2.shutdown();
    }

}
