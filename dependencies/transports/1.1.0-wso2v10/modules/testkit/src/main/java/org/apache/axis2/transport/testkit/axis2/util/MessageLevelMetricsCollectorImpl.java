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

package org.apache.axis2.transport.testkit.axis2.util;

import org.apache.axis2.transport.base.MessageLevelMetricsCollector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Message level metrics collector implementation used during the tests to check that
 * message level metrics collection is implemented.
 * <p>
 * On any update, all threads waiting for an instance of this class are notified.
 */
public class MessageLevelMetricsCollectorImpl implements MessageLevelMetricsCollector {
    private static final Log log = LogFactory.getLog(MessageLevelMetricsCollectorImpl.class);
    
    private long messagesSent;
    private long bytesSent;
    
    public void incrementBytesReceived(long size) {
    }

    public synchronized void incrementBytesSent(long size) {
        log.debug("incrementBytesSent called with size = " + size);
        bytesSent += size;
        notifyAll();
    }

    public void incrementFaultsReceiving(int errorCode) {
    }

    public void incrementFaultsSending(int errorCode) {
    }

    public void incrementMessagesReceived() {
    }

    public synchronized void incrementMessagesSent() {
        log.debug("incrementMessagesSent called");
        messagesSent++;
        notifyAll();
    }

    public void incrementTimeoutsReceiving() {
    }

    public void incrementTimeoutsSending() {
    }

    public void notifyReceivedMessageSize(long size) {
    }

    public void notifySentMessageSize(long size) {
    }

    public void reportReceivingFault(int errorCode) {
    }

    public void reportResponseCode(int respCode) {
    }

    public void reportSendingFault(int errorCode) {
    }

    public synchronized long getMessagesSent() {
        return messagesSent;
    }

    public synchronized long getBytesSent() {
        return bytesSent;
    }
}
