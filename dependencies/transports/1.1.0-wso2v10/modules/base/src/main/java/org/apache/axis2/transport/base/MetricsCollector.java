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

package org.apache.axis2.transport.base;
import org.apache.axis2.context.MessageContext;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * Collects metrics related to a transport that has metrics support enabled
 */
public class MetricsCollector {

    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_TRANSPORT = 1;
    public static final int LEVEL_FULL = 2;
    private static final Long ONE = (long) 1;

    /** By default, full metrics collection is enabled */
    private int level = LEVEL_FULL;

    private long messagesReceived;
    private long faultsReceiving;
    private long timeoutsReceiving;
    private long bytesReceived;
    private long minSizeReceived;
    private long maxSizeReceived;
    private double avgSizeReceived;

    private long messagesSent;
    private long faultsSending;
    private long timeoutsSending;
    private long bytesSent;
    private long minSizeSent;
    private long maxSizeSent;
    private double avgSizeSent;

    private final Map<Integer, Long> responseCodeTable =
        Collections.synchronizedMap(new HashMap<Integer, Long>());

    private long lastResetTime = System.currentTimeMillis();

    public void reset() {
        messagesReceived  = 0;
        faultsReceiving   = 0;
        timeoutsReceiving = 0;
        bytesReceived     = 0;
        minSizeReceived   = 0;
        maxSizeReceived   = 0;
        avgSizeReceived   = 0;

        messagesSent      = 0;
        faultsSending     = 0;
        timeoutsSending   = 0;
        bytesSent         = 0;
        minSizeSent       = 0;
        maxSizeSent       = 0;
        avgSizeSent       = 0;

        responseCodeTable.clear();
        lastResetTime = System.currentTimeMillis();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getLastResetTime() {
        return lastResetTime;
    }

    public long getMessagesReceived() {
        return messagesReceived;
    }

    public long getFaultsReceiving() {
        return faultsReceiving;
    }

    public long getTimeoutsReceiving() {
        return timeoutsReceiving;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    /**
     * Get the number of messages sent. This metrics is incremented after a
     * message has been completely and successfully put on the wire.
     * 
     * @return the number of messages sent
     */
    public long getMessagesSent() {
        return messagesSent;
    }

    public long getFaultsSending() {
        return faultsSending;
    }

    public long getTimeoutsSending() {
        return timeoutsSending;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getMinSizeReceived() {
        return minSizeReceived;
    }

    public long getMaxSizeReceived() {
        return maxSizeReceived;
    }

    public long getMinSizeSent() {
        return minSizeSent;
    }

    public long getMaxSizeSent() {
        return maxSizeSent;
    }

    public double getAvgSizeReceived() {
        return avgSizeReceived;
    }

    public double getAvgSizeSent() {
        return avgSizeSent;
    }

    public Map<Integer, Long> getResponseCodeTable() {
        return responseCodeTable;
    }

    public synchronized void incrementMessagesReceived() {
        messagesReceived++;
    }

    public synchronized void incrementFaultsReceiving() {
        faultsReceiving++;
    }

    public synchronized void incrementTimeoutsReceiving() {
        timeoutsReceiving++;
    }

    public synchronized void incrementBytesReceived(long size) {
        bytesReceived += size;
    }

    public synchronized void incrementMessagesSent() {
        messagesSent++;
    }

    public synchronized void incrementFaultsSending() {
        faultsSending++;
    }

    public synchronized void incrementTimeoutsSending() {
        timeoutsSending++;
    }

    public synchronized void incrementBytesSent(long size) {
        bytesSent += size;
    }
    
    public synchronized void notifyReceivedMessageSize(long size) {
        if (minSizeReceived == 0 || size < minSizeReceived) {
            minSizeReceived = size;
        }
        if (size > maxSizeReceived) {
            maxSizeReceived = size;
        }
        avgSizeReceived = (avgSizeReceived == 0 ? size : (avgSizeReceived + size) / 2);
    }

    public synchronized void notifySentMessageSize(long size) {
        if (minSizeSent == 0 || size < minSizeSent) {
            minSizeSent = size;
        }
        if (size > maxSizeSent) {
            maxSizeSent = size;
        }
        avgSizeSent = (avgSizeSent == 0 ? size : (avgSizeSent + size) / 2);
    }

    public void reportResponseCode(int respCode) {
        synchronized(responseCodeTable) {
            Object o = responseCodeTable.get(respCode);
            if (o == null) {
                responseCodeTable.put(respCode, ONE);
            } else {
                responseCodeTable.put(respCode, (Long) o + 1);
            }
        }
    }

    // --- enhanced methods ---
    private MessageLevelMetricsCollector getMsgLevelMetrics(MessageContext mc) {
        if (mc != null && level == LEVEL_FULL) {
            return (MessageLevelMetricsCollector) mc.getProperty(BaseConstants.METRICS_COLLECTOR);
        }
        return null;
    }

    public void incrementMessagesReceived(MessageContext mc) {
        incrementMessagesReceived();
        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.incrementMessagesReceived();
        }
    }

    public void incrementFaultsReceiving(int errorCode, MessageContext mc) {
        incrementFaultsReceiving();
        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.incrementFaultsReceiving(errorCode);
        }
    }

    public void incrementTimeoutsReceiving(MessageContext mc) {
        incrementTimeoutsReceiving();
        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.incrementTimeoutsReceiving();
        }
    }

    public void incrementBytesReceived(MessageContext mc, long size) {
        incrementBytesReceived(size);
        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.incrementBytesReceived(size);
        }
    }

    public void incrementMessagesSent(MessageContext mc) {
        incrementMessagesSent();
        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.incrementMessagesSent();
        }
    }

    public void incrementFaultsSending(int errorCode, MessageContext mc) {
        incrementFaultsSending();
        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.incrementFaultsSending(errorCode);
        }
    }

    public void incrementTimeoutsSending(MessageContext mc) {
        incrementTimeoutsSending();
        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.incrementTimeoutsSending();
        }
    }

    public void incrementBytesSent(MessageContext mc, long size) {
        incrementBytesSent(size);
        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.incrementBytesSent(size);
        }
    }

    public void notifyReceivedMessageSize(MessageContext mc, long size) {
        notifyReceivedMessageSize(size);

        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.notifyReceivedMessageSize(size);
        }
    }

    public void notifySentMessageSize(MessageContext mc, long size) {
        notifySentMessageSize(size);

        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.notifySentMessageSize(size);
        }
    }

    public void reportResponseCode(MessageContext mc, int respCode) {
        reportResponseCode(respCode);

        MessageLevelMetricsCollector m = getMsgLevelMetrics(mc);
        if (m != null) {
            m.reportResponseCode(respCode);
        }
    }
}
