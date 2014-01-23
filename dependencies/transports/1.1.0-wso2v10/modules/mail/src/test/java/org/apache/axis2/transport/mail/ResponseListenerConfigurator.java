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

package org.apache.axis2.transport.mail;

import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.testkit.axis2.client.AxisTestClientContextConfigurator;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

public class ResponseListenerConfigurator implements AxisTestClientContextConfigurator {
    private @Transient MailTestEnvironment env;
    private @Transient MailTestEnvironment.Account sender;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(MailTestEnvironment env, MailChannel channel) {
        this.env = env;
        sender = channel.getSender();
    }
    
    public boolean isTransportListenerRequired() {
        return true;
    }

    public void setupTransport(TransportInDescription trpInDesc,
            TransportOutDescription trpOutDesc) throws Exception{
        
        env.setupPoll(trpInDesc, sender);
    }
}
