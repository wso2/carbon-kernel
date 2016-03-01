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
     * @throws DataSourceException
     */
    public static void register(CarbonDataSource carbonDataSource) throws DataSourceException {
        register(carbonDataSource.getMetadata(), carbonDataSource.getDataSourceObject());
    }

    /**
     * Register the data source in the JNDI context.
     *
     * @param dsmInfo  {@code DataSourceMetaInfo}
     * @param dsObject {@code Object}
     * @throws DataSourceException
     */
    public static void register(DataSourceMetadata dsmInfo, Object dsObject) throws DataSourceException {
        JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
        //If JNDI configuration is not present, the datasource will not be bound to a JNDI context.
        if (jndiConfig == null) {
            if(log.isDebugEnabled()) {
                log.debug("JNDI info not found for " + dsmInfo.getName());
            }
            return;
        }
        if(log.isDebugEnabled()) {
            log.debug("Registering " + dsmInfo.getName() + " into JNDI context");
        }
        Context subContext = getBindingContext(jndiConfig);
        if (subContext == null) {
            return;
        }
        try {
            //If jndi configuration specify to use data source factory, then create a java.naming.Reference object
            //and pass to JNDI context.
            if (jndiConfig.isUseDataSourceFactory()) {
                dsObject = DataSourceBuilder.buildDataSourceObject(dsmInfo, true);
            }
            subContext.rebind(jndiConfig.getName(), dsObject);
        } catch (NamingException e) {
            throw new DataSourceException("Error in binding to JNDI with name '" + jndiConfig.getName()
                    + "' - " + e.getMessage(), e);
        }
    }

    private static Context getBindingContext(JNDIConfig jndiConfig) throws DataSourceException {
        InitialContext context;
        try {
            context = new InitialContext(jndiConfig.extractHashtableEnv());
        } catch (NamingException e) {
            throw new DataSourceException("Error creating JNDI initial context: " + e.getMessage(), e);
        }
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
            throws DataSourceException {
        Context compEnvContext;
        try {
            //Should we reuse the already existing context or destroy the old context and create a new one?
            Context compContext;
            try {
                compContext = (Context) context.lookup(JAVA_COMP_CONTEXT_STRING);
            } catch (NameNotFoundException e) {
                if(log.isDebugEnabled()) {
                    log.debug("Creating the JNDI context: " + JAVA_COMP_CONTEXT_STRING);
                }
                compContext = context.createSubcontext(JAVA_COMP_CONTEXT_STRING);
            }
            try {
                compEnvContext = (Context) compContext.lookup(ENV_CONTEXT_STRING);
            } catch (NameNotFoundException e) {
                if(log.isDebugEnabled()) {
                    log.debug("Creating the JNDI context: " + ENV_CONTEXT_STRING);
                }
                compEnvContext = compContext.createSubcontext(ENV_CONTEXT_STRING);
            }
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
    private static Context lookupJNDISubContext(Context context, String jndiName) throws DataSourceException {
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
     * Unregister a given data source from the repository.
     *
     * @param cds {@code CarbonDataSource}
     */
    public static void unregister(CarbonDataSource cds) {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering data source: " + cds.getMetadata().getName());
        }
        JNDIConfig jndiConfig = cds.getMetadata().getJndiConfig();
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
