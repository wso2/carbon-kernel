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
package org.wso2.carbon.bootstrap.logging;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @deprecated Queuing and flushing logs is done by Log4J2 appenders.
 */
@Deprecated
public class LoggingUtils {

    // This map holds the queue instances that stores log events for different logging
    // bridges when they are not yet registered with the LoggingBridgeRegister.
    private static final Map<String, Queue<LogRecord>> loggingBridgeQueues =
            new ConcurrentHashMap<String, Queue<LogRecord>>();
    private static Boolean isRegistrationCompleted = false;

    public static void pushLogRecord(String bridgeName,
                                     LoggingBridge loggingBridge,
                                     LogRecord record) {
        if (loggingBridge == null) {// get loggingBridge from LoggingBridgeRegister
            LoggingBridge bridge = LoggingBridgeRegister.getLoggingBridge(bridgeName);
            if (bridge != null) {
                loggingBridge = bridge;
            }
        }
        if (loggingBridge == null) {// LoggingBridge is not yet registered hence queuing
            // the log records
            if (!isRegistrationCompleted) {
                Queue<LogRecord> logQueue = loggingBridgeQueues.get(bridgeName);
                if (logQueue == null) {
                    synchronized (loggingBridgeQueues) {
                        logQueue = loggingBridgeQueues.get(bridgeName);
                        if (logQueue == null) {
                            logQueue = new LinkedList<LogRecord>();
                            loggingBridgeQueues.put(bridgeName, logQueue);
                        }
                    }
                }
                logQueue.add(record);
            }
        } else {
            loggingBridge.push(record);
        }
    }

    public static LogRecord formatMessage(Formatter formatter, LogRecord record) {
        record.setMessage(formatter.formatMessage(record));
        record.setSourceClassName(record.getSourceClassName());
        return record;
    }

    public static void flushLogs(String bridgeName, LoggingBridge loggingBridge) {
        Queue<LogRecord> logQueue = loggingBridgeQueues.get(bridgeName);
        if (logQueue != null && !logQueue.isEmpty()) {
            // first write old log records
            for (LogRecord rec : logQueue) {
                loggingBridge.push(rec);
            }
            // clear old log records from queue
            logQueue.clear();
        }
    }

    public static void clear() {
        isRegistrationCompleted = true;
        loggingBridgeQueues.clear();
    }
}