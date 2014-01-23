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
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.dataretrieval.OutputForm;
import org.apache.axis2.mex.MexConstants;
import org.apache.axis2.mex.MexException;
import org.apache.axis2.mex.util.MexUtil;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * Class implementing  mex:Metadata element 
 *
 */

public class Metadata extends MexOM implements IMexOM {
	private String namespaceValue = null;
	private OMFactory factory;
	private List  metadataSections = new ArrayList(); 
	private OMAttribute attribute = null;
	
        /**
	 * Constructor
	 * @throws MexException 
	 */

	public Metadata() throws MexException  {
		
		this.factory = MexUtil.getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);;
		this.namespaceValue = MexConstants.Spec_2004_09.NS_URI;
	}

	/**
	 * 
	 * @param defaultFactory
	 * @param namespaceValue
	 * @throws MexOMException
	 */

	public Metadata(OMFactory defaultFactory, String namespaceValue) throws MexOMException  {
		this.factory = defaultFactory;
		this.namespaceValue = namespaceValue;
	}

        /**
         * 
         * @return Array of MetadataSection of metadata units
         */
        public MetadataSection[] getMetadatSections() {
		return (MetadataSection[])metadataSections.toArray(new MetadataSection[0]);
	}

         /**
     * 
     * @param dialect
     * @param identifier
     * @return Array of MetadataSection for the specified dialect metadata type and identifier  
     */
    public MetadataSection[] getMetadataSection(String dialect, String identifier){
    	MetadataSection[] sections = getMetadataSection(dialect, identifier, null);
    	return sections;
    }
    
    /**
     * 
     * Answers the Metadata Sections that met the criteria specified in the dialect, identifier, and form.
     * Note: Null value parameter will be treated as wild card.
     * @param dialect
     * @param identifier
     * @param form specify the form of metadata: inline or by reference
     *        See <code>OutputForm</code> for valid output forms.
     * @return Array of MetadataSection for the specified dialect metadata type and identifier of
     *         the form specified.
     *
     */
    public MetadataSection[] getMetadataSection(String dialect, String identifier, OutputForm form ){
        
    	Iterator sections = metadataSections.iterator();
    	List foundSections = new ArrayList();
    	while (sections.hasNext()){
    		MetadataSection aSection = (MetadataSection) sections.next();
    		if ((dialect == null || dialect.equals(aSection.getDialect())) &&
    			(identifier == null || dialect.equals(aSection.getIdentifier())) &&
    			matchOutputForm(aSection, form)){
    			foundSections.add(aSection);
    		}	
    		
    	}
    	return (MetadataSection[])foundSections.toArray(new MetadataSection[0]);
    }
    
    /**
	 * Populates an Metadata object based on the <code>OMElement</code> passed. 
	 * @param inElement mex:Metadata element or element contains mex:Metadata element
	 * @return Metadata 
	 * @throws MexOMException
	 */
	public Metadata fromOM(OMElement inElement) throws MexOMException {
			
		OMElement mexElement = null;
        if (inElement == null ){
        	throw new MexOMException("Null element passed.");
        }
        
        if (inElement.getLocalName().equals(MexConstants.SPEC.METADATA)){
        	mexElement = inElement;
        }
        if (inElement.getLocalName().equals("EndpointReference")){
            try {
	    	  EndpointReference epr = EndpointReferenceHelper.fromOM(inElement);
				
		  ArrayList metadata = epr.getMetaData();
		  if (metadata != null)
		      mexElement = (OMElement)metadata.get(0);
		  else {
		      ArrayList refParm = epr.getExtensibleElements();
		      for (int i=0; i<refParm.size(); i++){
		          OMElement elem = (OMElement)refParm.get(i);
			  if (elem.getLocalName().equals(MexConstants.SPEC.METADATA)){
			      mexElement = elem;
			      break;
			   }
			}
			}
		} catch (AxisFault e) {
		   throw new MexOMException(e);
		}
        	
	        if (mexElement == null) {
		    throw new MexOMException("Missing expected Metadata element in element passed.");
		}
	   }
          else mexElement = inElement;
        
	  OMFactory aFactory = mexElement.getOMFactory();
	  if (aFactory == null) {
	     aFactory = factory;
	   }
	   Iterator mexSections = mexElement.getChildrenWithName(new QName(namespaceValue, MexConstants.SPEC.METADATA_SECTION));
        
           if (mexSections == null){
         	throw new MexOMException("Metadata element does not contain MetadataSection element.");
         }
        while (mexSections.hasNext()){
        	OMElement aSection = (OMElement) mexSections.next();
            MetadataSection metadataSection = new MetadataSection(aFactory, namespaceValue);
            addMetadatSection(metadataSection.fromOM(aSection)); 
        }
        
		return this;
	}
	
	
       /**
        * 
        * @return Array of MetadataSection of metadata units
        */
        public OMElement toOM() throws MexOMException
	{
		OMNamespace mexNamespace = factory.createOMNamespace(namespaceValue,MexConstants.SPEC.NS_PREFIX);
		OMElement metadata = factory.createOMElement(MexConstants.SPEC.METADATA, mexNamespace);

		Iterator sections = metadataSections.iterator();
		while (sections.hasNext()) {
			MetadataSection aSection = (MetadataSection) sections.next();
			metadata.addChild(aSection.toOM());
		}
		if (attribute != null){
			metadata.addAttribute(attribute); //???
		}
		return metadata;
	}
	
	public void setMetadatSections(List in_metadataSections) {
		metadataSections = in_metadataSections;
	}
	
	public void addMetadatSections(List in_metadataSections) {
		Iterator sections = in_metadataSections.iterator();
		while (sections.hasNext()) {
			addMetadatSection((MetadataSection) sections.next());
		}
	}

	public void addMetadatSection(MetadataSection section) {
		metadataSections.add(section);
	}
	
       	
	public void setAttribute(OMAttribute in_attribute) {
		attribute = in_attribute;
	}

  
    // check if section contains data matching the output form requested
    private boolean matchOutputForm(MetadataSection section,
			OutputForm outputForm) {
		boolean match = (outputForm == null);  // no matching needed in null outputForm is passed

		if (!match) {
			if (outputForm == OutputForm.LOCATION_FORM) {
				match = (section.getLocation() != null);
			} else if (outputForm == OutputForm.REFERENCE_FORM) {
				match = (section.getMetadataReference() != null);
			} else if (outputForm == OutputForm.INLINE_FORM) {
				match = (section.getInlineData() != null);
			}
		}

		return match;
	}
	

}
