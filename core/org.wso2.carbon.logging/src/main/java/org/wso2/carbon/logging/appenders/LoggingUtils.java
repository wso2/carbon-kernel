package org.wso2.carbon.logging.appenders;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import java.util.logging.LogRecord;

public class LoggingUtils {

    public static LoggingEvent getLogEvent(LogRecord record) {
        Priority level = getLogLevel(record.getLevel());
        return new LoggingEvent(record.getSourceClassName(), Logger.getLogger(record.getSourceClassName()), level, record.getMessage(), record.getThrown());
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
