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
import javax.naming.NamingException;

/**
 * This class contains the functionality in managing the data sources.
 */
public class DataSourceManager {

    private static Log log = LogFactory.getLog(DataSourceManager.class);
    private static DataSourceManager instance = new DataSourceManager();;
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
    public static DataSourceManager getInstance() {
        return instance;
    }

    /**
     * Returns the DataSourceRepository object, which encapsulates all the defined data sources.
     *
     * @return DataSourceRepository
     */
    public DataSourceRepository getDataSourceRepository() {
        return dataSourceRepository;
    }

    /**
     * Returns the types of data source readers registered in the system.
     *
     * @return {@code List<String>}
     */
    public List<String> getDataSourceTypes() {
        return new ArrayList<>(dataSourceReaders.keySet());
    }

    /**
     * Returns a DataSourceReader for the given DataSourceReader type.
     *
     * @param dataSourceType String
     * @return {@code DataSourceReader}
     * @throws DataSourceException
     */
    public DataSourceReader getDataSourceReader(String dataSourceType) throws DataSourceException {
        DataSourceReader reader = dataSourceReaders.get(dataSourceType);
        if (reader == null) {
            throw new DataSourceException("No reader found for type: " + dataSourceType);
        }
        return reader;
    }

    /**
     * Initializes the data sources.
     *
     * @param configurationDirectory String
     * @throws DataSourceException
     */
    public void initDataSources(String configurationDirectory)
            throws DataSourceException {
        this.dataSourcesPath = configurationDirectory;
        loadDataSourceProviders();
        initDataSources(dataSourcesPath, dataSourceReaders);
    }

    /**
     * @param configurationDir  String location of the configuration directory
     * @param dataSourceReaders {@code Map<String, DataSourceReader>}
     * @throws DataSourceException
     */
    public void initDataSources(String configurationDir, Map<String, DataSourceReader> dataSourceReaders)
            throws DataSourceException {
        this.dataSourceReaders = dataSourceReaders;
        if (initialized) {
            log.debug("Data sources are already initialized.");
            return;
        }
        log.debug("Initializing the data sources.");

        if (dataSourceReaders.isEmpty()) {
            throw new RuntimeException("No data source readers found. Data sources will not be initialized!");
        }
        try {
            Path dataSourcesPath = Paths.get(configurationDir);
            File dataSourcesFolder = dataSourcesPath.toFile();
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
        } catch (DataSourceException | NamingException e) {
            throw new DataSourceException("Error in initializing system data sources at '" +
                    dataSourceFile.getAbsolutePath() + " - " + e.getMessage(), e);
        }
    }

    /**
     * Load {@code DataSourceReader} implementations from the class path. This make use of {@link ServiceLoader}.
     */
    private void loadDataSourceProviders() {
        if (dataSourceReaders.size() == 0) {
            ServiceLoader<DataSourceReader> dsReaderLoader = ServiceLoader.load(DataSourceReader.class);
            dsReaderLoader.forEach(reader -> dataSourceReaders.put(reader.getType(), reader));
        }
    }
}
