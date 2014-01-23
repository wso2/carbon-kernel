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

import org.apache.axis2.description.AxisOperation;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

/**
 * An OperationDescripton corresponds to a method on an SEI.  That SEI could be explicit (i.e.
 * WebService.endpointInterface=sei.class) or implicit (i.e. public methods on the service
 * implementation are the contract and thus the implicit SEI).  Note that while
 * OperationDescriptions are created on both the client and service side, implicit SEIs will only
 * occur on the service side.
 * <p/>
 * OperationDescriptons contain information that is only relevent for and SEI-based service, i.e.
 * one that is invoked via specific methods.  This class does not exist for Provider-based services
 * (i.e. those that specify WebServiceProvider)
 * <p/>
 * <pre>
 * <b>OperationDescription details</b>
 * <p/>
 *     CORRESPONDS TO:      A single operation on an SEI (on both Client and Server)
 * <p/>
 *     AXIS2 DELEGATE:      AxisOperation
 * <p/>
 *     CHILDREN:            0..n ParameterDescription
 *                          0..n FaultDescription (Note: Not fully implemented)
 * <p/>
 *     ANNOTATIONS:
 *         WebMethod [181]
 *         SOAPBinding [181]
 *         Oneway [181]
 *         WebResult [181]
 *         RequestWrapper [224]
 *         ResponseWrapper [224]
 * <p/>
 *     WSDL ELEMENTS:
 *         operation
 * <p/>
 *  </pre>
 */
public interface OperationDescription {
    /**
     * Paramater set on AxisOperation which contains an ArrayList of SOAP header QNames
     * corresponding to SEI parameters. 
     */
    public static final String HEADER_PARAMETER_QNAMES = "org.apache.axis2.jaxws.description.OperationDescription.headerParameterQNames";
    
    public static final String AXIS_OPERATION_PARAMETER = "org.apache.axis2.jaxws.description.OperationDescription.axisOperationParameter";
    
    public EndpointInterfaceDescription getEndpointInterfaceDescription();

    public FaultDescription[] getFaultDescriptions();

    public FaultDescription resolveFaultByExceptionName(String exceptionClassName);

    public ParameterDescription getParameterDescription(int parameterNumber);

    public ParameterDescription getParameterDescription(String parameterName);

    public ParameterDescription[] getParameterDescriptions();
    
    // indicates whether or not an @XmlList annotation was found on the method
    public boolean isListType();

    public abstract AxisOperation getAxisOperation();

    public String getJavaMethodName();

    public String getJavaDeclaringClassName();

    public String[] getJavaParameters();

    /**
     * Client side and non-DBC service side only! Return the SEI method for which a
     * service.getPort(Class SEIClass) created the EndpointDescriptionInterface and the associated
     * OperationDescriptions.  Returns null on the service implementation side.
     *
     * @return
     */
    public Method getSEIMethod();

    /**
     * Service implementation side only!  Given a service implementation class, find the method on
     * that class that corresponds to this operation description.  This is necessary because on the
     * service impl side, the OperationDescriptions can be built using byte-scanning and without the
     * class actually having been loaded.
     *
     * @param serviceImpl
     * @return
     */
    public Method getMethodFromServiceImpl(Class serviceImpl);

    /**
     * Answer if this operation corresponds to the JAX-WS Client-only async methods.  These methods
     * are of the form: javax.xml.ws.Response<T> method(...) java.util.concurrent.Future<?>
     * method(..., javax.xml.ws.AsyncHandler<T>)
     *
     * @return
     */
    public boolean isJAXWSAsyncClientMethod();

    public QName getName();

    public String getOperationName();

    public String getAction();

    public boolean isOneWay();

    public boolean isExcluded();

    public boolean isOperationReturningResult();

    public String getResultName();

    public String getResultTargetNamespace();

    public String getResultPartName();

    public boolean isResultHeader();


    /**
     * Return the Class of the return type.  For JAX-WS async returns of type Response<T> or
     * AsyncHandler<T>, the class associated with Response or AsyncHanler respectively is returned.
     * To get the class associated with <T>
     *
     * @return Class
     * @see getResultActualType()
     */
    public Class getResultType();

    /**
     * Return the actual Class of the type.  For a JAX-WS async return type of Response<T> or
     * AsyncHandler<T>, the class associated with <T> is returned.  For non-JAX-WS async returns,
     * the class associated with the return type is returned.  Note that for a Generic return type,
     * such as List<Foo>, the class associated with List will be returned.
     *
     * @return actual Class
     */
    public Class getResultActualType();

    /**
     * @return the class name of the wrapper class. NOTE: This method will return null if the
     *         request wrapper class is not known during the description layer processing. In such
     *         cases the implementation may use proprietary code to find the class. For example,
     *         JAXWS may look for a matching class in the sei package, in a special jaxws package or
     *         proceed without the class name
     */
    public String getRequestWrapperClassName();

    public String getRequestWrapperTargetNamespace();

    public String getRequestWrapperLocalName();
    
    public String getRequestWrapperPartName();
    /**
     * @return the class name of the wrapper class. NOTE: This method will return null if the
     *         request wrapper class is not known during the description layer processing. In such
     *         cases the implementation may use proprietary code to find the class. For example,
     *         JAXWS may look for a matching class in the sei package, in a special jaxws package or
     *         proceed without the class name
     */
    public String getResponseWrapperClassName();

    public String getResponseWrapperTargetNamespace();

    public String getResponseWrapperLocalName();
    
    public String getResponseWrapperPartName();

    public String[] getParamNames();

    public javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle();

    public javax.jws.soap.SOAPBinding.Style getSoapBindingStyle();

    public javax.jws.soap.SOAPBinding.Use getSoapBindingUse();

    public OperationRuntimeDescription getOperationRuntimeDesc(String name);

    public void setOperationRuntimeDesc(OperationRuntimeDescription ord);

    /**
     * For JAX-WS client-side async operations, this will return the corresponding sync 
     * OperationDescription.
     * 
     * Note that if this method is used within the metadata layer, it is possible that it will return
     * null.  That will happen if the metadata layer is constructed from annotations on the SEI 
     * (not WSDL).  In that case, it is possible that the async methods on the SEI are processed 
     * before the sync method.  In that case, there will be no sync method yet.  If this method
     * is called outside the metadata layer, then if the async methods exist, the sync method
     * should also exist.  
     * 
     * @return OperationDescription corresponding to the sync operation, or null (see note above).
     */
    public OperationDescription getSyncOperation();

    /**
     * @return Attachment Description for the return type or null
     */
    public AttachmentDescription getResultAttachmentDescription();
    
    /**
    * Returns the namespace of binding input message for the operation
    */
    public String getBindingInputNamespace();
    
    /**
    * Returns the namespace of binding output message for the operation
    */
    public String getBindingOutputNamespace();
    
    /**
     * @return a boolean indicator of nested swaRef attachments on the request.
     */
    public boolean hasRequestSwaRefAttachments();
    
    /**
     * @param sets the indicator of nested request swaRef attachments.
     */
    public void setHasRequestSwaRefAttachments(boolean b);
    
    /**
     * @return a boolean indicator of nested swaRef attachments on the response.
     */
    public boolean hasResponseSwaRefAttachments();
    
    /**
     * @param sets the indicator of nested response swaRef attachments.
     */
    public void setHasResponseSwaRefAttachments(boolean b);
    
}