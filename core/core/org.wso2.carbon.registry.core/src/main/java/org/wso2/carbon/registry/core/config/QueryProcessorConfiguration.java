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

package org.wso2.carbon.registry.core.config;

/**
 * This class contains configuration used by the Query Processors.
 */
public class QueryProcessorConfiguration {

    private String queryType;
    private String processorClassName;

    /**
     * Method to obtain the type of the query.
     *
     * @return the type of the query.
     */
    public String getQueryType() {
        return queryType;
    }

    /**
     * Method to obtain the type of the query.
     *
     * @param queryType the type of the query.
     */
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    /**
     * Method to obtain the type of the query.
     *
     * @return the type of the query.
     */
    public String getProcessorClassName() {
        return processorClassName;
    }

    /**
     * Method set obtain the type of the query.
     *
     * @param processorClassName the type of the query.
     */
    public void setProcessorClassName(String processorClassName) {
        this.processorClassName = processorClassName;
    }
}
