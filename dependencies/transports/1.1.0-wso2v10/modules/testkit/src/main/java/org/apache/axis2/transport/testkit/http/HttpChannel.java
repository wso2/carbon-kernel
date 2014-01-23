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

import java.net.InetSocketAddress;
import java.util.UUID;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.channel.RequestResponseChannel;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.axis2.transport.testkit.util.tcpmon.Tunnel;

public class HttpChannel implements AsyncChannel, RequestResponseChannel {
    private @Transient String serviceName;
    private @Transient Tunnel tunnel;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(HttpTestEnvironment env) throws Exception {
        serviceName = "TestService-" + UUID.randomUUID();
        tunnel = new Tunnel(new InetSocketAddress("127.0.0.1", env.getServerPort()));
        tunnel.start();
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        tunnel.stop();
    }

    public String getServiceName() {
        return serviceName;
    }

    public EndpointReference getEndpointReference() throws Exception {
        return new EndpointReference("http://localhost:" + tunnel.getPort() + CONTEXT_PATH + "/" + serviceName);
    }
}