package org.wso2.carbon.clustering.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.clustering.CarbonCluster;
import org.wso2.carbon.clustering.ClusterConfiguration;
import org.wso2.carbon.clustering.ClusterContext;

public class DataHolder {
    private BundleContext bundleContext;

    private static DataHolder instance = new DataHolder();
    private CarbonCluster carbonCluster;
    private ClusterContext clusterContext;

    public  static DataHolder getInstance() {
        return instance;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setCarbonCluster(CarbonCluster carbonCluster) {
        this.carbonCluster = carbonCluster;
    }

    public CarbonCluster getCarbonCluster() {
        return carbonCluster;
    }

    public ClusterContext getClusterContext() {
        return clusterContext;
    }

    public void setClusterContext(ClusterContext clusterContext) {
        this.clusterContext = clusterContext;
    }
}
