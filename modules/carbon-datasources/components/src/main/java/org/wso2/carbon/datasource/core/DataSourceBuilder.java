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
package org.wso2.carbon.datasource.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import javax.naming.Reference;

/**
 * DataSourceBuilder is a util class responsible to build data source by passing the data source configuration to the
 * relevant DataSourceReader object.
 */
public class DataSourceBuilder {

    private static Log log = LogFactory.getLog(DataSourceBuilder.class);

    /**
     * Build a {@code CarbonDataSource} object from the given {@code DataSourceMetaInfo} object.
     *
     * @param dataSourceMetadata {@code DataSourceMetaInfo}
     * @throws DataSourceException
     */
    public static CarbonDataSource buildCarbonDataSource(DataSourceMetadata dataSourceMetadata,
                                                         DataSourceReader dataSourceReader)
            throws DataSourceException {
        Object dataSource = buildDataSourceObject(dataSourceMetadata, false, dataSourceReader);
        return new CarbonDataSource(dataSourceMetadata, dataSource);
    }

    /**
     * Creates the data source object by getting the appropriate DataSourceReader. The created object would be either
     * a {@link javax.sql.DataSource} or {@link Reference} if {@code isUseJndiReference} param is true.
     *
     * @param dataSourceMetadata                {@code DataSourceMetaInfo}
     * @param isUseDataSourceFactory {@code boolean}
     * @return {@code Object}
     */
    public static Object buildDataSourceObject(DataSourceMetadata dataSourceMetadata, boolean isUseDataSourceFactory,
                                               DataSourceReader dataSourceReader)
            throws DataSourceException {
        if(log.isDebugEnabled()) {
            log.debug("Generating the DataSource object from \"" + dataSourceReader.getType() + "\" type reader.");
        }

        Element configurationXmlDefinition = (Element) dataSourceMetadata.getDefinition().getDsXMLConfiguration();
        return dataSourceReader.createDataSource(DataSourceUtils.elementToString(configurationXmlDefinition),
                isUseDataSourceFactory);
    }
}
