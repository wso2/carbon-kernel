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
package org.wso2.carbon.metrics.manager.jmx;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricService;

/**
 * Implementation for Metric Manager JMX Bean
 */
public class MetricManagerMXBeanImpl implements MetricManagerMXBean {

    private final MetricService metricService;

    public MetricManagerMXBeanImpl(MetricService metricService) {
        super();
        this.metricService = metricService;
    }

    @Override
    public int getMetricsCount() {
        return metricService.getMetricsCount();
    }

    @Override
    public String getLevel() {
        return metricService.getLevel().name();
    }

    @Override
    public void setLevel(String level) {
        metricService.setLevel(Level.valueOf(level));
    }

}
