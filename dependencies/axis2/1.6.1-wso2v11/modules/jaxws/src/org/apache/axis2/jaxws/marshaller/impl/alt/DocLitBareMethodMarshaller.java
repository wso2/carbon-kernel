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
import org.apache.axis2.jaxws.core.MessageContext;
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

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class DocLitBareMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(DocLitBareMethodMarshaller.class);

    public DocLitBareMethodMarshaller() {
        super();
    }

    public Object demarshalResponse(Message message, Object[] signatureArgs,
                                    OperationDescription operationDesc)
            throws WebServiceException {

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:return ... >...</m:param>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element in the message
            //   2) The data blocks are located underneath the operation element. 
            //   3) The name of the data blocks (m:param) are defined by the schema.
            //      (SOAP indicates that the name of the element is not important, but
            //      for document processing, we will assume that the name corresponds to 
            //      a schema root element)
            //   4) The type of the data block is defined by schema; thus in most cases
            //      an xsi:type will not be present
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Remember this unmarshal information so that we can speed up processing
            // the next time.
            if (shouldRegisterUnmarshalInfo(operationDesc, message.getMessageContext())) {
                MethodMarshallerUtils.registerUnmarshalInfo(message.getMessageContext(),
                                                            packages,
                                                            marshalDesc.getPackagesKey());
            }

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
                    } else {
                        throw ExceptionFactory.
                          makeWebServiceException(Messages.getMessage("pdElementErr"));
                    }
                } else {
                    // If the webresult is in the header, we need the name of the header so that we can find it.
                    Element returnElement = null;
                    if (operationDesc.isResultHeader()) {
                        returnElement =
                            MethodMarshallerUtils.getReturnElement(packages, message, null, false, true,
                                                                   operationDesc.getResultTargetNamespace(),
                                                                   operationDesc.getResultName(),
                                                                   MethodMarshallerUtils.numOutputBodyParams(pds) > 0);

                    } else {
                        returnElement = MethodMarshallerUtils
                        .getReturnElement(packages, message, null, false, false, null, null,
                                          MethodMarshallerUtils.numOutputBodyParams(pds) > 0);
                        hasReturnInBody = true;
                    }
                    returnValue = returnElement.getTypeValue();
                    
                }
                if (ConvertUtils.isConvertable(returnValue, returnType)) {
                    returnValue = ConvertUtils.convert(returnValue, returnType);
                }              
            }

            // Unmarshall the ParamValues from the Message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds,
                                                                         message,
                                                                         packages,
                                                                         false, // output
                                                                         hasReturnInBody,
                                                                         null); // always unmarshal with "by element" mode

            // Populate the response Holders
            MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);

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
            //    <m:param .. >...</m:param>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element under the body.
            //   2) The data blocks are located underneath the body.  
            //   3) The name of the data blocks (m:param) are defined by the schema
            //   4) The type of the data block (data:foo) is defined by schema (and probably
            //      is not present in the message
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Remember this unmarshal information so that we can speed up processing
            // the next time.
            if (shouldRegisterUnmarshalInfo(operationDesc, message.getMessageContext())) {
                MethodMarshallerUtils.registerUnmarshalInfo(message.getMessageContext(),
                                                            packages,
                                                            marshalDesc.getPackagesKey());
            }

            // Unmarshal the ParamValues from the message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds,
                                                                         message,
                                                                         packages,
                                                                         true, // input
                                                                         false,
                                                                         null); // always unmarshal using "by element" mode

            // Build the signature arguments
            Object[] sigArguments = MethodMarshallerUtils.createRequestSignatureArgs(pds, pvList);

            return sigArguments;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalResponse(Object returnObject,
                                   Object[] signatureArgs,
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
            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:return ... >...</m:param>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element in the message
            //   2) The data blocks are located underneath the operation element. 
            //   3) The name of the data blocks (m:param) are defined by the schema.
            //      (SOAP indicates that the name of the element is not important, but
            //      for document processing, we will assume that the name corresponds to 
            //      a schema root element)
            //   4) The type of the data block is defined by schema; thus in most cases
            //      an xsi:type will not be present

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Put the return object onto the message
            Class returnType = operationDesc.getResultActualType();
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
                    Element returnElement = null;
                    QName returnQName = new QName(operationDesc.getResultTargetNamespace(),
                                                  operationDesc.getResultName());
                    if (marshalDesc.getAnnotationDesc(returnType).hasXmlRootElement()) {
                        returnElement = new Element(returnObject, returnQName);
                    } else {
                        /* when a schema defines a SimpleType with xsd list jaxws tooling generates art-effects with array rather than a java.util.List
                         * However the ObjectFactory definition uses a List and thus marshalling fails. Lets convert the Arrays to List.
                         */
                        if(operationDesc.isListType()){
                            List list= new ArrayList();
                            if(returnType.isArray()){
                                for(int count = 0; count < Array.getLength(returnObject); count++){
                                    Object obj = Array.get(returnObject, count);
                                    list.add(obj);
                                }
                                returnElement = new Element(list, returnQName, List.class);
                            }
                        }
                        else{
                            returnElement = new Element(returnObject, returnQName, returnType);
                        }
                    }
                    MethodMarshallerUtils.toMessage(returnElement, 
                                                    returnType,
                                                    operationDesc.isListType(),
                                                    marshalDesc, 
                                                    m,
                                                    null, // always marshal using "by element" mode
                                                    operationDesc.isResultHeader());
                }
            }

            // Convert the holder objects into a list of JAXB objects for marshalling
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(marshalDesc,
                                                                         pds,
                                                                         signatureArgs,
                                                                         false, // output
                                                                         false,
                                                                         false);

            // Put values onto the message
            MethodMarshallerUtils.toMessage(pvList, m, packages, null);
            
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

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        Protocol protocol = Protocol.getProtocolForBinding(endpointDesc.getClientBindingID());

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:param .. >...</m:param>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element under the body.
            //   2) The data blocks are located underneath the body.  
            //   3) The name of the data blocks (m:param) are defined by the schema
            //   4) The type of the data block (data:foo) is defined by schema (and probably
            //      is not present in the message

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // The input object represent the signature arguments.
            // Signature arguments are both holders and non-holders
            // Convert the signature into a list of JAXB objects for marshalling
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(marshalDesc,
                                                                         pds,
                                                                         signatureArguments,
                                                                         true,  // input
                                                                         false, false);

            // Put values onto the message
            MethodMarshallerUtils.toMessage(pvList, m, packages, requestContext);
            
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

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        MarshalServiceRuntimeDescription marshalDesc =
                MethodMarshallerUtils.getMarshalDesc(endpointDesc);

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
     * Registering UnmarshalInfo will cause JAXB unmarshalling to occur
     * during StAXOMBuilder processing the next time an xml message is targered
     * at this wsdl operation.  In some cases we want to disable this early unmarshalling.
     * 
     * @param OperationDescription
     * @param MessageContext
     * @return true or false
     */
    private static boolean shouldRegisterUnmarshalInfo(OperationDescription opDesc, 
                                                       MessageContext mc) {
        
        ParameterDescription[] pds = opDesc.getParameterDescriptions();
        
        // If one or more operations have a generic type of Object, then
        // avoid early unmarshalling
        for (int i=0; i<pds.length; i++) {
            ParameterDescription pd = pds[i];
            if (pd.getParameterActualType() == null ||
                pd.getParameterActualType().isAssignableFrom(Object.class)) {
                return false;
            }          
        }
        if (opDesc.getResultActualType() == null ||
                opDesc.getResultActualType().isAssignableFrom(Object.class)) {
            return false;
        }
        return true;
    }
}
