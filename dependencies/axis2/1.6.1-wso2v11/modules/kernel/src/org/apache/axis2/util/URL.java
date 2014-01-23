/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.util;

public class URL {
    private int port = -1;
    private String fileName;
    private String host;
    private String protocol;

    public URL(String url) {
        int start = 0;
        int end = 0;

        end = url.indexOf("://");

        if (end > 0) {
            protocol = url.substring(0, end);
            start = end + 3;
        }

        end = url.indexOf('/', start);

        if (end > 0) {
            String hostAndPort = url.substring(start, end);

            fileName = url.substring(end);

            int index = hostAndPort.indexOf(':');

            if (index > 0) {
                host = hostAndPort.substring(0, index);
                port = Integer.parseInt(hostAndPort.substring(index + 1));
            } else {
                host = hostAndPort;
            }
        } else {
            host = url;
        }
    }

    /**
     * @return Returns String.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return Returns String.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return Returns int.
     */
    public int getPort() {
        return port;
    }

    /**
     * @return Returns String.
     */
    public String getProtocol() {
        return protocol;
    }
}
