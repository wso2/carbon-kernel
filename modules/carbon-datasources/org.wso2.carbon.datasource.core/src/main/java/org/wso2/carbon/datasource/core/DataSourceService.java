///*
// *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//package org.wso2.carbon.datasource.core;
//
//import org.wso2.carbon.datasource.common.DataSourceException;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * This class represents the data source OSGi service.
// */
//public class DataSourceService {
//
//    public List<CarbonDataSource> getAllDataSources() throws DataSourceException {
//        return new ArrayList<CarbonDataSource>(DataSourceManager.getInstance().
//                getDataSourceRepository().getAllDataSources());
//    }
//
//    public List<CarbonDataSource> getAllDataSourcesForType(String dsType)
//            throws DataSourceException {
//        List<CarbonDataSource> result = new ArrayList<CarbonDataSource>();
//        for (CarbonDataSource cds : DataSourceManager.getInstance().
//                getDataSourceRepository().getAllDataSources()) {
//            if (dsType.equals(cds.getDSMInfo().getDefinition().getType())) {
//                result.add(cds);
//            }
//        }
//        return result;
//    }
//
//    public CarbonDataSource getDataSource(String dsName) throws DataSourceException {
//        return DataSourceManager.getInstance().getDataSourceRepository().getDataSource(dsName);
//    }
//
//    public List<String> getDataSourceTypes() throws DataSourceException {
//        return DataSourceManager.getInstance().getDataSourceTypes();
//    }
//
//    public void addDataSource(DataSourceMetaInfo dsmInfo) throws DataSourceException {
//        DataSourceManager.getInstance().getDataSourceRepository().addDataSource(dsmInfo);
//    }
//
//    public void deleteDataSource(String dsName) throws DataSourceException {
//        DataSourceManager.getInstance().getDataSourceRepository().deleteDataSource(dsName);
//    }
//
//}
