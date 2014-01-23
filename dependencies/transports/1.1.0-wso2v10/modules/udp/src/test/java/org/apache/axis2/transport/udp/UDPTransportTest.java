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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis2.transport.testkit.ManagedTestSuite;
import org.apache.axis2.transport.testkit.TransportTestSuiteBuilder;
import org.apache.axis2.transport.testkit.axis2.SimpleTransportDescriptionFactory;
import org.apache.axis2.transport.testkit.axis2.client.AxisAsyncTestClient;
import org.apache.axis2.transport.testkit.axis2.client.AxisRequestResponseTestClient;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisAsyncEndpoint;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisEchoEndpoint;

public class UDPTransportTest extends TestCase {
    public static TestSuite suite() throws Exception {
        ManagedTestSuite suite = new ManagedTestSuite(UDPTransportTest.class);
        
        // For the moment, we can only do SOAP 1.1 (see TODO item on UDPChannel)
        suite.addExclude("(|(messageType=SOAP12)(messageType=POX)(test=AsyncTextPlain)(test=AsyncBinary))");
        
        // Who would want to do SwA over UDP?
        suite.addExclude("(test=AsyncSwA)");

        // Obviously, UDP will not support large SOAP messages
        suite.addExclude("(test=AsyncSOAPLarge)");

        TransportTestSuiteBuilder builder = new TransportTestSuiteBuilder(suite);
        builder.addAsyncChannel(new UDPChannel());
        builder.addRequestResponseChannel(new UDPChannel());
        builder.addEnvironment(new SimpleTransportDescriptionFactory("udp", UDPListener.class, UDPSender.class));
        builder.addAxisAsyncTestClient(new AxisAsyncTestClient());
        builder.addAxisAsyncEndpoint(new AxisAsyncEndpoint());
        builder.addAxisRequestResponseTestClient(new AxisRequestResponseTestClient());
        builder.addEchoEndpoint(new AxisEchoEndpoint());
        builder.build();
        
        return suite;
    }
}
