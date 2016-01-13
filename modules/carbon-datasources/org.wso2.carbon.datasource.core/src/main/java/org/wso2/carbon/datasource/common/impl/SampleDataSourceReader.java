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
package org.wso2.carbon.datasource.common.impl;

import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.datasource.common.spi.DataSourceReader;

import javax.naming.Reference;

/**
 *
 */
public class SampleDataSourceReader implements DataSourceReader {
    @Override
    public String getType() {
        return "RDBMS";
    }

    @Override
    public Object createDataSource(String xmlConfiguration, boolean isDataSourceFactoryReference) throws DataSourceException {
        /**
         * This is only for the POC.
         */
        return new Reference("org.apache.tomcat.jdbc.pool.DataSource",
                "org.apache.tomcat.jdbc.pool.DataSourceFactory", null);
    }

    @Override
    public boolean testDataSourceConnection(String xmlConfiguration) throws DataSourceException {
        return false;
    }
}
