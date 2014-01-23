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
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.utility.ConvertUtils;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebParam.Mode;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class DocLitWrappedMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(DocLitWrappedMethodMarshaller.class);


    public DocLitWrappedMethodMarshaller() {
        super();
    }

    public Object demarshalResponse(Message message, Object[] signatureArgs,
                                    OperationDescription operationDesc)
            throws WebServiceException {
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
            
            // Remember this unmarshal information so that we can speed up processing
            // the next time.
            MessageContext mc = message.getMessageContext();
            if (MethodMarshallerUtils.getUnmarshalInfoParameter(mc) == null &&
                shouldRegiserUnmarshalInfo(operationDesc, marshalDesc, endpointDesc)) {
            	MethodMarshallerUtils.registerUnmarshalInfo(message.getMessageContext(),
                                                        packages,
                                                        packagesKey);
            }

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


            // Get the list of names for the output parameters
            List<String> names = new ArrayList<String>();
            List<ParameterDescription> pdList = new ArrayList<ParameterDescription>();
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                if (pd.getMode() == Mode.OUT ||
                        pd.getMode() == Mode.INOUT) {
                    names.add(pd.getParameterName());
                    pdList.add(pd);
                }
            }

            if (pdList.size() == 0) {
                // No OUT or INOUT parameters
                // Use return only shortcut
                if (isNoReturn) {
                    returnValue = null;
                } else if (isChildReturn) {
                    String returnName = operationDesc.getResultPartName();
                    // Use the wrapper tool to get the child objects.
                    JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
                    Object object = wrapperTool.unWrap(wrapperObject, 
                            returnName,
                            marshalDesc.getPropertyDescriptorMap(
                                    wrapperObject.getClass()).get(returnName));
                    returnValue = object;
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
            } else {
                // There are one or more OUT or INOUT parameters
                // The return name is added as the last name
                if (isChildReturn && !isNoReturn) {
                    names.add(operationDesc.getResultPartName());
                }

                // Use the wrapper tool to get the child objects.
                JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
                
                // Get the child objects
                Object[] objects = wrapperTool.unWrap(wrapperObject, names,
                        marshalDesc.getPropertyDescriptorMap(
                                wrapperObject.getClass()));

                // Now create a list of paramValues so that we can populate the signature
                List<PDElement> pvList = new ArrayList<PDElement>();
                for (int i = 0; i < pdList.size(); i++) {
                    ParameterDescription pd = pdList.get(i);
                    Object value = objects[i];
                    // The object in the PDElement must be an element
                    Element element = null;
                    QName qName = new QName(pd.getTargetNamespace(), pd.getPartName());
                    if (!marshalDesc.getAnnotationDesc(pd.getParameterActualType()).hasXmlRootElement())
                    {
                        element = new Element(value, qName,
                                pd.getParameterActualType());

                    } else {
                        element = new Element(value, qName);
                    }
                    pvList.add(new PDElement(pd, element, null));
                }

                // Populate the response Holders in the signature
                MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);
                
                // Now get the return value
                if (isNoReturn) {
                    returnValue = null;
                } else if (isChildReturn) {
                    returnValue = objects[objects.length - 1];
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
            }

            

            return returnValue;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Object[] demarshalRequest(Message message, OperationDescription operationDesc)
            throws WebServiceException {

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
            
            MessageContext mc = message.getMessageContext();
            if (MethodMarshallerUtils.getUnmarshalInfoParameter(mc) == null &&
                shouldRegiserUnmarshalInfo(operationDesc, marshalDesc, endpointDesc)) {
                MethodMarshallerUtils.registerUnmarshalInfo(message.getMessageContext(),
                                                        packages,
                                                        packagesKey);
            }

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
            List<String> names = new ArrayList<String>();
            List<ParameterDescription> pdList = new ArrayList<ParameterDescription>();
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                if (pd.getMode() == Mode.IN ||
                        pd.getMode() == Mode.INOUT) {
                    names.add(pd.getParameterName());
                    pdList.add(pd);
                }

            }

            // Get the child objects
            Object[] objects = wrapperTool.unWrap(wrapperObject, names,
                                                  marshalDesc.getPropertyDescriptorMap(
                                                          wrapperObject.getClass()));

            // Now create a list of paramValues 
            List<PDElement> pvList = new ArrayList<PDElement>();
            for (int i = 0; i < pdList.size(); i++) {
                ParameterDescription pd = pdList.get(i);
                Object value = objects[i];
                // The object in the PDElement must be an element
                Element element = null;
                QName qName = new QName(pd.getTargetNamespace(),
                                        pd.getPartName());
                if (!marshalDesc.getAnnotationDesc(pd.getParameterActualType()).hasXmlRootElement())
                {
                    element = new Element(value, qName, pd.getParameterActualType());
                } else {
                    element = new Element(value, qName);
                }
                pvList.add(new PDElement(pd, element, null));
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

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        MarshalServiceRuntimeDescription marshalDesc =
                MethodMarshallerUtils.getMarshalDesc(endpointDesc);
        TreeSet<String> packages = marshalDesc.getPackages();
        String packagesKey = marshalDesc.getPackagesKey();

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

            // Create the message 
            MessageFactory mf = marshalDesc.getMessageFactory();
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
            
            String wrapperName = marshalDesc.getResponseWrapperClassName(operationDesc);
            Class cls = loadClass(wrapperName, endpointDesc);
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            Object object = null;
            
            // Add the return object to the nameList and objectList
            Class returnType = operationDesc.getResultActualType();
            
            // Now we want to create a single JAXB element that contains the 
            // ParameterValues.  We will use the wrapper tool to do this.
            // Create the inputs to the wrapper tool
            if (pdeList.size() == 0) {
                if (returnType == void.class) {
                    // Use the short-cut for void return
                    object = wrapperTool.wrap(cls, 
                            (String) null, 
                            null, 
                            null, 
                            null);
                } else {
                    // Use the short-cut for a single return
                    String childName = operationDesc.getResultName();
                    object = wrapperTool.wrap(cls, 
                            childName, 
                            returnObject, 
                            returnType,
                            marshalDesc.getPropertyDescriptorMap(cls).get(childName));
                }
            } else {           

                // Now we want to create a single JAXB element that contains the 
                // ParameterValues.  We will use the wrapper tool to do this.
                // Create the inputs to the wrapper tool
                ArrayList<String> nameList = new ArrayList<String>();
                Map<String, Object> objectList = new HashMap<String, Object>();
                Map<String, Class>  declaredClassMap = new HashMap<String, Class>();

                for (PDElement pde : pdeList) {
                    String name = pde.getParam().getParameterName();

                    // The object list contains type rendered objects
                    Object value = pde.getElement().getTypeValue();
                    Class dclClass = pde.getParam().getParameterActualType();

                    nameList.add(name);
                    objectList.put(name, value);
                    declaredClassMap.put(name, dclClass);
                }

                // Add the return type
                if (returnType != void.class) {
                    String name = operationDesc.getResultName();
                    nameList.add(name);
                    objectList.put(name, returnObject);
                    declaredClassMap.put(name, returnType);
                }

                
                object = wrapperTool.wrap(cls, nameList, objectList, declaredClassMap,
                        marshalDesc.getPropertyDescriptorMap(cls));
            }

            QName wrapperQName = new QName(operationDesc.getResponseWrapperTargetNamespace(),
                                           operationDesc.getResponseWrapperLocalName());

            // Make sure object can be rendered as an element
            if (!marshalDesc.getAnnotationDesc(cls).hasXmlRootElement()) {
                object = new JAXBElement(wrapperQName, cls, object);
            }
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasResponseSwaRefAttachments()) {
                m.setDoingSWA(true);
            }

            // Put the object into the message
            JAXBBlockFactory factory =
                    (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            JAXBBlockContext blockContext = new JAXBBlockContext(packages, packagesKey);
            blockContext.setWebServiceNamespace(ed.getTargetNamespace());
            Block block = factory.createFrom(object,
                                             blockContext,
                                             wrapperQName);
            m.setBodyBlock(block);

            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalRequest(Object[] signatureArguments, 
            OperationDescription operationDesc,
            Map<String, Object> requestContext)
        throws WebServiceException {

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
            MessageFactory mf = marshalDesc.getMessageFactory();
            Message m = mf.create(protocol);

            // In usage=WRAPPED, there will be a single block in the body.
            // The signatureArguments represent the child elements of that block
            // The first step is to convert the signature arguments into list
            // of parameter values
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(marshalDesc,
                                                                         pds,
                                                                         signatureArguments,
                                                                         true,   // input
                                                                         true, false);

            String wrapperName = marshalDesc.getRequestWrapperClassName(operationDesc);
            Class cls = loadClass(wrapperName, endpointDesc);
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            Object object = null;
            
            // Now we want to create a single JAXB element that contains the 
            // ParameterValues.  We will use the wrapper tool to do this.
            // Create the inputs to the wrapper tool
            if (pvList.size() ==  0) {
                // Use the short-cut for 0 children
                object = wrapperTool.wrap(cls, 
                        (String) null, 
                        null, 
                        null, 
                        null);
            } else if (pvList.size() == 1) {
                // Use the short-cut for 1 child
                PDElement pde = pvList.get(0);
                String childName = pde.getParam().getParameterName();
                object = wrapperTool.wrap(cls, 
                        childName, 
                        pde.getElement().getTypeValue(), 
                        pde.getParam().getParameterActualType(),
                        marshalDesc.getPropertyDescriptorMap(cls).get(childName));

            } else {           
                ArrayList<String> nameList = new ArrayList<String>();
                Map<String, Object> objectList = new HashMap<String, Object>();
                Map<String, Class> declaredClassMap = new HashMap<String, Class>();

                for (PDElement pv : pvList) {
                    String name = pv.getParam().getParameterName();

                    // The object list contains type rendered objects
                    Object value = pv.getElement().getTypeValue();
                    Class dclClass = pv.getParam().getParameterActualType();
                    nameList.add(name);
                    objectList.put(name, value);
                    declaredClassMap.put(name, dclClass);
                }

                
                object = wrapperTool.wrap(cls, nameList, objectList, declaredClassMap, 
                        marshalDesc.getPropertyDescriptorMap(cls));
            }

            QName wrapperQName = new QName(operationDesc.getRequestWrapperTargetNamespace(),
                                           operationDesc.getRequestWrapperLocalName());

            // Make sure object can be rendered as an element
            if (!marshalDesc.getAnnotationDesc(cls).hasXmlRootElement()) {
                object = new JAXBElement(wrapperQName, cls, object);
            }
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasRequestSwaRefAttachments()) {
                m.setDoingSWA(true);
            }

            // Put the object into the message
            JAXBBlockFactory factory =
                    (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            JAXBBlockContext blockContext = new JAXBBlockContext(packages, packagesKey);
            blockContext.setWebServiceNamespace(ed.getTargetNamespace());
            Block block = factory.createFrom(object,
                                             blockContext, 
                                             wrapperQName);
            m.setBodyBlock(block);

            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalFaultResponse(Throwable throwable, OperationDescription operationDesc,
                                        Protocol protocol) throws WebServiceException {

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
     * @return true if should register UnmarshalInfo for faster processing
     */
    boolean shouldRegiserUnmarshalInfo(OperationDescription opDesc, 
			MarshalServiceRuntimeDescription msrd,
			EndpointDescription endpointDesc) {
    	// If either the request wrapper or the response wrapper is 
    	// not a rendered as a root element, then "by type" unmarshaling
    	// is needed.  In such cases faster unmarshaling should be disabled.
    	return isRequestWrapperAnXmlRootElement(opDesc, msrd, endpointDesc) &&
    		isResponseWrapperAnXmlRootElement(opDesc, msrd, endpointDesc);
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
