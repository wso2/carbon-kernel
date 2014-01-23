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

package org.apache.axis2.transport.testkit.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;

public class LifecycleFixTransportListenerProxy implements TransportListener {
    private final TransportListener target;
    private final int port;

    public LifecycleFixTransportListenerProxy(TransportListener target, int port) {
        this.target = target;
        this.port = port;
    }

    public void destroy() {
        target.destroy();
    }

    @SuppressWarnings("deprecation")
    public EndpointReference getEPRForService(String arg0, String arg1)
            throws AxisFault {
        return target.getEPRForService(arg0, arg1);
    }

    public EndpointReference[] getEPRsForService(String arg0, String arg1)
            throws AxisFault {
        return target.getEPRsForService(arg0, arg1);
    }

    public SessionContext getSessionContext(MessageContext arg0) {
        return target.getSessionContext(arg0);
    }

    public void init(ConfigurationContext arg0, TransportInDescription arg1)
            throws AxisFault {
        target.init(arg0, arg1);
    }

    public void start() throws AxisFault {
        target.start();
        try {
            ServerUtil.waitForServer(port);
        } catch (Exception ex) {
            throw new AxisFault("Unable to start server", ex);
        }
    }

    public void stop() throws AxisFault {
        target.stop();
    }
}
