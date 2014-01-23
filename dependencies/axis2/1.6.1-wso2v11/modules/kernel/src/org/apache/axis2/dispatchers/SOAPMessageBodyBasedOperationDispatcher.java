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

package org.apache.axis2.dispatchers;

import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class SOAPMessageBodyBasedOperationDispatcher extends AbstractOperationDispatcher {

    public static final String NAME = "SOAPMessageBodyBasedOperationDispatcher";
    private static final Log log = LogFactory.getLog(SOAPMessageBodyBasedOperationDispatcher.class);

    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        String localPart = messageContext.getEnvelope().getSOAPBodyFirstElementLocalName();
        if (localPart == null) {
            // Doc/Lit/Bare no arg; see if an operation is registered.
            AxisOperation axisOperation = service.getOperationByMessageElementQName(null);
            return axisOperation;
        }
        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug(messageContext.getLogIDString() +
                    " Checking for Operation using SOAP message body's first child's local name : "
                    + localPart);
        }
        AxisOperation axisOperation = service.getOperation(new QName(localPart));

        if (axisOperation == null) {
            OMNamespace ns = messageContext.getEnvelope().getSOAPBodyFirstElementNS();
            if (ns != null) {
                QName qName = new QName(ns.getNamespaceURI(), localPart);
                axisOperation = service.getOperationByMessageElementQName(qName);
            }
            if (axisOperation == null) {
                QName qName = new QName(localPart);
                axisOperation = service.getOperation(qName);
            }
        }
        return axisOperation;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
