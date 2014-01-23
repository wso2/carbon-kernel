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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.handlers.AbstractTemplatedHandler;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

public class AddressingInHandler extends AbstractTemplatedHandler implements AddressingConstants {

    private static final int TO_FLAG = 1, FROM_FLAG = 2, REPLYTO_FLAG = 3,
            FAULTO_FLAG = 4, MESSAGEID_FLAG = 6, ACTION_FLAG = 0;

    private static final Log log = LogFactory.getLog(AddressingInHandler.class);

    private boolean disableRefparamExtract = false;
    private AxisConfiguration configuration = null;
    private RolePlayer rolePlayer = null;

    public void init(HandlerDescription handlerdesc){
    	super.init(handlerdesc);
    	// check whether to process reference parameters.
        Parameter param = handlerdesc.getParameter(DISABLE_REF_PARAMETER_EXTRACT);
        String value = Utils.getParameterValue(param);
        disableRefparamExtract = JavaUtils.isTrueExplicitly(value);

        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug("disableRefparamExtract=" + disableRefparamExtract);
        }
    }
    
    public boolean shouldInvoke(MessageContext msgContext) throws AxisFault {
        //Set the defaults on the message context.
        msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
        msgContext.setProperty(IS_ADDR_INFO_ALREADY_PROCESSED, Boolean.FALSE);

        //Determine if we want to ignore addressing headers. This parameter must
        //be retrieved from the message context because it's value can vary on a
        //per service basis.
        Parameter disableParam = msgContext.getParameter(DISABLE_ADDRESSING_FOR_IN_MESSAGES);
        Object disableProperty = msgContext.getProperty(DISABLE_ADDRESSING_FOR_IN_MESSAGES);
        String value = Utils.getParameterValue(disableParam);
        if (JavaUtils.isTrueExplicitly(value)) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(
                        "The AddressingInHandler has been disabled. No further processing will take place.");
            }
            return false;         
        } else if (JavaUtils.isTrueExplicitly(disableProperty)) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(
                        "The AddressingInHandler has been disabled. No further processing will take place.");
            }
            return false;
        }

        // if there are not headers put a flag to disable addressing temporary
        SOAPHeader header = msgContext.getEnvelope().getHeader();
        return header != null;
    }
    
    public InvocationResponse doInvoke(MessageContext msgContext) throws AxisFault {
        SOAPHeader header = msgContext.getEnvelope().getHeader();
        if(configuration == null){
        	AxisConfiguration conf = msgContext.getConfigurationContext().getAxisConfiguration();
        	rolePlayer = (RolePlayer)conf.getParameterValue(Constants.SOAP_ROLE_PLAYER_PARAMETER);
        	configuration = conf;
        }
        
        // check whether another handler has explicitly set which addressing namespace to expect.
        Iterator iterator = null;
        String namespace = (String) msgContext.getProperty(WS_ADDRESSING_VERSION);
        
        // check whether the service is configured to use a particular version of WS-Addressing,
        // e.g. via JAX-WS annotations.
        if (namespace == null) {
            Parameter namespaceParam = msgContext.getParameter(WS_ADDRESSING_VERSION);
            namespace = Utils.getParameterValue(namespaceParam);
        }
        
        if (namespace == null) {
            namespace = Final.WSA_NAMESPACE;
            iterator = header.getHeadersToProcess(rolePlayer, namespace);

            if (!iterator.hasNext()) {
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug("No headers present corresponding to " + namespace);
                }

                namespace = Submission.WSA_NAMESPACE;
                iterator = header.getHeadersToProcess(rolePlayer, namespace);
            }
        }
        else if (Final.WSA_NAMESPACE.equals(namespace) || Submission.WSA_NAMESPACE.equals(namespace)) {
            iterator = header.getHeadersToProcess(rolePlayer, namespace);
            
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("The preconfigured namespace is, , " + namespace);
            }            
        }
        else {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("The specified namespace is not supported by this handler, " + namespace);
            }            

            return InvocationResponse.CONTINUE;
        }

        if (iterator.hasNext()) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(namespace +
                          " headers present in the SOAP message. Starting to process ...");
            }

            //Need to set these properties here, before we extract the WS-Addressing
            //information, in case we throw a fault.
            msgContext.setProperty(WS_ADDRESSING_VERSION, namespace);
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);
            
            extractAddressingInformation(msgContext, iterator, namespace);
            
            // check for reference parameters
            if (!disableRefparamExtract) {
                extractToEprReferenceParameters(msgContext.getTo(), header, namespace);
            }
            
            //We should only get to this point if we haven't thrown a fault.
            msgContext.setProperty(IS_ADDR_INFO_ALREADY_PROCESSED, Boolean.TRUE);
        }
        else {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("No headers present corresponding to " + namespace);
            }
        }

        return InvocationResponse.CONTINUE;
    }

    /**
     * Pull addressing headers out from the SOAP message.
     *
     * @param messageContext the active MessageContext
     * @param headers an Iterator over the addressing headers targeted to me
     * @param namespace the addressing namespace
     * @throws AxisFault if an error occurs
     */
    private void extractAddressingInformation(MessageContext messageContext, Iterator headers,
                                         String namespace)
            throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();

        ArrayList duplicateHeaderNames = new ArrayList(1); // Normally will not be used for more than 1 header

        ArrayList relatesToHeaders = null;
        SOAPHeaderBlock actionBlock = null, toBlock = null, messageIDBlock = null, replyToBlock =
                null, faultToBlock = null, fromBlock = null;

        // Per the SOAP Binding spec "headers with an incorrect cardinality MUST NOT be used" So these variables
        // are used to keep track of invalid cardinality headers so they are not deserialised.
        boolean[] ignoreHeaders = new boolean[7];
        boolean[] checkedHeaderNames = new boolean[7];

        // First pass just check for duplicates
        while (headers.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock)headers.next();
            String localName = soapHeaderBlock.getLocalName();
            if (WSA_ACTION.equals(localName)) {
                actionBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_ACTION, ACTION_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_TO.equals(localName)) {
                toBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_TO, TO_FLAG, checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_MESSAGE_ID.equals(localName)) {
                messageIDBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_MESSAGE_ID, MESSAGEID_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_REPLY_TO.equals(localName)) {
                replyToBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_REPLY_TO, REPLYTO_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_FAULT_TO.equals(localName)) {
                faultToBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_FAULT_TO, FAULTO_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_FROM.equals(localName)) {
                fromBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_FROM, FROM_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_RELATES_TO.equals(localName)) {
                if (relatesToHeaders == null) {
                    relatesToHeaders = new ArrayList(1);
                }
                relatesToHeaders.add(soapHeaderBlock);
            }
        }

        if (actionBlock != null && !ignoreHeaders[ACTION_FLAG]) {
            extractActionInformation(actionBlock, messageContext);
        }
        if (toBlock != null && !ignoreHeaders[TO_FLAG]) {
            extractToEPRInformation(toBlock,
                                    messageContextOptions,
                                    namespace);
        }
        if (messageIDBlock != null && !ignoreHeaders[MESSAGEID_FLAG]) {
            extractMessageIDInformation(messageIDBlock, messageContext);
        }
        if (relatesToHeaders != null) {
            for (int i = 0; i < relatesToHeaders.size(); i++) {
                extractRelatesToInformation((SOAPHeaderBlock) relatesToHeaders.get(i),
                                            messageContextOptions);
            }
        }
        if (replyToBlock != null && !ignoreHeaders[REPLYTO_FLAG]) {
            extractReplyToEPRInformation(replyToBlock, namespace, messageContext);
        }
        if (faultToBlock != null && !ignoreHeaders[FAULTO_FLAG]) {
            extractFaultToEPRInformation(faultToBlock, namespace, messageContext);
        }
        if (fromBlock != null && !ignoreHeaders[FROM_FLAG]) {
            extractFromEPRInformation(fromBlock, namespace, messageContext);
        }

        // Now that all the valid wsa headers have been read, throw an exception if there was an invalid cardinality
        // This means that if for example there are multiple MessageIDs and a FaultTo, the FaultTo will be respected.
        if (!duplicateHeaderNames.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Duplicate header names found:" + duplicateHeaderNames.get(0));
            }
            // Simply choose the first problem header we came across as we can only fault for one of them.
            AddressingFaultsHelper.triggerInvalidCardinalityFault(messageContext,
                                                                  (String) duplicateHeaderNames
                                                                          .get(0));
        }

        // check for the presence of madatory addressing headers
        checkForMandatoryHeaders(checkedHeaderNames, messageContext, namespace);

        // provide default values for headers that have not been found.
        setDefaults(checkedHeaderNames, messageContext, namespace);
    }

    private void checkForMandatoryHeaders(boolean[] alreadyFoundAddrHeader,
                                            MessageContext messageContext,
                                            String namespace) throws AxisFault {
        if (Final.WSA_NAMESPACE.equals(namespace)) {
            //Unable to validate the wsa:MessageID header here as we do not yet know which MEP
            //is in effect.
            // @see AddressingValidationHandler#checkMessageIDHeader
            
            if (!alreadyFoundAddrHeader[ACTION_FLAG]) {
                AddressingFaultsHelper
                .triggerMessageAddressingRequiredFault(messageContext, WSA_ACTION);
            }
        }
        else {
            if (!alreadyFoundAddrHeader[TO_FLAG]) {
                AddressingFaultsHelper.triggerMessageAddressingRequiredFault(messageContext, WSA_TO);
            }

            if (!alreadyFoundAddrHeader[ACTION_FLAG]) {
                AddressingFaultsHelper
                        .triggerMessageAddressingRequiredFault(messageContext, WSA_ACTION);
            }

            if (alreadyFoundAddrHeader[REPLYTO_FLAG] ||
                    alreadyFoundAddrHeader[FAULTO_FLAG]) {

                if (!alreadyFoundAddrHeader[MESSAGEID_FLAG]) {
                    AddressingFaultsHelper
                            .triggerMessageAddressingRequiredFault(messageContext, WSA_MESSAGE_ID);
                }
            }            
        }
    }

    private void setDefaults(boolean[] alreadyFoundAddrHeader, MessageContext messageContext, String namespace) {
        if (Final.WSA_NAMESPACE.equals(namespace)) {
            //According to the WS-Addressing spec, we should default the wsa:To header to the
            //anonymous URI. Doing that, however, might prevent a different value from being
            //used instead, such as the transport URL. Therefore, we only apply the default
            //on the inbound response side of a synchronous request-response exchange.
            if (!alreadyFoundAddrHeader[TO_FLAG] && !messageContext.isServerSide()) {
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(messageContext.getLogIDString() +
                    " setDefaults: Setting WS-Addressing default value for the To property.");
                }
                messageContext.setTo(new EndpointReference(Final.WSA_ANONYMOUS_URL));
            }
            
            if (!alreadyFoundAddrHeader[REPLYTO_FLAG]) {
                messageContext.setReplyTo(new EndpointReference(Final.WSA_ANONYMOUS_URL));
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(messageContext.getLogIDString() +
                    " setDefaults: Setting WS-Addressing default value for the ReplyTo property.");
                }
            }
        }
        else {
            //The none URI is not defined in the 2004/08 spec, but it is used here anyway
            //as a flag to indicate the correct semantics to apply, i.e. in the 2004/08 spec
            //the absence of a ReplyTo header indicates that a response is NOT required.
            if (!alreadyFoundAddrHeader[REPLYTO_FLAG]) {
                messageContext.setReplyTo(new EndpointReference(Final.WSA_NONE_URI));
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(
                            "setDefaults: Setting WS-Addressing default value for the ReplyTo property.");
                }
            }            
        }
    }

    private void checkDuplicateHeaders(String addressingHeaderName, int headerFlag,
                                       boolean[] checkedHeaderNames, boolean[] ignoreHeaders,
                                       ArrayList duplicateHeaderNames) {//throws AxisFault {
        // If the header name has been seen before then we should return true and add it to the list
        // of duplicate header names. Otherwise it is the first time we've seen the header so add it
        // to the checked liat and return false.
        ignoreHeaders[headerFlag] = checkedHeaderNames[headerFlag];
        if (ignoreHeaders[headerFlag]) {
            duplicateHeaderNames.add(addressingHeaderName);
        } else {
            checkedHeaderNames[headerFlag] = true;
        }

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("checkDuplicateHeaders: addressingHeaderName=" + addressingHeaderName
                      + " isDuplicate=" + ignoreHeaders[headerFlag]);
        }
    }

    private void extractRelatesToInformation(SOAPHeaderBlock soapHeaderBlock,
                                             Options messageContextOptions) {
        String address = soapHeaderBlock.getText();

        // Extract the RelationshipType attribute if it exists
        OMAttribute relationshipType =
                soapHeaderBlock.getAttribute(
                        new QName(AddressingConstants.WSA_RELATES_TO_RELATIONSHIP_TYPE));

        String relationshipTypeString =
                relationshipType == null ? null : relationshipType.getAttributeValue();

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("extractRelatesToInformation: Extracted Relationship. Value=" + address +
                      " RelationshipType=" + relationshipTypeString);
        }

        RelatesTo relatesTo = new RelatesTo(address, relationshipTypeString);

        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        relatesTo.setExtensibilityAttributes(attributes);

        messageContextOptions.addRelatesTo(relatesTo);

        // Completed processing of this header
        soapHeaderBlock.setProcessed();
    }

    private void extractFaultToEPRInformation(SOAPHeaderBlock soapHeaderBlock,
                                              String addressingNamespace,
                                              MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getFaultTo();
        if (epr == null) {
            epr = new EndpointReference("");
            messageContextOptions.setFaultTo(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("extractFaultToEPRInformation: Extracted FaultTo EPR: " + epr);
        }
        soapHeaderBlock.setProcessed();
    }

    private void extractReplyToEPRInformation(SOAPHeaderBlock soapHeaderBlock,
                                              String addressingNamespace,
                                              MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getReplyTo();
        if (epr == null) {
            epr = new EndpointReference("");
            messageContextOptions.setReplyTo(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("extractReplyToEPRInformation: Extracted ReplyTo EPR: " + epr);
        }
        soapHeaderBlock.setProcessed();
    }

    private void extractFromEPRInformation(SOAPHeaderBlock soapHeaderBlock,
                                           String addressingNamespace,
                                           MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getFrom();
        if (epr == null) {
            epr = new EndpointReference(
                    "");  // I don't know the address now. Let me pass the empty string now and fill this
            // once I process the Elements under this.
            messageContextOptions.setFrom(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("extractFromEPRInformation: Extracted From EPR: " + epr);
        }
        soapHeaderBlock.setProcessed();
    }

    private void extractToEPRInformation(SOAPHeaderBlock soapHeaderBlock,
                                         Options messageContextOptions, String namespace) {

        EndpointReference epr;
        //here the addressing epr overidde what ever already there in the message context
        epr = new EndpointReference(soapHeaderBlock.getText());
        messageContextOptions.setTo(epr);

        // check for address attributes
        Iterator addressAttributes = soapHeaderBlock.getAllAttributes();
        if (addressAttributes != null && addressAttributes.hasNext()) {
            ArrayList attributes = new ArrayList();
            while (addressAttributes.hasNext()) {
                OMAttribute attr = (OMAttribute) addressAttributes.next();
                attributes.add(attr);
            }
            epr.setAddressAttributes(attributes);
        }

        soapHeaderBlock.setProcessed();

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("extractToEPRInformation: Extracted To EPR: " + epr);
        }
    }

    private void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header,
                                                   String namespace) {
        if (Final.WSA_NAMESPACE.equals(namespace)) {
            Iterator headerBlocks = header.getChildElements();
            while (headerBlocks.hasNext()) {
                OMElement headerElement = (OMElement)headerBlocks.next();
                OMAttribute isRefParamAttr =
                    headerElement.getAttribute(new QName(namespace, "IsReferenceParameter"));
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace("extractToEprReferenceParameters: Checking header: " +
                            headerElement.getQName());
                }
                if (isRefParamAttr != null && "true".equals(isRefParamAttr.getAttributeValue())) {
                    toEPR.addReferenceParameter(headerElement);
                    if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                        log.trace("extractToEprReferenceParameters: Header: " +
                                headerElement.getQName() +
                        " has IsReferenceParameter attribute. Adding to toEPR.");
                    }
                }
            }
        }
        else {
            // there is no exact way to identify ref parameters for Submission version. So let's have a handler
            // at the end of the flow, which puts all the handlers (which are of course mustUnderstand=false)
            // as reference parameters

            // TODO : Chinthaka
        }
    }

    //We assume that any action that already exists in the message context must be the
    //soapaction. We compare that action to the WS-Addressing action, and if they are
    //different we throw a fault.
    private void extractActionInformation(SOAPHeaderBlock soapHeaderBlock,
                                          MessageContext messageContext)
            throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        String soapAction = messageContextOptions.getAction();
        String wsaAction = soapHeaderBlock.getText();

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("extractActionInformation: HTTP soapAction or action ='" + soapAction + 
                    "' wsa:Action='" + wsaAction + "'");
        }

        // Need to validate that the content of the wsa:Action header is not null or whitespace
        if ((wsaAction == null) || "".equals(wsaAction.trim())) {
            if (log.isDebugEnabled()) {
                log.debug("The wsa:Action header is present but its contents are empty.  This violates " +
                        "rules in the WS-A specification.  The SOAP node that sent this message must be changed.");
            }
            AddressingFaultsHelper.triggerActionNotSupportedFault(messageContext, wsaAction);
        }

        // The isServerSide check is because the underlying Options object is
        // shared between request and response MessageContexts for Sync
        // invocations. If the soapAction is set outbound and a wsa:Action is
        // received on the response they will differ (because there is no
        // SOAPAction header on an HTTP response). In this case we should not
        // check that soapAction==wsa:Action
        if (soapAction != null && !"".equals(soapAction) && messageContext.isServerSide()) {
            if (!soapAction.equals(wsaAction)) {
                if (log.isDebugEnabled()) {
                    log.debug("The wsa:Action header is (" + wsaAction + ") which conflicts with the HTTP soapAction or action " +
                            "(" + soapAction + ").  This is a violation of the WS-A specification.  The SOAP node that sent this message " +
                            " must be changed.");
                }
                AddressingFaultsHelper.triggerActionMismatchFault(messageContext, soapAction, wsaAction);
            }
        } else {
            messageContextOptions.setAction(wsaAction);
        }

        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        if (attributes != null) {
            messageContext.setProperty(AddressingConstants.ACTION_ATTRIBUTES, attributes);
        }

        soapHeaderBlock.setProcessed();
    }

    private void extractMessageIDInformation(SOAPHeaderBlock soapHeaderBlock,
                                             MessageContext messageContext) throws AxisFault {
        messageContext.getOptions().setMessageId(soapHeaderBlock.getText());

        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        if (attributes != null) {
            messageContext.setProperty(AddressingConstants.MESSAGEID_ATTRIBUTES, attributes);
        }

        soapHeaderBlock.setProcessed();
    }

    /**
     * Given the soap header block, this should extract the information within EPR.
     *
     * @param headerBlock         a SOAP header which is of type EndpointReference
     * @param epr                 the EndpointReference to fill in with the extracted data
     * @param addressingNamespace the WSA namespace URI
     * @param messageContext      the active MessageContext
     * @throws AxisFault if there is a problem
     */
    private void extractEPRInformation(SOAPHeaderBlock headerBlock, EndpointReference epr,
                                       String addressingNamespace, MessageContext messageContext)
            throws AxisFault {
        String namespace = null;
        
        try {
            namespace = EndpointReferenceHelper.fromOM(epr, headerBlock);            
        } catch (AxisFault af) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(
                        "extractEPRInformation: Exception occurred deserialising an EndpointReference.",
                        af);
            }
            AddressingFaultsHelper
                    .triggerMissingAddressInEPRFault(messageContext, headerBlock.getLocalName());
        }
        
        //Check that the EPR has the correct namespace.
        if (!namespace.equals(addressingNamespace)) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(
                        "extractEPRInformation: Addressing namespace = " + addressingNamespace +
                        ", EPR namespace = " + namespace);
            }
            AddressingFaultsHelper
                    .triggerInvalidEPRFault(messageContext, headerBlock.getLocalName());
        }
    }

    private ArrayList extractAttributesFromSOAPHeaderBlock(SOAPHeaderBlock soapHeaderBlock) {
        Iterator actionAttributes = soapHeaderBlock.getAllAttributes();
        if (actionAttributes != null && actionAttributes.hasNext()) {
            ArrayList attributes = new ArrayList();
            while (actionAttributes.hasNext()) {
                OMAttribute attr = (OMAttribute) actionAttributes.next();
                attributes.add(attr);
            }
            return attributes;
        }
        return null;
    }
}
