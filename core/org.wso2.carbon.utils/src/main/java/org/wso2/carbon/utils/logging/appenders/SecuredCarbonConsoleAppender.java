package org.wso2.carbon.utils.logging.appenders;

import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.utils.logging.LoggingUtils;

/**
 * CarbonConsoleAppender sanitized for Logging messages
 */
public class SecuredCarbonConsoleAppender extends CarbonConsoleAppender {

    @Override protected void subAppend(LoggingEvent loggingEvent) {

        LoggingEvent sanitizedLoggingEvent = LoggingUtils.getSanitizedLoggingEvent(loggingEvent);
        super.subAppend(sanitizedLoggingEvent);
    }
}
