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

import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.name.Key;

/**
 * Interface implemented by in-only test endpoints.
 * <p>
 * The endpoint must be ready to receive messages immediately after it
 * has been set up. In particular implementations must not make the
 * assumption that messages are only during a call to {@link #waitForMessage(int)}.
 * Indeed, a typical test case will set up the endpoint, send a message and only
 * afterwards call {@link #waitForMessage(int)}.
 * <p>
 * There are two strategies to guarantee this behavior:
 * <ul>
 *   <li>The underlying transport internally queues incoming messages.
 *       In that case {@link #waitForMessage(int)} should simply poll
 *       for new messages. An example of this type of transport is
 *       the mail transport.</li>
 *   <li>The underlying transport requires that incoming messages are
 *       processed immediately. In that case the implementation should
 *       set up the required receiver or message processor and add
 *       incoming messages to an internal queue that is polled
 *       when {@link #waitForMessage(int)} is called. An example of
 *       this kind of transport is HTTP. Implementations can use
 *       {@link InOnlyEndpointSupport} to manage the message queue.</li>
 * </ul>
 * 
 * @see org.apache.axis2.transport.testkit.endpoint
 * 
 * @param <M>
 */
// TODO: rename this to InOnlyEndpoint
@Key("endpoint")
public interface AsyncEndpoint<M> {
    /**
     * Discard any pending messages.
     */
    void clear() throws Exception;
    
    IncomingMessage<M> waitForMessage(int timeout) throws Throwable;
}
