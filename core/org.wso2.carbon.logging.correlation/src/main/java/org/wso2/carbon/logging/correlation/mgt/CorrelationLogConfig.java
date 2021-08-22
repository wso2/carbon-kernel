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


import org.wso2.carbon.logging.correlation.ConfigObserver;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogConstants;

/**
 * The implementation of the Correlation log MBean.
 */
public class CorrelationLogConfig implements CorrelationLogConfigMBean {
    private boolean enable;
    private String components;
    private String blacklistedThreads;
    private String logAllMethods;
    private ConfigObserver configObserver;

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void setEnable(boolean enable) {
        this.enable = enable;
        this.configObserver.notify(CorrelationLogConstants.ENABLE, this.enable);
    }

    @Override
    public String getComponents() {
        return components;
    }

    @Override
    public void setComponents(String components) {
        this.components = components;
        this.configObserver.notify(CorrelationLogConstants.COMPONENTS, this.components);
    }

    @Override
    public String getBlacklistedThreads() {
        return blacklistedThreads;
    }

    @Override
    public void setBlacklistedThreads(String blacklistedThreads) {
        this.blacklistedThreads = blacklistedThreads;
        this.configObserver.notify(CorrelationLogConstants.BLACKLISTED_THREADS, this.blacklistedThreads);
    }

    @Override
    public String getLogAllMethods() {
        return logAllMethods;
    }

    @Override
    public void setLogAllMethods(String logAllMethods) {
        this.logAllMethods = logAllMethods;
        this.configObserver.notify(CorrelationLogConstants.LOG_ALL_METHODS, this.logAllMethods);
    }

    /**
     * Register observer to be notified when all the field changes,
     *
     * @param configObserver
     */
    public void registerObserver(ConfigObserver configObserver) {
        this.configObserver = configObserver;
    }
}
