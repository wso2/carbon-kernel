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

    private DataHolder dataHolder = DataHolder.getInstance();

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        dataHolder.setBundleContext(bundleContext);
        ClusterContext clusterContext = new ClusterContext(new ClusterConfiguration());
        dataHolder.setClusterContext(clusterContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        dataHolder.setBundleContext(null);
        dataHolder.setClusterContext(null);
    }
}
