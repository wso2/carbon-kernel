/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.jdbc.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSource;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.dataaccess.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.DatabaseConstants;
import org.wso2.carbon.registry.core.dataaccess.QueryProcessor;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link DataAccessManager} to access a back-end
 * JDBC-based database.
 */
public class JDBCDataAccessManager implements DataAccessManager {

	private static final Log log = LogFactory
			.getLog(JDBCDataAccessManager.class);
	private DataSource dataSource;
	private static ClusterLock clusterLock = new JDBCClusterLock();
	private static DatabaseTransaction databaseTransaction = new JDBCDatabaseTransaction();
	private static DAOManager daoManager = new JDBCDAOManager();
	private static Map<String, DataSource> dataSources = new HashMap<String, DataSource>();

	/**
	 * Constructor accepting a JDBC data source.
	 * 
	 * @param dataSource
	 *            the JDBC data source.
	 */
	public JDBCDataAccessManager(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Creates a JDBC Data Access Manager from the given database configuration.
	 * 
	 * @param dataBaseConfiguration
	 *            the database configuration.
	 */
	public JDBCDataAccessManager(DataBaseConfiguration dataBaseConfiguration) {
		final String dataSourceName = dataBaseConfiguration.getDataSourceName();
		if (dataSourceName != null) {
			try {
                dataSource = dataSources.get(dataSourceName);
                if (dataSource == null) {
                    Context context = new InitialContext();
                    dataSource = (DataSource) context.lookup(dataSourceName);
                    dataSources.put(dataSourceName, dataSource);
                }
			} catch (NamingException e) {
				// Problems!
				log.error("Couldn't find dataSource '" + dataSourceName + "'",
						e);
			}
		} else {
			String configName = dataBaseConfiguration.getConfigName();
			dataSource = dataSources.get(configName);
			if (dataSource == null) {
				dataSource = buildDataSource(dataBaseConfiguration);
				dataSources.put(configName, dataSource);
			}
		}
	}

	public ClusterLock getClusterLock() {
		return clusterLock;
	}

	public TransactionManager getTransactionManager() {
		return new JDBCTransactionManager(this);
	}

	public DatabaseTransaction getDatabaseTransaction() {
		return databaseTransaction;
	}

	public QueryProcessor getQueryProcessor() {
		return new SQLQueryProcessor(this);
	}

	public DAOManager getDAOManager() {
		return daoManager;
	}

	public void createDatabase() throws RegistryException {
		DatabaseCreator databaseCreator = new DatabaseCreator(getDataSource());

		try {
			databaseCreator.createRegistryDatabase();
		} catch (Exception e) {
			String msg = "Error in creating the database";
			throw new RegistryException(msg, e);
		}
	}

	public boolean isDatabaseExisting() {
		try {
			if (log.isTraceEnabled()) {
				log.trace("Running a query to test the database tables existence.");
			}
			// check whether the tables are already created with a query
			Connection conn = dataSource.getConnection();
			String sql = "SELECT REG_PATH_ID FROM REG_PATH WHERE REG_PATH_VALUE='/'";
			Statement statement = null;
			try {
				statement = conn.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				if (rs != null) {
					rs.close();
				}
			} finally {
				try {
					if (statement != null) {
						statement.close();
					}
				} finally {
					if (conn != null) {
						conn.close();
					}
				}
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	/**
	 * Method to retrieve the JDBC data source.
	 * 
	 * @return the JDBC data source.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	
	
	/**
	 * Method to build a data source from a given database configuration.
	 * 
	 * @param config
	 *            the database configuration.
	 * 
	 * @return the built data source.
	 */
	public static DataSource buildDataSource(DataBaseConfiguration config) {
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setUrl(config.getDbUrl());
        poolProperties.setDriverClassName(config.getDriverName());
        poolProperties.setUsername(config.getUserName());
        poolProperties.setPassword(config.getResolvedPassword());

		if (config.getTestWhileIdle() != null) {
            poolProperties.setTestWhileIdle(Boolean.parseBoolean(config
					.getTestWhileIdle()));
		}

		if (config.getTimeBetweenEvictionRunsMillis() != null) {
            poolProperties.setTimeBetweenEvictionRunsMillis(Integer
					.parseInt(config.getTimeBetweenEvictionRunsMillis()));
		}

		if (config.getMinEvictableIdleTimeMillis() != null) {
            poolProperties.setMinEvictableIdleTimeMillis(Integer.parseInt(config
					.getMinEvictableIdleTimeMillis()));
		}

		if (config.getNumTestsPerEvictionRun() != null) {
            poolProperties.setNumTestsPerEvictionRun(Integer.parseInt(config
					.getNumTestsPerEvictionRun()));
		}

		if (config.getMaxActive() != null) {
            poolProperties
					.setMaxActive(Integer.parseInt(config.getMaxActive()));
		} else {
            poolProperties.setMaxActive(DatabaseConstants.DEFAULT_MAX_ACTIVE);
		}

		if (config.getMaxWait() != null) {
            poolProperties.setMaxWait(Integer.parseInt(config.getMaxWait()));
		} else {
            poolProperties.setMaxWait(DatabaseConstants.DEFAULT_MAX_WAIT);
		}

		if (config.getMaxIdle() != null) {
            poolProperties.setMaxIdle(Integer.parseInt(config.getMaxIdle()));
		}

		if (config.getMinIdle() != null) {
            poolProperties.setMinIdle(Integer.parseInt(config.getMinIdle()));
		} else {
            poolProperties.setMinIdle(DatabaseConstants.DEFAULT_MIN_IDLE);
		}

		if (config.getValidationQuery() != null) {
            poolProperties.setValidationQuery(config.getValidationQuery());
		}
		try {
		    return new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
		} catch (Exception e) {
			throw new RuntimeException("Error in creating data source for the registry: " +
		            e.getMessage(), e);
		}
	}
}
