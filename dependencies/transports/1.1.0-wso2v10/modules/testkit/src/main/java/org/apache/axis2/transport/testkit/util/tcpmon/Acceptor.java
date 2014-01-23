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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class Acceptor implements Runnable {
    private static final Log log = LogFactory.getLog(Acceptor.class);
    
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final InetSocketAddress target;
    
    public Acceptor(ServerSocket serverSocket, ExecutorService executorService, InetSocketAddress target) {
        this.serverSocket = serverSocket;
        this.executorService = executorService;
        this.target = target;
    }

    public void run() {
        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException ex) {
                break;
            }
            try {
                Socket targetSocket = new Socket(target.getAddress(), target.getPort());
                executorService.execute(new Relay(socket, targetSocket, false));
                executorService.execute(new Relay(targetSocket, socket, true));
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

}
