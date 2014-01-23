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

package org.apache.axis2.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.*;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

public class MessageContextBuilder {
	
	protected static final Log log = LogFactory.getLog(MessageContextBuilder.class);

    /**
     * Creates a new 'response' message context based on a 'request' message context
     * Only deals with properties/fields that are the same for both 'normal' and fault responses.
     *
     * @param inMessageContext context for which the response will be created
     * @return response message context for the given in message context
     * @throws org.apache.axis2.AxisFault error in creating the response message context
     */
    private static MessageContext createResponseMessageContext(MessageContext inMessageContext)
            throws AxisFault {
        MessageContext newmsgCtx =
                inMessageContext.getConfigurationContext().createMessageContext();

        newmsgCtx.setSessionContext(inMessageContext.getSessionContext());
        newmsgCtx.setTransportIn(inMessageContext.getTransportIn());
        newmsgCtx.setTransportOut(inMessageContext.getTransportOut());
        newmsgCtx.setServerSide(inMessageContext.isServerSide());
        newmsgCtx.setProperty(MessageContext.IN_MESSAGE_CONTEXT, inMessageContext);

        // TODO: Should this be specifying (or defaulting to) the "response" relationshipType??
        newmsgCtx.addRelatesTo(new RelatesTo(inMessageContext.getOptions().getMessageId()));

        newmsgCtx.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                              inMessageContext.getProperty(
                                      AddressingConstants.WS_ADDRESSING_VERSION));
        newmsgCtx.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES,
                              inMessageContext.getProperty(
                                      AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));

        newmsgCtx.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME,
                              inMessageContext.getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME));
        newmsgCtx.setProperty(Constants.AXIS_BINDING_OPERATION,
                              inMessageContext.getProperty(Constants.AXIS_BINDING_OPERATION));

        // Setting the charater set encoding
        newmsgCtx.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                              inMessageContext.getProperty(
                                      Constants.Configuration.CHARACTER_SET_ENCODING));
        //Setting the message type property
        newmsgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE,
                              inMessageContext.getProperty(Constants.Configuration.MESSAGE_TYPE));
        newmsgCtx.setDoingREST(inMessageContext.isDoingREST());

        newmsgCtx.setOperationContext(inMessageContext.getOperationContext());

        newmsgCtx.setProperty(MessageContext.TRANSPORT_OUT,
                              inMessageContext.getProperty(MessageContext.TRANSPORT_OUT));
        newmsgCtx.setProperty(Constants.OUT_TRANSPORT_INFO,
                              inMessageContext.getProperty(Constants.OUT_TRANSPORT_INFO));

        handleCorrelationID(inMessageContext,newmsgCtx);
        return newmsgCtx;
    }

    /**
     * Creates a MessageContext for use with a non-fault response based on an request MessageContext
     *
     * @param inMessageContext for the out message context to be created
     * @return created out message context from the given in message context
     * @throws org.apache.axis2.AxisFault error in creating the out message context
     */
    public static MessageContext createOutMessageContext(MessageContext inMessageContext)
            throws AxisFault {

        // Create a basic response MessageContext with basic fields copied
        MessageContext newmsgCtx = createResponseMessageContext(inMessageContext);

        // Simple response so set To to value of inbound ReplyTo
        newmsgCtx.setTo(inMessageContext.getReplyTo());
        if (newmsgCtx.getTo() == null) {
            newmsgCtx.setTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        }

        // do Target Resolution
        TargetResolver targetResolver =
                newmsgCtx.getConfigurationContext().getAxisConfiguration().getTargetResolverChain();
        if (targetResolver != null) {
            targetResolver.resolveTarget(newmsgCtx);
        }

        // Determine ReplyTo for response message.
        AxisService axisService = inMessageContext.getAxisService();
        if (axisService != null && Constants.SCOPE_SOAP_SESSION.equals(axisService.getScope())) {
            //If the wsa 2004/08 (submission) spec is in effect use the wsa anonymous URI as the default replyTo value.
            //This is necessary because the wsa none URI is not available in that spec.
            Object version = inMessageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
            if (AddressingConstants.Submission.WSA_NAMESPACE.equals(version)) {
                newmsgCtx.setReplyTo(
                        new EndpointReference(AddressingConstants.Submission.WSA_ANONYMOUS_URL));
            } else {
                newmsgCtx.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));
            }

            newmsgCtx.setMessageID(UIDGenerator.generateURNString());

            // add the service group id as a reference parameter
            String serviceGroupContextId = inMessageContext.getServiceGroupContextId();
            if (serviceGroupContextId != null && !"".equals(serviceGroupContextId)) {
                EndpointReference replyToEPR = newmsgCtx.getReplyTo();
                replyToEPR.addReferenceParameter(new QName(Constants.AXIS2_NAMESPACE_URI,
                                                           Constants.SERVICE_GROUP_ID,
                                                           Constants.AXIS2_NAMESPACE_PREFIX),
                                                 serviceGroupContextId);
            }
        } else {
            EndpointReference outboundToEPR = newmsgCtx.getTo();
            Object version = newmsgCtx.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
            if (AddressingConstants.Submission.WSA_NAMESPACE.equals(version) ||
                (outboundToEPR != null && !outboundToEPR.hasAnonymousAddress())) {
                newmsgCtx.setMessageID(UIDGenerator.generateURNString());
                newmsgCtx.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));
            }
        }

        // Set wsa:Action for response message
        // Use specified value if available
        AxisOperation ao = inMessageContext.getAxisOperation();
        if ((ao != null) && (ao.getOutputAction() != null)) {
            newmsgCtx.setWSAAction(ao.getOutputAction());
        } else { // If not, simply copy the request value. Almost always invalid.
            newmsgCtx.setWSAAction(inMessageContext.getWSAAction());
        }

        if (ao != null){
           newmsgCtx.setAxisMessage(ao.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
        }

        // setting the out bound binding message
        AxisBindingMessage inboundAxisBindingMessage
                = (AxisBindingMessage)inMessageContext.getProperty(Constants.AXIS_BINDING_MESSAGE);
        if (inboundAxisBindingMessage != null){
            AxisBindingOperation axisBindingOperation = inboundAxisBindingMessage.getAxisBindingOperation();
            newmsgCtx.setProperty(Constants.AXIS_BINDING_MESSAGE,
                    axisBindingOperation.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
        }

        newmsgCtx.setDoingMTOM(inMessageContext.isDoingMTOM());
        newmsgCtx.setDoingSwA(inMessageContext.isDoingSwA());
        newmsgCtx.setServiceGroupContextId(inMessageContext.getServiceGroupContextId());



        // Ensure transport settings match the scheme for the To EPR
        setupCorrectTransportOut(newmsgCtx);
        return newmsgCtx;
    }

    /**
     * Copies the correlation id (jms) from the in message ctx
     * to the out message ctx. Currently this check is for jms 
     * only, but can be expanded to other transports if the need
     * arises.
     */
    private static void handleCorrelationID(MessageContext inMessageContext,MessageContext outMessageContext)    
    {
    	if (inMessageContext.getIncomingTransportName()!= null &&
    	    inMessageContext.getIncomingTransportName().equals(Constants.TRANSPORT_JMS))
    	{
    		log.debug("Incoming Transport is JMS, lets check for JMS correlation id");
    		
	    	String correlationId =
	            (String) inMessageContext.getProperty(Constants.JMS_COORELATION_ID);
	    	log.debug("Correlation id is " + correlationId);
	        if (correlationId != null && correlationId.length() > 0) {
	        	outMessageContext.setProperty(Constants.JMS_COORELATION_ID, correlationId);
	        }
    	}
    }

    /**
     * This method is called to handle any error that occurs at inflow or outflow. But if the
     * method is called twice, it implies that sending the error handling has failed, in which case
     * the method logs the error and exits.
     */
    public static MessageContext createFaultMessageContext(MessageContext processingContext,
                                                           Throwable e)
            throws AxisFault {
        if (processingContext.isProcessingFault()) {
            // We get the error file processing the fault. nothing we can do
            throw new AxisFault(Messages.getMessage("errorwhileProcessingFault"));
        }

        // See if the throwable is an AxisFault and if it already contains the
        // fault MessageContext
        if (e instanceof AxisFault) {
            MessageContext faultMessageContext = ((AxisFault) e).getFaultMessageContext();
            if (faultMessageContext != null) {
                // These may not have been set correctly when the original context
                // was created -- an example of this is with the SimpleHTTPServer.
                // I'm not sure if this is the correct thing to do, or if the
                // code that created this context in the first place should
                // expect that the transport out info was set correctly, as
                // it may need to use that info at some point before we get to
                // this code.
                faultMessageContext.setProperty(MessageContext.TRANSPORT_OUT,
                                                processingContext.getProperty(
                                                        MessageContext.TRANSPORT_OUT));
                faultMessageContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                                                processingContext.getProperty(
                                                        Constants.OUT_TRANSPORT_INFO));
                faultMessageContext.setProcessingFault(true);
                faultMessageContext.setProperty(MessageContext.IN_MESSAGE_CONTEXT, processingContext);
                return faultMessageContext;
            }
        }

        // Create a basic response MessageContext with basic fields copied
        MessageContext faultContext = createResponseMessageContext(processingContext);

        // Register the fault message context
        OperationContext operationContext = processingContext.getOperationContext();
        if (operationContext != null) {
            processingContext.getAxisOperation().addFaultMessageContext(faultContext,
                                                                        operationContext);
        }

        faultContext.setProcessingFault(true);

        // Set wsa:Action for response message
        
        // Use specified value if available

        String faultAction = (e instanceof AxisFault) ? ((AxisFault)e).getFaultAction() : null;

        if (faultAction == null) {
            AxisOperation op = processingContext.getAxisOperation();
            if (op != null && op.getFaultAction() != null) {
                // TODO: Should the op be able to pick a fault action based on the fault?
                faultAction = op.getFaultAction();
            } else { //If, for some reason there is no value set, should use a sensible action.
                faultAction = Final.WSA_SOAP_FAULT_ACTION;
            }
        }

        faultContext.setWSAAction(faultAction);

        // there are some information  that the fault thrower wants to pass to the fault path.
        // Means that the fault is a ws-addressing one hence use the ws-addressing fault action.
        Object faultInfoForHeaders =
                processingContext.getLocalProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        if (faultInfoForHeaders != null) {
            faultContext.setProperty(Constants.FAULT_INFORMATION_FOR_HEADERS, faultInfoForHeaders);

            // Note that this overrides any action set above
            faultContext.setWSAAction(Final.WSA_FAULT_ACTION);
        }

        // if the exception is due to a problem in the faultTo header itself, we can not use that
        // fault information to send the error. Try to send using replyTo, else leave it to transport
        boolean shouldSendFaultToFaultTo =
                AddressingHelper.shouldSendFaultToFaultTo(processingContext);
        EndpointReference faultTo = processingContext.getFaultTo();
        if (faultTo != null && shouldSendFaultToFaultTo) {
            faultContext.setTo(faultTo);
        } else {
            faultContext.setTo(processingContext.getReplyTo());
        }

        if (faultContext.getTo() == null) {
            faultContext.setTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        }

        // Not worth setting up the session information on a fault flow
        EndpointReference outboundToEPR = faultContext.getTo();
        Object version = faultContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        if (AddressingConstants.Submission.WSA_NAMESPACE.equals(version) ||
            (outboundToEPR != null && !outboundToEPR.hasAnonymousAddress())) {
            faultContext.setMessageID(UIDGenerator.generateURNString());
            faultContext.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));
        }

        // do Target Resolution
        TargetResolver targetResolver = faultContext.getConfigurationContext()
                .getAxisConfiguration().getTargetResolverChain();
        if (targetResolver != null) {
            targetResolver.resolveTarget(faultContext);
        }

        // Ensure transport settings match the scheme for the To EPR
        setupCorrectTransportOut(faultContext);

        SOAPEnvelope envelope = createFaultEnvelope(processingContext, e);
        faultContext.setEnvelope(envelope);

        //get the SOAP headers, user is trying to send in the fault
        // TODO: Rationalize this mechanism a bit - maybe headers should live in the fault?
        List soapHeadersList =
                (List) processingContext.getProperty(SOAPConstants.HEADER_LOCAL_NAME);
        if (soapHeadersList != null) {
            SOAPHeader soapHeaderElement = envelope.getHeader();
            for (Object aSoapHeadersList : soapHeadersList) {
                OMElement soapHeaderBlock = (OMElement)aSoapHeadersList;
                soapHeaderElement.addChild(soapHeaderBlock);
            }
        }

        // TODO: Transport-specific stuff in here?  Why?  Is there a better way?
        // now add HTTP Headers
        faultContext.setProperty(HTTPConstants.HTTP_HEADERS,
                                 processingContext.getProperty(HTTPConstants.HTTP_HEADERS));

        //setting the out bound binding message
        AxisBindingMessage inboundAxisBindingMessage
                = (AxisBindingMessage)processingContext.getProperty(Constants.AXIS_BINDING_MESSAGE);
        if (inboundAxisBindingMessage != null){
                AxisBindingOperation axisBindingOperation = inboundAxisBindingMessage.getAxisBindingOperation();
                faultContext.setProperty(Constants.AXIS_BINDING_MESSAGE,
                        axisBindingOperation.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
        }
        faultContext.setAxisService(processingContext.getAxisService());

        return faultContext;
    }

    /**
     * Ensure that if the scheme of the To EPR for the response is different than the
     * transport used for the request that the correct TransportOut is available
     */
    private static void setupCorrectTransportOut(MessageContext context) throws AxisFault {
        // Determine that we have the correct transport available.
        TransportOutDescription transportOut = context.getTransportOut();

        try {
            EndpointReference responseEPR = context.getTo();
            if (context.isServerSide() && responseEPR != null) {
                if (!responseEPR.hasAnonymousAddress() && !responseEPR.hasNoneAddress()) {
                    URI uri = new URI(responseEPR.getAddress());
                    String scheme = uri.getScheme();
                    if ((transportOut == null) || !transportOut.getName().equals(scheme)) {
                        ConfigurationContext configurationContext =
                                context.getConfigurationContext();
                        transportOut = configurationContext.getAxisConfiguration()
                                .getTransportOut(scheme);
                        if (transportOut == null) {
                            throw new AxisFault("Can not find the transport sender : " + scheme);
                        }
                        context.setTransportOut(transportOut);
                    }
                    if (context.getOperationContext() != null) {
                        context.getOperationContext().setProperty(
                                Constants.DIFFERENT_EPR, Constants.VALUE_TRUE);
                    }
                }
            }
        } catch (URISyntaxException urise) {
            throw AxisFault.makeFault(urise);
        }
    }

    /**
     * Information to create the SOAPFault can be extracted from different places.
     * 1. Those information may have been put in to the message context by some handler. When someone
     * is putting like that, he must make sure the SOAPElements he is putting must be from the
     * correct SOAP Version.
     * 2. SOAPProcessingException is flexible enough to carry information about the fault. For example
     * it has an attribute to store the fault code. The fault reason can be extracted from the
     * message of the exception. I opted to put the stacktrace under the detail element.
     * eg : <Detail>
     * <Exception> stack trace goes here </Exception>
     * <Detail>
     * <p/>
     * If those information can not be extracted from any of the above places, I default the soap
     * fault values to following.
     * <Fault>
     * <Code>
     * <Value>env:Receiver</Value>
     * </Code>
     * <Reason>
     * <Text>unknown</Text>
     * </Reason>
     * <Role/>
     * <Node/>
     * <Detail/>
     * </Fault>
     * <p/>
     * -- EC
     *
     * @param context
     * @param e
     */
    private static SOAPEnvelope createFaultEnvelope(MessageContext context, Throwable e) {
        SOAPEnvelope envelope;
        
        if(log.isDebugEnabled()){
        	log.debug("start createFaultEnvelope()");
        }
        if (context.isSOAP11()) {
            envelope = OMAbstractFactory.getSOAP11Factory().getDefaultFaultEnvelope();
        } else {
            // Following will make SOAP 1.2 as the default, too.
            envelope = OMAbstractFactory.getSOAP12Factory().getDefaultFaultEnvelope();
        }
        SOAPFault fault = envelope.getBody().getFault();
        SOAPProcessingException soapException = null;
        AxisFault axisFault = null;

        if (e == null) return envelope;

        if (e instanceof AxisFault) {
            axisFault = (AxisFault) e;
        } else if (e.getCause() instanceof AxisFault) {
            axisFault = (AxisFault) e.getCause();
        }

        if (axisFault != null) {
            Iterator iter = axisFault.headerIterator();
            while (iter.hasNext()) {
                SOAPHeaderBlock header = (SOAPHeaderBlock) iter.next();
                envelope.getHeader().addChild(header);
            }
        }

        if (e instanceof SOAPProcessingException) {
            soapException = (SOAPProcessingException) e;
        } else if (axisFault != null) {
            if (axisFault.getCause() instanceof SOAPProcessingException) {
                soapException = (SOAPProcessingException) axisFault.getCause();
            }
        }

        // user can set the fault information to the message context or to the AxisFault itself.
        // whatever user sets to the message context, supercedes eerything.
        
        Object faultCode = context.getProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME);
        String soapFaultCode = "";

        if (faultCode != null) {
        	if(log.isDebugEnabled()){
        		log.debug("faultCode != null");
        	}
            fault.setCode((SOAPFaultCode) faultCode);
            soapFaultCode = ((SOAPFaultCode) faultCode).getText();
        } else if (soapException != null) {
        	if(log.isDebugEnabled()){
        		log.debug("soapException != null");
        	}
            soapFaultCode = soapException.getFaultCode();
            OMNamespace namespace = null;
            if(envelope!=null){
            	if(log.isDebugEnabled()){
            		log.debug("envelope!=null");
            	}	
            	namespace = envelope.getNamespace();
            }
            
            if (namespace != null){
                String sfcLocalPart = soapFaultCode.substring(soapFaultCode.lastIndexOf(":")+1);
      
                //If the fault code is one of the predefined ones that make sure the prefix 
                //matches that of the envelope NS
                if (sfcLocalPart.equals(SOAPConstants.FAULT_CODE_VERSION_MISMATCH) ||
                    sfcLocalPart.equals(SOAPConstants.FAULT_CODE_MUST_UNDERSTAND) ||
                    sfcLocalPart.equals(SOAPConstants.FAULT_CODE_DATA_ENCODING_UNKNOWN) ||
                    sfcLocalPart.equals(SOAPConstants.FAULT_CODE_RECEIVER) ||
                    sfcLocalPart.equals(SOAPConstants.FAULT_CODE_SENDER)) {
                
                    if(log.isDebugEnabled()){
                        log.debug("SoapFaultCode local part= " +sfcLocalPart);
                    }

                    String prefix = namespace.getPrefix() + ":";

                    if (!soapFaultCode.contains(":")) {
                        soapFaultCode = prefix + soapFaultCode;
                    } else {
                        soapFaultCode = prefix + soapFaultCode.substring(soapFaultCode.indexOf(":")+1);
                    }

                    if(log.isDebugEnabled()){
                        log.debug("SoapFaultCode reset to " +soapFaultCode);
                    }

                }
            } else {
                if(log.isDebugEnabled()){
                    log.debug("Namespace is null, cannot attach prefix to SOAPFaultCode");
                }
            }
            
            if(log.isDebugEnabled()){        	
                log.debug("SoapFaultCode ="+soapFaultCode);        	
            }
        	
        } else if (axisFault != null) {
        	if(log.isDebugEnabled()){
        		log.debug("axisFault != null");
        	}
            if (axisFault.getFaultCodeElement() != null) {
                fault.setCode(axisFault.getFaultCodeElement());
                soapFaultCode = axisFault.getFaultCodeElement().getText();               
            } else {
                QName faultCodeQName = axisFault.getFaultCode();
                if (faultCodeQName != null) {
                	if(log.isDebugEnabled()){
                		log.debug("prefix ="+faultCodeQName.getPrefix());
                		log.debug("Fault Code namespace ="+faultCodeQName.getNamespaceURI());
                		log.debug("Fault Code ="+faultCodeQName.getLocalPart());
                	}
                    if (faultCodeQName.getLocalPart().indexOf(":") == -1) {
                    	if(log.isDebugEnabled()){
                    		log.debug("faultCodeQName.getLocalPart().indexOf(\":\") == -1");
                    	}
                        String prefix = faultCodeQName.getPrefix();
                        if(log.isDebugEnabled()){
                        	log.debug("prefix = "+prefix);
                        }
                        String uri = faultCodeQName.getNamespaceURI();
                        // Get the specified prefix and uri
                        prefix = prefix == null ? "" : prefix;
                        uri = uri == null || "" .equals(uri) ?
                                fault.getNamespace().getNamespaceURI() : uri;
                        // Make sure the prefix and uri are declared on the fault, and 
                        // get the resulting prefix.
                        prefix = fault.declareNamespace(uri, prefix).getPrefix();
                        soapFaultCode = prefix + ":" + faultCodeQName.getLocalPart();
                        if(log.isDebugEnabled()){
                        	log.debug("Altered soapFaultCode ="+soapFaultCode);
                        }
                    } else {
                        soapFaultCode = faultCodeQName.getLocalPart();
                    }
                }
            }
        }

        // defaulting to fault code Receiver, if no message is available
        if (faultCode == null && context.getEnvelope() != null) {
            soapFaultCode = ("".equals(soapFaultCode) || (soapFaultCode == null))
                    ? SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX + ":" +
                        context.getEnvelope().getVersion().getReceiverFaultCode().getLocalPart()
                    : soapFaultCode;
        }
        
        if (faultCode == null) {
        	if(log.isDebugEnabled()){
        		log.debug("faultCode == null");
        	}
            if (context.isSOAP11()) {
            	if(log.isDebugEnabled()){
            		log.debug("context.isSOAP11() = true");
            		SOAPFaultCode code = (fault!=null)?fault.getCode():null;
            		SOAPFaultValue value = (code!=null)?code.getValue():null;
            		if(value !=null){
            			QName name = value.getQName();
            			log.debug("prefix ="+name.getPrefix());
            			log.debug("Fault Code namespace ="+name.getNamespaceURI());
            			log.debug("Fault Code ="+name.getLocalPart());
            		}
            	}

                fault.getCode().setText(soapFaultCode);
            } else {
            	if(log.isDebugEnabled()){
            		log.debug("context.isSOAP11() = false");
            		SOAPFaultCode code = (fault!=null)?fault.getCode():null;
            		SOAPFaultValue value = (code!=null)?code.getValue():null;
            		if(value !=null){
            			QName name = value.getQName();
            			log.debug("prefix ="+name.getPrefix());
            			log.debug("Fault Code namespace ="+name.getNamespaceURI());
            			log.debug("Fault Code ="+name.getLocalPart());
            		}
            	}
                SOAPFaultValue value = fault.getCode().getValue();
                if(log.isDebugEnabled()){
                    log.debug("soapFaultCode originally was set to : " + soapFaultCode);
                }
                OMNamespace namespace = value.getNamespace();
                soapFaultCode = switchNamespacePrefix(soapFaultCode, namespace);
                value.setText(soapFaultCode);
            }
        }
        
        if (axisFault != null && !context.isSOAP11()) {
            if (axisFault.getFaultSubCodes() != null) {
                
                List faultSubCodes = axisFault.getFaultSubCodes();
                
                QName faultSubCodeQName;

                for (Object faultSubCode : faultSubCodes) {

                    faultSubCodeQName = (QName)faultSubCode;

                    SOAPFactory sf = (SOAPFactory)envelope.getOMFactory();
                    SOAPFaultSubCode soapFaultSubCode = sf.createSOAPFaultSubCode(fault.getCode());
                    SOAPFaultValue saopFaultValue = sf.createSOAPFaultValue(fault.getCode());
                    saopFaultValue.setText(faultSubCodeQName);
                    soapFaultSubCode.setValue(saopFaultValue);
                    fault.getCode().setSubCode(soapFaultSubCode);
                }
                
            } 
        }

        SOAPFaultReason faultReason = (SOAPFaultReason)context.getProperty(
                                            SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME);

        if (faultReason == null && axisFault != null) {
            faultReason = axisFault.getFaultReasonElement();
        }
        if (faultReason != null) {
            fault.setReason(faultReason);
        } else {
            String message = "";
            if (soapException != null) {
                message = soapException.getMessage();
            } else if (axisFault != null) {
                // Couldn't find FaultReasonElement, try reason string
                message = axisFault.getReason();
            }

            if (message == null || "".equals(message)) {
                message = getFaultReasonFromException(e, context);
            }

            if (message == null || "".equals(message)) message = "unknown";

            if (context.isSOAP11()) {
                fault.getReason().setText(message);
            } else {
                fault.getReason().getFirstSOAPText().setLang("en-US");
                fault.getReason().getFirstSOAPText().setText(message);
            }
        }

        Object faultRole = context.getProperty(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME);
        if (faultRole != null) {
            fault.getRole().setText((String) faultRole);
        } else if (axisFault != null) {
            if (axisFault.getFaultRoleElement() != null) {
                fault.setRole(axisFault.getFaultRoleElement());
            }
        }

        Object faultNode = context.getProperty(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME);
        if (faultNode != null) {
            SOAPFaultNode soapFaultNode = fault.getNode();
            if(soapFaultNode != null) {
                soapFaultNode.setText((String) faultNode);
            }
        } else if (axisFault != null) {
            if (axisFault.getFaultNodeElement() != null) {
                fault.setNode(axisFault.getFaultNodeElement());
            }
        }

        // Allow handlers to override the sendStacktraceDetailsWithFaults setting from the Configuration to allow
        // WS-* protocol faults to not include the exception.
        boolean sendStacktraceDetailsWithFaults = false;
        OperationContext oc = context.getOperationContext();
        Object flagFromContext = null;
        if (oc != null) {
            flagFromContext = context.getOperationContext()
                    .getProperty(Constants.Configuration.SEND_STACKTRACE_DETAILS_WITH_FAULTS);
        }
        if (flagFromContext != null) {
            sendStacktraceDetailsWithFaults = JavaUtils.isTrue(flagFromContext);
        } else {
            Parameter param = context.getParameter(
                    Constants.Configuration.SEND_STACKTRACE_DETAILS_WITH_FAULTS);
            if (param != null) {
                sendStacktraceDetailsWithFaults = JavaUtils.isTrue(param.getValue());
            }
        }

        Object faultDetail = context.getProperty(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME);
        if (faultDetail != null) {
            fault.setDetail((SOAPFaultDetail) faultDetail);
        } else if (axisFault != null) {
            if (axisFault.getFaultDetailElement() != null) {
                fault.setDetail(axisFault.getFaultDetailElement());
            } else {
                OMElement detail = axisFault.getDetail();
                if (detail != null) {
                    fault.getDetail().addDetailEntry(detail);
                } else if (sendStacktraceDetailsWithFaults) {
                    fault.setException(axisFault);
                }
            }
        } else if (fault.getException() == null && sendStacktraceDetailsWithFaults) {
            if (e instanceof Exception) {
                fault.setException((Exception) e);
            } else {
                fault.setException(new Exception(e));
            }
        }
        
        if(log.isDebugEnabled())
            log.debug("End createFaultEnvelope()");
        return envelope;
    }
    
    /**
     * Switch the namespace prefix in the soap fault code. It should match the prefix used
     * by the outgoing soap envelope.
     * 
     * @param soapFaultCode
     * @param namespace
     * @return
     */
    public static String switchNamespacePrefix(String soapFaultCode, OMNamespace namespace) {
        if(soapFaultCode != null && 
                soapFaultCode.endsWith(":" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH)) {
            String prefix = namespace.getPrefix();
            if(log.isDebugEnabled()){
                log.debug("prefix being used in the outgoing soap envelope is : " + prefix);
            }
            // The following methods set the prefix of the incoming soap envelope in soapFaultCode 
            //    validateSOAPVersion method in BuilderUtil or 
            //    identifySOAPVersion method in StAXSOAPModelBuilder 
            // But the outgoing envelope may have a different prefix, if it does then
            // we need to strip the prefix set by those methods and add the
            // correct one.
            if (!soapFaultCode.startsWith(prefix + ":")) {
                if(log.isDebugEnabled()){
                    log.debug("stripping old prefix and adding the new one - " + prefix);
                }
                // Strip the original prefix
                int index = soapFaultCode.indexOf(':') + 1;
                soapFaultCode = soapFaultCode.substring(index);
                // Use the correct prefix for the outgoing soap envelope namespace 
                soapFaultCode = prefix + ":" + soapFaultCode;
            }
        }
        if(log.isDebugEnabled()){
                    log.debug("soapFaultCode is being set to : " + soapFaultCode);
        }
        return soapFaultCode;
    }
    
    /**
     * By the time the exception comes here it can be wrapped by so many levels. This will crip down
     * to the root cause and get the initial error depending on the property
     *
     * @param e exception to get the string representation
     * @param context current message context for which the exception occurred
     * @return generated fault reason as a string
     */
    private static String getFaultReasonFromException(Throwable e, MessageContext context) {
        Throwable throwable = e;
        Parameter param = context.getParameter(
                Constants.Configuration.DRILL_DOWN_TO_ROOT_CAUSE_FOR_FAULT_REASON);
        boolean drillDownToRootCauseForFaultReason =
                param != null && ((String) param.getValue()).equalsIgnoreCase("true");
        if (drillDownToRootCauseForFaultReason) {
            while (throwable.getCause() != null) {
                throwable = throwable.getCause();
            }
        }
        return throwable.getMessage();
    }

}
