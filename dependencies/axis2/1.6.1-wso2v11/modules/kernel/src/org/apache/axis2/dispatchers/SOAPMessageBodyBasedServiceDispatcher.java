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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SOAPMessageBodyBasedServiceDispatcher extends AbstractServiceDispatcher {

    public static final String NAME = "SOAPMessageBodyBasedServiceDispatcher";
    private static final Log log = LogFactory.getLog(SOAPMessageBodyBasedServiceDispatcher.class);

    public AxisService findService(MessageContext messageContext) throws AxisFault {
        String serviceName = null;
        String localPart = messageContext.getEnvelope().getSOAPBodyFirstElementLocalName();

        if (localPart != null) {
            OMNamespace ns = messageContext.getEnvelope().getSOAPBodyFirstElementNS();

            if (ns != null) {
                String filePart = ns.getNamespaceURI();

                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug(messageContext.getLogIDString() +
                            "Checking for Service using SOAP message body's first child's namespace : "
                            + filePart);
                }
                String[] values = Utils.parseRequestURLForServiceAndOperation(filePart,
                                                                              messageContext
                                                                                      .getConfigurationContext().getServiceContextPath());

                if (values[0] != null) {
                    serviceName = values[0];

                    AxisConfiguration registry =
                            messageContext.getConfigurationContext().getAxisConfiguration();

                    return registry.getService(serviceName);
                }
            }
        }

        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
