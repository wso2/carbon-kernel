/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.logging.correlation.utils;

import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigurable;
import org.wso2.carbon.logging.correlation.bean.CorrelationLogConfig;
import org.wso2.carbon.logging.correlation.bean.ImmutableCorrelationLogConfig;

public class CorrelationLogHolder {

    private static final Log log = LogFactory.getLog(CorrelationLogHolder.class);
    private static HashMap<String, CorrelationLogConfigurable> services = new HashMap<>();
    private static final CorrelationLogHolder correlationLogHolder = new CorrelationLogHolder();

    private boolean systemEnabledCorrelationLogs = false;

    private CorrelationLogHolder() {
    }

    public static CorrelationLogHolder getInstance() {
        return correlationLogHolder;
    }

    public void addCorrelationLogConfigurableService(CorrelationLogConfigurable service) {
        services.put(service.getName().trim(), service);
    }

    public void removeCorrelationLogConfigurableService(String serviceName) {
        services.remove(serviceName);
    }

    public CorrelationLogConfigurable getLogServiceInstance(String serviceName) {

        if (services.containsKey(serviceName)) {
            if (log.isDebugEnabled()) {
                log.debug("Accessing Log Service Instance : " + serviceName);
            }
            return services.get(serviceName);
        }
        return null;
    }

    public void setCorrelationLogServiceConfigs(CorrelationLogConfig config) {
        if (!systemEnabledCorrelationLogs) {
            for (CorrelationLogConfigurable service : services.values()) {
                String componentName = service.getName().trim();
                ImmutableCorrelationLogConfig componentConfig = new ImmutableCorrelationLogConfig(
                        config.isEnable() && CorrelationLogUtil.isComponentAllowed(componentName, config.getComponents()),
                        config.getDeniedThreads(),
                        false);
                service.onConfigure(componentConfig);
            }
        }
    }

    public void setSystemEnabledCorrelationLogs(boolean systemEnabledCorrelationLogs) {
        this.systemEnabledCorrelationLogs = systemEnabledCorrelationLogs;
    }
}
