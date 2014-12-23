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
package org.wso2.carbon.metrics.manager;

import java.util.concurrent.TimeUnit;

/**
 * Main interface for the service creating various metrics
 */
public interface MetricService {

    /**
     * @return The current {@link Level}
     */
    Level getLevel();

    /**
     * Set a new level to the Metrics Service
     * 
     * @param level New {@link Level}
     */
    void setLevel(Level level);

    /**
     * Return the number of metrics used
     * 
     * @return The metrics count
     */
    int getMetricsCount();

    /**
     * Create a {@link Meter} instance for the given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Meter} instance
     */
    Meter createMeter(Level level, String name);

    /**
     * Create a {@link Counter} instance for the given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Counter} instance
     */
    Counter createCounter(Level level, String name);

    /**
     * Create a {@link Timer} instance for the given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Timer} instance
     */
    Timer createTimer(Level level, String name);

    /**
     * Create a {@link Histogram} instance for the given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Histogram} instance
     */
    Histogram createHistogram(Level level, String name);

    /**
     * Register a {@link Gauge} for the given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param gauge An implementation of {@link Gauge}
     */
    <T> void createGauge(Level level, String name, Gauge<T> gauge);

    /**
     * Register a cached {@link Gauge} for the given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param timeout the timeout
     * @param timeoutUnit the unit of {@code timeout}
     * @param gauge An implementation of {@link Gauge}
     */
    <T> void createCachedGauge(Level level, String name, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge);

}
