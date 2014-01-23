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

package org.apache.axis2.transport.testkit.tests.echo;

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.channel.RequestResponseChannel;
import org.apache.axis2.transport.testkit.client.RequestResponseTestClient;
import org.apache.axis2.transport.testkit.endpoint.InOutEndpoint;
import org.apache.axis2.transport.testkit.tests.MessageTestCase;

public abstract class RequestResponseMessageTestCase<M,N> extends MessageTestCase {
    private final RequestResponseTestClient<M,N> client;
    private final InOutEndpoint endpoint;

    // TODO: maybe we don't need an explicit RequestResponseChannel
    public RequestResponseMessageTestCase(RequestResponseChannel channel, RequestResponseTestClient<M,N> client, InOutEndpoint endpoint, ContentType contentType, String charset, Object... resources) {
        super(client, contentType, charset, resources);
        this.client = client;
        this.endpoint = endpoint;
        addResource(channel);
        addResource(endpoint);
    }
    
    @Override
    protected void doRunTest() throws Throwable {
        M request = prepareRequest();
        InterruptingEndpointErrorListener listener = new InterruptingEndpointErrorListener(Thread.currentThread());
        N response;
        endpoint.addEndpointErrorListener(listener);
        try {
            response = client.sendMessage(options, contentType, request).getData();
        } catch (Throwable ex) {
            if (listener.getException() != null) {
                throw listener.getException();
            } else {
                throw ex;
            }
        } finally {
            endpoint.removeEndpointErrorListener(listener);
        }
        checkResponse(request, response);
    }

    protected abstract M prepareRequest() throws Exception;
    protected abstract void checkResponse(M request, N response) throws Exception;
}
