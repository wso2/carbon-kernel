/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.axis2.transport.testkit.tests.ManagedTestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;
import org.apache.log4j.WriterAppender;

public class LogManager {
    public static final LogManager INSTANCE = new LogManager();
    
    private final File logDir;
    private File testCaseDir;
    private WriterAppender appender;
    private int sequence;
    private List<OutputStream> logs;
    
    private LogManager() {
        logDir = new File("target" + File.separator + "testkit-logs");
    }
    
    public void setTestCase(ManagedTestCase testCase) throws IOException {
        if (appender != null) {
            Logger.getRootLogger().removeAppender(appender);
            appender.close();
            appender = null;
        }
        if (logs != null) {
            for (OutputStream log : logs) {
                IOUtils.closeQuietly(log);
            }
            logs = null;
        }
        if (testCase == null) {
            testCaseDir = null;
        } else {
            File testSuiteDir = new File(logDir, testCase.getTestClass().getName());
            testCaseDir = new File(testSuiteDir, testCase.getId());
            logs = new LinkedList<OutputStream>();
            sequence = 1;
            appender = new WriterAppender(new TTCCLayout(), createLog("debug"));
            Logger.getRootLogger().addAppender(appender);
        }
    }
    
    public synchronized OutputStream createLog(String name) throws IOException {
        testCaseDir.mkdirs();
        OutputStream log = new FileOutputStream(new File(testCaseDir, StringUtils.leftPad(String.valueOf(sequence++), 2, '0') + "-" + name + ".log"));
        logs.add(log);
        return log;
    }
}
