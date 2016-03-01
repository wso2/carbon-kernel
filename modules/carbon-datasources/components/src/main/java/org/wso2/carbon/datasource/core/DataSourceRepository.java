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
package org.wso2.carbon.datasource.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataSourceRepository represents the repository which is used to hold the data sources.
 */
public class DataSourceRepository {

    private static Log log = LogFactory.getLog(DataSourceRepository.class);
    private Map<String, CarbonDataSource> dataSources;

    /**
     * Default constructor for DataSourceRepository.
     */
    public DataSourceRepository() {
        this.dataSources = new HashMap<>();
    }

    public List<DataSourceMetadata> getMetadata() {
        return dataSources.values().stream().map(CarbonDataSource::getMetadata).collect(Collectors.toList());
    }

    public DataSourceMetadata getMetadata(String dataSourceName) {
        CarbonDataSource carbonDataSource = getDataSource(dataSourceName);
        if(carbonDataSource != null) {
            return carbonDataSource.getMetadata();
        }
        return null;
    }

    /**
     * Gets information about all the data sources.
     *
     * @return A list of all data sources
     */
    public List<CarbonDataSource> getDataSources() {
        return dataSources.values().stream().collect(Collectors.toList());
    }


    /**
     * Gets information about a specific given data source.
     *
     * @param dataSourceName The name of the data source.
     * @return The data source information
     */
    public CarbonDataSource getDataSource(String dataSourceName) {
        return dataSources.get(dataSourceName);
    }

    /**
     * Adds a new data source to the repository.
     *
     * @param carbonDataSource The meta information of the data source to be added.
     */
    public void addDataSource(CarbonDataSource carbonDataSource) throws DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Adding data source: " + carbonDataSource.getMetadata().getName());
        }
        dataSources.put(carbonDataSource.getMetadata().getName(), carbonDataSource);
    }


    /**
     * Unregisters and deletes the data source from the repository.
     *
     * @param dataSourceName The data source name
     */
    public void deleteDataSource(String dataSourceName) throws DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting data source: " + dataSourceName);
        }
        CarbonDataSource cds = getDataSource(dataSourceName);
        if (cds == null) {
            throw new DataSourceException("Data source does not exist: " + dataSourceName);
        }
        dataSources.remove(dataSourceName);
    }
}
