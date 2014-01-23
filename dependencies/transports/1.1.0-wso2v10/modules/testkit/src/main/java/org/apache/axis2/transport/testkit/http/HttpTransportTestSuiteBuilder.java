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

import java.util.LinkedList;
import java.util.List;

import org.apache.axis2.transport.testkit.ManagedTestSuite;
import org.apache.axis2.transport.testkit.TransportTestSuiteBuilder;
import org.apache.axis2.transport.testkit.axis2.TransportDescriptionFactory;
import org.apache.axis2.transport.testkit.axis2.client.AxisAsyncTestClient;
import org.apache.axis2.transport.testkit.axis2.client.AxisRequestResponseTestClient;
import org.apache.axis2.transport.testkit.axis2.client.AxisTestClientConfigurator;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisAsyncEndpoint;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisEchoEndpoint;
import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.tests.misc.MinConcurrencyTest;

public class HttpTransportTestSuiteBuilder {
    private final ManagedTestSuite suite;
    private final TransportDescriptionFactory tdf;
    
    private final List<AxisTestClientConfigurator> axisTestClientConfigurators = new LinkedList<AxisTestClientConfigurator>();
    
    public HttpTransportTestSuiteBuilder(ManagedTestSuite suite,
            TransportDescriptionFactory tdf) {
        this.suite = suite;
        this.tdf = tdf;
    }
    
    public void addAxisTestClientConfigurator(AxisTestClientConfigurator configurator) {
        axisTestClientConfigurators.add(configurator);
    }
    
    public void build() {
        TransportTestSuiteBuilder builder = new TransportTestSuiteBuilder(suite);
        
        builder.addEnvironment(tdf);
        
        HttpChannel channel = new HttpChannel();
        
        builder.addAsyncChannel(channel);
        
        builder.addByteArrayAsyncTestClient(new JavaNetClient());
        if (axisTestClientConfigurators.isEmpty()) {
            builder.addAxisAsyncTestClient(new AxisAsyncTestClient());
        } else {
            for (AxisTestClientConfigurator configurator : axisTestClientConfigurators) {
                builder.addAxisAsyncTestClient(new AxisAsyncTestClient(), configurator);
            }
        }
        builder.addRESTAsyncTestClient(new JavaNetRESTClient());
        
        builder.addAxisAsyncEndpoint(new AxisAsyncEndpoint());
        builder.addByteArrayAsyncEndpoint(new JettyByteArrayAsyncEndpoint());
        builder.addRESTAsyncEndpoint(new JettyRESTAsyncEndpoint());
        
        builder.addRequestResponseChannel(channel);
        
        builder.addAxisRequestResponseTestClient(new AxisRequestResponseTestClient());
        
        builder.addEchoEndpoint(new AxisEchoEndpoint());
        builder.addEchoEndpoint(new JettyEchoEndpoint());
        
        builder.build();
        
        suite.addTest(new MinConcurrencyTest(new AsyncChannel[] { new HttpChannel(), new HttpChannel() }, 2, false, tdf));
    }
}
