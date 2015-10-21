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
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Convenience class for configuring java.util.logging to append to
 * wso2carbon.log file.  This could be used for bootstrap logging
 * prior to start of the framework.
 */
public class CarbonLogger {
    private static final String CARBON_BOOTSTRAP_LOG = "wso2carbon.log";

    public static synchronized Handler getDefaultHandler() throws IOException {
        String logFilename = Utils.getRepositoryDir() + File.separator + "logs" + File.separator + CARBON_BOOTSTRAP_LOG;
        return new CarbonLogger.SimpleFileHandler(new File(logFilename));

    }

    /**
     * Implementation of java.util.logging.Handler that does simple appending
     * to a named file.  Should be able to use this for bootstrap logging
     * via java.util.logging prior to startup of pax logging.
     */
    private static class SimpleFileHandler extends StreamHandler {

        private SimpleFileHandler(File file) throws IOException {
            open(file, true);
        }

        private void open(File logfile, boolean append) throws IOException {
            if (!logfile.getParentFile().exists()) {
                try {
                    if (!logfile.getParentFile().mkdirs()) {
                        throw new IOException("Could not make directories " +
                                logfile.getParentFile().getAbsolutePath());
                    }
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
