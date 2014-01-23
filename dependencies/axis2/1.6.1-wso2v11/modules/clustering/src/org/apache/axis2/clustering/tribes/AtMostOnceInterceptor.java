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

package org.apache.axis2.clustering.tribes;

import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message intereceptor for handling at-most-once message processing semantics
 */
public final class AtMostOnceInterceptor extends ChannelInterceptorBase {

    private static Log log = LogFactory.getLog(AtMostOnceInterceptor.class);
    private static final Map<MessageId, Long> receivedMessages =
            new ConcurrentHashMap<MessageId, Long>();

    /**
     * The time a message lives in the receivedMessages Map
     */
    private static final int TIMEOUT = 5 * 60 * 1000;

    public AtMostOnceInterceptor() {
        Thread cleanupThread = new Thread(new MessageCleanupTask());
        cleanupThread.setPriority(Thread.MIN_PRIORITY);
        cleanupThread.setName("AtMostOnceInterceptor:Message-cleanup-thread"); // for debugging purposes
        cleanupThread.start();
    }

    public void messageReceived(ChannelMessage msg) {
        if (okToProcess(msg.getOptions())) {
            synchronized (receivedMessages) {
                MessageId msgId = new MessageId(msg.getUniqueId());
                if (receivedMessages.get(msgId) == null) {  // If it is a new message, keep track of it
                    receivedMessages.put(msgId, System.currentTimeMillis());
                    super.messageReceived(msg);
                } else {  // If it is a duplicate message, discard it. i.e. dont call super.messageReceived
                    log.info("Duplicate message received from " + TribesUtil.getName(msg.getAddress()));
                }
            }
        } else {
            super.messageReceived(msg);
        }
    }

    private static class MessageCleanupTask implements Runnable {

        public void run() {
            while (true) { // This task should never terminate
                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    List<MessageId> toBeRemoved = new ArrayList<MessageId>();
                    Thread.yield();
                    synchronized (receivedMessages) {
                        for (MessageId msgId : receivedMessages.keySet()) {
                            long arrivalTime = receivedMessages.get(msgId);
                            if (System.currentTimeMillis() - arrivalTime >= TIMEOUT) {
                                toBeRemoved.add(msgId);
                                if (toBeRemoved.size() > 10000) { // Do not allow this thread to run for too long
                                    break;
                                }
                            }
                        }
                        for (MessageId msgId : toBeRemoved) {
                            receivedMessages.remove(msgId);
                            if (log.isDebugEnabled()) {
                                log.debug("Cleaned up message ");
                            }
                        }
                    }
                } catch (Throwable e) {
                    log.error("Exception occurred while trying to cleanup messages", e);
                }
            }
        }
    }

    /**
     * Represents a Message ID
     */
    private static class MessageId {
        private byte[] id;

        private MessageId(byte[] id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MessageId messageId = (MessageId) o;

            if (!Arrays.equals(id, messageId.id)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(id);
        }
    }
}
