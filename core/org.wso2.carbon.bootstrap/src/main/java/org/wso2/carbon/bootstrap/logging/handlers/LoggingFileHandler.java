/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bootstrap.logging.handlers;


import org.wso2.carbon.bootstrap.logging.LoggingBridge;
import org.wso2.carbon.bootstrap.logging.LoggingUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

public class LoggingFileHandler extends FileHandler{

    private static String BRIDGE_NAME = "CARBON_LOGFILE";

    private static LoggingBridge loggingBridge;
    private static Queue<LogRecord> logQueue = new LinkedList<LogRecord>(); // TODO replace this with circular buffer implementation

    public LoggingFileHandler() throws IOException, SecurityException {
    }

    public LoggingFileHandler(String pattern) throws IOException, SecurityException {
        super(pattern);
    }

    public LoggingFileHandler(String pattern, boolean append) throws IOException, SecurityException {
        super(pattern, append);
    }

    public LoggingFileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
        super(pattern, limit, count);
    }

    public LoggingFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
        super(pattern, limit, count, append);
    }

    @Override
    public synchronized void publish(LogRecord record) {
        LoggingUtils.pushLogRecord(BRIDGE_NAME, LoggingFileHandler.loggingBridge, record, logQueue);
    }

}
