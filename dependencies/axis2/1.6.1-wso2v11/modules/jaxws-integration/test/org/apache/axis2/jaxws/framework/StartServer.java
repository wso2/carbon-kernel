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

package org.apache.axis2.jaxws.framework;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.utility.SimpleServer;

public class StartServer extends TestCase {

    public StartServer(String name) {
        super(name);
    }
    
    /*
     * users may pass in their own repositoryDir path and path to custom configuration file.
     * Passing 'null' for either param will use the default
     */
    public void testStartServer(String repositoryDir, String axis2xml) {
        SimpleServer server = new SimpleServer(repositoryDir, axis2xml);
        server.start();
    }
    
    public void testStartServer() {
        SimpleServer server = new SimpleServer();
        server.start();
    }
}
