package org.wso2.carbon.extensions.internal;


import org.osgi.framework.BundleContext;

public class DataHolder {
    private  static DataHolder instance = new DataHolder();
    private BundleContext bundleContext;

    public  static DataHolder getInstance() {
        return instance;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }
}
