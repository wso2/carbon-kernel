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
package org.wso2.carbon.clustering.internal;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.CarbonCluster;
import org.wso2.carbon.clustering.ClusterContext;
import org.wso2.carbon.clustering.ClusterUtil;
import org.wso2.carbon.clustering.ClusteringConstants;
import org.wso2.carbon.clustering.api.Cluster;
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.config.ClusterConfigFactory;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.spi.ClusteringAgent;

import java.util.Map;


@Component(
        name = "org.wso2.carbon.clustering.internal.CarbonClusterServiceComponent",
        description = "This service  component is responsible for retrieving the ClusteringAgent " +
                      "OSGi service and initialize the cluster",
        immediate = true
)

@Reference(
        name = "carbon.clustering.agent.service.listener",
        referenceInterface = ClusteringAgent.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setClusteringAgent",
        unbind = "unsetClusteringAgent"
)

public class CarbonClusterServiceComponent {

    private static Logger logger = LoggerFactory.getLogger(CarbonClusterServiceComponent.class);

    private DataHolder dataHolder = DataHolder.getInstance();
    private ServiceRegistration serviceRegistration;
    private ClusterConfiguration clusterConfiguration;


    @Activate
    protected void start() {
    }

    /**
     * This is the main method which checks the "Agent" property in clustering agent registrations
     * and calls the initialize method of clustering agent.
     * In here we are not using the @Activate method of this DS component because with multiple
     * clustering agent registrations we can't use the @Activate method as it gets called only once,
     * when the policy and cardinality satisfies. But the actual registered clustering agent may
     * get invoked after the @Activate method is called.
     */
    protected void setClusteringAgent(ClusteringAgent clusteringAgent, Map<String, ?> ref) {
        try {
            clusterConfiguration = ClusterConfigFactory.build();
            if (clusterConfiguration.isEnabled()) {
                Object clusterAgentTypeParam = ref.get(ClusteringConstants.CLUSTER_AGENT);
                if (clusterAgentTypeParam != null) {
                    String registeredAgentType = (String) clusterAgentTypeParam;
                    if (clusterConfiguration.getAgent().equals(registeredAgentType)) {
                        initializeCluster(clusteringAgent, clusterConfiguration);
                    } else {
                        logger.error("Unsupported clustering agent is registered : {} \n" +
                        "Expected clustering agent is : {}", clusterAgentTypeParam,
                                     clusterConfiguration.getAgent());
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("Error while initializing cluster configuration", e);
        }
    }

    protected void unsetClusteringAgent(ClusteringAgent clusteringAgent, Map<String, ?> ref) {
        if (clusterConfiguration.isEnabled()) {
            String registeredAgentType = (String) ref.get(ClusteringConstants.CLUSTER_AGENT);
            if (clusterConfiguration.getAgent().equals(registeredAgentType)) {
                terminateCluster(clusteringAgent);
            }
        }
    }

    private void initializeCluster(ClusteringAgent clusteringAgent,
                                   ClusterConfiguration clusterConfiguration) {
        try {
            // Create cluster context with the given cluster configuration
            ClusterContext clusterContext = new ClusterContext(clusterConfiguration);

            // Initialize the clustering agent
            logger.info("Initializing clustering agent");
            clusteringAgent.init(clusterContext);

            dataHolder.setClusterContext(clusterContext);

            // Initialize and register carbon cluster service
            CarbonCluster carbonCluster = new CarbonCluster(clusteringAgent);
            BundleContext bundleContext = dataHolder.getBundleContext();
            serviceRegistration = bundleContext.registerService(Cluster.class, carbonCluster, null);
            dataHolder.setCarbonCluster(carbonCluster);
        } catch (Throwable e) {
            logger.error("Error while initializing cluster", e);
        }
    }

    private void terminateCluster(ClusteringAgent clusteringAgent) {
        try {
            logger.info("Shutting down clustering agent");
            serviceRegistration.unregister();
            dataHolder.setClusterContext(null);
            dataHolder.setCarbonCluster(null);
            clusteringAgent.shutdown();
        } catch (Throwable ignore) {
        }
    }
}
