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

import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.axis2.transport.testkit.util.PortAllocator;

public class HttpTestEnvironment {
    public static final HttpTestEnvironment INSTANCE = new HttpTestEnvironment();
    
    private @Transient PortAllocator portAllocator;
    private int serverPort;

    private HttpTestEnvironment() {}
    
    @Setup @SuppressWarnings("unused")
    private void setUp(PortAllocator portAllocator) throws Exception {
        this.portAllocator = portAllocator;
        serverPort = portAllocator.allocatePort();
    }

    public int getServerPort() {
        return serverPort;
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        portAllocator.releasePort(serverPort);
    }
}
