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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Histogram;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.Timer;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

/**
 * Implementation class for {@link MetricService}, which will use the Metrics (https://dropwizard.github.io/metrics)
 * library. This is registered as an OSGi service
 */
public class MetricServiceImpl extends Observable implements MetricService {

    private static final Log log = LogFactory.getLog(MetricServiceImpl.class);

    /**
     * The level configured for Metrics collection
     */
    private Level level;

    /**
     * The previous level to identify changes in level configuration
     */
    private Level previousLevel;

    /**
     * The {@link MetricRegistry} instance from the Metrics Implementation
     */
    private final MetricRegistry metricRegistry;

    private static final String JMX_REPORTING_ENABLED = "Reporting.JMX.Enabled";
    private static final String CSV_REPORTING_ENABLED = "Reporting.CSV.Enabled";
    private static final String CSV_REPORTING_LOCATION = "Reporting.CSV.Location";
    private static final String CSV_REPORTING_POLLING_PERIOD = "Reporting.CSV.PollingPeriod";

    /**
     * JMX domain registered with MBean Server
     */
    private static final String JMX_REPORTING_DOMAIN = "org.wso2.carbon.metrics";

    private final List<Reporter> reporters = new ArrayList<Reporter>();

    /**
     * Initialize the MetricRegistry, Level and Reporters
     */
    public MetricServiceImpl(Level level) {
        metricRegistry = new MetricRegistry();
        Reporter jmxReporter = null;
        try {
            jmxReporter = configureJMXReporter();
        } catch (Throwable e) {
            log.error("Error when configuring JMX reporter", e);
        }

        if (jmxReporter != null) {
            reporters.add(jmxReporter);
        }

        Reporter csvReporter = null;
        try {
            csvReporter = configureCSVReporter();
        } catch (Throwable e) {
            log.error("Error when configuring CSV reporter", e);
        }

        if (csvReporter != null) {
            reporters.add(csvReporter);
        }

        // Initial level
        this.level = Level.OFF;
        setLevel(level);
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.previousLevel = this.level;
        this.level = level;
        boolean changed = !this.level.equals(this.previousLevel);
        if (changed) {
            setChanged();
            notifyObservers(level);
            if (this.level.compareTo(Level.OFF) > 0) {
                startReporters();
            } else if (this.level.equals(Level.OFF)) {
                stopReporters();
            }
        }
    }

    public int getObserverCount() {
        // This count may not be always equal to the actual number of metrics used in the system. The Metrics
        // implementation keeps a map based on the metric name. Hence if a user retrieves the same metric type with
        // existing name, this service will wrap an existing metric instance and add as an observer.
        return countObservers();
    }

    public int getMetricsCount() {
        return metricRegistry.getMetrics().size();
    }

    public Meter createMeter(Level level, String name) {
        MeterImpl meter = new MeterImpl(level, metricRegistry.meter(name));
        meter.setEnabled(getLevel());
        addObserver(meter);
        return meter;
    }

    public Counter createCounter(Level level, String name) {
        CounterImpl counter = new CounterImpl(level, metricRegistry.counter(name));
        counter.setEnabled(getLevel());
        addObserver(counter);
        return counter;
    }

    public Timer createTimer(Level level, String name) {
        TimerImpl timer = new TimerImpl(level, metricRegistry.timer(name));
        timer.setEnabled(getLevel());
        addObserver(timer);
        return timer;
    }

    public Histogram createHistogram(Level level, String name) {
        HistogramImpl histogram = new HistogramImpl(level, metricRegistry.histogram(name));
        histogram.setEnabled(getLevel());
        addObserver(histogram);
        return histogram;
    }

    public <T> void createGauge(Level level, String name, Gauge<T> gauge) {
        GaugeImpl<T> gaugeImpl = new GaugeImpl<T>(level, gauge);
        gaugeImpl.setEnabled(getLevel());
        addObserver(gaugeImpl);
        metricRegistry.register(name, gaugeImpl);
    }
    
    public <T> void createCachedGauge(Level level, String name, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        CachedGaugeImpl<T> gaugeImpl = new CachedGaugeImpl<T>(level, timeout, timeoutUnit, gauge);
        gaugeImpl.setEnabled(getLevel());
        addObserver(gaugeImpl);
        metricRegistry.register(name, gaugeImpl);
    }

    /**
     * Reporter interface to manage multiple reporters in this service
     */
    private interface Reporter {
        void start();

        boolean isRunning();

        void stop();
    }

    private class JmxReporterImpl implements Reporter {

        private final JmxReporter jmxReporter;

        private boolean running;

        public JmxReporterImpl(JmxReporter jmxReporter) {
            this.jmxReporter = jmxReporter;
        }

        @Override
        public void start() {
            jmxReporter.start();
            running = true;
            if (log.isInfoEnabled()) {
                log.info("Started JMX reporter for Metrics");
            }
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public void stop() {
            jmxReporter.stop();
            running = false;
            if (log.isInfoEnabled()) {
                log.info("Stopped JMX reporter for Metrics");
            }
        }
    }

    private class CsvReporterImpl implements Reporter {

        private final CsvReporter csvReporter;

        private final long pollingPeriod;

        private boolean running;

        public CsvReporterImpl(CsvReporter csvReporter, long pollingPeriod) {
            this.csvReporter = csvReporter;
            this.pollingPeriod = pollingPeriod;
        }

        @Override
        public void start() {
            csvReporter.start(pollingPeriod, TimeUnit.SECONDS);

            running = true;
            if (log.isInfoEnabled()) {
                log.info(String.format("Started CSV reporter for Metrics with %d seconds polling period.",
                        pollingPeriod));
            }
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public void stop() {
            csvReporter.stop();
            running = false;
            if (log.isInfoEnabled()) {
                log.info("Stopped CSV reporter for Metrics");
            }
        }
    }

    private void startReporters() {
        for (Reporter reporter : reporters) {
            if (!reporter.isRunning()) {
                reporter.start();
            }
        }
    }

    private void stopReporters() {
        for (Reporter reporter : reporters) {
            if (reporter.isRunning()) {
                reporter.stop();
            }
        }
    }

    private Reporter configureJMXReporter() {
        MetricsConfiguration metricsConfiguration = MetricsConfiguration.getInstance();
        if (!Boolean.parseBoolean(metricsConfiguration.getFirstProperty(JMX_REPORTING_ENABLED))) {
            if (log.isDebugEnabled()) {
                log.debug("JMX Reporting for Metrics is not enabled");
            }
            return null;
        }
        final JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(JMX_REPORTING_DOMAIN)
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        return new JmxReporterImpl(jmxReporter);
    }

    private Reporter configureCSVReporter() {
        MetricsConfiguration metricsConfiguration = MetricsConfiguration.getInstance();
        if (!Boolean.parseBoolean(metricsConfiguration.getFirstProperty(CSV_REPORTING_ENABLED))) {
            if (log.isDebugEnabled()) {
                log.debug("CSV Reporting for Metrics is not enabled");
            }
            return null;
        }
        String location = metricsConfiguration.getFirstProperty(CSV_REPORTING_LOCATION);
        if (location == null || location.trim().isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("CSV Reporting location is not specified");
            }
            return null;
        }
        File file = new File(location);
        if (!file.exists()) {
            if (!file.mkdir()) {
                if (log.isWarnEnabled()) {
                    log.warn("CSV Reporting location was not created!");
                }
                return null;
            }
        }
        if (!file.isDirectory()) {
            if (log.isWarnEnabled()) {
                log.warn("CSV Reporting location is not a directory");
            }
            return null;
        }
        String pollingPeriod = metricsConfiguration.getFirstProperty(CSV_REPORTING_POLLING_PERIOD);
        // Default polling period for CSV reporter is 60 seconds
        long csvReporterPollingPeriod = 60;
        try {
            csvReporterPollingPeriod = Long.parseLong(pollingPeriod);
        } catch (NumberFormatException e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Error parsing the polling period for CSV Reporting. Using %d seconds",
                        csvReporterPollingPeriod));
            }
        }
        if (log.isInfoEnabled()) {
            log.info(String.format("Creating CSV reporter for Metrics with location: %s", location));
        }

        final CsvReporter csvReporter = CsvReporter.forRegistry(metricRegistry).formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build(file);
        return new CsvReporterImpl(csvReporter, csvReporterPollingPeriod);
    }
}
