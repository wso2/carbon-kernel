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
import org.wso2.carbon.datasource.core.beans.DataSourceMetaInfo;
import org.wso2.carbon.datasource.core.common.DataSourceException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataSourceServiceImpl implements DataSourceService {

    @Override
    public List<CarbonDataSource> getAllDataSources() throws DataSourceException {
        return new ArrayList(DataSourceManager.getInstance().
                getDataSourceRepository().getAllDataSources());
    }

    @Override
    public List<CarbonDataSource> getAllDataSourcesForType(String dsType) throws DataSourceException {
        return DataSourceManager.getInstance().
                getDataSourceRepository().getAllDataSources().stream()
                .filter(cds -> cds.getDSMInfo().getDefinition().getType().equals(dsType))
                .collect(Collectors.toList());
    }

    @Override
    public CarbonDataSource getDataSource(String dsName) throws DataSourceException {
        return DataSourceManager.getInstance().getDataSourceRepository().getDataSource(dsName);
    }

    @Override
    public List<String> getDataSourceTypes() throws DataSourceException {
        return DataSourceManager.getInstance().getDataSourceTypes();
    }

    @Override
    public void addDataSource(DataSourceMetaInfo dsmInfo) throws DataSourceException {
        DataSourceManager.getInstance().getDataSourceRepository().addDataSource(dsmInfo);
    }

    @Override
    public void deleteDataSource(String dsName) throws DataSourceException {
        DataSourceManager.getInstance().getDataSourceRepository().deleteDataSource(dsName);
    }
}
