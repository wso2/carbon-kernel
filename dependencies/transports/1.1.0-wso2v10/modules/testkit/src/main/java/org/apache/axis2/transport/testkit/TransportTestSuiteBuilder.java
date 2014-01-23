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

package org.apache.axis2.transport.testkit;

import static org.apache.axis2.transport.testkit.AdapterUtils.adapt;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.channel.RequestResponseChannel;
import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.client.RequestResponseTestClient;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.endpoint.InOutEndpoint;
import org.apache.axis2.transport.testkit.endpoint.TestEndpoint;
import org.apache.axis2.transport.testkit.message.AxisMessage;
import org.apache.axis2.transport.testkit.message.MessageDecoder;
import org.apache.axis2.transport.testkit.message.MessageEncoder;
import org.apache.axis2.transport.testkit.message.RESTMessage;
import org.apache.axis2.transport.testkit.message.XMLMessage;
import org.apache.axis2.transport.testkit.message.RESTMessage.Parameter;
import org.apache.axis2.transport.testkit.tests.async.BinaryTestCase;
import org.apache.axis2.transport.testkit.tests.async.LargeSOAPAsyncMessageTestCase;
import org.apache.axis2.transport.testkit.tests.async.RESTTestCase;
import org.apache.axis2.transport.testkit.tests.async.SwATestCase;
import org.apache.axis2.transport.testkit.tests.async.TextPlainTestCase;
import org.apache.axis2.transport.testkit.tests.async.XMLAsyncMessageTestCase;
import org.apache.axis2.transport.testkit.tests.echo.XMLRequestResponseMessageTestCase;

public class TransportTestSuiteBuilder {
    static class ResourceRelation<T> {
        private final T primaryResource;
        private final Object[] relatedResources;
        
        public ResourceRelation(T primaryResource, Object... relatedResources) {
            this.primaryResource = primaryResource;
            this.relatedResources = relatedResources;
        }

        public T getPrimaryResource() {
            return primaryResource;
        }

        public Object[] getRelatedResources() {
            return relatedResources;
        }
    }
    
    static class ResourceList<T> implements Iterable<ResourceRelation<T>> {
        private final List<ResourceRelation<T>> list = new LinkedList<ResourceRelation<T>>();
        
        public void add(T primaryResource, Object... relatedResources) {
            list.add(new ResourceRelation<T>(primaryResource, relatedResources));
        }

        public Iterator<ResourceRelation<T>> iterator() {
            return list.iterator();
        }
    }
    
    public static final String testString = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";
    
    public static final MessageTestData ASCII_TEST_DATA = new MessageTestData("ASCII", "test string", "us-ascii");
    public static final MessageTestData UTF8_TEST_DATA = new MessageTestData("UTF8", testString, "UTF-8");
    public static final MessageTestData LATIN1_TEST_DATA = new MessageTestData("Latin1", testString, "ISO-8859-1");
    
    private static final MessageTestData[] messageTestData = new MessageTestData[] {
        ASCII_TEST_DATA,
        UTF8_TEST_DATA,
        LATIN1_TEST_DATA,
    };
    
    private static final RESTMessage restTestMessage1 = new RESTMessage(new Parameter[] {
        new Parameter("param1", "value1"),
        new Parameter("param2", "value2"),
    });
    
    private static final RESTMessage restTestMessage2 = new RESTMessage(new Parameter[] {
            new Parameter("param", "value1"),
            new Parameter("param", "value2"),
        });
    
    private final ManagedTestSuite suite;
    
    private final List<Object[]> environments = new LinkedList<Object[]>();
    
    private final ResourceList<AsyncChannel> asyncChannels = new ResourceList<AsyncChannel>();
    
    private final ResourceList<AsyncTestClient<byte[]>> byteAsyncClients = new ResourceList<AsyncTestClient<byte[]>>();
    private final ResourceList<AsyncTestClient<XMLMessage>> xmlAsyncClients = new ResourceList<AsyncTestClient<XMLMessage>>();
    private final ResourceList<AsyncTestClient<RESTMessage>> restAsyncClients = new ResourceList<AsyncTestClient<RESTMessage>>();
    private final ResourceList<AsyncTestClient<String>> stringAsyncClients = new ResourceList<AsyncTestClient<String>>();
    
    private final ResourceList<AsyncEndpoint<byte[]>> byteAsyncEndpoints = new ResourceList<AsyncEndpoint<byte[]>>();
    private final ResourceList<AsyncEndpoint<XMLMessage>> xmlAsyncEndpoints = new ResourceList<AsyncEndpoint<XMLMessage>>();
    private final ResourceList<AsyncEndpoint<RESTMessage>> restAsyncEndpoints = new ResourceList<AsyncEndpoint<RESTMessage>>();
    private final ResourceList<AsyncEndpoint<String>> stringAsyncEndpoints = new ResourceList<AsyncEndpoint<String>>();
    
    private final ResourceList<RequestResponseChannel> requestResponseChannels = new ResourceList<RequestResponseChannel>();
    
    private final ResourceList<RequestResponseTestClient<XMLMessage,XMLMessage>> xmlRequestResponseClients = new ResourceList<RequestResponseTestClient<XMLMessage,XMLMessage>>();
    
    private final ResourceList<InOutEndpoint> echoEndpoints = new ResourceList<InOutEndpoint>();
    
    public TransportTestSuiteBuilder(ManagedTestSuite suite) {
        this.suite = suite;
        try {
            // We only want tests with client and/or endpoint based on Axis
            suite.addExclude("(&(client=*)(endpoint=*)(!(|(client=axis)(endpoint=axis))))");
        } catch (ParseException ex) {
            throw new Error(ex);
        }
    }
    
    public void addEnvironment(Object... resources) {
        environments.add(resources);
    }
    
    public void addAsyncChannel(AsyncChannel channel, Object... relatedResources) {
        asyncChannels.add(channel, relatedResources);
    }
    
    public void addAxisAsyncTestClient(AsyncTestClient<AxisMessage> client, Object... relatedResources) {
        byteAsyncClients.add(adapt(client, MessageEncoder.BINARY_WRAPPER), relatedResources);
        xmlAsyncClients.add(adapt(client, MessageEncoder.XML_TO_AXIS), relatedResources);
        stringAsyncClients.add(adapt(client, MessageEncoder.TEXT_WRAPPER), relatedResources);
    }
    
    public void addByteArrayAsyncTestClient(AsyncTestClient<byte[]> client, Object... relatedResources) {
        byteAsyncClients.add(client, relatedResources);
        xmlAsyncClients.add(adapt(client, MessageEncoder.XML_TO_BYTE), relatedResources);
        stringAsyncClients.add(adapt(client, MessageEncoder.STRING_TO_BYTE), relatedResources);
    }
    
    public void addRESTAsyncTestClient(AsyncTestClient<RESTMessage> client, Object... relatedResources) {
        restAsyncClients.add(client, relatedResources);
    }
    
    public void addStringAsyncTestClient(AsyncTestClient<String> client, Object... relatedResources) {
        xmlAsyncClients.add(adapt(client, MessageEncoder.XML_TO_STRING), relatedResources);
        stringAsyncClients.add(client, relatedResources);
    }
    
    public void addAxisAsyncEndpoint(AsyncEndpoint<AxisMessage> endpoint, Object... relatedResources) {
        byteAsyncEndpoints.add(adapt(endpoint, MessageDecoder.AXIS_TO_BYTE), relatedResources);
        xmlAsyncEndpoints.add(adapt(endpoint, MessageDecoder.AXIS_TO_XML), relatedResources);
        restAsyncEndpoints.add(adapt(endpoint, MessageDecoder.AXIS_TO_REST), relatedResources);
        stringAsyncEndpoints.add(adapt(endpoint, MessageDecoder.AXIS_TO_STRING), relatedResources);
    }
    
    public void addByteArrayAsyncEndpoint(AsyncEndpoint<byte[]> endpoint, Object... relatedResources) {
        byteAsyncEndpoints.add(endpoint, relatedResources);
        xmlAsyncEndpoints.add(adapt(endpoint, MessageDecoder.BYTE_TO_XML), relatedResources);
        stringAsyncEndpoints.add(adapt(endpoint, MessageDecoder.BYTE_TO_STRING), relatedResources);
    }
    
    public void addRESTAsyncEndpoint(AsyncEndpoint<RESTMessage> endpoint, Object... relatedResources) {
        restAsyncEndpoints.add(endpoint, relatedResources);
    }
    
    public void addRequestResponseChannel(RequestResponseChannel channel, Object... relatedResources) {
        requestResponseChannels.add(channel, relatedResources);
    }
    
    public void addAxisRequestResponseTestClient(RequestResponseTestClient<AxisMessage,AxisMessage> client, Object... relatedResources) {
        xmlRequestResponseClients.add(adapt(client, MessageEncoder.XML_TO_AXIS, MessageDecoder.AXIS_TO_XML), relatedResources);
    }
    
    public void addByteArrayRequestResponseTestClient(RequestResponseTestClient<byte[],byte[]> client, Object... relatedResources) {
        xmlRequestResponseClients.add(adapt(client, MessageEncoder.XML_TO_BYTE, MessageDecoder.BYTE_TO_XML), relatedResources);
    }
    
    public void addStringRequestResponseTestClient(RequestResponseTestClient<String,String> client, Object... relatedResources) {
        xmlRequestResponseClients.add(adapt(client, MessageEncoder.XML_TO_STRING, MessageDecoder.STRING_TO_XML), relatedResources);
    }
    
    public void addEchoEndpoint(InOutEndpoint endpoint, Object... relatedResources) {
        echoEndpoints.add(endpoint, relatedResources);
    }
    
    private Object[] merge(Object[] environment, ResourceRelation<?>... resourceRelations) {
        Set<Object> resources = new LinkedHashSet<Object>();
        resources.addAll(Arrays.asList(environment));
        for (ResourceRelation<?> resourceRelation : resourceRelations) {
            resources.addAll(Arrays.asList(resourceRelation.getRelatedResources()));
        }
        return resources.toArray();
    }
    
    private void build(Object[] env) {
        for (ResourceRelation<AsyncChannel> channel : asyncChannels) {
            for (ResourceRelation<AsyncTestClient<XMLMessage>> client : xmlAsyncClients) {
                for (ResourceRelation<AsyncEndpoint<XMLMessage>> endpoint : xmlAsyncEndpoints) {
                    Object[] resources = merge(env, channel, client, endpoint);
                    for (MessageTestData data : messageTestData) {
                        for (XMLMessage.Type type : XMLMessage.Type.values()) {
                            if (type != XMLMessage.Type.SWA) {
                                suite.addTest(new XMLAsyncMessageTestCase(channel.getPrimaryResource(), client.getPrimaryResource(), endpoint.getPrimaryResource(), type, data, resources));
                            }
                        }
                    }
                    suite.addTest(new SwATestCase(channel.getPrimaryResource(), client.getPrimaryResource(), endpoint.getPrimaryResource(), resources));
                    // Regression test for SYNAPSE-423:
                    suite.addTest(new LargeSOAPAsyncMessageTestCase(channel.getPrimaryResource(), client.getPrimaryResource(), endpoint.getPrimaryResource(), resources));
                }
            }
            for (ResourceRelation<AsyncTestClient<String>> client : stringAsyncClients) {
                for (ResourceRelation<AsyncEndpoint<String>> endpoint : stringAsyncEndpoints) {
                    Object[] resources = merge(env, channel, client, endpoint);
                    for (MessageTestData data : messageTestData) {
                        suite.addTest(new TextPlainTestCase(channel.getPrimaryResource(), client.getPrimaryResource(), endpoint.getPrimaryResource(), data, resources));
                    }
                }
            }
            for (ResourceRelation<AsyncTestClient<byte[]>> client : byteAsyncClients) {
                for (ResourceRelation<AsyncEndpoint<byte[]>> endpoint : byteAsyncEndpoints) {
                    Object[] resources = merge(env, channel, client, endpoint);
                    suite.addTest(new BinaryTestCase(channel.getPrimaryResource(), client.getPrimaryResource(), endpoint.getPrimaryResource(), resources));
                }
            }
            for (ResourceRelation<AsyncTestClient<RESTMessage>> client : restAsyncClients) {
                for (ResourceRelation<AsyncEndpoint<RESTMessage>> endpoint : restAsyncEndpoints) {
                    Object[] resources = merge(env, channel, client, endpoint);
                    suite.addTest(new RESTTestCase(channel.getPrimaryResource(), client.getPrimaryResource(), endpoint.getPrimaryResource(), restTestMessage1, resources));
                    // TODO: regression test for SYNAPSE-431
//                    addTest(new RESTTestCase(env, channel, client, endpoint, restTestMessage2));
                }
            }
        }
        for (ResourceRelation<RequestResponseChannel> channel : requestResponseChannels) {
            for (ResourceRelation<RequestResponseTestClient<XMLMessage,XMLMessage>> client : xmlRequestResponseClients) {
                for (ResourceRelation<InOutEndpoint> endpoint : echoEndpoints) {
                    Object[] resources = merge(env, channel, client, endpoint);
                    for (MessageTestData data : messageTestData) {
                        for (XMLMessage.Type type : XMLMessage.Type.values()) {
                            if (type != XMLMessage.Type.SWA) {
                                suite.addTest(new XMLRequestResponseMessageTestCase(channel.getPrimaryResource(), client.getPrimaryResource(), endpoint.getPrimaryResource(), type, data, resources));
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void build() {
        if (environments.isEmpty()) {
            build(new Object[0]);
        } else {
            for (Object[] env : environments) {
                build(env);
            }
        }
    }
}
