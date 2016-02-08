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

public interface DataSourceService {

    List<CarbonDataSource> getAllDataSources() throws DataSourceException;

    List<CarbonDataSource> getAllDataSourcesForType(String dsType) throws DataSourceException;

    CarbonDataSource getDataSource(String dsName) throws DataSourceException;

    List<String> getDataSourceTypes() throws DataSourceException;

    void addDataSource(DataSourceMetaInfo dsmInfo) throws DataSourceException;

    void deleteDataSource(String dsName) throws DataSourceException;
}
