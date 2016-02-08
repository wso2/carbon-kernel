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
import org.wso2.carbon.datasource.core.beans.SystemDataSourcesConfiguration;
import org.wso2.carbon.datasource.core.common.DataSourceConstants;
import org.wso2.carbon.datasource.core.common.DataSourceException;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Private constructor for DataSourceManager class. This is a singleton class, thus the constructor is private.
     */
    private DataSourceManager() {
        this.dsReaders = new HashMap();
    }

    /**
     * Returns the singleton instance of DataSourceManager.
     *
     * @return DataSourceManager
     */
    public static DataSourceManager getInstance() {
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
    public DataSourceRepository getDataSourceRepository() {
        if (dsRepo == null) {
            dsRepo = new DataSourceRepository();
        }
        return dsRepo;
    }

    /**
     * Returns the types of data source readers specified in the system.
     *
     * @return {@code List<String>}
     * @throws DataSourceException if no datasource readers are defined.
     */
    public List<String> getDataSourceTypes() throws DataSourceException {
        if (dsReaders == null) {
            throw new DataSourceException("The data source readers are not initialized yet.");
        }
        return new ArrayList(dsReaders.keySet());
    }

    public DataSourceReader getDataSourceReader(String dsType) throws DataSourceException {
        if (dsReaders == null) {
            throw new DataSourceException("The data source readers are not initialized yet");
        }
        return dsReaders.get(dsType);
    }

    /**
     * Sets the configuration directory.
     *
     * @param configDir String
     */
    public void setConfigDir(String configDir) {
        this.dataSourcesPath = configDir;
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
            Path masterDSPath = dSPath.resolve(DataSourceConstants.MASTER_DS_FILE_NAME);
            File masterDSFile = masterDSPath.toFile();
            log.debug("Master data source path: " + masterDSPath.toString());

			/* initialize the master data sources first */
            if (masterDSFile.exists()) {
                log.debug("Initializing master data source.");
                initSystemDataSource(masterDSFile);
            }
            /* then rest of the system data sources */
            File dataSourcesFolder = dSPath.toFile();
            for (File sysDSFile : dataSourcesFolder.listFiles()) {
                if (sysDSFile.getName().endsWith(DataSourceConstants.SYS_DS_FILE_NAME_SUFFIX)
                        && !sysDSFile.getName().equals(DataSourceConstants.MASTER_DS_FILE_NAME)) {
                    log.debug("Initializing data source: " + sysDSFile.getName());
                    initSystemDataSource(sysDSFile);
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
            SystemDataSourcesConfiguration sysDS = getSystemDataSourcesFromConfigFile(sysDSFile);
            DataSourceRepository dsRepo = getDataSourceRepository();
            addDataSourceProviders(sysDS.getProviders());
            for (DataSourceMetaInfo dsmInfo : sysDS.getDataSources()) {
                dsRepo.addDataSource(dsmInfo);
            }
        } catch (DataSourceException e) {
            throw new DataSourceException("Error in initializing system data sources at '" +
                    sysDSFile.getAbsolutePath() + " - " + e.getMessage(), e);
        }
    }

    private SystemDataSourcesConfiguration getSystemDataSourcesFromConfigFile(File sysDSFile)
            throws DataSourceException {
        try {
            log.debug("Parsing configuration file: " + sysDSFile.getName());
            JAXBContext ctx = JAXBContext.newInstance(SystemDataSourcesConfiguration.class);
            Document doc = DataSourceUtils.convertToDocument(sysDSFile);
            return (SystemDataSourcesConfiguration) ctx.createUnmarshaller().
                    unmarshal(doc);
        } catch (JAXBException e) {
            throw new DataSourceException("Error occurred while converting configuration into jaxb beans", e);
        }
    }

    private void addDataSourceProviders(List<String> providers) throws DataSourceException {
        if (providers == null) {
            log.debug("No data source providers found!!!");
            return;
        }
        DataSourceReader tmpReader;
        for (String provider : providers) {
            try {
                log.debug("Loading data source provider: " + provider);
                tmpReader = (DataSourceReader) Class.forName(provider).newInstance();
                dsReaders.put(tmpReader.getType(), tmpReader);
            } catch (Exception e) {
                throw new DataSourceException("Error in loading data source provider: " +
                        e.getMessage(), e);
            }
        }
    }

}
