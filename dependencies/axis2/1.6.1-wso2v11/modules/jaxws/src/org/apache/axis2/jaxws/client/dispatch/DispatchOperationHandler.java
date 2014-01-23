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

package org.apache.axis2.jaxws.client.dispatch;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * In JAX-WS Dispatch clients we don't know what actual operation we are calling
 * until the message is converted to a SOAP envelope. So let's try to find the real
 * operation based on the first element in the body in order to send the right WSA
 * action.
 * 
 * This handler should be installed in OutFlow chain.
 */
public class DispatchOperationHandler extends org.apache.axis2.handlers.AbstractHandler {
    
    private static final Log LOG = LogFactory.getLog(DispatchOperationHandler.class);

    private boolean isAnonymousOperation(AxisOperation op) {
        return (ServiceClient.ANON_OUT_IN_OP.equals(op.getName()) ||
                ServiceClient.ANON_OUT_ONLY_OP.equals(op.getName()));
    }
    
    private QName getFirstBodyElement(SOAPEnvelope envelope) {
        SOAPBody body = envelope.getBody();
        if (body != null) {
            OMElement firstElement = body.getFirstElement();
            if (firstElement != null) {
                return firstElement.getQName();
            }
        }
        return null;
    }
    
    private AxisOperation findRealOperationAction(MessageContext msgContext) {
        AxisOperation axisOperation = null;
        QName firstBodyElement = getFirstBodyElement(msgContext.getEnvelope());
        if (firstBodyElement != null) {
            AxisService service = msgContext.getAxisService();
            axisOperation = service.getOperationByMessageElementQName(firstBodyElement);
        }
        return axisOperation;            
    }
    
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        AxisOperation op = msgContext.getAxisOperation();       
        if (!msgContext.isServerSide() && op != null && isAnonymousOperation(op)) {
            op = findRealOperationAction(msgContext);         
            if (op != null) {
                msgContext.setAxisOperation(op);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Anonymous operation detected. Replaced with real operation: " + op);
                }
            }
        }                
        return InvocationResponse.CONTINUE;
    }
}
