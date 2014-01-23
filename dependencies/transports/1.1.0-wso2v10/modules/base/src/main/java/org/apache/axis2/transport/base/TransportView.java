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

import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;

import java.util.Map;

public class TransportView implements TransportViewMBean {

    private ManagementSupport managementSupport = null;

    private TransportListener listener = null;

    private TransportSender sender = null;

    public TransportView(TransportListener listener, TransportSender sender) {
        if (listener != null) {
            this.listener = listener;
            if (listener instanceof  ManagementSupport) {
                managementSupport = (ManagementSupport) listener;
            }
        }
        if (sender != null) {
            this.sender = sender;
            if (sender instanceof ManagementSupport) {
                managementSupport = (ManagementSupport) sender;
            }
        }
    }

    // JMX Attributes
    public long getMessagesReceived() {
        if (managementSupport != null) {
            return managementSupport.getMessagesReceived();
        }
        return -1;
    }

    public long getFaultsReceiving() {
        if (managementSupport != null) {
            return managementSupport.getFaultsReceiving();
        }
        return -1;
    }

    public long getTimeoutsReceiving() {
        if (managementSupport != null) {
            return managementSupport.getTimeoutsReceiving();
        }
        return -1;
    }

    public long getTimeoutsSending() {
        if (managementSupport != null) {
            managementSupport.getTimeoutsSending();
        }
        return -1;
    }

    public long getBytesReceived() {
        if (managementSupport != null) {
            return managementSupport.getBytesReceived();
        }
        return -1;
    }

    public long getMessagesSent() {
        if (managementSupport != null) {
            return managementSupport.getMessagesSent();
        }
        return -1;
    }

    public long getFaultsSending() {
        if (managementSupport != null) {
            return managementSupport.getFaultsSending();
        }
        return -1;
    }

    public long getBytesSent() {
        if (managementSupport != null) {
            return managementSupport.getBytesSent();
        }
        return -1;
    }

    public long getMinSizeReceived() {
        if (managementSupport != null) {
            return managementSupport.getMinSizeReceived();
        }
        return -1;
    }

    public long getMaxSizeReceived() {
        if (managementSupport != null) {
            return managementSupport.getMaxSizeReceived();
        }
        return -1;
    }

    public double getAvgSizeReceived() {
        if (managementSupport != null) {
            return managementSupport.getAvgSizeReceived();
        }
        return -1;
    }

    public long getMinSizeSent() {
        if (managementSupport != null) {
            return managementSupport.getMinSizeSent();
        }
        return -1;
    }

    public long getMaxSizeSent() {
        if (managementSupport != null) {
            return managementSupport.getMaxSizeSent();
        }
        return -1;
    }

    public double getAvgSizeSent() {
        if (managementSupport != null) {
            return managementSupport.getAvgSizeSent();
        }
        return -1;
    }

    public Map getResponseCodeTable() {
        if (managementSupport != null ) {
            return managementSupport.getResponseCodeTable();
        }
        return null;
    }    

    public int getActiveThreadCount() {
        if (managementSupport != null) {
            return managementSupport.getActiveThreadCount();
        }
        return -1;
    }

    public int getQueueSize() {
        if (managementSupport != null) {
            return managementSupport.getQueueSize();
        }
        return -1;
    }

    // JMX Operations
    public void start() throws Exception{
        if (listener != null) {
            listener.start();
        }
    }

    public void stop() throws Exception {
        if (listener != null) {
            listener.stop();
        } else if (sender != null) {
            sender.stop();
        }
    }

    public void pause() throws Exception {
        if (managementSupport != null) {
            managementSupport.pause();
        }
    }

    public void resume() throws Exception {
        if (managementSupport != null) {
            managementSupport.resume();
        }
    }

    public void maintenenceShutdown(long seconds) throws Exception {
        if (managementSupport != null) {
            managementSupport.maintenenceShutdown(seconds * 1000);
        }
    }

    public void resetStatistics() {
        if (managementSupport != null) {
            managementSupport.resetStatistics();
        }
    }

    public long getLastResetTime() {
        if (managementSupport != null) {
            return managementSupport.getLastResetTime();
        }
        return -1;
    }

    public long getMetricsWindow() {
        if (managementSupport != null) {
            return System.currentTimeMillis() - managementSupport.getLastResetTime();
        }
        return -1;
    }    
}
