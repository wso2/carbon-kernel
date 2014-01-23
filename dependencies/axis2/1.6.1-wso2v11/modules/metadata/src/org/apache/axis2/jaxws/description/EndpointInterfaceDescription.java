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

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

/**
 * An EndpointInterfaceDescription corresponds to a particular SEI-based Service Implementation. It
 * can correspond to either either a client to that impl or the actual service impl.
 * <p/>
 * The EndpointInterfaceDescription contains information that is relevant only to an SEI-based (aka
 * Endpoint-based or Java-based) enpdoint; Provider-based endpoint, which are not operation based
 * and do not have an associated SEI, will not have an an EndpointInterfaceDescription class and
 * sub-hierachy.
 * <p/>
 * <pre>
 * <b>EndpointInterfaceDescription details</b>
 * <p/>
 *     CORRESPONDS TO:      An SEI (on both Client and Server)
 * <p/>
 *     AXIS2 DELEGATE:      none
 * <p/>
 *     CHILDREN:            1..n OperationDescription
 * <p/>
 *     ANNOTATIONS:
 *         SOAPBinding [181]
 * <p/>
 *     WSDL ELEMENTS:
 *         portType
 * <p/>
 *  </pre>
 */

public interface EndpointInterfaceDescription {
    
    /**
     * The name of a special operation added to EndpointInterfaceDescriptions for provider 
     * endpoints that do not specify WSDL, and therefore do not have specific WSDL operations
     * created. Note that this is currently only supported for HTTP bindings, not for SOAP bindings.
     */
    public static String JAXWS_NOWSDL_PROVIDER_OPERATION_NAME = "jaxwsNoWSDLProviderOperation";

    public abstract EndpointDescription getEndpointDescription();

    public abstract String getTargetNamespace();

    public abstract OperationDescription getOperation(Method seiMethod);

    /**
     * Returns all the operations matching the operation QName associated with this endpoint
     * description. Note that if the SEI or service implementation (and thus the implicit SEI)
     * contained JAX-WS client side async operations then they will also be returned. Use
     * getDispatchableOperations() to return an array of operations that does not include the JAX-WS
     * client side async operations.
     *
     * @param operationQName
     * @return
     * @see #getDispatchableOperation(QName operationQName)
     */
    public abstract OperationDescription[] getOperation(QName operationQName);

    /**
     * Returns all the dispatchable operations matching the operation QName.  A dispatchable
     * operation is one that is NOT a JAX-WS client-side async method invocation and does NOT
     * carry an @WebMethod(exclude=true) annotation.
     * 
     * JAX-WS client-side async methods which have signatures of the following forms are 
     * filtered out of this list: javax.xml.ws.Response<T>
     * method(...) java.util.concurrent.Future<?> method(..., javax.xml.ws.AsyncHandler<T>)
     * <p/>
     * These methods are filtered because a common use case is to use the same SEI on both the
     * client and service implementation side, generating both the client and service implemntation
     * code from that SEI.  If that SEI happens to contain the client-side-only JAX-WS methods, they
     * should be ingored on the service implemenation side.  To return all the operations, use
     * getOperation(QName).
     *
     * @param operationQName
     * @return
     * @see #getOperation(QName operationQName)
     */
    public abstract OperationDescription[] getDispatchableOperation(QName operationQName);
    public abstract OperationDescription[] getDispatchableOperations();
    public abstract OperationDescription getOperation(String operationName);

    public abstract OperationDescription[] getOperations();

    public abstract OperationDescription[] getOperationForJavaMethod(String javaMethodName);

    public abstract Class getSEIClass();

    public abstract QName getPortType();

    public abstract javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle();

    public abstract javax.jws.soap.SOAPBinding.Style getSoapBindingStyle();

    public abstract javax.jws.soap.SOAPBinding.Use getSoapBindingUse();

}