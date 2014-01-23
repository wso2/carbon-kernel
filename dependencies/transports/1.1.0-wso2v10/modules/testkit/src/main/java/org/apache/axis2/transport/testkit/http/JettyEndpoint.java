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

package org.apache.axis2.transport.testkit.http;

import java.io.IOException;

import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

public abstract class JettyEndpoint {
    private @Transient JettyServer server;
    private @Transient HttpHandler handler;

    @Setup @SuppressWarnings({ "unused", "serial" })
    private void setUp(JettyServer server, HttpChannel channel) throws Exception {
        this.server = server;
        final String path = "/" + channel.getServiceName();
        handler = new AbstractHttpHandler() {
            public void handle(String pathInContext, String pathParams,
                    HttpRequest request, HttpResponse response) throws HttpException,
                    IOException {
                
                if (pathInContext.equals(path)) {
                    JettyEndpoint.this.handle(pathParams, request, response);
                    request.setHandled(true);
                }
            }
        };
        server.getContext().addHandler(handler);
        handler.start();
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        handler.stop();
        server.getContext().removeHandler(handler);
    }
    
    protected abstract void handle(String pathParams, HttpRequest request, HttpResponse response)
            throws HttpException, IOException;
    
}
