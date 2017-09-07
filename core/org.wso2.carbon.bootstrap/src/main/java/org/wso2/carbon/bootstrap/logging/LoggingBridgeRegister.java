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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @deprecated Pax-logging has an in-built mechanism for bridging to Log4J2.
 */
@Deprecated
public class LoggingBridgeRegister {

    private static Map<String, LoggingBridge> loggingBridgeMap = new ConcurrentHashMap<String, LoggingBridge>();

    public static void addAppender(String name, LoggingBridge bridge) {
        if (name != null && bridge != null) {
            loggingBridgeMap.put(name, bridge);
            // flush logs belonging to this appender that were queued
            LoggingUtils.flushLogs(name, bridge);
        }
    }

    public static LoggingBridge getLoggingBridge(String name) {
        return loggingBridgeMap.get(name);
    }
}
