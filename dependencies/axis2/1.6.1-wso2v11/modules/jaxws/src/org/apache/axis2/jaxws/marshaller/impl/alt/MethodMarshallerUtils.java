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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.AttachmentDescription;
import org.apache.axis2.jaxws.description.AttachmentType;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.impl.alt.Element;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescriptionFactory;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.ConvertUtils;
import org.apache.axis2.jaxws.utility.SAAJFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Static Utilty Classes used by the MethodMarshaller implementations in the alt package. */
public class MethodMarshallerUtils {

    private static Log log = LogFactory.getLog(MethodMarshallerUtils.class);

    private static JAXBBlockFactory factory =
            (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);

    
    /** Intentionally Private.  This is a static utility class */
    private MethodMarshallerUtils() {
    }

    /**
     * Returns the list of PDElements that need to be marshalled onto the wire
     *
     * @param marshalDesc
     * @param params          ParameterDescription for this operation
     * @param sigArguments    arguments
     * @param isInput         indicates if input or output  params(input args on client, 
     *                        output args on server)
     * @param isDocLitWrapped
     * @param isRPC
     * @return PDElements
     */
    static List<PDElement> getPDElements(MarshalServiceRuntimeDescription marshalDesc,
                                         ParameterDescription[] params,
                                         Object[] sigArguments,
                                         boolean isInput,
                                         boolean isDocLitWrapped,
                                         boolean isRPC) {
        List<PDElement> pdeList = new ArrayList<PDElement>();

        int index = 0;
        for (int i = 0; i < params.length; i++) {
            ParameterDescription pd = params[i];

            if (pd.getMode() == Mode.IN && isInput ||
                    pd.getMode() == Mode.INOUT ||
                    pd.getMode() == Mode.OUT && !isInput) {

                // Get the matching signature argument
                Object value = sigArguments[i];

                // Don't consider async handlers, they are are not represented on the wire,
                // thus they don't have a PDElement
                if (isAsyncHandler(value)) {
                    continue;
                }

                // Convert from Holder into value
                if (isHolder(value)) {
                    value = ((Holder)value).value;
                }

                // Get the formal type representing the value
                Class formalType = pd.getParameterActualType();

                // The namespace and local name are obtained differently depending on 
                // the style/use and header
                QName qName = null;
                if (pd.isHeader()) {
                    // Headers (even rpc) are marshalled with the name defined by the 
                    // element= attribute on the wsd:part
                    qName = new QName(pd.getTargetNamespace(), pd.getParameterName());
                } else if (isDocLitWrapped) {
                    // For doc/lit wrapped, the localName comes from the PartName
                    qName = new QName(pd.getTargetNamespace(), pd.getPartName());
                } else if (isRPC) {
                    // Per WSI-BP, the namespace uri is unqualified
                    qName = new QName(pd.getPartName());
                } else {
                    qName = new QName(pd.getTargetNamespace(), pd.getParameterName());
                }

                // Create an Element rendering
                Element element = null;
                AttachmentDescription attachmentDesc = pd.getAttachmentDescription();
                if (attachmentDesc != null) {
                    PDElement pde = createPDElementForAttachment(pd, qName, value, formalType);
                    pdeList.add(pde);
                } else {
                    if (!marshalDesc.getAnnotationDesc(formalType).hasXmlRootElement()) {
                        /* when a schema defines a SimpleType with xsd list jaxws tooling 
                         * generates artifacts with array rather than a java.util.List
                         * However the ObjectFactory definition uses a List and thus 
                         * marshalling fails. Lets convert the Arrays to List and recreate
                         * the JAXBElements for the same.
                         */
                        if (pd.isListType()) {
                            
                            List<Object> list = new ArrayList<Object>();
                            if (formalType.isArray()) {
                                for (int count = 0; count < Array.getLength(value); count++) {
                                    Object obj = Array.get(value, count);
                                    list.add(obj);
                                }

                            }
                            element = new Element(list, qName, List.class);
                        } else {
                            element = new Element(value, qName, formalType);
                        }
                      }
                    else{
                        element = new Element(value, qName);
                    }
                    // The object is now ready for marshalling
                    PDElement pde = new PDElement(pd, element, null);
                    pdeList.add(pde);
                }
            }
        }

        return pdeList;
    }
    
    /**
     * @param pd
     * @param qName
     * @param value
     * @param formalType
     * @return
     */
    private static PDElement createPDElementForAttachment(ParameterDescription pd, 
                                                          QName qName, 
                                                          Object value, 
                                                          Class formalType) {
        PDElement pde;
        if (log.isDebugEnabled()) {
            log.debug("Creating a PDElement for an attachment value: " + 
                      ((value == null)? "null":value.getClass().getName()));
            log.debug("ParameterDescription = " + pd.toString());
        }
        AttachmentDescription attachmentDesc = pd.getAttachmentDescription();
        
        AttachmentType attachmentType = attachmentDesc.getAttachmentType();
        if (attachmentType == AttachmentType.SWA) {
            // Create an Attachment object with the signature value
            Attachment attachment = new Attachment(value, 
                                                   formalType, 
                                                   attachmentDesc,
                                                   pd.getPartName());
            pde = new PDElement(pd, 
                    null, // For SWA Attachments, there is no element reference to the attachment
                    null, 
                    attachment);
        } else {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("pdElementErr"));
        }
        return pde;
    }

    /**
     * Return the list of PDElements that is unmarshalled from the wire
     * 
     * @param params ParameterDescription for this operation
     * @param message Message
     * @param packages set of packages needed to unmarshal objects for this operation
     * @param isInput indicates if input or output  params (input on server, output on client)
     * @param hasReturnInBody if isInput=false, then this parameter indicates whether a 
     * return value is expected in the body.
     * @param unmarshalByJavaType in most scenarios this is null.  
     * Only use this in the scenarios that require unmarshalling by java type
     * @see getPDElementsWithMissingElements
     * @return ParamValues
     */
    static List<PDElement> getPDElements(ParameterDescription[] params,
                                         Message message,
                                         TreeSet<String> packages,
                                         boolean isInput,
                                         boolean hasReturnInBody, 
                                         Class[] unmarshalByJavaType) throws XMLStreamException {

        List<PDElement> pdeList = new ArrayList<PDElement>();

        // Count 
        int totalBodyBlocks = 0;
        for (int i = 0; i < params.length; i++) {
            ParameterDescription pd = params[i];

            if (pd.getMode() == Mode.IN && isInput ||
                    pd.getMode() == Mode.INOUT ||
                    pd.getMode() == Mode.OUT && !isInput) {
                if (!pd.isHeader() && !isSWAAttachment(pd)) {
                    totalBodyBlocks++;
                }
            }
        }

        if (!isInput && hasReturnInBody) {
            totalBodyBlocks++;
        }
            
        int index = (!isInput && hasReturnInBody) ? 1 : 0;
        // TODO What if return is an swa attachment, then this should start
        // at 1 not 0.
        int swaIndex = 0;
        for (int i = 0; i < params.length; i++) {
            ParameterDescription pd = params[i];

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
                    
                    // Normal Processing: Not an Attachment
                    // Trigger unmarshal by java type if necessary
                    if (unmarshalByJavaType != null && unmarshalByJavaType[i] != null) {
                        context.setProcessType(unmarshalByJavaType[i]);
                        context.setIsxmlList(pd.isListType());
                    }

                    boolean consume = true;
                    // Unmarshal the object into a JAXB object or JAXBElement
                    if (pd.isHeader()) {
                        
                        // Get the Block from the header
                        // NOTE The parameter name is always used to get the header 
                        // element...even if the style is RPC.
                        String localName = pd.getParameterName();
                        block = message.getHeaderBlock(pd.getTargetNamespace(),
                                                       localName,
                                                       context,
                                                       factory);
                        consume = false;
                    } else {
                        if (totalBodyBlocks > 1) {
                            // You must use this method if there are more than one body block
                            // This method may cause OM expansion
                            block = message.getBodyBlock(index, context, factory);
                        } else {
                            // Use this method if you know there is only one body block.
                            // This method prevents OM expansion.
                            block = message.getBodyBlock(context, factory);
                        }
                        index++;
                    }
                    
                    Element element;
                    if (block != null) {
                        element = new Element(block.getBusinessObject(true), 
                                              block.getQName());
                    } else {
                        // The block could be null if the header is missing (which is allowed)
                        QName qName = new QName(pd.getTargetNamespace(),pd.getParameterName());
                        if (log.isDebugEnabled()) {
                            log.debug("There is no value in the incoming message for " + qName);
                        }
                        element = new Element(null, qName, pd.getParameterActualType());
                    }
                    PDElement pde =
                        new PDElement(pd, element, unmarshalByJavaType == null ? null
                                : unmarshalByJavaType[i]);
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

        return pdeList;
    }

    
    
    
    
    /**
     * Creates the request signature arguments (server) from a list
     * of element enabled object (PDEements)
     * @param pds ParameterDescriptions for this Operation
     * @param pvList Element enabled object
     * @return Signature Args
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static Object[] createRequestSignatureArgs(ParameterDescription[] pds, 
                                               List<PDElement> pdeList)
    throws InstantiationException, IOException,
    IllegalAccessException,
    ClassNotFoundException {
        Object[] args = new Object[pds.length];
        int pdeIndex = 0;
        for (int i = 0; i < args.length; i++) {
            // Get the paramValue
            PDElement pde = (pdeIndex < pdeList.size()) ? pdeList.get(pdeIndex) : null;
            ParameterDescription pd = pds[i];
            if (pde == null ||
                    pde.getParam() != pd) {
                // We have a ParameterDesc but there is not an equivalent PDElement. 
                // Provide the default
                if (pd.isHolderType()) {
                    args[i] = createHolder(pd.getParameterType(), null);
                } else {
                    args[i] = null;
                }
            } else {

                // We have a matching paramValue.  Get the type object that represents the type
                Object value = null;
                if (pde.getAttachment() != null) {
                    value = pde.getAttachment().getDataHandler();
                } else {
                    value = pde.getElement().getTypeValue();
                }
                pdeIndex++;

                // Now that we have the type, there may be a mismatch
                // between the type (as defined by JAXB) and the formal
                // parameter (as defined by JAXWS).  Frequently this occurs
                // with respect to T[] versus List<T>.  
                // Use the convert utility to silently do any conversions
                if (ConvertUtils.isConvertable(value, pd.getParameterActualType())) {
                    value = ConvertUtils.convert(value, pd.getParameterActualType());
                } else {
                    String objectClass = (value == null) ? "null" : value.getClass().getName();
                    throw ExceptionFactory.makeWebServiceException(
                            Messages.getMessage("convertProblem", objectClass,
                                                pd.getParameterActualType().getName()));
                }

                // The signature may want a holder representation
                if (pd.isHolderType()) {
                    args[i] = createHolder(pd.getParameterType(), value);
                } else {
                    args[i] = value;
                }
            }

        }
        return args;
    }

    /**
     * Update the signature arguments on the client with the unmarshalled element enabled objects
     * (pvList)
     *
     * @param pds           ParameterDescriptions
     * @param pdeList       Element Enabled objects
     * @param signatureArgs Signature Arguments (the out/inout holders are updated)
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static void updateResponseSignatureArgs(ParameterDescription[] pds, List<PDElement> pdeList,
                                            Object[] signatureArgs)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        int pdeIndex = 0;

        // Each ParameterDescriptor has a correspondinging signatureArg from the 
        // the initial client call.  The pvList contains the response values from the message.
        // Walk the ParameterDescriptor/SignatureArg list and populate the holders with 
        // the match PDElement
        for (int i = 0; i < pds.length; i++) {
            // Get the param value
            PDElement pde = (pdeIndex < pdeList.size()) ? pdeList.get(pdeIndex) : null;
            ParameterDescription pd = pds[i];
            if (pde != null && pde.getParam() == pd) {
                // We have a matching paramValue.  Get the value that represents the type
                Object value = null;
                if (pde.getAttachment() == null) {
                    value = pde.getElement().getTypeValue();
                } else {
                    value = pde.getAttachment().getDataHandler();
                }
                pdeIndex++;

                // Now that we have the type, there may be a mismatch
                // between the type (as defined by JAXB) and the formal
                // parameter (as defined by JAXWS).  Frequently this occurs
                // with respect to T[] versus List<T>.  
                // Use the convert utility to silently do any conversions
                if (ConvertUtils.isConvertable(value, pd.getParameterActualType())) {
                    value = ConvertUtils.convert(value, pd.getParameterActualType());
                } else {
                    String objectClass = (value == null) ? "null" : value.getClass().getName();
                    throw ExceptionFactory.makeWebServiceException(
                            Messages.getMessage("convertProblem", objectClass,
                                                pd.getParameterActualType().getName()));
                }

                // TODO Assert that this ParameterDescriptor must represent
                // an OUT or INOUT and must have a non-null holder object to 
                // store the value
                if (isHolder(signatureArgs[i])) {
                    ((Holder)signatureArgs[i]).value = value;
                }
            }
        }
    }

    
    /**
     * Marshal the element enabled objects (pvList) to the Message
     *
     * @param pdeList  element enabled objects
     * @param message  Message
     * @param packages Packages needed to do a JAXB Marshal
     * @param contextProperties RequestContext or ResponseContext or null
     * @throws MessageException
     */
    static void toMessage(List<PDElement> pdeList,
                          Message message,
                          TreeSet<String> packages, 
                          Map<String, Object> contextProperties) throws WebServiceException {

        int totalBodyBlocks = 0;
        for (int i = 0; i < pdeList.size(); i++) {
            PDElement pde = pdeList.get(i);
            if (!pde.getParam().isHeader() &&
                 pde.getElement() != null) { // Element is null for SWARef attachment
                totalBodyBlocks++;
            }
        }

        int index = message.getNumBodyBlocks();
        for (int i = 0; i < pdeList.size(); i++) {
            PDElement pde = pdeList.get(i);

            // Create JAXBContext
            JAXBBlockContext context = new JAXBBlockContext(packages);

            Attachment attachment = pde.getAttachment();
            if (attachment == null) {
                // Normal Flow: Not an attachment
                
                
                // Marshal by type only if necessary
                if (pde.getByJavaTypeClass() != null) {
                    context.setProcessType(pde.getByJavaTypeClass());
                    if(pde.getParam()!=null){
                        context.setIsxmlList(pde.getParam().isListType());
                    }
                }
                // Create a JAXBBlock out of the value.
                // (Note that the PDElement.getValue always returns an object
                // that has an element rendering...ie. it is either a JAXBElement or
                // has @XmlRootElement defined
                Block block =
                    factory.createFrom(pde.getElement().getElementValue(),
                                       context,
                                       pde.getElement().getQName());
                
                if (pde.getParam().isHeader()) {
                    // Header block
                    if (pde.getElement().getTypeValue() != null) {
                        // The value is non-null, add a header.
                        QName qname = block.getQName();
                        message.setHeaderBlock(qname.getNamespaceURI(), qname.getLocalPart(), block);
                    } else {
                        // The value is null, it is still best to add a nil header.
                        // But query to see if an override is desired.
                        if (isWriteWithNilHeader(contextProperties)) {
                            QName qname = block.getQName();
                            message.setHeaderBlock(qname.getNamespaceURI(), qname.getLocalPart(), block);
                        }
                    }
                } else {
                    // Body block
                    if (totalBodyBlocks < 1) {
                        // If there is only one block, use the following "more performant" method
                        message.setBodyBlock(block);
                    } else {
                        message.setBodyBlock(index, block);
                    }
                    index++;
                }
            } else {
                // The parameter is an attachment
                AttachmentType type = pde.getParam().
                   getAttachmentDescription().getAttachmentType();
                if (type == AttachmentType.SWA) {
                    // All we need to do is set the data handler on the message.  
                    // For SWA attachments, the message does not reference the attachment.
                    message.addDataHandler(attachment.getDataHandler(), 
                                           attachment.getContentID());
                    message.setDoingSWA(true);
                } else {
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("pdElementErr"));
                }
            }
        }
    }

    /**
     * @return Determine if a null header parameter should be written with a header
     * element containing nill (true) or whether the header element should
     * not be written at all (false)
     */
    private static boolean isWriteWithNilHeader(Map<String, Object> map) {
        if (map == null) {
            if (log.isDebugEnabled()) {
                log.debug("Context Properties are not available.  Return true ");
            }
            return true;
        }
        Object value = map.get(Constants.WRITE_HEADER_ELEMENT_IF_NULL);
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("Write header element with xsi:nil because the following property is not set " + 
                        Constants.WRITE_HEADER_ELEMENT_IF_NULL);
            }
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Key=" + Constants.WRITE_HEADER_ELEMENT_IF_NULL + " Value=" + value);
            }
            return ((Boolean) value).booleanValue();          
        }
    }
    /**
     * Marshals the return object to the message (used on server to marshal return object)
     *
     * @param returnElement              element
     * @param returnType
     * @param marshalDesc
     * @param message
     * @param marshalByJavaTypeClass..we must do this for RPC...discouraged otherwise
     * @param isHeader
     * @throws MessageException
     */
    static void toMessage(Element returnElement,
                          Class returnType,
                          boolean isList,
                          MarshalServiceRuntimeDescription marshalDesc,
                          Message message,
                          Class marshalByJavaTypeClass,
                          boolean isHeader)
            throws WebServiceException {

        // Create the JAXBBlockContext
        // RPC uses type marshalling, so recored the rpcType
        JAXBBlockContext context = new JAXBBlockContext(marshalDesc.getPackages());
        if (marshalByJavaTypeClass != null) {
            context.setProcessType(marshalByJavaTypeClass);
            context.setIsxmlList(isList);
        }

        //  Create a JAXBBlock out of the value.
        Block block = factory.createFrom(returnElement.getElementValue(),
                                         context,
                                         returnElement.getQName());

        if (isHeader) {
            message.setHeaderBlock(returnElement.getQName().getNamespaceURI(),
                                   returnElement.getQName().getLocalPart(), block);
        } else {
            message.setBodyBlock(block);
        }
    }

    /**
     * Unmarshal the return object from the message
     *
     * @param packages
     * @param message
     * @param unmarshalByJavaTypeClass Used only to indicate unmarshaling by type...only necessary
     *                                 in some scenarios
     * @param isHeader
     * @param headerNS                 (only needed if isHeader)
     * @param headerLocalPart          (only needed if isHeader)
     * @param hasOutputBodyParams (true if the method has out or inout params other 
     * than the return value)
     * @return Element
     * @throws WebService
     * @throws XMLStreamException
     */
    static Element getReturnElement(TreeSet<String> packages,
                                    Message message,
                                    Class unmarshalByJavaTypeClass,  // normally null
                                    boolean isList,
                                    boolean isHeader,
                                    String headerNS,
                                    String headerLocalPart,
                                    boolean hasOutputBodyParams)

            throws WebServiceException, XMLStreamException {

        // The return object is the first block in the body
        JAXBBlockContext context = new JAXBBlockContext(packages);
        if (unmarshalByJavaTypeClass != null && !isHeader) {
            context.setProcessType(unmarshalByJavaTypeClass);
            context.setIsxmlList(isList);
        }
        Block block = null;
        boolean isBody = false;
        if (isHeader) {
            block = message.getHeaderBlock(headerNS, headerLocalPart, context, factory);
        } else {
            if (hasOutputBodyParams) {
                block = message.getBodyBlock(0, context, factory);
                isBody = true;
            } else {
                // If there is only 1 block, we can use the get body block method
                // that streams the whole block content.
                block = message.getBodyBlock(context, factory);
                //We look for body block only when the return type associated with operation is not void.
                //If a null body block is returned in response on a operation that is not void, its a user error.               
                isBody = true;
            }
        }
        //We look for body block only when the return type associated with operation is not void.
        //If a null body block is returned in response on a operation that has non void return type, its a user error.
        if(isBody && block == null){
           	if(log.isDebugEnabled()){
           		log.debug("Empty Body Block Found in response Message for wsdl Operation defintion that expects an Output");
           		log.debug("Return type associated with SEI operation is not void, Body Block cannot be null");
           	}
           	throw ExceptionFactory.makeWebServiceException(Messages.getMessage("MethodMarshallerUtilErr1"));	
        }
        // Get the business object.  We want to return the object that represents the type.
        Element returnElement = new Element(block.getBusinessObject(true), block.getQName());
        return returnElement;
    }

    /**
     * Marshaling a fault is essentially the same for rpc/lit and doc/lit. This method is used by
     * all of the MethodMarshallers
     *
     * @param throwable     Throwable to marshal
     * @param operationDesc OperationDescription
     * @param packages      Packages needed to marshal the object
     * @param message       Message
     */
    static void marshalFaultResponse(Throwable throwable,
                                     MarshalServiceRuntimeDescription marshalDesc,
                                     OperationDescription operationDesc,
                                     Message message) {
        // Get the root cause of the throwable object
        Throwable t = ClassUtils.getRootCause(throwable);
        if (log.isDebugEnabled()) {
            log.debug("Marshal Throwable =" + throwable.getClass().getName());
            log.debug("  rootCause =" + t.getClass().getName());
            log.debug("  exception=" + t.toString());
            log.debug("  stack=" + stackToString(t));
        }

        XMLFault xmlfault = null;

        try {

            // There are 5 different categories of exceptions.  
            // Each category has a little different marshaling code.
            // A) Service Exception that matches the JAX-WS 
            //    specification (chapter 2.5 of the spec)
            // B) Service Exception that matches the JAX-WS "legacy" 
            //    exception (chapter 3.7 of the spec)
            // C) SOAPFaultException
            // D) WebServiceException
            // E) Other runtime exceptions (i.e. NullPointerException)

            // Get the FaultDescriptor matching this Exception.
            // If FaultDescriptor is found, this is a JAX-B Service Exception.
            // If not found, this is a System Exception
            FaultDescription fd =
                    operationDesc.resolveFaultByExceptionName(t.getClass().getCanonicalName());

            if (fd != null) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a Service Exception");
                }
                // Create the JAXB Context
                JAXBBlockContext context = new JAXBBlockContext(marshalDesc.getPackages());

                // The exception is a Service Exception.  
                // It may be (A) JAX-WS compliant exception or 
                // (B) JAX-WS legacy exception

                // The faultBeanObject is a JAXB object that represents the data of the exception.
                // It is marshalled in the detail section of the soap fault.  
                // The faultBeanObject is obtained direction from the exception (A) or via 
                // the legacy exception rules (B).
                Object faultBeanObject = null;

                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
                String faultInfo = fd.getFaultInfo();
                if (faultInfo == null || faultInfo.length() == 0) {
                    // Legacy Exception case
                    faultBeanObject = LegacyExceptionUtil.createFaultBean(t, fd, marshalDesc);
                } else {
                    // Normal case
                    // Get the fault bean object.  
                    Method getFaultInfo = t.getClass().getMethod("getFaultInfo", null);
                    faultBeanObject = getFaultInfo.invoke(t, null);
                }

                if (log.isErrorEnabled()) {
                    log.debug("The faultBean type is" + faultBeanObject.getClass().getName());
                }

                // Use "by java type" marshalling if necessary
                if (faultBeanObject == t ||
                        (context.getConstructionType() != JAXBUtils.CONSTRUCTION_TYPE
                                .BY_CONTEXT_PATH &&
                                isNotJAXBRootElement(faultBeanObject.getClass(), marshalDesc))) {
                    context.setProcessType(faultBeanObject.getClass());
                }

                QName faultBeanQName = new QName(faultBeanDesc.getFaultBeanNamespace(),
                                                 faultBeanDesc.getFaultBeanLocalName());
                // Make sure the faultBeanObject can be marshalled as an element
                if (!marshalDesc.getAnnotationDesc(faultBeanObject.getClass()).
                        hasXmlRootElement())
                {
                    faultBeanObject = new JAXBElement(faultBeanQName, faultBeanObject.getClass(),
                                                      faultBeanObject);
                }

                // Create a detailblock representing the faultBeanObject
                Block[] detailBlocks = new Block[1];
                detailBlocks[0] = factory.createFrom(faultBeanObject, context, faultBeanQName);

                if (log.isDebugEnabled()) {
                    log.debug("Create the xmlFault for the Service Exception");
                }
                // Get the fault text using algorithm defined in JAX-WS 10.2.2.3
                String text = t.getMessage();
                if (text == null || text.length() == 0) {
                    text = t.toString();
                }
                // Now make a XMLFault containing the detailblock
                xmlfault = new XMLFault(null, new XMLFaultReason(text), detailBlocks);
            } else {
                xmlfault = createXMLFaultFromSystemException(t);
            }
        } catch (Throwable e) {
            // If an exception occurs while demarshalling an exception, 
            // then rinse and repeat with a system exception
            if (log.isDebugEnabled()) {
                log.debug("An exception (" + e + ") occurred while marshalling exception (" + t +
                        ")");
            }
            WebServiceException wse = ExceptionFactory.makeWebServiceException(e);
            xmlfault = createXMLFaultFromSystemException(wse);
        }

        // Add the fault to the message
        message.setXMLFault(xmlfault);
    }

    /**
     * This method is used by WebService Impl and Provider to create an XMLFault (for marshalling)
     * from an exception that is a non-service exception
     *
     * @param t Throwable that represents a Service Exception
     * @return XMLFault
     */
    public static XMLFault createXMLFaultFromSystemException(Throwable t) {

        try {
            XMLFault xmlfault = null;
            if (t instanceof SOAPFaultException) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal SOAPFaultException");
                }
                // Category C: SOAPFaultException 
                // Construct the xmlFault from the SOAPFaultException's Fault
                SOAPFaultException sfe = (SOAPFaultException)t;
                SOAPFault soapFault = sfe.getFault();
                if (soapFault == null) {
                    // No fault ?  I will treat this like category E
                    xmlfault = 
                        new XMLFault(null,       // Use the default XMLFaultCode
                                     new XMLFaultReason(
                                     t.toString()));  // Assumes text lang of current Locale
                } else {
                    xmlfault = XMLFaultUtils.createXMLFault(soapFault);
                }

            } else if (t instanceof WebServiceException) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a WebServiceException");
                }
                // Category D: WebServiceException
                // The reason is constructed with the getMessage of the exception.  
                // There is no detail
                WebServiceException wse = (WebServiceException)t;

                // Get the fault text using algorithm defined in JAX-WS 10.2.2.3
                String text = wse.getMessage();
                if (text == null || text.length() == 0) {
                    text = wse.toString();
                }
                xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                                        new XMLFaultReason(
                                             text));  // Assumes text lang of current Locale
            } else {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a unchecked System Exception");
                }
                // Category E: Other System Exception
                // The reason is constructed with the toString of the exception.  
                // This places the class name of the exception in the reason
                // There is no detail.
                // Get the fault text using algorithm defined in JAX-WS 10.2.2.3
                String text = t.getMessage();
                if (text == null || text.length() == 0) {
                    text = t.toString();
                }
                xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                                        new XMLFaultReason(
                                                text));  // Assumes text lang of current Locale
            }
            return xmlfault;
        } catch (Throwable e) {
            try {
                // If an exception occurs while demarshalling an exception, 
                // then rinse and repeat with a webservice exception
                if (log.isDebugEnabled()) {
                    log.debug("An exception (" + e + ") occurred while marshalling exception (" +
                            t + ")");
                }
                // Get the fault text using algorithm defined in JAX-WS 10.2.2.3
                String text = e.getMessage();
                if (text == null || text.length() == 0) {
                    text = e.toString();
                }
                WebServiceException wse = ExceptionFactory.makeWebServiceException(e);

                return new XMLFault(null,       // Use the default XMLFaultCode
                                    new XMLFaultReason(
                                            text));  // Assumes text lang of current Locale
            } catch (Exception e2) {
                // Exception while creating Exception for Exception
                throw ExceptionFactory.makeWebServiceException(e2);
            }
        }
    }

    /**
     * Unmarshal the service/system exception from a Message. This is used by all of the
     * marshallers
     *
     * @param operationDesc
     * @param marshalDesc
     * @param message
     * @return Throwable
     * @throws WebServiceException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws XMLStreamException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    static Throwable demarshalFaultResponse(OperationDescription operationDesc,
                                            MarshalServiceRuntimeDescription marshalDesc,
                                            Message message)
            throws WebServiceException, ClassNotFoundException, IllegalAccessException,
            InstantiationException, XMLStreamException, InvocationTargetException,
            NoSuchMethodException {

        Throwable exception = null;
        
        // Get the fault from the message and get the detail blocks (probably one)
        XMLFault xmlfault = message.getXMLFault();
        Block[] detailBlocks = xmlfault.getDetailBlocks();

        // If there is only one block, get the element name of that block.
        QName elementQName = null;
        if (detailBlocks != null && detailBlocks.length >= 1) {
            elementQName = detailBlocks[0].getQName();
            if (log.isDebugEnabled()) {
                if (detailBlocks.length > 1) {
                    log.debug("The detail element has multiple child elements.  " +
                            "Only the first child is examined to determine if this is a service exception.  " + 
                            "If this first detail child is mapped to a service exception, " + 
                            "the information in other detail children may be ignored.  " + 
                            "The most common scenario is that the second child is a " +
                            "{http://jax-ws.dev.java.net/}exception element, which is used " +
                            "by some vendors for debugging. ");
                }
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("element QName which will be used to find a service exception = " + elementQName);
            log.debug("XMLFault Dump = " +xmlfault.dump(""));
            log.debug("OperationDesc Dump =" + operationDesc);
        }

        // Use the element name to find the matching FaultDescriptor
        FaultDescription faultDesc = null;
        if (elementQName != null) {
            for (int i = 0; i < operationDesc.getFaultDescriptions().length && faultDesc == null;
                 i++) {
                FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);

				if (faultBeanDesc != null) {
					QName tryQName = new QName(faultBeanDesc.getFaultBeanNamespace(),
							faultBeanDesc.getFaultBeanLocalName());
					if (log.isErrorEnabled()) {
						log.debug("  FaultDescription qname is (" + tryQName +
								") and detail element qname is (" + elementQName + ")");
					}

					if (elementQName.equals(tryQName)) {
						faultDesc = fd;
					}
				}
            }
        }

        if (faultDesc == null && elementQName != null) {
            // If not found, retry the search using just the local name
            for (int i = 0; i < operationDesc.getFaultDescriptions().length && faultDesc == null;
                 i++) {
                FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
                if (faultBeanDesc != null) {
                	String tryName = faultBeanDesc.getFaultBeanLocalName();
                	if (elementQName.getLocalPart().equals(tryName)) {
                		faultDesc = fd;
                	}
                }
            }
        }


        if (faultDesc == null) {
            // This is a system exception if the method does not throw a checked exception or if
            // the detail block is missing or contains multiple items.
            exception = createSystemException(xmlfault, message);
        } else {
            if (log.isErrorEnabled()) {
                log.debug("Ready to demarshal service exception.  The detail entry name is " +
                        elementQName);
            }
            FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(faultDesc);
            boolean isLegacy =
                    (faultDesc.getFaultInfo() == null || faultDesc.getFaultInfo().length() == 0);

            // Get the JAXB object from the block
            JAXBBlockContext blockContext = new JAXBBlockContext(marshalDesc.getPackages());

            // Note that faultBean may not be a bean, it could be a primitive 
            Class faultBeanFormalClass;
            try {
                faultBeanFormalClass = loadClass(faultBeanDesc.getFaultBeanClassName());
            } catch (ClassNotFoundException e){
                faultBeanFormalClass = loadClass(faultBeanDesc.getFaultBeanClassName(), operationDesc.getEndpointInterfaceDescription().getEndpointDescription().getAxisService().getClassLoader());
            }

            // Use "by java type" marshalling if necessary
            if (blockContext.getConstructionType() != 
                JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH &&
                    isNotJAXBRootElement(faultBeanFormalClass, marshalDesc)) {
                blockContext.setProcessType(faultBeanFormalClass);
            }

            // Get the jaxb block and business object
            Block jaxbBlock = factory.createFrom(detailBlocks[0], blockContext);
            Object faultBeanObject = jaxbBlock.getBusinessObject(true);

            // At this point, faultBeanObject is an object that can be rendered as an
            // element.  We want the object that represents the type.
            if (faultBeanObject instanceof JAXBElement) {
                faultBeanObject = ((JAXBElement)faultBeanObject).getValue();
            }

            if (log.isErrorEnabled()) {
                log.debug("Unmarshalled the detail element into a JAXB object");
            }

            // Construct the JAX-WS generated exception that holds the faultBeanObject
            Class exceptionClass;
            try {
                exceptionClass = loadClass(faultDesc.getExceptionClassName());
            } catch (ClassNotFoundException e){
                exceptionClass = loadClass(faultDesc.getExceptionClassName(), operationDesc.getEndpointInterfaceDescription().getEndpointDescription().getAxisService().getClassLoader());
            }
            if (log.isErrorEnabled()) {
                log.debug("Found FaultDescription.  The exception name is " +
                        exceptionClass.getName());
            }
            exception = createServiceException(xmlfault.getReason().getText(),
                                               exceptionClass,
                                               faultBeanObject,
                                               faultBeanFormalClass,
                                               marshalDesc,
                                               isLegacy);
        }
        return exception;
    }

    
    /**
     * @param pds
     * @return Number of inout or out parameters
     */
    static int numOutputBodyParams(ParameterDescription[] pds) {
        int count = 0;
        for (int i=0; i<pds.length; i++) {
            // TODO Need to change this to also detect not attachment
            if (!pds[i].isHeader()) {
                if (pds[i].getMode() == Mode.INOUT ||
                        pds[i].getMode() == Mode.OUT) {
                    count++;
                }
            }
        }
        return count;
    }
    

    /**
     * @param value
     * @return if async handler
     */
    static boolean isAsyncHandler(Object value) {
        return (value instanceof AsyncHandler);
    }

    /**
     * @param value
     * @return true if value is holder
     */
    static boolean isHolder(Object value) {
        return value != null && Holder.class.isAssignableFrom(value.getClass());
    }

    /**
     * Crate a Holder
     *
     * @param <T>
     * @param paramType
     * @param value
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    static <T> Holder<T> createHolder(Class paramType, T value)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (Holder.class.isAssignableFrom(paramType)) {
            Holder holder = (Holder) paramType.newInstance();
            holder.value = value;
            return holder;
        }
        return null;
    }

    /**
     * Load the class
     *
     * @param className
     * @return loaded class
     * @throws ClassNotFoundException
     */
    static Class loadClass(String className) throws ClassNotFoundException {
        // Don't make this public, its a security exposure
        Class cls = ClassUtils.getPrimitiveClass(className);
        if (cls == null) {
            cls = forName(className, true, getContextClassLoader());
        }
        return cls;
    }

    /**
     * Load the class
     *
     * @param className
     * @return loaded class
     * @throws ClassNotFoundException
     */
    static Class loadClass(String className, ClassLoader cl) throws ClassNotFoundException {
        // Don't make this public, its a security exposure
        Class cls = ClassUtils.getPrimitiveClass(className);
        if (cls == null) {
            cls = forName(className, true, cl);
        }
        return cls;
    }
    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
                                 final ClassLoader classLoader) throws ClassNotFoundException {
        // NOTE: This method must remain private because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            // Class.forName does not support primitives
                            Class cls = ClassUtils.getPrimitiveClass(className);
                            if (cls == null) {
                                cls = Class.forName(className, initialize, classLoader);
                            }
                            return cls;
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (ClassNotFoundException) e.getException();
        }

        return cl;
    }

    /** @return ClassLoader */
    private static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }


    /**
     * Create a JAX-WS Service Exception (Generated Exception)
     *
     * @param message
     * @param exceptionclass
     * @param bean
     * @param beanFormalType
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @parma marshalDesc is used to get cached information about the exception class and bean
     */
    private static Exception createServiceException(String message,
                                                    Class exceptionclass,
                                                    Object bean,
                                                    Class beanFormalType,
                                                    MarshalServiceRuntimeDescription marshalDesc,
                                                    boolean isLegacyException) throws
            InvocationTargetException, IllegalAccessException, InstantiationException,
            NoSuchMethodException {

        if (log.isDebugEnabled()) {
            log.debug("Constructing JAX-WS Exception:" + exceptionclass);
        }
        Exception exception = null;
        if (isLegacyException) {
            // Legacy Exception
            exception = LegacyExceptionUtil.createFaultException(exceptionclass, 
                                                                 bean, 
                                                                 marshalDesc);
        } else {
            // Normal case, use the contstructor to create the exception
            Constructor constructor =
                    exceptionclass.getConstructor(new Class[] { String.class, beanFormalType });
            exception = (Exception)constructor.newInstance(new Object[] { message, bean });
        }

        return exception;

    }

    /**
     * Create a system exception
     *
     * @param message
     * @return
     */
    public static ProtocolException createSystemException(XMLFault xmlFault, Message message) {
        ProtocolException e = null;
        Protocol protocol = message.getProtocol();
        String text = xmlFault.getReason().getText();

        if (protocol == Protocol.soap11 || protocol == Protocol.soap12) {
            // Throw a SOAPFaultException
            if (log.isDebugEnabled()) {
                log.debug("Constructing SOAPFaultException for " + text);
            }
            String protocolNS = (protocol == Protocol.soap11) ?
                    SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE :
                    SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE;
            try {
                // The following set of instructions is used to avoid 
                // some unimplemented methods in the Axis2 SAAJ implementation
                javax.xml.soap.MessageFactory mf = SAAJFactory.createMessageFactory(protocolNS);
                SOAPBody body = mf.createMessage().getSOAPBody();
                SOAPFault soapFault = XMLFaultUtils.createSAAJFault(xmlFault, body);
                e = new SOAPFaultException(soapFault);
            } catch (Exception ex) {
                // Exception occurred during exception processing.
                // TODO Probably should do something better here
                if (log.isDebugEnabled()) {
                    log.debug("Exception occurred during fault processing:", ex);
                }
                e = ExceptionFactory.makeProtocolException(text, null);
            }
        } else if (protocol == Protocol.rest) {
            if (log.isDebugEnabled()) {
                log.debug("Constructing ProtocolException for " + text);
            }
            // TODO Is there an explicit exception for REST
            e = ExceptionFactory.makeProtocolException(text, null);
        } else if (protocol == Protocol.unknown) {
            // REVIEW What should happen if there is no protocol
            if (log.isDebugEnabled()) {
                log.debug("Constructing ProtocolException for " + text);
            }
            e = ExceptionFactory.makeProtocolException(text, null);
        }
        return e;
    }

    /**
     * @param ed
     * @return
     */
    static MarshalServiceRuntimeDescription getMarshalDesc(EndpointDescription ed) {
        ServiceDescription sd = ed.getServiceDescription();
        return MarshalServiceRuntimeDescriptionFactory.get(sd);
    }

    /**
     * This probably should be available from the ParameterDescription
     *
     * @param cls
     * @param marshalDesc
     * @return true if primitive, wrapper, java.lang.String. Calendar (or GregorianCalendar),
     *         BigInteger etc or anything other java type that is mapped by the basic schema types
     */
    static boolean isNotJAXBRootElement(Class cls, MarshalServiceRuntimeDescription marshalDesc) {
        if (cls == String.class ||
                cls.isPrimitive() ||
                cls == Calendar.class ||
                cls == byte[].class ||
                cls == GregorianCalendar.class ||
                cls == Date.class ||
                cls == BigInteger.class ||
                cls == BigDecimal.class) {

            return true;
        }
        AnnotationDesc aDesc = marshalDesc.getAnnotationDesc(cls);
        if (aDesc != null) {
            // XmlRootElementName returns null if @XmlRootElement is not specified
            return (aDesc.getXmlRootElementName() == null);
        }
        return true;  
    }
    
    /**
     * Get a string containing the stack of the specified exception   
     * @param e   
     * @return    
     */   
    public static String stackToString(Throwable e) {       
        java.io.StringWriter sw= new java.io.StringWriter();        
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);       
        java.io.PrintWriter pw= new java.io.PrintWriter(bw);       
        e.printStackTrace(pw);       
        pw.close();       
        return sw.getBuffer().toString();      
    }
    
    static boolean isSWAAttachment(ParameterDescription pd) {
        return pd.getAttachmentDescription() != null &&
            pd.getAttachmentDescription().getAttachmentType() == AttachmentType.SWA;
    }
    
    /**
     * Register the unmarshalling information so that it can 
     * be used to speed up subsequent marshalling events.
     * @param mc
     * @param packages
     * @param packagesKey
     */
    static Parameter getUnmarshalInfoParameter(MessageContext mc) throws AxisFault {
        
        // The information is registered on the AxisOperation.
        if (mc == null ||
            mc.getAxisMessageContext() == null ||
            mc.getAxisMessageContext().getAxisService() == null ||
            mc.getAxisMessageContext().getAxisOperation() == null) {
            return null;
        }
        
        // This needs to be stored on the AxisOperation as unmarshalling
        // info will be specific to a method and its parameters
        AxisOperation axisOp = mc.getAxisMessageContext().getAxisOperation();
        
        Parameter param = axisOp.getParameter(UnmarshalInfo.KEY);
        return param;
    }
    /**
     * Register the unmarshalling information so that it can 
     * be used to speed up subsequent marshalling events.
     * @param mc
     * @param packages
     * @param packagesKey
     */
    static void registerUnmarshalInfo(MessageContext mc, 
                                 TreeSet<String> packages, 
                                 String packagesKey) throws AxisFault {
        
        // The information is registered on the AxisOperation.
        if (mc == null ||
            mc.getAxisMessageContext() == null ||
            mc.getAxisMessageContext().getAxisService() == null ||
            mc.getAxisMessageContext().getAxisOperation() == null) {
            return;
        }
        
        // This needs to be stored on the AxisOperation as unmarshalling
        // info will be specific to a method and its parameters
        AxisOperation axisOp = mc.getAxisMessageContext().getAxisOperation();
        
        // There are two things that need to be saved.
        // 1) The UnmarshalInfo object containing the packages 
        //    (which will be used by the CustomBuilder)
        // 2) A MessageContextListener which (when triggered) registers
        //    the JAXBCustomBuilder
        Parameter param = axisOp.getParameter(UnmarshalInfo.KEY);
        if (param == null) {
            UnmarshalInfo info = new UnmarshalInfo(packages, packagesKey);
            axisOp.addParameter(UnmarshalInfo.KEY, info);
            param = axisOp.getParameter(UnmarshalInfo.KEY);
            param.setTransient(true);
            // Add a listener that will set the JAXBCustomBuilder
            UnmarshalMessageContextListener.
                create(mc.getAxisMessageContext().getServiceContext());
        }
    }
}
