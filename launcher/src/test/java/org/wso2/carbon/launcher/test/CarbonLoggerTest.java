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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * CarbonLoggerTest class which extends BaseTest class for launcher tests.
 *
 * @since 5.0.0
 */
public class CarbonLoggerTest extends BaseTest {
    private File logFile;
    Logger logger;
    CarbonLogHandler carbonLogHandler;

    public CarbonLoggerTest() {
        super();
    }

    @BeforeClass
    public void doBeforeEachTest() throws IOException {
        setupCarbonHome();
        logFile = new File(Paths.get(Utils.getCarbonHomeDirectory().toString(),
                "logs", Constants.CARBON_LOG_FILE_NAME).toString());
        logger = Logger.getLogger(CarbonLoggerTest.class.getName());
        carbonLogHandler = new CarbonLogHandler(logFile);
        carbonLogHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(carbonLogHandler);
    }

    @Test
    public void testCarbonLogAppend() throws IOException {
        String sampleMessage = "Sample message-01";
        String resultLog = "INFO {org.wso2.carbon.launcher.test.CarbonLoggerTest testCarbonLogAppend} -" +
                " Sample message-01";

        logger.info(sampleMessage);
        ArrayList<String> logRecords =
                getLogsFromTestResource(new FileInputStream(logFile));
        Assert.assertTrue(containsLogRecord(logRecords, resultLog));
    }

    @AfterTest
    public void cleanupLogfile() throws IOException {
        FileOutputStream writer = new FileOutputStream(logFile);
        writer.write((new String()).getBytes());
        writer.close();
    }


    /**
     * Implementation of java.util.logging.Handler that does simple appending
     * to a named file.  Should be able to use this for bootstrap logging
     * via java.util.logging prior to startup of pax logging.
     */
    public static class CarbonLogHandler extends StreamHandler {

        public CarbonLogHandler(File file) throws IOException {
            open(file, true);
        }

        private void open(File logfile, boolean append) throws IOException {
            if (!logfile.getParentFile().exists()) {
                try {
                    logfile.getParentFile().mkdirs();
                } catch (SecurityException se) {
                    throw new IOException(se.getMessage());
                }
            }
            FileOutputStream fout = new FileOutputStream(logfile, append);
            BufferedOutputStream out = new BufferedOutputStream(fout);
            setOutputStream(out);
        }

        public synchronized void publish(LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }
            super.publish(record);
            flush();
        }
    }
}
