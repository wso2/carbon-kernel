/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.log.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class LogServerService {

    private static final Logger log = LoggerFactory.getLogger(LogServerService.class);
    private static final String MOUNT_PATH = "/var/carbon/deployment/logs/carbon.log";
    private FileWriter fileWriter = null;

    @POST
    @Path("/")
    public void post(String logMessage) throws IOException {
        System.out.println(logMessage);
        writeToLogFile(logMessage);
    }

    private void writeToLogFile(String logMessage) throws IOException {
        File file = new File(MOUNT_PATH);
        if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
            try {
                fileWriter = new FileWriter(file, true);
                fileWriter.write(logMessage);
            } catch (FileNotFoundException e) {
                String errorMessage = "Error while reading file :" + file;
                log.error(errorMessage);
                throw new FileNotFoundException(errorMessage);
            } catch (IOException e) {
                throw new IOException("Unable to write a file");
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
        }
    }

}
