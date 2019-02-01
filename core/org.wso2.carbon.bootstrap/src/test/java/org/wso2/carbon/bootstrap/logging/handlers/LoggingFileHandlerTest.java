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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests functionality of LoggingFileHandler class
 */
public class LoggingFileHandlerTest {
    List<LoggingFileHandler> loggingFileHandlers = new ArrayList<LoggingFileHandler>();

    @BeforeMethod
    public void setUp() throws Exception {
        String pattern = "[%1$tc] %4$s: %2$s - %5$s %6$s%n";
        loggingFileHandlers.add(new LoggingFileHandler());
        loggingFileHandlers.add(new LoggingFileHandler(pattern));
        loggingFileHandlers.add(new LoggingFileHandler(pattern, true));
        loggingFileHandlers.add(new LoggingFileHandler(pattern, 100, 100));
        loggingFileHandlers.add(new LoggingFileHandler(pattern, 100, 100, true));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        for (LoggingFileHandler loggingFileHandler : loggingFileHandlers) {
            loggingFileHandler.flush();
        }
    }

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if LoggingFileHandler publishes events if the " +
                    "level of log record is lower/higher than the Handler log level")
    public void testPublishLevel() throws IOException {
        for (LoggingFileHandler loggingFileHandler : loggingFileHandlers) {
            loggingFileHandler = new LoggingFileHandler();
            loggingFileHandler.setLevel(Level.INFO);

            LogRecord logRecord = new LogRecord(Level.INFO, "Info level log message");

            loggingFileHandler.publish(logRecord);

            loggingFileHandler.setLevel(Level.WARNING);
            assertFalse(loggingFileHandler.isLoggable(logRecord));
            loggingFileHandler.publish(logRecord);

            loggingFileHandler.setLevel(Level.FINE);
            assertTrue(loggingFileHandler.isLoggable(logRecord));
            loggingFileHandler.publish(logRecord);

            logRecord.setLevel(Level.OFF);
            loggingFileHandler.setLevel(Level.OFF);
            assertFalse(loggingFileHandler.isLoggable(logRecord));
            loggingFileHandler.publish(logRecord);
        }
    }

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if the formatter is an instance of XMLFormatter")
    public void testPublishFormatter() throws IOException {
        for (LoggingFileHandler loggingFileHandler : loggingFileHandlers) {
            assertTrue(loggingFileHandler.getFormatter() instanceof XMLFormatter);
        }
    }

    @Test(expectedExceptions = Exception.class,
            groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if publishing a null element throws an Exception")
    public void testPublishNull() throws IOException {
        for (LoggingFileHandler loggingFileHandler : loggingFileHandlers) {
            loggingFileHandler.setFormatter(new SimpleFormatter());

            loggingFileHandler.publish(null);
        }
    }

    @Test(groups = {"org.wso2.carbon.bootstrap.logging"},
            description = "Test if FileHandler allows to publish after Handler is closed")
    public void testPublishAfterClose() throws IOException {
        LogRecord logRecord = new LogRecord(Level.INFO, "Log message after close");

        for (LoggingFileHandler loggingFileHandler : loggingFileHandlers) {
            assertTrue(loggingFileHandler.isLoggable(logRecord));
            loggingFileHandler.close();
            assertFalse(loggingFileHandler.isLoggable(logRecord));

            loggingFileHandler.publish(logRecord);
        }
    }
}
