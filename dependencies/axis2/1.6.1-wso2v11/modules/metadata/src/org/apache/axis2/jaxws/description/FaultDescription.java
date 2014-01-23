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


package org.apache.axis2.jaxws.description;

/**
 * A FaultDescription corresponds to a fault that can be thrown from an operation.  NOTE this it not 
 * implemented yet!
 * 
 * FaultDescriptons contain information that is only relevent for and SEI-based service, i.e. one that is invoked via specific
 * methods.  This class does not exist for Provider-based services (i.e. those that specify WebServiceProvider)
 * 
 * <pre>
 * <b>OperationDescription details</b>
 * 
 *     CORRESPONDS TO:      An exception thrown by an operation on an SEI (on both Client and Server)      
 *         
 *     AXIS2 DELEGATE:      None
 *     
 *     CHILDREN:            None
 *     
 *     ANNOTATIONS:
 *         WebFault [224]
 *     
 *     WSDL ELEMENTS:
 *         fault message
 *         
 *  </pre>       
 */

public interface FaultDescription {
    public OperationDescription getOperationDescription();


    /** @return the name of the exception class */
    public String getExceptionClassName();

    /**
     * @return the class that is provided via the getFaultInfo method. "" is returned if the
     *         exception class does not provide a getFaultInfo method (such exceptions are
     *         considered non-compliant by JAX-WS).
     */
    public String getFaultInfo();

    /**
     * @return the name of the JAXB class defined in the schema for this exception.  Note that this
     *         is usually a bean, but it could also be a java primitive.  If not defined, the
     *         getFaultInfo type is returned.
     *         <p/>
     *         NOTE For non-compliant exceptions, the getFaultInfo information is not availabled.
     *         In these cases, a "" is returned.  The runtime (JAXWS) may use other information to
     *         locate and/or build the faultbean
     */
    public String getFaultBean();

    /**
     * @return the element localname (for the JAXB class) corresponding to this exception. "" if not
     *         defined.
     */
    public String getName();

    /**
     * @return the element targetNamespace (for the JAXB class) corresponding to this exception. ""
     *         if not defined.
     */
    public String getTargetNamespace();
    
    /**
     *  @return the name of the wsdl:message that defines the fault element.
     * @return
     */
    public String getMessageName();
}