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
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AttributeHelper;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.addressing.i18n.AddressingMessages;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
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
import java.util.List;
import java.util.Map;

public class AddressingOutHandler extends AbstractTemplatedHandler implements AddressingConstants {

    private static final Log log = LogFactory.getLog(AddressingOutHandler.class);

    // TODO: This is required for MessageContext#getModuleParameter.
    //       Not clear why there is no way to automatically determine this!
    private static final String MODULE_NAME = "addressing";


    public boolean shouldInvoke(MessageContext msgContext) throws AxisFault {
        Parameter param = null;
        boolean disableAddressing = false;
        
        Object o = msgContext.getProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        if (o == null || !(o instanceof Boolean)) {
            //determine whether outbound addressing has been disabled or not.
            // Get default value from module.xml or axis2.xml files
            param = msgContext.getModuleParameter(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, MODULE_NAME, handlerDesc);
            disableAddressing =
                msgContext.isPropertyTrue(DISABLE_ADDRESSING_FOR_OUT_MESSAGES,
                    JavaUtils.isTrueExplicitly(Utils.getParameterValue(param)));
        } else {
            disableAddressing = (Boolean) o;
        }

        if (disableAddressing) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(msgContext.getLogIDString() +
                        " Addressing is disabled. Not adding WS-Addressing headers.");
            }
            return false;
        }
        return true;
    }
    
    public InvocationResponse doInvoke(MessageContext msgContext) throws AxisFault {
        

        // Determine the addressin namespace in effect.
        Object addressingVersionFromCurrentMsgCtxt = msgContext.getProperty(WS_ADDRESSING_VERSION);
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("Addressing version string from messageContext=" +
                    addressingVersionFromCurrentMsgCtxt);
        }
        boolean isSubmissionNamespace =
                Submission.WSA_NAMESPACE.equals(addressingVersionFromCurrentMsgCtxt);

        // Determine whether to include optional addressing headers in the output.
        // Get default value from module.xml or axis2.xml files
        Parameter param = msgContext.getModuleParameter(
                INCLUDE_OPTIONAL_HEADERS, MODULE_NAME, handlerDesc);
        boolean includeOptionalHeaders =
            msgContext.isPropertyTrue(INCLUDE_OPTIONAL_HEADERS,
                    JavaUtils.isTrueExplicitly(Utils.getParameterValue(param)));

        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug("includeOptionalHeaders=" + includeOptionalHeaders);
        }

        // Determine if a MustUnderstand attribute will be added to all headers in the
        // addressing namespace.
        boolean addMustUnderstandAttribute =
                msgContext.isPropertyTrue(ADD_MUST_UNDERSTAND_TO_ADDRESSING_HEADERS);

        // what if there are addressing headers already in the message. Do you replace that or not?
        // Lets have a parameter to control that. The default behavior is you won't replace addressing
        // headers if there are any (this was the case so far).
        boolean replaceHeaders = msgContext.isPropertyTrue(REPLACE_ADDRESSING_HEADERS);
        
        // Allow the user to specify the role these WS-Addressing headers should be targetted at.
        String role = (String) msgContext.getProperty(SOAP_ROLE_FOR_ADDRESSING_HEADERS);

        WSAHeaderWriter writer = new WSAHeaderWriter(msgContext, isSubmissionNamespace,
                                                     addMustUnderstandAttribute, replaceHeaders,
                                                     includeOptionalHeaders, role);
        writer.writeHeaders();

        return InvocationResponse.CONTINUE;
    }

    private class WSAHeaderWriter {

        private MessageContext messageContext;
        private SOAPEnvelope envelope;
        private SOAPHeader header;
        private SOAPFactory factory;
        private Options messageContextOptions;
        private OMNamespace addressingNamespaceObject;
        private String addressingNamespace;
        private String addressingRole;

        private boolean isFinalAddressingNamespace;
        private boolean addMustUnderstandAttribute;
        private boolean replaceHeaders;  // determines whether we replace the existing headers or not, if they present
        private boolean includeOptionalHeaders;

        private ArrayList existingWSAHeaders = null;
        
        public WSAHeaderWriter(MessageContext mc, boolean isSubmissionNamespace, boolean addMU,
                               boolean replace, boolean includeOptional, String role) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("WSAHeaderWriter: isFinal=" + !isSubmissionNamespace + " addMU=" + addMU +
                        " replace=" + replace + " includeOptional=" + includeOptional+" role="+role);
            }

            messageContext = mc;
            envelope = mc.getEnvelope();
            factory = (SOAPFactory)envelope.getOMFactory();

            messageContextOptions = messageContext.getOptions();

            addressingNamespace =
                    isSubmissionNamespace ? Submission.WSA_NAMESPACE : Final.WSA_NAMESPACE;

            header = envelope.getHeader();
            // if there is no soap header in the envelope being processed, add one.
            if (header == null) {
            	header = factory.createSOAPHeader(envelope);
            }else{
            	ArrayList addressingHeaders = header.getHeaderBlocksWithNSURI(addressingNamespace);
            	if(addressingHeaders!=null && !addressingHeaders.isEmpty()){
            		existingWSAHeaders = new ArrayList(addressingHeaders.size());
            		for(Iterator iter=addressingHeaders.iterator();iter.hasNext();){
            			SOAPHeaderBlock oe = (SOAPHeaderBlock)iter.next();
            			if(addressingRole == null || addressingRole.length() ==0 || addressingRole.equals(oe.getRole())){
            				existingWSAHeaders.add(oe.getLocalName());
            			}
            		}
            	}
            	if(addressingHeaders != null && addressingHeaders.size() ==0){
            		addressingHeaders = null;
            	}
            }
            
            isFinalAddressingNamespace = !isSubmissionNamespace;
            addMustUnderstandAttribute = addMU;
            replaceHeaders = replace;
            includeOptionalHeaders = includeOptional;
            addressingRole = role;
            
            if(!isFinalAddressingNamespace && mc.getTo() == null){
            	mc.setTo(new EndpointReference(AddressingConstants.Submission.WSA_ANONYMOUS_URL));
            }
        }

        public void writeHeaders() throws AxisFault {

            // by this time, we definitely have some addressing information to be sent. This is because,
            // we have tested at the start of this whether messageInformationHeaders are null or not.
            // So rather than declaring addressing namespace in each and every addressing header, lets
            // define that in the Header itself.
        	addressingNamespaceObject = header.declareNamespace(addressingNamespace, WSA_DEFAULT_PREFIX);

            // processing WSA To
            processToEPR();

            // processing WSA replyTo
            processReplyTo();

            // processing WSA From
            processFromEPR();

            // processing WSA FaultTo
            processFaultToEPR();

            // processing WSA MessageID
            processMessageID();

            // processing WSA Action
            processWSAAction();

            // processing WSA RelatesTo
            processRelatesTo();

            // process fault headers, if present
            processFaultsInfoIfPresent();

            // process mustUnderstand attribute, if required.
            processMustUnderstandProperty();
        }


        private void processMessageID() {
            String messageID = messageContextOptions.getMessageId();
            
            //Check whether we want to force a message id to be sent.
            if (messageID == null && includeOptionalHeaders) {
                messageID = UIDGenerator.generateURNString();
                messageContextOptions.setMessageId(messageID);
            }
            
            if (messageID != null && !isAddressingHeaderAlreadyAvailable(WSA_MESSAGE_ID, false))
            {//optional
            	ArrayList attributes = (ArrayList)messageContext.getLocalProperty(
                        AddressingConstants.MESSAGEID_ATTRIBUTES);
                createSOAPHeaderBlock(messageID, WSA_MESSAGE_ID, attributes);
            }
        }

        private void processWSAAction() throws AxisFault {
            String action = messageContextOptions.getAction();

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(messageContext.getLogIDString() +
                        " processWSAAction: action from messageContext: " + action);
            }
            if (action == null || action.length()==0) {
                if (messageContext.getAxisOperation() != null) {
                    action = messageContext.getAxisOperation().getOutputAction();
                    if(action!=null){
                    	// Set this action back to obviate possible action mismatch problems
                    	messageContext.setWSAAction(action);
                    }
                    if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                        log.trace(messageContext.getLogIDString() +
                                " processWSAAction: action from AxisOperation: " + action);
                    }
                }
            }else{
	            // Use the correct fault action for the selected namespace
	            if(isFinalAddressingNamespace){
	            	if(Submission.WSA_FAULT_ACTION.equals(action)){
	            		action = Final.WSA_FAULT_ACTION;
	            		messageContextOptions.setAction(action);
	            	}
	            }else{
	            	if(Final.WSA_FAULT_ACTION.equals(action)){
	            		action = Submission.WSA_FAULT_ACTION;
	            		messageContextOptions.setAction(action);
	            	}else if(Final.WSA_SOAP_FAULT_ACTION.equals(action)){
	                    action = Submission.WSA_FAULT_ACTION;
	                    messageContextOptions.setAction(action);
	            	}
	            }
            }

            // If we need to add a wsa:Action header
            if (!isAddressingHeaderAlreadyAvailable(WSA_ACTION, false)) {
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(messageContext.getLogIDString() +
                            " processWSAAction: No existing wsa:Action header found");
                }
                // If we don't have an action to add,
                if (action == null || action.length()==0) {
                    if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                        log.trace(messageContext.getLogIDString() +
                                " processWSAAction: No action to add to header");
                    }
                    // Fault unless validation has been explictily turned off
                    if (!messageContext.isPropertyTrue(
                            AddressingConstants.DISABLE_OUTBOUND_ADDRESSING_VALIDATION))
                    {
                        throw new AxisFault(AddressingMessages.getMessage("outboundNoAction"));
                    }
                } else {
                    if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                        log.trace(messageContext.getLogIDString() +
                                " processWSAAction: Adding action to header: " + action);
                    }
                    // Otherwise just add the header
                    ArrayList attributes = (ArrayList)messageContext.getLocalProperty(
                            AddressingConstants.ACTION_ATTRIBUTES);
                    createSOAPHeaderBlock(action, WSA_ACTION, attributes);
                }
            }
        }

        private void processFaultsInfoIfPresent() {
            OMElement detailElement = AddressingFaultsHelper
                    .getDetailElementForAddressingFault(messageContext, addressingNamespaceObject);
            if (detailElement != null) {
                //The difference between SOAP 1.1 and SOAP 1.2 fault messages is explained in the WS-Addressing Specs.
                if (isFinalAddressingNamespace && messageContext.isSOAP11()) {
                    // Add detail as a wsa:FaultDetail header
                    if (!isAddressingHeaderAlreadyAvailable(Final.FAULT_HEADER_DETAIL, false)) {
                        SOAPHeaderBlock faultDetail = header.addHeaderBlock(
                                Final.FAULT_HEADER_DETAIL, addressingNamespaceObject);
                        faultDetail.addChild(ElementHelper.importOMElement(detailElement, factory));
                    }
                } else if (!messageContext.isSOAP11()) {
                    // Add detail to the Fault in the SOAP Body
                    SOAPFault fault = envelope.getBody().getFault();
                    if (fault != null && fault.getDetail() != null) {
                        fault.getDetail().addDetailEntry(
                                ElementHelper.importOMElement(detailElement, factory));
                    }
                }
            }
        }

        private void processRelatesTo() {
            if (!isAddressingHeaderAlreadyAvailable(WSA_RELATES_TO, true)) {
                RelatesTo[] relatesTo = messageContextOptions.getRelationships();

                if (relatesTo != null) {
                    for (int i = 0, length = relatesTo.length; i < length; i++) {
                        OMElement relatesToHeader = createSOAPHeaderBlock(relatesTo[i].getValue(),
                        		WSA_RELATES_TO, relatesTo[i].getExtensibilityAttributes());
                        String relationshipType = relatesTo[i].getRelationshipType();
                        if (relatesToHeader != null) {
                        	
                        	if(!includeOptionalHeaders){
                        		if (Final.WSA_DEFAULT_RELATIONSHIP_TYPE.equals(relationshipType) ||
                                        Submission.WSA_DEFAULT_RELATIONSHIP_TYPE
                                                .equals(relationshipType)) {
                        			relationshipType = null; //Omit the attribute.
                        		}
                        	}
                        	
                            if(relationshipType != null){
	                            relatesToHeader.addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE,
	                                                         relationshipType,
	                                                         null);
                            }
                        }
                    }
                }
            }
        }

        private void processFaultToEPR() throws AxisFault {
            EndpointReference epr = messageContextOptions.getFaultTo();
            String headerName = AddressingConstants.WSA_FAULT_TO;

            //Omit the header if the epr is null.
            if (epr != null && !isAddressingHeaderAlreadyAvailable(headerName, false)) {
                addToSOAPHeader(epr, headerName);
            }
        }

        private void processFromEPR() throws AxisFault {
            EndpointReference epr = messageContextOptions.getFrom();
            String headerName = AddressingConstants.WSA_FROM;

            //Omit the header if the epr is null.
            if (epr != null && !isAddressingHeaderAlreadyAvailable(headerName, false)) {
                addToSOAPHeader(epr, headerName);
            }
        }

        private void processReplyTo() throws AxisFault {
            EndpointReference epr = messageContextOptions.getReplyTo();
            String headerName = AddressingConstants.WSA_REPLY_TO;

            //Don't check epr for null here as addToSOAPHeader() will provide an appropriate default.
            //This default is especially useful for client side outbound processing.
            if (!isAddressingHeaderAlreadyAvailable(headerName, false)) {
                addToSOAPHeader(epr, headerName);
            }
        }

        private void processToEPR() throws AxisFault {
            EndpointReference epr = messageContextOptions.getTo();
            if (epr != null && !isAddressingHeaderAlreadyAvailable(WSA_TO, false)) {
                try {
                    processToEPRReferenceInformation(epr.getAllReferenceParameters());
                }
                catch (Exception e) {
                    throw new AxisFault(AddressingMessages.getMessage("referenceParameterError"), e);
                }
                String address = epr.getAddress();
                if (address != null && address.length()!=0) {
                    if (!includeOptionalHeaders && isFinalAddressingNamespace &&
                            epr.isWSAddressingAnonymous())
                    {
                        return; //Omit the header.
                    }
                    createSOAPHeaderBlock(address, WSA_TO, epr.getAddressAttributes());
                }
            }
        }

        private OMElement createSOAPHeaderBlock(String value, String headerName, ArrayList attributes) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace("createSOAPHeaderBlock: value=" + value + " headerName=" + headerName);
            }
            if (value != null && value.length()!=0) {
                SOAPHeaderBlock soapHeaderBlock =
                        header.addHeaderBlock(headerName, addressingNamespaceObject);
                soapHeaderBlock.addChild(factory.createOMText(value));
                if (attributes != null && !attributes.isEmpty()) {
                    Iterator attrIterator = attributes.iterator();
                    while (attrIterator.hasNext()) {
                        AttributeHelper
                                .importOMAttribute((OMAttribute)attrIterator.next(), soapHeaderBlock);
                    }
                }
                addRoleToHeader(soapHeaderBlock);
                return soapHeaderBlock;
            }
            return null;
        }

        private void addToSOAPHeader(EndpointReference epr, String headerName) throws AxisFault {
            String prefix = addressingNamespaceObject.getPrefix();
            String anonymous = isFinalAddressingNamespace ?
                    Final.WSA_ANONYMOUS_URL : Submission.WSA_ANONYMOUS_URL;

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace("addToSOAPHeader: epr=" + epr + " headerName=" + headerName);
            }

            if (epr == null) {
                if (!includeOptionalHeaders && isFinalAddressingNamespace &&
                        AddressingConstants.WSA_REPLY_TO.equals(headerName)) {
                    return; //Omit the header.
                } else {
                    epr = new EndpointReference(anonymous);
                }
            }
            else if (!isFinalAddressingNamespace && epr.hasNoneAddress()) {
                return; //Omit the header.
            }
            else if (epr.isWSAddressingAnonymous())
            {
                if (!includeOptionalHeaders && isFinalAddressingNamespace &&
                        AddressingConstants.WSA_REPLY_TO.equals(headerName)) {
                    return; //Omit the header.
                } else {
                    epr.setAddress(anonymous);
                }
            }

            OMElement soapHeaderBlock = EndpointReferenceHelper.toOM(factory,
                                                                     epr,
                                                                     new QName(addressingNamespace,
                                                                               headerName, prefix),
                                                                     addressingNamespace);
            addRoleToHeader((SOAPHeaderBlock) soapHeaderBlock);
            header.addChild(soapHeaderBlock);
        }

        /**
         * This will add reference parameters and/or reference properties in to the message
         *
         * @param referenceInformation a Map from QName -> OMElement
         * @param parent               is the element to which the referenceparameters should be
         *                             attached
         */
        private void processToEPRReferenceInformation(Map referenceInformation) throws Exception {
            if (referenceInformation != null) {
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace("processToEPRReferenceInformation: " + referenceInformation);
                }
                Iterator iterator = referenceInformation.values().iterator();
                while (iterator.hasNext()) {
                    OMElement omElement = (OMElement)iterator.next();
                    SOAPHeaderBlock newElement = ElementHelper.toSOAPHeaderBlock(omElement, factory);
                    if (isFinalAddressingNamespace) {
                        newElement.addAttribute(Final.WSA_IS_REFERENCE_PARAMETER_ATTRIBUTE,
                                               Final.WSA_TYPE_ATTRIBUTE_VALUE,
                                               addressingNamespaceObject);
                    }
                    addRoleToHeader(newElement);
                    header.addChild(newElement);
                }
            }
            // Now add reference parameters we found in the WSDL (if any)
            AxisService service = messageContext.getAxisService();
            if(service != null){
            	AxisEndpoint endpoint = service.getEndpoint(service.getEndpointName());
            	if(endpoint != null){
            		ArrayList referenceparameters = (ArrayList) endpoint.getParameterValue(REFERENCE_PARAMETER_PARAMETER);
            		if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            			log.trace("processToEPRReferenceInformation: Reference Parameters from WSDL:" + referenceparameters);
            		}
            		if(referenceparameters!=null){
            			Iterator iterator = referenceparameters.iterator();
            			while (iterator.hasNext()) {
            				OMElement omElement = (OMElement)iterator.next();
                            SOAPHeaderBlock newElement = ElementHelper.toSOAPHeaderBlock(omElement, factory);
            				if (isFinalAddressingNamespace) {
            					newElement.addAttribute(Final.WSA_IS_REFERENCE_PARAMETER_ATTRIBUTE,
            							Final.WSA_TYPE_ATTRIBUTE_VALUE,
            							addressingNamespaceObject);
            				}
            				addRoleToHeader(newElement);
            				header.addChild(newElement);
            			}
            		}
            	}
            }
        }

        /**
         * This will check for the existence of message information headers already in the message.
         * If there are already headers, then replacing them or not depends on the replaceHeaders
         * property.
         *
         * @param name            - Name of the message information header
         * @param multipleHeaders - determines whether to search for multiple headers, or not.
         * @return false - if one can add new headers (always the case if multipleHeaders is true),
         *         true - if new headers can't be added.
         */
        private boolean isAddressingHeaderAlreadyAvailable(String name, boolean multipleHeaders) {
        	boolean status = false;

        	if (multipleHeaders) {
        		if (replaceHeaders) {
        			QName qname = new QName(addressingNamespace, name, WSA_DEFAULT_PREFIX);
        			Iterator iterator = header.getChildrenWithName(qname);
        			while (iterator.hasNext()) {
        				iterator.next();
        				iterator.remove();
        			}
        		}
        	} else {
        		boolean exists = didAddressingHeaderExist(name);
        		if (exists && replaceHeaders) {
        			QName qname = new QName(addressingNamespace, name, WSA_DEFAULT_PREFIX);
        			OMElement addressingHeader = header.getFirstChildWithName(qname);
        			if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
        				log.trace("isAddressingHeaderAlreadyAvailable: Removing existing header:" +
        						addressingHeader.getLocalName());
        			}
        			addressingHeader.detach();
        		} else {
        			status = exists;
        		}
        	}

        	if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
        		log.trace("isAddressingHeaderAlreadyAvailable: name=" + name + " status=" + status);
        	}
        	return status;
        }

        private boolean didAddressingHeaderExist(String headerName){
        	if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
        		log.trace("didAddressingHeaderExist: headerName=" + headerName);
        	}
        	boolean result = false;
        	if(existingWSAHeaders != null){
        		result = existingWSAHeaders.contains(headerName);
        		if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
        			log.trace("didAddressingHeaderExist: existingWSAHeaders=" + existingWSAHeaders+" result="+result);
        		}
        	}
        	return result;
        }

        /**
         * Sets a mustUnderstand attribute on all headers that are found with the appropriate
         * addressing namespace.
         */
        private void processMustUnderstandProperty() {
            if (addMustUnderstandAttribute) {
                List headers = header.getHeaderBlocksWithNSURI(addressingNamespace);

                for (int i = 0, size = headers.size(); i < size; i++) {
                    SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock)headers.get(i);
                    soapHeaderBlock.setMustUnderstand(true);
                    if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                        log.trace(
                                "processMustUnderstandProperty: Setting mustUnderstand=true on: " +
                                        soapHeaderBlock.getLocalName());
                    }
                }
            }
        }
        
        private void addRoleToHeader(SOAPHeaderBlock header){
        	if(addressingRole == null || addressingRole.length()==0){
        		return;
        	}
        	header.setRole(addressingRole);
        }
    }
}

