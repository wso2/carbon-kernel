package org.wso2.carbon.utils.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class CarbonUtilsDataHolder {

    private static final Log LOG = LogFactory.getLog(CarbonUtilsDataHolder.class);
    private static ConfigurationContext configContext;
    private static DataSource dataSource;

    private static CarbonUtilsDataHolder carbonUtilsDataHolder = new CarbonUtilsDataHolder();

    public static CarbonUtilsDataHolder getInstance() {

        return carbonUtilsDataHolder;
    }

    public static void setConfigContext(ConfigurationContext configContext) {
        CarbonUtilsDataHolder.configContext = configContext;
    }

    public static ConfigurationContext getConfigContext() {
        CarbonUtils.checkSecurity();
        return configContext;
    }

    public DataSource getDataSource() {

        if (dataSource == null) {
            LOG.warn("Key Store Data source is not initialized. Hence Initializing the data source again.");
            initializeDatasource();
        }
        return dataSource;
    }

    public void setDataSource() {

        LOG.info("Initializing KeyStore Data Source at the bundle activation.");
        initializeDatasource();
        if (dataSource == null) {
            LOG.error("KeyStore Data source was not initialized at the bundle activation.");
        }
    }

    private static synchronized void initializeDatasource() {

        String dataSourceName = CarbonUtils.getServerConfiguration().getFirstProperty(
                "KeyStoreDataPersistenceManager.DataSourceName");
        if (StringUtils.isNotBlank(dataSourceName)) {
            try {
                dataSource = InitialContext.doLookup(dataSourceName);
                if (dataSource == null) {
                    LOG.warn("Data source for KeyStore Data Persistence not found: " + dataSourceName);
                }
            } catch (NamingException e) {
                LOG.error("Error in looking up keystore data source due to a NamingException: ", e);
                throw new RuntimeException("Error in looking up keystore data source.", e);
            }
        } else {
            throw new RuntimeException("Data source name is not configured for KeyStore Data Persistence Manager.");
        }
    }
}
