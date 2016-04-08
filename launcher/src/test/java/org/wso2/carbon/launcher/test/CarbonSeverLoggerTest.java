package org.wso2.carbon.launcher.test;

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

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.CarbonServer;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Test adding info logs in carbon.log file with log handlers in CarbonServer.class.
 *
 * @since 5.0.0
 */
public class CarbonSeverLoggerTest extends BaseTest {
    private Logger logger;
    private File logFile;

    public CarbonSeverLoggerTest() {
        super();
    }

    @BeforeClass
    public void doBeforeEachTest() throws IOException {
        setupCarbonHome();
        logFile = Paths.get(Utils.getCarbonHomeDirectory().toString(), "logs", Constants.CARBON_LOG_FILE_NAME).toFile();
        logger = Logger.getLogger(CarbonServer.class.getName());
        logger.addHandler(new CarbonLoggerTest.CarbonLogHandler(logFile));
    }

    @Test
    public void testCarbonLogAppendTestCase() throws IOException {
        String sampleMessage = "Sample message-test logging with class CarbonServer";
        String resultLog = "INFO {org.wso2.carbon.launcher.test.CarbonSeverLoggerTest testCarbonLogAppendTestCase} " +
                "- Sample message-test logging with class CarbonServer";

        logger.info(sampleMessage);
        ArrayList<String> logRecords =
                getLogsFromTestResource(new FileInputStream(logFile));
        //test if log records are added to carbon.log
        boolean isContainsInLogs = containsLogRecord(logRecords, resultLog);
        Assert.assertTrue(isContainsInLogs);
    }

    @AfterTest
    public void cleanupLogfile() throws IOException {
        FileOutputStream writer = new FileOutputStream(logFile);
        writer.write((new String()).getBytes());
        writer.close();
    }

}
