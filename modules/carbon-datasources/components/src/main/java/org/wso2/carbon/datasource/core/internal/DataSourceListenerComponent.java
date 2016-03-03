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
package org.wso2.carbon.datasource.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.api.DataSourceManagementService;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.impl.DataSourceManagementServiceImpl;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;
import org.wso2.carbon.datasource.utils.DataSourceUtils;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.util.HashMap;
import java.util.Map;

/**
 * DataSourceListenerComponent implements RequiredCapabilityListener interface. This wait till all the DataSourceReader
 * components are registered and then initialize the DataSourceManager. Followed by register the DataSourceService and
 * DataSourceManagementService.
 */
@Component(
        name = "org.wso2.carbon.kernel.datasource.core.internal.DataSourceListenerComponent",
        immediate = true,
        property = {
                "capability-name=org.wso2.carbon.datasource.core.spi.DataSourceReader",
                "component-key=carbon-datasource-service"
        }
)
public class DataSourceListenerComponent implements RequiredCapabilityListener {

    private static final Log log = LogFactory.getLog(DataSourceListenerComponent.class);

    private BundleContext bundleContext;
    private Map<String, DataSourceReader> readers;

    @Activate
    protected void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.readers = new HashMap<>();
    }

    @Reference(
            name = "carbon.datasource.DataSourceReader",
            service = DataSourceReader.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterReader"
    )
    protected void registerReader(DataSourceReader reader) {
        if (readers.containsKey(reader.getType())) {
            log.warn("A reader with the type " + reader.getType() + "already exists. "
                    + reader.getClass().toString() + " will be ignored.");
            return;
        }
        readers.put(reader.getType(), reader);
    }

    protected void unregisterReader(DataSourceReader reader) {
        readers.remove(reader);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        try {
            String dataSourcesPath = DataSourceUtils.getDataSourceConfigPath().toString();
            DataSourceManager dataSourceManager = DataSourceManager.getInstance();
            dataSourceManager.initDataSources(dataSourcesPath, readers);

            DataSourceService dsService = new DataSourceServiceImpl();
            bundleContext.registerService(DataSourceService.class.getName(), dsService, null);

            DataSourceManagementService dataSourceMgtService = new DataSourceManagementServiceImpl();
            bundleContext.registerService(DataSourceManagementService.class.getName(), dataSourceMgtService, null);
        } catch (DataSourceException e) {
            log.error("Error occurred while initializing data sources", e);
        }
    }
}
