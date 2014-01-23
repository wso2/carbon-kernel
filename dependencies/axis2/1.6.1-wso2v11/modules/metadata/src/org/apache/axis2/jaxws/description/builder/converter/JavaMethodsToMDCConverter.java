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

package org.apache.axis2.jaxws.description.builder.converter;

import org.apache.axis2.jaxws.description.builder.ActionAnnot;
import org.apache.axis2.jaxws.description.builder.FaultActionAnnot;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.RequestWrapperAnnot;
import org.apache.axis2.jaxws.description.builder.ResponseWrapperAnnot;
import org.apache.axis2.jaxws.description.builder.WebEndpointAnnot;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebResultAnnot;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebEndpoint;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JavaMethodsToMDCConverter {

    private Method[] methods;
    private Constructor[] constructors;
    private String declaringClass;

    public JavaMethodsToMDCConverter(Method[] methods, Constructor[] constructors,
                                     String declaringClass) {
        this.methods = methods;
        this.constructors = constructors;
        this.declaringClass = declaringClass;
    }

    /**
     * This will drive the creation of a <code>MethodDescriptionComposite</code> for every public 
     * Java Method in the methods array and every Java Constructor in the constructors array.
     *
     * @return - <code>List</code>
     */
    public List<MethodDescriptionComposite> convertMethods() {
        List<MethodDescriptionComposite> mdcList = new
                ArrayList<MethodDescriptionComposite>();
        for (Method method : methods) {
            if (!ConverterUtils.isInherited(method, declaringClass) 
                && Modifier.isPublic(method.getModifiers())) {
                MethodDescriptionComposite mdc = new MethodDescriptionComposite();
                setExceptionList(mdc, method);
                mdc.setMethodName(method.getName());
                setReturnType(mdc, method);
                setIsListType(mdc, method);
                mdc.setStaticModifier(Modifier.isStatic(method.getModifiers()));
                mdc.setFinalModifier(Modifier.isFinal(method.getModifiers()));
                mdc.setDeclaringClass(method.getDeclaringClass().getName());
                attachHandlerChainAnnotation(mdc, method);
                attachOnewayAnnotation(mdc, method);
                attachSoapBindingAnnotation(mdc, method);
                attachRequestWrapperAnnotation(mdc, method);
                attachResponseWrapperAnnotation(mdc, method);
                attachWebEndpointAnnotation(mdc, method);
                attachWebMethodAnnotation(mdc, method);
                attachWebResultAnnotation(mdc, method);
                attachWebServiceRefAnnotation(mdc, method);
                attachActionAnnotation(mdc, method);
                if (method.getGenericParameterTypes().length > 0) {
                    JavaParamToPDCConverter paramConverter = new JavaParamToPDCConverter(
                            method.getGenericParameterTypes(), method.getParameterAnnotations());
                    List<ParameterDescriptionComposite> pdcList = paramConverter.
                            convertParams();
                    ConverterUtils.attachParameterDescriptionComposites(pdcList, mdc);
                }
                mdcList.add(mdc);
            }
        }

        for (Constructor constructor : constructors) {
            MethodDescriptionComposite mdc = new MethodDescriptionComposite();
            mdc.setMethodName("<init>");
            mdc.setDeclaringClass(constructor.getDeclaringClass().getName());
            mdcList.add(mdc);
            if (constructor.getGenericParameterTypes().length > 0) {
                JavaParamToPDCConverter paramConverter = new JavaParamToPDCConverter(
                        constructor.getGenericParameterTypes(),
                        constructor.getParameterAnnotations());
                List<ParameterDescriptionComposite> pdcList = paramConverter.
                        convertParams();
                ConverterUtils.attachParameterDescriptionComposites(pdcList, mdc);
            }
        }

        return mdcList;
    }

    /**
     * This method attaches the list of exceptions for a Java Method to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void setExceptionList(MethodDescriptionComposite mdc, Method method) {
        if (method.getExceptionTypes().length > 0) {
            Type[] exceptionTypes = method.getGenericExceptionTypes();
            String[] exceptions = new String[exceptionTypes.length];
            for (int i = 0; i < exceptionTypes.length; i++) {
                Type type = exceptionTypes[i];
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)type;
                    String fullType = "";
                    fullType = ConverterUtils.getFullType(pt, fullType);
                    exceptions[i] = fullType;
                } else if (type instanceof Class) {
                    exceptions[i] = ((Class)type).getName();
                }
            }
            mdc.setExceptions(exceptions);
        }
    }

    /**
     * This method will drive the attachment of @HandlerChain annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachHandlerChainAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        ConverterUtils.attachHandlerChainAnnotation(mdc, method);
    }

    /**
     * This method will be used to drive the setting of @SOAPBinding annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param composite - <code>MethodDescriptionComposite</code>
     */
    private void attachSoapBindingAnnotation(MethodDescriptionComposite mdc, Method method) {
        ConverterUtils.attachSoapBindingAnnotation(mdc, method);
    }


    /**
     * This method will drive the attachment of @Oneway annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachOnewayAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        Oneway oneway = (Oneway)ConverterUtils.getAnnotation(Oneway.class, method);
        if (oneway != null) {
            mdc.setOneWayAnnot(true);
        } else {
            mdc.setOneWayAnnot(false);
        }
    }

    /**
     * This method will drive the attachment of @RequestWrapper annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachRequestWrapperAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        RequestWrapper requestWrapper = (RequestWrapper)ConverterUtils.getAnnotation(
                RequestWrapper.class, method);
        if (requestWrapper != null) {
            RequestWrapperAnnot rwAnnot = RequestWrapperAnnot.createRequestWrapperAnnotImpl();
            rwAnnot.setClassName(requestWrapper.className());
            rwAnnot.setLocalName(requestWrapper.localName());
            rwAnnot.setTargetNamespace(requestWrapper.targetNamespace());
            rwAnnot.setPartName(requestWrapper.partName());
            mdc.setRequestWrapperAnnot(rwAnnot);
        }
    }

    /**
     * This method will drive the attachment of @ResponeWrapper annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachResponseWrapperAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        ResponseWrapper responseWrapper = (ResponseWrapper)ConverterUtils.getAnnotation(
                ResponseWrapper.class, method);
        if (responseWrapper != null) {
            ResponseWrapperAnnot rwAnnot = ResponseWrapperAnnot.createResponseWrapperAnnotImpl();
            rwAnnot.setClassName(responseWrapper.className());
            rwAnnot.setLocalName(responseWrapper.localName());
            rwAnnot.setTargetNamespace(responseWrapper.targetNamespace());
            rwAnnot.setPartName(responseWrapper.partName());
            mdc.setResponseWrapperAnnot(rwAnnot);
        }
    }

    /**
     * This method will drive the attachment of @WebEndpoint annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachWebEndpointAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        WebEndpoint webEndpoint = (WebEndpoint)ConverterUtils.getAnnotation(
                WebEndpoint.class, method);
        if (webEndpoint != null) {
            WebEndpointAnnot weAnnot = WebEndpointAnnot.createWebEndpointAnnotImpl();
            weAnnot.setName(webEndpoint.name());
            mdc.setWebEndpointAnnot(weAnnot);
        }
    }

    /**
     * This method will drive the attachment of @WebMethod annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachWebMethodAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        WebMethod webMethod = (WebMethod)ConverterUtils.getAnnotation(WebMethod.class,
                                                                      method);
        if (webMethod != null) {
            WebMethodAnnot wmAnnot = WebMethodAnnot.createWebMethodAnnotImpl();
            wmAnnot.setAction(webMethod.action());
            wmAnnot.setExclude(webMethod.exclude());
            wmAnnot.setOperationName(webMethod.operationName());
            mdc.setWebMethodAnnot(wmAnnot);
        }
    }

    /**
     * This method will drive the attachment of @WebResult annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachWebResultAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        WebResult webResult = (WebResult)ConverterUtils.getAnnotation(WebResult.class,
                                                                      method);
        if (webResult != null) {
            WebResultAnnot wrAnnot = WebResultAnnot.createWebResultAnnotImpl();
            wrAnnot.setHeader(webResult.header());
            wrAnnot.setName(webResult.name());
            wrAnnot.setPartName(webResult.partName());
            wrAnnot.setTargetNamespace(webResult.targetNamespace());
            mdc.setWebResultAnnot(wrAnnot);
        }
    }

    /**
     * This method will drive the attachment of @WebServiceRef annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachWebServiceRefAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        ConverterUtils.attachWebServiceRefAnnotation(mdc, method);
    }

    /**
     * This method will drive the attachment of @Action annotation data to the
     * <code>MethodDescriptionComposite</code>
     *
     * @param mdc    - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void attachActionAnnotation(MethodDescriptionComposite mdc, Method
            method) {
        Action action = (Action)ConverterUtils.getAnnotation(Action.class,
                                                             method);
        if (action != null) {
            ActionAnnot actionAnnot = ActionAnnot.createActionAnnotImpl();
            FaultAction[] faults = action.fault();
            
            if (faults != null && faults.length != 0) {
                List<FaultAction> list = new ArrayList<FaultAction>();
                for (FaultAction fault : faults) {
                    FaultActionAnnot faultAnnot =
                        FaultActionAnnot.createFaultActionAnnotImpl();
                    faultAnnot.setClassName(fault.className());
                    faultAnnot.setValue(fault.value());
                    list.add(faultAnnot);
                }
                actionAnnot.setFault(list.toArray(new FaultAction[0]));
            }
            
            actionAnnot.setInput(action.input());
            actionAnnot.setOutput(action.output());
            mdc.setActionAnnot(actionAnnot);
        }
    }

    /**
     * This method will determine the return type of a <code>Method</code> and
     * attach it to a <code>MethodDescriptionComposite</code> object.
     * @param mdc - <code>MethodDescriptionComposite</code>
     * @param method - <code>Method</code>
     */
    private void setReturnType(MethodDescriptionComposite mdc, Method method) {
        Type type = method.getGenericReturnType();
        if (type == null) {
            mdc.setReturnType("void");
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)type;
            String fullType = "";
            fullType = ConverterUtils.getFullType(pt, fullType);
            mdc.setReturnType(fullType);
        } else if (type instanceof Class) {
            mdc.setReturnType(((Class)type).getName());
        } else if (type instanceof GenericArrayType) {
            mdc.setReturnType(type.getClass().getName());
        }
    }
    
    private void setIsListType(MethodDescriptionComposite mdc, Method method) {
        mdc.setIsListType(ConverterUtils.hasXmlListAnnotation(method.getAnnotations()));
    }
        
}
