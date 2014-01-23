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

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;

public class DummyTransportListener implements TransportListener {
    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn) throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void start() throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stop() throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        return new EndpointReference[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
