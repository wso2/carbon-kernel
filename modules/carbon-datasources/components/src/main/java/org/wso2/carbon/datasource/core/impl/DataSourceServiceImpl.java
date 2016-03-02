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

import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.util.List;

/**
 * DataSourceServiceImpl is the implementation of DataSourceService interface. This has implemented the operations
 * allowed to perform on data sources.
 */
public class DataSourceServiceImpl implements DataSourceService {

    /**
     * Return a data source with the given name.
     *
     * @param name Name of the data source.
     * @return {@code CarbonDataSource}
     * @throws DataSourceException
     */
    @Override
    public CarbonDataSource getDataSource(String name) throws DataSourceException {
        return DataSourceManager.getInstance().getDataSourceRepository().getDataSource(name);
    }
}
