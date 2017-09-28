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
import org.wso2.carbon.bootstrap.logging.LoggingBridge;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * Tests functionality of LogEventHandler class
 */
public class LogEventHandlerTest {
    private static LoggingBridge loggingBridge;

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if LoggingConsoleHandler publishes events if the " +
                    "level of log record is lower/higher than the Handler log level")
    public void testPublishLevel() {
        Handler logEventHandler = new LogEventHandler();
        logEventHandler.setLevel(Level.INFO);
        LogRecord logRecord = new LogRecord(Level.INFO, "Info level log message");
        logEventHandler.setLevel(Level.WARNING);
        assertFalse(logEventHandler.isLoggable(logRecord));

        logEventHandler.setLevel(Level.FINE);
        assertTrue(logEventHandler.isLoggable(logRecord));

        logRecord.setLevel(Level.OFF);
        logEventHandler.setLevel(Level.OFF);
        assertFalse(logEventHandler.isLoggable(logRecord));
    }

    @Test(expectedExceptions = Exception.class,
            groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if publishing an element throws an Exception")
    public void testPublishNull() {
        LogRecord logRecord = new LogRecord(Level.INFO, "Log message after close");
        Handler logEventHandler = new LogEventHandler();
        logEventHandler.publish(logRecord);
    }

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if ConsoleHandler allows to publish after Handler is closed")
    public void testPublishAfterClose() {
        Handler logEventHandler = new LogEventHandler();
        LogRecord logRecord = new LogRecord(Level.INFO, "Log message after close");

        assertTrue(logEventHandler.isLoggable(logRecord));
        logEventHandler.close();
        assertTrue(logEventHandler.isLoggable(logRecord));
    }
}
