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
import org.wso2.carbon.clustering.ClusterConfiguration;
import org.wso2.carbon.clustering.ClusterContext;
import org.wso2.carbon.clustering.ClusterUtil;
import org.wso2.carbon.clustering.ClusteringConstants;
import org.wso2.carbon.clustering.api.Cluster;
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
    private String clusteringAgentType;


    @Activate
    protected void start() {
    }

    protected void setClusteringAgent(ClusteringAgent clusteringAgent, Map<String, ?> ref) {
        if (ClusterUtil.isClusteringEnabled()) {
            Object clusterAgentTypeParam = ref.get(ClusteringConstants.CLUSTER_AGENT_TYPE);
            if (clusterAgentTypeParam != null) {
                ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
                String registeredAgentType = (String) clusterAgentTypeParam;
                try {
                    if (clusterConfiguration.shouldInitialize(registeredAgentType)) {
                        initializeCluster(clusteringAgent, clusterConfiguration);
                        clusteringAgentType = registeredAgentType;
                    } else {
                        logger.error("Unsupported clustering agent type is registered : {} ",
                                     registeredAgentType);
                    }
                } catch (ClusterConfigurationException e) {
                    logger.error("Error while initializing cluster configuration", e);
                }
            }
        }
    }

    protected void unsetClusteringAgent(ClusteringAgent clusteringAgent, Map<String, ?> ref) {
        if (ClusterUtil.isClusteringEnabled()) {
            String registeredAgentType = (String) ref.get(ClusteringConstants.CLUSTER_AGENT_TYPE);
            if (clusteringAgentType.equals(registeredAgentType)) {
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

            // Initialize and register carbon cluster service
            CarbonCluster carbonCluster = new CarbonCluster(clusteringAgent);
            BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
            serviceRegistration = bundleContext.registerService(Cluster.class, carbonCluster, null);
            dataHolder.setCarbonCluster(carbonCluster);
        } catch (ClusterInitializationException e) {
            logger.error("Error while initializing cluster", e);
        }
    }

    private void terminateCluster(ClusteringAgent clusteringAgent) {
        try {
            logger.info("Shutting down clustering agent");
            serviceRegistration.unregister();
            clusteringAgent.shutdown();
        } catch (Exception ignore) {
        }
    }
}
