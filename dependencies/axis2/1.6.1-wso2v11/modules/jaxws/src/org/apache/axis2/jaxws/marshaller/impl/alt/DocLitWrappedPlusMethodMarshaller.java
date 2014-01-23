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
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.utility.ConvertUtils;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebParam.Mode;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * The DocLitWrapped "plus" marshaller is used when the java method is doc/lit wrapped, but it does
 * not exactly comply with the doc/lit rules.  This can result from the customer adding annotations,
 * but it can also be the result of WSGEN generation
 * <p/>
 * Here is an example:
 *
 * @WebMethod(action = "http://addheaders.sample.test.org/echoStringWSGEN1")
 * @RequestWrapper(localName = "echoStringWSGEN1", targetNamespace = "http://wrap.sample.test.org",
 * className = "org.test.sample.wrap.EchoStringWSGEN1")
 * @ResponseWrapper(localName = "echoStringWSGEN1Response", targetNamespace =
 * "http://wrap.sample.test.org", className = "org.test.sample.wrap.EchoStringWSGEN1Response")
 * public String echoStringWSGEN1(
 * @WebParam(name = "headerValue", targetNamespace = "http://wrap.sample.test.org", header = true)
 * String headerValue )
 * <p/>
 * In this case the method is doc/lit, but the headerValue parameter is passed in the header.  The
 * wrapper class EchoStringWSGEN1 will not have any child elements.
 * <p/>
 * A similar example is:
 * @WebMethod(action = "http://addheaders.sample.test.org/echoStringWSGEN2")
 * @RequestWrapper(localName = "echoStringWSGEN2", targetNamespace = "http://wrap.sample.test.org",
 * className = "org.test.sample.wrap.EchoStringWSGEN2")
 * @ResponseWrapper(localName = "echoStringWSGEN2Response", targetNamespace =
 * "http://wrap.sample.test.org", className = "org.test.sample.wrap.EchoStringWSGEN2Response")
 * @WebResult(name = "headerValue", targetNamespace = "http://wrap.sample.test.org", header = true)
 * public String echoStringWSGEN2(
 * @WebParam(name = "data", targetNamespace = "") String data )
 * <p/>
 * In this second case, the return is passed in a header (as defined by @WebResult)
 * <p/>
 * At the time of this writing, the "plus" marshaller is only used if a doc/lit wrapped method has
 * "header" parameters.  If other deviations are found, this class will be updated.  The advantage
 * of using DocLitWrapperPlus for these deviations is that it does not impact the normal
 * DocLitWrapper marshalling (which is used for 99% of the cases).  Thus these deviations will not
 * polute or slow down the normal flow.
 * <p/>
 * Scheu
 */
public class DocLitWrappedPlusMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(DocLitWrappedPlusMethodMarshaller.class);


    public DocLitWrappedPlusMethodMarshaller() {
        super();
    }

    public Object demarshalResponse(Message message, Object[] signatureArgs,
                                    OperationDescription operationDesc)
            throws WebServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Calling DocLitWrapperPlusMethodMarshaller.demarshalResponse");
            log.debug(
                    "  The DocLitWrapped Plus marshaller is used when the web service method deviates from the normal doc/lit rules.");
        }
        // Note all exceptions are caught and rethrown with a WebServiceException

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();

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
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();
            String packagesKey = marshalDesc.getPackagesKey();

            // Determine if a returnValue is expected.
            // The return value may be an child element
            // The wrapper element 
            // or null
            Object returnValue = null;
            Class returnType = operationDesc.getResultActualType();
            boolean isChildReturn = !operationDesc.isJAXWSAsyncClientMethod() &&
                    (operationDesc.getResultPartName() != null);
            boolean isNoReturn = (returnType == void.class);

            // In usage=WRAPPED, there will be a single JAXB block inside the body.
            // Get this block
            JAXBBlockContext blockContext = new JAXBBlockContext(packages, packagesKey);
            
            blockContext.setWebServiceNamespace(ed.getTargetNamespace());
            // If the wrapper is not a root element, then the process type
            // must be set on the context so that "by type" unmarshal is performed.
            if (!isResponseWrapperAnXmlRootElement(operationDesc, marshalDesc, endpointDesc)) {
            	String clsName = marshalDesc.getResponseWrapperClassName(operationDesc);
            	Class cls = loadClass(clsName,endpointDesc);
            	blockContext.setProcessType(cls);
            }
            JAXBBlockFactory factory =
                    (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            Block block = message.getBodyBlock(blockContext, factory);
            Object wrapperObject = block.getBusinessObject(true);

            // The child elements are within the object that 
            // represents the type
            if (wrapperObject instanceof JAXBElement) {
                wrapperObject = ((JAXBElement)wrapperObject).getValue();
            }

            // Use the wrapper tool to get the child objects.
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();

            // Get the list of names for the output parameters
            List<String> names = new ArrayList<String>();
            List<ParameterDescription> pdList = new ArrayList<ParameterDescription>();
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                if (pd.getMode() == Mode.OUT ||
                        pd.getMode() == Mode.INOUT) {
                    if (!pd.isHeader()) {
                        // Header names are not in the wrapped element
                        names.add(pd.getParameterName());
                    }
                    pdList.add(pd);
                }
            }


            if (!operationDesc.isResultHeader()) {
                // Normal case (Body Result) The return name is in the wrapped object
                if (isChildReturn && !isNoReturn) {
                    names.add(operationDesc.getResultPartName());
                }
            }

            // Get the child objects
            Object[] objects = wrapperTool.unWrap(wrapperObject, names,
                                                  marshalDesc.getPropertyDescriptorMap(
                                                          wrapperObject.getClass()));

            // Now create a list of paramValues so that we can populate the signature
            List<PDElement> pvList = new ArrayList<PDElement>();
            int bodyIndex = 0;
            for (int i = 0; i < pdList.size(); i++) {
                ParameterDescription pd = pdList.get(i);
                if (!pd.isHeader()) {
                    // Body elements are obtained from the unwrapped array of objects
                    Object value = objects[bodyIndex];
                    // The object in the PDElement must be an element
                    QName qName = new QName(pd.getTargetNamespace(),
                                            pd.getPartName());
                    Element element = null;
                    if (!marshalDesc.getAnnotationDesc(pd.getParameterActualType())
                            .hasXmlRootElement()) {
                        element = new Element(value, qName, pd.getParameterActualType());
                    } else {
                        element = new Element(value, qName);
                    }
                    pvList.add(new PDElement(pd, element, null));
                    bodyIndex++;
                } else {
                    // Header
                    // Get the Block from the header
                    String localName = pd.getParameterName();
                    JAXBBlockContext blkContext = new JAXBBlockContext(packages, packagesKey);

                    // Set up "by java type" unmarshalling if necessary
                    if (blkContext.getConstructionType() != JAXBUtils.CONSTRUCTION_TYPE
                            .BY_CONTEXT_PATH) {
                        Class actualType = pd.getParameterActualType();
                        if (MethodMarshallerUtils.isNotJAXBRootElement(actualType, marshalDesc)) {
                            blkContext.setProcessType(actualType);
                            blkContext.setIsxmlList(pd.isListType());
                        }
                    }
                    block = message.getHeaderBlock(pd.getTargetNamespace(), localName, blkContext,
                                                   factory);
                    Object value = block.getBusinessObject(true);
                    Element element =
                            new Element(value, new QName(pd.getTargetNamespace(), localName));
                    pvList.add(new PDElement(pd, element, blkContext.getProcessType()));
                }
            }

            // Populate the response Holders in the signature
            MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);

            // Now get the return value

            if (isNoReturn) {
                returnValue = null;
            } else if (isChildReturn) {
                if (!operationDesc.isResultHeader()) {
                    // Normal: Get the return from the jaxb object
                    returnValue = objects[objects.length - 1];
                } else {
                    // Header result: Get the value from the headers
                    Element returnElement =
                            MethodMarshallerUtils.getReturnElement(packages, message, null, false, true,
                                                                   operationDesc.getResultTargetNamespace(),
                                                                   operationDesc.getResultPartName(),
                                                                   false);
                    returnValue = returnElement.getTypeValue();
                }
                // returnValue may be incompatible with JAX-WS signature
                if (ConvertUtils.isConvertable(returnValue, returnType)) {
                    returnValue = ConvertUtils.convert(returnValue, returnType);
                } else {
                    String objectClass =
                            (returnValue == null) ? "null" : returnValue.getClass().getName();
                    throw ExceptionFactory.makeWebServiceException(
                            Messages.getMessage("convertProblem", objectClass,
                                                returnType.getName()));
                }
            } else {
                returnValue = wrapperObject;
            }

            return returnValue;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Object[] demarshalRequest(Message message, OperationDescription operationDesc)
            throws WebServiceException {

        if (log.isDebugEnabled()) {
            log.debug("Calling DocLitWrapperPlusMethodMarshaller.demarshalRequest");
            log.debug(
                    "  The DocLitWrapped Plus marshaller is used when the web service method deviates from the normal doc/lit rules.");
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
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();
            String packagesKey = marshalDesc.getPackagesKey();

            // In usage=WRAPPED, there will be a single JAXB block inside the body.
            // Get this block
            JAXBBlockContext blockContext = new JAXBBlockContext(packages, packagesKey);            
            blockContext.setWebServiceNamespace(ed.getTargetNamespace());
            
            // If the wrapper is not a root element, then the process type
            // must be set on the context so that "by type" unmarshal is performed.
            if (!isRequestWrapperAnXmlRootElement(operationDesc, marshalDesc, endpointDesc)) {
            	String clsName = marshalDesc.getRequestWrapperClassName(operationDesc);
            	Class cls = loadClass(clsName,endpointDesc);
            	blockContext.setProcessType(cls);
            }
            JAXBBlockFactory factory =
                    (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            Block block = message.getBodyBlock(blockContext, factory);
            Object wrapperObject = block.getBusinessObject(true);

            // The child elements are within the object that 
            // represents the type
            if (wrapperObject instanceof JAXBElement) {
                wrapperObject = ((JAXBElement)wrapperObject).getValue();
            }

            // Use the wrapper tool to get the child objects.
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();

            // Get the list of names for the input parameters
            List<String> xmlNames = new ArrayList<String>();
            List<ParameterDescription> pdList = new ArrayList<ParameterDescription>();
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                if (pd.getMode() == Mode.IN ||
                        pd.getMode() == Mode.INOUT) {
                    if (!pd.isHeader()) {
                        // Only the body parameters will be present in the wrapper object
                        xmlNames.add(pd.getParameterName());
                    }
                    pdList.add(pd);
                }

            }

            // Get the child objects
            Object[] objects = wrapperTool.unWrap(wrapperObject, xmlNames,
                                                  marshalDesc.getPropertyDescriptorMap(
                                                          wrapperObject.getClass()));

            // Now create a list of paramValues 
            List<PDElement> pvList = new ArrayList<PDElement>();
            int bodyIndex = 0;
            for (int i = 0; i < pdList.size(); i++) {
                ParameterDescription pd = pdList.get(i);
                if (!pd.isHeader()) {
                    // Normal case: Get the parameter value from the list of objects
                    Object value = objects[bodyIndex];

                    //  The object in the PDElement must be an element
                    QName qName = new QName(pd.getTargetNamespace(), pd.getPartName());
                    Element element = null;
                    if (!marshalDesc.getAnnotationDesc(pd.getParameterActualType())
                            .hasXmlRootElement()) {
                        element = new Element(value, qName, pd.getParameterActualType());
                    } else {
                        element = new Element(value, qName);
                    }
                    pvList.add(new PDElement(pd, element, null));
                    bodyIndex++;
                } else {
                    // Header
                    // Get the Block from the header
                    String localName = pd.getParameterName();
                    JAXBBlockContext blkContext =
                            new JAXBBlockContext(packages, marshalDesc.getPackagesKey());
                    // Set up "by java type" unmarshalling if necessary
                    if (blkContext.getConstructionType() != JAXBUtils.CONSTRUCTION_TYPE
                            .BY_CONTEXT_PATH) {
                        Class actualType = pd.getParameterActualType();
                        if (MethodMarshallerUtils.isNotJAXBRootElement(actualType, marshalDesc)) {
                            blkContext.setProcessType(actualType);
                        } else {
                            Annotation annos[] = actualType.getAnnotations();
                            if (annos == null || annos.length == 0) {
                                blkContext.setProcessType(actualType);
                                blkContext.setIsxmlList(pd.isListType());
                            }
                        }
                    }
                    block = message.getHeaderBlock(pd.getTargetNamespace(), localName, blkContext,
                                                   factory);
                    Object value = block.getBusinessObject(true);
                    Element element =
                            new Element(value, new QName(pd.getTargetNamespace(), localName));
                    pvList.add(new PDElement(pd, element, blkContext.getProcessType()));
                }

            }

            // Build the signature arguments
            Object[] sigArguments = MethodMarshallerUtils.createRequestSignatureArgs(pds, pvList);

            return sigArguments;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalResponse(Object returnObject, Object[] signatureArgs,
                                   OperationDescription operationDesc, Protocol protocol)
            throws WebServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Calling DocLitWrapperPlusMethodMarshaller.marshalResponse");
            log.debug(
                    "  The DocLitWrapped Plus marshaller is used when the web service method deviates from the normal doc/lit rules.");
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

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();
            String packagesKey = marshalDesc.getPackagesKey();

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // In usage=WRAPPED, there will be a single block in the body.
            // The signatureArguments represent the child elements of that block
            // The first step is to convert the signature arguments into a list
            // of parameter values
            List<PDElement> pdeList =
                    MethodMarshallerUtils.getPDElements(marshalDesc,
                                                        pds,
                                                        signatureArgs,
                                                        false,  // output
                                                        true, false);

            // Now we want to create a single JAXB element that contains the 
            // parameter values.  We will use the wrapper tool to do this.
            // Create the inputs to the wrapper tool
            ArrayList<String> nameList = new ArrayList<String>();
            Map<String, Object> objectList = new HashMap<String, Object>();
            Map<String, Class> declaredClassMap = new HashMap<String, Class>();
            List<PDElement> headerPDEList = new ArrayList<PDElement>();

            Iterator<PDElement> it = pdeList.iterator();
            while (it.hasNext()) {
                PDElement pde = it.next();
                String name = pde.getParam().getParameterName();
                if (!pde.getParam().isHeader()) {
                    // Normal case
                    // The object list contains type rendered objects
                    Object value = pde.getElement().getTypeValue();
                    Class dclClass = pde.getParam().getParameterActualType();
                    nameList.add(name);
                    objectList.put(name, value);
                    declaredClassMap.put(name, dclClass);
                } else {
                    // Header Case:
                    // Remove the header from the list, it will
                    // not be placed in the wrapper
                    it.remove();
                    headerPDEList.add(pde);
                }
            }

            Class returnType = operationDesc.getResultActualType();
            if (!operationDesc.isResultHeader()) {
                // Normal (Body Result): Add the return object to the nameList and objectList

                if (returnType != void.class) {
                    String name = operationDesc.getResultName();
                    Class dclClass = operationDesc.getResultActualType();
                    nameList.add(name);
                    objectList.put(name, returnObject);
                    declaredClassMap.put(name, dclClass);
                }
            } else {
                // Header Result:
                // Put the return object onto the message
                if (returnType != void.class) {
                    Element returnElement = null;
                    QName returnQName = new QName(operationDesc.getResultTargetNamespace(),
                                                  operationDesc.getResultName());
                    if (marshalDesc.getAnnotationDesc(returnType).hasXmlRootElement()) {
                        returnElement = new Element(returnObject, returnQName);
                    } else {
                        returnElement = new Element(returnObject, returnQName, returnType);
                    }

                    Class byJavaType =
                            MethodMarshallerUtils.isNotJAXBRootElement(returnType, marshalDesc) ? returnType : null;

                    MethodMarshallerUtils.toMessage(returnElement, 
                    								returnType,
                    								operationDesc.isListType(),
                                                    marshalDesc,
                                                    m,
                                                    byJavaType,
                                                    true);
                }
            }

            // Now create the single JAXB element
            String wrapperName = marshalDesc.getResponseWrapperClassName(operationDesc);
            Class cls = loadClass(wrapperName, endpointDesc);
            
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            Object object = wrapperTool.wrap(cls, nameList, objectList, declaredClassMap,
                                             marshalDesc.getPropertyDescriptorMap(cls));

            QName wrapperQName = new QName(operationDesc.getResponseWrapperTargetNamespace(),
                                           operationDesc.getResponseWrapperLocalName());

            // Make sure object can be rendered as an element
            if (!marshalDesc.getAnnotationDesc(cls).hasXmlRootElement()) {
                object = new JAXBElement(wrapperQName, cls, object);
            }

            // Put the object into the message
            JAXBBlockFactory factory =
                    (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            JAXBBlockContext blockContext = new JAXBBlockContext(packages, packagesKey);
            blockContext.setWebServiceNamespace(ed.getTargetNamespace());
            Block block = factory.createFrom(object,
                                             blockContext, 
                                             wrapperQName);  // The factory will get the qname from the value
            m.setBodyBlock(block);

            //  Now place the headers in the message
            if (headerPDEList.size() > 0) {

                // Use "by java type" marshalling if necessary
                for (PDElement pde : headerPDEList) {
                    Class actualType = pde.getParam().getParameterActualType();
                    if (MethodMarshallerUtils.isNotJAXBRootElement(actualType, marshalDesc)) {
                        pde.setByJavaTypeClass(actualType);
                    }
                }
                MethodMarshallerUtils.toMessage(headerPDEList, m, packages, null);
            }
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasResponseSwaRefAttachments()) {
                m.setDoingSWA(true);
            }
            
            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalRequest(Object[] signatureArguments, 
            OperationDescription operationDesc,
            Map<String, Object> requestContext)
        throws WebServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Calling DocLitWrapperPlusMethodMarshaller.marshalRequest");
            log.debug(
                    "  The DocLitWrapped Plus marshaller is used when the web service method deviates from the normal doc/lit rules.");
        }
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        Protocol protocol = Protocol.getProtocolForBinding(endpointDesc.getClientBindingID());
        MarshalServiceRuntimeDescription marshalDesc =
                MethodMarshallerUtils.getMarshalDesc(endpointDesc);
        TreeSet<String> packages = marshalDesc.getPackages();
        String packagesKey = marshalDesc.getPackagesKey();

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

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // In usage=WRAPPED, there will be a single block in the body.
            // The signatureArguments represent the child elements of that block
            // The first step is to convert the signature arguments into list
            // of parameter values
            List<PDElement> pdeList = MethodMarshallerUtils.getPDElements(marshalDesc,
                                                                          pds,
                                                                          signatureArguments,
                                                                          true,   // input
                                                                          true, false);

            // Now we want to create a single JAXB element that contains the 
            // ParameterValues.  We will use the wrapper tool to do this.
            // Create the inputs to the wrapper tool
            ArrayList<String> nameList = new ArrayList<String>();
            Map<String, Object> objectList = new HashMap<String, Object>();
            Map<String, Class> declardClassMap = new HashMap<String, Class>();
            List<PDElement> headerPDEList = new ArrayList<PDElement>();

            Iterator<PDElement> it = pdeList.iterator();
            while (it.hasNext()) {
                PDElement pde = it.next();
                String name = pde.getParam().getParameterName();
                if (!pde.getParam().isHeader()) {
                    // Normal case:
                    // The object list contains type rendered objects
                    Object value = pde.getElement().getTypeValue();
                    Class dclClass = pde.getParam().getParameterActualType();
                    nameList.add(name);
                    objectList.put(name, value);
                    declardClassMap.put(name, dclClass);
                } else {
                    // Header Case:
                    // Remove the header from the list, it will
                    // not be placed in the wrapper
                    it.remove();
                    headerPDEList.add(pde);
                }
            }

            // Now create the single JAXB element 
            String wrapperName = marshalDesc.getRequestWrapperClassName(operationDesc);
            Class cls = loadClass(wrapperName, endpointDesc);
            
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            Object object = wrapperTool.wrap(cls, nameList, objectList, declardClassMap,
                                             marshalDesc.getPropertyDescriptorMap(cls));

            QName wrapperQName = new QName(operationDesc.getRequestWrapperTargetNamespace(),
                                           operationDesc.getRequestWrapperLocalName());

            // Make sure object can be rendered as an element
            if (!marshalDesc.getAnnotationDesc(cls).hasXmlRootElement()) {
                object = new JAXBElement(wrapperQName, cls, object);
            }

            // Put the object into the message
            JAXBBlockFactory factory =
                    (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            JAXBBlockContext blockContext = new JAXBBlockContext(packages, packagesKey);
            blockContext.setWebServiceNamespace(ed.getTargetNamespace());
            Block block = factory.createFrom(object,
                                             blockContext,
                                             wrapperQName);  // The factory will get the qname from the value
            m.setBodyBlock(block);

            // Now place the headers in the message
            if (headerPDEList.size() > 0) {

                // Use "by java type" marshalling if necessary
                for (PDElement pde : headerPDEList) {
                    Class actualType = pde.getParam().getParameterActualType();
                    if (MethodMarshallerUtils.isNotJAXBRootElement(actualType, marshalDesc)) {
                        pde.setByJavaTypeClass(actualType);
                    }
                }

                MethodMarshallerUtils.toMessage(headerPDEList, m, packages, requestContext);
            }
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasRequestSwaRefAttachments()) {
                m.setDoingSWA(true);
            }
            
            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalFaultResponse(Throwable throwable, OperationDescription operationDesc,
                                        Protocol protocol) throws WebServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Calling DocLitWrapperPlusMethodMarshaller.marshalFaultResponse");
            log.debug(
                    "  The DocLitWrapped Plus marshaller is used when the web service method deviates from the normal doc/lit rules.");
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
            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Throwable demarshalFaultResponse(Message message, OperationDescription operationDesc)
            throws WebServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Calling DocLitWrapperPlusMethodMarshaller.demarshalFaultResponse");
            log.debug(
                    "  The DocLitWrapped Plus marshaller is used when the web service method deviates from the normal doc/lit rules.");
        }
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        MarshalServiceRuntimeDescription marshalDesc =
                MethodMarshallerUtils.getMarshalDesc(endpointDesc);

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            Throwable t = MethodMarshallerUtils.demarshalFaultResponse(operationDesc,
                                                                       marshalDesc,
                                                                       message);
            return t;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * @param opDesc
     * @param msrd
     * @param endpointDesc
     * @return true if request wrapper is a root element
     */
    boolean isRequestWrapperAnXmlRootElement(OperationDescription opDesc, 
    			MarshalServiceRuntimeDescription msrd,
    			EndpointDescription endpointDesc) {
    	boolean isRootElement = false;
		String wrapperClassName = msrd.getRequestWrapperClassName(opDesc);
    	try {
			
			if (wrapperClassName != null) {
				AnnotationDesc aDesc = msrd.getAnnotationDesc(wrapperClassName);
				if (aDesc == null) {
					Class cls = loadClass(wrapperClassName, endpointDesc);
					aDesc = msrd.getAnnotationDesc(cls);
				}
				isRootElement = aDesc.hasXmlRootElement();
			}
			
		} catch (Throwable t) {
			if (log.isDebugEnabled()) {
				log.debug("An error occurred while processing class " + wrapperClassName + " exception is " + t);
				log.debug("The error is ignored and processing continues.");				
			}
		}
		return isRootElement;
    }
    
    /**
     * @param opDesc
     * @param msrd
     * @param endpointDesc
     * @return true if response wrapper is a root element
     */
    boolean isResponseWrapperAnXmlRootElement(OperationDescription opDesc, 
    			MarshalServiceRuntimeDescription msrd,
    			EndpointDescription endpointDesc) {
    	boolean isRootElement = false;
		String wrapperClassName = msrd.getResponseWrapperClassName(opDesc);
    	try {
			
			if (wrapperClassName != null) {
				AnnotationDesc aDesc = msrd.getAnnotationDesc(wrapperClassName);
				if (aDesc == null) {
					Class cls = loadClass(wrapperClassName, endpointDesc);
					aDesc = msrd.getAnnotationDesc(cls);
				}
				isRootElement = aDesc.hasXmlRootElement();
			}
			
		} catch (Throwable t) {
			if (log.isDebugEnabled()) {
				log.debug("An error occurred while processing class " + wrapperClassName + " exception is " + t);
				log.debug("The error is ignored and processing continues.");				
			}
		}
		return isRootElement;
    }

    /**
     * @param clsName
     * @param endpontDesc
     * @return
     * @throws ClassNotFoundException
     */
    Class loadClass(String clsName, EndpointDescription endpointDesc) throws ClassNotFoundException {
    	Class cls = null;
    	try {
            cls = MethodMarshallerUtils.loadClass(clsName);
        } catch (ClassNotFoundException e){
            cls = MethodMarshallerUtils.loadClass(clsName, endpointDesc.getAxisService().getClassLoader());
        }
        return cls;
    }
}
