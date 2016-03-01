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

import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.util.List;

/**
 * Service that exposes management functionalities for the data sources.
 */
public interface DataSourceManagementService {

    /**
     * Return all the registered data sources.
     *
     * @return {@code List<DataSourceMetadata>}
     * @throws DataSourceException
     */
    List<DataSourceMetadata> getMetadata() throws DataSourceException;

    /**
     * Returns a {@code DataSourceMetadata} for the given name.
     *
     * @param dataSourceName name of the data source
     * @return {@code CarbonDataSource}
     * @throws DataSourceException
     */
    DataSourceMetadata getMetadata(String dataSourceName) throws DataSourceException;

    /**
     * Add a new data source metadata object to the repository.
     *
     * @param dataSourceMetadata {@code DataSourceMetaInfo}
     * @throws DataSourceException
     */
    void addMetadata(DataSourceMetadata dataSourceMetadata) throws DataSourceException;

    /**
     * Deletes a data source from the repository.
     *
     * @param dataSourceName {@code String}
     * @throws DataSourceException
     */
    void deleteMetadata(String dataSourceName) throws DataSourceException;
}
