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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class ActionBasedOperationDispatcher extends AbstractOperationDispatcher {

    public static final String NAME = "ActionBasedOperationDispatcher";
    private static final Log log = LogFactory.getLog(ActionBasedOperationDispatcher.class);

    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        String action = messageContext.getSoapAction();

        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug(messageContext.getLogIDString() + " Checking for Operation using Action : " +
                    action);
        }
        if (action != null) {
            // REVIEW: Should we FIRST try to find an operation that explicitly mapped this 
            // SOAPAction as an alias by calling: 
            // AxisOperation op = service.getOperationByAction(action);
            // And THEN, if we didn't find an explicit mapping of this action, see if there's an 
            // operation that has the same name as the action, and route to that by calling:
            // service.getOperationBySOAPAction(action);
            AxisOperation op = service.getOperationBySOAPAction(action);
            if (op == null) {
                op = service.getOperationByAction(action);
            }
            
            /*
             * HACK: Please remove this when we add support for custom action
             * uri
             */
            if ((op == null) && (action.lastIndexOf('/') != -1)) {
                op = service.getOperation(new QName(action.substring(action.lastIndexOf('/'),
                                                                     action.length())));
            }
            
            return op;
        }

        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
