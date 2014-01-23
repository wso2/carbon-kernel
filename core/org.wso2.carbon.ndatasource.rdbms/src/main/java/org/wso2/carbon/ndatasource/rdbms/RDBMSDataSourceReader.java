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

import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;
import org.wso2.carbon.utils.CarbonUtils;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class represents the RDBMS based data source reader implementation.
 */
public class RDBMSDataSourceReader implements DataSourceReader {
	
	@Override
	public String getType() {
		return RDBMSDataSourceConstants.RDBMS_DATASOURCE_TYPE;
	}

	public static RDBMSConfiguration loadConfig(String xmlConfiguration) 
			throws DataSourceException {
		try {
            xmlConfiguration = CarbonUtils.replaceSystemVariablesInXml(xmlConfiguration);
		    JAXBContext ctx = JAXBContext.newInstance(RDBMSConfiguration.class);
		    return (RDBMSConfiguration) ctx.createUnmarshaller().unmarshal(
		    		new ByteArrayInputStream(xmlConfiguration.getBytes()));
		} catch (Exception e) {
			throw new DataSourceException("Error in loading RDBMS configuration: " +
		            e.getMessage(), e);
		}
	}

	@Override
	public Object createDataSource(String xmlConfiguration, boolean isDataSourceFactoryReference)
			throws DataSourceException {
		if (isDataSourceFactoryReference) {
			return (new RDBMSDataSource(loadConfig(xmlConfiguration)).getDataSourceFactoryReference());
		} else {
			return (new RDBMSDataSource(loadConfig(xmlConfiguration)).getDataSource());
		}
	}

	@Override
	public boolean testDataSourceConnection(String xmlConfiguration) throws DataSourceException {
		RDBMSConfiguration rdbmsConfiguration = loadConfig(xmlConfiguration);
		DataSource dataSource = new RDBMSDataSource(rdbmsConfiguration).getDataSource();
		
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			throw new DataSourceException("Error establishing data source connection: " +
		            e.getMessage(), e);
		} 
        if (connection != null) {
        	String validationQuery = rdbmsConfiguration.getValidationQuery();
        	if (validationQuery != null && !"".equals(validationQuery)) {
        		PreparedStatement ps = null;
                try {
                	ps = connection.prepareStatement(validationQuery.trim());
                    ps.execute();
                    ps.close();
                } catch (SQLException e) {
                    throw new DataSourceException("Error during executing validation query: " +
            		            e.getMessage(), e);
                    } 
                }
        	try {
				connection.close();
			} catch (SQLException ignored) {
				
			}
        }
 		return true;
	}

}
