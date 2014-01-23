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

package org.apache.axis2.jaxws.description.validator;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionJava;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;

import java.util.Collection;

/**
 * 
 */
public class ServiceDescriptionValidator extends Validator {

    private ServiceDescription serviceDesc;
    private ServiceDescriptionJava serviceDescJava;
    private ServiceDescriptionWSDL serviceDescWSDL;

    public ServiceDescriptionValidator(ServiceDescription toValidate) {
        serviceDesc = toValidate;
        serviceDescJava = (ServiceDescriptionJava)serviceDesc;
        serviceDescWSDL = (ServiceDescriptionWSDL)serviceDesc;
    }

    /**
     * Validate the ServiceDescription as follows 1) Validate that annotations and whatever WSDL is
     * specified is valid 2) Validate that Java implementations are correc a) Service
     * Implementations match SEIs if specified b) Operations match SEI methods
     *
     * @return true if the ServiceDescription is valid
     */
    public boolean validate() {
        return validate(false);
    }

    @Override
    public boolean validate(boolean performValidation) {
        if (getValidationLevel() == ValidationLevel.OFF) {
            return VALID;
        }

        if (!validateEndpointDescriptions(performValidation)) {
            return INVALID;
        }

        return VALID;
    }

    private boolean validateEndpointDescriptions(boolean performValidation) {
        boolean areAllValid = true;
        // Validate all the Endpoints that were created under this Service Description
        Collection<EndpointDescription> endpointDescs = serviceDesc.getEndpointDescriptions_AsCollection();
        for (EndpointDescription endpointDesc:endpointDescs) {
            EndpointDescriptionValidator endpointValidator = new EndpointDescriptionValidator(endpointDesc);

            boolean isEndpointValid = endpointValidator.validate(performValidation);
            if (!isEndpointValid) {
                addValidationFailure(endpointValidator, "Endpoint failed validation");
                areAllValid = false;
            }
        }
        return areAllValid;
    }

}
