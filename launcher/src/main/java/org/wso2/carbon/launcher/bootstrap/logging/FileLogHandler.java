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

import org.wso2.carbon.launcher.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Convenience class for configuring java.util.logging to append to
 * wso2carbon.log file.  This could be used for bootstrap logging
 * prior to start of the framework.
 *
 * @since 5.0.0
 */
public class FileLogHandler extends StreamHandler {
    private static final String WSO2CARBON_LOG_FILE_NAME = "wso2carbon.log";

    /**
     * Set a log formatter and append to a named file.
     *
     * @throws IOException
     */
    private FileLogHandler() throws IOException {
        this.setFormatter(new LoggingFormatter());
        openLogFile();
    }

    /**
     * Initialize logging handler for Carbon log file.
     *
     * @return CarbonLogFileHandler
     * @throws IOException
     */
    public static synchronized Handler getInstance() throws IOException {
        return new FileLogHandler();

    }

    /**
     * Open new output stream for the log file.
     *
     * @throws IOException
     */
    private void openLogFile() throws IOException {
        String logFilename = Paths.get(Utils.getRepositoryDirectory().toString(),
                "logs", WSO2CARBON_LOG_FILE_NAME).toString();
        File logfile = new File(logFilename);
        if (!logfile.getParentFile().exists()) {
            try {
                if (!logfile.getParentFile().mkdirs()) {
                    throw new IOException("Could not make directories " + logfile.getParentFile().getAbsolutePath());
                }
            } catch (SecurityException se) {
                throw new IOException(se.getMessage());
            }
        }
        FileOutputStream fout = new FileOutputStream(logfile, true);
        BufferedOutputStream out = new BufferedOutputStream(fout);
        setOutputStream(out);
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
        flush();
    }

}
