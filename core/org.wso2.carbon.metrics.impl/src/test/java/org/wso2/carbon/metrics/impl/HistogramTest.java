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

import org.wso2.carbon.metrics.manager.Histogram;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * Test Cases for {@link Histogram}
 */
public class HistogramTest extends TestCase {

    private MetricService metricService;

    private Random randomGenerator = new Random();

    protected void setUp() throws Exception {
        super.setUp();
        metricService = new MetricServiceImpl(Level.TRACE);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testInitialCount() {
        Histogram histogram = MetricManager.histogram(Level.INFO,
                MetricManager.name(this.getClass(), "test-initial-count"));
        assertEquals("Initial count should be zero", 0, histogram.getCount());
    }

    public void testSameMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-histogram");
        Histogram histogram = MetricManager.histogram(Level.INFO, name);
        histogram.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, histogram.getCount());

        Histogram histogram2 = MetricManager.histogram(Level.INFO, name);
        assertEquals("Count should be one", 1, histogram2.getCount());
    }

    public void testUpdateInt() {
        Histogram histogram = MetricManager.histogram(Level.INFO,
                MetricManager.name(this.getClass(), "test-histogram-update-int"));
        histogram.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, histogram.getCount());

        metricService.setLevel(Level.OFF);
        histogram.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, histogram.getCount());
    }

    public void testUpdateLong() {
        Histogram histogram = MetricManager.histogram(Level.INFO,
                MetricManager.name(this.getClass(), "test-histogram-update-long"));

        histogram.update(randomGenerator.nextLong());
        assertEquals("Count should be one", 1, histogram.getCount());

        metricService.setLevel(Level.OFF);
        histogram.update(randomGenerator.nextLong());
        assertEquals("Count should be one", 1, histogram.getCount());
    }

}
