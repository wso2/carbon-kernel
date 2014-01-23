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

public class MultiHopRedirectService2 {
    public OMElement echoRedirect(OMElement ome) throws AxisFault {
        System.out.println("MultiHopRedirectService2");
        MessageContext currInMc = MessageContext.getCurrentMessageContext();
        MessageContext currOutMc = currInMc.getOperationContext().getMessageContext("Out");
        currOutMc.setTo(currInMc.getReplyTo());
        currOutMc.getRelatesTo().setValue(currInMc.getRelatesTo().getValue());
        return ome;
    }
}
