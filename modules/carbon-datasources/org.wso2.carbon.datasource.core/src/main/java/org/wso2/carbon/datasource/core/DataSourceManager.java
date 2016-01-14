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
import org.w3c.dom.Document;
import org.wso2.carbon.datasource.common.DataSourceConstants;
import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.datasource.common.spi.DataSourceReader;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;

/**
 * This class contains the functionality in managing the data sources.
 */
public class DataSourceManager {

    private static Log log = LogFactory.getLog(DataSourceManager.class);

    private static DataSourceManager instance = new DataSourceManager();

    private Map<String, DataSourceReader> dsReaders;

    private static DataSourceRepository dsRepo;
    private String dataSourcesPath = null;
    private boolean initialized = false;

    public DataSourceManager() {
        this.dsReaders = new HashMap<>();
    }

    public static DataSourceManager getInstance() {
        return instance;
    }

    public DataSourceRepository getDataSourceRepository() throws DataSourceException {
        if (dsRepo == null) {
            dsRepo = new DataSourceRepository();
        }
        return dsRepo;
    }

    public List<String> getDataSourceTypes() throws DataSourceException {
        if (this.dsReaders == null) {
            throw new DataSourceException("The data source readers are not initialized yet");
        }
        return new ArrayList<>(this.dsReaders.keySet());
    }

    public DataSourceReader getDataSourceReader(String dsType) throws DataSourceException {
        if (this.dsReaders == null) {
            throw new DataSourceException("The data source readers are not initialized yet");
        }
        return this.dsReaders.get(dsType);
    }

    private void addDataSourceProviders(List<String> providers) throws DataSourceException {
        if (providers == null) {
            return;
        }
        DataSourceReader tmpReader;
        for (String provider : providers) {
            try {
                tmpReader = (DataSourceReader) Class.forName(provider).newInstance();
                this.dsReaders.put(tmpReader.getType(), tmpReader);
            } catch (Exception e) {
                throw new DataSourceException("Error in loading data source provider: " +
                        e.getMessage(), e);
            }
        }
    }

    public void setConfigDir(String configDir) {
        this.dataSourcesPath = configDir;
    }

    /**
     * Initializes the system data sources, i.e. /repository/conf/datasources/*-datasources.xml.
     *
     * @throws DataSourceException
     */
    public void initSystemDataSources() throws DataSourceException {
        if(initialized) {
            return;
        }
        try {
            if(dataSourcesPath == null) {
                dataSourcesPath = DataSourceUtils.getDataSourceConfigPath().toString();
            }
            Path dSPath = Paths.get(this.dataSourcesPath);
            Path masterDSPath = dSPath.resolve(DataSourceConstants.MASTER_DS_FILE_NAME);
            File masterDSFile = masterDSPath.toFile();

			/* initialize the master data sources first */
            if (masterDSFile.exists()) {
                this.initSystemDataSource(masterDSFile);
            }
            /* then rest of the system data sources */
            File dataSourcesFolder = dSPath.toFile();
            for (File sysDSFile : dataSourcesFolder.listFiles()) {
                if (sysDSFile.getName().endsWith(DataSourceConstants.SYS_DS_FILE_NAME_SUFFIX)
                        && !sysDSFile.getName().equals(DataSourceConstants.MASTER_DS_FILE_NAME)) {
                    this.initSystemDataSource(sysDSFile);
                }
            }
        } catch (Exception e) {
            throw new DataSourceException("Error in initializing system data sources: " +
                    e.getMessage(), e);
        }
        initialized = true;
    }

    private void initSystemDataSource(File sysDSFile) throws DataSourceException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(SystemDataSourcesConfiguration.class);
            Document doc = DataSourceUtils.convertToDocument(sysDSFile);
            SystemDataSourcesConfiguration sysDS = (SystemDataSourcesConfiguration) ctx.createUnmarshaller().
                    unmarshal(doc);
            DataSourceRepository dsRepo = this.getDataSourceRepository();
            addDataSourceProviders(sysDS.getProviders());
            for (DataSourceMetaInfo dsmInfo : sysDS.getDataSources()) {
                dsmInfo.setSystem(true);
                dsRepo.addDataSource(dsmInfo);
            }
        } catch (Exception e) {
            throw new DataSourceException("Error in initializing system data sources at '" +
                    sysDSFile.getAbsolutePath() + "' - " + e.getMessage(), e);
        }
    }

}
