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

package org.apache.axis2.mtompolicy;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.policy.model.MTOM10Assertion;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.axis2.util.PolicyUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * This is a MTOM module utility class. It contains some useful methods used into the MTOM module. 
 */
public class Utils {

	private static final Log log = LogFactory.getLog(Utils.class);
	
    private static final String MTOM_ASSERTION_APPLIED = "MTOM_ASSERTION_APPLIED";

    /**
     * Extracts the MTOM assertion object if it is exists into the policy based on a given {@link AxisDescription}.
     * 
     * @param axisDescription the {@link AxisDescription} object that should be searched.
     * @return {@link MTOMAssertion}  the {@link MTOMAssertion} found. If it is not found "null" is returned.
     */
    public static MTOMAssertion getMTOMAssertion(AxisDescription axisDescription) {

        if (axisDescription == null) {
            if (log.isDebugEnabled()) {
                log.debug("AxisDescription passed as parameter has a \"null\" value.");
            }        	
            return null;
        }

        ArrayList policyList = new ArrayList();
        policyList.addAll(axisDescription.getPolicySubject()
                .getAttachedPolicyComponents());

        Policy policy = PolicyUtil.getMergedPolicy(policyList, axisDescription);

        if (policy == null) {
            return null;
        }

        List<Assertion> list = (List<Assertion>) policy.getAlternatives()
                .next();

        for (Assertion assertion : list) {
            if (assertion instanceof MTOMAssertion) {
                return (MTOMAssertion) assertion;
            }
        }

        return null;

    }

    /**
     * Locates the {@link AxisService} object searching up in the hierarchy recursively </br>
     * the {@link AxisDescription} object passed.
     *   
     * @param axisDescription the {@link AxisDescription} object that should be checked.
     * @return the {@link AxisService} object found or "null".
     */
    public static AxisService locateAxisService(AxisDescription axisDescription) {

        if (axisDescription == null || axisDescription instanceof AxisService) {
            return (AxisService) axisDescription;
        } else {
            return locateAxisService(axisDescription.getParent());
        }
    }

    /**
     * Based on the parameter passed a {@link MTOM10Assertion} object is created by default. </br>
     * Then it is added to a {@link Policy} object and returned. 
     * 
     * @param param the {@link Parameter} object that contains the value of the "enableMTOM" parameter: </br>
     * "true" or "optional".
     * @return The {@link Policy} object for the MTOM assertion. 
     */
    public static Policy getMTOMPolicy(Parameter param) {

        if (param == null) {
            return null;
        }

        // TODO We need to give the user the ability to specify if MTOM 1.0 or MTOM 1.1 should be used.
        MTOMAssertion mtom10;

        if (Constants.VALUE_TRUE.equals(param.getValue())) {
            mtom10 = new MTOM10Assertion();
        } else if (Constants.VALUE_OPTIONAL.equals(param.getValue())) {
            mtom10 = new MTOM10Assertion();
            mtom10.setOptional(true);
        } else {
            return null;
        }

        Policy policy = new Policy();
        policy.addAssertion(mtom10);

        return policy;

    }

    /**
     * Applies the MTOM policy to the binding level of the web service.
     * 
     * @param axisService the {@link AxisService} to whom binding level the MTOM policy should be attached.
     * @param policy the {@link Policy} object that contains the MTOM assertion.
     * @throws AxisFault thrown if the parameter is locked on a parent level - thus it could not be added.
     */
    public static void applyPolicyToSOAPBindings(AxisService axisService,
            Policy policy) throws AxisFault {
        
        Parameter param = axisService.getParameter(MTOM_ASSERTION_APPLIED);

        if ( policy == null || (param != null && Constants.VALUE_TRUE.equals(param.getValue()))) {
            return;
        }

        for (Object obj : axisService.getEndpoints().values()) {

            AxisEndpoint endpoint = (AxisEndpoint) obj;
            AxisBinding binding = endpoint.getBinding(); 
            if (Java2WSDLConstants.TRANSPORT_URI.equals(binding.getType())
                    || WSDL2Constants.URI_WSDL2_SOAP.equals(binding.getType())) {
                binding.getPolicySubject().attachPolicy(policy);
            }
        }

        axisService
                .addParameter("MTOM_ASSERTION_APPLIED", Constants.VALUE_TRUE);

    }

}
