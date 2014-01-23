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

package org.apache.axis2.jaxws.binding;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.utility.SAAJFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.AddressingFeature.Responses;

import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of the <link>javax.xml.ws.soap.SOAPBinding</link>
 * interface. This is the default binding for JAX-WS, and will exist for all
 * Dispatch and Dynamic Proxy instances unless the XML/HTTP Binding is
 * explicitly specificied.
 */
public class SOAPBinding extends BindingImpl implements javax.xml.ws.soap.SOAPBinding {
    private boolean mtomEnabled = false;
    private int mtomThreshold = 0;
    private boolean respectBindingEnabled = false;
    private boolean addressingConfigured = false;
    private boolean addressingEnabled = false;
    private boolean addressingRequired = false;
    private Responses addressingResponses = Responses.ALL;

    private static Log log = LogFactory.getLog(SOAPBinding.class);
    
    private EndpointReference epr;
    
    private String addressingNamespace;

    public SOAPBinding(EndpointDescription endpointDesc) {
        super(endpointDesc);
    }

    public int getMTOMThreshold() {
        return mtomThreshold;
    }
    public void setMTOMThreshold(int threshold) {
        mtomThreshold = threshold;
    }
    public boolean isRespectBindingEnabled() {
        return respectBindingEnabled;
    }
    public void setRespectBindingEnabled(boolean enabled) {
        respectBindingEnabled = enabled;
    }
    
    /**
     * Indicates if Addressing was configured explicitly via metadata, such as through a deployment descriptor.
     * If an AddressingAnnot was specified in the DBC, then this will answer true.  The related addressing methods
     * will return a default value if this method returns false.
     * @see #isAddressingEnabled()
     * @see #isAddressingRequired()
     * @see #getAddressingResponses()
     * @return true if addressing was explicitly configured via an AddressingAnnot in the DBC; false otherwise.
     */
    public boolean isAddressingConfigured() {
        return addressingConfigured;
    }
    
    /**
     * Set whether Addressing was explicitly configured via metadata.  The default is false.
     * @param configured boolean indicating of addressing was configured via metadata.
     */
    public void setAddressingConfigured(boolean configured) {
        addressingConfigured = configured;
    }
    
    /**
     * Indicates if addressing is enabled or disabled.
     * Note that if addressing was not explicitly configured via metadata, this will return a default value.
     * @see #isAddressingConfigured()
     * @return true if addressing is enabled, false (default) otherwise.
     */
    public boolean isAddressingEnabled() {
        return addressingEnabled;
    }
    public void setAddressingEnabled(boolean enabled) {
        addressingEnabled = enabled;
    }
    
    /**
     * Indicates if addressing is required or not.
     * Note that if addressing was not explicitly configured via metadata, this will return a default value.
     * @see #isAddressingConfigured()
     * @return true if addressing is required, false (default) otherwise.
     */
    public boolean isAddressingRequired() {
        return addressingRequired;
    }
    public void setAddressingRequired(boolean required) {
        addressingRequired = required;
    }
    
    /**
     * Return the type of responses required by Addressing.
     * Note that if addressing was not explicitly configured via metadata, this will return a default value.
     * @see #isAddressingConfigured()
     * @return AddressingFeature.Responses ENUM value indicating what type of addressing responses are required. 
     */
    public Responses getAddressingResponses() {
        return addressingResponses;
    }
    public void setAddressingResponses(Responses responses) {
        addressingResponses = responses;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.soap.SOAPBinding#getMessageFactory()
     */
    public MessageFactory getMessageFactory() {
        String bindingNamespace = null;
        try {
            /*
             * SAAJFactory.createMessageFactory takes a namespace String as a
             * param: "http://schemas.xmlsoap.org/soap/envelope/" (SOAP1.1)
             * "http://www.w3.org/2003/05/soap-envelope" (SOAP1.2)
             * 
             * The bindingId will be in one of the following forms:
             * "http://schemas.xmlsoap.org/wsdl/soap/http" (SOAP1.1)
             * "http://www.w3.org/2003/05/soap/bindings/HTTP/" (SOAP1.2)
             */
        	
            if (BindingUtils.isSOAP12Binding(bindingId)){
            	bindingNamespace = SOAP12_ENV_NS;
            } else {
                // TODO currently defaults to SOAP11. Should we be more stricct
                // about checking?
                bindingNamespace = SOAP11_ENV_NS;
            }
            return SAAJFactory.createMessageFactory(bindingNamespace);
        } catch (WebServiceException e) {
            // TODO log it and then what?
            if (log.isDebugEnabled()) {
                log.debug("WebServiceException calling SAAJFactory.createMessageFactory(\""
                                + bindingNamespace + "\")");
            }
        } catch (SOAPException e) {
            // TODO log it and then what?
            if (log.isDebugEnabled()) {
                log.debug("SOAPException calling SAAJFactory.createMessageFactory(\""
                                + bindingNamespace + "\")");
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.soap.SOAPBinding#getRoles()
     */
    public Set<String> getRoles() {
        // do not allow null roles, per the JAX-WS CTS
        if (roles == null)
            roles = addDefaultRoles(null);
        return roles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.soap.SOAPBinding#getSOAPFactory()
     */
    public SOAPFactory getSOAPFactory() {
        String bindingNamespace = null;
        try {
            /*
             * SAAJFactory.createMessageFactory takes a namespace String as a
             * param: "http://schemas.xmlsoap.org/soap/envelope/" (SOAP1.1)
             * "http://www.w3.org/2003/05/soap-envelope" (SOAP1.2)
             * 
             * The bindingId will be in one of the following forms:
             * "http://schemas.xmlsoap.org/wsdl/soap/http" (SOAP1.1)
             * "http://www.w3.org/2003/05/soap/bindings/HTTP/" (SOAP1.2)
             */
            if (bindingId.equalsIgnoreCase(SOAPBinding.SOAP12HTTP_BINDING)
                            || bindingId.equalsIgnoreCase(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                bindingNamespace = SOAP12_ENV_NS;
            } else {
                // TODO currently defaults to SOAP11. Should we be more stricct
                // about checking?
                bindingNamespace = SOAP11_ENV_NS;
            }
            return SAAJFactory.createSOAPFactory(bindingNamespace);
        } catch (WebServiceException e) {
            // TODO log it and then what?
            if (log.isDebugEnabled()) {
                log.debug("WebServiceException calling SAAJFactory.createSOAPFactory(\""
                                + bindingNamespace + "\")");
            }
        } catch (SOAPException e) {
            // TODO log it and then what?
            if (log.isDebugEnabled()) {
                log.debug("SOAPException calling SAAJFactory.createSOAPFactory(\""
                                + bindingNamespace + "\")");
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.soap.SOAPBinding#isMTOMEnabled()
     */
    public boolean isMTOMEnabled() {
        return mtomEnabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.soap.SOAPBinding#setMTOMEnabled(boolean)
     */
    public void setMTOMEnabled(boolean flag) {
        mtomEnabled = flag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.soap.SOAPBinding#setRoles(java.util.Set)
     */
    public void setRoles(Set<String> set) {
        // Validate the values in the set
        if (set != null && !set.isEmpty()) {
            // Throw an exception for setting a role of "none"
            // Per JAXWS 2.0 Sec 10.1.1.1 SOAP Roles, page 116:
            if (set.contains(SOAPConstants.URI_SOAP_1_2_ROLE_NONE)) {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("roleValidatioErr"));
            }
        }
        
        roles = addDefaultRoles(set);
    }
    private Set<String> addDefaultRoles(Set<String> set) {
        
        Set<String> returnSet = set;
        if (returnSet == null) {
            returnSet = new HashSet<String>();
        }
        
        // Make sure the defaults for the binding type are in the set
        // and add them if not.  Note that for both SOAP11 and SOAP12, the role of ultimate
        // reciever can be indicated by the omission of the role attribute.
        // REVIEW: This is WRONG because the bindingID from the WSDL is not the same value as
        //   SOAPBinding annotation constants.  There are other places in the code that have
        //   this same issue I am sure.
        if (SOAPBinding.SOAP12HTTP_BINDING.equals(bindingId)
                || SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(bindingId)) {
            if (returnSet.isEmpty() || !returnSet.contains(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT)) {
                returnSet.add(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
            }
            
        } else {
            if (returnSet.isEmpty() || !returnSet.contains(SOAPConstants.URI_SOAP_ACTOR_NEXT)) {
                returnSet.add(SOAPConstants.URI_SOAP_ACTOR_NEXT);
            }
        }
        return returnSet;
    }

    @Override
    public String getAddressingNamespace() {
        return addressingNamespace;
    }

    @Override
    public EndpointReference getAxis2EndpointReference() {
        return epr;
    }

    @Override
    public void setAddressingNamespace(String addressingNamespace) {
        this.addressingNamespace = addressingNamespace;
    }

    @Override
    public void setAxis2EndpointReference(EndpointReference epr) {
        this.epr = epr;
    }
}
