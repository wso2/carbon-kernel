/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.launcher.test;


import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.bootstrapLogging.BootstrapLogger;
import org.wso2.carbon.launcher.test.bootstrapLoggingTest.LogHandler;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ConsoleLoggerTest {


    private Logger logger;
    LogHandler logHandler;

        @BeforeSuite
        public void doBeforeEachTest() {
            logger = BootstrapLogger.getBootstrapLogger();
            logHandler = new LogHandler();
            logger.addHandler(logHandler);
        }

        @Test
        public void testLog4JAppend() {
            String sampleMessage = "Sample message-01";
            LogRecord record = new LogRecord(Level.INFO, sampleMessage);
            logHandler.publish(record);
            Assert.assertEquals(logHandler.getLogList().get(0).getMessage(), sampleMessage);
        }

}
