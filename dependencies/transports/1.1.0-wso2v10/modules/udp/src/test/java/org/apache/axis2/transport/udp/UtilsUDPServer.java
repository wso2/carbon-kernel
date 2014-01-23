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

import java.util.LinkedList;
import java.util.List;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.UtilsTransportServer;

public class UtilsUDPServer extends UtilsTransportServer {
    public UtilsUDPServer() throws Exception {
        TransportInDescription trpInDesc = new TransportInDescription("udp");
        trpInDesc.setReceiver(new UDPListener());
        TransportOutDescription trpOutDesc = new TransportOutDescription("udp");
        trpOutDesc.setSender(new UDPSender());
        addTransport(trpInDesc, trpOutDesc);
        enableAddressing();
        
        List<Parameter> params = new LinkedList<Parameter>();
        params.add(new Parameter(UDPConstants.PORT_KEY, 3333));
        params.add(new Parameter(UDPConstants.CONTENT_TYPE_KEY, "text/xml+soap"));
        deployEchoService("EchoService", params);
    }
}
