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

package org.apache.axis2.jaxws.context;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceUtils;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.security.Principal;

public class WebServiceContextImpl implements WebServiceContext {

    private static final Log log = LogFactory.getLog(WebServiceContext.class);
    
    private MessageContext soapMessageContext;

    public WebServiceContextImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.WebServiceContext#getMessageContext()
     */
    public MessageContext getMessageContext() {
        
        // Note that the MessageContext might not be set up yet, or it
        // may have been released because the lifetime of the WebServiceContext is completed.
        if (log.isDebugEnabled()) {
            if (soapMessageContext == null) {
                log.debug("The MessageContext is not available");
            }
        }
        return soapMessageContext;
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.WebServiceContext#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        
        // Note that the MessageContext might not be set up yet, or it
        // may have been released because the lifetime of the WebServiceContext is completed.
        if (log.isDebugEnabled()) {
            if (soapMessageContext == null) {
                log.debug("The MessageContext is not available");
            }
        }
        
        if (soapMessageContext != null) {
            HttpServletRequest request = (HttpServletRequest) soapMessageContext.get(MessageContext.SERVLET_REQUEST);
            if (request != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Access to the user Principal was requested.");
                }
                return request.getUserPrincipal();    
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("No HttpServletRequest object was found, so no Principal can be found.");
                }
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.WebServiceContext#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String user) {
        
        // Note that the MessageContext might not be set up yet, or it
        // may have been released because the lifetime of the WebServiceContext is completed.
        if (log.isDebugEnabled()) {
            if (soapMessageContext == null) {
                log.debug("The MessageContext is not available");
            }
        }
        
        if (soapMessageContext != null) {
            HttpServletRequest request = (HttpServletRequest) soapMessageContext.get(MessageContext.SERVLET_REQUEST);
            if (request != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Checking to see if the user in the role.");
                }
                return request.isUserInRole(user);    
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("No HttpServletRequest object was found, so no role check can be performed.");
                }
            }
        }
        
        return false;
    }

    public void setSoapMessageContext(MessageContext soapMessageContext) {
        this.soapMessageContext = soapMessageContext;
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
        
        // Note that the MessageContext might not be set up yet, or it
        // may have been released because the lifetime of the WebServiceContext is completed.
        if (log.isDebugEnabled()) {
            if (soapMessageContext == null) {
                log.debug("The MessageContext is not available");
            }
        }
        
        EndpointReference jaxwsEPR = null;
        String addressingNamespace = EndpointReferenceUtils.getAddressingNamespace(clazz);
        
        if (soapMessageContext != null) {
            QName service = (QName) soapMessageContext.get(MessageContext.WSDL_SERVICE);
            QName endpoint = (QName) soapMessageContext.get(MessageContext.WSDL_PORT);
            
            org.apache.axis2.addressing.EndpointReference axis2EPR =
                EndpointReferenceUtils.createAxis2EndpointReference(null, service, endpoint, null, addressingNamespace);
            
            try {
                EndpointReferenceUtils.addReferenceParameters(axis2EPR, referenceParameters);
                jaxwsEPR = EndpointReferenceUtils.convertFromAxis2(axis2EPR, addressingNamespace);
            }
            catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(
                       Messages.getMessage("endpointRefConstructionFailure3", e.toString()));
            }
        }
        else {
            throw new IllegalStateException(
                      Messages.getMessage("webServiceContextErr1"));
        }
        
        return clazz.cast(jaxwsEPR);
    }

    public EndpointReference getEndpointReference(Element... referenceParameters) {
        return getEndpointReference(W3CEndpointReference.class, referenceParameters);
    }
    
    /**
     * Release objects held by WebServiceContext so that they can be garbage collected.
     */
    public void releaseResources() {
        if (log.isDebugEnabled()) {
            log.debug("Releasing WebServiceContextImpl resources");
        }
        soapMessageContext = null; // unlink the soapMessageContxt so that it can be gc'd
    }
}
