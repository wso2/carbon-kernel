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


import java.util.Queue;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggingUtils {

    public static void pushLogRecord(String bridgeName,
                                     LoggingBridge loggingBridge,
                                     LogRecord record,
                                     Queue<LogRecord> logQueue ) {
        if (loggingBridge == null) { // get loggingBridge  from LoggingBridgeRegister
            LoggingBridge bridge = LoggingBridgeRegister.getLoggingBridge(bridgeName);
            if (bridge != null) {
                loggingBridge = bridge;
            }
        }

        if (loggingBridge == null) {  // not yet registered a LoggingBridge under BRIDGE_NAME
            logQueue.add(record);
        } else {
            // check old log records
            if (logQueue.isEmpty()) {
                loggingBridge.push(record);
            } else {
                // first write old log records
                for (LogRecord rec : logQueue) {
                    loggingBridge.push(rec);
                }
                loggingBridge.push(record);
            }
        }
    }

    public static LogRecord formatMessage(Formatter formatter, LogRecord record){
        record.setMessage(formatter.formatMessage(record));
        return record;
    }
}
