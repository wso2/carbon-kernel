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
package org.wso2.carbon.launcher.bootstrap.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Convenience class for configuring java.util.logging to append to
 * carbon console. This does simple appending
 * to a named file. Should be able to use this for bootstrap logging
 * via java.util.logging prior to startup of pax logging.
 *
 * @since 5.0.0
 */
public class ConsoleLogHandler extends ConsoleHandler {

    public ConsoleLogHandler() {
        super();
        // Setting the log4j.properties format.
        this.setFormatter(new LoggingFormatter());
    }

    public static Handler getInstance() {
        return new ConsoleLogHandler();
    }

    /**
     * Publish log records.
     *
     * @param record to publish
     */
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        super.publish(record);
    }

}
