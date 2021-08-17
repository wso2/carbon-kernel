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

package org.wso2.carbon.logging.correlation.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.logging.correlation.mgt.CorrelationLogConfig;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogConstants;
import org.wso2.carbon.logging.correlation.CorrelationLogService;
import org.wso2.carbon.logging.correlation.Notifiable;
import org.wso2.carbon.utils.MBeanRegistrar;

import java.util.HashMap;
import java.util.Map;

@Component(immediate = true)
public class CorrelationLogManager implements Notifiable {

    Map<String, CorrelationLogService> serviceMap;
    Map<String, Object> logProperties;
    org.wso2.carbon.logging.correlation.mgt.CorrelationLogConfig logConfig;

    @Activate
    protected void activate(ComponentContext context) {
        serviceMap = new HashMap<>();
        logProperties = initProperties();

        logConfig = new CorrelationLogConfig();
        logConfig.registerNotifier(this);
        MBeanRegistrar.registerMBean(logConfig);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        serviceMap = null;
    }

    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MULTIPLE,
            unbind = "unsetCorrelationLogService"
    )
    protected void setCorrelationLogService(CorrelationLogService service) {
        serviceMap.put(service.getName(), service);
        service.reconfigure(logProperties);
    }

    protected void unsetCorrelationLogService(CorrelationLogService service) {
        serviceMap.remove(service.getName());
    }

    @Override
    public void notify(String key, Object value) {
        logProperties.put(key, value);
        reconfigure();
    }

    private void reconfigure() {
        for (Map.Entry<String, CorrelationLogService> entry : serviceMap.entrySet()) {
            entry.getValue().reconfigure(logProperties);
        }
    }

    private Map<String, Object> initProperties() {
        // TODO: Read default properties from the deployment.toml file
        Map<String, Object> properties = new HashMap<>();
        properties.put(CorrelationLogConstants.ENABLE, false);
        properties.put(CorrelationLogConstants.COMPONENTS, "");
        properties.put(CorrelationLogConstants.BLACKLISTED_THREADS, "");
        return properties;
    }
}
