package org.wso2.carbon.datasource.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.datasource.core.DataSourceManager;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataSourceBundleActivator implements BundleActivator {

    private static final Log log = LogFactory.getLog(DataSourceBundleActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        log.info("Activating data source bundle");
        DataSourceManager.getInstance().initSystemDataSources();
    }


    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
