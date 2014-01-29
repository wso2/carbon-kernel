package org.wso2.carbon.clustering.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.clustering.api.Cluster;
import org.wso2.carbon.clustering.hazelcast.HazelcastCarbonCluster;

public class DataHolder {
    private BundleContext bundleContext;

    private static DataHolder instance = new DataHolder();
    private HazelcastCarbonCluster carbonCluster;

    public  static DataHolder getInstance() {
        return instance;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setCarbonCluster(HazelcastCarbonCluster carbonCluster) {
        this.carbonCluster = carbonCluster;
    }

    public HazelcastCarbonCluster getCarbonCluster() {
        return carbonCluster;
    }
}
