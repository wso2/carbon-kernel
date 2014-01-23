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

package org.apache.axis2.transport.testkit.tests.async;

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.tests.MessageTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AsyncMessageTestCase<M> extends MessageTestCase {
    private static final Log log = LogFactory.getLog(AsyncMessageTestCase.class);
    
    private final AsyncTestClient<M> client;
    private final AsyncEndpoint<M> endpoint;
    
    // TODO: maybe we don't need an explicit AsyncChannel
    public AsyncMessageTestCase(AsyncChannel channel, AsyncTestClient<M> client, AsyncEndpoint<M> endpoint, ContentType contentType, String charset, Object... resources) {
        super(client, contentType, charset, resources);
        this.client = client;
        this.endpoint = endpoint;
        addResource(channel);
        addResource(endpoint);
    }

    @Override
    protected void doRunTest() throws Throwable {
        endpoint.clear();
        log.debug("Preparing message");
        M expected = prepareMessage();
        
        // Run the test.
//                    contentTypeMode == ContentTypeMode.TRANSPORT ? contentType : null);
        log.debug("Sending message");
        client.sendMessage(options, contentType, expected);
        log.debug("Message sent; waiting for endpoint to receive message");
        IncomingMessage<M> actual = endpoint.waitForMessage(8000);
        if (actual == null) {
            log.debug("Message NOT received by endpoint; failing test");
            fail("Failed to get message");
        }
        
        log.debug("Message received by endpoint; checking message data");
        checkMessageData(expected, actual.getData());
        log.debug("Message received by endpoint has expected content");
    }
    
    protected abstract M prepareMessage() throws Exception;
    protected abstract void checkMessageData(M expected, M actual) throws Exception;
}