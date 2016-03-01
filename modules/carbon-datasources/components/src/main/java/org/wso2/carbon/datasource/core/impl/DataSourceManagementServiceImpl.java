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
package org.wso2.carbon.datasource.core.impl;

import org.wso2.carbon.datasource.core.DataSourceBuilder;
import org.wso2.carbon.datasource.core.DataSourceJndiManager;
import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.DataSourceRepository;
import org.wso2.carbon.datasource.core.api.DataSourceManagementService;
import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.util.ArrayList;
import java.util.List;

/**
 * DataSourceManagementServiceImpl is the implementation of DataSourceManagementService interface. This has implemented
 * the management operations allowed to perform on data sources.
 */
public class DataSourceManagementServiceImpl implements DataSourceManagementService {

    /**
     * Return all the registered data sources.
     *
     * @return {@code List<CarbonDataSource>}
     * @throws DataSourceException
     */
    public List<DataSourceMetadata> getMetadata() throws DataSourceException {
        return new ArrayList<>(DataSourceManager.getInstance().getDataSourceRepository().getMetadata());
    }

    /**
     * Returns a CarbonDataSource for the given name.
     *
     * @param dataSourceName name of the data source
     * @return {@code CarbonDataSource}
     * @throws DataSourceException
     */
    public DataSourceMetadata getMetadata(String dataSourceName) throws DataSourceException {
        return DataSourceManager.getInstance().getDataSourceRepository().getMetadata(dataSourceName);
    }

    /**
     * Returns the registered data source types.
     *
     * @return {@code List<String>}
     * @throws DataSourceException
     */
    public List<String> getDataSourceReaderTypes() throws DataSourceException {
        return DataSourceManager.getInstance().getDataSourceTypes();
    }

    /**
     * Add a new data source metadata object to the repository. This creates a CarbonDataSource and
     * register it in the JNDI context an the in memory repository.
     *
     * @param dataSourceMetaInfo {@code DataSourceMetaInfo}
     * @throws DataSourceException
     */
    public void addMetadata(DataSourceMetadata dataSourceMetaInfo) throws DataSourceException {
        CarbonDataSource cds = DataSourceBuilder.buildCarbonDataSource(dataSourceMetaInfo);
        DataSourceManager.getInstance().getDataSourceRepository().addDataSource(cds);
        DataSourceJndiManager.register(cds);
    }

    /**
     * Deletes a data source from the repository.
     *
     * @param dataSourceName {@code String}
     * @throws DataSourceException
     */
    public void deleteMetadata(String dataSourceName) throws DataSourceException {
        DataSourceRepository repository = DataSourceManager.getInstance().getDataSourceRepository();
        //Removal from the in memory repository
        repository.deleteDataSource(dataSourceName);

        //Removal from the JNDI context.
        CarbonDataSource carbonDataSource = repository.getDataSource(dataSourceName);
        DataSourceJndiManager.unregister(carbonDataSource);
    }
}
