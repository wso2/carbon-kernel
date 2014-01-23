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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;

public class PortAllocator {
    private static final int BASE_PORT = 9000;
    private static final int BASE_PORT_INCREMENT = 10;
    
    public static final PortAllocator INSTANCE = new PortAllocator();
    
    static class PortRange {
        private final ServerSocket serverSocket;
        private final int basePort;
        private final boolean[] allocated = new boolean[BASE_PORT_INCREMENT-1];
        
        PortRange(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
            basePort = serverSocket.getLocalPort();
        }
        
        /**
         * Allocate a port in this range.
         * 
         * @return the allocated port, or -1 if there are no more available ports
         */
        int allocatePort() {
            for (int i=0; i<BASE_PORT_INCREMENT-1; i++) {
                if (!allocated[i]) {
                    allocated[i] = true;
                    return basePort + i + 1;
                }
            }
            return -1;
        }
        
        /**
         * Determine if the given port belongs to the range.
         * 
         * @return <code>true</code> if the port belongs to the range; <code>false</code> otherwise
         */
        boolean hasPort(int port) {
            return port > basePort && port < basePort + BASE_PORT_INCREMENT;
        }
        
        /**
         * Release the given port.
         * 
         * @param port the number of the port to release
         */
        void releasePort(int port) {
            int i = port - basePort - 1;
            if (!allocated[i]) {
                throw new IllegalStateException("Port is not allocated");
            }
            allocated[i] = false;
        }
        
        void release() {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
    
    private int basePort;
    private List<PortRange> ranges;
    
    private PortAllocator() {
    }
    
    @Setup @SuppressWarnings("unused")
    private void setUp() {
        basePort = BASE_PORT;
        ranges = new ArrayList<PortRange>();
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        for (PortRange range : ranges) {
            range.release();
        }
        ranges = null;
    }
    
    public synchronized int allocatePort() {
        for (PortRange range : ranges) {
            int port = range.allocatePort();
            if (port != -1) {
                return port;
            }
        }
        while (true) {
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(basePort);
            } catch (IOException ex) {
                serverSocket = null;
            }
            basePort += BASE_PORT_INCREMENT;
            if (serverSocket != null) {
                PortRange range = new PortRange(serverSocket);
                ranges.add(range);
                return range.allocatePort();
            }
        }
    }
    
    public synchronized void releasePort(int port) {
        for (PortRange range : ranges) {
            if (range.hasPort(port)) {
                range.releasePort(port);
                return;
            }
        }
        throw new IllegalArgumentException("Invalid port number");
    }
}
