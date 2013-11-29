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

import java.lang.SecurityException;
import java.lang.String;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

// TODO - move this class to platform level (org.wso2.carbon.logging.service bundle)
public class LogEventHandler extends Handler {
    private static String BRIDGE_NAME = "LOGEVENT";

    private static LoggingBridge loggingBridge;
    private static Queue<LogRecord> logQueue = new LinkedList<LogRecord>(); // TODO replace this with circular buffer implementation

    @Override
    public void publish(LogRecord record) {
        LoggingUtils.pushLogRecord(BRIDGE_NAME, LogEventHandler.loggingBridge, record, logQueue);

    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
