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

package org.apache.axis2.transport.base.event;

import java.util.LinkedList;
import java.util.List;

import org.apache.axis2.description.AxisService;

public class TransportErrorSourceSupport implements TransportErrorSource {
    private final Object source;
    private final List<TransportErrorListener> listeners = new LinkedList<TransportErrorListener>();

    public TransportErrorSourceSupport(Object source) {
        this.source = source;
    }

    public synchronized void addErrorListener(TransportErrorListener listener) {
        listeners.add(listener);
    }
    
    public synchronized void removeErrorListener(TransportErrorListener listener) {
        listeners.remove(listener);
    }

    public synchronized void error(AxisService service, Throwable ex) {
        if (!listeners.isEmpty()) {
            TransportError error = new TransportError(source, service, ex);
            for (TransportErrorListener listener : listeners) {
                listener.error(error);
            }
        }
    }
}
