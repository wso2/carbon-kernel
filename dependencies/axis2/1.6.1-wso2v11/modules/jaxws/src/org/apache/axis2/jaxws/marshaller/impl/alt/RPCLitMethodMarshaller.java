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
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.utility.ConvertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class RPCLitMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(RPCLitMethodMarshaller.class);

    public RPCLitMethodMarshaller() {
        super();
    }

    public Message marshalRequest(Object[] signatureArguments, 
            OperationDescription operationDesc,
            Map<String, Object> requestContext)
        throws WebServiceException {

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        Protocol protocol = Protocol.getProtocolForBinding(endpointDesc.getClientBindingID());

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {

            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:op xmlns:m="urn://api">
            //       <param xsi:type="data:foo" >...</param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (param) are defined by the wsdl:part not the
            //      schema.  Note that the param is unqualified...per WS-I BP.
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  Since we are using JAXB to marshal the data, 
            //      we always generate an xsi:type attribute.  This is an implemenation detail
            //      and is not defined by any spec.

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // TODO This needs more work.  We need to check inside holders of input params.  We also
            // may want to exclude header params from this check
            //Validate input parameters for operation and make sure no input parameters are null.
            //As per JAXWS Specification section 3.6.2.3 if a null value is passes as an argument 
            //to a method then an implementation MUST throw WebServiceException.
            if (pds.length > 0) {
                if (signatureArguments == null) {
                    throw ExceptionFactory.makeWebServiceException(
                    		Messages.getMessage("NullParamErr2",operationDesc.getJavaMethodName()));
                }
                if (signatureArguments != null) {
                    for (Object argument : signatureArguments) {
                        if (argument == null) {
                        	throw ExceptionFactory.makeWebServiceException(
                            		Messages.getMessage("NullParamErr2",operationDesc.getJavaMethodName()));
                        }
                    }
                }
            }

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Indicate the style and operation element name.  This triggers the message to
            // put the data blocks underneath the operation element
            m.setStyle(Style.RPC);
            m.setOperationElement(getRPCOperationQName(operationDesc, true));

            // The input object represent the signature arguments.
            // Signature arguments are both holders and non-holders
            // Convert the signature into a list of JAXB objects for marshalling
            List<PDElement> pdeList =
                    MethodMarshallerUtils.getPDElements(marshalDesc,
                                                        pds,
                                                        signatureArguments,
                                                        true,  // input
                                                        false,
                                                        true); // use partName since this is rpc/lit

            // We want to use "by Java Type" marshalling for 
            // all body elements and all non-JAXB objects
            for (PDElement pde : pdeList) {
                ParameterDescription pd = pde.getParam();
                Class type = pd.getParameterActualType();
                if (!pd.isHeader() ||
                        MethodMarshallerUtils.isNotJAXBRootElement(type, marshalDesc)) {
                    pde.setByJavaTypeClass(type);
                }
            }

            // Put values onto the message
            MethodMarshallerUtils.toMessage(pdeList, m, packages, requestContext);
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasRequestSwaRefAttachments()) {
                m.setDoingSWA(true);
            }

            return m;
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
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:op xmlns:m="urn://api">
            //       <param xsi:type="data:foo" >...</param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (param) are defined by the wsdl:part not the
            //      schema.  Note that it is unqualified per WSI-BP
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  
            //   5) We always send an xsi:type, but other vendor's may not.
            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Indicate that the style is RPC.  This is important so that the message understands
            // that the data blocks are underneath the operation element
            message.setStyle(Style.RPC);

            // We want to use "by Java Type" unmarshalling for 
            // all body elements and all non-JAXB objects
            Class[] javaTypes = new Class[pds.length];
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                Class type = pd.getParameterActualType();
                if (!pd.isHeader() ||
                        MethodMarshallerUtils.isNotJAXBRootElement(type, marshalDesc)) {
                    javaTypes[i] = type;
                }
            }

            // Unmarshal the ParamValues from the Message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds,
                                                                         message,
                                                                         packages,
                                                                         true, // input
                                                                         false,
                                                                         javaTypes); // unmarshal by type

            // Build the signature arguments
            Object[] sigArguments = MethodMarshallerUtils.createRequestSignatureArgs(pds, pvList);

            // TODO This needs more work.  We need to check inside holders of input params.  We also
            // may want to exclude header params from this check
            //Validate input parameters for operation and make sure no input parameters are null.
            //As per JAXWS Specification section 3.6.2.3 if a null value is passes as an argument 
            //to a method then an implementation MUST throw WebServiceException.
            if (sigArguments != null) {
                for (Object argument : sigArguments) {
                    if (argument == null) {
                    	throw ExceptionFactory.makeWebServiceException(
                        		Messages.getMessage("NullParamErr2",operationDesc.getJavaMethodName()));

                    }
                }
            }
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
        // We want to respond with the same protocol as the request,
        // It the protocol is null, then use the Protocol defined by the binding
        if (protocol == null) {
            protocol = Protocol.getProtocolForBinding(endpointDesc.getBindingType());
        }

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:opResponse xmlns:m="urn://api">
            //       <param xsi:type="data:foo" >...</param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (param) are defined by the wsdl:part not the
            //      schema.  Note that it is unqualified.
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  Since we are using JAXB to marshal the data, 
            //      we always generate an xsi:type attribute.  This is an implemenation detail
            //      and is not defined by any spec.

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Indicate the style and operation element name.  This triggers the message to
            // put the data blocks underneath the operation element
            m.setStyle(Style.RPC);


            QName rpcOpQName = getRPCOperationQName(operationDesc, false);
            String localPart = rpcOpQName.getLocalPart() + "Response";
            QName responseOp =
                    new QName(rpcOpQName.getNamespaceURI(), localPart, rpcOpQName.getPrefix());
            m.setOperationElement(responseOp);

            // Put the return object onto the message
            Class returnType = operationDesc.getResultActualType();
            String returnNS = null;
            String returnLocalPart = null;
            if (operationDesc.isResultHeader()) {
                returnNS = operationDesc.getResultTargetNamespace();
                returnLocalPart = operationDesc.getResultName();
            } else {
                returnNS = "";  // According to WSI BP the body part is unqualified
                returnLocalPart = operationDesc.getResultPartName();
            }

            if (returnType != void.class) {

                AttachmentDescription attachmentDesc = 
                    operationDesc.getResultAttachmentDescription();
                if (attachmentDesc != null) {
                    if (attachmentDesc.getAttachmentType() == AttachmentType.SWA) {
                        // Create an Attachment object with the signature value
                        Attachment attachment = new Attachment(returnObject, 
                                                               returnType, 
                                                               attachmentDesc,
                                                               operationDesc.getResultPartName());
                        m.addDataHandler(attachment.getDataHandler(), 
                                         attachment.getContentID());
                        m.setDoingSWA(true);
                    } else {
                        throw ExceptionFactory.
                          makeWebServiceException(Messages.getMessage("pdElementErr"));
                    }
                } else {
                    // TODO should we allow null if the return is a header?
                    //Validate input parameters for operation and make sure no input parameters are null.
                    //As per JAXWS Specification section 3.6.2.3 if a null value is passes as an argument 
                    //to a method then an implementation MUST throw WebServiceException.
                    if (returnObject == null) {
                        throw ExceptionFactory.makeWebServiceException(
                                                                       Messages.getMessage("NullParamErr3",operationDesc.getJavaMethodName()));

                    }
                    Element returnElement = null;
                    QName returnQName = new QName(returnNS, returnLocalPart);
                    if (marshalDesc.getAnnotationDesc(returnType).hasXmlRootElement()) {
                        returnElement = new Element(returnObject, returnQName);
                    } else {
                        returnElement = new Element(returnObject, returnQName, returnType);
                    }

                    // Use marshalling by java type if necessary
                    Class byJavaType = null;
                    if (!operationDesc.isResultHeader() ||
                            MethodMarshallerUtils.isNotJAXBRootElement(returnType, marshalDesc)) {
                        byJavaType = returnType;
                    }
                    MethodMarshallerUtils.toMessage(returnElement,
                                                    returnType,
                                                    operationDesc.isListType(),
                                                    marshalDesc,
                                                    m,
                                                    byJavaType,
                                                    operationDesc.isResultHeader());
                }
            }

            // Convert the holder objects into a list of JAXB objects for marshalling
            List<PDElement> pdeList =
                    MethodMarshallerUtils.getPDElements(marshalDesc,
                                                        pds,
                                                        signatureArgs,
                                                        false,  // output
                                                        false,
                                                        true);   // use partName since this is rpc/lit

            // We want to use "by Java Type" marshalling for 
            // all body elements and all non-JAXB objects
            for (PDElement pde : pdeList) {
                ParameterDescription pd = pde.getParam();
                Class type = pd.getParameterActualType();
                if (!pd.isHeader() ||
                        MethodMarshallerUtils.isNotJAXBRootElement(type, marshalDesc)) {
                    pde.setByJavaTypeClass(type);
                }
            }
            // TODO Should we check for null output body values?  Should we check for null output header values ?
            // Put values onto the message
            MethodMarshallerUtils.toMessage(pdeList, m, packages, null);
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasResponseSwaRefAttachments()) {
                m.setDoingSWA(true);
            }

            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }


    public Object demarshalResponse(Message message, Object[] signatureArgs,
                                    OperationDescription operationDesc)
            throws WebServiceException {

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:opResponse xmlns:m="urn://api">
            //       <param xsi:type="data:foo" >...</param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (param) are defined by the wsdl:part not the
            //      schema.  Note that it is unqualified per WSI-BP
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  
            //   5) We always send an xsi:type, but other vendor's may not.
            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Indicate that the style is RPC.  This is important so that the message understands
            // that the data blocks are underneath the operation element
            message.setStyle(Style.RPC);

            // Get the return value.
            Class returnType = operationDesc.getResultActualType();
            Object returnValue = null;
            boolean hasReturnInBody = false;
            if (returnType != void.class) {
                AttachmentDescription attachmentDesc = 
                    operationDesc.getResultAttachmentDescription();
                if (attachmentDesc != null) {
                    if (attachmentDesc.getAttachmentType() == AttachmentType.SWA) {
                       String cid = message.getAttachmentID(0);
                       returnValue = message.getDataHandler(cid);
                       if (ConvertUtils.isConvertable(returnValue, returnType)) {
                           returnValue = ConvertUtils.convert(returnValue, returnType);
                       } 
                    } else {
                        throw ExceptionFactory.
                          makeWebServiceException(Messages.getMessage("pdElementErr"));
                    }
                } else {
                    // If the webresult is in the header, we need the name of the header so that we can find it.
                    Element returnElement = null;
                    // Use "byJavaType" unmarshalling if necessary
                    Class byJavaType = null;
                    if (!operationDesc.isResultHeader() ||
                            MethodMarshallerUtils.isNotJAXBRootElement(returnType, marshalDesc)) {
                        byJavaType = returnType;
                    }
                    if (operationDesc.isResultHeader()) {
                        returnElement = MethodMarshallerUtils
                        .getReturnElement(packages, message, byJavaType,  operationDesc.isListType(), true,
                                          operationDesc.getResultTargetNamespace(),
                                          operationDesc.getResultPartName(),
                                          MethodMarshallerUtils.numOutputBodyParams(pds) > 0);

                    } else {
                        returnElement = MethodMarshallerUtils
                        .getReturnElement(packages, message, byJavaType,  operationDesc.isListType(), false, null, null,
                                          MethodMarshallerUtils.numOutputBodyParams(pds) > 0);
                        hasReturnInBody = true;
                    }
                    returnValue = returnElement.getTypeValue();
                    // TODO should we allow null if the return is a header?
                    //Validate input parameters for operation and make sure no input parameters are null.
                    //As per JAXWS Specification section 3.6.2.3 if a null value is passes as an argument 
                    //to a method then an implementation MUST throw WebServiceException.
                    if (returnValue == null) {
                        throw ExceptionFactory.makeWebServiceException(
                                                                       Messages.getMessage("NullParamErr3",operationDesc.getJavaMethodName()));
                    }
                }  
            }

            // We want to use "by Java Type" unmarshalling for 
            // all body elements and all non-JAXB objects
            Class[] javaTypes = new Class[pds.length];
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                Class type = pd.getParameterActualType();
                if (!pd.isHeader() ||
                        MethodMarshallerUtils.isNotJAXBRootElement(type, marshalDesc)) {
                    javaTypes[i] = type;
                }
            }

            // Unmarshall the ParamValues from the Message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds,
                                                                         message,
                                                                         packages,
                                                                         false, // output
                                                                         hasReturnInBody,
                                                                         javaTypes); // unmarshal by type

            // TODO Should we check for null output body values?  Should we check for null output header values ?

            // Populate the response Holders
            MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);

            return returnValue;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalFaultResponse(Throwable throwable,
                                        OperationDescription operationDesc, Protocol protocol)
            throws WebServiceException {

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
            Throwable t = MethodMarshallerUtils
                    .demarshalFaultResponse(operationDesc, marshalDesc, message);
            return t;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * @param opDesc
     * @return qualified qname to use in the rpc message to represent the operation (per WSI BP)
     */
    private static QName getRPCOperationQName(OperationDescription opDesc, boolean isRequest) {
        QName qName = opDesc.getName();

        String localPart = qName.getLocalPart();
        String uri = null;
        
        if (isRequest) {
            uri = opDesc.getBindingInputNamespace();
        } else {
            uri = opDesc.getBindingOutputNamespace();
        }
        
        String prefix = "rpcOp";  // Prefer using an actual prefix


        qName = new QName(uri, localPart, prefix);
        return qName;
    }

}
