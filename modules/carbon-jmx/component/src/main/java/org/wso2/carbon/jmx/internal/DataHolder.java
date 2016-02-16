package org.wso2.carbon.jmx.internal;

import javax.management.MBeanServer;

public class DataHolder {
    private static DataHolder instance = new DataHolder();
    MBeanServer mBeanServer;

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return instance;
    }

    public MBeanServer getmBeanServer() {
        return mBeanServer;
    }

    public void setmBeanServer(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }
}
