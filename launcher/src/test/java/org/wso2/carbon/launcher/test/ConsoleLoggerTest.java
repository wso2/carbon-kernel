/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.launcher.test;


import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;
import org.wso2.carbon.launcher.test.logging.handlers.CommonsLogHandler;
import org.wso2.carbon.launcher.test.logging.handlers.JavaUtilLogHandler;
import org.wso2.carbon.launcher.test.logging.handlers.SLF4jLogHandler;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Console Logger Test class.
 *
 * @since 5.0.0
 */
public class ConsoleLoggerTest extends BaseTest {


    JavaUtilLogHandler javaUtilLogHandler;
    CommonsLogHandler commonsLogHandler;
    SLF4jLogHandler slf4jLogHandler;
    private Logger logger;

    public ConsoleLoggerTest() {
        super();
    }

    @BeforeSuite
    public void doBeforeEachTest() {
        javaUtilLogHandler = new JavaUtilLogHandler();
        commonsLogHandler = new CommonsLogHandler();
        slf4jLogHandler = new SLF4jLogHandler();
    }

    @Test
    public void testJavaUtilLogs() {
        setupCarbonHome();
        logger = BootstrapLogger.getCarbonLogger(ConsoleLoggerTest.class.getName());
        logger.addHandler(javaUtilLogHandler);
        String sampleMessage = "Sample javaUtilLog message-01";
        LogRecord record = new LogRecord(Level.INFO, sampleMessage);
        javaUtilLogHandler.publish(record);
        Assert.assertEquals(javaUtilLogHandler.getLogList().get(0).getMessage(), sampleMessage);
    }

    @Test
    public void testCommonsLogs() {
        String sampleMessage = "Sample commonsLog message-01";
        commonsLogHandler.info(sampleMessage);
        Assert.assertEquals(commonsLogHandler.getLogList().get(0), sampleMessage);
    }

    @Test
    public void testSLF4jLogs() {
        String sampleMessage = "Sample SLF4jLog message-01";
        slf4jLogHandler.info(sampleMessage);
        Assert.assertEquals(slf4jLogHandler.getLogList().get(0), sampleMessage);
    }
}
