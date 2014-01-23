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

package org.apache.axis2.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a MessageReceiver which is used on the client side to accept the
 * messages (response) that come to the client. This correlates the incoming message to
 * the related messages and makes a call to the appropriate callback.
 */
public class CallbackReceiver implements MessageReceiver {

    private static final Log log = LogFactory.getLog(CallbackReceiver.class);

    public static String SERVICE_NAME = "ClientService";
    protected ConcurrentHashMap callbackStore;

    public CallbackReceiver() {
        callbackStore = new ConcurrentHashMap();
    }

    public void addCallback(String msgID, AxisCallback callback) throws AxisFault {
        putIfAbsent(msgID, callback);
    }

    /**
     * Inserts the specified key, value into the callback map. It throws an
     * exception if the message id was a duplicate.
     *
     * @param msgID The message id.
     * @param callback The callback object.
     * @throws AxisFault If the message id was a duplicate.
     */
    private void putIfAbsent(String msgID, Object callback) throws AxisFault {
        if (callbackStore.putIfAbsent(msgID, callback) == null) {
            if (log.isDebugEnabled()) {
                log.debug("CallbackReceiver: add callback " + msgID + ", " + callback + " ," + this);
            }
        } else {
            throw new AxisFault("The Callback for MessageID " + msgID + " is a duplicate");
        }
    }

    public Object lookupCallback(String msgID) {
        Object o = callbackStore.remove(msgID);
        if (log.isDebugEnabled()) log.debug("CallbackReceiver: lookup callback " + msgID + ", " + o + " ," + this);
        return o;
    }

    public void receive(MessageContext msgContext) throws AxisFault {
        RelatesTo relatesTO = msgContext.getOptions().getRelatesTo();
        if (relatesTO == null) {
            throw new AxisFault("Cannot identify correct Callback object. RelatesTo is null");
        }
        String messageID = relatesTO.getValue();

        Object callbackObj = callbackStore.remove(messageID);
        if (log.isDebugEnabled()) log.debug("CallbackReceiver: receive found callback " + callbackObj + ", " + messageID + ", " + this + ", " + msgContext.getAxisOperation());

        if (callbackObj == null) {
            throw new AxisFault("The Callback for MessageID " + messageID + " was not found");
        }

        if (callbackObj instanceof AxisCallback) {
            AxisCallback axisCallback = (AxisCallback)callbackObj;
            if (msgContext.isFault()) {
                axisCallback.onFault(msgContext);
            } else {
                axisCallback.onMessage(msgContext);
            }
            // Either way we're done.
            axisCallback.onComplete();
            return;
        }

    }

    //to get the pending request
    public Map getCallbackStore() {
        return callbackStore;
    }
}
