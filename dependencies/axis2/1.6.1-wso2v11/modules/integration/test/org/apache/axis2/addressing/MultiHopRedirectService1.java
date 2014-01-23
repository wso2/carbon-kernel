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

package org.apache.axis2.addressing;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.integration.UtilServer;

public class MultiHopRedirectService1 {

    public OMElement echoRedirect(OMElement ome) throws AxisFault {
        MessageContext currInMc = MessageContext.getCurrentMessageContext();
        MessageContext currOutMc = currInMc.getOperationContext().getMessageContext("Out");
        currOutMc.setTo(new EndpointReference("http://127.0.0.1:" + (UtilServer.TESTING_PORT) +
                "/axis2/services/MultiHopRedirectService2/echoRedirect"));
        currOutMc.setReplyTo(currInMc.getReplyTo());
        return ome;
    }
}
