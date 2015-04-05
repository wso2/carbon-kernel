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

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * Test Cases for {@link MetricService}
 */
public class MetricsServiceTest extends TestCase {

    private static MetricService metricService;

    protected void setUp() throws Exception {
        super.setUp();
        metricService = new MetricServiceImpl(Level.OFF);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testMeterInitialCount() {
        Meter meter = MetricManager.meter(Level.INFO, MetricManager.name(this.getClass(), "test-initial-count"));
        assertEquals("Initial count should be zero", 0, meter.getCount());
        assertEquals("Metrics count should be one", 1, metricService.getMetricsCount());
    }

    public void testMetricServiceLevels() {
        Meter meter = MetricManager.meter(Level.INFO, MetricManager.name(this.getClass(), "test-levels"));
        meter.mark();
        // This is required as we need to check whether level changes are applied to existing metrics
        assertEquals("Count should be zero", 0, meter.getCount());

        metricService.setLevel(Level.TRACE);
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());

        metricService.setLevel(Level.DEBUG);
        meter.mark();
        assertEquals("Count should be two", 2, meter.getCount());

        metricService.setLevel(Level.INFO);
        meter.mark();
        assertEquals("Count should be three", 3, meter.getCount());

        metricService.setLevel(Level.ALL);
        meter.mark();
        assertEquals("Count should be four", 4, meter.getCount());

        metricService.setLevel(Level.OFF);
        meter.mark();
        // There should be no change
        assertEquals("Count should be four", 4, meter.getCount());
    }

    public void testMetricLevels() {
        Meter meter = MetricManager.meter(Level.OFF, MetricManager.name(this.getClass(), "test1"));
        meter.mark();
        assertEquals("Count should be zero", 0, meter.getCount());
        metricService.setLevel(Level.OFF);
        meter.mark();
        assertEquals("Count should be zero", 0, meter.getCount());

        metricService.setLevel(Level.TRACE);
        meter = MetricManager.meter(Level.TRACE, MetricManager.name(this.getClass(), "test2"));
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());

        meter = MetricManager.meter(Level.DEBUG, MetricManager.name(this.getClass(), "test3"));
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());

        metricService.setLevel(Level.DEBUG);
        meter = MetricManager.meter(Level.TRACE, MetricManager.name(this.getClass(), "test4"));
        meter.mark();
        assertEquals("Count should be zero", 0, meter.getCount());

        meter = MetricManager.meter(Level.DEBUG, MetricManager.name(this.getClass(), "test5"));
        meter.mark(100);
        assertEquals("Count should be one hundred", 100, meter.getCount());

    }
}
