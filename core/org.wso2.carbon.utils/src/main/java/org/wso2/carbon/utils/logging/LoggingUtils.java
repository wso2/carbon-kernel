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
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.xml.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @deprecated Tenant specific information is taken form Log4J2 Converters.
 */
@Deprecated
public class LoggingUtils {

    private static final String REPLACEMENT_STRING = "*****";
    private static final String DEFAULT_MASKING_PATTERNS_FILE_NAME = "wso2-log-masking.properties";
    private static final Logger log = Logger.getLogger(LoggingUtils.class);

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

    @Deprecated
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
                loggingEvent.getMessage(),
                throwable);
        tenantAwareLoggingEvent.setTenantId(Integer.toString(tenantId));
        tenantAwareLoggingEvent.setServiceName(serviceName);
        return tenantAwareLoggingEvent;
    }

    /**
     * Returns a TenantAwareLoggingEvent that wraps the LoggingEvent with tenant specific tenantId
     * and serviceName
     *
     * @param maskingPatterns
     *         - The masking patterns which the log message should be masked.
     * @param loggingEvent
     *         -  The LoggingEvent with the log content
     * @param tenantId
     *         - tenant Id of the tenant which triggered log event
     * @param serviceName
     *         - service name of the current log event
     * @return a TenantAwareLoggingEvent
     */
    public static TenantAwareLoggingEvent getTenantAwareLogEvent(List<Pattern> maskingPatterns, LoggingEvent
            loggingEvent, int tenantId, String serviceName) {
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

        String logMessage = getLogMessage(loggingEvent);

        // Check whether there are any masking patterns defined.
        if (logMessage != null && maskingPatterns != null && maskingPatterns.size() > 0) {

            for (Pattern pattern : maskingPatterns) {
                Matcher matcher = pattern.matcher(logMessage);
                StringBuffer stringBuffer = new StringBuffer();

                while (matcher.find()) {
                    matcher.appendReplacement(stringBuffer, REPLACEMENT_STRING);
                }
                matcher.appendTail(stringBuffer);
                logMessage = stringBuffer.toString();
            }
        }

        tenantAwareLoggingEvent = new TenantAwareLoggingEvent(loggingEvent.fqnOfCategoryClass,
                                                              logger, loggingEvent.timeStamp,
                                                              loggingEvent.getLevel(),
                                                              logMessage,
                                                              throwable);
        tenantAwareLoggingEvent.setTenantId(Integer.toString(tenantId));
        tenantAwareLoggingEvent.setServiceName(serviceName);
        return tenantAwareLoggingEvent;
    }

    private static String getLogMessage(LoggingEvent loggingEvent) {

        if (loggingEvent.getMessage() == null) {
            return null;
        } else {
            return loggingEvent.getMessage().toString();
        }
    }

    /**
     * Method to get the masking patterns (regex) from the properties file.
     * @param maskingFilePath : The absolute path to the masking properties file.
     */
    public static List<Pattern> loadMaskingPatterns(String maskingFilePath) {

        String defaultFile = CarbonUtils.getCarbonConfigDirPath() + File.separatorChar +
                DEFAULT_MASKING_PATTERNS_FILE_NAME;
        List<Pattern> maskingPatterns = new ArrayList<>();
        Properties properties = new Properties();
        InputStream propsStream = null;

        try {
            if (!StringUtils.isEmpty(maskingFilePath)) {
                //Check whether the configured properties file is not null and the file is present.
                if (!(Files.exists(Paths.get(maskingFilePath)) && !Files.isDirectory(Paths.get(maskingFilePath)))) {
                    log.error("Could not load the masking patterns from the provided file : "
                            + maskingFilePath);
                    return maskingPatterns;
                }
                propsStream = new FileInputStream(maskingFilePath);
            } else {
                // If the masking file is not configured, load the configs from the default file.
                if (Files.exists(Paths.get(defaultFile))) {
                    propsStream = new FileInputStream(defaultFile);
                } else {
                    return maskingPatterns;
                }
            }
            properties.load(propsStream);

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                Pattern maskingPattern = Pattern.compile((String) entry.getValue());
                maskingPatterns.add(maskingPattern);
            }
        } catch (IOException e) {
            // If the masking patterns cannot be loaded print an error message.
            log.error("Error loading the masking patterns, due to : " + e.getMessage());
        } finally {
            if (propsStream != null) {
                try {
                    propsStream.close();
                } catch (IOException e) {
                    // ignore this exception.
                }
            }
        }
        return maskingPatterns;
    }

    /**
     * Return the <code>org.apache.log4j.Level</code> from the given
     * <code>java.util.logging.Level</code>
     *
     * @param level
     *         - the <code>java.util.logging.Level</code>
     * @return - an <code>org.apache.log4j.Level</code>
     */
    public static Level getLogLevel(java.util.logging.Level level) {
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
