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

package org.apache.axis2.jaxws.server.config;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.SubmissionAddressing;
import org.apache.axis2.jaxws.addressing.SubmissionAddressingFeature;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.feature.ServerConfigurator;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.registry.ServerConfiguratorRegistry;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.AddressingFeature.Responses;

/**
 * This class will enable/disable WS-Addressing for a JAX-WS 2.1 web service
 * endpoint, based on the configuration passed to it via an <code>Addressing</code>
 * annotation and/or a <code>SubmissionAddressing</code> annotation from the
 * endpoint implementation bean.
 * 
 * @see javax.xml.ws.soap.Addressing
 * @see org.apache.axis2.jaxws.addressing.SubmissionAddressing
 */
public class AddressingConfigurator implements ServerConfigurator {

    private static Log log = LogFactory.getLog(AddressingConfigurator.class);
    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.ServerConfigurator#configure(org.apache.axis2.jaxws.description.EndpointDescription)
     */
    public void configure(EndpointDescription endpointDescription) {
        if (log.isDebugEnabled()) {
            log.debug("Start configure");
        }
    	Addressing addressing =
    		(Addressing) ((EndpointDescriptionJava) endpointDescription).getAnnoFeature(AddressingFeature.ID);
    	SubmissionAddressing submissionAddressing =
    		(SubmissionAddressing) ((EndpointDescriptionJava) endpointDescription).getAnnoFeature(SubmissionAddressingFeature.ID);
    	Parameter namespace = new Parameter(AddressingConstants.WS_ADDRESSING_VERSION, null);
    	Parameter disabled = new Parameter(AddressingConstants.DISABLE_ADDRESSING_FOR_IN_MESSAGES, "false");
    	Parameter required = new Parameter(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER, AddressingConstants.ADDRESSING_UNSPECIFIED);
    	Parameter responses = null;
    	
    	if (addressing != null && submissionAddressing != null) {
    	    if (log.isDebugEnabled()) {
                log.debug("Both Addressing and SubmissionAddressing are specified");
            }
            //Both annotations must have been specified.
            boolean w3cAddressingEnabled = addressing.enabled();
            boolean submissionAddressingEnabled = submissionAddressing.enabled();
            
            if (w3cAddressingEnabled && submissionAddressingEnabled) {
            	namespace.setValue(null);

                boolean w3cAddressingRequired = addressing.required();
                boolean submissionAddressingRequired = submissionAddressing.required();

                if (w3cAddressingRequired || submissionAddressingRequired)
                	required.setValue(AddressingConstants.ADDRESSING_REQUIRED);
            }
            else if (w3cAddressingEnabled) {
            	namespace.setValue(AddressingConstants.Final.WSA_NAMESPACE);
            	
            	if (addressing.required())
            		required.setValue(AddressingConstants.ADDRESSING_REQUIRED);
            }
            else if (submissionAddressingEnabled) {
            	namespace.setValue(AddressingConstants.Submission.WSA_NAMESPACE);
            	
            	if (submissionAddressing.required())
            		required.setValue(AddressingConstants.ADDRESSING_REQUIRED);
            }
            else {
            	disabled.setValue("true");
            }
    	}
    	else if (addressing != null) {
    	    if (log.isDebugEnabled()) {
                log.debug("Only Addressing is specified.  SubmissionAddressing is not specified");
            }
            //The Addressing annotation must have been specified.
            boolean w3cAddressingEnabled = addressing.enabled();

            if (w3cAddressingEnabled) {
            	namespace.setValue(AddressingConstants.Final.WSA_NAMESPACE);
            	
            	if (addressing.required())
            		required.setValue(AddressingConstants.ADDRESSING_REQUIRED);
            }
            else {
            	namespace.setValue(AddressingConstants.Submission.WSA_NAMESPACE);
            }
    	}
    	else if (submissionAddressing != null) {
    	    if (log.isDebugEnabled()) {
                log.debug("Only SubmssionAddressing is specified.  Addressing is not specified");
            }
            //The SubmissionAddressing annotation must have been specified.
            boolean submissionAddressingEnabled = submissionAddressing.enabled();

            if (submissionAddressingEnabled) {
            	namespace.setValue(AddressingConstants.Submission.WSA_NAMESPACE);
            	
            	if (submissionAddressing.required())
            		required.setValue(AddressingConstants.ADDRESSING_REQUIRED);
            }
            else {
            	namespace.setValue(AddressingConstants.Final.WSA_NAMESPACE);
            }                		
    	}
    	else {
            //If neither were specified then this configurator should never run.
            throw ExceptionFactory.makeWebServiceException(
                 Messages.getMessage("NoWSAddressingFeatures"));
    	}
    	
    	// If the Addressing annotation was used, then get the responses value from it and map it to the 
    	// value the addressing handler expects
    	if (addressing != null) {
    	    responses = new Parameter(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME, 
    	            mapResponseAttributeToAddressing(addressing.responses()));
    	}
    	
    	try {
            AxisService service = endpointDescription.getAxisService();
    		service.addParameter(namespace);
    		service.addParameter(disabled);
    		service.addParameter(required);
    		if (responses != null) {
                service.addParameter(responses);
    		}
            
            String value = Utils.getParameterValue(disabled);
    		if (JavaUtils.isFalseExplicitly(value)) {
    			ServiceDescription sd = endpointDescription.getServiceDescription();
    			AxisConfiguration axisConfig = sd.getAxisConfigContext().getAxisConfiguration();
    			if (!axisConfig.isEngaged(Constants.MODULE_ADDRESSING))
    				axisConfig.engageModule(Constants.MODULE_ADDRESSING);
    		}
    	}
    	catch (Exception e) {
    	    if (log.isDebugEnabled()) {
                log.debug("Unexpected Exception occurred:", e);
            }
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("AddressingEngagementError", e.toString()));
    	}
    	if (log.isDebugEnabled()) {
    	    log.debug("End configure");
        }
    }

    /**
     * Given a value for the Addressing.responses annotation attribute, map it to the corresponding
     * Addressing constant to be set on the AxisSservice
     * 
     * @param responses Enum value from the Addressing.responses annotation attribute
     * @return String from AddressingContstants corresponding to the responses value.
     */
    static public String mapResponseAttributeToAddressing(Responses responses) {
        String addressingType = null;
        switch (responses) {
            case ALL:
                addressingType = AddressingConstants.WSAM_INVOCATION_PATTERN_BOTH;
                break;
            case ANONYMOUS:
                addressingType = AddressingConstants.WSAM_INVOCATION_PATTERN_SYNCHRONOUS;
                break;
            case NON_ANONYMOUS:
                addressingType = AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS;
                break;
        }
        return addressingType;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.ServerConfigurator#supports(java.lang.String)
     */
    public boolean supports(String bindingId) {
        return ServerConfiguratorRegistry.isSOAPBinding(bindingId);
    }
}
