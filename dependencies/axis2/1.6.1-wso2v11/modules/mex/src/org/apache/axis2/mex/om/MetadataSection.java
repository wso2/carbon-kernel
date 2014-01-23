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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.mex.MexConstants;
import org.apache.axis2.mex.MexException;
import org.apache.axis2.mex.util.MexUtil;

import javax.xml.namespace.QName;


/**
 * Class implemented for MetadataSection element defined in 
 * the WS-MEX spec. A unit of metdata i.e. a MetadataSection may be included in-line data, 
 * or may be by reference as an endpoint reference (MetadataReference) or a URL (Location).
 * An instance of MetadataSection can have one form of data: inline, location, or reference.
 *
 */

public  class MetadataSection extends MexOM implements IMexOM {
	private String namespaceValue = null;
	private OMFactory factory;
	// Choices of content: inline metadata, MetadataReference, Location
	private String anyAttribute = null;
	private OMNode inlineData = null;
	//private String inlineData = null;
	private Location location = null;
	private MetadataReference ref = null;
	
    // Attributes
    private String dialect;
    private String identifier;
    
    
    /**
	 * Constructor
	 * @throws MexException 
	 */

	public MetadataSection() throws MexException  {
		
		this.factory = MexUtil.getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);;
		this.namespaceValue = MexConstants.Spec_2004_09.NS_URI;
	}
	
    /**
     * Constructor
     * @param defaultFactory
     * @param namespaceValue
     * @throws MexOMException
     */
	public MetadataSection(OMFactory defaultFactory, String namespaceValue) throws MexOMException  {
		this.factory = defaultFactory;
		this.namespaceValue = namespaceValue;
	}
	
	
	/**
	 * Populates an MetadataSection object based on the <code>OMElement</code> passed. 
	 * @param inElement mex:MetadataSection element
	 * @return MetadataSection 
	 * @throws MexOMException
	 */
	
	public MetadataSection fromOM(OMElement element) throws MexOMException {
		if (element == null) {
			throw new MexOMException("Null element passed.");
		}
		if (!element.getLocalName().equals(MexConstants.SPEC.METADATA_SECTION)) {
			throw new MexOMException("Invalid element passed.");
		}
		OMAttribute dialectAttr  = element.getAttribute(new QName(MexConstants.SPEC.DIALECT));
		if (dialectAttr == null){
			throw new MexOMException("Missing Dialect Attribute in MetadataSection.");
		}
		setDialect(dialectAttr.getAttributeValue());
		OMAttribute identifierAttr  = element.getAttribute(new QName(MexConstants.SPEC.IDENTIFIER));
		if (identifierAttr != null){
			setIdentifier(identifierAttr.getAttributeValue());		
		}
		// validate one of the following element must exist: Location, MetadataReference, inline data
		OMElement locationElem = element.getFirstChildWithName(new QName(namespaceValue, MexConstants.SPEC.LOCATION));
		Location location = null;
		MetadataReference ref = null;
		
		if (locationElem != null){
			location = new Location(factory, namespaceValue);
		    setLocation(location.fromOM(locationElem));	
		}
		else { // check for MetadataReference
			OMElement refElem = element.getFirstChildWithName(new QName(namespaceValue, MexConstants.SPEC.METADATA_REFERENCE));
			if (refElem != null ){
				ref = new MetadataReference(factory, namespaceValue);
				setMetadataReference(ref.fromOM(refElem));
			}
		}
		if (location == null && ref == null) { // check for inline content
			OMNode inline = element.getFirstOMChild();
		    if (inline != null)
		    	setinlineData(inline);
		    else {
		    	throw new MexOMException("Invalid empty MetadataSection.");
		    }
		    
		}

		return this;
	}
	
	/**
	 * Convert MetadatSection content to the OMElement representation.
	 * 
	 * @return OMElement representation of MetadataSection.
	 * @throws MexOMException
	 */
	public OMElement toOM() throws MexOMException {
		OMNamespace mexNamespace = factory.createOMNamespace(namespaceValue,
				MexConstants.SPEC.NS_PREFIX);
		OMElement metadataSection = factory.createOMElement(
				MexConstants.SPEC.METADATA_SECTION, mexNamespace);

		// dialet is required
		if (dialect == null) {
			throw new MexOMException("Dialet was not set. Dialet must be set.");
		}
		OMAttribute dialetAttrib = factory.createOMAttribute(
				MexConstants.SPEC.DIALECT, null, dialect);

		metadataSection.addAttribute(dialetAttrib);

		if (identifier != null && identifier.trim().length() > 0) {
			OMAttribute identifierAttrib = factory.createOMAttribute(
					MexConstants.SPEC.IDENTIFIER, null, identifier);

			metadataSection.addAttribute(identifierAttrib);
		}
		if (anyAttribute != null) {
			OMAttribute anyAttrib = factory.createOMAttribute("AnyAttribute",
					null, anyAttribute);

			metadataSection.addAttribute(anyAttrib);
		}

		if (inlineData != null) {
			metadataSection.addChild(inlineData);
			
		}

		if (location != null) {
			metadataSection.addChild(location.toOM());
		}

		if (ref != null) {
			metadataSection.addChild(ref.toOM());
		}
		return metadataSection;

	}
	
	public String getDialect() {
		return dialect;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getanyAttribute() {
		return anyAttribute;
	}
	
	/**
	 * Return metadata unit in URL form i.e. mex:Location
	 * 
	 * @return
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * Return metadata unit in inline form such as WSDL definitions, XML schema document, etc.
	 * @return
	 */
	public OMNode getInlineData() {
		return inlineData;
	}
	
	/**
	 * Return metadata unit in endpoint reference form i.e. mex:MetadataReference.
	 * @return
	 */
	public MetadataReference getMetadataReference() {
		return ref;
	}
	
	public void setIdentifier(String in_identifier) {
		identifier =in_identifier;
	}
	
	public void setDialect(String in_dialect) {
		dialect = in_dialect;
	}
	
	
	public void setLocation(Location in_location) {
		location = in_location;
	}
	
	public void setinlineData(Object in_inlineData) {
		inlineData = (OMNode)in_inlineData;
	}
	
	public void setMetadataReference(MetadataReference in_ref) {
		ref = in_ref;
	}
}
