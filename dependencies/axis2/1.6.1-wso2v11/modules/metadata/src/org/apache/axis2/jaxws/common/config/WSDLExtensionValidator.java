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

package org.apache.axis2.jaxws.common.config;


import java.util.Set;
import javax.wsdl.Definition;

import org.apache.axis2.jaxws.description.EndpointDescription;


/**
 * 
 * An implementation of the <code>WSDLExtensionValidator</code> will perform validation
 * on required=true wsdl extensibility elements.
 * The RespectBindingConfigurator will collect all the required=true extensions from wsdl these
 * set of extensions will then have to be validated by WSDLExtensionValidator to check if jax-ws 
 * runtime can process these required extension. The job of WSDLExtensionValidator is to perform
 * these validation.
 */

public interface WSDLExtensionValidator {
    /**
     * Performs validation of input extensionSets from RespectBindingConfigurator.
     * @param extensionSet - Set of found required=true extensions from wsdl, read WSDLValidatorElement object definition.
     * @param wsdlDefinition - a wsdl definition instance.
     * @param endpointDesc - EndpointDescription that describes JAX-WS Endpoint definition.
     */
    public void validate(Set<WSDLValidatorElement> extensionSet, Definition wsdlDefinition, EndpointDescription endpointDesc);
}
