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

package org.apache.axis2.integration;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.transport.local.LocalTransportReceiver;

/**
 * LocalTestCase is an extendable base class which provides common functionality
 * for building JUnit tests which exercise Axis2 using the (fast, in-process)
 * "local" transport.
 */
public class RPCLocalTestCase extends LocalTestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
        ///////////////////////////////////////////////////////////////////////
        // Set up raw message receivers for RPC based tests

        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_ONLY,
                                        new RPCInOnlyMessageReceiver());
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_OUT,
                                        new RPCMessageReceiver());
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
                                        new RPCMessageReceiver());
    }
    
    /**
     * Get a pre-initialized ServiceClient set up to talk to our local
     * server.  If you want to set options, call this and then use getOptions()
     * on the return. Clients created using this method have their To EPR
     * preset to include the address for the service+operation.
     *
     * @return a ServiceClient, pre-initialized to talk using our local sender
     * @throws AxisFault if there's a problem
     */
    protected RPCServiceClient getRPCClient(String serviceName, String operationName) throws AxisFault {
        String url = LocalTransportReceiver.CONFIG_CONTEXT.getServiceContextPath()+"/"+serviceName;

        Options opts = getOptions();
        opts.setTo(new EndpointReference(url));
        opts.setAction(operationName);
        RPCServiceClient client = new RPCServiceClient(clientCtx, null);
        client.setOptions(opts);
        return client;
    }
}
