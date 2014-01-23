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

package org.apache.axis2.transport.testkit.endpoint;

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.Adapter;
import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.message.MessageDecoder;

public class AsyncEndpointAdapter<M,N> implements AsyncEndpoint<M>, Adapter {
    private final AsyncEndpoint<N> target;
    private final MessageDecoder<N,M> decoder;
    
    public AsyncEndpointAdapter(AsyncEndpoint<N> target, MessageDecoder<N,M> decoder) {
        this.target = target;
        this.decoder = decoder;
    }
    
    public AsyncEndpoint<N> getTarget() {
        return target;
    }

    public void clear() throws Exception {
        target.clear();
    }

    public IncomingMessage<M> waitForMessage(int timeout) throws Throwable {
        IncomingMessage<N> message = target.waitForMessage(timeout);
        if (message == null) {
            return null;
        } else {
            ContentType contentType = message.getContentType();
            return new IncomingMessage<M>(contentType, 
                                          decoder.decode(contentType, message.getData()));
        }
    }
}
