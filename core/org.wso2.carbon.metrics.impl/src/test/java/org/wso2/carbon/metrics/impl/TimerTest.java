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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.metrics.manager.Timer.Context;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * Test Cases for {@link Timer}
 */
public class TimerTest extends TestCase {

    private MetricService metricService;

    protected void setUp() throws Exception {
        super.setUp();
        metricService = new MetricServiceImpl(Level.TRACE);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testInitialCount() {
        Timer timer = MetricManager.timer(Level.INFO, MetricManager.name(this.getClass(), "test-initial-count"));
        assertEquals("Initial count should be zero", 0, timer.getCount());
    }
    
    public void testSameMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-timer");
        Timer timer = MetricManager.timer(Level.INFO, name);
        timer.update(1, TimeUnit.SECONDS);
        assertEquals("Timer count should be one", 1, timer.getCount());

        Timer timer2 = MetricManager.timer(Level.INFO, name);
        assertEquals("Timer count should be one", 1, timer2.getCount());
    }

    public void testTime() {
        Timer timer = MetricManager.timer(Level.INFO, MetricManager.name(this.getClass(), "test-timer-start"));
        Context context = timer.start();
        assertTrue("Timer works!", context.stop() > 0);
        assertEquals("Timer count should be one", 1, timer.getCount());

        metricService.setLevel(Level.OFF);
        context = timer.start();
        assertEquals("Timer should not work", 0, context.stop());
    }

    public void testTimerUpdateCount() {
        Timer timer = MetricManager.timer(Level.INFO, MetricManager.name(this.getClass(), "test-timer-update"));
        timer.update(1, TimeUnit.SECONDS);
        assertEquals("Timer count should be one", 1, timer.getCount());

        metricService.setLevel(Level.OFF);
        timer.update(1, TimeUnit.SECONDS);
        assertEquals("Timer count should be one", 1, timer.getCount());
    }

    public void testTimerCallableInstances() throws Exception {
        Timer timer = MetricManager.timer(Level.INFO, MetricManager.name(this.getClass(), "test-timer-callable"));
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "test";
            }

        };
        String value = timer.time(callable);
        assertEquals("Value should be 'test'", "test", value);

        metricService.setLevel(Level.OFF);
        value = timer.time(callable);
        assertNull("Value should be null", value);
    }

}
