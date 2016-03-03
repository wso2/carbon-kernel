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
import org.wso2.carbon.datasource.core.spi.DataSourceReader;

import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;

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
    public List<DataSourceMetadata> getDataSource() throws DataSourceException {
        return new ArrayList<>(DataSourceManager.getInstance().getDataSourceRepository().getMetadata());
    }

    /**
     * Returns a CarbonDataSource for the given name.
     *
     * @param dataSourceName name of the data source
     * @return {@code CarbonDataSource}
     * @throws DataSourceException
     */
    public DataSourceMetadata getDataSource(String dataSourceName) throws DataSourceException {
        return DataSourceManager.getInstance().getDataSourceRepository().getMetadata(dataSourceName);
    }

    /**
     * Add a new data source metadata object to the repository. This creates a CarbonDataSource and
     * register it in the JNDI context an the in memory repository.
     *
     * @param dataSourceMetadata {@code DataSourceMetaInfo}
     * @throws DataSourceException
     */
    public void addDataSource(DataSourceMetadata dataSourceMetadata) throws DataSourceException {
        DataSourceManager dataSourceManager =  DataSourceManager.getInstance();
        String dataSourceType = dataSourceMetadata.getDefinition().getType();
        DataSourceReader dataSourceReader = dataSourceManager.getDataSourceReader(dataSourceType);
        CarbonDataSource cds = DataSourceBuilder.buildCarbonDataSource(dataSourceMetadata, dataSourceReader);
        DataSourceManager.getInstance().getDataSourceRepository().addDataSource(cds);
        try {
            DataSourceJndiManager.register(cds, dataSourceReader);
        } catch (NamingException e) {
            throw new DataSourceException("Error occurred while binding data source into JNDI context", e);
        }
    }

    /**
     * Deletes a data source from the repository.
     *
     * @param dataSourceName {@code String}
     * @throws DataSourceException
     */
    public void deleteDataSource(String dataSourceName) throws DataSourceException {
        DataSourceRepository repository = DataSourceManager.getInstance().getDataSourceRepository();
        CarbonDataSource carbonDataSource = repository.getDataSource(dataSourceName);

        //Removal from the in memory repository
        repository.deleteDataSource(dataSourceName);

        //Removal from the JNDI context.
        try {
            DataSourceJndiManager.unregister(carbonDataSource);
        } catch (NamingException e) {
            throw new DataSourceException("Error occurred while unbinding data source in JNDI context", e);
        }
    }
}
