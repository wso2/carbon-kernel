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

package org.apache.axis2.transport.jms;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.testkit.ManagedTestSuite;
import org.apache.axis2.transport.testkit.TransportTestSuiteBuilder;
import org.apache.axis2.transport.testkit.axis2.client.AxisAsyncTestClient;
import org.apache.axis2.transport.testkit.axis2.client.AxisRequestResponseTestClient;
import org.apache.axis2.transport.testkit.axis2.client.AxisTestClientConfigurator;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisAsyncEndpoint;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisEchoEndpoint;
import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.tests.misc.MinConcurrencyTest;

public class JMSTransportTest extends TestCase {
    public static TestSuite suite() throws Exception {
        ManagedTestSuite suite = new ManagedTestSuite(JMSTransportTest.class);
        
        // SwA doesn't make sense with text messages
        suite.addExclude("(&(test=AsyncSwA)(client=jms)(jmsType=text))");
        
        // Don't execute all possible test combinations:
        //  * Use a single setup to execute tests with all message types.
        //  * Only use a small set of message types for the other setups.
        suite.addExclude("(!(|(&(broker=qpid)(singleCF=false)(cfOnSender=false)(!(|(destType=topic)(replyDestType=topic))))" +
        		             "(&(test=AsyncXML)(messageType=SOAP11)(data=ASCII))" +
        		             "(&(test=EchoXML)(messageType=POX)(data=ASCII))" +
        		             "(test=MinConcurrency)))");
        
        // SYNAPSE-436:
        suite.addExclude("(&(test=EchoXML)(replyDestType=topic)(endpoint=axis))");

        // Example to run a few use cases.. please leave these commented out - asankha
        //suite.addExclude("(|(test=AsyncXML)(test=MinConcurrency)(destType=topic)(broker=qpid)(destType=topic)(replyDestType=topic)(client=jms)(endpoint=mock)(cfOnSender=true))");
        //suite.addExclude("(|(test=EchoXML)(destType=queue)(broker=qpid)(cfOnSender=true)(singleCF=false)(destType=queue)(client=jms)(endpoint=mock))");
        //suite.addExclude("(|(test=EchoXML)(test=AsyncXML)(test=AsyncSwA)(test=AsyncTextPlain)(test=AsyncBinary)(test=AsyncSOAPLarge)(broker=qpid))");

        TransportTestSuiteBuilder builder = new TransportTestSuiteBuilder(suite);

        JMSTestEnvironment[] environments = new JMSTestEnvironment[] { new QpidTestEnvironment(), new ActiveMQTestEnvironment() };
        for (boolean singleCF : new boolean[] { false, true }) {
            for (boolean cfOnSender : new boolean[] { false, true }) {
                for (JMSTestEnvironment env : environments) {
                    builder.addEnvironment(env, new JMSTransportDescriptionFactory(singleCF, cfOnSender, 1));
                }
            }
        }
        
        builder.addAsyncChannel(new JMSAsyncChannel(JMSConstants.DESTINATION_TYPE_QUEUE, ContentTypeMode.TRANSPORT));
        builder.addAsyncChannel(new JMSAsyncChannel(JMSConstants.DESTINATION_TYPE_TOPIC, ContentTypeMode.TRANSPORT));
        
        builder.addAxisAsyncTestClient(new AxisAsyncTestClient());
        builder.addAxisAsyncTestClient(new AxisAsyncTestClient(), new JMSAxisTestClientConfigurator(JMSConstants.JMS_BYTE_MESSAGE));
        builder.addAxisAsyncTestClient(new AxisAsyncTestClient(), new JMSAxisTestClientConfigurator(JMSConstants.JMS_TEXT_MESSAGE));
        builder.addByteArrayAsyncTestClient(new JMSAsyncClient<byte[]>(JMSBytesMessageFactory.INSTANCE));
        builder.addStringAsyncTestClient(new JMSAsyncClient<String>(JMSTextMessageFactory.INSTANCE));
        
        builder.addAxisAsyncEndpoint(new AxisAsyncEndpoint());
        
        builder.addRequestResponseChannel(new JMSRequestResponseChannel(JMSConstants.DESTINATION_TYPE_QUEUE, JMSConstants.DESTINATION_TYPE_QUEUE, ContentTypeMode.TRANSPORT));
        
        AxisTestClientConfigurator timeoutConfigurator = new AxisTestClientConfigurator() {
            public void setupRequestMessageContext(MessageContext msgContext) throws AxisFault {
                msgContext.setProperty(JMSConstants.JMS_WAIT_REPLY, "5000");
            }
        };
        
        builder.addAxisRequestResponseTestClient(new AxisRequestResponseTestClient(), timeoutConfigurator);
        builder.addStringRequestResponseTestClient(new JMSRequestResponseClient<String>(JMSTextMessageFactory.INSTANCE));
        
        builder.addEchoEndpoint(new MockEchoEndpoint());
        builder.addEchoEndpoint(new AxisEchoEndpoint());

        for (JMSTestEnvironment env : new JMSTestEnvironment[] { new QpidTestEnvironment(), new ActiveMQTestEnvironment() }) {
            suite.addTest(new MinConcurrencyTest(new AsyncChannel[] {
                    new JMSAsyncChannel("endpoint1", JMSConstants.DESTINATION_TYPE_QUEUE, ContentTypeMode.TRANSPORT),
                    new JMSAsyncChannel("endpoint2", JMSConstants.DESTINATION_TYPE_QUEUE, ContentTypeMode.TRANSPORT) },
                    2, false, env, new JMSTransportDescriptionFactory(false, false, 2)));
        }
        
        
        builder.build();
        
        return suite;
    }
}
