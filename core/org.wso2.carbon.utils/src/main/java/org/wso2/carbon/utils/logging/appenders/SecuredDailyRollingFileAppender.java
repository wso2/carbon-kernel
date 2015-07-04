package org.wso2.carbon.utils.logging.appenders;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.utils.logging.LoggingUtils;

/**
 * DailyRollingFileAppender sanitized for Logging messages
 */
public class SecuredDailyRollingFileAppender extends DailyRollingFileAppender{

    @Override protected void subAppend(LoggingEvent event) {

        LoggingEvent sanitizedLoggingEvent = LoggingUtils.getSanitizedLoggingEvent(event);
        super.subAppend(sanitizedLoggingEvent);
    }
}
