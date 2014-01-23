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
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.mex.MexConstants;

/**
 * Base Class implemented for element(s) that of type='xs:anyURI' defined in 
 * the WS-MEX spec.
 *
 */

public abstract class AnyURIType extends MexOM implements IMexOM {
    private String uri = null;
	private OMFactory defaultFactory;
	
	private String namespaceValue = null;
	
	public AnyURIType(OMFactory defaultFactory, String namespaceValue, String uri) throws MexOMException {
		if (!isNamespaceSupported(namespaceValue))
			throw new MexOMException ("Unsupported namespace");
		
		this.defaultFactory = defaultFactory;
		this.namespaceValue = namespaceValue;
		this.uri = uri;
	}
	

	/**
	 * Convert object content to the OMElement representation.
	 * @return OMElement representation of sub-class of AnyURIType.
	 * @throws MexOMException
	 */
	
	public OMElement toOM() throws MexOMException {
		if (uri == null || uri == "") {
			throw new MexOMException("Expected URI type is not set .. ");
		}
			
		OMNamespace mexNamespace = defaultFactory.createOMNamespace(namespaceValue,MexConstants.SPEC.NS_PREFIX);
		OMElement  element = defaultFactory.createOMElement(getElementName(), mexNamespace);	
		element.setText(uri);
		return element;
	}
	
    protected void setURI(String uri){
	     this.uri = uri;	
	}
    
    public String getURI(){
	   return uri;	
	}
	/*
	 * Child class must implement to answer the element name.
	 */
  protected abstract String getElementName();
}
