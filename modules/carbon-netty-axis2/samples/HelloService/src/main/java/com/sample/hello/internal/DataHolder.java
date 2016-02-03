package com.sample.hello.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.osgi.framework.BundleContext;

/**
 * Created by jayanga on 1/22/16.
 */
public class DataHolder {
    private static DataHolder dataHolder = new DataHolder();
    private ConfigurationContext configurationContext;

    private DataHolder(){}
    public static DataHolder getInstance() {
        return dataHolder;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }
}
