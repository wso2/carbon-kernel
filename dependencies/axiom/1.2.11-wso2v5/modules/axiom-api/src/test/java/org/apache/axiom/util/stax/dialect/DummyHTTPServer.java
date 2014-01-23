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

package org.apache.axiom.util.stax.dialect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Dummy HTTP server used to determine if a parser attempts to access an external HTTP resource.
 */
public class DummyHTTPServer implements Runnable {
    private ServerSocket serverSocket;
    private volatile boolean requestReceived;
    
    public void run() {
        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException ex) {
                return;
            }
            requestReceived = true;
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(0);
        new Thread(this).start();
    }
    
    public void stop() throws IOException {
        serverSocket.close();
    }
    
    public String getBaseURL() {
        return "http://127.0.0.1:" + serverSocket.getLocalPort() + "/";
    }

    public boolean isRequestReceived() {
        return requestReceived;
    }
}
