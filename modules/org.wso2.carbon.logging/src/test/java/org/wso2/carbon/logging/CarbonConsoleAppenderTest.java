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

package org.wso2.carbon.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.logging.appenders.TestAppender;

import java.util.logging.LogRecord;

public class CarbonConsoleAppenderTest {

    private TestAppender testAppender;

    @BeforeSuite
    public void doBeforeEachTest() {
        testAppender = new TestAppender();
        Logger.getRootLogger().addAppender(testAppender);
    }

    @Test
    public void testLog4JAppend() {
        String sampleMessage = "Sample message-01";
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", Logger.getRootLogger(),
                                              Level.INFO, sampleMessage, null);
        testAppender.append(event);
        Assert.assertEquals(testAppender.getLog().get(0).getMessage(), sampleMessage);
    }


    @Test(dependsOnMethods = {"testLog4JAppend"})
    public void testJavaUtilAppend() {
        String sampleMessage = "Sample message-02";
        LogRecord record = new LogRecord(java.util.logging.Level.INFO, sampleMessage);
        record.setSourceClassName("org.apache.log4j.Logger");
        testAppender.push(record);
        Assert.assertEquals(testAppender.getLog().get(1).getMessage(), sampleMessage);
    }

}
