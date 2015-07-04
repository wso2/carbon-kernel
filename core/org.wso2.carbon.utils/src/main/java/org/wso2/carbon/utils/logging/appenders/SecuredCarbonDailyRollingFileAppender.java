package org.wso2.carbon.utils.logging.appenders;

import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.utils.logging.LoggingUtils;

/**
 * CarbonDailyRollingFileAppender sanitized for Logging messages
 */
public class SecuredCarbonDailyRollingFileAppender extends CarbonDailyRollingFileAppender{

    @Override protected void subAppend(LoggingEvent loggingEvent) {

        LoggingEvent sanitizedLoggingEvent = LoggingUtils.getSanitizedLoggingEvent(loggingEvent);
        super.subAppend(sanitizedLoggingEvent);
    }
}
