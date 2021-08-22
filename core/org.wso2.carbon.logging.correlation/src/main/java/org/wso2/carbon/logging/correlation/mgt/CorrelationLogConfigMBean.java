/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.logging.correlation.mgt;

/**
 * The MBean used for the JMX endpoint.
 */
public interface CorrelationLogConfigMBean {

    /**
     * Returns a flag indicating whether the correlation logs are enabled or disabled.
     *
     * @return
     */
    boolean isEnable();

    /**
     * Enable/disable correlation logs.
     *
     * @param enable
     */
    void setEnable(boolean enable);

    /**
     * Returns a comma separated list of components to enable correlation logs.
     *
     * @return
     */
    String getComponents();

    /**
     * Set comma separated list of components to enable correlation logs.
     * @param components
     */
    void setComponents(String components);

    /**
     * Returns a comma separated list of threads which are ignored while logging.
     *
     * @return
     */
    String getBlacklistedThreads();

    /**
     * Set a comma separated list of threads which are ignored while logging.
     * @param blacklistedThreads
     */
    void setBlacklistedThreads(String blacklistedThreads);

    /**
     * Returns a flag indicating logging all methods.
     *
     * @return
     */
    String getLogAllMethods();

    /**
     * Enable/disable logging all methods.
     *
     * @param logAllMethods
     */
    void setLogAllMethods(String logAllMethods);
}
