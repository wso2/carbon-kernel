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
package org.apache.axis2.transport.udp;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.testkit.axis2.AxisServiceConfigurator;
import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.channel.RequestResponseChannel;

// TODO: need to find a way to get the content type (which differs from one test case to another) at this level
public class UDPChannel implements AsyncChannel, RequestResponseChannel, AxisServiceConfigurator {
    private final int port = 3333; // TODO: should use port allocator here
    
    public EndpointReference getEndpointReference() throws Exception {
        return new EndpointReference("udp://127.0.0.1:" + port + "?contentType=text/xml");
    }

    public void setupService(AxisService service, boolean isClientSide) throws Exception {
        if (!isClientSide) {
            service.addParameter(UDPConstants.PORT_KEY, String.valueOf(port));
            service.addParameter(UDPConstants.CONTENT_TYPE_KEY, "text/xml");
        }
    }
}
