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

import java.io.IOException;

import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.axis2.transport.base.datagram.AbstractDatagramTransportListener;
import org.apache.axis2.transport.base.datagram.DatagramDispatcherCallback;

/**
 * Transport listener for the UDP protocol.
 * Services accepting messages using this transport must be configured with the
 * following parameters:
 * <dl>
 *   <dt>transport.udp.port</dt>
 *   <dd>The UDP port to listen to (required).</dd>
 *   <dt>transport.udp.contentType</dt>
 *   <dd>The content type of the messages received (required). This setting
 *       is used to select the appropriate message builder.</dd>
 *   <dt>transport.udp.maxPacketSize</dt>
 *   <dd>The maximum packet size (optional; default 1024). Packets longer
 *       than the specified length will be truncated.</dd>
 * </dl>
 * 
 * @see org.apache.axis2.transport.udp
 */
public class UDPListener extends AbstractDatagramTransportListener<Endpoint>
        implements ManagementSupport {

    @Override
    protected IODispatcher createDispatcher(DatagramDispatcherCallback callback)
            throws IOException {
    	IODispatcher dispatcher = new IODispatcher(callback);
    	new Thread(dispatcher, getTransportName() + "-dispatcher").start();
        // Start a new thread for the I/O dispatcher
    	return dispatcher;
    }

    @Override
    protected Endpoint doCreateEndpoint() {
    	return new Endpoint();
    }
}
