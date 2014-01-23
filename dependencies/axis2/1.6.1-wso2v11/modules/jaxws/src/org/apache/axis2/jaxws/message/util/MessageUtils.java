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

package org.apache.axis2.jaxws.message.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.handler.AttachmentsAdapter;
import org.apache.axis2.jaxws.handler.SOAPHeadersAdapter;
import org.apache.axis2.jaxws.handler.TransportHeadersAdapter;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/** Miscellaneous Utilities that may be useful inside and outside the Message subcomponent. */
public class MessageUtils {

    private static final Log log = LogFactory.getLog(MessageUtils.class);

    /**
     * Get an axiom SOAPFactory for the specified element
     *
     * @param e OMElement
     * @return SOAPFactory
     */
    public static SOAPFactory getSOAPFactory(OMElement e) {
        // Getting a factory from a SOAPEnvelope is not straight-forward.
        // Please change this code if an easier mechanism is discovered.

        OMXMLParserWrapper builder = e.getBuilder();
        if (builder instanceof StAXBuilder) {
            StAXBuilder staxBuilder = (StAXBuilder)builder;
            OMDocument document = staxBuilder.getDocument();
            if (document != null) {
                OMFactory factory = document.getOMFactory();
                if (factory instanceof SOAPFactory) {
                    return (SOAPFactory)factory;
                }
            }
        }
        // Flow to here indicates that the envelope does not have
        // an accessible factory.  Create a new factory based on the 
        // protocol.

        while (e != null && !(e instanceof SOAPEnvelope)) {
            e = (OMElement)e.getParent();
        }
        if (e instanceof SOAPEnvelope) {
            if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.
                    equals(e.getNamespace().getNamespaceURI())) {
                return OMAbstractFactory.getSOAP11Factory();
            } else {
                return OMAbstractFactory.getSOAP12Factory();
            }
        }
        return null;
    }

    /**
     * Create an SAAJ AttachmentPart from a JAXWS Attachment
     * @param cid String content id
     * @param dh DataHandler
     * @param message SOAPMessage
     * @return AttachmentPart
     */
    public static AttachmentPart createAttachmentPart(String cid, DataHandler dh, SOAPMessage message) {
        // Create the Attachment Part
        AttachmentPart ap = message.createAttachmentPart(dh);
        
        // REVIEW
        // Do we need to copy the content type from the datahandler ?
        
        // Preserve the original content id
        ap.setContentId(cid);
        return ap;
    }


    /**
     * Create a JAX-WS Message from the information on an Axis 2 Message Context
     *
     * @param msgContext
     * @return Message
     */
    public static Message getMessageFromMessageContext(MessageContext msgContext)
            throws WebServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Start getMessageFromMessageContext");
        }

        Message message = null;
        // If the Axis2 MessageContext that was passed in has a SOAPEnvelope
        // set on it, grab that and create a JAX-WS Message out of it.
        SOAPEnvelope soapEnv = msgContext.getEnvelope();
        if (soapEnv != null) {
            MessageFactory msgFactory =
                    (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            try {
                Protocol protocol = msgContext.isDoingREST() ? Protocol.rest : null;
                message = msgFactory.createFrom(soapEnv, protocol);
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(
                		Messages.getMessage("msgFromMsgErr"), e);
            }

            Object property = msgContext.getProperty(Constants.Configuration.ENABLE_MTOM);
            if (property != null && JavaUtils.isTrueExplicitly(property)) {
                message.setMTOMEnabled(true);
            }

            // Add all the MimeHeaders from the Axis2 MessageContext
            Map headerMap = (Map)msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (headerMap != null) {
                message.setMimeHeaders(headerMap);
            }
            
            // TODO: This is a WORKAROUND for missing SOAPFault data.  If we do a toString on the
            // SOAPEnvelope, then all the data will be available to the provider.  Otherwise, it
            // will be missing the <Reason> element corresponding to the <faultstring> element.  
            // The SOAPFaultProviderTests will check for this failure.
            if (soapEnv.hasFault()) {
                soapEnv.toString();
            }
        }
        return message;
    }

    /**
     * Put the JAX-WS Message onto the Axis2 MessageContext
     *
     * @param message    JAX-WS Message
     * @param msgContext Axis2MessageContext
     */
    public static void putMessageOnMessageContext(Message message, MessageContext msgContext)
            throws AxisFault, WebServiceException {
        // Put the XML message on the Axis 2 Message Context
        SOAPEnvelope envelope = (SOAPEnvelope)message.getAsOMElement();
        msgContext.setEnvelope(envelope);

        // Put the Headers onto the MessageContext
        Map headerMap = message.getMimeHeaders();
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
        msgContext.setProperty(HTTPConstants.HTTP_HEADERS, headerMap);

        if (message.getProtocol() == Protocol.rest) {
            msgContext.setDoingREST(true);
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
        }
        
        // Detect if a SOAPAction header is set as a Mime header in request message
        String soapAction = (String) headerMap.get("SOAPAction");
        if (soapAction != null) {
        	if (log.isDebugEnabled()) {
        		log.debug("Found SOAPAction as Mime header: " + soapAction);
        	}
        	// Set on MessageContext and it will be written out in request
        	msgContext.setSoapAction(soapAction);
        }
        
        // Make sure the the JAX-WS AttachmentAdapter is correctly installed
        // So that any user attachments provide are moved to the Axiom Attachments
        // Map
        if (message.getMessageContext() != null) {
            AttachmentsAdapter.install(message.getMessageContext());
            TransportHeadersAdapter.install(message.getMessageContext());
            SOAPHeadersAdapter.install(message.getMessageContext());
        }
        
        if (message.isDoingSWA()) {
            // Enable SWA on the Axis2 MessageContext
            msgContext.setDoingSwA(true);
            msgContext.setProperty(Configuration.ENABLE_SWA, "true");
        }

        // Enable MTOM Attachments 
        if (message.isMTOMEnabled()) {
            // Enable MTOM on the Axis2 MessageContext
            msgContext.setProperty(Configuration.ENABLE_MTOM, "true");
        }
    }
    
    /**
     * This is for debug purposes only
     * @param mc
     */
    private static void persistMessageContext(MessageContext mc) {
        try {
            ConfigurationContext cc = mc.getConfigurationContext();
            OperationContext op = mc.getOperationContext();
            if (cc == null && op != null) {
                cc = op.getConfigurationContext();
            }
            
            File theFile = null;
            theFile = File.createTempFile("DebugPersist", null);
            
            // Setup an output stream to a physical file
            FileOutputStream outStream = new FileOutputStream(theFile);

            // Attach a stream capable of writing objects to the 
            // stream connected to the file
            ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

            // Try to save the message context
            outObjStream.writeObject(mc);
            outObjStream.flush();
            outObjStream.close();
            outStream.flush();
            outStream.close();
            
            // Now read in the persisted message
            // Setup an input stream to the file
            FileInputStream inStream = new FileInputStream(theFile);
            
            // attach a stream capable of reading objects from the 
            // stream connected to the file
            ObjectInputStream inObjStream = new ObjectInputStream(inStream);

            org.apache.axis2.context.MessageContext restoredMC = 
                (org.apache.axis2.context.MessageContext) inObjStream.readObject();
            inObjStream.close();
            inStream.close();
            if (cc == null && op == null) {
                return;
            }
            
            if (cc != null) {
                restoredMC.activate(cc);
            } else {
                restoredMC.activateWithOperationContext(op);
            }
            if (restoredMC.getServiceContext() == null) {
                throw ExceptionFactory.makeWebServiceException("No Service Group!");
            }
            if (cc != null) {
                mc.activate(cc);
            } else {
                mc.activateWithOperationContext(op);
            }
            if (mc.getOperationContext() == null) {
                throw new RuntimeException("No Operation Context");
            }
            if (mc.getOperationContext().getServiceContext() == null) {
                throw new RuntimeException("No Service Context");
            }
            return;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
        return;
    }
}
