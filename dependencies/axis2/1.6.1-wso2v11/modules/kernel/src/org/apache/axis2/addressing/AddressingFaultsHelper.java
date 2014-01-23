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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.addressing.i18n.AddressingMessages;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class AddressingFaultsHelper {

    private static final Log log = LogFactory.getLog(AddressingFaultsHelper.class);

    /**
     * Build an understanndable fault string for the given faultCode and wsa:FaultDetail info.
     * Should really use a message bundle.
     *
     * @param faultCodeLocalName
     * @param faultDetail
     * @return
     */
    public static String getMessageForAxisFault(String faultCodeLocalName, String faultDetail) {
        String result = null;

        if (Submission.FAULT_INVALID_HEADER.equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages
                        .getMessage("specificInvalidAddressingHeader", faultDetail);
            } else {
                result = AddressingMessages.getMessage("invalidAddressingHeader");
            }
        } else if (Final.FAULT_INVALID_HEADER.equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages
                        .getMessage("specificInvalidAddressingHeader", faultDetail);
            } else {
                result = AddressingMessages.getMessage("invalidAddressingHeader");
            }
        } else if ("InvalidCardinality".equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages.getMessage("specificInvalidCardinality", faultDetail);
            } else {
                result = AddressingMessages.getMessage("invalidCardinality");
            }
        } else if ("MissingAddressInEPR".equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages.getMessage("specificMissingAddressInEPR", faultDetail);
            } else {
                result = AddressingMessages.getMessage("missingAddressInEPR");
            }
        } else if ("DuplicateMessageID".equals(faultCodeLocalName)) {
            result = AddressingMessages.getMessage("duplicateMessageID");
        } else if ("ActionMismatch".equals(faultCodeLocalName)) {
            result = AddressingMessages.getMessage("actionMismatch");
        } else if (Final.FAULT_ONLY_ANONYMOUS_ADDRESS_SUPPORTED.equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages
                        .getMessage("specificOnlyAnonymousSupported", faultDetail);
            } else {
                result = AddressingMessages.getMessage("onlyAnonymousSupported");
            }
        } else if (Final.FAULT_ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED.equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages.getMessage("specificOnlyNonAnonSupported", faultDetail);
            } else {
                result = AddressingMessages.getMessage("onlyNonAnonSupported");
            }
        } else if (Submission.FAULT_ADDRESSING_HEADER_REQUIRED.equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages
                        .getMessage("specificAddressingHeaderRequired", faultDetail);
            } else {
                result = AddressingMessages.getMessage("addressingHeaderRequired");
            }
        } else if (Final.FAULT_ADDRESSING_HEADER_REQUIRED.equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages
                        .getMessage("specificAddressingHeaderRequired", faultDetail);
            } else {
                result = AddressingMessages.getMessage("addressingHeaderRequired");
            }
        } else
        if (AddressingConstants.FAULT_ADDRESSING_DESTINATION_UNREACHABLE.equals(faultCodeLocalName))
        {
            if (faultDetail != null) {
                result = AddressingMessages
                        .getMessage("specificDestinationUnreachable", faultDetail);
            } else {
                result = AddressingMessages.getMessage("destinationUnreachable");
            }
        } else if (AddressingConstants.FAULT_ACTION_NOT_SUPPORTED.equals(faultCodeLocalName)) {
            if (faultDetail != null) {
                result = AddressingMessages.getMessage("specificActionNotRecognised", faultDetail);
            } else {
                result = AddressingMessages.getMessage("actionNotRecognised");
            }
        }

        return result;
    }

    //    wsa:InvalidAddressingHeader [Reason] the string: "A header representing a Message Addressing Property is not valid and the message cannot be processed"
    //      wsa:InvalidAddress
    //      wsa:InvalidEPR
    public static void triggerInvalidEPRFault(MessageContext messageContext,
            String incorrectHeaderName)
    throws AxisFault {
        log.warn("triggerInvalidEPRFault: messageContext: " + messageContext +
                " incorrectHeaderName: " + incorrectHeaderName);
        String namespace =
            (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (Submission.WSA_NAMESPACE.equals(namespace)) {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                    AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                    incorrectHeaderName, Submission.FAULT_INVALID_HEADER,
                    null, AddressingMessages.getMessage(
                    "spec.submission.FAULT_INVALID_HEADER_REASON"));
        } else {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                    AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                    incorrectHeaderName, Final.FAULT_INVALID_HEADER,
                    "InvalidEPR",
                    AddressingMessages.getMessage(
                    "spec.final.FAULT_INVALID_HEADER_REASON"));
        }
    }

    //      wsa:InvalidCardinality
    public static void triggerInvalidCardinalityFault(MessageContext messageContext,
                                                      String incorrectHeaderName) throws AxisFault {
        log.warn("triggerInvalidCardinalityFault: messageContext: " + messageContext +
                 " incorrectHeaderName: " + incorrectHeaderName);
        String namespace =
                (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (Submission.WSA_NAMESPACE.equals(namespace)) {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                                           incorrectHeaderName, Submission.FAULT_INVALID_HEADER,
                                                                null, AddressingMessages.getMessage(
                    "spec.submission.FAULT_INVALID_HEADER_REASON"));
        } else {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                                           incorrectHeaderName, Final.FAULT_INVALID_HEADER,
                                                                "InvalidCardinality",
                                                                AddressingMessages.getMessage(
                                                                        "spec.final.FAULT_INVALID_HEADER_REASON"));
        }
    }

    //      wsa:MissingAddressInEPR
    public static void triggerMissingAddressInEPRFault(MessageContext messageContext,
                                                       String incorrectHeaderName)
            throws AxisFault {
        log.warn("triggerMissingAddressInEPRFault: messageContext: " + messageContext +
                    " incorrectHeaderName: " + incorrectHeaderName);
        String namespace =
                (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (Submission.WSA_NAMESPACE.equals(namespace)) {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                                           incorrectHeaderName, Submission.FAULT_INVALID_HEADER,
                                                                null, AddressingMessages.getMessage(
                    "spec.submission.FAULT_INVALID_HEADER_REASON"));
        } else {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                                           incorrectHeaderName, Final.FAULT_INVALID_HEADER,
                                                                "MissingAddressInEPR",
                                                                AddressingMessages.getMessage(
                                                                        "spec.final.FAULT_INVALID_HEADER_REASON"));
        }
    }

    //      wsa:DuplicateMessageID
    //      wsa:ActionMismatch
    public static void triggerActionMismatchFault(MessageContext messageContext, String soapAction, String wsaAction) throws AxisFault {
        log.warn("triggerActionMismatchFault: messageContext: " + messageContext+" soapAction="+soapAction+" wsaAction="+wsaAction);
        String namespace =
                (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (Submission.WSA_NAMESPACE.equals(namespace)) {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":Action",
                                   Submission.FAULT_INVALID_HEADER, null,
                                   AddressingMessages.getMessage(
                                           "spec.submission.FAULT_INVALID_HEADER_REASON"));
        } else {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":Action",
                                   Final.FAULT_INVALID_HEADER, "ActionMismatch",
                                   AddressingMessages.getMessage(
                                           "spec.final.FAULT_INVALID_HEADER_REASON"));
        }
    }

    //      wsa:OnlyAnonymousAddressSupported
    public static void triggerOnlyAnonymousAddressSupportedFault(MessageContext messageContext,
                                                                 String incorrectHeaderName)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("triggerOnlyAnonymousAddressSupportedFault: messageContext: " +
                    messageContext + " incorrectHeaderName: " + incorrectHeaderName);
        }
        String namespace =
                (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (Submission.WSA_NAMESPACE.equals(namespace)) {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                                           incorrectHeaderName, Submission.FAULT_INVALID_HEADER,
                                                                null, AddressingMessages.getMessage(
                    "spec.submission.FAULT_INVALID_HEADER_REASON"));
        } else {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                                           incorrectHeaderName, Final.FAULT_INVALID_HEADER,
                                                                Final.FAULT_ONLY_ANONYMOUS_ADDRESS_SUPPORTED,
                                                                AddressingMessages.getMessage(
                                                                        "spec.final.FAULT_INVALID_HEADER_REASON"));
        }
    }

    //      wsa:OnlyNonAnonymousAddressSupported
    public static void triggerOnlyNonAnonymousAddressSupportedFault(MessageContext messageContext,
                                                                    String incorrectHeaderName)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("triggerOnlyNonAnonymousAddressSupportedFault: messageContext: " +
                    messageContext + " incorrectHeaderName: " + incorrectHeaderName);
        }
        String namespace =
                (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (Submission.WSA_NAMESPACE.equals(namespace)) {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                                           incorrectHeaderName, Submission.FAULT_INVALID_HEADER,
                                                                null, AddressingMessages.getMessage(
                    "spec.submission.FAULT_INVALID_HEADER_REASON"));
        } else {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" +
                                           incorrectHeaderName, Final.FAULT_INVALID_HEADER,
                                                                Final.FAULT_ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED,
                                                                AddressingMessages.getMessage(
                                                                        "spec.final.FAULT_INVALID_HEADER_REASON"));
        }
    }

    //    wsa:MessageAddressingHeaderRequired [Reason] the string: "A required header representing a Message Addressing Property is not present"
    public static void triggerMessageAddressingRequiredFault(MessageContext messageContext,
                                                             String missingHeaderName)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("triggerMessageAddressingRequiredFault: messageContext: " + messageContext +
                    " missingHeaderName: " + missingHeaderName);
        }
        String namespace =
                (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (Submission.WSA_NAMESPACE.equals(namespace)) {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" + missingHeaderName,
                                   Submission.FAULT_ADDRESSING_HEADER_REQUIRED, null,
                                   AddressingMessages.getMessage(
                                           "spec.submission.FAULT_ADDRESSING_HEADER_REQUIRED_REASON"));
        } else {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME,
                                   AddressingConstants.WSA_DEFAULT_PREFIX + ":" + missingHeaderName,
                                   Final.FAULT_ADDRESSING_HEADER_REQUIRED, null,
                                   AddressingMessages.getMessage(
                                           "spec.final.FAULT_ADDRESSING_HEADER_REQUIRED_REASON"));
        }
    }

    //    wsa:DestinationUnreachable [Reason] the string: "No route can be determined to reach [destination]"
    public static void triggerDestinationUnreachableFault(MessageContext messageContext,
                                                          String address) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("triggerDestinationUnreachableFault: messageContext: " + messageContext +
                    " address: " + address);
        }
        String namespace =
                (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (Submission.WSA_NAMESPACE.equals(namespace)) {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_IRI, null,
                                   AddressingConstants.FAULT_ADDRESSING_DESTINATION_UNREACHABLE,
                                   null,
                                   AddressingMessages.getMessage(
                                           "spec.submission.FAULT_ADDRESSING_DESTINATION_UNREACHABLE_REASON"));
        } else {
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_IRI, address,
                                   AddressingConstants.FAULT_ADDRESSING_DESTINATION_UNREACHABLE,
                                   null,
                                   AddressingMessages.getMessage(
                                           "spec.final.FAULT_ADDRESSING_DESTINATION_UNREACHABLE_REASON"));
        }
    }

    //    wsa:ActionNotSupported [Reason] the string: "The [action] cannot be processed at the receiver"
    public static void triggerActionNotSupportedFault(MessageContext messageContext,
                                                      String problemAction) throws AxisFault {
        log.warn("triggerActionNotSupportedFault: messageContext: " + messageContext +
                 " problemAction: " + problemAction);
        triggerAddressingFault(messageContext, Final.FAULT_PROBLEM_ACTION_NAME, problemAction,
                               AddressingConstants.FAULT_ACTION_NOT_SUPPORTED, null,
                               AddressingMessages.getMessage(
                                       "spec.FAULT_ACTION_NOT_SUPPORTED_REASON"));
    }

    //    wsa:EndpointUnavailable [Reason] the string "The endpoint is unable to process the message at this time"

    private static void triggerAddressingFault(MessageContext messageContext,
                                               String faultInformationKey,
                                               Object faultInformationValue, String faultcode,
                                               String faultSubcode, String faultReason)
            throws AxisFault {
        Map faultInformation =
                (Map)messageContext.getLocalProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        if (faultInformation == null) {
            faultInformation = new HashMap();
            messageContext.setProperty(Constants.FAULT_INFORMATION_FOR_HEADERS, faultInformation);
        }

        faultInformation.put(faultInformationKey, faultInformationValue);

        if (messageContext.isSOAP11()) {
            faultcode = (faultSubcode != null) ? faultSubcode : faultcode;
        } else {
            setFaultCode(messageContext, faultcode, faultSubcode);
        }

        OperationContext oc = messageContext.getOperationContext();
        if (oc != null) {
            oc.setProperty(Constants.Configuration.SEND_STACKTRACE_DETAILS_WITH_FAULTS, "false");
        }

        messageContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES,
                                   Boolean.FALSE);
        String namespace =
                (String)messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        throw new AxisFault(faultReason, new QName(namespace, faultcode,
                                                   AddressingConstants.WSA_DEFAULT_PREFIX));
    }

    private static void setFaultCode(MessageContext messageContext, String faultCode,
                                     String faultSubCode) {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP12Factory();
        SOAPFaultCode soapFaultCode = soapFac.createSOAPFaultCode();
        if (messageContext.isSOAP11()) {
            soapFaultCode.setText(faultCode);
        } else {
            SOAPFaultValue soapFaultValue = soapFac.createSOAPFaultValue(soapFaultCode);
            soapFaultValue.setText(SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX + ":" +
                    SOAP12Constants.FAULT_CODE_SENDER);
            SOAPFaultSubCode soapFaultSubCode = soapFac.createSOAPFaultSubCode(soapFaultCode);
            SOAPFaultValue soapFaultSubcodeValue = soapFac.createSOAPFaultValue(soapFaultSubCode);

            if (faultCode != null){
                String namespace =
                        (String) messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
                if (namespace == null) {
                    namespace = Final.WSA_NAMESPACE;
                }
                OMNamespace wsaNS = soapFac.createOMNamespace(namespace,
                        AddressingConstants.WSA_DEFAULT_PREFIX);
                soapFaultSubcodeValue.declareNamespace(wsaNS);
                soapFaultSubcodeValue
                        .setText(AddressingConstants.WSA_DEFAULT_PREFIX + ":" + faultCode);
            }

            if (faultSubCode != null) {
                SOAPFaultSubCode soapFaultSubCode2 =
                        soapFac.createSOAPFaultSubCode(soapFaultSubCode);
                SOAPFaultValue soapFaultSubcodeValue2 =
                        soapFac.createSOAPFaultValue(soapFaultSubCode2);
                String namespace =
                        (String) messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
                if (namespace == null) {
                    namespace = Final.WSA_NAMESPACE;
                }
                OMNamespace wsaNS = soapFac.createOMNamespace(namespace,
                        AddressingConstants.WSA_DEFAULT_PREFIX);
                soapFaultSubcodeValue2.declareNamespace(wsaNS);
                soapFaultSubcodeValue2
                        .setText(AddressingConstants.WSA_DEFAULT_PREFIX + ":" + faultSubCode);
            }
        }
        messageContext.setProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, soapFaultCode);
    }

    public static OMElement getDetailElementForAddressingFault(MessageContext messageContext,
                                                               OMNamespace addressingNamespaceObject) {
        Map faultInfo = (Map)messageContext.getLocalProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        OMElement problemDetail = null;
        if (faultInfo != null) {
            String faultyHeaderQName = (String)faultInfo.get(Final.FAULT_HEADER_PROB_HEADER_QNAME);
            String faultyAction = (String)faultInfo.get(Final.FAULT_PROBLEM_ACTION_NAME);
            String faultyAddress = (String)faultInfo.get(Final.FAULT_HEADER_PROB_IRI);
            if (faultyAddress != null && !"".equals(faultyAddress)) {
                problemDetail = messageContext.getEnvelope().getOMFactory().createOMElement(
                        Final.FAULT_HEADER_PROB_IRI, addressingNamespaceObject);
                problemDetail.setText(faultyAddress);
            }
            if (faultyAction != null && !"".equals(faultyAction)) {
                problemDetail = messageContext.getEnvelope().getOMFactory().createOMElement(
                        Final.FAULT_PROBLEM_ACTION_NAME, addressingNamespaceObject);
                OMElement probH2 = messageContext.getEnvelope().getOMFactory().createOMElement(
                        AddressingConstants.WSA_ACTION, addressingNamespaceObject, problemDetail);
                probH2.setText(faultyAction);
            }
            if (faultyHeaderQName != null && !"".equals(faultyHeaderQName)) {
                problemDetail = messageContext.getEnvelope().getOMFactory().createOMElement(
                        Final.FAULT_HEADER_PROB_HEADER_QNAME, addressingNamespaceObject);
                problemDetail.setText(faultyHeaderQName);
            }
        }
        return problemDetail;
    }
}