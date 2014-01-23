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

package org.apache.axis2.mex.om;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.mex.MexConstants;
import org.apache.axis2.mex.MexException;
import org.apache.axis2.mex.util.MexUtil;

import javax.xml.namespace.QName;

/**
 * Class implemented for MetadataReference element defined in 
 * the WS-MetadataExchange spec.
 *
 */

public class MetadataReference extends MexOM implements IMexOM {

	private OMFactory factory;
	private OMElement eprElement = null; 
        private EndpointReference epr = null;
 	private String namespaceValue = null;

	 /**
	 * Constructor
	 * @throws MexException 
	 */

	public MetadataReference() throws MexException  {
		
		this.factory = MexUtil.getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		this.namespaceValue = MexConstants.Spec_2004_09.NS_URI;
	}
	
	/**
	 * Constructor
	 * @param defaultFactory
	 * @param namespaceValue
	 * @throws MexOMException
	 */

	public MetadataReference(OMFactory defaultFactory, String namespaceValue)
	 throws MexOMException {
		if (!isNamespaceSupported(namespaceValue))
			throw new MexOMException("Unsupported namespace");

		this.factory = defaultFactory;
		this.namespaceValue = namespaceValue;
		}

	 public OMElement getEPRElement() {
			return eprElement;
		}
	  
	public EndpointReference getEPR() {
			return epr;
	}


	/**
	 * Populates an MetadataReference object based on the endpoint reference type <code>OMElement</code> passed. 
	 * @param inElement MetadataReference element
	 * @return MetadataReference 
	 * @throws MexOMException
	 */
	
	public MetadataReference fromOM(OMElement element) throws MexOMException{
		
		if (element == null) {
			throw new MexOMException("Null element passed.");
		}
		if (!element.getLocalName().equals(MexConstants.SPEC.METADATA_REFERENCE)) {
			throw new MexOMException("Invalid element passed.");
		}
		eprElement = element;
		try {
			epr = EndpointReferenceHelper.fromOM(element);
		} catch (AxisFault e) {
			throw new MexOMException (e);
		}
		
		return this;
	}
	/**
	 * Convert MetadatReference object content to the OMElement representation.
	 * @return OMElement representation of MetadatReference.
	 * @throws MexOMException
	 */
	public OMElement toOM() throws MexOMException {
		if (eprElement == null) {
			throw new MexOMException(
					"Must have EndpointReference element in MetadataReference");
		}

		OMElement metadataRef = null;
		try {
			metadataRef = EndpointReferenceHelper.toOM(factory, epr, new QName(
					namespaceValue, MexConstants.SPEC.METADATA_REFERENCE,
					MexConstants.SPEC.NS_PREFIX), eprElement.getNamespace()
					.getNamespaceURI());
	
		} catch (AxisFault e) {
			throw new MexOMException(e);
		}
		

		return metadataRef;
	}
	
	/**
	 * Set EPR 
	 * 
	 * @param element Endpoint Reference Type element
	 */
	public void setEPR(OMElement element) throws MexOMException {
		eprElement = element;
		try {
			epr = EndpointReferenceHelper.fromOM(eprElement);
		} catch (AxisFault e) {
			throw new MexOMException (e);
		}
		
	}
	
	/**
	 * Set EPR 
	 * 
	 * @param element Endpoint Reference Type elem
	 */
	public void setEPR(EndpointReference endRef) throws MexOMException {
		epr = endRef;
		
	}
	
}
