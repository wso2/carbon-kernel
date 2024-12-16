package org.wso2.carbon.utils.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.utils.CarbonUtils;

import javax.sql.DataSource;

public class CarbonUtilsDataHolder {

    private static ConfigurationContext configContext;
    private DataSource dataSource;

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

        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {

        this.dataSource = dataSource;
    }
}
