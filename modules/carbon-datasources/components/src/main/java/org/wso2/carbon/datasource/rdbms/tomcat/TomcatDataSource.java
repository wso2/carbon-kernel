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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.rdbms.tomcat.utils.TomcatDataSourceUtils;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Map;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

/**
 * Wrapper class which wraps {@code org.apache.tomcat.jdbc.pool.DataSource} with a set of utility methods.
 */
public class TomcatDataSource {

    private static Log log = LogFactory.getLog(TomcatDataSource.class);

    private static final String STANDARD_TOMCAT_JDBC_INTERCEPTORS = "ConnectionState;StatementFinalizer;" +
            "org.wso2.carbon.datasource.rdbms.tomcat.ConnectionRollbackOnReturnInterceptor;";

    private DataSource dataSource;

    private Reference dataSourceFactoryReference;

    private PoolConfiguration poolProperties;

    /**
     * Constructs TomcatDataSource object.
     *
     * @param config {@code TomcatDataSourceConfiguration}
     * @throws DataSourceException
     */
    public TomcatDataSource(TomcatDataSourceConfiguration config) throws DataSourceException {
        this.poolProperties = TomcatDataSourceUtils.createPoolConfiguration(config);
        this.populateStandardProps();
    }

    /**
     * Add jdbc interceptors to the poolProperties.
     */
    private void populateStandardProps() {
        String jdbcInterceptors = this.poolProperties.getJdbcInterceptors();
        if (jdbcInterceptors == null) {
            jdbcInterceptors = "";
        }
        jdbcInterceptors = STANDARD_TOMCAT_JDBC_INTERCEPTORS + jdbcInterceptors;
        this.poolProperties.setJdbcInterceptors(jdbcInterceptors);
    }

    /**
     * Returns a DataSource object.
     * @return {@link DataSource}
     */
    public DataSource getDataSource() {
        if (this.dataSource == null) {
            this.dataSource = new DataSource(poolProperties);
        }
        if (poolProperties.isJmxEnabled()) {
            this.registerMBean();
        }
        return this.dataSource;
    }

    /**
     * Register MBeans.
     */
    private void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        String mBean = "";
        try {
            if (DataSourceUtils.getCurrentDataSourceId() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("The current dataSource id is not set");
                }
                return;
            }
            String[] dataSourceId = DataSourceUtils.getCurrentDataSourceId().split(":");
            mBean = dataSourceId[1] + "," + dataSourceId[0];
            ObjectName objectName = new ObjectName(mBean + ":type=DataSource");
            mBeanServer.registerMBean(this.dataSource.createPool().getJmxPool(), objectName);
        } catch (InstanceAlreadyExistsException e) {
            log.warn("Registering already existing mbean. '"
                    + mBean + "' " + e.getMessage(), e);
        } catch (MalformedObjectNameException | NotCompliantMBeanException | SQLException
                | MBeanRegistrationException e) {
            log.error("Error while registering the MBean for dataSource '"  + mBean + " " + e.getMessage(), e);
        }
    }

    /**
     * Returns a {@link Reference} object representing the DataSource.
     *
     * @return {@link Reference}
     * @throws DataSourceException
     */
    public Reference getDataSourceFactoryReference() throws DataSourceException {
        if (dataSourceFactoryReference == null) {
            dataSourceFactoryReference = new Reference("org.apache.tomcat.jdbc.pool.DataSource",
                    "org.apache.tomcat.jdbc.pool.DataSourceFactory", null);

            Map<String, String> poolConfigMap =
                    DataSourceUtils.extractPrimitiveFieldNameValuePairs(poolProperties);
            poolConfigMap.forEach((key, value) -> dataSourceFactoryReference.add(new StringRefAddr(key, value)));
        }
        return dataSourceFactoryReference;
    }

}
