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

package org.apache.axis2.mex;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.dataretrieval.Data;
import org.apache.axis2.dataretrieval.DataRetrievalException;
import org.apache.axis2.dataretrieval.DataRetrievalRequest;
import org.apache.axis2.dataretrieval.OutputForm;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.mex.om.Location;
import org.apache.axis2.mex.om.Metadata;
import org.apache.axis2.mex.om.MetadataReference;
import org.apache.axis2.mex.om.MetadataSection;
import org.apache.axis2.mex.om.MexOMException;
import org.apache.axis2.mex.util.MexUtil;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Message Receiver for processing WS-MEX GetMetadata request. 
 *
 */
public class MexMessageReceiver extends AbstractInOutMessageReceiver {
	private static final Log log = LogFactory.getLog(MexMessageReceiver.class);
	Parameter axisConfigMEXParm = null;
    Parameter serviceConfigMEXParm = null;
	String mexNamespaceValue = null;

	/**
	 * Process GetMetadata request
        */
	public void invokeBusinessLogic(MessageContext msgContext,
			MessageContext newmsgContext) throws AxisFault {
		AxisService theService = msgContext.getAxisService();       
		axisConfigMEXParm = msgContext.getConfigurationContext().getAxisConfiguration().getParameter(MexConstants.MEX_CONFIG.MEX_PARM);
		serviceConfigMEXParm = theService.getParameter(MexConstants.MEX_CONFIG.MEX_PARM);
		
		check_MEX_disabled(serviceConfigMEXParm);
		
		try {
			Metadata metadata = handleRequest(msgContext);
		        theService.setEndpointURL(msgContext.getTo().getAddress());

			if (metadata != null) {
				SOAPEnvelope envelope = newmsgContext.getEnvelope();
				if (envelope == null) {
					SOAPFactory fac = getSOAPFactory(msgContext);
					envelope = fac.getDefaultEnvelope();
				}
           
				OMElement result = metadata.toOM();
				if (result != null) {
					AxisService service = msgContext.getAxisService();
					result.declareNamespace(service.getTargetNamespace(),
							service.getTargetNamespacePrefix());
					envelope.getBody().addChild(result);
				}

				newmsgContext.setEnvelope(envelope);
			}
			// AxisService service = msgContext.getAxisService();

		} catch (Exception e) {
			log.info(e);
			if (e instanceof MexException) {
				throw (MexException) e;
			}
			throw new MexException(e);
		}

	}


	/*
	 * Handle GetMetadata Request 
	 * Interprete GetMetadata request and process request.
	 * @returns Metadata object
	 */
	private Metadata handleRequest(MessageContext msgContext) throws AxisFault {
		Metadata metadata = null;
		SOAPEnvelope envelope = msgContext.getEnvelope();

		SOAPBody body = envelope.getBody();
		OMElement aReq = body.getFirstChildWithName(new QName(
                MexConstants.Spec_2004_09.NS_URI,
				MexConstants.SPEC.GET_METADATA));
        
		List metadata_request_list;
		if (aReq != null) {
            mexNamespaceValue = MexConstants.Spec_2004_09.NS_URI;
			metadata_request_list = determineMetadataTypes(aReq);

		} else {
			throw new MexException("Invalid Metadata request");
		}

		metadata = processRequest(metadata_request_list, msgContext, aReq);

		return metadata;
	}

	/*
	 * Process the requests
	 * 
	 * @param metadata_request_list list of mex Dialect for requesting data
	 * @msgContext MessageContext
	 * @aReq GetMetadata request 
     */
	
	public Metadata processRequest(List metadata_request_list,
			MessageContext msgContext, OMElement aReq) throws MexException {
		
	    //  Instantiate Metadata instance to build the WS-Mex Metadata element
        SOAPEnvelope envelope = msgContext.getEnvelope();
        String soapNamespaceURI = envelope.getNamespace().getNamespaceURI();
        SOAPFactory factory = MexUtil.getSOAPFactory(soapNamespaceURI);
        
        Metadata metadata = new Metadata(factory, mexNamespaceValue);
		DataRetrievalRequest requestOptions = new DataRetrievalRequest();
        
        String identifier_value = null;
		// Check if Identifier element included in request
        OMElement dialectElem = aReq.getFirstChildWithName(new QName(
                mexNamespaceValue, MexConstants.SPEC.DIALECT));
        
        if (dialectElem != null)  {
    		OMElement identifier = dialectElem.getFirstChildWithName(new QName(
                    mexNamespaceValue, MexConstants.SPEC.IDENTIFIER));
            
    		if (identifier != null) {
    			identifier_value = identifier.getText();
    			if (identifier_value != null && identifier_value.length() > 0) {
    				requestOptions.putIdentifier(identifier_value);
    			}
    		}
        }
        
		// Process the request and append MetadataSection to Metadata
		// Loop through the metadata_request_list for Dialect(s)), and setup requestOptions.
		// Basically, one requestOptions is setup for each supported outputForm for the Dialect
		// and Identifier specified in the GetMetadata request.
		int len = metadata_request_list.size();
		OutputForm[] outputforms;
		
		for (int i = 0; i < len; i++) { // metadata request

            String dialect = "";
			try {
				dialect = (String) metadata_request_list.get(i);

				requestOptions.putDialect(dialect);

				outputforms = MexUtil.determineOutputForm(dialect, axisConfigMEXParm, serviceConfigMEXParm);
				// Loop to call AxisService::getData API to retrieve data
				// for the Dialect and Identifier(if specified) in the request
				// for each
				// supported output form.
				
				for (int j = 0; j < outputforms.length; j++) { // output form
					requestOptions.putOutputForm(outputforms[j]);

					Data[] result =  msgContext.getAxisService().getData(requestOptions,
							msgContext);

					ArrayList sections = processData(result, outputforms[j], dialect,
							   identifier_value, factory);
					metadata.addMetadatSections(sections);
				}

			} catch (DataRetrievalException e) {
				log.error("Data Retrieval exception detected for dialect, " + dialect, e);
				
				throw new MexException(e);
			} catch (Throwable e) {
				
				log.error("Throwable detected for dialect, " + dialect , e);
                e.printStackTrace();

				throw new MexException(e);
			}

		}
		return metadata;
	}

	/*
	 * Create MetadataSection for each Data element, and add the
	 * MetadataSections to Metadata.
	 */
	private ArrayList processData(Data[] data, OutputForm outputForm,
			String dialect, String identifier_value, SOAPFactory factory) throws MexException {
		MetadataSection section=null;
		ArrayList sections = new ArrayList();
		if (data == null || data.length == 0) {
			if (log.isDebugEnabled())
				log
						.debug("No result was returned from getData request for dialect,"
								+ dialect
								+ " Form: "
								+ outputForm.getType()
								+ ". No MetadataSection will be added!");

		} else {
			for (int k = 0; k < data.length; k++) {

				section = createMetadataSection(outputForm, data[k].getData(),
						factory, mexNamespaceValue);

				section.setDialect(dialect);
				identifier_value = data[k].getIdentifier();

				if (identifier_value != null) {
					section.setIdentifier(identifier_value);
				}
				sections.add(section);

			}
		}
		return sections;
	}

	private MetadataSection createMetadataSection(OutputForm outputForm,
			Object result, SOAPFactory factory, String mexNamespaceValue)
			throws MexOMException {
		MetadataSection section = new MetadataSection(factory,
				mexNamespaceValue);

		if (outputForm == OutputForm.INLINE_FORM)
			section.setinlineData(result);
		else if (outputForm == OutputForm.LOCATION_FORM)
			section.setLocation(new Location(factory, mexNamespaceValue,
					(String) result));
		else if (outputForm == OutputForm.REFERENCE_FORM) {
			MetadataReference ref = new MetadataReference(factory,
					mexNamespaceValue);
			
			ref.setEPR((OMElement) result);
			section.setMetadataReference(ref);
		} else {

			section.setinlineData((OMElement) result);
		}

		return section;
	}

	/*
	 * Traverse and interprete the GetMetadata OMElement for Dialect element
	 * that specified in the request. @returns a List with Dialect(s) of
	 * metadata requested.
	 * 
	 */
	private List determineMetadataTypes(OMElement aReq) {
		List metadata_request_list = new ArrayList();

		boolean allTypes = false;

		OMElement dialect = aReq.getFirstChildWithName(new QName(mexNamespaceValue,
				MexConstants.SPEC.DIALECT));
		if (dialect != null) {
			String dialectText = dialect.getText();
			if (dialectText != null && dialectText.length() > 0) {
				metadata_request_list.add(dialectText.trim());
			} else {
				allTypes = true;
            }
		} else {
			allTypes = true;
        }

		if (allTypes) { // retrieve all metadata
			metadata_request_list.add(MexConstants.SPEC.DIALECT_TYPE_POLICY);
			metadata_request_list.add(MexConstants.SPEC.DIALECT_TYPE_SCHEMA);
			metadata_request_list.add(MexConstants.SPEC.DIALECT_TYPE_WSDL);
		}
		return metadata_request_list;
	}


    private void check_MEX_disabled (Parameter mexConfig) throws MexDisabledException{
        if (MexUtil.isMexDisabled(mexConfig)){
            throw new MexDisabledException("'metadataexchange' parameter configured to disable MEX for the service.");
        }
    }
}