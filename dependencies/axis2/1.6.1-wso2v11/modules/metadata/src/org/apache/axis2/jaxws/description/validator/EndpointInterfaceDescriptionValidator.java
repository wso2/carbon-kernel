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

import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionWSDL;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 */
public class EndpointInterfaceDescriptionValidator extends Validator {
    EndpointInterfaceDescription epInterfaceDesc;
    EndpointInterfaceDescriptionJava epInterfaceDescJava;
    EndpointInterfaceDescriptionWSDL epInterfaceDescWSDL;

    private static final Log log = LogFactory.getLog(EndpointInterfaceDescriptionValidator.class);
    public EndpointInterfaceDescriptionValidator(EndpointInterfaceDescription toValidate) {
        epInterfaceDesc = toValidate;
        epInterfaceDescJava = (EndpointInterfaceDescriptionJava)epInterfaceDesc;
        epInterfaceDescWSDL = (EndpointInterfaceDescriptionWSDL)epInterfaceDesc;

    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.description.validator.Validator#validate()
    */
    @Override
    public boolean validate() {
        if (getValidationLevel() == ValidationLevel.OFF) {
            return VALID;
        }
        if (!validateSEIvsWSDLPortType()) {
            return INVALID;
        }
        if (!validateSEIvsImplementation()) {
            return INVALID;
        }
        return VALID;
    }

    private boolean validateSEIvsWSDLPortType() {
        PortType portType = epInterfaceDescWSDL.getWSDLPortType();
        if (portType != null) {
            // TODO: Need more validation here, including: operation name, parameters, faults
            List wsdlOperationList = portType.getOperations();

            OperationDescription[] dispatchableOpDescArray = 
                epInterfaceDesc.getDispatchableOperations();
    
            if (wsdlOperationList.size() != dispatchableOpDescArray.length) {
                //We used to throw a Validation error here.
                //I am removing the validation error due to new interpretations
                // of @WebMethod annotations introduced in 2.2 TCK and newer RI/JDK wsgen tools,
                //where it's possilbe for a wsdl to have more operations than what the jaxws 
                //runtime exposes from the SEI impl.
                
                //For Example server endpoint defines the following operations
                // @WebService
                // public MySEIImpl {
                //public boolean x() {...}
                //@WebMethod(exclude=false)
                //public boolean y(){ ...}
                //public String z(){ ...} 
                // ..}
                
                //A wsGen run on this will generate a wsdl with following operations
                //<operation name="x">
                //<operation name="y">
                //<operation name="z">
                
                // And this is the WSDL Provider would return in a ?wsld request,
                // even though the provider may only allow you to dispatch to operation y.

                // To avoid security exposure, the provider may not be
                // able to expose operations x & z, unless it's specifically
                // requested to do so by service application.  If client neglects to regen their 
                // artifacts, it's SEI will only contain operation y.
                // This is the reason why we need to relax this error.
                
                // The additional 
                // operation can be invoked using JAX-WS Dispatch client, 
                // or by regenerating the client artifacts (wsimport)
                // and only when the service chooses for the runtime to expose those
                // operations via custom settings.

                //If TCK tests complaint here, we might have to reverse this
                //change and add the validaiton back.
                                 
                if(log.isWarnEnabled()){
                    log.warn("The number of operations in the WSDL " +
                            "portType does not match the number of methods in the SEI or " +
                            "Web service implementation class.  " +
                            "wsdl operations = [" + toString(wsdlOperationList) +"] " +
                            "dispatch operations = [" + toString(dispatchableOpDescArray) +"]");
                }
                return VALID;
            }

            // If they are the same size, let's check to see if the operation names match
            if (!checkOperationsMatchMethods(wsdlOperationList, dispatchableOpDescArray)) {
                addValidationFailure(this, "The operation names in the WSDL portType " +
                        "do not match the method names in the SEI or Web service i" +
                        "mplementation class.  " +
                        "wsdl operations = [" + toString(wsdlOperationList) +"] " +
                        "dispatch operations = [" + toString(dispatchableOpDescArray) +"]");
                return INVALID;
            }
        }
        return VALID;
    }

    private boolean checkOperationsMatchMethods(List wsdlOperationList, OperationDescription[]
            opDescArray) {
        List<String> opNameList = createWSDLOperationNameList(wsdlOperationList);
        for (int i = 0; i < opDescArray.length; i++) {
            OperationDescription opDesc = opDescArray[i];
            if (opNameList.contains(opDesc.getOperationName())) {
                opNameList.remove(opDesc.getOperationName());
            } else {
                return false;
            }
        }
        return true;
    }

    private List<String> createWSDLOperationNameList(List wsdlOperationList) {
        List<String> opNameList = new ArrayList<String>();
        Iterator wsdlOpIter = wsdlOperationList.iterator();
        while (wsdlOpIter.hasNext()) {
            Object obj = wsdlOpIter.next();
            if (obj instanceof Operation) {
                Operation operation = (Operation)obj;
                opNameList.add(operation.getName());
            }
        }
        return opNameList;
    }

    private boolean validateSEIvsImplementation() {
        // REVIEW: This level of validation is currently being done by the DBC Composite validation
        return VALID;
    }
    
    private static String toString(List wsdlOperationList) {
        String result = "";
        Iterator wsdlOpIter = wsdlOperationList.iterator();
        while (wsdlOpIter.hasNext()) {
            Object obj = wsdlOpIter.next();
            if (obj instanceof Operation) {
                Operation operation = (Operation)obj;
                result += operation.getName() + " ";
            }
        }
        return result;
    }
    
    private static String toString(OperationDescription[] wsdlOpDescs) {
        String result = "";
        for (int i= 0; i<wsdlOpDescs.length; i++) {
            result += wsdlOpDescs[i].getOperationName() + " ";
        }
        return result;
    }
}
