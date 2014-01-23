/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.client.async;

import org.apache.axis2.context.MessageContext;

public interface AxisCallback {
    /**
     * This is called when we receive a message.
     *
     * @param msgContext the (response) MessageContext
     */
    void onMessage(MessageContext msgContext);

    /**
     * This gets called when a fault message is received.
     *
     * @param msgContext the MessageContext containing the fault.
     */
    void onFault(MessageContext msgContext);

    /**
     * This gets called ONLY when an internal processing exception occurs.
     *
     * @param e the Exception which caused the problem
     */
    void onError(Exception e);

    /**
     * This is called at the end of the MEP no matter what happens, quite like a finally block.
     */
    void onComplete();
}
