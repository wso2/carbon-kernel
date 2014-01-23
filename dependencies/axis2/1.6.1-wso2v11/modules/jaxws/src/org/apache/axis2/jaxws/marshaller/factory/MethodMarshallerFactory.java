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

package org.apache.axis2.jaxws.marshaller.factory;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitBareMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitBareMinimalMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitWrappedMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitWrappedMinimalMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitWrappedPlusMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.alt.RPCLitMethodMarshaller;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescriptionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.Holder;

/**
 * The MethodMarshallerFactory creates a Doc/Lit Wrapped, Doc/Lit Bare or RPC Marshaller using
 * SOAPBinding information
 */
public class MethodMarshallerFactory {
	
	private static Log log = LogFactory.getLog(MethodMarshallerFactory.class);
	
    private enum SUBTYPE {
        NORMAL, PLUS, MINIMAL }

    ;

    /** Intentionally private */
    private MethodMarshallerFactory() {
    }

    /**
     * Create Marshaller usining the Binding information
     *
     * @param style
     * @param paramStyle
     * @param isPlus     used to designated DOCLITWRAPPED plus additional rules (i.e. header
     *                   processing)
     * @param isClient
     * @return
     */
    private static MethodMarshaller createMethodMarshaller(SOAPBinding.Style style,
                                                           SOAPBinding.ParameterStyle paramStyle,
                                                           SUBTYPE subType,
                                                           boolean isClient) {  // This flag is for testing only !
        if (style == SOAPBinding.Style.RPC) {
            return new RPCLitMethodMarshaller();
        } else if (paramStyle == SOAPBinding.ParameterStyle.WRAPPED) {
            if (subType == SUBTYPE.PLUS) {
                // Abnormal case
                return new DocLitWrappedPlusMethodMarshaller();
            } else if (subType == SUBTYPE.MINIMAL) {
                // Abnormal case
                return new DocLitWrappedMinimalMethodMarshaller();
            } else {
                return new DocLitWrappedMethodMarshaller();
            }
        } else if (paramStyle == SOAPBinding.ParameterStyle.BARE) {
            if (subType == SUBTYPE.MINIMAL) {
                // Abnormal case
                return new DocLitBareMinimalMethodMarshaller();
            } else {
                return new DocLitBareMethodMarshaller();
            }
        }
        return null;
    }
    
    public static MethodMarshaller getMarshaller(OperationDescription op, boolean isClient) {
        return getMarshaller(op, isClient, null);
    }

    public static MethodMarshaller getMarshaller(OperationDescription op, boolean isClient,
                                                 ClassLoader cl) {

    	// Always make sure the MarshalServiceRuntimeDescription is built before getting the MethodMarshaller.
     	// Getting the MarshalServiceRuntimeDescription will ensure that it is built.
     	ServiceDescription serviceDesc =
             op.getEndpointInterfaceDescription()
               .getEndpointDescription()
               .getServiceDescription();
     	MarshalServiceRuntimeDescription marshalDesc =
             MarshalServiceRuntimeDescriptionFactory.get(serviceDesc);
     	
        MethodMarshaller marshaller = null;
        if (op.getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT) {
            marshaller = createDocLitMethodMarshaller(op, isClient, cl);
        } else if (op.getSoapBindingStyle() == SOAPBinding.Style.RPC) {
            marshaller = createRPCLitMethodMarshaller(isClient);
        }
        return marshaller;
    }

    private static MethodMarshaller createDocLitMethodMarshaller(OperationDescription op,
                                                                 boolean isClient,
                                                                 ClassLoader cl) {
        SOAPBinding.ParameterStyle parameterStyle = null;
        SUBTYPE subType = SUBTYPE.NORMAL;
        if (isDocLitBare(op)) {
            if (isDocLitBareMinimal(op, cl)) {
                subType = SUBTYPE.MINIMAL;
            }
            parameterStyle = SOAPBinding.ParameterStyle.BARE;
        } else {
            if (isDocLitWrappedMinimal(op)) {
                subType = SUBTYPE.MINIMAL;
            } else if (isDocLitWrappedPlus(op)) {
                subType = SUBTYPE.PLUS;
            }
            parameterStyle = SOAPBinding.ParameterStyle.WRAPPED;
        }
        return createMethodMarshaller(SOAPBinding.Style.DOCUMENT, parameterStyle, subType,
                                      isClient);
    }

    private static MethodMarshaller createRPCLitMethodMarshaller(boolean isClient) {
        return createMethodMarshaller(SOAPBinding.Style.RPC, SOAPBinding.ParameterStyle.WRAPPED,
                                      SUBTYPE.NORMAL, isClient);
    }

    protected static boolean isDocLitBare(OperationDescription op) {
        SOAPBinding.ParameterStyle methodParamStyle = op.getSoapBindingParameterStyle();
        if (methodParamStyle != null) {
            return methodParamStyle == SOAPBinding.ParameterStyle.BARE;
        } else {
            SOAPBinding.ParameterStyle SEIParamStyle =
                    op.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
            return SEIParamStyle == SOAPBinding.ParameterStyle.BARE;
        }
    }

    protected static boolean isDocLitWrapped(OperationDescription op) {
        SOAPBinding.ParameterStyle methodParamStyle = op.getSoapBindingParameterStyle();
        if (methodParamStyle != null) {
            return methodParamStyle == SOAPBinding.ParameterStyle.WRAPPED;
        } else {
            SOAPBinding.ParameterStyle SEIParamStyle =
                    op.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
            return SEIParamStyle == SOAPBinding.ParameterStyle.WRAPPED;
        }
    }

    /**
     * If an web service is created using wsgen, it is possible that the sei does not comply with
     * the wrapped rules.  For example, wsgen will allow header parameters and return values. In
     * such cases we will use the DocLitWrappedPlus marshaller to marshal and unmarshal the xml in
     * these extraordinary situations
     *
     * @param op
     * @return
     */
    protected static boolean isDocLitWrappedPlus(OperationDescription op) {
        if (isDocLitWrapped(op)) {
            if (op.isResultHeader()) {
                return true;
            }
            ParameterDescription[] pds = op.getParameterDescriptions();
            for (int i = 0; i < pds.length; i++) {
                if (pds[i].isHeader()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * If a webservices is created without xjc, then there will be no ObjectFactory classes packaged
     * with the webservice.  In such cases, use the doc/lit bare minimal marshaller. This marshaller
     * will use "by java type" marshalling/unmarshalling for primitives and Strings.
     *
     * @param op
     * @return
     */
    protected static boolean isDocLitBareMinimal(OperationDescription op, ClassLoader cl) {
        return isDocLitBare(op) && !isContextPathConstruction(op, cl);
    }

    /**
     * @param op
     * @return true if JAXBContext constructed using CONTEXT PATH
     */
    private static boolean isContextPathConstruction(OperationDescription op, ClassLoader cl) {
        ServiceDescription serviceDesc = op.getEndpointInterfaceDescription()
                .getEndpointDescription().getServiceDescription();
        MarshalServiceRuntimeDescription marshalDesc =
                MarshalServiceRuntimeDescriptionFactory.get(serviceDesc);
        // Get the JAXBContext...Since this is a cached object we incur no penalty by looking at this point.
        Holder<JAXBUtils.CONSTRUCTION_TYPE> holder = new Holder<JAXBUtils.CONSTRUCTION_TYPE>();
        try {
            JAXBContext context = JAXBUtils.getJAXBContext(marshalDesc.getPackages(), holder,
                                                           marshalDesc.getPackagesKey(), cl, null);
        } catch (JAXBException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        if (holder.value == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH) {
            // If JAXBContext was constructed with a context path, this indicates that ObjectFactory (or other
            // objects) are available.  
            return true;
        } else {
            // If JAXBContext was constructed using a class[] or we don't know how it was constructed, then assume
            // that we need to do the specialized "minimal" marshalling.
            return false;
        }
    }

    /**
     * If a web service is created without wsgen, it is possible that the wrapper elements are
     * missing.  In such cases, use the doc/lit wrapped minimal marshaller
     *
     * @param op
     * @return
     */
    protected static boolean isDocLitWrappedMinimal(OperationDescription op) {
        if (isDocLitWrapped(op)) {
            ServiceDescription serviceDesc = op.getEndpointInterfaceDescription()
                    .getEndpointDescription().getServiceDescription();
            MarshalServiceRuntimeDescription marshalDesc =
                    MarshalServiceRuntimeDescriptionFactory.get(serviceDesc);
            String requestWrapper = marshalDesc.getRequestWrapperClassName(op);
            
            
            if (op.isOneWay()) {
                if (!exists(requestWrapper)) {
                        if(log.isDebugEnabled()){
                                log.debug("OneWay Request wrapper class name is NULL.");
                        }
                        return true;
                } 
                
                return false;
            } else  { //This is 2-way or async so both wrappers should exist
                if (!exists(requestWrapper)) {
                    if(log.isDebugEnabled()){
                        log.debug("Request wrapper class name is NULL.");
                    }
                    return true;
                } 

                String responseWrapper = marshalDesc.getResponseWrapperClassName(op);
                if (!exists(responseWrapper)) {
                    if(log.isDebugEnabled()){
                        log.debug("Response wrapper class name is NULL.");
                    }
                    return true;
                }
                
                return false;
            }
            // TODO Do the same for the fault beans
        }
        
        return false;
    }

    private static boolean exists(String className) {
        if (className == null || className.length() == 0) {
            return false;
        }
        // TODO try and load the class
        return true;
    }
}
