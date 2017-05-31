/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.jdbc.queries;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.QueryProcessorConfiguration;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.dataaccess.QueryProcessor;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserRealm;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * The class is handling the query processing
 */
public class QueryProcessorManager {

    private Map<String, QueryProcessor> queryProcessors = new HashMap<String, QueryProcessor>();

    /**
     * Construct a query processor manager object from the data source and registry context.
     *
     * @param dataAccessManager the data access manager object
     * @param registryContext   registry context
     *
     * @throws RegistryException throws if the construction failed.
     */
    public QueryProcessorManager(DataAccessManager dataAccessManager, RegistryContext registryContext)
            throws RegistryException {
        queryProcessors.put(RegistryConstants.SQL_QUERY_MEDIA_TYPE,
                dataAccessManager.getQueryProcessor());

        // add run-time query processors configured using registry.xml

        if (registryContext != null) {
            for (Object queryProcessorObject : registryContext.getQueryProcessors()) {
                QueryProcessorConfiguration configuration =
                        (QueryProcessorConfiguration) queryProcessorObject;

                try {
                    Class qpClass = Class.forName(configuration.getProcessorClassName());
                    Constructor constructor =
                            qpClass.getConstructor(DataAccessManager.class);

                    QueryProcessor queryProcessor =
                            (QueryProcessor) constructor.newInstance(dataAccessManager);

                    queryProcessors.put(configuration.getQueryType(), queryProcessor);

                } catch (ClassNotFoundException e) {
                    String msg = "Could not find the query processor class for query type: " +
                            configuration.getQueryType();
                    throw new RegistryException(msg, e);

                } catch (Exception e) {
                    String msg = "Failed to initiate query processor for query type: " +
                            configuration.getQueryType();
                    throw new RegistryException(msg, e);

                }
            }
        }
    }

    /**
     * Return the query processor with the given type.
     *
     * @param queryType the query type
     *
     * @return the query processor of the given type.
     */
    @SuppressWarnings("unused")
    public QueryProcessor getQueryProcessor(String queryType) {
        return queryProcessors.get(queryType);
    }

    /**
     * Registers the provided query processor with the given type.
     *
     * @param queryType the query type
     * @param processor the query processor
     */
    @SuppressWarnings("unused")
    public void setQueryProcessor(String queryType, QueryProcessor processor) {
        queryProcessors.put(queryType, processor);
    }

    /**
     * Execute a query using the query processor associated with the media type of the query.
     *
     * @param registry      the registry
     * @param queryResource the query as a resource.
     * @param parameters    the custom parameters to be passed to the query.
     *
     * @return a collection containing query results as children
     * @throws RegistryException throws if the query execution failed.
     */
    public Collection executeQuery(Registry registry, Resource queryResource, Map parameters)
            throws RegistryException {

        if (queryResource.getMediaType() == null || queryResource.getMediaType().length() == 0) {
            String msg = "Failed to execute query at path: " + queryResource.getPath() +
                    ". Query resources should have a media type to map to a query processor.";
            throw new RegistryException(msg);
        }

        QueryProcessor queryProcessor =
                queryProcessors.get(queryResource.getMediaType());
        if (queryProcessor == null) {
            String msg = "Failed to execute query at path: " + queryResource.getPath() +
                    ". No query processor is associated with the query type: " +
                    queryResource.getMediaType();
            throw new RegistryException(msg);
        }

        return queryProcessor.executeQuery(registry, queryResource, parameters);
    }
}
