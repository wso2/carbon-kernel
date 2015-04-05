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

import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * Test Cases for {@link Counter}
 */
public class CounterTest extends TestCase {

    private MetricService metricService;

    private Random randomGenerator = new Random();

    protected void setUp() throws Exception {
        super.setUp();
        metricService = new MetricServiceImpl(Level.TRACE);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testInitialCount() {
        Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass(), "test-counter"));
        assertEquals("Initial count should be zero", 0, counter.getCount());
    }

    public void testSameMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-counter");

        Counter counter = MetricManager.counter(Level.INFO, name);
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());
        Counter counter2 = MetricManager.counter(Level.INFO, name);
        assertEquals("Count should be one", 1, counter2.getCount());
    }

    public void testIncrementByOne() {
        Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass(), "test-counter-inc1"));
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());

        metricService.setLevel(Level.OFF);
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());
    }

    public void testIncrementByRandomNumber() {
        Counter counter = MetricManager.counter(Level.INFO,
                MetricManager.name(this.getClass(), "test-counter-inc-rand"));
        int n = randomGenerator.nextInt();
        counter.inc(n);
        assertEquals("Count should be " + n, n, counter.getCount());

        metricService.setLevel(Level.OFF);
        counter.inc(n);
        assertEquals("Count should be " + n, n, counter.getCount());
    }

    public void testDecrementByOne() {
        Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass(), "test-counter-dec1"));
        counter.dec();
        assertEquals("Count should be -1", -1, counter.getCount());

        metricService.setLevel(Level.OFF);
        counter.dec();
        assertEquals("Count should be -1", -1, counter.getCount());
    }

    public void testDecrementByRandomNumber() {
        Counter counter = MetricManager.counter(Level.INFO,
                MetricManager.name(this.getClass(), "test-counter-dec-rand"));
        int n = randomGenerator.nextInt();
        counter.dec(n);
        assertEquals("Count should be " + n, 0 - n, counter.getCount());

        metricService.setLevel(Level.OFF);
        counter.dec(n);
        assertEquals("Count should be " + n, 0 - n, counter.getCount());
    }

}
