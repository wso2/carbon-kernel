/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.utils.logging;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.logging.LogRecord;

public class LoggingUtils {

    /**
     * Return a logging event from the given log record
     *
     * @param record
     *         - the log record
     * @return - a LoggingEvent
     */
    public static LoggingEvent getLogEvent(LogRecord record) {
        Priority level = getLogLevel(record.getLevel());
        return new LoggingEvent(record.getSourceClassName(),
                                Logger.getLogger(record.getSourceClassName()), level,
                                record.getMessage(), record.getThrown());
    }

    /**
     * Returns a TenantAwareLoggingEvent that wraps the LoggingEvent with tenant specific tenantId
     * and serviceName
     *
     * @param loggingEvent
     *         -  The LoggingEvent with the log content
     * @param tenantId
     *         - tenant Id of the tenant which triggered log event
     * @param serviceName
     *         - service name of the current log event
     * @return a TenantAwareLoggingEvent
     */
    public static TenantAwareLoggingEvent getTenantAwareLogEvent(LoggingEvent loggingEvent,
                                                                 int tenantId, String serviceName) {
        Logger logger = Logger.getLogger(loggingEvent.getLoggerName());
        TenantAwareLoggingEvent tenantAwareLoggingEvent;
        ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();

        Throwable throwable;
        // loggingEvent.getThrowableInformation may return null if there's no such information
        // therefore add a null check here
        if (null == throwableInformation) {
            throwable = null;
        } else {
            throwable = throwableInformation.getThrowable();
        }
        tenantAwareLoggingEvent = new TenantAwareLoggingEvent(loggingEvent.fqnOfCategoryClass,
                                                              logger, loggingEvent.timeStamp,
                                                              loggingEvent.getLevel(),
                                                              loggingEvent.getMessage(), throwable);
        tenantAwareLoggingEvent.setTenantId(Integer.toString(tenantId));
        tenantAwareLoggingEvent.setServiceName(serviceName);
        return tenantAwareLoggingEvent;
    }

    /**
     * Return the <code>org.apache.log4j.Level</code> from the given
     * <code>java.util.logging.Level</code>
     *
     * @param level
     *         - the <code>java.util.logging.Level</code>
     * @return - an <code>org.apache.log4j.Level</code>
     */
    public static org.apache.log4j.Level getLogLevel(java.util.logging.Level level) {
        if (level == java.util.logging.Level.OFF) {
            return Level.OFF;
        } else if (level == java.util.logging.Level.SEVERE) {
            return Level.ERROR;
        } else if (level == java.util.logging.Level.WARNING) {
            return Level.WARN;
        } else if (level == java.util.logging.Level.INFO) {
            return Level.INFO;
        } else if (level == java.util.logging.Level.CONFIG || level == java.util.logging.Level
                .FINE) {
            return Level.DEBUG;
        } else {
            return Level.TRACE;
        }
    }
}
