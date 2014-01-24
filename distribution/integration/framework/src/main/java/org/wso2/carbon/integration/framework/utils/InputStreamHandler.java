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

package org.wso2.carbon.integration.framework.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * This class is used to consume the input streams and error streams of the
 * processes created by Runtime.exec() to avoid possible deadlock situations due
 * to unconsumed input streams.
 * An instance of this class will be created for input stream and output stream
 * of each process and it will log those streams to the standard output.
 */
public class InputStreamHandler implements Runnable {
    private String streamType;
    private InputStream inputStream;
    private StringBuilder stringBuilder;
    private static final String STREAM_TYPE_IN = "inputStream";
    private static final String STREAM_TYPE_ERROR = "errorStream";

    private static final Log log = LogFactory.getLog(InputStreamHandler.class);

    public InputStreamHandler(String name, InputStream is) {
        this.streamType = name;
        this.inputStream = is;
        this.stringBuilder = new StringBuilder();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while (true) {
                String s = bufferedReader.readLine();
                if (s == null) {
                    break;
                }
                if (STREAM_TYPE_IN.equals(streamType)) {
                    stringBuilder.append(s + "\n");
                    log.info(s);
                } else if (STREAM_TYPE_ERROR.equals(streamType)) {
                    stringBuilder.append(s + "\n");
                    log.error(s);
                }
            }
        } catch (Exception ex) {
            log.error("Problem reading the [" + streamType + "] due to: " + ex.getMessage(), ex);

        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error occured while closing the stream: " + e.getMessage(), e);
                }
            }

        }
    }

    public String getOutput() {
        return stringBuilder.toString();
    }

}
