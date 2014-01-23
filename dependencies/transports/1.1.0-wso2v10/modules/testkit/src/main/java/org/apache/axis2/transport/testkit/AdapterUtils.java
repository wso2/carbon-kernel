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

import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.client.AsyncTestClientAdapter;
import org.apache.axis2.transport.testkit.client.RequestResponseTestClient;
import org.apache.axis2.transport.testkit.client.RequestResponseTestClientAdapter;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpointAdapter;
import org.apache.axis2.transport.testkit.message.MessageDecoder;
import org.apache.axis2.transport.testkit.message.MessageEncoder;

public class AdapterUtils {
    public static <M,N> AsyncTestClient<M> adapt(AsyncTestClient<N> target, MessageEncoder<M,N> encoder) {
        return new AsyncTestClientAdapter<M,N>(target, encoder);
    }

    public static <M,N,O,P> RequestResponseTestClient<M,O> adapt(RequestResponseTestClient<N,P> target, MessageEncoder<M,N> encoder, MessageDecoder<P,O> decoder) {
        return new RequestResponseTestClientAdapter<M,N,O,P>(target, encoder, decoder);
    }

    public static <M,N> AsyncEndpoint<M> adapt(AsyncEndpoint<N> target, MessageDecoder<N,M> decoder) {
        return new AsyncEndpointAdapter<M,N>(target, decoder);
    }
}
