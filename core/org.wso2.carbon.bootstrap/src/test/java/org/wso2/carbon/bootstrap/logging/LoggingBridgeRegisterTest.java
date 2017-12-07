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

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;

/**
 * Tests functionality of LoggingBridgeRegister class
 */
public class LoggingBridgeRegisterTest {
    private static LoggingBridge loggingBridge;

    @Test(expectedExceptions = ClassCastException.class,
            groups = {"org.wso2.carbon.utils.logging"},
            description = "Test if addAppender method throws Exception for " +
                    "adding appender that does not implement LoggingBridge interface")
    public void testAddAppender() {
        Appender appender = new ConsoleAppender();
        appender.setName("SIMPLE_CONSOLE");
        LoggingBridgeRegister.addAppender(appender.getName(), (LoggingBridge) appender);
    }

    @Test(groups = {"org.wso2.carbon.utils.logging"},
            description = "Test if adding appender with no name throws Exception")
    public void testAddAppenderWithNoName() {
        Appender appender = new ConsoleAppender();
        LoggingBridgeRegister.addAppender(appender.getName(), LoggingBridgeRegisterTest.loggingBridge);
    }

    @Test(groups = {"org.wso2.carbon.utils.logging"},
            description = "Test if a non-existent logging bridge returns null")
    public void testGetLoggingBridge() {
        assertNull(LoggingBridgeRegister.getLoggingBridge("SIMPLE_CONSOLE"));
    }
}
