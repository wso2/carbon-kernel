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

package samples.ping.receivers;

import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.engine.Pingable;

import java.lang.reflect.Method;

public class RawXMLINOutMessageReceiver extends org.apache.axis2.receivers.RawXMLINOutMessageReceiver
        implements MessageReceiver, Pingable {

    public int ping() throws AxisFault{
        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(MessageContext.getCurrentMessageContext());

        if(obj instanceof Pingable){
            return ((Pingable)obj).ping();
        }
        return PING_MR_LEVEL;
    }
}
