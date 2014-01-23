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
import org.apache.axis2.mex.MexConstants;
import org.apache.axis2.mex.MexException;
import org.apache.axis2.mex.util.MexUtil;

/**
 * Class implemented for Location element defined in 
 * the WS-MEX spec.
 *
 */

public class Location extends AnyURIType {

	
	/**
	 * @param uri  valid URL to retrieve the metadata using HTTP Get
	 * @throws MexException 
         *
	 */
	public Location(String uri) throws MexException {
		super(MexUtil.getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI), MexConstants.Spec_2004_09.NS_URI, uri);
	}
	
	public Location(OMFactory defaultFactory, String namespaceValue, String uri) throws MexOMException {
		super(defaultFactory, namespaceValue, uri);
	}

	public Location(OMFactory defaultFactory, String uri) throws MexOMException {
		super(defaultFactory, MexConstants.Spec_2004_09.NS_URI, uri );
	}
	
	public Location fromOM(OMElement element) throws MexOMException{
		if (element == null) {
			throw new MexOMException("Null element passed.");
		}
		if (!element.getLocalName().equals(MexConstants.SPEC.LOCATION)) {
			throw new MexOMException("Invalid element passed.");
		}
		super.setURI(element.getText());
			
		return this;
	}
	
	/*
	 * Return name of this element
	 * (non-Javadoc)
	 * @see org.apache.axis2.Mex.OM.AnyURIType#getElementName()
	 */
	protected String getElementName(){
		return MexConstants.SPEC.LOCATION;
	}
}
