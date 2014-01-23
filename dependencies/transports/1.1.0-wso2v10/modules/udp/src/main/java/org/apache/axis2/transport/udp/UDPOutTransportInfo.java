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

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.datagram.DatagramOutTransportInfo;

/**
 * Holder of information to send an outgoing message to a UDP destination.
 */
public class UDPOutTransportInfo extends DatagramOutTransportInfo {
    private InetSocketAddress address;

    public UDPOutTransportInfo(String eprString) throws AxisFault {
        URI epr;
        try {
            epr = new URI(eprString);
        } catch (URISyntaxException ex) {
            throw new AxisFault("Invalid endpoint reference", ex);
        }
        
        // TODO: quick&dirty; need to do this in a proper way
        String params = epr.getQuery();
        if (!params.startsWith("contentType=")) {
            throw new AxisFault("Invalid endpoint reference: no content type");
        }
        address = new InetSocketAddress(epr.getHost(), epr.getPort());
        setContentType(params.substring(12));
    }
    
    public UDPOutTransportInfo(InetSocketAddress address) {
        this.address = address;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }
}
