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
import org.wso2.carbon.datasource.common.DataSourceConstants.DataSourceStatusModes;
import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.datasource.common.spi.DataSourceReader;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * This class represents the repository which is used to hold the data sources.
 */
public class DataSourceRepository {

    private static Log log = LogFactory.getLog(DataSourceRepository.class);

    private Map<String, CarbonDataSource> dataSources;

    private Map<String, DataSourceMetaInfo> dsMetaInfoMap;

    public DataSourceRepository() throws DataSourceException {
        this.dataSources = new HashMap<>();
        this.dsMetaInfoMap = new HashMap<>();
    }

    private Object createDataSourceObject(DataSourceMetaInfo dsmInfo, boolean isUseDataSourceFactory)
            throws DataSourceException {
        DataSourceReader dsReader = DataSourceManager.getInstance()
                .getDataSourceReader(dsmInfo.getDefinition().getType());

        if (dsReader == null) {
            throw new DataSourceException("A data source reader cannot be found for the type '" +
                    dsmInfo.getDefinition().getType() + "'");
        }
//		/* sets the current data source's (name) as a thread local value
//		 * so it can be read by data source readers */
//		DataSourceUtils.setCurrentDataSourceId(dsmInfo.getName());
        return dsReader.createDataSource(DataSourceUtils.elementToString(
                (Element) dsmInfo.getDefinition().getDsXMLConfiguration()), isUseDataSourceFactory);
    }

    private Context lookupJNDISubContext(Context context, String jndiName)
            throws DataSourceException {
        try {
            Object obj = context.lookup(jndiName);
            if (!(obj instanceof Context)) {
                throw new DataSourceException("Non JNDI context already exists at '" +
                        context + "/" + jndiName);
            }
            return (Context) obj;
        } catch (NamingException e) {
            return null;
        }
    }

    private void checkAndCreateJNDISubContexts(Context context, String jndiName)
            throws DataSourceException {
        String[] tokens = jndiName.split("/");
        Context tmpCtx;
        String token;
        for (int i = 0; i < tokens.length - 1; i++) {
            token = tokens[i];
            tmpCtx = this.lookupJNDISubContext(context, token);
            if (tmpCtx == null) {
                try {
                    tmpCtx = context.createSubcontext(token);
                } catch (NamingException e) {
                    throw new DataSourceException(
                            "Error in creating JNDI subcontext '" + context +
                                    "/" + token + ": " + e.getMessage(), e);
                }
            }
            context = tmpCtx;
        }
    }

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
            throw new DataSourceException("Error creating JNDI initial context: " +
                    e.getMessage(), e);
        }
        this.checkAndCreateJNDISubContexts(context, jndiConfig.getName());

        try {
            context.bind(jndiConfig.getName(), dsObject);
//            context.bind(jndiConfig.getName(), new Reference("org.apache.tomcat.jdbc.pool.DataSource",
//                    "org.apache.tomcat.jdbc.pool.DataSourceFactory", null));
        } catch (NamingException e) {
            throw new DataSourceException("Error in binding to JNDI with name '" +
                    jndiConfig.getName() + "' - " + e.getMessage(), e);
        }
    }

    private void unregisterJNDI(DataSourceMetaInfo dsmInfo) {
        JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
        if (jndiConfig == null) {
            return;
        }
        try {
            InitialContext context = new InitialContext(jndiConfig.extractHashtableEnv());
            context.unbind(jndiConfig.getName());
        } catch (NamingException e) {
            log.error("Error in unregistering JNDI name: " +
                    jndiConfig.getName() + " - " + e.getMessage(), e);
        }
    }

    private void unregisterDataSource(String dsName) {
        CarbonDataSource cds = this.getDataSource(dsName);
        if (cds == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Unregistering data source: " + dsName);
        }
        this.unregisterJNDI(cds.getDSMInfo());
        this.dataSources.remove(dsName);
    }

    private synchronized void registerDataSource(DataSourceMetaInfo dsmInfo) throws DataSourceException {
//        this.unregisterDataSource(dsmInfo.getJndiConfig().getName());
        this.unregisterJNDI(dsmInfo);
        Object dsObject = null;
        boolean isDataSourceFactoryReference = false;
        DataSourceStatus dsStatus;
        try {
            JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
            if (jndiConfig != null) {
                isDataSourceFactoryReference = jndiConfig.isUseDataSourceFactory();
            }
            dsObject = this.createDataSourceObject(dsmInfo, isDataSourceFactoryReference);
            this.registerJNDI(dsmInfo, dsObject);
            dsStatus = new DataSourceStatus(DataSourceStatusModes.ACTIVE, null);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            dsStatus = new DataSourceStatus(DataSourceStatusModes.ERROR, msg);
        }
        /* Creating DataSource object , if dsObject is a Reference */
        if (isDataSourceFactoryReference) {
            dsObject = this.createDataSourceObject(dsmInfo, false);
        }
        CarbonDataSource cds = new CarbonDataSource(dsmInfo, dsStatus, dsObject);
        this.dataSources.put(cds.getDSMInfo().getName(), cds);
    }

    /**
     * Gets information about all the data sources.
     *
     * @return A list of all data sources
     */
    public java.util.Collection<CarbonDataSource> getAllDataSources() {
        return dataSources.values();
    }

    /**
     * Gets information about a specific given data source.
     *
     * @param dsName The name of the data source.
     * @return The data source information
     */
    public CarbonDataSource getDataSource(String dsName) {
        return this.dataSources.get(dsName);
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
        this.registerDataSource(dsmInfo);
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
        CarbonDataSource cds = this.getDataSource(dsName);
        if (cds == null) {
            throw new DataSourceException("Data source does not exist: " + dsName);
        }
        if (cds.getDSMInfo().isSystem()) {
            throw new DataSourceException("System data sources cannot be deleted: " + dsName);
        }
        this.unregisterDataSource(dsName);
        if (cds.getDSMInfo().isPersistable()) {
            this.dsMetaInfoMap.remove(dsName);
        }
    }

    /**
     * Tests Connection of the data source
     *
     * @param dsmInfo The meta information of the data source to be tested.
     */
    public boolean testDataSourceConnection(DataSourceMetaInfo dsmInfo) throws DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Testing connection of data source: " + dsmInfo.getName());
        }
        DataSourceReader dsReader = DataSourceManager.getInstance().getDataSourceReader(
                dsmInfo.getDefinition().getType());
        try {
            return dsReader.testDataSourceConnection(DataSourceUtils.elementToString((Element) dsmInfo.getDefinition().getDsXMLConfiguration()));
        } catch (DataSourceException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }

}
