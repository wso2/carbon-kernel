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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * MetricManager is a static utility class providing various metrics.
 */
public final class MetricManager {

    private static final ConcurrentMap<String, MetricWrapper<? extends Metric>> metrics = new ConcurrentHashMap<String, MetricWrapper<? extends Metric>>();

    private MetricManager() {
    }

    /**
     * MetricWrapper class is used for the metrics map. This class keeps the associated {@link Level} with metric
     */
    private static class MetricWrapper<T extends Metric> {

        private final Level level;
        private final T metric;

        private MetricWrapper(Level level, T metric) {
            super();
            this.level = level;
            this.metric = metric;
        }
    }

    /**
     * Concatenates elements to form a dotted name
     *
     * @param name the first element of the name
     * @param names the remaining elements of the name
     * @return {@code name} and {@code names} concatenated by periods
     */
    public static String name(String name, String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, name);
        if (names != null) {
            for (String s : names) {
                append(builder, s);
            }
        }
        return builder.toString();
    }

    /**
     * Concatenates a class name and elements to form a dotted name
     *
     * @param klass the first element of the name
     * @param names the remaining elements of the name
     * @return {@code klass} and {@code names} concatenated by periods
     */
    public static String name(Class<?> klass, String... names) {
        return name(klass.getName(), names);
    }

    private static void append(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Metric> T getOrCreateMetric(Level level, String name, MetricBuilder<T> metricBuilder) {
        MetricWrapper<? extends Metric> metricWrapper = metrics.get(name);
        if (metricWrapper != null) {
            Metric metric = metricWrapper.metric;
            if (metricBuilder.isInstance(metric)) {
                if (level.equals(metricWrapper.level)) {
                    return (T) metric;
                } else {
                    throw new IllegalArgumentException(name + " is already used with a different level");
                }
            } else {
                throw new IllegalArgumentException(name + " is already used for a different type of metric");
            }
        } else {
            T newMetric = metricBuilder.createMetric(level, name);
            metricWrapper = new MetricWrapper<T>(level, newMetric);
            metrics.put(name, metricWrapper);
            return newMetric;
        }
    }

    /**
     * An interface for creating a new metric from MetricService
     */
    private static interface MetricBuilder<T extends Metric> {
        T createMetric(Level level, String name);

        boolean isInstance(Metric metric);
    }

    private static final MetricBuilder<Meter> METER_BUILDER = new MetricBuilder<Meter>() {
        @Override
        public Meter createMetric(Level level, String name) {
            MetricService metricService = ServiceReferenceHolder.getInstance().getMetricService();
            return metricService.createMeter(level, name);
        }

        @Override
        public boolean isInstance(Metric metric) {
            return Meter.class.isInstance(metric);
        }
    };

    private static final MetricBuilder<Counter> COUNTER_BUILDER = new MetricBuilder<Counter>() {
        @Override
        public Counter createMetric(Level level, String name) {
            MetricService metricService = ServiceReferenceHolder.getInstance().getMetricService();
            return metricService.createCounter(level, name);
        }

        @Override
        public boolean isInstance(Metric metric) {
            return Counter.class.isInstance(metric);
        }
    };

    private static final MetricBuilder<Timer> TIMER_BUILDER = new MetricBuilder<Timer>() {
        @Override
        public Timer createMetric(Level level, String name) {
            MetricService metricService = ServiceReferenceHolder.getInstance().getMetricService();
            return metricService.createTimer(level, name);
        }

        @Override
        public boolean isInstance(Metric metric) {
            return Timer.class.isInstance(metric);
        }
    };

    private static final MetricBuilder<Histogram> HISTOGRAM_BUILDER = new MetricBuilder<Histogram>() {
        @Override
        public Histogram createMetric(Level level, String name) {
            MetricService metricService = ServiceReferenceHolder.getInstance().getMetricService();
            return metricService.createHistogram(level, name);
        }

        @Override
        public boolean isInstance(Metric metric) {
            return Histogram.class.isInstance(metric);
        }
    };

    private static class GaugeBuilder<T> implements MetricBuilder<Gauge<T>> {

        private final Gauge<T> gauge;

        public GaugeBuilder(Gauge<T> gauge) {
            super();
            this.gauge = gauge;
        }

        @Override
        public Gauge<T> createMetric(Level level, String name) {
            MetricService metricService = ServiceReferenceHolder.getInstance().getMetricService();
            metricService.createGauge(level, name, gauge);
            return gauge;
        }

        @Override
        public boolean isInstance(Metric metric) {
            return Gauge.class.isInstance(metric);
        }
    };

    private static class CachedGaugeBuilder<T> extends GaugeBuilder<T> implements MetricBuilder<Gauge<T>> {

        private final Gauge<T> gauge;
        private final long timeout;
        private final TimeUnit timeoutUnit;

        public CachedGaugeBuilder(Gauge<T> gauge, long timeout, TimeUnit timeoutUnit) {
            super(gauge);
            this.gauge = gauge;
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
        }

        @Override
        public Gauge<T> createMetric(Level level, String name) {
            MetricService metricService = ServiceReferenceHolder.getInstance().getMetricService();
            metricService.createCachedGauge(level, name, timeout, timeoutUnit, gauge);
            return gauge;
        }

    };

    /**
     * Return a {@link Meter} instance registered under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Meter} instance
     */
    public static Meter meter(Level level, String name) {
        return getOrCreateMetric(level, name, METER_BUILDER);
    }

    /**
     * Return a {@link Counter} instance registered under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Counter} instance
     */
    public static Counter counter(Level level, String name) {
        return getOrCreateMetric(level, name, COUNTER_BUILDER);
    }

    /**
     * Return a {@link Timer} instance registered under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Timer} instance
     */
    public static Timer timer(Level level, String name) {
        return getOrCreateMetric(level, name, TIMER_BUILDER);
    }

    /**
     * Return a {@link Histogram} instance registered under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Histogram} instance
     */
    public static Histogram histogram(Level level, String name) {
        return getOrCreateMetric(level, name, HISTOGRAM_BUILDER);
    }

    /**
     * Register a {@link Gauge} instance under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void gauge(Level level, String name, Gauge<T> gauge) {
        getOrCreateMetric(level, name, new GaugeBuilder<T>(gauge));
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metrics
     * @param timeout The timeout value
     * @param timeoutUnit The {@link TimeUnit} for the {@link timeout}
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(Level level, String name, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        getOrCreateMetric(level, name, new CachedGaugeBuilder<T>(gauge, timeout, timeoutUnit));
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout in seconds
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metrics
     * @param timeout The timeout value in seconds
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(Level level, String name, long timeout, Gauge<T> gauge) {
        getOrCreateMetric(level, name, new CachedGaugeBuilder<T>(gauge, timeout, TimeUnit.SECONDS));
    }
}
