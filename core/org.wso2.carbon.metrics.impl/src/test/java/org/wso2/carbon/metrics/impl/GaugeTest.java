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
package org.wso2.carbon.metrics.impl;

import junit.framework.TestCase;

import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * Test Cases for {@link Gauge}
 */
public class GaugeTest extends TestCase {

    private MetricService metricService;

    protected void setUp() throws Exception {
        super.setUp();
        metricService = new MetricServiceImpl(Level.TRACE);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testSameMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-guage");

        Gauge<Integer> gauge = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        };

        MetricManager.gauge(Level.INFO, name, gauge);

        // This call also should be successful as we are getting the same gauge
        MetricManager.gauge(Level.INFO, name, gauge);
    }

}
