/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.core.multitenancy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.dispatchers.AddressingBasedDispatcher;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Axis2 dispatcher will deal with tenant specific requests as well as general requests which
 * contain addressing headers needed for dispatching.
 */
public class MultitenantAddressingBasedDispatcher extends AddressingBasedDispatcher {

    /**
     * Field NAME
     */
    public static final String NAME = "MultitenantAddressingBasedDispatcher";
    private static final Log log = LogFactory.getLog(MultitenantAddressingBasedDispatcher.class);

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }

    /**
     * @param msgctx
     * @throws org.apache.axis2.AxisFault
     * @noinspection MethodReturnOfConcreteClass
     */
    public InvocationResponse invoke(MessageContext msgctx) throws AxisFault {
        InvocationResponse response = InvocationResponse.CONTINUE;

        // first check we can dispatch using the relates to
        if (msgctx.getRelatesTo() != null) {
            String relatesTo = msgctx.getRelatesTo().getValue();

            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(msgctx.getLogIDString() + " " + Messages.getMessage("checkingrelatesto",
                                                                              relatesTo));
            }
            if (relatesTo != null && !"".equals(relatesTo) && (msgctx.getOperationContext() == null)) {
                OperationContext operationContext =
                        msgctx.getConfigurationContext()
                                .getOperationContext(relatesTo);

                if (operationContext != null) //noinspection TodoComment
                {
//                    if(operationContext.isComplete()){
//                        // If the dispatch happens because of the RelatesTo and the mep is complete
//                        // we should throw a more descriptive fault.
//                        throw new AxisFault(Messages.getMessage("duplicaterelatesto",relatesTo));
//                    }
                    msgctx.setAxisOperation(operationContext.getAxisOperation());
                    msgctx.setAxisMessage(operationContext.getAxisOperation().getMessage(
                            WSDLConstants.MESSAGE_LABEL_IN_VALUE));
                    msgctx.setOperationContext(operationContext);
                    msgctx.setServiceContext((ServiceContext) operationContext.getParent());
                    msgctx.setAxisService(
                            ((ServiceContext) operationContext.getParent()).getAxisService());

                    // TODO : Is this necessary here?
                    msgctx.getAxisOperation().registerMessageContext(msgctx, operationContext);

                    msgctx.setServiceGroupContextId(
                            ((ServiceGroupContext) msgctx.getServiceContext().getParent()).getId());

                    if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                        log.debug(msgctx.getLogIDString() +
                                  " Dispatched successfully on the RelatesTo. operation=" +
                                  operationContext.getAxisOperation());
                    }
                }
            }
        }
        //Else we will try to dispatch based on the WS-A Action
        else {
            EndpointReference to = msgctx.getTo();
            if (to == null || to.getAddress().indexOf("/t/") != -1) {
                return InvocationResponse.CONTINUE;
            }
            response = super.invoke(msgctx);
            Object flag = msgctx.getLocalProperty(IS_ADDR_INFO_ALREADY_PROCESSED);
            if (log.isTraceEnabled()) {
                log.trace("invoke: IS_ADDR_INFO_ALREADY_PROCESSED=" + flag);
            }

            if (JavaUtils.isTrueExplicitly(flag)) {
                // If no AxisOperation has been found at the end of the dispatch phase and addressing
                // is in use we should throw an ActionNotSupported Fault, unless we've been told
                // not to do this check (by Synapse, for instance)
                if (JavaUtils.isTrue(msgctx.getProperty(ADDR_VALIDATE_ACTION), true)) {
                    checkAction(msgctx);
                }
            }
        }

        return response;
    }

    /**
     * If addressing was found and the dispatch failed we SHOULD (and hence will) return a
     * WS-Addressing ActionNotSupported fault. This will make more sense once the
     * AddressingBasedDsipatcher is moved into the addressing module
     */
    private void checkAction(MessageContext msgContext) throws AxisFault {
        if ((msgContext.getAxisService() == null) || (msgContext.getAxisOperation() == null)) {
            AddressingFaultsHelper
                    .triggerActionNotSupportedFault(msgContext, msgContext.getWSAAction());
        }
    }
}

