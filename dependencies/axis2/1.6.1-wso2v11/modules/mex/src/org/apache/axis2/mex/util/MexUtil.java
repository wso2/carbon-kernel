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

package org.apache.axis2.mex.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.dataretrieval.OutputForm;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.mex.MexConstants;
import org.apache.axis2.mex.MexException;
import org.apache.axis2.mex.om.Metadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class MexUtil {
	private static final Log log = LogFactory.getLog(MexUtil.class);
	/**
	 * Answer SOAPVersion for specified envelope
	 * @param envelope SOAP Envelope
	 * @return version of SOAP
	 * @throws MexException
	 */
	public static int getSOAPVersion(SOAPEnvelope envelope) throws MexException {
		String namespaceName = envelope.getNamespace().getNamespaceURI();
		if (namespaceName.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return MexConstants.SOAPVersion.v1_1;
		else if (namespaceName.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return MexConstants.SOAPVersion.v1_2;
		else
			throw new MexException("Unknown SOAP version");
	}

	/**
	 * Answer SOAPFactory corresponding to specified SOAP namespace URI
	 * @param soapNameSpaceURI soap namespace uri
	 * @return
	 * @throws MexException
	 */
	public static SOAPFactory getSOAPFactory(String soapNameSpaceURI) throws MexException {
			if (soapNameSpaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return  OMAbstractFactory.getSOAP11Factory();
		else if (soapNameSpaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return OMAbstractFactory.getSOAP12Factory();
		else
			throw new MexException("Unknown SOAP soapNameSpaceURI");
	}

	/**
	 * Answers SOAPFactory corresponding to specified SOAP Version
	 * @param SOAPVersion SOAP version
	 * @return SOAPFactory
	 */
	public static SOAPFactory getSOAPFactory(int SOAPVersion) {

		if (SOAPVersion == MexConstants.SOAPVersion.v1_1)
			return OMAbstractFactory.getSOAP11Factory();
		else
			return OMAbstractFactory.getSOAP12Factory();

	}
	
	
	// Return all supported output forms
	public static OutputForm[] allSupportedOutputForms(){
		OutputForm[]outputforms = new OutputForm[] {
		OutputForm.INLINE_FORM,
		OutputForm.LOCATION_FORM, 
		OutputForm.REFERENCE_FORM};
		return outputforms;
	}
	
	 public static Metadata fromEPR(EndpointReference epr) throws MexException {
		ArrayList eprMetdata = epr.getMetaData();
		OMElement mexElement = null;
		Metadata metadata = null;
		if (eprMetdata != null) {
			mexElement = (OMElement) eprMetdata.get(0);
		} else {
			ArrayList refParm = epr.getExtensibleElements();
			if (refParm != null) {
				for (int i = 0; i < refParm.size(); i++) {
					OMElement elem = (OMElement) refParm.get(i);
					if (elem.getLocalName().equals(MexConstants.SPEC.METADATA)) {
						mexElement = elem;
						break;
					}
				}
			}
		}
		if (mexElement != null)
			metadata = fromOM(mexElement, MexConstants.Spec_2004_09.NS_URI);
		return metadata;
	}	
	 
	/**
	 * Answers WS-Addressing namespace
	 * 
	 * @param toAddress
	 *            To Address element
	 * @return OMNamespaceImpl WS-Addressing namespace
	 * @throws AxisFault
	 */
	
	 public static OMNamespaceImpl getAddressingNameSpace(OMElement toAddress)
			throws MexException {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespaceImpl wsa = null;
		try {
			String prefix = toAddress.getNamespace().getPrefix();
			String nsURI = toAddress.getNamespace().getNamespaceURI();
			wsa = (OMNamespaceImpl) factory.createOMNamespace(nsURI, prefix);
		} catch (Exception e) {
		    throw new MexException(e);
		}
		return wsa;

	}

       /**
	 * API to map mex:Metadata element to a Metadata object representation. Data validation will 
	 * be performed on omElement content. See {@link Metadata} for APIs to access metadata section.
	 * 
	 * @param omElement an element such as endpoint reference type that contains mex:Metadata or
	 *                  an mex:Metadata element 
	 * @return Metadata object representation of mex:Metadata element passed.
	 * @throws MexException if invalid mex:Metadata element content is detected 
	 */ 

	 
    public static Metadata fromOM(OMElement omElement) throws MexException{
	      Metadata metadata = fromOM(omElement, MexConstants.Spec_2004_09.NS_URI);
	      return metadata;
	}	
	
        /**
	 * API to map mex:Metadata element to a Metadata object representation. Data validation will 
	 * be performed on omElement content. See {@link Metadata} for APIs to access metadata section.
	 * 
	 * @param omElement an element such as endpoint reference type that contains mex:Metadata or
	 *                  an mex:Metadata element 
	 * @param mexNamespaceValue  the namespace of the WS-MEX spec to comply with.
	 * @return Metadata object representation of mex:Metadata element passed.
	 * @throws MexException if invalid mex:Metadata element content is detected 
	 */ 	
	public static Metadata fromOM(OMElement omElement, String mexNamespaceValue) throws MexException{
		
		SOAPFactory factory = MexUtil.getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		Metadata metadata = new Metadata(factory, mexNamespaceValue);
		metadata = metadata.fromOM(omElement);
		return metadata;
	}
	

       
    /**
	 * Check if metadata exchange has been disabled for a service. 
	 * @param serviceConfigMEXParm metadataexchange configured in services.xml
	 * @return
	 */
	public static boolean isMexDisabled(Parameter serviceConfigMEXParm) {
		boolean disabled = false;
		if (serviceConfigMEXParm != null) {
			OMElement mexConfig = serviceConfigMEXParm.getParameterElement();
			String disable = mexConfig.getAttributeValue(new QName(
					MexConstants.MEX_CONFIG.ENABLE_MEX));
			if (disable != null && disable.equals("false"))
				disabled = true;
		}
		return disabled;
	}
	
	/**
	 * Determine output forms for specified dialect based on "metadataexchange" configured in 
	 * axis2.xml and services.xml.
	 * The order of precedence in determining  output form configured:  
	 *   a. dialect specific at service level.
	 *   b. service level  i.e. without dialect attribute specified
	 *   c. dialect specific at global level i,e, configured in axis2.xml
	 *   d. service level  i.e. without dialect attribute specified
	 *   e. default output forms to all: inline, location, reference
	 *   
	 * @param dialect
	 * @param axisConfigMEXParm "metadataexchange" parameter configured in axis2.xml
	 * @param serviceConfigMEXParm  "metadataexchange" parameter configured in services.xml
	 * @return
	 */
	public static OutputForm[] determineOutputForm(String dialect, Parameter axisConfigMEXParm, Parameter serviceConfigMEXParm){
		
		if (axisConfigMEXParm == null && serviceConfigMEXParm == null){
			return allSupportedOutputForms();
		}
	    OutputForm[] outputform = new OutputForm[0];
	    outputform = determineOutputForm(dialect, serviceConfigMEXParm);
	    
	    if (outputform.length == 0) { // output form not configured in service level config
	    	outputform = determineOutputForm(dialect, axisConfigMEXParm);
	    }
		
	    if (outputform.length == 0){
	    	log.debug("No outputform configured, use default output forms");
	    	outputform = allSupportedOutputForms();
	    }
		return outputform;
	}
	
	
	private static OutputForm[] determineOutputForm(String dialect, Parameter mexParm) {
		OutputForm[] forms = new OutputForm[0];
		if (mexParm == null)
			return forms;
		
		OMElement mexConfig = mexParm.getParameterElement();
		Iterator ite = mexConfig.getChildrenWithName(new QName(
				MexConstants.MEX_CONFIG.OUTPUT_FORM_PARM));
		String dialectForm_configured = null;
		String serviceForm_configured = null;
		while (ite.hasNext()) {
			OMElement elem = (OMElement) ite.next();
			String form_value = elem.getAttributeValue(new QName(
					MexConstants.MEX_CONFIG.FORMS_PARM));
			String dialect_value = elem.getAttributeValue(new QName(
					MexConstants.MEX_CONFIG.DIALECT_PARM));
			if (dialect_value == null){
				serviceForm_configured = form_value;
			}	
			else if (dialect_value != null
					&& dialect_value.equals(dialect)) {
				dialectForm_configured = form_value;
			}	
	     }
			
		if (dialectForm_configured != null){
			forms = parseForms(dialectForm_configured);
		}
		else if (serviceForm_configured != null){
			forms = parseForms(serviceForm_configured);
		}
		
		return forms;
	}	
	
	
	private static OutputForm[] parseForms(String form_values) {
		List forms = new ArrayList();
		StringTokenizer st = new StringTokenizer(form_values,
				MexConstants.MEX_CONFIG.DELIMITER);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.equals(MexConstants.MEX_CONFIG.INLINE))
				forms.add(OutputForm.INLINE_FORM);
			else if (token.equals(MexConstants.MEX_CONFIG.LOCATION))
				forms.add(OutputForm.LOCATION_FORM);
			else if (token.equals(MexConstants.MEX_CONFIG.REFERENCE))
				forms.add(OutputForm.REFERENCE_FORM);
			else {
              log.debug("Invalid form configured, " + form_values);
			}

		}

		return (OutputForm[]) forms.toArray(new OutputForm[0]);
	}

}
