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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class AddressingHelper {

    private static final Log log = LogFactory.getLog(AddressingHelper.class);

    /**
     * Returns true if the ReplyTo address matches one of the supported
     * anonymous urls. If the ReplyTo is not set, anonymous is assumed, per the Final
     * spec. The AddressingInHandler should have set the ReplyTo to non-null in the
     * 2004/08 case to ensure the different semantics. (per AXIS2-885)
     * 
     * According to the WS-Addressing Metadata spec the none URI must not be rejected.
     *
     * @param messageContext
     */
    public static boolean isSyncReplyAllowed(MessageContext messageContext) {
        EndpointReference replyTo = messageContext.getReplyTo();
        if (replyTo == null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(messageContext.getLogIDString() +
                        " isSyncReplyAllowed: ReplyTo is null. Returning true");
            }
            return true;
        } else {
            return replyTo.hasAnonymousAddress() || replyTo.hasNoneAddress();
        }
    }

    /**
     * Returns true if the FaultTo address matches one of the supported
     * anonymous urls. If the FaultTo is not set, the ReplyTo is checked per the
     * spec.
     * 
     * According to the WS-Addressing Metadata spec the none URI must not be rejected.
     *
     * @param messageContext
     * @see #isSyncReplyAllowed(org.apache.axis2.context.MessageContext)
     */
    public static boolean isSyncFaultAllowed(MessageContext messageContext) {
        EndpointReference faultTo = messageContext.getFaultTo();
        if (faultTo == null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(messageContext.getLogIDString() +
                        " isSyncFaultAllowed: FaultTo is null. Returning isSyncReplyAllowed");
            }
            return isSyncReplyAllowed(messageContext);
        } else {
            return faultTo.hasAnonymousAddress() || faultTo.hasNoneAddress();
        }
    }

    /**
     * Returns true if the ReplyTo address does not match one of the supported
     * anonymous urls. If the ReplyTo is not set, anonymous is assumed, per the Final
     * spec. The AddressingInHandler should have set the ReplyTo to non-null in the
     * 2004/08 case to ensure the different semantics. (per AXIS2-885)
     *
     * @param messageContext
     */
    public static boolean isReplyRedirected(MessageContext messageContext) {
        EndpointReference replyTo = messageContext.getReplyTo();
        if (replyTo == null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(messageContext.getLogIDString() +
                        " isReplyRedirected: ReplyTo is null. Returning false");
            }
            return false;
        } else {
            return !replyTo.hasAnonymousAddress();
        }
    }

    /**
     * Returns true if the FaultTo address does not match one of the supported
     * anonymous urls. If the FaultTo is not set, the ReplyTo is checked per the
     * spec.
     *
     * @param messageContext
     * @see #isReplyRedirected(org.apache.axis2.context.MessageContext)
     */
    public static boolean isFaultRedirected(MessageContext messageContext) {
        EndpointReference faultTo = messageContext.getFaultTo();
        if (faultTo == null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(messageContext.getLogIDString() +
                        " isFaultRedirected: FaultTo is null. Returning isReplyRedirected");
            }
            return isReplyRedirected(messageContext);
        } else {
            return !faultTo.hasAnonymousAddress();
        }
    }

    /**
     * If the inbound FaultTo header was invalid and caused a fault, the fault should not be
     * sent to it.
     *
     * @return true if the fault should be sent to the FaultTo
     */
    public static boolean shouldSendFaultToFaultTo(MessageContext messageContext) {
        // there are some information  that the fault thrower wants to pass to the fault path.
        // Means that the fault is a ws-addressing one hence use the ws-addressing fault action.
        Object faultInfoForHeaders =
                messageContext.getLocalProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        // if the exception is due to a problem in the faultTo header itself, we can not use those
        // fault informatio to send the error. Try to send using replyTo, leave it to transport
        boolean doNotSendFaultUsingFaultTo = false;
        if (faultInfoForHeaders != null) {
            // TODO: This should probably store a QName instead of a String.. currently we rely on prefix string matching!!
            String problemHeaderName = (String) ((Map) faultInfoForHeaders)
                    .get(AddressingConstants.Final.FAULT_HEADER_PROB_HEADER_QNAME);
            doNotSendFaultUsingFaultTo = (problemHeaderName != null && (AddressingConstants
                    .WSA_DEFAULT_PREFIX + ":" + AddressingConstants.WSA_FAULT_TO)
                    .equals(problemHeaderName));
        }
        return !doNotSendFaultUsingFaultTo;
    }

    public static String getAddressingRequirementParemeterValue(AxisDescription axisDescription){
    	String value = "";
        if (axisDescription != null) {
            value = Utils.getParameterValue(
            		axisDescription.getParameter(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER));
            if(value !=null){
            	value = value.trim();
            }
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("getAddressingRequirementParemeterValue: value: '" + value + "'");
            }
        }

        if (value == null || "".equals(value)) {
            value = AddressingConstants.ADDRESSING_UNSPECIFIED;
        }
        return value;
    }

    public static String getRequestAddressingRequirementParameterValue(MessageContext response){
        String value = "";
        if (response != null) {
            HashMap<String,MessageContext> operationMessageContexts = response.getOperationContext().getMessageContexts();
            for(MessageContext messageContext : operationMessageContexts.values()) {
                // Assumes at most 2 messages on operation, if there is more than 2 it
                // will use the value from the first message it gets that is != response
                if(!messageContext.equals(response)) {
                    value = (String) messageContext.getProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
                    if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                        log.debug("getRequestAddressingRequirementParameterValue: got value from MessageContext "+messageContext+", value: '" + value + "'");
                    }
                    break;
                }
            }
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("getRequestAddressingRequirementParameterValue: value: '" + value + "'");
            }
        }

        if (value == null || "".equals(value)) {
            value = AddressingConstants.ADDRESSING_UNSPECIFIED;
        }
        return value;
    }
    
    /**
     * Extract the parameter representing the Anonymous flag from the AxisOperation
     * and return the String value. Return the default of "optional" if not specified.
     *
     * @param axisOperation
     */
    public static String getInvocationPatternParameterValue(AxisOperation axisOperation) {
        String value = "";
        if (axisOperation != null) {
            value = Utils.getParameterValue(
                    axisOperation.getParameter(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME));
            if(value !=null){
            	value = value.trim();
            }
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("getInvocationPatternParameterValue: value: '" + value + "'");
            }
        }

        if (value == null || "".equals(value)) {
            value = AddressingConstants.WSAM_INVOCATION_PATTERN_BOTH;
        }
        return value;
    }

    /**
     * Set the value of an existing unlocked Parameter representing Anonymous or add a new one if one
     * does not exist. If a locked Parameter of the same name already exists the method will trace and
     * return.
     *
     * @param axisOperation
     * @param value
     */
    public static void setInvocationPatternParameterValue(AxisOperation axisOperation, String value) {
        if (value == null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("setInvocationPatternParameterValue: value passed in is null. return");
            }
            return;
        }

        Parameter param =
                axisOperation.getParameter(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME);
        // If an existing parameter exists
        if (param != null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("setInvocationPatternParameterValue: Parameter already exists");
            }
            // and is not locked
            if (!param.isLocked()) {
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug("setInvocationPatternParameterValue: Parameter not locked. Setting value: " +
                            value);
                }
                // set the value
                param.setValue(value);
            }
        } else {
            // otherwise, if no Parameter exists
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("setInvocationPatternParameterValue: Parameter does not exist");
            }
            // Create new Parameter with correct name/value
            param = new Parameter();
            param.setName(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME);
            param.setValue(value);
            try {
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug("setInvocationPatternParameterValue: Adding parameter with value: " + value);
                }
                // and add it to the AxisOperation object
                axisOperation.addParameter(param);
            } catch (AxisFault af) {
                // This should not happen. AxisFault is only ever thrown when a locked Parameter
                // of the same name already exists and this should be dealt with by the outer
                // if statement.
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug(
                            "setInvocationPatternParameterValue: addParameter failed: " + af.getMessage());
                }
            }
        }
    }

	public static void setAddressingRequirementParemeterValue(AxisDescription axisDescription, String value) {
		if (value == null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("setAddressingRequirementParemeterValue: value passed in is null. return");
            }
            return;
        }

        Parameter param =
        	axisDescription.getParameter(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
        // If an existing parameter exists
        if (param != null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("setAddressingRequirementParemeterValue: Parameter already exists");
            }
            // and is not locked
            if (!param.isLocked()) {
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug("setAddressingRequirementParemeterValue: Parameter not locked. Setting value: " +
                            value);
                }
                // set the value
                param.setValue(value);
            }
        } else {
            // otherwise, if no Parameter exists
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("setAddressingRequirementParemeterValue: Parameter does not exist");
            }
            // Create new Parameter with correct name/value
            param = new Parameter();
            param.setName(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
            param.setValue(value);
            try {
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug("setAddressingRequirementParemeterValue: Adding parameter with value: " + value);
                }
                // and add it to the AxisOperation object
                axisDescription.addParameter(param);
            } catch (AxisFault af) {
                // This should not happen. AxisFault is only ever thrown when a locked Parameter
                // of the same name already exists and this should be dealt with by the outer
                // if statement.
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug(
                            "setAddressingRequirementParemeterValue: addParameter failed: " + af.getMessage());
                }
            }
        }
	}
}
