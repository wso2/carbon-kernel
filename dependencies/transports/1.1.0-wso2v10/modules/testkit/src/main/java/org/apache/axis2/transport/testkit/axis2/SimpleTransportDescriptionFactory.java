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

package org.apache.axis2.transport.testkit.axis2;

import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;

public class SimpleTransportDescriptionFactory implements TransportDescriptionFactory {
    private final String name;
    private final Class<? extends TransportListener> listenerClass;
    private final Class<? extends TransportSender> senderClass;
    
    public SimpleTransportDescriptionFactory(String name,
                                             Class<? extends TransportListener> listenerClass,
                                             Class<? extends TransportSender> senderClass) {
        this.name = name;
        this.listenerClass = listenerClass;
        this.senderClass = senderClass;
    }

    public TransportInDescription createTransportInDescription() throws Exception {
        TransportInDescription trpInDesc = new TransportInDescription(name);
        trpInDesc.setReceiver(listenerClass.newInstance());
        return trpInDesc;
    }

    public TransportOutDescription createTransportOutDescription() throws Exception {
        TransportOutDescription trpOutDesc = new TransportOutDescription(name);
        trpOutDesc.setSender(senderClass.newInstance());
        return trpOutDesc;
    }
}
