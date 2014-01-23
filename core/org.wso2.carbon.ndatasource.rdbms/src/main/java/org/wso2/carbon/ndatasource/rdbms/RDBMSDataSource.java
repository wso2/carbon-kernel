/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ndatasource.rdbms;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.rdbms.utils.RDBMSDataSourceUtils;

/**
 * RDBMS data source implementation.
 */
public class RDBMSDataSource {

	private DataSource dataSource;
	
	private Reference dataSourceFactoryReference;
	
	private PoolConfiguration poolProperties;
	
	public RDBMSDataSource(RDBMSConfiguration config) throws DataSourceException {
		this.poolProperties = RDBMSDataSourceUtils.createPoolConfiguration(config);
		this.populateStandardProps();
	}
	
	private void populateStandardProps() {
		String jdbcInterceptors = this.poolProperties.getJdbcInterceptors();
		if (jdbcInterceptors == null) {
			jdbcInterceptors = "";
		}
		jdbcInterceptors = RDBMSDataSourceConstants.STANDARD_JDBC_INTERCEPTORS + jdbcInterceptors;
		this.poolProperties.setJdbcInterceptors(jdbcInterceptors);
	}
	
	public DataSource getDataSource() {
		if (this.dataSource == null) {
			this.dataSource = new DataSource(poolProperties);
		}
		return this.dataSource;
	}
	
	public Reference getDataSourceFactoryReference() throws DataSourceException {
		if (dataSourceFactoryReference == null) {
			dataSourceFactoryReference = new Reference("org.apache.tomcat.jdbc.pool.DataSource",
	                "org.apache.tomcat.jdbc.pool.DataSourceFactory", null);
			
			Map<String, String> poolConfigMap = RDBMSDataSourceUtils.extractPrimitiveFieldNameValuePairs(poolProperties);
			Iterator<Entry<String, String>> poolConfigMapIterator = poolConfigMap.entrySet().iterator();
			
			while (poolConfigMapIterator.hasNext()) {
				Entry<String, String> pairs = poolConfigMapIterator.next();
				dataSourceFactoryReference.add(new StringRefAddr(pairs.getKey(),
						pairs.getValue()));
			}
		}
		return dataSourceFactoryReference;
	}
	
}
