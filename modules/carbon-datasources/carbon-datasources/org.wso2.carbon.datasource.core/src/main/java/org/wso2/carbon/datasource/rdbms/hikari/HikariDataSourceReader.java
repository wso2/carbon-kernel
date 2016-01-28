package org.wso2.carbon.datasource.rdbms.hikari;

import com.zaxxer.hikari.HikariConfig;
import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.datasource.common.spi.DataSourceReader;
import org.wso2.carbon.datasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.datasource.rdbms.RDBMSDataSourceConstants;
import org.wso2.carbon.datasource.rdbms.utils.RDBMSDataSourceUtils;

public class HikariDataSourceReader implements DataSourceReader {
    @Override
    public String getType() {
        return RDBMSDataSourceConstants.RDBMS_DATASOURCE_TYPE;
    }

    @Override
    public Object createDataSource(String xmlConfiguration, boolean isDataSourceFactoryReference) throws DataSourceException {
        HikariRDBMSDataSource dataSource = new HikariRDBMSDataSource(loadConfig(xmlConfiguration));
        if (isDataSourceFactoryReference) {
            return dataSource.getDataSourceFactoryReference();
        } else {
            return dataSource.getDataSource();
        }
    }


    public static HikariConfig loadConfig(String xmlConfiguration)
            throws DataSourceException {
        try {

            RDBMSConfiguration configuration = RDBMSDataSourceUtils.loadConfig(xmlConfiguration);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(configuration.getUrl());
            config.setUsername(configuration.getUsername());
            config.setPassword(configuration.getPassword());
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//            config.addDataSourceProperty("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
            config.setMinimumIdle(10);

            return config;
        } catch (Exception e) {
            throw new DataSourceException("Error in loading RDBMS configuration: " +
                    e.getMessage(), e);
        }
    }
}
