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

package org.apache.axis2.transport.jms;

import org.apache.qpid.AMQException;
import org.apache.qpid.framing.AMQShortString;
import org.apache.qpid.server.queue.AMQQueue;
import org.apache.qpid.server.queue.QueueRegistry;
import org.apache.qpid.server.virtualhost.VirtualHost;

public class QpidUtil {
    private QpidUtil() {}
    
    public static void createQueue(VirtualHost virtualHost, AMQShortString exchangeName, String name) throws AMQException {
        AMQShortString _name = new AMQShortString(name);
        QueueRegistry queueRegistry = virtualHost.getQueueRegistry();
        if (queueRegistry.getQueue(_name) != null) {
            throw new IllegalStateException("Queue " + name + " already exists");
        }
        AMQQueue queue = new AMQQueue(_name, false, null, false, virtualHost);
        queueRegistry.registerQueue(queue);
        queue.bind(_name, null, virtualHost.getExchangeRegistry().getExchange(exchangeName));
    }
    
    public static void deleteQueue(VirtualHost virtualHost, String name) throws AMQException {
        AMQShortString _name = new AMQShortString(name);
        AMQQueue queue = virtualHost.getQueueRegistry().getQueue(_name);
        if (queue == null) {
            throw new IllegalArgumentException("Queue " + name + " doesn't exist");
        }
        queue.delete();
    }
}
