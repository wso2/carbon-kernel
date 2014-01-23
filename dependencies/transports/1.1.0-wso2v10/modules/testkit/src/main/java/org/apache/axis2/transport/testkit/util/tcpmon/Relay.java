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

package org.apache.axis2.transport.testkit.util.tcpmon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.axis2.transport.base.datagram.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class Relay implements Runnable {
    private static final Log log = LogFactory.getLog(Relay.class);
    
    private final Socket inSocket;
    private final InputStream in;
    private final OutputStream out;
    private final String connectionSpec;
    
    public Relay(Socket inSocket, Socket outSocket, boolean isResponse) throws IOException {
        this.inSocket = inSocket;
        this.in = inSocket.getInputStream();
        this.out = outSocket.getOutputStream();
        if (isResponse) {
            connectionSpec = outSocket.getRemoteSocketAddress() + " <- " + inSocket.getRemoteSocketAddress();
        } else {
            connectionSpec = inSocket.getRemoteSocketAddress() + " -> " + outSocket.getRemoteSocketAddress();
        }
    }
    
    public void run() {
        byte buf[] = new byte[4096];
        try {
            int n;
            while ((n = in.read(buf)) > 0) {
                StringBuilder dump = new StringBuilder(connectionSpec);
                dump.append('\n');
                Utils.hexDump(dump, buf, n);
                log.debug(dump);
                out.write(buf, 0, n);
                out.flush();
            }
        } catch (IOException ex) {
            if (!inSocket.isClosed()) {
                log.error(ex);
            }
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        log.debug(connectionSpec + ": closed");
    }
}
