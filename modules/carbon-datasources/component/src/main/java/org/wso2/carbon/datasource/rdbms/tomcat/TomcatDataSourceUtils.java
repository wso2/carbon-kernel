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
package org.wso2.carbon.datasource.rdbms.tomcat;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.wso2.carbon.datasource.core.common.DataSourceException;
import org.wso2.carbon.datasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.datasource.rdbms.RDBMSDataSourceConstants;
import org.wso2.carbon.datasource.rdbms.utils.RDBMSDataSourceUtils;

import java.sql.Connection;
import java.util.Properties;

public class TomcatDataSourceUtils {

    public static PoolConfiguration createPoolConfiguration(RDBMSConfiguration config)
            throws DataSourceException {
        PoolProperties props = new PoolProperties();
        props.setUrl(config.getUrl());
        if (config.isDefaultAutoCommit() != null) {
            props.setDefaultAutoCommit(config.isDefaultAutoCommit());
        }
        if (config.isDefaultReadOnly() != null) {
            props.setDefaultReadOnly(config.isDefaultReadOnly());
        }
        String isolationLevelString = config.getDefaultTransactionIsolation();
        if (isolationLevelString != null) {
            if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.NONE.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
            } else if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.READ_UNCOMMITTED.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            } else if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.READ_COMMITTED.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            } else if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.REPEATABLE_READ.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } else if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.SERIALIZABLE.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            }
        }
        props.setDefaultCatalog(config.getDefaultCatalog());
        props.setDriverClassName(config.getDriverClassName());
        String username = config.getUsername();
        if (null != username && !("").equals(username)) {
            props.setUsername(username);
            String password = config.getPassword();
            if (null != password && !("").equals(password)) {
                props.setPassword(password);
            }
        }
        if (config.getMaxActive() != null) {
            props.setMaxActive(config.getMaxActive());
        }
        if (config.getMaxIdle() != null) {
            props.setMaxIdle(config.getMaxIdle());
        }
        if (config.getMinIdle() != null) {
            props.setMinIdle(config.getMinIdle());
        }
        if (config.getInitialSize() != null) {
            props.setInitialSize(config.getInitialSize());
        }
        if (config.getMaxWait() != null) {
            props.setMaxWait(config.getMaxWait());
        }
        if (config.isTestOnBorrow() != null) {
            props.setTestOnBorrow(config.isTestOnBorrow());
        }
        if (config.isTestOnReturn() != null) {
            props.setTestOnReturn(config.isTestOnReturn());
        }
        if (config.isTestWhileIdle() != null) {
            props.setTestWhileIdle(config.isTestWhileIdle());
        }
        props.setValidationQuery(config.getValidationQuery());
        props.setValidatorClassName(config.getValidatorClassName());
        if (config.getTimeBetweenEvictionRunsMillis() != null) {
            props.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        }
        if (config.getNumTestsPerEvictionRun() != null) {
            props.setNumTestsPerEvictionRun(config.getNumTestsPerEvictionRun());
        }
        if (config.getMinEvictableIdleTimeMillis() != null) {
            props.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
        }
        if (config.isAccessToUnderlyingConnectionAllowed() != null) {
            props.setAccessToUnderlyingConnectionAllowed(
                    config.isAccessToUnderlyingConnectionAllowed());
        }
        if (config.isRemoveAbandoned() != null) {
            props.setRemoveAbandoned(config.isRemoveAbandoned());
        }
        if (config.getRemoveAbandonedTimeout() != null) {
            props.setRemoveAbandonedTimeout(config.getRemoveAbandonedTimeout());
        }
        if (config.isLogAbandoned() != null) {
            props.setLogAbandoned(config.isLogAbandoned());
        }
        props.setConnectionProperties(config.getConnectionProperties());
        props.setInitSQL(config.getInitSQL());
        props.setJdbcInterceptors(config.getJdbcInterceptors());
        if (config.getValidationInterval() != null) {
            props.setValidationInterval(config.getValidationInterval());
        }
        if (config.isJmxEnabled() != null) {
            props.setJmxEnabled(config.isJmxEnabled());
        }
        if (config.isFairQueue() != null) {
            props.setFairQueue(config.isFairQueue());
        }
        if (config.getAbandonWhenPercentageFull() != null) {
            props.setAbandonWhenPercentageFull(config.getAbandonWhenPercentageFull());
        }
        if (config.getMaxAge() != null) {
            props.setMaxAge(config.getMaxAge());
        }
        if (config.isUseEquals() != null) {
            props.setUseEquals(config.isUseEquals());
        }
        if (config.getSuspectTimeout() != null) {
            props.setSuspectTimeout(config.getSuspectTimeout());
        }
        if (config.getValidationQueryTimeout() != null) {
            props.setValidationQueryTimeout(config.getValidationQueryTimeout());
        }
        if (config.isAlternateUsernameAllowed() != null) {
            props.setAlternateUsernameAllowed(config.isAlternateUsernameAllowed());
        }
        if (config.getDataSourceClassName() != null) {
            handleExternalDataSource(props, config);
        }
        if (config.getDatabaseProps() != null) {
            Properties properties = new Properties();
            if (!config.getDatabaseProps().isEmpty()) {
                for (RDBMSConfiguration.DataSourceProperty property : config.getDatabaseProps()) {
                    properties.setProperty(property.getName(), property.getValue());
                }
            }
            props.setDbProperties(properties);
        }
        return props;
    }

    private static void handleExternalDataSource(PoolProperties poolProps, RDBMSConfiguration config)
            throws DataSourceException {
        String dsClassName = config.getDataSourceClassName();
        try {
            Object extDataSource = Class.forName(dsClassName).newInstance();
            RDBMSDataSourceUtils.assignBeanProps(extDataSource,
                    RDBMSDataSourceUtils.dataSourcePropsToMap(config.getDataSourceProps()));
            poolProps.setDataSource(extDataSource);
        } catch (Exception e) {
            throw new DataSourceException("Error in creating external data source: " +
                    e.getMessage(), e);
        }
    }
}
