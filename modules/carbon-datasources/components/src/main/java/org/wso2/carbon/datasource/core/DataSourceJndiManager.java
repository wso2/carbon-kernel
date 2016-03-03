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
import org.wso2.carbon.datasource.core.beans.JNDIConfig;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * This class is responsible to handle all the JNDI related operations with respect to data sources.
 */
public class DataSourceJndiManager {

    private static Log log = LogFactory.getLog(DataSourceJndiManager.class);

    private static final String JAVA_COMP_CONTEXT_STRING = "java:comp";
    private static final String ENV_CONTEXT_STRING = "env";

    /**
     * Register the data source in the JNDI context.
     *
     * @param carbonDataSource {@code CarbonDataSource}
     * @param dataSourceReader {@code DataSourceReader}
     * @throws DataSourceException
     */
    public static void register(CarbonDataSource carbonDataSource, DataSourceReader dataSourceReader)
            throws DataSourceException, NamingException {
        register(carbonDataSource.getMetadata(), carbonDataSource.getDataSourceObject(), dataSourceReader);
    }

    /**
     * Register the data source in the JNDI context.
     *
     * @param dataSourceMetadata {@code DataSourceMetaInfo}
     * @param dataSourceObject   {@code Object}
     * @param dataSourceReader   {@code DataSourceReader}
     * @throws DataSourceException
     */
    public static void register(DataSourceMetadata dataSourceMetadata, Object dataSourceObject,
                                DataSourceReader dataSourceReader)
            throws DataSourceException, NamingException {
        JNDIConfig jndiConfig = dataSourceMetadata.getJndiConfig();
        //If JNDI configuration is not present, the data source will not be bound to a JNDI context.
        if (jndiConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug("JNDI info not found for " + dataSourceMetadata.getName());
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Registering " + dataSourceMetadata.getName() + " into JNDI context");
        }
        Context subContext = getBindingContext(jndiConfig);

        //If jndi configuration specify to use data source factory, then create a java.naming.Reference object
        //and pass to JNDI context.
        if (jndiConfig.isUseJndiReference()) {
            dataSourceObject = DataSourceBuilder.buildDataSourceObject(dataSourceMetadata, true, dataSourceReader);
        }
        subContext.rebind(jndiConfig.getName(), dataSourceObject);
    }

    /**
     * Returns the binding context.
     *
     * @param jndiConfig {@code JNDIConfig}
     * @return {@code Context}
     * @throws DataSourceException
     */
    private static Context getBindingContext(JNDIConfig jndiConfig) throws DataSourceException, NamingException {
        InitialContext context;
        context = new InitialContext(jndiConfig.extractHashtableEnv());
        return checkAndCreateJNDISubContexts(context, jndiConfig);
    }

    /**
     * Check for existence of JNDI sub contexts and create if not found.
     *
     * @param context    {@link Context}
     * @param jndiConfig {@code JNDIConfig}
     * @throws DataSourceException
     */
    private static Context checkAndCreateJNDISubContexts(Context context, JNDIConfig jndiConfig)
            throws DataSourceException, NamingException {
        String jndiName = JAVA_COMP_CONTEXT_STRING + "/" + ENV_CONTEXT_STRING + "/" + jndiConfig.getName();
        String[] tokens = jndiName.split("/");
        jndiConfig.setName(tokens[tokens.length - 1]);
        Context tmpCtx = context;
        Context subContext = null;
        String token;
        for (int i = 0; i < tokens.length - 1; i++) {
            token = tokens[i];
            subContext = lookupJNDISubContext(tmpCtx, token);
            if (subContext == null) {
                subContext = tmpCtx.createSubcontext(token);
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
    private static Context lookupJNDISubContext(Context context, String jndiName) throws DataSourceException
            , NamingException {
        try {
            Object obj = context.lookup(jndiName);
            if (!(obj instanceof Context)) {
                throw new DataSourceException("Non JNDI context exists at '" + context + "/" + jndiName);
            }
            return (Context) obj;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Unregister a given data source from the repository.
     *
     * @param cds {@code CarbonDataSource}
     */
    public static void unregister(CarbonDataSource cds) throws NamingException {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering data source: " + cds.getMetadata().getName());
        }
        JNDIConfig jndiConfig = cds.getMetadata().getJndiConfig();
        if (jndiConfig == null) {
            return;
        }
        InitialContext context = new InitialContext(jndiConfig.extractHashtableEnv());
        context.unbind(jndiConfig.getName());
    }
}
