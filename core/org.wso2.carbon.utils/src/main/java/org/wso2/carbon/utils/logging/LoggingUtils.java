package org.wso2.carbon.utils.logging;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.logging.LogRecord;

public class LoggingUtils {

    public static LoggingEvent getLogEvent(LogRecord record) {
        Priority level = getLogLevel(record.getLevel());
        return new LoggingEvent(record.getSourceClassName(), Logger.getLogger(record.getSourceClassName()), level, record.getMessage(), record.getThrown());
    }

    /**
     * Returns a TenantAwareLoggingEvent that wraps the LoggingEvent with tenant specific
     * tenantId and serviceName
     *
     * @param loggingEvent -  The LoggingEvent with the log content
     * @param tenantId     - tenant Id of the tenant which triggered log event
     * @param serviceName  - service name of the current log event
     * @return a TenantAwareLoggingEvent
     */
    public static TenantAwareLoggingEvent getTenantAwareLogEvent(LoggingEvent loggingEvent,
                                                                 int tenantId, String serviceName) {
        Logger logger = Logger.getLogger(loggingEvent.getLoggerName());
        TenantAwareLoggingEvent tenantAwareLoggingEvent;
        ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();

        Throwable throwable;
        if (null == throwableInformation) { // null check
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

    public static org.apache.log4j.Level getLogLevel(java.util.logging.Level level) {
        if (level == java.util.logging.Level.OFF) {
            return  Level.OFF;
        }else if (level == java.util.logging.Level.SEVERE) {
            return Level.ERROR;
        }else if (level == java.util.logging.Level.WARNING) {
            return Level.WARN;
        } else if (level == java.util.logging.Level.INFO) {
            return Level.INFO;
        }else if (level == java.util.logging.Level.CONFIG || level == java.util.logging.Level.FINE) {
            return Level.DEBUG;
//      }else if (level == java.util.logging.Level.FINER || level == java.util.logging.Level.FINEST) {
        }else{
            return Level.TRACE;
        }
    }
}
