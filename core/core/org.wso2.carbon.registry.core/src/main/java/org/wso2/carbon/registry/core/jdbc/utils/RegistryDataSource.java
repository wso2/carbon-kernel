/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.jdbc.utils;

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Use {@link DataAccessManager#createDatabase()}.
 */
@Deprecated
public class RegistryDataSource implements DataSource {

    private static final int DEFAULT_MAX_ACTIVE = 40;
    private static final int DEFAULT_MAX_WAIT = 1000 * 60;
    private static final int DEFAULT_MIN_IDLE = 5;

    private DataSource dataSource = null;

    /**
     * Constructor accepting a data source which will be wrapped as a registry data source
     *
     * @param dataSource
     */
    public RegistryDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public RegistryDataSource(DataBaseConfiguration config) {

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(config.getDbUrl());
        basicDataSource.setDriverClassName(config.getDriverName());
        basicDataSource.setUsername(config.getUserName());
        basicDataSource.setPassword(config.getResolvedPassword());

        if (config.getMaxActive() != null) {
            basicDataSource.setMaxActive(Integer.parseInt(config.getMaxActive()));
        } else {
            basicDataSource.setMaxActive(DEFAULT_MAX_ACTIVE);
        }

        if (config.getMaxWait() != null) {
            basicDataSource.setMaxWait(Integer.parseInt(config.getMaxWait()));
        } else {
            basicDataSource.setMaxWait(DEFAULT_MAX_WAIT);
        }

        if (config.getMaxIdle() != null) {
            basicDataSource.setMaxIdle(Integer.parseInt(config.getMaxIdle()));
        }

        if (config.getMinIdle() != null) {
            basicDataSource.setMinIdle(Integer.parseInt(config.getMinIdle()));
        } else {
            basicDataSource.setMinIdle(DEFAULT_MIN_IDLE);
        }

        this.dataSource = basicDataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    public <T> T unwrap(Class<T> i) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> i) throws SQLException {
        return false;
    }
    
    ////////////////////////////////////////////////////////
    // JDK 7 only methods.
    ////////////////////////////////////////////////////////

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// This is a JDK7 specific operation that we don't support for JDK5 and 6 compatibility
		throw new UnsupportedOperationException("This method is not supported");
	}
}
