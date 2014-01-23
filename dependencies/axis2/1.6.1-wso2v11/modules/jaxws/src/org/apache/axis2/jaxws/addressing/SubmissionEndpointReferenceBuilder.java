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

package org.apache.axis2.jaxws.addressing;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceUtils;
import org.apache.axis2.jaxws.i18n.Messages;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used to create instances of {@link SubmissionEndpointReference}.
 *
 * @see javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder
 */
public final class SubmissionEndpointReferenceBuilder {
	private static final Element[] ZERO_LENGTH_ARRAY = new Element[0];

	private String address;
    private QName serviceName;
    private QName endpointName;
    private String wsdlDocumentLocation;
    private List<Element> referenceParameters;
    private QName portType;
    
    /**
     * Constructor
     *
     */
    public SubmissionEndpointReferenceBuilder() {
    }
    
    /**
     * Add the address URI to use.
     * 
     * @param address the address URI
     * @return an instance of <code>SubmissionEndpointReferenceBuilder</code> that has
     * been updated as specified.
     */
    public SubmissionEndpointReferenceBuilder address(String address) {
        this.address = address;
        return this;
    }
    
    /**
     * Add the WSDL service name of the endpoint that the endpoint reference will target.
     * 
     * @param serviceName the WSDL service name
     * @return an instance of <code>SubmissionEndpointReferenceBuilder</code> that has
     * been updated as specified.
     */
    public SubmissionEndpointReferenceBuilder serviceName(QName serviceName) {
        this.serviceName = serviceName;
        return this;
    }
    
    /**
     * Add the WSDL port name of the endpoint that the endpoint reference will target.
     * The WSDL port name can only be set after the WSDL service name has been set.
     * 
     * @param endpointName the WSDL port name
     * @return an instance of <code>SubmissionEndpointReferenceBuilder</code> that has
     * been updated as specified.
     */
    public SubmissionEndpointReferenceBuilder endpointName(QName endpointName) {
        if (this.serviceName == null) {
            throw new IllegalStateException(Messages.getMessage("endpointQNameSetError", 
                                                                endpointName.toString()));
        }
        
        this.endpointName = endpointName;
        return this;
    }
    
    /**
     * Add the URI from where the WSDL for the endpoint that the endpoint reference will
     * target can be retrieved.
     * 
     * @param wsdlDocumentLocation the location URI of the WSDL
     * @return an instance of <code>SubmissionEndpointReferenceBuilder</code> that has
     * been updated as specified.
     */
    public SubmissionEndpointReferenceBuilder wsdlDocumentLocation(String wsdlDocumentLocation) {
        this.wsdlDocumentLocation = wsdlDocumentLocation;
        return this;
    }
    
    /**
     * Add reference properties. These will appear in the endpoint reference as reference
     * parameters.
     * 
     * @param referenceProperty the reference property
     * @return an instance of <code>SubmissionEndpointReferenceBuilder</code> that has
     * been updated as specified.
     */
    public SubmissionEndpointReferenceBuilder referenceProperty(Element referenceProperty) {
        if (referenceProperty == null) {
            throw new IllegalArgumentException(Messages.getMessage("referencePropertyNullErr"));
        }
        
        if (this.referenceParameters == null) {
            this.referenceParameters = new ArrayList<Element>();
        }
        
        this.referenceParameters.add(referenceProperty);
        return this;
    }
    
    /**
     * Add reference parameters.
     * 
     * @param referenceParameter the reference parameter
     * @return an instance of <code>SubmissionEndpointReferenceBuilder</code> that has
     * been updated as specified.
     */
    public SubmissionEndpointReferenceBuilder referenceParameter(Element referenceParameter) {
        if (referenceParameter == null) {
            throw new IllegalArgumentException(Messages.getMessage("referenceParameterNullErr"));
        }
        
        if (this.referenceParameters == null) {
            this.referenceParameters = new ArrayList<Element>();
        }
        
        this.referenceParameters.add(referenceParameter);
        return this;
    }
    
    /**
     * Add the name of the WSDL port type.
     * 
     * @param portType the WSDL port type name
     * @return an instance of <code>SubmissionEndpointReferenceBuilder</code> that has
     * been updated as specified.
     */
    public SubmissionEndpointReferenceBuilder portType(QName portType) {
        this.portType = portType;
        return this;
    }
    
    /**
     * Construct an instance of <code>EndpointReference</code> based on the values
     * specified.
     * 
     * @return an instance of <code>SubmissionEndpointReference</code>
     */
    public SubmissionEndpointReference build() {
    	SubmissionEndpointReference submissionEPR = null;
    	
        String addressingNamespace =
        	EndpointReferenceUtils.getAddressingNamespace(SubmissionEndpointReference.class);    	
        org.apache.axis2.addressing.EndpointReference axis2EPR =
        	EndpointReferenceUtils.createAxis2EndpointReference(address, serviceName, endpointName, wsdlDocumentLocation, addressingNamespace);
    	
        try {
            if (referenceParameters != null)
                EndpointReferenceUtils.addReferenceParameters(axis2EPR, referenceParameters.toArray(ZERO_LENGTH_ARRAY));
        	
            if (portType != null)
                EndpointReferenceUtils.addInterface(axis2EPR, portType, addressingNamespace);
        	
            submissionEPR =
                (SubmissionEndpointReference) EndpointReferenceUtils.convertFromAxis2(axis2EPR, addressingNamespace);
        }
        catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(
                Messages.getMessage("endpointRefConstructionFailure2", e.toString()));
        }
        
        return submissionEPR;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        if (address != null) {
            buffer.append("Address: ").append(address);
        }
        
        if (serviceName != null) {
            buffer.append(", Service name: ").append(serviceName);
        }
        
        if (endpointName != null) {
            buffer.append(", Endpoint name: ").append(endpointName);
        }
        
        if (portType != null) {
            buffer.append(", Port type: ").append(portType);
        }
        
        if (referenceParameters != null) {
            buffer.append(", Reference parameters: ").append(referenceParameters);
        }
        
        if (wsdlDocumentLocation != null) {
            buffer.append(", WSDL location: ").append(wsdlDocumentLocation);
        }

        return buffer.toString();
    }
}
