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
package org.wso2.carbon.bootstrap.logging;

import org.testng.annotations.Test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests functionality of LoggingUtils  class
 */
public class LoggingUtilsTest {
    private static LoggingBridge loggingBridge;

    @Test(expectedExceptions = NullPointerException.class,
            groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if pushing a log record without a bridge name throws a NullPointerException")
    public void testPushLogRecord() {
        LogRecord logRecord = new LogRecord(Level.INFO, "Console log message");
        LoggingUtils.pushLogRecord(null, LoggingUtilsTest.loggingBridge, logRecord);
    }

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if PushLogRecord method allows null logging bridges")
    public void testPushLogRecordUnregisteredBridge() {
        LogRecord logRecord = new LogRecord(Level.INFO, "Console log message");
        String bridgeName = "SIMPLE_CONSOLE";
        assertNull(LoggingUtilsTest.loggingBridge);
        LoggingUtils.pushLogRecord(bridgeName, LoggingUtilsTest.loggingBridge, logRecord);
    }

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if the Handler formatter is the default JUL SimpleFormatter")
    public void testFormatMessage() {
        LogRecord logRecord = new LogRecord(Level.INFO, "Formatted log message");
        ConsoleHandler consoleHandler = new ConsoleHandler();
        Formatter formatter = consoleHandler.getFormatter();
        Formatter simpleFormatter = new SimpleFormatter();
        assertEquals(LoggingUtils.formatMessage(formatter, logRecord),
                LoggingUtils.formatMessage(simpleFormatter, logRecord));
    }

    @Test(expectedExceptions = NullPointerException.class,
            groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if flushing a queue throws a NullPointerException " +
                    "for unspecified bridge")
    public void testFlushLogs() {
        LoggingUtils.flushLogs(null, LoggingUtilsTest.loggingBridge);
    }
}
