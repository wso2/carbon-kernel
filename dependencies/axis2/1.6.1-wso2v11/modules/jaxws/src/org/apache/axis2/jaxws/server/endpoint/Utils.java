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

package org.apache.axis2.jaxws.server.endpoint;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultCode;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.ws.http.HTTPBinding;
import java.util.Collection;
import java.util.Iterator;

public class Utils {
    
    public static final Log log = LogFactory.getLog(Utils.class);
    
    @Deprecated
    /**
     * Compares the version of the message in the MessageContext to what's expected
     * given the ServiceDescription.  The behavior is described in the SOAP 1.2
     * specification under Appendix 'A'.
     * 
     * @param mc
     * @param serviceDesc
     * @return
     */
    public static boolean bindingTypesMatch(MessageContext mc, ServiceDescription serviceDesc) {
        Collection<EndpointDescription> eds = serviceDesc.getEndpointDescriptions_AsCollection();

        // Dispatch endpoints do not have SEIs, so watch out for null or empty array
        if ((eds != null) && (eds.size() > 0)) {
            Iterator<EndpointDescription> i = eds.iterator();
            if (i.hasNext()) {
                EndpointDescription ed = eds.iterator().next();
                
                Protocol protocol = mc.getMessage().getProtocol();
                String bindingType = ed.getBindingType();
                
                if (log.isDebugEnabled()) {
                    log.debug("Checking for matching binding types.");
                    log.debug("    message protocol: " + protocol);
                    log.debug("        binding type: " + bindingType);
                }
                
                if (protocol.equals(Protocol.soap11)) { 
                	return (BindingUtils.isSOAP11Binding(bindingType));
                } else if (protocol.equals(Protocol.soap12)) {
                	return (BindingUtils.isSOAP12Binding(bindingType));               	
                } else if (protocol.equals(Protocol.rest)) {
                    return HTTPBinding.HTTP_BINDING.equalsIgnoreCase(bindingType);
                }                
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("There were no endpoint descriptions found, thus the binding match failed.");
                }
                return false; 
            }
        }
        return true;
    }
    
    /**
     * Compares the version of the message in the MessageContext to what's expected
     * given the ServiceDescription.  The behavior is described in the SOAP 1.2
     * specification under Appendix 'A'.
     * 
     * @param mc
     * @param serviceDesc
     * @return
     */
    public static boolean bindingTypesMatch(MessageContext mc, EndpointDescription ed) {
        
        Protocol protocol = mc.getMessage().getProtocol();
        String bindingType = ed.getBindingType();
        
        if (log.isDebugEnabled()) {
            log.debug("Checking for matching binding types.");
            log.debug("    message protocol: " + protocol);
            log.debug("        binding type: " + bindingType);
        }
        
        if (protocol.equals(Protocol.soap11)) { 
                return (BindingUtils.isSOAP11Binding(bindingType));
        } else if (protocol.equals(Protocol.soap12)) {
                return (BindingUtils.isSOAP12Binding(bindingType));                     
        } else if (protocol.equals(Protocol.rest)) {
            return HTTPBinding.HTTP_BINDING.equalsIgnoreCase(bindingType);
        }               
        return true;
    }
    
    /**
     * Creates a fault message that reflects a version mismatch for the configured message protocol.
     * The returned message will always be a SOAP 1.1 message per the specification.
     * 
     * @param mc
     * @param msg
     * @return
     */
    public static MessageContext createVersionMismatchMessage(MessageContext mc, Protocol protocol) {
        // Only if protocol is soap12 and MISmatches the endpoint do we halt processing
        if (protocol.equals(Protocol.soap12)) {
            String msg = "Incoming SOAP message protocol is version 1.2, but endpoint is configured for SOAP 1.1";
            return Utils.createFaultMessage(mc, msg);
        } else if (protocol.equals(Protocol.soap11)) {
            // SOAP 1.1 message and SOAP 1.2 binding

            // The canSupport flag indicates that we can support this scenario.
            // Possible Examples of canSupport:  JAXB impl binding, JAXB Provider
            // Possible Example of !canSupport: Application handler usage, non-JAXB Provider
            // Initially I vote to hard code this as false.
            boolean canSupport = false;
            if (canSupport) {
                // TODO: Okay, but we need to scrub the Message create code to make sure that the response message
                // is always built from the receiver protocol...not the binding protocol
                return null;
            } else {
                String msg = "Incoming SOAP message protocol is version 1.1, but endpoint is configured for SOAP 1.2.  This is not supported.";
                return Utils.createFaultMessage(mc, msg);
            }
        } else {
            String msg = "Incoming message protocol does not match endpoint protocol.";
            return Utils.createFaultMessage(mc, msg);
        }
    }
    
    public static MessageContext createFaultMessage(MessageContext mc, String msg) {
        try {
            XMLFault xmlfault =
                    new XMLFault(XMLFaultCode.VERSIONMISMATCH, new XMLFaultReason(msg));
            MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
            Message message = mf.create(Protocol.soap11);
            message.setXMLFault(xmlfault);
            
            MessageContext responseMsgCtx = MessageContextUtils.createFaultMessageContext(mc);
            responseMsgCtx.setMessage(message);
            return responseMsgCtx;
        } catch (XMLStreamException e) {
            // Need to fix this !   At least provide logging
            // TODO for now, throw it.  We probably should try to make an XMLFault object and set it on the message
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    /*
     * Gets the OperationDescription associated with the request that is currently
     * being processed.
     * 
     *  Note that this is not done in the EndpointController since operations are only relevant
     *  to Endpoint-based implementation (i.e. not to Proxy-based ones)s
     */
    public static OperationDescription getOperationDescription(MessageContext mc) {
        OperationDescription op = null;
        
        op = mc.getOperationDescription();
        if (op == null) {
            if (log.isDebugEnabled()) {
                log.debug("No OperationDescription found on MessageContext, searching existing operations");
            }

            EndpointDescription ed = mc.getEndpointDescription();
            EndpointInterfaceDescription eid = ed.getEndpointInterfaceDescription();

            OperationDescription[] ops = eid.getDispatchableOperation(mc.getOperationName());
            // TODO: Implement signature matching.  Currently only matching on the wsdl:OperationName is supported.
            //       That means that overloading of wsdl operations is not supported (although that's not supported in 
            //       WSDL 1.1 anyway).
            if (ops == null || ops.length == 0) {
                throw ExceptionFactory.makeWebServiceException(
                		Messages.getMessage("oprDescrErr",mc.getOperationName().toString()));
            }
            if (ops.length > 1) {
                throw ExceptionFactory.makeWebServiceException(
                		Messages.getMessage("oprDescrErr1",mc.getOperationName().toString()));
            }
            op = ops[0];
            if (log.isDebugEnabled()) {
                log.debug("wsdl operation: " + op.getName());
                log.debug("   java method: " + op.getJavaMethodName());
            }
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("An existing OperationDescription was found on the MessageContext.");
            }
        }
        
        return op;
    }

}
