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

package org.apache.axis2.jaxws.marshaller.impl.alt;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.AttachmentDescription;
import org.apache.axis2.jaxws.description.AttachmentType;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.OccurrenceArray;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 *   1) The web service is Doc/Lit Wrapped, and 
 *   2) The wrapper and fault bean objects are missing (hence the term 'Minimal')
 *   
 */
/**
 * @author scheu
 *
 */
public class DocLitWrappedMinimalMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(DocLitWrappedMinimalMethodMarshaller.class);

    private static JAXBBlockFactory factory =
        (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
    
    public DocLitWrappedMinimalMethodMarshaller() {
        super();
    }

    public Message marshalRequest(Object[] signatureArguments, 
            OperationDescription operationDesc,
            Map<String, Object> requestContext)
        throws WebServiceException {

        if (log.isDebugEnabled()) {
            log.debug("enter marshalRequest operationDesc = " + operationDesc.getName());
        }
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        Protocol protocol = Protocol.getProtocolForBinding(endpointDesc.getClientBindingID());

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {

            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:operation>
            //      <param>hello</param>
            //    </m:operation>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element under the body.
            //   2) The data blocks are located underneath the body.  
            //   3) The name of the data block (m:operation) is defined by the schema and match the name of the operation.
            //      This is called the wrapper element.  The wrapper element has a corresponding JAXB element pojo.
            //   4) The parameters (m:param) are child elements of the wrapper element.
            //   5) NOTE: For doc/literal wrapped "minimal", the wrapper JAXB element pojo is missing.

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Indicate the style and wrapper element name.  This triggers the message to
            // put the data blocks underneath the wrapper element
            m.setStyle(Style.DOCUMENT);
            m.setIndirection(1);
            m.setOperationElement(getRequestWrapperQName(operationDesc));

            // The input object represent the signature arguments.
            // Signature arguments are both holders and non-holders
            // Convert the signature into a list of JAXB objects for marshalling
            List<PDElement> pdeList =
                    MethodMarshallerUtils.getPDElements(marshalDesc,
                                                        pds,
                                                        signatureArguments,
                                                        true,  // input
                                                        true,  // doc/lit wrapped
                                                        true); // false

            // We want to use "by Java Type" marshalling for 
            // all objects
            for (int i=0; i<pdeList.size(); i++) {
                
                PDElement pde = pdeList.get(i);
                
                // If the actual value is an array or list
                // this should be modeled as an 
                // occurrence of elements
                pde = processOccurrence(pde);
                pdeList.set(i, pde);
                
                // Set by java type marshaling
                ParameterDescription pd = pde.getParam();
                Class type = pd.getParameterActualType();
                pde.setByJavaTypeClass(type);
            }
            
            // Put values onto the message
            MethodMarshallerUtils.toMessage(pdeList, m, packages, requestContext);
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasRequestSwaRefAttachments()) {
                m.setDoingSWA(true);
            }

            if (log.isDebugEnabled()) {
                log.debug("exit marshalRequest");
            }
            return m;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("fail marshalRequest e=" + e);
                log.debug(" " + JavaUtils.stackToString(e));
            }
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
    }

    public Object[] demarshalRequest(Message message, OperationDescription operationDesc)
            throws WebServiceException {

        if (log.isDebugEnabled()) {
            log.debug("enter demarshalRequest operationDesc = " + operationDesc.getName());
        }
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
         // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:operation>
            //      <param>hello</param>
            //    </m:operation>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element under the body.
            //   2) The data blocks are located underneath the body.  
            //   3) The name of the data block (m:operation) is defined by the schema and match the name of the operation.
            //      This is called the wrapper element.  The wrapper element has a corresponding JAXB element pojo.
            //   4) The parameters (m:param) are child elements of the wrapper element.
            //   5) NOTE: For doc/literal wrapped "minimal", the wrapper JAXB element pojo is missing.
            
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Indicate that the style is Document, but the blocks are underneath
            // the wrapper element
            message.setStyle(Style.DOCUMENT);
            message.setIndirection(1);

            // Create an array of indices indicating where each parameter is located in the body
            // A -1 indicates that the parameter is not in the body
            int[] firstIndex = new int[pds.length];
            int[] lastIndex = new int[pds.length];
            for (int i=0; i<firstIndex.length; i++) {
                firstIndex[i] = -1;  
                lastIndex[i] = -1;  
            }
            calculateBodyIndex(firstIndex, lastIndex, pds, message.getBodyBlockQNames());
            
            // We want to use "by Java Type" unmarshalling for 
            // all objects
            Class[] javaTypes = new Class[pds.length];
            Class[] componentJavaTypes = new Class[pds.length];
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                javaTypes[i] = pd.getParameterActualType();
                if (javaTypes[i].isArray()) {
                    componentJavaTypes[i] = javaTypes[i].getComponentType();
                } else if (javaTypes[i].isAssignableFrom(List.class)) {
                    componentJavaTypes[i] = getComponentType(pd, operationDesc, marshalDesc);
                } else {
                    componentJavaTypes[i]= null;
                }   
            }

            // Unmarshal the ParamValues from the Message
            List<PDElement> pvList = getPDElementsForDocLitWrappedMinimal(pds,
                    message,
                    packages,
                    true, // input
                    false,
                    javaTypes,
                    componentJavaTypes,
                    firstIndex,
                    lastIndex);

            // Build the signature arguments
            Object[] sigArguments = MethodMarshallerUtils.createRequestSignatureArgs(pds, pvList);

            // Note:  The code used to check to ensure that parameters were not null.
            // The code sited 3.6.2.3 of the JAX-WS specification, but that portion of the specification
            // is for rpc/literal marshaling.  This code is for document/literal marshaling.
            // Nulls are allowed.
            
            if (log.isDebugEnabled()) {
                log.debug("exit demarshalRequest operationDesc");
            }
            return sigArguments;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("fail demarshalRequest e= " + e);
                log.debug(" " + JavaUtils.stackToString(e));
            }
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }


    public Message marshalResponse(Object returnObject, Object[] signatureArgs,
                                   OperationDescription operationDesc, Protocol protocol)
            throws WebServiceException {


        if (log.isDebugEnabled()) {
            log.debug("enter marshalResponse operationDesc = " + operationDesc.getName());
        }
        
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        // We want to respond with the same protocol as the request,
        // It the protocol is null, then use the Protocol defined by the binding
        if (protocol == null) {
            protocol = Protocol.getProtocolForBinding(endpointDesc.getBindingType());
        }

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:operationResponse ... >
            //       <param>hello</param>
            //    </m:operationResponse>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element in the message
            //   2) The data blocks are located underneath the body element. 
            //   3) The name of the data block (m:operationResponse) is defined by the schema.
            //      It matches the operation name + "Response", and it has a corresponding JAXB element.
            //      This element is called the wrapper element
            //   4) The parameters are (param) are child elements of the wrapper element.
            //   5) For "minimal" the pojo bean representing the OperationResponse is missing.
            
            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Indicate the style and wrapper element name.  This triggers the message to
            // put the data blocks underneath the operation element
            m.setStyle(Style.DOCUMENT);
            m.setIndirection(1);
            QName responseOp = getResponseWrapperQName(operationDesc);
            m.setOperationElement(responseOp);

            // Put the return object onto the message
            Class returnType = operationDesc.getResultActualType();
            String returnNS = null;
            String returnLocalPart = null;
            if (operationDesc.isResultHeader()) {
                returnNS = operationDesc.getResultTargetNamespace();
                returnLocalPart = operationDesc.getResultName();
            } else {
                returnNS = operationDesc.getResultTargetNamespace();
                returnLocalPart = operationDesc.getResultPartName();
            }

            if (returnType != void.class) {
                Element returnElement = null;
                QName returnQName = new QName(returnNS, returnLocalPart);
                if (representAsOccurrence(returnObject, returnType)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Return element isListOrArray");
                    }
                    OccurrenceArray occurrenceArray = new OccurrenceArray(returnObject);
                    JAXBElement jaxb = new JAXBElement(returnQName, returnType, occurrenceArray);
                    returnElement = new Element(jaxb, returnQName);
                } else if (marshalDesc.getAnnotationDesc(returnType).hasXmlRootElement()) {
                    returnElement = new Element(returnObject, returnQName);
                } else {
                    returnElement = new Element(returnObject, returnQName, returnType);
                }
                MethodMarshallerUtils.toMessage(returnElement,
                                                returnType,
                                                operationDesc.isListType(),
                                                marshalDesc,
                                                m,
                                                returnType, // force marshal by type
                                                operationDesc.isResultHeader());
            }

            // Convert the holder objects into a list of JAXB objects for marshalling
            List<PDElement> pdeList =
                    MethodMarshallerUtils.getPDElements(marshalDesc,
                                                        pds,
                                                        signatureArgs,
                                                        false,  // output
                                                        true,   // doc/lit wrapped
                                                        false); // not rpc

            // We want to use "by Java Type" marshalling for 
            // all objects
            for (int i=0; i<pdeList.size(); i++) {
                
                PDElement pde = pdeList.get(i);
                
                // If the actual value is an array or list
                // this should be modeled as an 
                // occurrence of elements
                pde = processOccurrence(pde);
                pdeList.set(i, pde);
                
                // Set by java type marshaling
                ParameterDescription pd = pde.getParam();
                Class type = pd.getParameterActualType();
                pde.setByJavaTypeClass(type);
            }

            // TODO Should we check for null output body values?  Should we check for null output header values ?
            // Put values onto the message
            MethodMarshallerUtils.toMessage(pdeList, m, packages, null);
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasResponseSwaRefAttachments()) {
                m.setDoingSWA(true);
            }

            if (log.isDebugEnabled()) {
                log.debug("exit marshalResponse");
            }
            return m;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("fail marshalResponse e= " + e);
                log.debug(" " + JavaUtils.stackToString(e));
            }
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * If the PDElement represents an array or List,
     * a new PDElement is returned that models the 
     * the array or List as a series of elements.
     * @param pde
     * @return new PDElement or same PDElement
     */
    private static PDElement processOccurrence(PDElement pde) {
        // All arrays and lists should be marshaled as
        // separate (occurrence) elements
        Element element = pde.getElement();
        if (element != null) {
            Object elementValue = element.getElementValue();
            if (elementValue instanceof JAXBElement) {
                JAXBElement jaxb = (JAXBElement) elementValue;
                Object value = jaxb.getValue();
                if (representAsOccurrence(value, jaxb.getDeclaredType())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Build OccurrentArray");
                    }
                    OccurrenceArray occurrenceArray = new OccurrenceArray(value);
                    JAXBElement newJAXBElement = 
                        new JAXBElement(jaxb.getName(),
                                jaxb.getDeclaredType(),
                                occurrenceArray);
                    element = new Element(newJAXBElement, jaxb.getName());
                    pde = new PDElement(pde.getParam(), element, null);
                }
            }
        }
        return pde;
    }
    
    /**
     * @param value
     * @return true if this value should be represented as a series of occurrence
     * elements
     */
    private static boolean representAsOccurrence(Object value, Class inClass) {
        // Represent as a series of occurrence elements if not List/Array
        // but not a byte[].  A byte[] has its own encoding.
        
        boolean rc = false;
        Class cls = (value == null) ? inClass : value.getClass();
  
        if (cls == null) {
            return true;
        }else if (List.class.isAssignableFrom(cls)) {
            rc = true;
        } else if (cls.equals(byte[].class)) {
            rc = false;  // assume base64binary
        } else if (cls.isArray()) {
            rc = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("representAsOccurrence for " + JavaUtils.getObjectIdentity(value) + 
                        " of class: " + inClass + rc);
        }
        return rc;
    }

    public Object demarshalResponse(Message message, Object[] signatureArgs,
                                    OperationDescription operationDesc)
            throws WebServiceException {

        if (log.isDebugEnabled()) {
            log.debug("enter demarshalResponse operationDesc = " + operationDesc.getName());
        }
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
         // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:operationResponse ... >
            //       <param>hello</param>
            //    </m:operationResponse>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element in the message
            //   2) The data blocks are located underneath the body element. 
            //   3) The name of the data block (m:operationResponse) is defined by the schema.
            //      It matches the operation name + "Response", and it has a corresponding JAXB element.
            //      This element is called the wrapper element
            //   4) The parameters are (param) are child elements of the wrapper element.
            //   5) For "minimal" the pojo bean representing the OperationResponse is missing
            
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Indicate that the style is Document. 
            message.setStyle(Style.DOCUMENT);
            message.setIndirection(1);

            // Create an array of indices indicating where each parameter is located in the body
            // A -1 indicates that the parameter is not in the body
            int[] firstIndex = new int[pds.length];
            int[] lastIndex = new int[pds.length];
            for (int i=0; i<firstIndex.length; i++) {
                firstIndex[i] = -1;  
                lastIndex[i] = -1;  
            }
            calculateBodyIndex(firstIndex, lastIndex, pds, message.getBodyBlockQNames());
            
            int firstBodyParamIndex = -1;
            
            for (int i=0; i < pds.length; i++) {
                if (pds[i].getMode() == Mode.OUT || pds[i].getMode() == Mode.INOUT) {
                    if (firstIndex[i] >= 0 && firstBodyParamIndex == -1) {
                        firstBodyParamIndex = firstIndex[i];
                    }
                }
            }
            
            
            // Get the return value.
            Class returnType = operationDesc.getResultActualType();
            Class returnComponentType = null;
            if (returnType.isArray()) {
                returnComponentType = returnType.getComponentType();
            } else if (returnType.isAssignableFrom(List.class)) {
                returnComponentType = getComponentType(null, operationDesc, marshalDesc);
            } else {
                returnComponentType= null;
            }
            Object returnValue = null;
            boolean hasReturnInBody = false;
            if (returnType != void.class) {
                // If the webresult is in the header, we need the name of the header so that we can find it.
                Element returnElement = null;
                if (operationDesc.isResultHeader()) {
                    returnElement = getReturnElementForDocLitWrappedMinimal(packages,
                                                                           message,
                                                                           returnType,
                                                                           returnComponentType,
                                                                           operationDesc.isListType(),
                                                                           true,  // is a header
                                                                           operationDesc.getResultTargetNamespace(),
                                                                           // header ns
                                                                           operationDesc.getResultPartName(),     // header local part
                                                                           MethodMarshallerUtils.numOutputBodyParams(pds) > 0,
                                                                           firstBodyParamIndex);

                } else {
                    returnElement = getReturnElementForDocLitWrappedMinimal(packages,
                                                                           message,
                                                                           returnType,
                                                                           returnComponentType,
                                                                           operationDesc.isListType(),
                                                                           false,
                                                                           null,
                                                                           null,
                                                                           MethodMarshallerUtils.numOutputBodyParams(pds) > 0,
                                                                           firstBodyParamIndex);
                    hasReturnInBody = true;

                }
                returnValue = returnElement.getTypeValue();
            }

            // We want to use "by Java Type" unmarshalling for 
            // all objects
            Class[] javaTypes = new Class[pds.length];
            Class[] componentJavaTypes = new Class[pds.length];
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                Class type = pd.getParameterActualType();
                
                if (type.isArray()) {
                    componentJavaTypes[i] = type.getComponentType();
                } else if (type.isAssignableFrom(List.class)) {
                    componentJavaTypes[i] = getComponentType(pd, operationDesc, marshalDesc);
                } else {
                    componentJavaTypes[i]= null;
                }
                javaTypes[i] = type;
            }

            // Unmarshall the ParamValues from the Message
            List<PDElement> pvList = getPDElementsForDocLitWrappedMinimal(pds,
                    message,
                    packages,
                    false, // output
                    hasReturnInBody,
                    javaTypes,
                    componentJavaTypes,
                    firstIndex,
                    lastIndex);


            // Populate the response Holders
            MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);

            if (log.isDebugEnabled()) {
                log.debug("exit demarshalResponse");
            }
            return returnValue;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("fail demarshalResponse e=" + e);
                log.debug(" " + JavaUtils.stackToString(e));
            }
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalFaultResponse(Throwable throwable,
                                        OperationDescription operationDesc, Protocol protocol)
            throws WebServiceException {

        if (log.isDebugEnabled()) {
            log.debug("marshalFaultResponse operationDesc = " + operationDesc);
        }
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        MarshalServiceRuntimeDescription marshalDesc =
                MethodMarshallerUtils.getMarshalDesc(endpointDesc);
        TreeSet<String> packages = marshalDesc.getPackages();

        // We want to respond with the same protocol as the request,
        // It the protocol is null, then use the Protocol defined by the binding
        if (protocol == null) {
            protocol = Protocol.getProtocolForBinding(endpointDesc.getBindingType());
        }

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Put the fault onto the message
            MethodMarshallerUtils.marshalFaultResponse(throwable,
                                                       marshalDesc,
                                                       operationDesc,
                                                       m);
            if (log.isDebugEnabled()) {
                log.debug("exit marshalFaultResponse");
            }
            return m;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("fail marshalFaultResponse e=" + e);
                log.debug(" " + JavaUtils.stackToString(e));
            }
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Throwable demarshalFaultResponse(Message message, OperationDescription operationDesc)
            throws WebServiceException {

        if (log.isDebugEnabled()) {
            log.debug("demarshalFaultResponse operationDesc = " + operationDesc);
        }
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        MarshalServiceRuntimeDescription marshalDesc =
                MethodMarshallerUtils.getMarshalDesc(endpointDesc);

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            Throwable t = MethodMarshallerUtils
                    .demarshalFaultResponse(operationDesc, marshalDesc, message);
            if (log.isDebugEnabled()) {
                log.debug("exit demarshalFaultResponse");
            }
            return t;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("fail demarshalFaultResponse e=" +e);
                log.debug(" " + JavaUtils.stackToString(e));
            }
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * @param opDesc
     * @return request wrapper qname
     */
    private static QName getRequestWrapperQName(OperationDescription opDesc) {

        QName qName = opDesc.getName();

        String localPart = opDesc.getRequestWrapperLocalName();
        String uri = opDesc.getRequestWrapperTargetNamespace();
        String prefix = "dlwmin";  // Prefer using an actual prefix


        qName = new QName(uri, localPart, prefix);
        return qName;
    }

    /**
     * @param opDesc
     * @return request wrapper qname
     */
    private static QName getResponseWrapperQName(OperationDescription opDesc) {

        QName qName = opDesc.getName();

        String localPart = opDesc.getResponseWrapperLocalName();
        String uri = opDesc.getResponseWrapperTargetNamespace();
        String prefix = "dlwmin";  // Prefer using an actual prefix


        qName = new QName(uri, localPart, prefix);
        return qName;
    }
    
    /**
     * Return the list of PDElements that is unmarshalled from the wire.
     * NOTE: This method is slower as the normal getPDElements method because it 
     * must cache the message in order to do QName matches.
     * This method is only necessary to support the cases where the incoming message
     * may have missing data items.  
     * Currently this is limited to the document/literal minimal case.
     * 
     * @param params ParameterDescription for this operation
     * @param message Message
     * @param packages set of packages needed to unmarshal objects for this operation
     * @param isInput indicates if input or output  params (input on server, output on client)
     * @param hasReturnInBody if isInput=false, then this parameter indicates whether a 
     * return value is expected in the body.
     * @param javaType 
     * @param javaComponentType
     * @param firstIndex (array indicating the first block corresponding to the parameter)
     * @param lastIndex  (array indicating the last block corresponding to the parameter)
     * @see getPDElements
     * @return ParamValues
     */
    static List<PDElement> getPDElementsForDocLitWrappedMinimal(ParameterDescription[] params,
                                         Message message,
                                         TreeSet<String> packages,
                                         boolean isInput,
                                         boolean hasReturnInBody, 
                                         Class[] javaType,
                                         Class[] javaComponentType,
                                         int[] firstIndex,
                                         int[] lastIndex
                                         ) throws XMLStreamException {

        if (log.isDebugEnabled()) {
            log.debug("start getPDElementsForDocLitWrappedMinimal");
        }
        List<PDElement> pdeList = new ArrayList<PDElement>();
        
        int totalBodyBlocks = message.getNumBodyBlocks();  
        
        
        
        // TODO What if return is an swa attachment, then this should start
        // at 1 not 0.
        int swaIndex = 0;
        
        for (int i = 0; i < params.length; i++) {
            ParameterDescription pd = params[i];
            
            if (log.isDebugEnabled()) {
                log.debug("  processing Parameter " + pd);
            }
            

            if (pd.getMode() == Mode.IN && isInput ||
                    pd.getMode() == Mode.INOUT ||
                    pd.getMode() == Mode.OUT && !isInput) {

                // Don't consider async handlers, they are are not represented on the wire,
                // thus they don't have a PDElement
                // TODO
                //if (isAsyncHandler(param)) {
                //    continue;
                //}

                Block block = null;
                JAXBBlockContext context = new JAXBBlockContext(packages);

                AttachmentDescription attachmentDesc = pd.getAttachmentDescription();
                if (attachmentDesc == null) {
                    
                    boolean isBase64Binary = byte[].class.equals(javaType[i]);
                    
                    
                    // In most cases the entire java object is unmarshalled.
                    // But in some cases, the java object is a series of
                    // elements.
                    boolean unmarshalComponents = false;
                    if (pd.isListType() || 
                        javaComponentType[i] == null ||
                        isBase64Binary) {
                        context.setProcessType(javaType[i]);
                        context.setIsxmlList(pd.isListType());
                    } else {
                        context.setProcessType(javaComponentType[i]);
                        unmarshalComponents = true;
                    }
                    
                    
                    // Unmarshal the object into a JAXB object or JAXBElement
                    Element element = null;
                    if (pd.isHeader()) {
                        
                        if (log.isDebugEnabled()) {
                            log.debug("  get block from the headers");
                        }
                        // Get the Block from the header
                        // NOTE The parameter name is always used to get the header 
                        // element
                        String localName = pd.getParameterName();
                        block = message.getHeaderBlock(pd.getTargetNamespace(),
                                                       localName,
                                                       context,
                                                       factory);
                        element = new Element(block.getBusinessObject(true), 
                                block.getQName());
                    } else if (firstIndex[i] >= 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("  get block from the " + firstIndex[i] +
                                    " to the " + lastIndex[i]);
                        }
                        // You must use this method if there are more than one body block
                        // This method may cause OM expansion
                        if (unmarshalComponents) {
                            Object container = makeContainer(javaType[i],
                                    javaComponentType[i],
                                    (lastIndex[i] - firstIndex[i]) + 1);

                            for (int blockI=firstIndex[i]; blockI<=lastIndex[i]; blockI++) {
                                block = message.getBodyBlock(blockI, context, factory);
                                Object value = block.getBusinessObject(true);
                                if (value instanceof JAXBElement) {
                                    value = ((JAXBElement) value).getValue();
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug(" get Business Object " + JavaUtils.getObjectIdentity(value) + " from block " + blockI);

                                }

                                addComponent(container, value, blockI - firstIndex[i]);
                                
                            }
                            element = new Element(container,     
                                    block.getQName());

                        } else {
                            block = message.getBodyBlock(firstIndex[i], context, factory);
                            element = new Element(block.getBusinessObject(true), 
                                    block.getQName());
                        }
                    }  else {
                        // Missing parameter
                        if (log.isDebugEnabled()) {
                            log.debug("  there is no block for this parameter.");
                        }
                        QName qName = new QName(pd.getTargetNamespace(), pd.getPartName());
                        if (!unmarshalComponents) {
                            element = new Element(null, qName);
                        } else {
                            Object container = makeContainer(javaType[i],
                                    javaComponentType[i], 0);
                            element = new Element(container, qName);
                        }
                    }
                    
                    
                    PDElement pde =
                        new PDElement(pd, element, javaComponentType[i] == null ? null
                                : javaComponentType[i]);
                    pdeList.add(pde);
                } else {
                    // Attachment Processing
                    if (attachmentDesc.getAttachmentType() == AttachmentType.SWA) {
                        String partName = pd.getPartName();
                        String cid = null;
                        if (log.isDebugEnabled()) {
                            log.debug("Getting the attachment dataHandler for partName=" + partName);
                        }
                        if (partName != null && partName.length() > 0) {
                            // Compliant WS-I behavior
                            cid = message.getAttachmentID(partName);
                        }
                        if (cid == null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Attachment dataHandler was not found.  Fallback to use attachment " + swaIndex);
                            }
                            // Toleration mode for non-compliant attachment names
                            cid = message.getAttachmentID(swaIndex);
                        }
                        DataHandler dh = message.getDataHandler(cid);
                        Attachment attachment = new Attachment(dh, cid);
                        PDElement pde = new PDElement(pd, null, null, attachment);
                        pdeList.add(pde);
                        swaIndex++;
                    } else {
                        throw ExceptionFactory.makeWebServiceException(Messages.getMessage("pdElementErr"));
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("end getPDElementsWithMissingElements");
        }
        return pdeList;
    }
    
    /**
     * Calculate the index of the first block for the parameter and
     * the last block (inclusive) for the parameter
     * @param firstIndex
     * @param lastIndex
     * @param params
     * @param qNames
     */
    private static void calculateBodyIndex(int[] firstIndex, int[] lastIndex, 
            ParameterDescription[] params, 
            List<QName> qNames) {
        if (log.isDebugEnabled()) {
            log.debug("Start calculateBodyIndex");
            for (int i=0; i<qNames.size(); i++) {
                log.debug("   QName " + i + " = " + qNames.get(i));
            }
        }
        for (int pi=0; pi<params.length; pi++) {
            if (pi >= 0) {
                ParameterDescription pd = params[pi];
                if (log.isDebugEnabled()) {
                    log.debug("  ParameterDescription =" + pd.toString());
                    log.debug("  original firstIndex = " + firstIndex[pi]);
                    log.debug("  original lastIndex = " + lastIndex[pi]);
                }
                firstIndex[pi] = -1;  // Reset index
                lastIndex[pi] = -1;  // Reset index
                // Found a parameter that is expected in the body
                // Calculate its index by looking for an exact qname match
                for (int qi=0; qi<qNames.size(); qi++) {
                    QName qName = qNames.get(qi);
                    if (qName.getLocalPart().equals(pd.getPartName())) {
                        if (qName.getNamespaceURI().equals(pd.getTargetNamespace())) {
                            if (firstIndex[pi] < 0) {
                                if(log.isDebugEnabled()) {
                                    log.debug("    set first index to " + qi);
                                }
                                firstIndex[pi] = qi;
                            }
                            lastIndex[pi] = qi;
                        }
                    }
                }
                // Fallback to searching for just the part name
                if (firstIndex[pi] < 0) {
                    for (int qi=0; qi<qNames.size(); qi++) {
                        QName qName = qNames.get(qi);
                        if (qName.getLocalPart().equals(pd.getPartName())) {
                            if (firstIndex[pi] < 0) {
                                if(log.isDebugEnabled()) {
                                    log.debug("    fallback: set first index to " + qi);
                                }
                                firstIndex[pi] = qi;
                            }
                            lastIndex[pi] = qi;
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("  last index = " + lastIndex[pi]);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("End calculateBodyIndex");
        }
    }

    /**
     * Unmarshal the return object from the message
     *
     * @param packages
     * @param message
     * @param javaTypeClass
     * @param javaComponentTypeClass
     * @param isHeader
     * @param headerNS                 (only needed if isHeader)
     * @param headerLocalPart          (only needed if isHeader)
     * @param hasOutputBodyParams (true if the method has out or inout params other 
     * than the return value)
     * @param ioutputBodyArgIndes (first index of output body argument or -1)
     * 
     * @return Element
     * @throws WebService
     * @throws XMLStreamException
     */
    static Element getReturnElementForDocLitWrappedMinimal(TreeSet<String> packages,
                                    Message message,
                                    Class javaTypeClass,  
                                    Class javaComponentTypeClass,  
                                    boolean isList,
                                    boolean isHeader,
                                    String headerNS,
                                    String headerLocalPart,
                                    boolean hasOutputBodyParams,
                                    int outputBodyArgIndex) 

            throws WebServiceException, XMLStreamException {

        // The return object is the first block in the body
        JAXBBlockContext context = new JAXBBlockContext(packages);
        
        // In most cases the entire java object is unmarshalled.
        // But in some cases, the java object is a series of
        // elements.
        boolean unmarshalComponents = false;
        if (isList || javaComponentTypeClass == null) {
            context.setProcessType(javaTypeClass);
            context.setIsxmlList(isList);
        } else {
            context.setProcessType(javaComponentTypeClass);
            unmarshalComponents = true;
        }
        
        Element returnElement = null;
        if (isHeader) {
            
            // In header
            Block block = message.getHeaderBlock(headerNS, headerLocalPart, context, factory);
            // Get the business object.  We want to return the object that represents the type.
            returnElement = new Element(block.getBusinessObject(true), block.getQName());
        } 
        else {
            // In Body
            
            // Determine how many return elements are present
            int numBodyBlocks = message.getNumBodyBlocks();
            if (outputBodyArgIndex >= 0) {
                numBodyBlocks = outputBodyArgIndex;
            }
            if (!unmarshalComponents) {
                if (numBodyBlocks == 1) {
                    if (log.isDebugEnabled()) {
                        log.debug("Case A: Not unmarshalling components, and only 1 block");
                    }
                    // Normal case: Only one body block
                    // We can use the get body block method
                    // that streams the whole block content.
                    Block block = message.getBodyBlock(context, factory);
                    //We look for body block only when the return type associated with operation is not void.
                    //If a null body block is returned in response on a operation that is not void, its a user error.               
                    returnElement = new Element(block.getBusinessObject(true), block.getQName());
                } else if (numBodyBlocks > 1) {
                    if (log.isDebugEnabled()) {
                        log.debug("Case B: Not unmarshalling components, and multiple blocks");
                    }
                    // There is only one return element
                    Block block = message.getBodyBlock(0, context, factory);
                    returnElement = new Element(block.getBusinessObject(true), block.getQName());

                }       
            } else {        
                if (numBodyBlocks > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Case C: unmarshalling components.  Number of blocks=" + numBodyBlocks);
                    }
                    // Assume that all the qnames matching the first QName are for the return
                    List<QName> qNames = message.getBodyBlockQNames();
                    int firstIndex = 0;
                    int lastIndex = 0;
                    QName returnQName = qNames.get(0);
                    if (log.isDebugEnabled()) {
                        log.debug(" returnQName =" + returnQName);
                    }
                    do {
                        lastIndex++;
                    } while (lastIndex < qNames.size() &&
                            returnQName.equals(qNames.get(lastIndex)));

                    // Multiple Elements for QName
                    int numElements = lastIndex - firstIndex;
                    if (log.isDebugEnabled()) {
                        log.debug(" number of return blocks=" + numElements);
                    }
                    Object container = makeContainer(javaTypeClass, 
                            javaComponentTypeClass, 
                            numElements);
                    
                    for (int blockI=firstIndex; blockI<lastIndex; blockI++) {
                        Block block = message.getBodyBlock(blockI, context, factory);
                        Object value = block.getBusinessObject(true);
                        if (value instanceof JAXBElement) {
                            value = ((JAXBElement) value).getValue();
                        }
                        if (log.isDebugEnabled()) {
                            log.debug(" get return Business object (" + JavaUtils.getObjectIdentity(value) +
                                    ") for block " + blockI);

                        }
                        
                        addComponent(container, value, blockI);
                        
                        
                    }
                    returnElement = new Element(container, returnQName);

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Case D: unmarshalling components, but there are no blocks");
                    }
                    Object container = makeContainer(javaTypeClass, 
                            javaComponentTypeClass, 
                            0);
                    QName qName = new QName("", "return");
                    returnElement = new Element(container, qName);
                }
            }
        
            //We look for body block only when the return type associated with operation is not void.
            //If a null body block is returned in response on a operation that has non void return type, its a user error.
            if (returnElement == null){
                if(log.isDebugEnabled()){
                    log.debug("Empty Body Block Found in response Message for wsdl Operation defintion that expects an Output");
                    log.debug("Return type associated with SEI operation is not void, Body Block cannot be null");
                }
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("MethodMarshallerUtilErr1"));    
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("getReturnElementForDocLitWrappedMinimal " + JavaUtils.getObjectIdentity(returnElement));
            
        }
        
        return returnElement;
    }
    
    /**
     * Make a container array or List
     * @param type
     * @param componentType
     * @param numComponents
     * @return container array or list
     */
    private static Object makeContainer(Class type, 
            Class componentType, 
            int numComponents) {
        Object container = null;
        if (type.isArray()) {
            container = Array.newInstance(componentType, 
                    numComponents);
        } else {
            try {
                container = type.newInstance();
            } catch (Exception e) {
                container = new ArrayList();
            }
        }
        return container;
    }
    
    /**
     * Add component to the container object
     * @param container array or List
     * @param component
     * @param index
     * @param container
     */
    private static void addComponent(Object container, Object component, int index) {
        if (container.getClass().isArray()) {
            if (component != null) {
                Array.set(container, index, component);
            }
        } else {
            ((List) container).add(component);
        }
    }
    
    /**
     * Return ComponentType, might need to look at the GenericType
     * @param pd ParameterDesc or null if return
     * @param operationDesc OperationDescription
     * @param msrd MarshalServiceRuntimeDescription
     * @return
     */
    private static Class getComponentType(ParameterDescription pd, 
            OperationDescription operationDesc,    
            MarshalServiceRuntimeDescription msrd) {
        Class componentType = null;
        if (log.isDebugEnabled()) {
            log.debug("start getComponentType");
            log.debug(" ParameterDescription=" + pd);
        }
        
        // Determine if array, list, or other
        Class cls = null;
        if (pd == null) {
            cls = operationDesc.getResultActualType();
        } else {
            cls = pd.getParameterActualType();
        }
        
        if (cls != null) {
            if (cls.isArray()) {
                componentType = cls.getComponentType();
            } else if (List.class.isAssignableFrom(cls)) {
                if (log.isDebugEnabled()) {
                    log.debug("Parameter is a List: " + cls);
                }
                Method method = msrd.getMethod(operationDesc);
                if (log.isDebugEnabled()) {
                    log.debug("Method is: " + method);
                }
                Type genericType = null;
                if (pd == null) {
                   genericType =method.getGenericReturnType();
                } else {
                    ParameterDescription[] pds = operationDesc.getParameterDescriptions();
                    for (int i=0; i< pds.length; i++) {
                        if (pds[i] == pd) {
                            genericType = method.getGenericParameterTypes()[i];
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("genericType is: " + genericType.getClass() + " "+ genericType);
                }
                if (genericType instanceof Class) {
                    if (log.isDebugEnabled()) {
                        log.debug(" genericType instanceof Class");
                    }
                    componentType =  String.class;
                } else if (genericType instanceof ParameterizedType) {
                    if (log.isDebugEnabled()) {
                        log.debug(" genericType instanceof ParameterizedType");
                    }
                    ParameterizedType pt = (ParameterizedType) genericType;
                    if (pt.getRawType() == Holder.class) {
                        if (log.isDebugEnabled()) {
                            log.debug(" strip off holder");
                        }
                        genericType = pt.getActualTypeArguments()[0];
                        if (genericType instanceof Class) {
                            componentType =  String.class;
                        } else if (genericType instanceof ParameterizedType) {
                            pt = (ParameterizedType) genericType;
                        }
                    }
                    if (componentType == null) {
                        Type comp = pt.getActualTypeArguments()[0];
                        if (log.isDebugEnabled()) {
                            log.debug(" comp =" + comp.getClass() + " " + comp);
                        }
                        if (comp instanceof Class) {
                            componentType = (Class) comp;
                        } else if (comp instanceof ParameterizedType) {
                            componentType = (Class) ((ParameterizedType) comp).getRawType();
                        }
                    }
                }
                    
            }
        }
        
        
        if (log.isDebugEnabled()) {
            log.debug("end getComponentType=" + componentType);
        }
        return componentType;
    }
}
