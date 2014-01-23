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

package org.apache.axis2.transport.tcp;

import org.apache.axis2.transport.base.AbstractTransportListenerEx;
import org.apache.axis2.AxisFault;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TCPTransportListener extends AbstractTransportListenerEx<TCPEndpoint> {

    private Map<TCPEndpoint, TCPServer> serverTable = new ConcurrentHashMap<TCPEndpoint, TCPServer>();

    protected void doInit() throws AxisFault {

    }

    protected TCPEndpoint createEndpoint() {
        return new TCPEndpoint();
    }

    protected void startEndpoint(TCPEndpoint endpoint) throws AxisFault {
        try {
            TCPServer server = new TCPServer(endpoint, workerPool);
            server.startServer();
            serverTable.put(endpoint, server);
        } catch (IOException e) {
            handleException("Error while starting the TCP endpoint", e);
        }
    }

    protected void stopEndpoint(TCPEndpoint endpoint) {
        try {
            TCPServer server = serverTable.get(endpoint);
            if (server != null) {
                server.stopServer();
            }
        } catch (IOException e) {
            log.error("Error while stopping the TCP endpoint", e);
        } finally {
            serverTable.remove(endpoint);
        }
    }
}
