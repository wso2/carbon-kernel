/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ndatasource.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceConstants;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;
import org.wso2.carbon.ndatasource.core.internal.DataSourceServiceComponent;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains the functionality in managing the data sources.
 */
public class DataSourceManager {

	private static Log log = LogFactory.getLog(DataSourceManager.class);
	
	private static DataSourceManager instance = new DataSourceManager();
	
	private Map<Integer, DataSourceRepository> dsRepoMap;
	
	private Map<String, DataSourceReader> dsReaders;
	
	private Map<Integer, Boolean> userDSRepoInitMap;
	
	public DataSourceManager() {
		this.dsReaders = new HashMap<String, DataSourceReader>();
		this.dsRepoMap = new ConcurrentHashMap<Integer, DataSourceRepository>();
		this.userDSRepoInitMap = new ConcurrentHashMap<Integer, Boolean>();
	}
	
	public static DataSourceManager getInstance() {
		return instance;
	}


    public DataSourceRepository getDataSourceRepository() throws DataSourceException {
		int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
		return this.getDataSourceRepository(tenantId);
	}
	
	private synchronized DataSourceRepository getDataSourceRepository(int tenantId) 
			throws DataSourceException {
		DataSourceRepository dsRepo = this.dsRepoMap.get(tenantId);
		if (dsRepo == null) {
			dsRepo = DataSourceServiceComponent.getNewTenantDataSourceRepository(tenantId);
			this.dsRepoMap.put(tenantId, dsRepo);
		}
		return dsRepo;
	}


    private synchronized void removeDataSourceRepository(int tenantId) {
		this.dsRepoMap.remove(tenantId);
		this.userDSRepoInitMap.remove(tenantId);
		if (log.isDebugEnabled()) {
			log.debug("Data source repository removed for tenant: " + tenantId);
		}
	}
	
	/**
	 * Initializes user data sources of a specific tenant.
	 * @param tenantId The tenant id of the tenant to be initialized
	 * @throws DataSourceException
	 */
	public void initTenant(int tenantId) throws DataSourceException {
		if (!this.isDSRepoUserDSInitialized(tenantId)) {
		    this.getDataSourceRepository(tenantId).initRepository();
		    this.userDSRepoInitMap.put(tenantId, true);
		}
	}
	
	private synchronized boolean isDSRepoUserDSInitialized(int tenantId) {
		Boolean result = this.userDSRepoInitMap.get(tenantId);
		return result != null ? result : false;
	}
	
	/**
	 * Unloads the user data sources from a specific tenant.
	 * @param tenantId The tenant id of the tenant to be unloaded
	 * @throws DataSourceException
	 */
	public void unloadTenant(int tenantId) throws DataSourceException {
		this.getDataSourceRepository(tenantId).unregisterAllUserDataSources();
		this.removeDataSourceRepository(tenantId);
	}
	
	public List<String> getDataSourceTypes() throws DataSourceException {
		if (this.dsReaders == null) {
			throw new DataSourceException("The data source readers are not initialized yet");
		}
		return new ArrayList<String>(this.dsReaders.keySet());
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
	
	/**
	 * Initializes the system data sources, i.e. /repository/conf/datasources/*-datasources.xml.
	 * @throws DataSourceException
	 */
	public void initSystemDataSources() throws DataSourceException {
		try {
			String dataSourcesDir = CarbonUtils.getCarbonConfigDirPath() + File.separator + 
					DataSourceConstants.DATASOURCES_DIRECTORY_NAME;
			File masterDSFile = new File(dataSourcesDir + File.separator + 
					DataSourceConstants.MASTER_DS_FILE_NAME);
			/* initialize the master data sources first */
			if (masterDSFile.exists()) {
			    this.initSystemDataSource(masterDSFile);
			}
			/* then rest of the system data sources */
			File dataSourcesFolder = new File(dataSourcesDir);
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
	}
	
	private void initSystemDataSource(File sysDSFile) throws DataSourceException {
		try {
		    JAXBContext ctx = JAXBContext.newInstance(SystemDataSourcesConfiguration.class);
            Document doc = DataSourceUtils.convertToDocument(sysDSFile);
            DataSourceUtils.secureResolveDocument(doc, true);
		    SystemDataSourcesConfiguration sysDS = (SystemDataSourcesConfiguration) ctx.createUnmarshaller().
		    		unmarshal(doc);
		    this.addDataSourceProviders(sysDS.getProviders());
		    DataSourceRepository dsRepo = this.getDataSourceRepository(
		    		MultitenantConstants.SUPER_TENANT_ID);
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
