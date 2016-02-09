/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.datasource.rdbms.hikari;

import com.zaxxer.hikari.HikariConfig;
import org.wso2.carbon.datasource.core.common.DataSourceException;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;
import org.wso2.carbon.datasource.rdbms.RDBMSDataSourceConstants;
import org.wso2.carbon.datasource.rdbms.hikari.utils.HikariDataSourceUtils;

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
            HikariConfiguration configuration = HikariDataSourceUtils.loadConfig(xmlConfiguration);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(configuration.getUrl());
            config.setUsername(configuration.getUsername());
            config.setPassword(configuration.getPassword());
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setDriverClassName(configuration.getDriverClassName());
            config.setMinimumIdle(10);

            return config;
        } catch (Exception e) {
            throw new DataSourceException("Error in loading RDBMS configuration: " +
                    e.getMessage(), e);
        }
    }
}
