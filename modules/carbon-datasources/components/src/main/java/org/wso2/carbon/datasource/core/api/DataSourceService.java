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
package org.wso2.carbon.datasource.core.api;

import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
import org.wso2.carbon.datasource.core.beans.DataSourceMetaInfo;
import org.wso2.carbon.datasource.core.common.DataSourceException;

import java.util.List;

/**
 * DataSourceService interface which exposes the operations allowed to perform on data sources.
 */
public interface DataSourceService {

    /**
     * Return all the registered data sources.
     *
     * @return {@code List<CarbonDataSource>}
     * @throws DataSourceException
     */
    List<CarbonDataSource> getAllDataSources() throws DataSourceException;

    /**
     * Return all the registered data sources for the given type.
     *
     * @param dsType String
     * @return {@code List<CarbonDataSource>}
     * @throws DataSourceException
     */
    List<CarbonDataSource> getAllDataSourcesForType(String dsType) throws DataSourceException;

    /**
     * Returns a CarbonDataSource for the given name.
     *
     * @param dsName name of the data source
     * @return {@code CarbonDataSource}
     * @throws DataSourceException
     */
    CarbonDataSource getDataSource(String dsName) throws DataSourceException;

    /**
     * Returns the registered data source types.
     *
     * @return {@code List<String>}
     * @throws DataSourceException
     */
    List<String> getDataSourceTypes() throws DataSourceException;

    /**
     * Add a new data source to the data sources repository.
     *
     * @param dsmInfo {@code DataSourceMetaInfo}
     * @throws DataSourceException
     */
    void addDataSource(DataSourceMetaInfo dsmInfo) throws DataSourceException;

    /**
     * Deletes a data source from the repository.
     *
     * @param dsName {@code String}
     * @throws DataSourceException
     */
    void deleteDataSource(String dsName) throws DataSourceException;
}
