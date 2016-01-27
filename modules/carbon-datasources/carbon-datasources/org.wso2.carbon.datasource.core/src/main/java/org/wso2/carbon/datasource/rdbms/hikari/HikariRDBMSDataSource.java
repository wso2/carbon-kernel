package org.wso2.carbon.datasource.rdbms.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.datasource.rdbms.utils.RDBMSDataSourceUtils;

import java.util.Map;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class HikariRDBMSDataSource {
    private static Log log = LogFactory.getLog(HikariRDBMSDataSource.class);

    private HikariDataSource dataSource;

    private Reference dataSourceFactoryReference;


    private HikariConfig config;

    public HikariRDBMSDataSource(HikariConfig config) throws DataSourceException {
        this.config = config;
    }

    public HikariDataSource getDataSource() {
        if (this.dataSource == null) {
            this.dataSource = new HikariDataSource(this.config);
        }
        return this.dataSource;
    }

    public Reference getDataSourceFactoryReference() throws DataSourceException {
        if (dataSourceFactoryReference == null) {
            dataSourceFactoryReference = new Reference("javax.sql.DataSource",
                    "com.zaxxer.hikari.HikariJNDIFactory", null);

            Map<String, String> poolConfigMap =
                    RDBMSDataSourceUtils.extractPrimitiveFieldNameValuePairs(this.config);
            poolConfigMap.forEach((key, value) -> dataSourceFactoryReference.add(new StringRefAddr(key, value)));
        }
        return dataSourceFactoryReference;
    }
}
