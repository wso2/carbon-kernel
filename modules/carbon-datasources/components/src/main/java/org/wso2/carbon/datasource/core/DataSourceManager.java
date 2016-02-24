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
import org.wso2.carbon.datasource.core.beans.DataSourceMetaInfo;
import org.wso2.carbon.datasource.core.beans.DataSourcesConfiguration;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This class contains the functionality in managing the data sources.
 */
public class DataSourceManager {

    private static Log log = LogFactory.getLog(DataSourceManager.class);
    private static DataSourceManager instance;
    private Map<String, DataSourceReader> dsReaders;
    private static DataSourceRepository dsRepo;
    private String dataSourcesPath = null;
    private boolean initialized = false;
    private static final String SYS_DS_FILE_NAME_SUFFIX = "-datasources.xml";


    /**
     * Private constructor for DataSourceManager class. This is a singleton class, thus the constructor is private.
     */
    private DataSourceManager() {
        this.dsReaders = new HashMap<>();
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
        if (dsRepo == null) {
            dsRepo = new DataSourceRepository();
        }
        return dsRepo;
    }

    /**
     * Returns the types of data source readers specified in the system.
     *
     * @return {@code List<String>}
     * @throws DataSourceException if no data source readers are defined.
     */
    public List<String> getDataSourceTypes() throws DataSourceException {
        return new ArrayList<>(dsReaders.keySet());
    }

    /**
     * Returns a DataSourceReader for the given DataSourceReader type.
     *
     * @param dsType String
     * @return {@code DataSourceReader}
     * @throws DataSourceException
     */
    public DataSourceReader getDataSourceReader(String dsType) throws DataSourceException {
        DataSourceReader reader = dsReaders.get(dsType);
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
    public void initSystemDataSources() throws DataSourceException {
        log.debug("Initializing the data sources.");
        if (initialized) {
            log.debug("Data sources are already initialized.");
            return;
        }
        try {
            if (dataSourcesPath == null) {
                dataSourcesPath = DataSourceUtils.getDataSourceConfigPath().toString();
            }
            log.debug("Data sources configuration path: " + dataSourcesPath);
            Path dSPath = Paths.get(dataSourcesPath);

            File dataSourcesFolder = dSPath.toFile();
            File[] files = dataSourcesFolder.listFiles();
            if (files != null) {
                for (File sysDSFile : files) {
                    if (sysDSFile.getName().endsWith(SYS_DS_FILE_NAME_SUFFIX)) {
                        log.debug("Initializing data source: " + sysDSFile.getName());
                        initSystemDataSource(sysDSFile);
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
     * @param sysDSFile {@link File}
     * @throws DataSourceException
     */
    private void initSystemDataSource(File sysDSFile) throws DataSourceException {
        try {
            DataSourcesConfiguration sysDS = getSystemDataSourcesFromConfigFile(sysDSFile);
            DataSourceRepository dsRepo = getDataSourceRepository();
            if(this.dsReaders.size() == 0) {
                findDataSourceProviders();
            }
            for (DataSourceMetaInfo dsmInfo : sysDS.getDataSources()) {
                dsRepo.addDataSource(dsmInfo);
            }
        } catch (DataSourceException e) {
            throw new DataSourceException("Error in initializing system data sources at '" +
                    sysDSFile.getAbsolutePath() + " - " + e.getMessage(), e);
        }
    }

    /**
     * Generate {@code SystemDataSourceConfiguration} jaxb bean from the given data source configuration file.
     *
     * @param sysDSFile {@link File}
     * @return {@code SystemDataSourcesConfiguration}
     * @throws DataSourceException
     */
    private DataSourcesConfiguration getSystemDataSourcesFromConfigFile(File sysDSFile)
            throws DataSourceException {
        try {
            log.debug("Parsing configuration file: " + sysDSFile.getName());
            JAXBContext ctx = JAXBContext.newInstance(DataSourcesConfiguration.class);
            Document doc = DataSourceUtils.convertToDocument(sysDSFile);
            return (DataSourcesConfiguration) ctx.createUnmarshaller().unmarshal(doc);
        } catch (JAXBException e) {
            throw new DataSourceException("Error occurred while converting configuration into jaxb beans", e);
        }
    }

    public void addDataSourceProviders(Map<String, DataSourceReader> readers) {
        if(readers != null && readers.size() > 0) {
            this.dsReaders = readers;
        }
    }

    public void findDataSourceProviders() {
        if(dsReaders.size() == 0) {
            ServiceLoader<DataSourceReader> dsReaderLoader = ServiceLoader.load(DataSourceReader.class);
            Iterator<DataSourceReader> iterator = dsReaderLoader.iterator();
            while (iterator.hasNext()) {
                DataSourceReader reader = iterator.next();
                if(dsReaders.containsKey(reader.getType())) {
                    log.warn("A reader with the type " + reader.getType() + "already exists. "
                            + reader.getClass().toString() + " will be ignored.");
                    continue;
                }
                dsReaders.put(reader.getType(), reader);
            }
        }
    }

}
