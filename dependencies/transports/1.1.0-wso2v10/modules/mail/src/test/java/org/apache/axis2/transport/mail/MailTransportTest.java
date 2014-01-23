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

package org.apache.axis2.transport.mail;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis2.transport.testkit.ManagedTestSuite;
import org.apache.axis2.transport.testkit.TransportTestSuiteBuilder;
import org.apache.axis2.transport.testkit.axis2.client.AxisAsyncTestClient;
import org.apache.axis2.transport.testkit.axis2.client.AxisRequestResponseTestClient;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisAsyncEndpoint;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisEchoEndpoint;
import org.apache.axis2.transport.testkit.tests.misc.MinConcurrencyTest;

public class MailTransportTest extends TestCase {
    public static TestSuite suite() throws Exception {
        ManagedTestSuite suite = new ManagedTestSuite(MailTransportTest.class);
        
        // SwA doesn't work with the mock client because attachments are sent with
        // "Content-Transfer-Encoding: binary" and mail servers don't like that.
        suite.addExclude("(&(test=AsyncSwA)(client=javamail))");
        // There seems to be a problem with Sun's IMAP client or GreenMail's IMAP server
        // in this particular case:
        suite.addExclude("(&(protocol=imap)(test=AsyncSwA))");
        // SYNAPSE-434
        suite.addExclude("(test=MinConcurrency)");
        
        TransportTestSuiteBuilder builder = new TransportTestSuiteBuilder(suite);
        
        builder.addEnvironment(new GreenMailTestEnvironment("pop3"), MailMessageContextValidator.INSTANCE);
        builder.addEnvironment(new GreenMailTestEnvironment("imap"), MailMessageContextValidator.INSTANCE);
        
        MailChannel channel = new MailChannel();
        
        builder.addAsyncChannel(channel);
        
        builder.addAxisAsyncTestClient(new AxisAsyncTestClient(), new MailAxisTestClientConfigurator(MailConstants.TRANSPORT_FORMAT_TEXT));
        builder.addAxisAsyncTestClient(new AxisAsyncTestClient(), new MailAxisTestClientConfigurator(MailConstants.TRANSPORT_FORMAT_MP));
        builder.addByteArrayAsyncTestClient(new MailAsyncClient(new FlatLayout()));
        builder.addByteArrayAsyncTestClient(new MailAsyncClient(new MultipartLayout()));
        
        builder.addAxisAsyncEndpoint(new AxisAsyncEndpoint());
        
        builder.addRequestResponseChannel(channel);
        
        // TODO: this doesn't work because of WSCOMMONS-544
//        builder.addAxisRequestResponseTestClient(new AxisRequestResponseTestClient(), new ResponseListenerConfigurator());
        builder.addByteArrayRequestResponseTestClient(new MailRequestResponseClient(new FlatLayout()));
        builder.addByteArrayRequestResponseTestClient(new MailRequestResponseClient(new MultipartLayout()));
        
        builder.addEchoEndpoint(new AxisEchoEndpoint());
        
        builder.build();
        
        suite.addTest(new MinConcurrencyTest(new MailChannel[] { new MailChannel(), new MailChannel() }, 2, true, new GreenMailTestEnvironment("pop3")));
        return suite;
    }
}
