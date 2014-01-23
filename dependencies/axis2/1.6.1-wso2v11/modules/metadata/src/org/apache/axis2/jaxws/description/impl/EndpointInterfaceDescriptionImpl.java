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


package org.apache.axis2.jaxws.description.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionWSDL;
import org.apache.axis2.jaxws.description.MethodRetriever;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.impl.ServiceDescriptionImpl;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.WSToolingUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** @see ../EndpointInterfaceDescription */
public class EndpointInterfaceDescriptionImpl
        implements EndpointInterfaceDescription, EndpointInterfaceDescriptionJava,
        EndpointInterfaceDescriptionWSDL {
    private EndpointDescriptionImpl parentEndpointDescription;
    private ArrayList<OperationDescription> operationDescriptions =
            new ArrayList<OperationDescription>();
    private Map<QName, List<OperationDescription>> dispatchableOperations;
    private DescriptionBuilderComposite dbc;

    //Logging setup
    private static final Log log = LogFactory.getLog(EndpointInterfaceDescriptionImpl.class);

    // ===========================================
    // ANNOTATION related information
    // ===========================================

    // ANNOTATION: @WebService
    private WebService webServiceAnnotation;
    private String webServiceTargetNamespace;
    private String webService_Name;


    // ANNOTATION: @SOAPBinding
    // Note this is the Type-level annotation.  See OperationDescription for the Method-level annotation
    private SOAPBinding soapBindingAnnotation;
    private javax.jws.soap.SOAPBinding.Style soapBindingStyle;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.Style SOAPBinding_Style_DEFAULT =
            javax.jws.soap.SOAPBinding.Style.DOCUMENT;
    private javax.jws.soap.SOAPBinding.Use soapBindingUse;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.Use SOAPBinding_Use_DEFAULT =
            javax.jws.soap.SOAPBinding.Use.LITERAL;
    private javax.jws.soap.SOAPBinding.ParameterStyle soapParameterStyle;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.ParameterStyle SOAPBinding_ParameterStyle_DEFAULT =
            javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;

    
    /**
     * Add the operationDescription to the list of operations.  Note that we can not create the
     * list of dispatchable operations at this points.
     * @see #initializeDispatchableOperationsList()
     * 
     * @param operation The operation description to add to this endpoint interface
     */
    void addOperation(OperationDescription operation) {
        if (log.isDebugEnabled()) {
            log.debug("start addOperation for " + operation);
        }
        operationDescriptions.add(operation);
        // Clear the runtime description information, it will need to be rebuilt.
        ServiceDescriptionImpl sd = (getEndpointDescriptionImpl() == null) ? null:
            getEndpointDescriptionImpl().getServiceDescriptionImpl();
        if (sd != null) {
            sd.runtimeDescMap.clear();
        }
        
        if (log.isDebugEnabled()) {
            log.debug("end addOperation");
        }
    }

    /**
     * Construct a service requester (aka client-side) EndpointInterfaceDescription for the
     * given SEI class.  This constructor is used if hierachy is being built fully from annotations
     * and not WSDL.
     * @param sei
     * @param parent
     */
    EndpointInterfaceDescriptionImpl(Class sei, EndpointDescriptionImpl parent) {
        parentEndpointDescription = parent;
        dbc = new DescriptionBuilderComposite();
        dbc.setCorrespondingClass(sei);

        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)
        for (Method method : getSEIMethods(dbc.getCorrespondingClass())) {
            OperationDescription operation = new OperationDescriptionImpl(method, this);
            addOperation(operation);
        } 
    }

    /**
     * Construct a service requester (aka client-side) EndpointInterfaceDescrption for
     * an SEI represented by an AxisService.  This constructor is used if the hierachy is
     * being built fully from WSDL.  The AxisService and underlying AxisOperations were built
     * based on the WSDL, so we will use them to create the necessary objects.
     *
     * @param parent
     */
    EndpointInterfaceDescriptionImpl(EndpointDescriptionImpl parent) {
        parentEndpointDescription = parent;
        dbc = new DescriptionBuilderComposite();
        AxisService axisService = parentEndpointDescription.getAxisService();
        if (axisService != null) {
            ArrayList publishedOperations = axisService.getPublishedOperations();
            Iterator operationsIterator = publishedOperations.iterator();
            while (operationsIterator.hasNext()) {
                AxisOperation axisOperation = (AxisOperation)operationsIterator.next();
                addOperation(new OperationDescriptionImpl(axisOperation, this));
            }
        }
    }
    /**
     * Construct as Provider-based endpoint which does not have specific WSDL operations.  Since there
     * are no specific WSDL operations in this case, there will be a single generic operation that
     * will accept any incoming operation.
     * 
     * @param dbc
     * @param parent
     */
    EndpointInterfaceDescriptionImpl(DescriptionBuilderComposite dbc, 
                                     EndpointDescriptionImpl parent) {
        if (log.isDebugEnabled()) {
            log.debug("Creating a EndpointInterfaceDescription for a generic WSDL-less provider");
        }
        parentEndpointDescription = parent;
        this.dbc = dbc;
        
        // Construct the generic provider AxisOperation to use then construct
        // an OperactionDescription for it.
        AxisOperation genericProviderAxisOp = null;
        try {
            genericProviderAxisOp = 
                AxisOperationFactory.getOperationDescription(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT);
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("eiDescrImplErr"),e);
        }
        
        genericProviderAxisOp.setName(new QName(JAXWS_NOWSDL_PROVIDER_OPERATION_NAME));
        OperationDescription opDesc = new OperationDescriptionImpl(genericProviderAxisOp, this);
        
        addOperation(opDesc);
        AxisService axisService = getEndpointDescription().getAxisService();
        axisService.addOperation(genericProviderAxisOp);
    }

    /**
     * Build an EndpointInterfaceDescription from a DescriptionBuilderComposite.  This EID has
     * WSDL operations associated with it.  It could represent an SEI-based endpoint built from
     * WSDL or annotations, OR it could represent a Provider-based enpoint built from WSDL.  It will
     * not represent a Provider-based endpoint built without WSDL (which does not know about
     * specific WSDL operations). For that type of EID, see:
     * @see  #EndpointInterfaceDescriptionImpl(DescriptionBuilderComposite dbc, EndpointDescriptionImpl parent)
     * @param dbc
     * @param isClass
     * @param parent
     */
    EndpointInterfaceDescriptionImpl(DescriptionBuilderComposite dbc,
                                     boolean isClass,
                                     EndpointDescriptionImpl parent) {

        parentEndpointDescription = parent;
        this.dbc = dbc;

        getEndpointDescription().getAxisService()
                .setTargetNamespace(getEndpointDescriptionImpl().getTargetNamespace());

        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)

        //We are processing the SEI composite
        //For every MethodDescriptionComposite in this list, call OperationDescription 
        //constructor for it, then add this operation

        //Retrieve the relevant method composites for this dbc (and those in the superclass chain)
        
        MethodRetriever methodRetriever = getMethodRetriever();

        Iterator<MethodDescriptionComposite> iter = methodRetriever.retrieveMethods();
        
        if (log.isDebugEnabled()) {
            log.debug("EndpointInterfaceDescriptionImpl: Finished retrieving methods");
        }
        
        MethodDescriptionComposite mdc = null;

        while (iter.hasNext()) {
            mdc = iter.next();

            mdc.setDeclaringClass(dbc.getClassName());

            // Only add if it is a method that would be or is in the WSDL i.e. 
            // don't create an OperationDescriptor for the MDC representing the
            // constructor
            if (DescriptionUtils.createOperationDescription(mdc.getMethodName())) {
                //First check if this operation already exists on the AxisService, if so
                //then use that in the description hierarchy

                AxisService axisService = getEndpointDescription().getAxisService();
                AxisOperation axisOperation = axisService
                        .getOperation(OperationDescriptionImpl.determineOperationQName(this, mdc));
                
                OperationDescription operation =
                    new OperationDescriptionImpl(mdc, this, axisOperation);

                //1) if wsdl is defined then we should only expose operations that are in wsdl.
                //NOTE:If wsdl is defined AxisService will have all operations found in wsdl, 
                //AxisServiceBuilder will do that part before metadata layer is invoked.
                //2) if wsdl not defined we need to expose operations based on annotation, in 
                //which case we need add operations not found in AxisService. 
                if(getWSDLDefinition() != null){
                    if(log.isDebugEnabled()){
                        log.debug("wsdl definition found, we will not expose operation not found in AxisService.");
                    }
                    if (log.isDebugEnabled())
                        log.debug("EID: Just added operation= " + operation.getOperationName());
                    addOperation(operation);
                    //Cater to partial wsdl case, if wsdl is found but AxisService was
                    //not built using this wsdl we need to add operation to AxisService.
                    if(!getEndpointDescriptionImpl().isWSDLFullySpecified()){
                        if(log.isDebugEnabled()){
                            log.debug("Partial wsdl definition found, we will add operation to the AxisService.");
                        }
                        ((OperationDescriptionImpl)operation).addToAxisService(axisService);
                    }
                }
                //Since wsdl is not defined add all operations to AxisService and OperationDescriptionList.
                else if(axisOperation == null) {
                    if(log.isDebugEnabled()){
                        log.debug("wsdl defintion NOT found, we will expose operation using annotations.");
                    }
                    // This axisOperation did not already exist on the AxisService, and so was created
                    // with the OperationDescription, so we need to add the operation to the service
                    ((OperationDescriptionImpl)operation).addToAxisService(axisService);
                    if (log.isDebugEnabled())
                        log.debug("EID: Just added operation= " + operation.getOperationName());
                    addOperation(operation);
                }
                //This is the case where wsdl is not defined and AxisOperation is found in Axis Service.
                //Here we have to ensure that corresponding OperationDescription is added to OperationDescriptionList.
                else if(getWSDLDefinition()==null && axisOperation!=null){
                    if (log.isDebugEnabled())
                        log.debug("EID: Just added operation= " + operation.getOperationName());
                    addOperation(operation);
                }
            }
           
        }

        if (log.isDebugEnabled())
            log.debug("EndpointInterfaceDescriptionImpl: Finished Adding operations");

    }


    private static Method[] getSEIMethods(Class sei) {
        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)
        Method[] seiMethods = sei.getMethods();
        ArrayList methodList = new ArrayList();
        if (sei != null) {
            for (Method method : seiMethods) {

                if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
                    continue;
                }
                methodList.add(method);
                if (!Modifier.isPublic(method.getModifiers())) {
                    // JSR-181 says methods must be public (p14)
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("seiMethodsErr"));
                }
                // TODO: other validation per JSR-181
            }

        }
        return (Method[])methodList.toArray(new Method[methodList.size()]);
//        return seiMethods;
    }

    /**
     * Update a previously created EndpointInterfaceDescription with information from an annotated
     * SEI.  This should only be necessary when the this was created with WSDL. In this case, the
     * information from the WSDL is augmented based on the annotated SEI.
     *
     * @param sei
     */
    void updateWithSEI(Class sei) {
        Class seiClass = dbc.getCorrespondingClass();
        if (seiClass != null && seiClass != sei) {
            throw ExceptionFactory.makeWebServiceException(new UnsupportedOperationException(Messages.getMessage("seiProcessingErr")));
        }
        else if (seiClass != null && seiClass == sei) {
            // We've already done the necessary updates for this SEI
            return;
        }
        else if (sei != null) {
            seiClass = sei;
            dbc.setCorrespondingClass(sei);
            // Update (or possibly add) the OperationDescription for each of the methods on the SEI.
            for (Method seiMethod : getSEIMethods(seiClass)) {

                if (getOperation(seiMethod) != null) {
                    // If an OpDesc already exists with this java method set on it, then the OpDesc has already
                    // been updated for this method, so skip it.
                    continue;
                }
                // At this point (for now at least) the operations were created with WSDL previously.
                // If they had been created from an annotated class and no WSDL, then the seiClass would have 
                // already been set so we would have taken other branches in this if test.  (Note this could
                // change once AxisServices can be built from annotations by the ServiceDescription class).
                // Since the operations were created from WSDL, they will not have a java method, which
                // comes from the SEI, set on them yet.
                //
                // Another consideration is that currently Axis2 does not support overloaded WSDL operations.
                // That means there will only be one OperationDesc build from WSDL.  Still another consideration is
                // that the JAXWS async methods which may exist on the SEI will NOT exist in the WSDL.  An example
                // of these methods for the WSDL operation:
                //     String echo(String)
                // optionally generated JAX-WS SEI methods from the tooling; take note of the annotation specifying the 
                // operation name
                //     @WebMethod(operationName="echo" ...)
                //     Response<String> echoStringAsync(String)
                //     @WebMethod(operationName="echo" ...)
                //     Future<?> echoStringAsync(String, AsyncHandler)
                //
                // So given all the above, the code does the following based on the operation QName
                // (which might also be the java method name; see determineOperationQName for details)
                // (1) If an operationDesc does not exist, add it.
                // (2) If an operationDesc does exist but does not have a java method set on it, set it
                // (3) If an operationDesc does exist and has a java method set on it already, add a new one. 
                //
                // TODO: May need to change when Axis2 supports overloaded WSDL operations

                // Get the QName for this java method and then update (or add) the appropriate OperationDescription
                // See comments below for imporant notes about the current implementation.
                // NOTE ON OVERLOADED OPERATIONS
                // Axis2 does NOT currently support overloading WSDL operations.
                QName seiOperationQName =
                        OperationDescriptionImpl.determineOperationQName(seiMethod);
                OperationDescription[] updateOpDesc = getOperation(seiOperationQName);
                if (updateOpDesc == null || updateOpDesc.length == 0) {
                    // This operation wasn't defined in the WSDL.  Note that the JAX-WS async methods
                    // which are defined on the SEI are not defined as operations in the WSDL.
                    // Although they usually specific the same OperationName as the WSDL operation, 
                    // there may be cases where they do not.
                    OperationDescription operation = new OperationDescriptionImpl(seiMethod, this);
                    addOperation(operation);
                } else {
                    // Currently Axis2 does not support overloaded operations.  That means that even if the WSDL
                    // defined overloaded operations, there would still only be a single AxisOperation, and it
                    // would be the last operation encounterd.
                    // HOWEVER the generated JAX-WS async methods (see above) may (will always?) have the same
                    // operation name and so will come down this path; they need to be added.
                    // TODO: When Axis2 starts supporting overloaded operations, then this logic will need to be changed

                    // Loop through all the opdescs; if one doesn't currently have a java method set, set it
                    // If all have java methods set, then add a new one.  Assume we'll need to add a new one.
                    boolean addOpDesc = true;
                    for (OperationDescription checkOpDesc : updateOpDesc) {
                        if (checkOpDesc.getSEIMethod() == null) {
                            // TODO: Should this be checking (somehow) that the signature matches?  Probably not an issue until overloaded WSDL ops are supported.
                            
                            //Make sure that this is not one of the 'async' methods associated with
                            //this operation. If it is, let it be created as its own opDesc.
                            if (!DescriptionUtils.isAsync(seiMethod)) {
                                ((OperationDescriptionImpl) checkOpDesc).setSEIMethod(seiMethod);
                                addOpDesc = false;
                                break;
                            }
                        }
                    }
                    if (addOpDesc) {
                        OperationDescription operation =
                                new OperationDescriptionImpl(seiMethod, this);
                        addOperation(operation);
                    }
                }
            }
        }
    }

    /**
     * Return the OperationDescriptions corresponding to a particular Java method name. Note that an
     * array is returned because a method could be overloaded.
     *
     * @param javaMethodName String representing a Java Method Name
     * @return
     */
    public OperationDescription[] getOperationForJavaMethod(String javaMethodName) {
        if (DescriptionUtils.isEmpty(javaMethodName)) {
            return null;
        }

        ArrayList<OperationDescription> matchingOperations = new ArrayList<OperationDescription>();
        for (OperationDescription operation : getOperations()) {
            if (javaMethodName.equals(operation.getJavaMethodName())) {
                matchingOperations.add(operation);
            }
        }

        if (matchingOperations.size() == 0)
            return null;
        else
            return matchingOperations.toArray(new OperationDescription[0]);
    }

    /**
     * Return the OperationDesription (only one) corresponding to the OperationName passed in.
     *
     * @param operationName
     * @return
     */
    public OperationDescription getOperation(String operationName) {
        if (DescriptionUtils.isEmpty(operationName)) {
            return null;
        }

        OperationDescription matchingOperation = null;
        for (OperationDescription operation : getOperations()) {
            if (operationName.equals(operation.getOperationName())) {
                matchingOperation = operation;
                break;
            }
        }
        return matchingOperation;
    }

    public OperationDescription[] getOperations() {
        return operationDescriptions.toArray(new OperationDescription[0]);
    }

    public EndpointDescriptionImpl getEndpointDescriptionImpl() {
        return (EndpointDescriptionImpl)parentEndpointDescription;
    }

    public EndpointDescription getEndpointDescription() {
        return parentEndpointDescription;
    }

    /**
     * Return an array of Operations given an operation QName.  Note that an array is returned since
     * a WSDL operation may be overloaded per JAX-WS.
     *
     * @param operationQName
     * @return
     */
    public OperationDescription[] getOperation(QName operationQName) {
        OperationDescription[] returnOperations = null;
        if (!DescriptionUtils.isEmpty(operationQName)) {
            ArrayList<OperationDescription> matchingOperations =
                    new ArrayList<OperationDescription>();
            OperationDescription[] allOperations = getOperations();
            for (OperationDescription operation : allOperations) {
                if (operation.getName().getLocalPart().equals(operationQName.getLocalPart())) {
                    matchingOperations.add(operation);
                }
            }
            // Only return an array if there's anything in it
            if (matchingOperations.size() > 0) {
                returnOperations = matchingOperations.toArray(new OperationDescription[0]);
            }
        }
        return returnOperations;
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.description.EndpointInterfaceDescription#getDispatchableOperation(QName operationQName)
    */
    public OperationDescription[] getDispatchableOperation(QName operationQName) {
        // REVIEW: Can this be synced at a more granular level?  Can't sync on dispatchableOperations because
        //         it may be null, but also the initialization must finish before next thread sees 
        //         dispatachableOperations != null
        synchronized(this) {
            if (dispatchableOperations == null) {
                initializeDispatchableOperationsList();
            }
        }

        // Note that OperationDescriptionImpl creates operation qname with empty namespace. Thus 
        // using only the localPart to get dispatchable operations.
    	QName key = new QName("",operationQName.getLocalPart());
    	List<OperationDescription> operations = dispatchableOperations.get(key);
    	if(operations!=null){
    		return operations.toArray(new OperationDescription[operations.size()]);
    	}
    	return new OperationDescription[0];
    }
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.EndpointInterfaceDescription#getDispatchableOperations()
     */
    public OperationDescription[] getDispatchableOperations() {
        OperationDescription[] returnOperations = null;

        // REVIEW: Can this be synced at a more granular level?  Can't sync on dispatchableOperations because
        //         it may be null, but also the initialization must finish before next thread sees 
        //         dispatachableOperations != null
        synchronized(this) {
            if (dispatchableOperations == null) {
                initializeDispatchableOperationsList();
            }
        }
        Collection<List<OperationDescription>> dispatchableValues = dispatchableOperations.values();
        Iterator<List<OperationDescription>> iteratorValues = dispatchableValues.iterator();
        ArrayList<OperationDescription> allDispatchableOperations = new ArrayList<OperationDescription>();
        while (iteratorValues.hasNext()) {
            List<OperationDescription> opDescList = iteratorValues.next();
            allDispatchableOperations.addAll(opDescList);
        }
        if (allDispatchableOperations.size() > 0) {
            returnOperations = allDispatchableOperations.toArray(new OperationDescription[allDispatchableOperations.size()]);
        }
        return returnOperations;
    }

    /**
     * Create the list of dispatchable operations from the list of all the operations.  A 
     * dispatchable operation is one that can be invoked on the endpoint, so it DOES NOT include:
     * - JAXWS Client Async methods
     * - Methods that have been excluded via WebMethod.exclude annotation
     *
     * Note: We have to create the list of dispatchable operations in a lazy way; we can't
     * create it as the operations are added via addOperations() because on the client
     * that list is built in two parts; first using AxisOperations from the WSDL, which will
     * not have any annotation information (such as WebMethod.exclude).  That list will then
     *  be updated with SEI information, which is the point annotation information becomes
     *  available.
     */
    private void initializeDispatchableOperationsList() {
        dispatchableOperations = new HashMap<QName, List<OperationDescription>>();
        OperationDescription[] opDescs = getOperations();
        for (OperationDescription opDesc : opDescs) {
          if (!opDesc.isJAXWSAsyncClientMethod() && !opDesc.isExcluded()) {
              List<OperationDescription> dispatchableOperationsWithName = dispatchableOperations.get(opDesc.getName());
              if(dispatchableOperationsWithName == null) {
                  dispatchableOperationsWithName = new ArrayList<OperationDescription>();
                  dispatchableOperations.put(opDesc.getName(), dispatchableOperationsWithName);
              }
              dispatchableOperationsWithName.add(opDesc);
          }
        }
    }

    /**
     * Return an OperationDescription for the corresponding SEI method.  Note that this ONLY works
     * if the OperationDescriptions were created from introspecting an SEI.  If the were created
     * with a WSDL then use the getOperation(QName) method, which can return > 1 operation.
     *
     * @param seiMethod The java.lang.Method from the SEI for which an OperationDescription is
     *                  wanted
     * @return
     */
    public OperationDescription getOperation(Method seiMethod) {
        OperationDescription returnOperation = null;
        if (seiMethod != null) {
            OperationDescription[] allOperations = getOperations();
            for (OperationDescription operation : allOperations) {
                if (operation.getSEIMethod() != null && operation.getSEIMethod().equals(seiMethod))
                {
                    returnOperation = operation;
                }
            }
        }
        return returnOperation;
    }

    public Class getSEIClass() {
        return dbc.getCorrespondingClass();
    }
    // Annotation-realted getters

    // ========================================
    // SOAP Binding annotation realted methods
    // ========================================
    public SOAPBinding getAnnoSoapBinding() {
        if (soapBindingAnnotation == null) {
            soapBindingAnnotation = dbc.getSoapBindingAnnot();
        }
        return soapBindingAnnotation;
    }

    public javax.jws.soap.SOAPBinding.Style getSoapBindingStyle() {
        return getAnnoSoapBindingStyle();
    }

    public javax.jws.soap.SOAPBinding.Style getAnnoSoapBindingStyle() {
        if (soapBindingStyle == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().style() != null) {
                soapBindingStyle = getAnnoSoapBinding().style();
            } else {
                soapBindingStyle = SOAPBinding_Style_DEFAULT;
            }
        }
        return soapBindingStyle;
    }

    public javax.jws.soap.SOAPBinding.Use getSoapBindingUse() {
        return getAnnoSoapBindingUse();
    }

    public javax.jws.soap.SOAPBinding.Use getAnnoSoapBindingUse() {
        if (soapBindingUse == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().use() != null) {
                soapBindingUse = getAnnoSoapBinding().use();
            } else {
                soapBindingUse = SOAPBinding_Use_DEFAULT;
            }
        }
        return soapBindingUse;
    }

    public javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle() {
        return getAnnoSoapBindingParameterStyle();
    }

    public javax.jws.soap.SOAPBinding.ParameterStyle getAnnoSoapBindingParameterStyle() {
        if (soapParameterStyle == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().parameterStyle() != null) {
                soapParameterStyle = getAnnoSoapBinding().parameterStyle();
            } else {
                soapParameterStyle = SOAPBinding_ParameterStyle_DEFAULT;
            }
        }
        return soapParameterStyle;
    }

    private Definition getWSDLDefinition() {
        return ((ServiceDescriptionWSDL)getEndpointDescription().getServiceDescription())
                .getWSDLDefinition();
    }

    public PortType getWSDLPortType() {
        PortType portType = null;
//        EndpointDescriptionWSDL endpointDescWSDL = (EndpointDescriptionWSDL) getEndpointDescription();
//        Binding wsdlBinding = endpointDescWSDL.getWSDLBinding();
//        if (wsdlBinding != null) {
//            portType = wsdlBinding.getPortType();
//        }
        Definition wsdlDefn = getWSDLDefinition();
        if (wsdlDefn != null) {
            String tns = getEndpointDescription().getTargetNamespace();
            String localPart = getEndpointDescription().getName();
            if (localPart != null) {
                portType = wsdlDefn.getPortType(new QName(tns, localPart));
            }
        }
        return portType;
    }


    public String getTargetNamespace() {
        return getAnnoWebServiceTargetNamespace();
    }

    public WebService getAnnoWebService() {
        if (webServiceAnnotation == null) {
            webServiceAnnotation = dbc.getWebServiceAnnot();
        }
        return webServiceAnnotation;
    }

    public String getAnnoWebServiceTargetNamespace() {
        if (webServiceTargetNamespace == null) {
            if (getAnnoWebService() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebService().targetNamespace())) {
                webServiceTargetNamespace = getAnnoWebService().targetNamespace();
            } else {
                // Default value per JSR-181 MR Sec 4.1 pg 15 defers to "Implementation defined, 
                // as described in JAX-WS 2.0, section 3.2" which is JAX-WS 2.0 Sec 3.2, pg 29.
                webServiceTargetNamespace =
                    DescriptionUtils.makeNamespaceFromPackageName(
                            DescriptionUtils.getJavaPackageName(dbc.getClassName()),
                            "http");
            }
        }
        return webServiceTargetNamespace;
    }

    public String getAnnoWebServiceName() {
        if (webService_Name == null) {

            if (getAnnoWebService() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebService().name())) {
                webService_Name = getAnnoWebService().name();
            } else {
                // Per the JSR 181 Specification, the default
                // is the simple name of the class.
                webService_Name = DescriptionUtils.getSimpleJavaClassName(dbc.getClassName());
            }
        }
        return webService_Name;
    }

    public String getName() {
        return getAnnoWebServiceName();
    }

    public QName getPortType() {
        String name = getName();
        String tns = getTargetNamespace();
        return new QName(tns, name);
    }

    public String toString() {
        final String newline = "\n";
        final String sameline = "; ";
        StringBuffer string = new StringBuffer();
        try {
            string.append(super.toString());
            string.append(newline);
            string.append("Name: " + getName());
            string.append(sameline);
            string.append("PortType: " + getPortType());
            //
            string.append(newline);
            string.append("SOAP Style: " + getSoapBindingStyle());
            string.append(sameline);
            string.append("SOAP Use: " + getSoapBindingUse());
            string.append(sameline);
            string.append("SOAP Paramater Style: " + getSoapBindingParameterStyle());
            //
            string.append(newline);
            OperationDescription[] operations = getOperations();
            if (operations != null && operations.length > 0) {
                string.append("Number of operations: " + operations.length);
                for (OperationDescription operation : operations) {
                    string.append(newline);
                    string.append("Operation: " + operation.toString());
                }
            } else {
                string.append("OperationDescription array is null");
            }
        }
        catch (Throwable t) {
            string.append(newline);
            string.append("Complete debug information not currently available for " +
                    "EndpointInterfaceDescription");
            return string.toString();
        }
        return string.toString();
    }
    /**
     * Get an annotation.  This is wrappered to avoid a Java2Security violation.
     * @param cls Class that contains annotation 
     * @param annotation Class of requrested Annotation
     * @return annotation or null
     */
    private static Annotation getAnnotation(final Class cls, final Class annotation) {
        return (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return cls.getAnnotation(annotation);
            }
        });
    }
    
    
    /*
     * This method returns a method retriever that properly implements the specified behavior, 
     * which is determined by a user-defined system property
     */
    private MethodRetriever getMethodRetriever () {

        MethodRetriever methodRetriever = null;
        //default behavior
        boolean newSunBehavior = true;
        //Lets make sure the default behavior is supported by packaged JDK.
        //this check verifies JAX-WS2.2 tooling is supported.
        newSunBehavior = isNewSunBehaviorSupported();
        
        //The system property always overrides the manifest
        //property. So, if both are set than the manifest property will be ignored
        
        String legacyWebmethod = readLegacyWebMethodFlag();
        if (WSToolingUtils.hasValue(legacyWebmethod) && legacyWebmethod.equalsIgnoreCase("true")) {
            if (log.isDebugEnabled()){
                log.debug("EndpointInterfaceDescriptionImpl: System or Manifest property USE_LEGACY_WEB_METHOD_RULES set to true" );
            }
            //LegacyWebmethod property set to true, use old JAX-WS tooling behavior.  
            newSunBehavior = false;
        }else {
            //If LegacyWebmehtod was not set check for sun property.
            String newSunRulesFlag = getNewSunRulesFlag();
            if (WSToolingUtils.hasValue(newSunRulesFlag) && newSunRulesFlag.equalsIgnoreCase("true")) {
                if (log.isDebugEnabled()){
                    log.debug("EndpointInterfaceDescriptionImpl: System property USE_LEGACY_WEB_METHOD_RULES_SUN set to true" );
                }
                newSunBehavior = false;;
            }
        }
        //Now based on the outcome of LegacyWebmethod and sun property check, retrieve methods to expose.
        methodRetriever = newSunBehavior ? new PostRI216MethodRetrieverImpl(dbc, this) : new LegacyMethodRetrieverImpl(dbc, this);
        //set LegacyWebmethod Definition on MethodRetriever.
        methodRetriever.setLegacyWebMethod(legacyWebmethod);
        if(log.isDebugEnabled()) {
            if (newSunBehavior) {
                log.debug("getMethodRetriever: returning a PostRI216MethodRetrieverImpl");
            } else {
                log.debug("getMethodRetriever: returning a LegacyMethodRetrieverImpl");
            }
        }
        
        return methodRetriever;
    }
    
    /**
     * The user has indicated that they want to use the new Sun behavior (regardless) of which flag 
     * they were using.
     * This method determines whether we have the proper JDK version for using the new SUN behavior for
     * retrieving methods. We determine this by checking the version of WsGen.
     * @param propertyToSet
     * @return
     */
    private boolean isNewSunBehavior(String propertyToSet) {

        if (log.isDebugEnabled()) {
            log.debug("isNewSunBehavior: Validating that JDK version can be used with property: " +propertyToSet);

        }
        
        boolean versionValid = false;

        try {
            
            String wsGenVersion = WSToolingUtils.getWsGenVersion();
            
            versionValid = WSToolingUtils.isValidVersion(wsGenVersion);
            
            if (log.isDebugEnabled()) {
                log.debug("isNewSunBehavior: versionValid is: " +versionValid);
            }
            
            if (!versionValid) {

                if (log.isWarnEnabled()) {
                    log.warn("You are attempting set a property: "
                        + propertyToSet
                        + " This property is not supported with this version of the JDK");
                }
            }

            // We don't want to affect existing systems, if anything goes
            // wrong just display
            // a warning and default to old behavior
        } catch (ClassNotFoundException e) {
            if (log.isWarnEnabled()) {
                log.warn(" Unable to determine WsGen version being used");
            }
        } catch (IOException ioex) {
            if (log.isWarnEnabled()) {
                log.warn(" Unable to determine WsGen version being used");
            }
        }
        
        return versionValid;
    }
    /**
     * The user has indicated that they want to use the new Sun behavior (regardless) of which flag 
     * they were using.
     * This method determines whether we have the proper JDK version for using the new SUN behavior for
     * retrieving methods. We determine this by checking the version of WsGen.
     * @param propertyToSet
     * @return
     */
    private boolean isNewSunBehaviorSupported() {

        if (log.isDebugEnabled()) {
            log.debug("isNewSunBehavior: Validating that JDK version can be used");

        }
        
        boolean versionValid = false;

        try {
            
            String wsGenVersion = WSToolingUtils.getWsGenVersion();
            
            versionValid = WSToolingUtils.isValidVersion(wsGenVersion);
            
            if (log.isDebugEnabled()) {
                log.debug("isNewSunBehavior: versionValid is: " +versionValid);
            }
            
            if (!versionValid) {

                if (log.isDebugEnabled()) {
                    log.debug("New Sun tooling behavior is not supported with this version of the JDK");
                }
            }

            // We don't want to affect existing systems, if anything goes
            // wrong just display
            // a warning and default to old behavior
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug(" Unable to determine WsGen version being used");
            }
        } catch (IOException ioex) {
            if (log.isDebugEnabled()) {
                log.debug(" Unable to determine WsGen version being used");
            }
        }
        
        return versionValid;
    }
    private String readLegacyWebMethodFlag () {
        
       String legacyWebmethod= null;
            
        try {
            legacyWebmethod = (String) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() {
                        //System property takes precedence over manifest property.
                        //So first lets check for system property.
                        return (System.getProperty(MDQConstants.USE_LEGACY_WEB_METHOD_RULES));
                    }
                });
        } catch (PrivilegedActionException e) {
            // Swallow and continue
            if (log.isWarnEnabled()) {
                log.debug("Exception getting USE_LEGACY_WEB_METHOD_RULES system property: " +e.getException());
            }
        }
        //System property not set, so let return the manifest property.
        if(!WSToolingUtils.hasValue(legacyWebmethod)){
            if (log.isDebugEnabled()){
                log.debug("EndpointInterfaceDescriptionImpl: system property '"+MDQConstants.USE_LEGACY_WEB_METHOD_RULES + "' not set" );
            }
            ConfigurationContext configContext = getEndpointDescription().getServiceDescription().getAxisConfigContext();
            if(configContext!=null){
                if (log.isDebugEnabled()){
                    log.debug("EndpointInterfaceDescriptionImpl: Reading Manifest property '"+ MDQConstants.USE_MANIFEST_LEGACY_WEB_METHOD_RULES+"'" );
                }
                String param =(String)configContext.getProperty(MDQConstants.USE_MANIFEST_LEGACY_WEB_METHOD_RULES);
                if(param == null){
                    if (log.isDebugEnabled()){
                        log.debug("EndpointInterfaceDescriptionImpl: Manifest property '"+ MDQConstants.USE_MANIFEST_LEGACY_WEB_METHOD_RULES+ "' not set" );
                    }
                }else{
                    if (log.isDebugEnabled()){
                        log.debug("EndpointInterfaceDescriptionImpl: Manifest property '"+ MDQConstants.USE_MANIFEST_LEGACY_WEB_METHOD_RULES+ "' is set to"+param );
                    }
                    legacyWebmethod = param;
                }
            }else{
                if (log.isDebugEnabled()){
                    log.debug("EndpointInterfaceDescriptionImpl: Unable to Read Manifest property '"+ MDQConstants.USE_MANIFEST_LEGACY_WEB_METHOD_RULES+"'" );
                    log.debug("EndpointInterfaceDescriptionImpl: AxisConfigContext was null" );
                }
            }
        }else{
            if (log.isDebugEnabled()){
                log.debug("EndpointInterfaceDescriptionImpl: system property '"+MDQConstants.USE_LEGACY_WEB_METHOD_RULES + "' set" );
            }
        }
        return legacyWebmethod;
    }
    
    private static String getNewSunRulesFlag () {
        
        String newSunRulesFlag = null;
            
        try {
            newSunRulesFlag = (String) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() {
                        return (System.getProperty(MDQConstants.USE_LEGACY_WEB_METHOD_RULES_SUN));
                    }
                });
        } catch (PrivilegedActionException e) {
            // Swallow and continue
            if (log.isWarnEnabled()) {
                log.debug("Exception getting USE_LEGACY_WEB_METHOD_RULES_SUN system property: " +e.getException());
            }
        }
        if(WSToolingUtils.hasValue(newSunRulesFlag)){
            if (log.isDebugEnabled()){
                log.debug("EndpointInterfaceDescriptionImpl: system property '"+MDQConstants.USE_LEGACY_WEB_METHOD_RULES_SUN + "' is set" );
                log.debug("MDQConstants.USE_LEGACY_WEB_METHOD_RULES_SUN ="+newSunRulesFlag);
            }            
        }else{
            if (log.isDebugEnabled()){
                log.debug("EndpointInterfaceDescriptionImpl: system property '"+MDQConstants.USE_LEGACY_WEB_METHOD_RULES_SUN + "' is not set" );
            }
        }
        return newSunRulesFlag;
    }
    

}
