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

import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;

public class TCPServer implements Runnable {
    
    private TCPEndpoint endpoint;
    private ServerSocket serverSocket;
    private WorkerPool workerPool;
    private boolean started = false;

    private static final Log log = LogFactory.getLog(TCPServer.class);

    public TCPServer(TCPEndpoint endpoint, WorkerPool workerPool) {
        this.endpoint = endpoint;
        this.workerPool = workerPool;
    }

    public void run() {
        while (started) {
            Socket socket = null;

            try {
                socket = serverSocket.accept();
            } catch (java.io.InterruptedIOException ignored) {

            } catch (Exception e) {
                log.debug(e);
                break;
            }

            if (socket != null) {
                workerPool.execute(new TCPWorker(endpoint, socket));
            }
        }
    }

    public void startServer() throws IOException {
        if (serverSocket == null) {
            if (endpoint.getHost() != null) {
                InetAddress address = InetAddress.getByName(endpoint.getHost());
                serverSocket = new ServerSocket(endpoint.getPort(), endpoint.getBacklog(), address);
            } else {
                serverSocket = new ServerSocket(endpoint.getPort(), endpoint.getBacklog());
            }
        }
        started = true;
        endpoint.getListener().getConfigurationContext().getThreadPool().execute(this);
        log.info("TCP server started on port : " + endpoint.getPort());
    }

    public void stopServer() throws IOException {
        started = false;
        serverSocket.close();
        serverSocket = null;
        log.info("TCP server stopped on port : " + endpoint.getPort());
    }
}
