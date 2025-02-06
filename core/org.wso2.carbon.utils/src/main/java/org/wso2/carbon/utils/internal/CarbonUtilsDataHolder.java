package org.wso2.carbon.utils.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.naming.Context;
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
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (dataSource == null && tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                LOG.warn("KeyStore Data source not found: " + dataSourceName + " in tenant space for tenant Id: " +
                        tenantId + ". Hence trying to get the data source from Super Tenant space.");
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext privilegedCarbonContext =
                            PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    try {
                        Context context = new InitialContext();
                        dataSource = (DataSource) context.lookup(dataSourceName);
                    } catch (NamingException e) {
                        LOG.error("Couldn't find KeyStore dataSource in Super Tenant Space'" + dataSourceName + "'", e);
                        throw new RuntimeException("Error in looking up keystore data source.", e);
                    }
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        } else {
            throw new RuntimeException("Data source name is not configured for KeyStore Data Persistence Manager.");
        }
    }
}
