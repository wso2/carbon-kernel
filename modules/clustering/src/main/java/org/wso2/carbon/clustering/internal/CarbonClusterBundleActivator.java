package org.wso2.carbon.clustering.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterUtil;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.hazelcast.HazelcastClusteringAgent;
import org.wso2.carbon.clustering.spi.ClusteringAgent;


public class CarbonClusterBundleActivator implements BundleActivator {
    private static Logger logger = LoggerFactory.getLogger(CarbonClusterBundleActivator.class);

    private ClusteringAgent clusteringAgent = null;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        DataHolder.getInstance().setBundleContext(bundleContext);
        if (ClusterUtil.shouldInitialize("hazelcast")) {
            try {
                clusteringAgent = new HazelcastClusteringAgent();
                logger.info("Initializing Clustering Agent");
                clusteringAgent.init();
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
        }
    }
}
