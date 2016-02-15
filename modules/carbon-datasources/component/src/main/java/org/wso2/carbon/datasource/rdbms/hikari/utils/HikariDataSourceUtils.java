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
package org.wso2.carbon.datasource.rdbms.hikari.utils;

import com.zaxxer.hikari.HikariConfig;
import org.wso2.carbon.datasource.core.common.DataSourceException;
import org.wso2.carbon.datasource.rdbms.hikari.HikariConfiguration;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.io.ByteArrayInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Encapsulates a set of utility methods for HikariDataSource.
 */
public class HikariDataSourceUtils {

    /**
     * Generate the configuration bean by reading the xml configuration.
     *
     * @param xmlConfiguration String
     * @return {@code HikariConfig}
     * @throws DataSourceException
     */
    public static HikariConfig buildConfiguration(String xmlConfiguration) throws DataSourceException {
        try {
            HikariConfiguration configuration = HikariDataSourceUtils.loadConfig(xmlConfiguration);
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(configuration.getUrl());
            config.setUsername(configuration.getUsername());
            config.setPassword(configuration.getPassword());
            config.addDataSourceProperty("cachePrepStmts", configuration.getCachePrepStmts());
            config.addDataSourceProperty("prepStmtCacheSize", configuration.getPrepStmtCacheSize());
            config.addDataSourceProperty("prepStmtCacheSqlLimit", configuration.getPrepStmtCacheSqlLimit());
            config.setDriverClassName(configuration.getDriverClassName());
            return config;
        } catch (DataSourceException e) {
            throw new DataSourceException("Error in loading Hikari configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Generate the configuration bean by reading the xml configuration.
     *
     * @param xmlConfiguration String
     * @return {@code HikariConfiguration}
     * @throws DataSourceException
     */
    public static HikariConfiguration loadConfig(String xmlConfiguration) throws DataSourceException {
        try {
            xmlConfiguration = DataSourceUtils.replaceSystemVariablesInXml(xmlConfiguration);
            JAXBContext ctx = JAXBContext.newInstance(HikariConfiguration.class);
            return (HikariConfiguration) ctx.createUnmarshaller().unmarshal(
                    new ByteArrayInputStream(xmlConfiguration.getBytes()));
        } catch (JAXBException e) {
            throw new DataSourceException("Error in loading X configuration: " + e.getMessage(), e);
        }
    }
}
