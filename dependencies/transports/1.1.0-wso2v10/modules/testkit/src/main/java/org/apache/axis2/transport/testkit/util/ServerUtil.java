/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.util;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class with methods useful when working with test servers.
 */
public class ServerUtil {
    private static final Log log = LogFactory.getLog(ServerUtil.class);
    
    private ServerUtil() {}
    
    /**
     * Wait until the server listening on a given TCP port is ready to accept
     * connections.
     * 
     * @param port The TCP port the server listens on.
     * @throws Exception
     */
    public static void waitForServer(int port) throws Exception {
        InetAddress localhost = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
        int attempts = 0;
        Socket socket = null;
        while (socket == null) {
            attempts++;
            try {
                socket = new Socket(localhost, port);
            } catch (ConnectException ex) {
                if (attempts < 10) {
                    Thread.sleep(50);
                } else {
                    throw ex;
                }
            }
        }
        log.debug("Server on port " + port + " ready after " + attempts + " connection attempts");
        socket.close();
    }
}
