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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Dispatches the operation based on the information from the target endpoint URL.
 */
public class RequestURIBasedOperationDispatcher extends AbstractOperationDispatcher {

    public static final String NAME = "RequestURIBasedOperationDispatcher";
    private static final Log log = LogFactory.getLog(RequestURIBasedOperationDispatcher.class);

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findOperation(org.apache.axis2.description.AxisService, org.apache.axis2.context.MessageContext)
     */
    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {

        EndpointReference toEPR = messageContext.getTo();
        if (toEPR != null) {
            String filePart = toEPR.getAddress();
            String operation  = Utils.getOperationName(filePart, service.getName());

            if (operation != null) {
                QName operationName = new QName(operation);
                log.debug(messageContext.getLogIDString() +
                        " Checking for Operation using QName(target endpoint URI fragment) : " +
                        operationName);
                return service.getOperation(operationName);
            } else {
                log.debug(messageContext.getLogIDString() +
                        " Attempted to check for Operation using target endpoint URI, but the operation fragment was missing");
                return null;
            }
        } else {
            log.debug(messageContext.getLogIDString() +
                    " Attempted to check for Operation using null target endpoint URI");
            return null;
        }
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
