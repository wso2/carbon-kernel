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

package org.apache.axis2.engine.util.profiling;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.util.Utils;

public class Server implements TestConstants {
    protected AxisService service;

    public void start() throws Exception {
        UtilServer.start();
        service =
                Utils.createSimpleService(serviceName,
                                          Echo.class.getName(),
                                          operationName);
        UtilServer.deployService(service);
    }

    public void shutdown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }


    public static void main(String[] args) throws Exception {
        Server server = new Server();
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            server.shutdown();
        }
    }

}
