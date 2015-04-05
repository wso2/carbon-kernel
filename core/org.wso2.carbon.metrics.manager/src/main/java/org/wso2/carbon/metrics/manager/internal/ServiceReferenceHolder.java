/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.manager.internal;

import org.wso2.carbon.metrics.manager.MetricService;

/**
 * Holding references to the OSGi services
 */
public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder INSTANCE = new ServiceReferenceHolder();

    private MetricService metricService;

    public static ServiceReferenceHolder getInstance() {
        return INSTANCE;
    }

    private ServiceReferenceHolder() {
    }

    public void setMetricService(MetricService metricService) {
        this.metricService = metricService;
    }

    public MetricService getMetricService() {
        return metricService;
    }

}
