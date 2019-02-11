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
package org.wso2.carbon.bootstrap.logging.handlers;

import org.testng.annotations.Test;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests functionality of LoggingConsoleHandler class
 */
public class LoggingConsoleHandlerTest {

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if LoggingConsoleHandler publishes events if the " +
                    "level of log record is lower/higher than the Handler log level")
    public void testPublishLevel() {
        Handler loggingConsoleHandler = new LoggingConsoleHandler();
        loggingConsoleHandler.setLevel(Level.INFO);
        LogRecord logRecord = new LogRecord(Level.INFO, "Info level log message");
        loggingConsoleHandler.publish(logRecord);

        loggingConsoleHandler.setLevel(Level.WARNING);
        assertFalse(loggingConsoleHandler.isLoggable(logRecord));
        loggingConsoleHandler.publish(logRecord);

        loggingConsoleHandler.setLevel(Level.FINE);
        assertTrue(loggingConsoleHandler.isLoggable(logRecord));
        loggingConsoleHandler.publish(logRecord);

        logRecord.setLevel(Level.OFF);
        loggingConsoleHandler.setLevel(Level.OFF);
        assertFalse(loggingConsoleHandler.isLoggable(logRecord));
        loggingConsoleHandler.publish(logRecord);
    }

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if the formatter is an instance of SimpleFormatter")
    public void testPublishFormatter() {
        Handler loggingConsoleHandler = new LoggingConsoleHandler();
        assertTrue(loggingConsoleHandler.getFormatter() instanceof SimpleFormatter);
    }

    @Test(expectedExceptions = Exception.class,
            groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if publishing a null element throws an Exception")
    public void testPublishNull() {
        Handler loggingConsoleHandler = new LoggingConsoleHandler();
        loggingConsoleHandler.setFormatter(new SimpleFormatter());
        loggingConsoleHandler.publish(null);
    }

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if ConsoleHandler allows to publish after Handler is closed")
    public void testPublishAfterClose() {
        Handler loggingConsoleHandler = new LoggingConsoleHandler();
        LogRecord logRecord = new LogRecord(Level.INFO, "Log message after close");

        assertTrue(loggingConsoleHandler.isLoggable(logRecord));
        loggingConsoleHandler.close();
        assertTrue(loggingConsoleHandler.isLoggable(logRecord));
        loggingConsoleHandler.publish(logRecord);
    }
}
