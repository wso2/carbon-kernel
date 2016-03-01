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
package org.wso2.carbon.datasource.core.beans;

/**
 * This class represents the full Carbon data source, including its meta-data,
 * status and the actual data source object.
 */
public class CarbonDataSource {

    private DataSourceMetadata dataSourceMetadata;

    private Object dataSourceObject;

    /**
     *
     * @param dataSourceMetadata {@code DataSourceMetadata}
     * @param dataSourceObject {@code Object} - This object is either a {@link javax.sql.DataSource}
     *                                       or {@link javax.naming.Reference}
     */
    public CarbonDataSource(DataSourceMetadata dataSourceMetadata, Object dataSourceObject) {
        this.dataSourceMetadata = dataSourceMetadata;
        this.dataSourceObject = dataSourceObject;
    }

    /**
     * Returns {@code DataSourceMetadata} object of this {@code CarbonDataSource}
     * @return {@code DataSourceMetadata}
     */
    public DataSourceMetadata getMetadata() {
        return dataSourceMetadata;
    }

    /**
     * Returns a data source object.
     * @return {@code Object}- This object should be either a {@link javax.sql.DataSource}
     *                                       or {@link javax.naming.Reference}
     */
    public Object getDataSourceObject() {
        return dataSourceObject;
    }

}
