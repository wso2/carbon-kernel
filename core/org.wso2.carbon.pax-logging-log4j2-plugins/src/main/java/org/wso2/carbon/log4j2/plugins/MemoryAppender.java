/*
 * Copyright 2017 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.log4j2.plugins;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Booleans;

import java.io.Serializable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.wso2.carbon.utils.logging.CircularBuffer;
import org.wso2.carbon.utils.logging.MutableLogEvent;

/**
 * This appender will be used to capture the logs and later send to clients, if requested via the
 * logging web service.
 * This maintains a circular buffer, of some fixed amount (say 100).
 * Replaces the previous MemoryAppender and no changes were done to the variable values
 */
@Plugin(name = "MemoryAppender", category = "Core", elementType = "appender", printObject = true)
public final class MemoryAppender extends AbstractAppender {
    private CircularBuffer<LogEvent> circularBuffer;
    private int bufferSize = -1;

    /**
     * Constructs an instance of MemoryAppender.
     *
     * @param name appender name
     * @param filter null if not specified
     * @param layout pattern of log messages
     * @param ignoreExceptions default is true
     *
     * Called by {@link #createAppender(String, Filter, Layout, String)}
     */
    private MemoryAppender(final String name, final Filter filter,
            final Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    /**
     * Creates a CarbonMemoryAppender instance with
     * attributes configured in log4j2.properties.
     *
     * @param name appender name
     * @param filter null if not specified
     * @param layout pattern of log messages
     * @param ignore default is true
     * @return intance of MemoryAppender
     */
    @PluginFactory
    public static MemoryAppender createAppender(@PluginAttribute("name") final String name,
                                                @PluginElement("Filters") final Filter filter,
                                                @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                @PluginAttribute("ignoreExceptions") final String ignore) {
        if (name == null) {
            LOGGER.error("No name provided for MemoryAppender");
            return null;
        } else {
            if (layout == null) {
                layout = PatternLayout.createDefaultLayout();
            }
            final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
            return new MemoryAppender(name, filter, layout, ignoreExceptions);
        }
    }

    /**
     * This is the overridden method from the Appender interface.
     * This allows to write log events to preferred destination.
     *
     * Converts the default log events to tenant aware log events and writes to a CircularBuffer
     *
     * @param logEvent the LogEvent object
     */
    @Override
    public void append(LogEvent logEvent) {
        if (circularBuffer != null) {
            MutableLogEvent mutableLogEvent = new MutableLogEvent(logEvent);
            circularBuffer.append(mutableLogEvent.getMutableLogEvent());
        }
    }

    /**
     * Taken from the previous MemoryAppender
     */
    public CircularBuffer<LogEvent> getCircularQueue() {
        return circularBuffer;
    }

    /**
     * Taken from the previous MemoryAppender
     */
    public void setCircularBuffer(CircularBuffer<LogEvent> circularBuffer) {
        this.circularBuffer = circularBuffer;
    }

    /**
     * Taken from the previous MemoryAppender
     */
    public void activateOptions() {
        if (bufferSize < 0) {
            if (circularBuffer == null) {
                this.circularBuffer = new CircularBuffer<LogEvent>();
            }
        } else {
            this.circularBuffer = new CircularBuffer<LogEvent>(bufferSize);
        }
    }

    /**
     * Taken from the previous MemoryAppender
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Taken from the previous MemoryAppender
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
