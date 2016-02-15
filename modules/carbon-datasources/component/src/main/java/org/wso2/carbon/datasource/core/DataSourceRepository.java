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
import org.wso2.carbon.datasource.core.beans.DataSourceMetaInfo;
import org.wso2.carbon.datasource.core.beans.DataSourceStatus;
import org.wso2.carbon.datasource.core.beans.JNDIConfig;
import org.wso2.carbon.datasource.core.common.DataSourceConstants.DataSourceStatusModes;
import org.wso2.carbon.datasource.core.common.DataSourceException;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This class represents the repository which is used to hold the data sources.
 */
public class DataSourceRepository {

    private static Log log = LogFactory.getLog(DataSourceRepository.class);
    private Map<String, CarbonDataSource> dataSources;

    /**
     * Default constructor for DataSourceRepository.
     */
    public DataSourceRepository() {
        this.dataSources = new HashMap<>();
    }

    /**
     * Adds a new data source to the repository.
     *
     * @param dsmInfo The meta information of the data source to be added.
     */
    public void addDataSource(DataSourceMetaInfo dsmInfo) throws DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Adding data source: " + dsmInfo.getName());
        }
        registerDataSource(dsmInfo);
    }

    /**
     * Register a given data source object in JNDI context and in the repository.
     *
     * @param dsmInfo {@code DataSourceMetaInfo}
     * @throws DataSourceException
     */
    private void registerDataSource(DataSourceMetaInfo dsmInfo) throws DataSourceException {
        Object dsObject = null;
        boolean isDataSourceFactoryReference = false;
        DataSourceStatus dsStatus;
        try {
            JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
            if (jndiConfig != null) {
                isDataSourceFactoryReference = jndiConfig.isUseDataSourceFactory();
            }
            dsObject = createDataSourceObject(dsmInfo, isDataSourceFactoryReference);
            registerJNDI(dsmInfo, dsObject);
            dsStatus = new DataSourceStatus(DataSourceStatusModes.ACTIVE, null);
        } catch (DataSourceException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            dsStatus = new DataSourceStatus(DataSourceStatusModes.ERROR, msg);
        }

        //Creates a data source object in any error occurred while registering through JNDI.
        if (dsObject == null) {
            dsObject = createDataSourceObject(dsmInfo, isDataSourceFactoryReference);
        }
        CarbonDataSource cds = new CarbonDataSource(dsmInfo, dsStatus, dsObject);
        dataSources.put(cds.getDSMInfo().getName(), cds);
    }

    /**
     * Creates the data source object by getting the appropriate DataSourceReader.
     *
     * @param dsmInfo                {@code DataSourceMetaInfo}
     * @param isUseDataSourceFactory {@code boolean}
     * @return {@code Object}
     */
    private Object createDataSourceObject(DataSourceMetaInfo dsmInfo, boolean isUseDataSourceFactory)
            throws DataSourceException {

        DataSourceReader dsReader = DataSourceManager.getInstance()
                .getDataSourceReader(dsmInfo.getDefinition().getType());

        log.debug("Generating the DataSource object from \"" + dsReader.getType() + "\" type reader.");

//		/* sets the current data source's (name) as a thread local value
//		 * so it can be read by data source readers */
//		DataSourceUtils.setCurrentDataSourceId(dsmInfo.getName());

        Element configurationXmlDefinition = (Element) dsmInfo.getDefinition().getDsXMLConfiguration();
        return dsReader.createDataSource(DataSourceUtils.elementToString(configurationXmlDefinition),
                isUseDataSourceFactory);
    }

    /**
     * Register the data source in the JNDI context.
     *
     * @param dsmInfo  {@code DataSourceMetaInfo}
     * @param dsObject {@code Object}
     * @throws DataSourceException
     */
    private void registerJNDI(DataSourceMetaInfo dsmInfo, Object dsObject)
            throws DataSourceException {
        JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
        if (jndiConfig == null) {
            return;
        }
        InitialContext context;
        try {
            context = new InitialContext(jndiConfig.extractHashtableEnv());
        } catch (NamingException e) {
            throw new DataSourceException("Error creating JNDI initial context: " + e.getMessage(), e);
        }
        Context subContext = checkAndCreateJNDISubContexts(context, jndiConfig);
        if(subContext == null) {
            return;
        }
        try {
            subContext.rebind(jndiConfig.getName(), dsObject);
        } catch (NamingException e) {
            throw new DataSourceException("Error in binding to JNDI with name '" +
                    jndiConfig.getName() + "' - " + e.getMessage(), e);
        }
    }

    /**
     * Check for existence of JNDI sub contexts and create if not found.
     *
     * @param context  {@link Context}
     * @param jndiConfig {@code JNDIConfig}
     * @throws DataSourceException
     */
    private Context checkAndCreateJNDISubContexts(Context context, JNDIConfig jndiConfig)
            throws DataSourceException {
        Context compEnvContext;
        try {
            Context compContext = context.createSubcontext("java:comp");
            compEnvContext  = compContext.createSubcontext("env");
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        String jndiName = jndiConfig.getName();
        String[] tokens = jndiName.split("/");
        jndiConfig.setName(tokens[tokens.length - 1]);
        Context tmpCtx = compEnvContext;
        Context subContext = null;
        String token;
        for (int i = 0; i < tokens.length - 1; i++) {
            token = tokens[i];
            subContext = lookupJNDISubContext(tmpCtx, token);
            if (subContext == null) {
                try {
                    subContext = tmpCtx.createSubcontext(token);
                } catch (NamingException e) {
                    throw new DataSourceException("Error in creating JNDI subcontext '" + compEnvContext + "/"
                            + token + ": " + e.getMessage(), e);
                }
            }
            tmpCtx = subContext;
        }
        return subContext;
    }

    /**
     * Look up a jndi sub context. This method returns null if a NamingException occurred.
     *
     * @param context  {@link Context}
     * @param jndiName String
     * @return {@link Context}
     * @throws DataSourceException
     */
    private Context lookupJNDISubContext(Context context, String jndiName)
            throws DataSourceException {
        try {
            Object obj = context.lookup(jndiName);
            if (!(obj instanceof Context)) {
                throw new DataSourceException("Non JNDI context already exists at '" + context + "/" + jndiName);
            }
            return (Context) obj;
        } catch (NamingException e) {
            return null;
        }
    }

    /**
     * Gets information about a specific given data source.
     *
     * @param dsName The name of the data source.
     * @return The data source information
     */
    public CarbonDataSource getDataSource(String dsName) {
        return dataSources.get(dsName);
    }

    /**
     * Gets information about all the data sources.
     *
     * @return A list of all data sources
     */
    public Collection<CarbonDataSource> getAllDataSources() {
        return dataSources.values();
    }

    /**
     * Unregisters and deletes the data source from the repository.
     *
     * @param dsName The data source name
     */
    public void deleteDataSource(String dsName) throws DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting data source: " + dsName);
        }
        CarbonDataSource cds = getDataSource(dsName);
        if (cds == null) {
            throw new DataSourceException("Data source does not exist: " + dsName);
        }
        unregisterDataSource(cds, dsName);
    }

    /**
     * Unregister a given data source from the repository.
     *
     * @param cds    {@code CarbonDataSource}
     * @param dsName String
     */
    private void unregisterDataSource(CarbonDataSource cds, String dsName) {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering data source: " + dsName);
        }
        unregisterJNDI(cds.getDSMInfo());
        dataSources.remove(dsName);
    }

    /**
     * Unregister a given JNDI binding.
     *
     * @param dsmInfo {@code DataSourceMetaInfo}
     */
    private void unregisterJNDI(DataSourceMetaInfo dsmInfo) {
        JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
        if (jndiConfig == null) {
            return;
        }
        try {
            InitialContext context = new InitialContext(jndiConfig.extractHashtableEnv());
            context.unbind(jndiConfig.getName());
        } catch (NamingException e) {
            log.error("Error in unregistering JNDI name: " + jndiConfig.getName() + " - " + e.getMessage(), e);
        }
    }

}
