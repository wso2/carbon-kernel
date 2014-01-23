/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.statistics;

/**
 * An interface to be implemented by a statistics collector. Please note that statistics collectors
 * registered as OSGi services are required to be registered after the registry kernel has finished
 * publishing its OSGi service.
 */
public interface StatisticsCollector {

    /**
     * Method to collect statistics.
     *
     * @param parameters the parameters passed into the method initiating a statistics collection
     *                   request.
     */
    void collect(Object ... parameters);

}
