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


package org.apache.axis2.engine;

import java.util.Iterator;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This the base class for all dispatchers. A dispatcher's task is
 * to find the service for an incoming SOAP message.
 * <p/>
 * In Axis2, a chain of dispatchers is setup. Each tries to
 * dispatch and return without throwing an exception, in case it fails
 * to find the service or operation. Dispatchers look for services, operations,
 * or both.
 * 
 */
public abstract class AbstractDispatcher extends AbstractHandler {

    /**
     * Field NAME
     */
    public static final String NAME = "AbstractDispatcher";
    private static final Log log = LogFactory.getLog(AbstractDispatcher.class);

    public AbstractDispatcher() {
        init(new HandlerDescription(NAME));
    }

    /**
     * Called by Axis Engine to find the operation.
     *
     * @param service
     * @param messageContext
     * @return Returns AxisOperation.
     * @throws AxisFault
     */
    public abstract AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault;

    /**
     * Called by Axis Engine to find the service.
     *
     * @param messageContext
     * @return Returns AxisService.
     * @throws AxisFault
     */
    public abstract AxisService findService(MessageContext messageContext) throws AxisFault;

    public abstract void initDispatcher();

    /**
     * @param msgctx
     * @throws org.apache.axis2.AxisFault
     */
    public InvocationResponse invoke(MessageContext msgctx) throws AxisFault {
        AxisService axisService = msgctx.getAxisService();

        if (axisService == null) {
            axisService = findService(msgctx);

            if (axisService != null) {
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug(msgctx.getLogIDString() + " " + Messages.getMessage("servicefound",
                                                                                  axisService.getName()));
                }
                msgctx.setAxisService(axisService);
            }
        }

        if ((axisService != null) && (msgctx.getAxisOperation() == null)) {
            AxisOperation axisOperation = findOperation(axisService, msgctx);

            if (axisOperation != null) {
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug(msgctx.getLogIDString() + " " + Messages.getMessage("operationfound",
                                                                                  axisOperation
                                                                                          .getName().getLocalPart()));
                }

                msgctx.setAxisOperation(axisOperation);
                //setting axisMessage into messageContext
                msgctx.setAxisMessage(axisOperation.getMessage(
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE));
                AxisEndpoint axisEndpoint =
                        (AxisEndpoint) msgctx.getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);

                if (axisEndpoint != null) {
                    AxisBinding axisBinding = axisEndpoint.getBinding();
					AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisBinding
							.getChild(axisOperation.getName());
					if (axisBindingOperation == null) {
						String localName = axisOperation.getName()
								.getLocalPart();
						AxisBindingOperation bindingOp = null;
						for (Iterator<AxisBindingOperation> iterator = axisBinding.getChildren(); iterator
								.hasNext();) {
							bindingOp = (AxisBindingOperation) iterator.next();
							if (localName.equals(bindingOp.getName().getLocalPart())) {
								axisBindingOperation = bindingOp;
								break;
							}
						}
					}
					 if (axisBindingOperation != null) {
						msgctx
								.setProperty(
										Constants.AXIS_BINDING_MESSAGE,
										axisBindingOperation
												.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
						msgctx.setProperty(Constants.AXIS_BINDING_OPERATION,
								axisBindingOperation);
					}
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }
}
