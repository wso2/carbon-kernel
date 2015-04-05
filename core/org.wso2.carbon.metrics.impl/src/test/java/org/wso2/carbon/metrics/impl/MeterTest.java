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

import java.util.Random;

import junit.framework.TestCase;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * Test Cases for {@link Meter}
 */
public class MeterTest extends TestCase {

    private MetricService metricService;

    private Random randomGenerator = new Random();

    protected void setUp() throws Exception {
        super.setUp();
        metricService = new MetricServiceImpl(Level.TRACE);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testInitialCount() {
        Meter meter = MetricManager.meter(Level.INFO, MetricManager.name(this.getClass(), "test-initial-count"));
        assertEquals("Initial count should be zero", 0, meter.getCount());
        Meter meter2 = MetricManager.meter(Level.INFO, MetricManager.name(this.getClass(), "test-initial-count"));
        assertEquals("Initial count should be zero", 0, meter2.getCount());
    }

    public void testSameMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-meter");
        Meter meter = MetricManager.meter(Level.INFO, name);
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());

        Meter meter2 = MetricManager.meter(Level.INFO, name);
        assertEquals("Count should be one", 1, meter2.getCount());
    }

    public void testMarkEvent() {
        Meter meter = MetricManager.meter(Level.INFO, MetricManager.name(this.getClass(), "test-meter-mark"));
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());

        metricService.setLevel(Level.OFF);
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());
    }

    public void testMarkEventByRandomNumber() {
        Meter meter = MetricManager.meter(Level.INFO, MetricManager.name(this.getClass(), "test-meter-mark-rand"));
        int n = randomGenerator.nextInt();
        meter.mark(n);
        assertEquals("Count should be " + n, n, meter.getCount());

        metricService.setLevel(Level.OFF);
        meter.mark(n);
        assertEquals("Count should be " + n, n, meter.getCount());
    }
}
