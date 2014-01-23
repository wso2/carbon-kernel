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

package org.apache.axis2.handlers.addressing;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingValidationHandler extends AbstractHandler implements AddressingConstants {

    private static final Log log = LogFactory.getLog(AddressingValidationHandler.class);

    /* (non-Javadoc)
    * @see org.apache.axis2.engine.Handler#invoke(org.apache.axis2.context.MessageContext)
    */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        Object flag = msgContext.getLocalProperty(IS_ADDR_INFO_ALREADY_PROCESSED);
        if (log.isTraceEnabled()) {
            log.trace("invoke: IS_ADDR_INFO_ALREADY_PROCESSED=" + flag);
        }

        if (JavaUtils.isTrueExplicitly(flag)) {
            // Check if the wsa:MessageID is required or not.
            checkMessageIDHeader(msgContext);
            
            // Check that if wsamInvocationPattern flag is in effect that the replyto and faultto are valid.
            if (JavaUtils.isTrue(msgContext.getProperty(ADDR_VALIDATE_INVOCATION_PATTERN), true)) {
                checkWSAMInvocationPattern(msgContext);
            }
        }
        else {
            // Check that if wsaddressing=required that addressing headers were found inbound
            checkUsingAddressing(msgContext);
        }

        return InvocationResponse.CONTINUE;
    }

    /**
     * Check that if the wsaddressing="required" attribute exists on the service definition
     * (or AddressingFeature on the client) or <wsaw:UsingAddressing wsdl:required="true" />
     * was found in the WSDL (provider side only) that WS-Addressing headers were found on
     * the inbound message.
     */
    private void checkUsingAddressing(MessageContext msgContext)
            throws AxisFault {
        String addressingFlag;
        if (!msgContext.isServerSide()) {
            // On client side, get required value from the request message context
            // (set by AddressingConfigurator).
            // We do not use the UsingAddressing required attribute on the
            // client side since it is not generated/processed by java tooling.
            addressingFlag = AddressingHelper.getRequestAddressingRequirementParameterValue(msgContext);
            if (log.isTraceEnabled()) {
                log.trace("checkUsingAddressing: WSAddressingFlag from MessageContext=" + addressingFlag);
            }
        } else {
            // On provider side, get required value from AxisOperation
            // (set by AddressingConfigurator and UsingAddressing WSDL processing).
            AxisDescription ad = msgContext.getAxisService();
            if(msgContext.getAxisOperation()!=null){
        	   ad = msgContext.getAxisOperation();
            }
            addressingFlag =
                AddressingHelper.getAddressingRequirementParemeterValue(ad);
            if (log.isTraceEnabled()) {
                log.trace("checkUsingAddressing: WSAddressingFlag from AxisOperation=" + addressingFlag);
            }
        }
        if (AddressingConstants.ADDRESSING_REQUIRED.equals(addressingFlag)) {
            AddressingFaultsHelper.triggerMessageAddressingRequiredFault(msgContext,
                                                                         AddressingConstants.WSA_ACTION);
        }
    }

    /**
     * Check that if a wsaw:Anonymous value was set on the AxisOperation that the values in the
     * ReplyTo+FaultTo are valid and fault if not.
     */
    private void checkWSAMInvocationPattern(MessageContext msgContext) throws AxisFault {
        String value =
                AddressingHelper.getInvocationPatternParameterValue(msgContext.getAxisOperation());
        if (log.isTraceEnabled()) {
            log.trace("checkWSAMInvocationPattern: value=" + value);
        }
        if(!AddressingConstants.WSAM_INVOCATION_PATTERN_BOTH.equals(value)){
        	if (WSAM_INVOCATION_PATTERN_SYNCHRONOUS.equals(value)) {
        		if (!AddressingHelper.isSyncReplyAllowed(msgContext)) {
        			EndpointReference anonEPR =
        				new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL);
        			msgContext.setReplyTo(anonEPR);
        			msgContext.setFaultTo(anonEPR);
        			AddressingFaultsHelper.triggerOnlyAnonymousAddressSupportedFault(msgContext,
        					AddressingConstants.WSA_REPLY_TO);
        		}
        		if (!AddressingHelper.isSyncFaultAllowed(msgContext)) {
        			EndpointReference anonEPR =
        				new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL);
        			msgContext.setReplyTo(anonEPR);
        			msgContext.setFaultTo(anonEPR);
        			AddressingFaultsHelper.triggerOnlyAnonymousAddressSupportedFault(msgContext,
        					AddressingConstants.WSA_FAULT_TO);
        		}
        	} else if (WSAM_INVOCATION_PATTERN_ASYNCHRONOUS.equals(value)) {
        		if (!AddressingHelper.isReplyRedirected(msgContext)) {
        			AddressingFaultsHelper.triggerOnlyNonAnonymousAddressSupportedFault(msgContext,
        					AddressingConstants.WSA_REPLY_TO);
        		}
        		if (!AddressingHelper.isFaultRedirected(msgContext)) {
        			AddressingFaultsHelper.triggerOnlyNonAnonymousAddressSupportedFault(msgContext,
        					AddressingConstants.WSA_FAULT_TO);
        		}
        	}
        }
    }

    /**
     * Validate that a message id is present when required. The check applied here only applies to
     * WS-Addressing headers that comply with the 2005/08 (final) spec.
     *
     * @param msgContext
     * @throws AxisFault
     * @see AddressingInHandler#checkForMandatoryHeaders
     */
    private void checkMessageIDHeader(MessageContext msgContext) throws AxisFault {
        String namespace = (String)msgContext.getLocalProperty(WS_ADDRESSING_VERSION);
        if (!Final.WSA_NAMESPACE.equals(namespace)) {
            return;
        }

        AxisOperation axisOperation = msgContext.getAxisOperation();
        
        if (axisOperation != null) {
            String mep = axisOperation.getMessageExchangePattern();
            int mepConstant = Utils.getAxisSpecifMEPConstant(mep);
            
            if (mepConstant == WSDLConstants.MEP_CONSTANT_IN_OUT ||
                    mepConstant == WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT ||
                    mepConstant == WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY) {
                String messageId = msgContext.getOptions().getMessageId();
                if (messageId == null || "".equals(messageId)) {
                    AddressingFaultsHelper
                    .triggerMessageAddressingRequiredFault(msgContext, WSA_MESSAGE_ID);
                }
            }
        }
    }
}
