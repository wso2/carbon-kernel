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
import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.beans.DataSourcesConfiguration;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * This class contains the functionality in managing the data sources.
 */
public class DataSourceManager {

    private static Log log = LogFactory.getLog(DataSourceManager.class);
    private static DataSourceManager instance;
    private DataSourceRepository dataSourceRepository;
    private Map<String, DataSourceReader> dataSourceReaders;

    private String dataSourcesPath = null;

    private static final String FILE_NAME_SUFFIX = "-datasources.xml";
    private boolean initialized = false;

    /**
     * Private constructor for DataSourceManager class. This is a singleton class, thus the constructor is private.
     */
    private DataSourceManager() {
        this.dataSourceReaders = new HashMap<>();
        this.dataSourceRepository = new DataSourceRepository();
    }

    /**
     * Returns the singleton instance of DataSourceManager.
     *
     * @return DataSourceManager
     */
    public synchronized static DataSourceManager getInstance() {
        if (instance == null) {
            instance = new DataSourceManager();
        }
        return instance;
    }

    /**
     * Returns the DataSourceRepository object, which encapsulates all the defined data sources.
     *
     * @return DataSourceRepository
     */
    public synchronized DataSourceRepository getDataSourceRepository() {
        return dataSourceRepository;
    }

    /**
     * Returns the types of data source readers registered in the system.
     *
     * @return {@code List<String>}
     * @throws DataSourceException if no data source readers are defined.
     */
    public List<String> getDataSourceTypes() throws DataSourceException {
        return new ArrayList<>(dataSourceReaders.keySet());
    }

    /**
     * Returns a DataSourceReader for the given DataSourceReader type.
     *
     * @param dsType String
     * @return {@code DataSourceReader}
     * @throws DataSourceException
     */
    public DataSourceReader getDataSourceReader(String dsType) throws DataSourceException {
        DataSourceReader reader = dataSourceReaders.get(dsType);
        if (reader == null) {
            throw new DataSourceException("No reader found for type: " + dsType);
        }
        return reader;
    }

    public void setConfigurationDirectory(String path) {
        this.dataSourcesPath = path;
    }

    /**
     * Initializes the system data sources, i.e. /repository/conf/datasources/*-datasources.xml.
     *
     * @throws DataSourceException
     */
    public void initDataSources() throws DataSourceException {
        log.debug("Initializing the data sources.");
        if (initialized) {
            log.debug("Data sources are already initialized.");
            return;
        }
        if (dataSourceReaders.isEmpty()) {
            loadDataSourceProviders();
        }
        if (dataSourceReaders.isEmpty()) {
            //Should throw an RuntimeException??
            throw new DataSourceException("No data source readers found. Data sources will not be initialized!");
        }
        try {
            if (dataSourcesPath == null) {
                dataSourcesPath = DataSourceUtils.getDataSourceConfigPath().toString();
            }
            if (log.isDebugEnabled()) {
                log.debug("Data sources configuration path: " + dataSourcesPath);
            }
            Path dSPath = Paths.get(dataSourcesPath);
            File dataSourcesFolder = dSPath.toFile();
            File[] dataSourceConfigFiles = dataSourcesFolder.listFiles();

            if (dataSourceConfigFiles != null) {
                for (File dataSourceConfigFile : dataSourceConfigFiles) {
                    if (dataSourceConfigFile.getName().endsWith(FILE_NAME_SUFFIX)) {
                        initDataSource(dataSourceConfigFile);
                    }
                }
            }
        } catch (DataSourceException e) {
            throw new DataSourceException("Error in initializing system data sources: " + e.getMessage(), e);
        }
        initialized = true;
    }

    /**
     * Initialize the data sources given in data source config files.
     *
     * @param dataSourceFile {@link File}
     * @throws DataSourceException
     */
    private void initDataSource(File dataSourceFile) throws DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing data source: " + dataSourceFile.getName());
        }
        try {
            DataSourcesConfiguration dataSourceConfiguration = DataSourceUtils
                    .loadJAXBConfiguration(dataSourceFile, DataSourcesConfiguration.class);

            for (DataSourceMetadata dsmInfo : dataSourceConfiguration.getDataSources()) {
                DataSourceReader dataSourceReader = getDataSourceReader(dsmInfo.getDefinition().getType());
                CarbonDataSource carbonDataSource = DataSourceBuilder.buildCarbonDataSource(dsmInfo, dataSourceReader);
                dataSourceRepository.addDataSource(carbonDataSource);
                DataSourceJndiManager.register(carbonDataSource, dataSourceReader);
            }
        } catch (DataSourceException e) {
            throw new DataSourceException("Error in initializing system data sources at '" +
                    dataSourceFile.getAbsolutePath() + " - " + e.getMessage(), e);
        }
    }

    /**
     * Allows the API consumers to provide a map containing a set of {@code DataSourceReader} objects.
     *
     * @param readers {@code Map<String, DataSourceReader>}
     */
    public void addDataSourceProviders(Map<String, DataSourceReader> readers) {
        if (readers != null && readers.size() > 0) {
            this.dataSourceReaders.putAll(readers);
        }
    }

    /**
     * If {@code List<DataSourceReader>} is not set from {@code addDataSourceProviders} method,
     * {@code loadDataSourceProviders} is called internally and load data source readers using
     * {@link java.util.ServiceLoader}.
     */
    private void loadDataSourceProviders() {
        if (dataSourceReaders.size() == 0) {
            ServiceLoader<DataSourceReader> dsReaderLoader = ServiceLoader.load(DataSourceReader.class);
            dsReaderLoader.forEach(reader -> dataSourceReaders.put(reader.getType(), reader));
        }
    }
}
