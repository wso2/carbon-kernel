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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.message.IncomingMessage;

public class InOnlyEndpointSupport<M> {
    private final BlockingQueue<Event<M>> queue = new LinkedBlockingQueue<Event<M>>();
    
    private interface Event<M> {
        IncomingMessage<M> process() throws Throwable;
    }
    
    public void putException(final Throwable ex) {
        queue.add(new Event<M>() {
            public IncomingMessage<M> process() throws Throwable {
                throw ex;
            }
        });
    }
    
    public void putMessage(final ContentType contentType, final M data) {
        queue.add(new Event<M>() {
            public IncomingMessage<M> process() throws Throwable {
                return new IncomingMessage<M>(contentType, data);
            }
        });
    }
    
    public void putMessage(final IncomingMessage<M> message) {
        queue.add(new Event<M>() {
            public IncomingMessage<M> process() throws Throwable {
                return message;
            }
        });
    }

    public void clear() {
        queue.clear();
    }

    public IncomingMessage<M> waitForMessage(int timeout) throws Throwable {
        Event<M> event = queue.poll(timeout, TimeUnit.MILLISECONDS);
        return event == null ? null : event.process();
    }
}
