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

import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.OperationDescriptionJava;
import org.apache.axis2.jaxws.description.OperationDescriptionWSDL;

/**
 * 
 */
public class OperationDescriptionValidator extends Validator {
    private OperationDescription opDesc;
    private OperationDescriptionJava opDescJava;
    private OperationDescriptionWSDL opDescWSDL;


    public OperationDescriptionValidator(OperationDescription toValidate) {
        opDesc = toValidate;
        opDescJava = (OperationDescriptionJava)opDesc;
        opDescWSDL = (OperationDescriptionWSDL)opDesc;
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.description.validator.Validator#validate()
    */
    @Override
    public boolean validate() {
        if (getValidationLevel() == ValidationLevel.OFF) {
            return VALID;
        }

        // REVIEW: Should there be operation-level validation performed here?
        return VALID;
    }

}
