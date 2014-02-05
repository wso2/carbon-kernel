package org.wso2.carbon.clustering.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.CarbonCluster;
import org.wso2.carbon.clustering.ClusterConfiguration;
import org.wso2.carbon.clustering.ClusterContext;
import org.wso2.carbon.clustering.ClusterUtil;
import org.wso2.carbon.clustering.api.Cluster;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.hazelcast.HazelcastClusteringAgent;
import org.wso2.carbon.clustering.spi.ClusteringAgent;


public class CarbonClusterBundleActivator implements BundleActivator {
    private static Logger logger = LoggerFactory.getLogger(CarbonClusterBundleActivator.class);

    private ClusteringAgent clusteringAgent = null;
    private DataHolder dataHolder = DataHolder.getInstance();

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        dataHolder.setBundleContext(bundleContext);
        ClusterContext clusterContext = new ClusterContext(new ClusterConfiguration());
        if (clusterContext.shouldInitialize("hazelcast")) {
            try {
                bundleContext.registerService(ClusterContext.class, clusterContext, null);
                dataHolder.setClusterContext(clusterContext);
                // Initialize the clustering agent
                logger.info("Initializing Clustering Agent");
                clusteringAgent = new HazelcastClusteringAgent();
                clusteringAgent.init(clusterContext);

                // Initialize and register carbon cluster service
                CarbonCluster carbonCluster = new CarbonCluster(clusteringAgent);
                bundleContext.registerService(Cluster.class, carbonCluster, null);
                dataHolder.setCarbonCluster(carbonCluster);
            } catch (ClusterInitializationException e) {
                logger.error("Error while initializing clustering", e);
            }
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (clusteringAgent != null) {
            logger.info("Shutting Down Clustering Agent");
            clusteringAgent.shutdown();
            dataHolder.setBundleContext(null);
        }
    }
}
