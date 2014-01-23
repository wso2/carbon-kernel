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

package org.apache.axis2.jaxws.message.impl;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLPart;
import org.apache.axis2.jaxws.message.attachments.AttachmentUtils;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLPartFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MessageImpl
 * A Message is an XML part + Attachments.
 * Most of the implementation delegates to the XMLPart implementation.
 * 
 * NOTE: For XML/HTTP (REST), a SOAP 1.1. Envelope is built and the rest payload is placed
 * in the body.  This purposely mimics the Axis2 implementation.
 */
public class MessageImpl implements Message {
    private static final Log log = LogFactory.getLog(MessageImpl.class);
    
    Protocol protocol = Protocol.unknown; // the protocol, defaults to unknown
    XMLPart xmlPart = null; // the representation of the xmlpart
    
    boolean mtomEnabled;
    
    // The transport headers are stored in a Map, which is the 
    // same data representation used by the Axis2 MessageContext (TRANSPORT_HEADERS).
    private Map transportHeaders = null; 
    
    // The Message is connected to a MessageContext.
    // Prior to that connection, attachments are stored locally
    // After the connection, attachments are obtained from the MessageContext
    Attachments attachments = new Attachments();  // Local Attachments
    private MessageContext messageContext;
    
    // Set after we have past the pivot point when the message is consumed
    private boolean postPivot = false;
    private boolean doingSWA = false;
    
    /**
     * MessageImpl should be constructed via the MessageFactory.
     * This constructor constructs an empty message with the specified protocol
     * @param protocol
     */
    MessageImpl(Protocol protocol) throws WebServiceException, XMLStreamException {
        createXMLPart(protocol);
    }
    
    /**
     * Message is constructed by the MessageFactory.
     * This constructor creates a message from the specified root.
     * @param root
     * @param protocol or null
     */
    MessageImpl(OMElement root, Protocol protocol) 
    throws WebServiceException, XMLStreamException  {
        createXMLPart(root, protocol);
    }
    
    /**
     * Message is constructed by the MessageFactory.
     * This constructor creates a message from the specified root.
     * @param root
     * @param protocol or null
     */
    MessageImpl(SOAPEnvelope root) throws WebServiceException, XMLStreamException  {
        createXMLPart(root);
    }
    
    /**
     * Create a new XMLPart and Protocol from the root
     * @param root SOAPEnvelope
     * @throws WebServiceException
     * @throws XMLStreamException
     */
    private void createXMLPart(SOAPEnvelope root) throws WebServiceException, XMLStreamException {
        XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
        xmlPart = factory.createFrom(root);
        this.protocol = xmlPart.getProtocol();
        xmlPart.setParent(this); 
    }
    
    /**
     * Create a new XMLPart and Protocol from the root
     * @param root OMElement
     * @throws WebServiceException
     * @throws XMLStreamException
     */
    private void createXMLPart(OMElement root, Protocol protocol) 
    throws WebServiceException, XMLStreamException {
        XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
        xmlPart = factory.createFrom(root, protocol);
        this.protocol = xmlPart.getProtocol();
        xmlPart.setParent(this);
    }
    
    /**
     * Create a new empty XMLPart from the Protocol
     * @param protocol
     * @throws WebServiceException
     * @throws XMLStreamException
     */
    private void createXMLPart(Protocol protocol) throws WebServiceException, XMLStreamException {
        this.protocol = protocol;
        if (protocol.equals(Protocol.unknown)) {
            throw ExceptionFactory.
            makeWebServiceException(Messages.getMessage("ProtocolIsNotKnown"));
        } 
        XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
        xmlPart = factory.create(protocol);
        xmlPart.setParent(this);
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#getAsSOAPMessage()
     */
    public SOAPMessage getAsSOAPMessage() throws WebServiceException {
        
        // TODO: 
        // This is a non performant way to create SOAPMessage. I will serialize
        // the xmlpart content and then create an InputStream of byte.
        // Finally create SOAPMessage using this InputStream.
        // The real solution may involve using non-spec, implementation
        // constructors to create a Message from an Envelope
        try {
            if (log.isDebugEnabled()) {
                log.debug("start getAsSOAPMessage");
            }
            // Get OMElement from XMLPart.
            OMElement element = xmlPart.getAsOMElement();
            
            // Get the namespace so that we can determine SOAP11 or SOAP12
            OMNamespace ns = element.getNamespace();
            
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            element.serialize(outStream);
            
            // In some cases (usually inbound) the builder will not be closed after
            // serialization.  In that case it should be closed manually.
            if (element.getBuilder() != null && !element.getBuilder().isCompleted()) {
                element.close(false);
            }
            
            byte[] bytes = outStream.toByteArray();
            
            if (log.isDebugEnabled()) {
                String text = new String(bytes);
                log.debug("  inputstream = " + text);
            }
            
            // Create InputStream
            ByteArrayInputStream inStream = new ByteArrayInputStream(bytes);
            
            // Create MessageFactory that supports the version of SOAP in the om element
            MessageFactory mf = getSAAJConverter().createMessageFactory(ns.getNamespaceURI());
            
            // Create soapMessage object from Message Factory using the input
            // stream created from OM.
            
            // Get the MimeHeaders from the transportHeaders map
            MimeHeaders defaultHeaders = new MimeHeaders();
            if (transportHeaders != null) {
                Iterator it = transportHeaders.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String key = (String) entry.getKey();
                    if (entry.getValue() == null) {
                        // This is not necessarily a problem; log it and make sure not to NPE
                        if (log.isDebugEnabled()) {
                            log.debug("  Not added to transport header. header =" + key + 
                                      " because value is null;");
                        }
                    }
                    else if (entry.getValue() instanceof String) {
                        // Normally there is one value per key
                        if (log.isDebugEnabled()) {
                            log.debug("  add transport header. header =" + key + 
                                      " value = " + entry.getValue());
                        }
                        defaultHeaders.addHeader(key, (String) entry.getValue());
                    } else {
                        // There may be multiple values for each key.  This code
                        // assumes the value is an array of String.
                        String values[] = (String[]) entry.getValue();
                        for (int i=0; i<values.length; i++) {
                            if (log.isDebugEnabled()) {
                                log.debug("  add transport header. header =" + key + 
                                          " value = " + values[i]);
                            }
                            defaultHeaders.addHeader(key, values[i]);
                        }
                    }
                }
            }
            
            // Toggle based on SOAP 1.1 or SOAP 1.2
            String contentType = null;
            if (ns.getNamespaceURI().equals(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
                contentType = SOAPConstants.SOAP_1_1_CONTENT_TYPE;
            } else {
                contentType = SOAPConstants.SOAP_1_2_CONTENT_TYPE;
            }
            
            // Override the content-type
            String ctValue = contentType +"; charset=UTF-8";
            defaultHeaders.setHeader("Content-type", ctValue);
            if (log.isDebugEnabled()) {
                log.debug("  setContentType =" + ctValue);
            }
            SOAPMessage soapMessage = mf.createMessage(defaultHeaders, inStream);
            
            // At this point the XMLPart is still an OMElement.  
            // We need to change it to the new SOAPEnvelope.
            createXMLPart(soapMessage.getSOAPPart().getEnvelope());
            
            // If axiom read the message from the input stream, 
            // then one of the attachments is a SOAPPart.  Ignore this attachment
            String soapPartContentID = getSOAPPartContentID();  // This may be null
            
            if (log.isDebugEnabled()) {
                log.debug("  soapPartContentID =" + soapPartContentID);
            }
            
            List<String> dontCopy = new ArrayList<String>();
            if (soapPartContentID != null) {
                dontCopy.add(soapPartContentID);
            }
            
            // Add any new attachments from the SOAPMessage to this Message
            Iterator it = soapMessage.getAttachments();
            while (it.hasNext()) {
                
                AttachmentPart ap = (AttachmentPart) it.next();
                String cid = ap.getContentId();
                if (log.isDebugEnabled()) {
                    log.debug("  add SOAPMessage attachment to Message.  cid = " + cid);
                }
                addDataHandler(ap.getDataHandler(),  cid);
                dontCopy.add(cid);
            }
            
            // Add the attachments from this Message to the SOAPMessage
            for (String cid:getAttachmentIDs()) {
                DataHandler dh = attachments.getDataHandler(cid);
                if (!dontCopy.contains(cid)) {
                    if (log.isDebugEnabled()) {
                        log.debug("  add Message attachment to SoapMessage.  cid = " + cid);
                    }
                    AttachmentPart ap = MessageUtils.createAttachmentPart(cid, dh, soapMessage);
                    soapMessage.addAttachmentPart(ap);
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("  The SOAPMessage has the following attachments");
                Iterator it2 = soapMessage.getAttachments();
                while (it2.hasNext()) {
                    AttachmentPart ap = (AttachmentPart) it2.next();
                    log.debug("    AttachmentPart cid=" + ap.getContentId());
                    log.debug("        contentType =" + ap.getContentType());
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("end getAsSOAPMessage");
            }
            return soapMessage;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
    }
    
    /**
     * Get the indicated (non-soap part) attachment id
     * @param index
     * @return CID or null if not present
     */
    public String getAttachmentID(int index) {
        List<String> cids = getAttachmentIDs();
        String spCID = getSOAPPartContentID();
        if (log.isDebugEnabled()) {
            log.debug("getAttachmentID for index =" + index);
            for (int i = 0; i < cids.size(); i++) {
                log.debug("Attachment CID (" + i + ") = " + cids.indexOf(i));
            }
            log.debug("The SOAP Part CID is ignored.  It's CID is (" + spCID + ")");
        }
        int spIndex = (spCID == null) ? -1 : cids.indexOf(spCID);
        
        // Bump index so we don't consider the soap part
        index = (spIndex != -1 && spIndex <= index) ? index + 1 : index;
        
        // Return the content id at the calculated index
        String resultCID = null;
        if (index < cids.size()) {
            resultCID = cids.get(index);
        }
        if (log.isDebugEnabled()) {
            log.debug("Returning CID=" + resultCID);
         }
        return resultCID;
    }
    

    public String getAttachmentID(String partName) {
        // Find the prefix that starts with the 
        // partName=
        String prefix = partName + "=";
        List<String> cids = getAttachmentIDs();
        for (String cid: cids) {
            if (cid.startsWith(prefix)) {
                return cid;
            }
        }
        return null;
    }

    
    private String getSOAPPartContentID() {
        String contentID = null;
        if (messageContext == null) {
            return null;  // Attachments set up programmatically...so there is no SOAPPart
        }
        try {
            contentID = attachments.getSOAPPartContentID();
        } catch (RuntimeException e) {
            // OM will kindly throw an OMException or NPE if the attachments is set up 
            // programmatically. 
            return null;
        }
        return contentID;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#getValue(java.lang.Object, 
     * org.apache.axis2.jaxws.message.factory.BlockFactory)
     */
    public Object getValue(Object context, BlockFactory blockFactory) throws WebServiceException {
        try {
            Object value = null;
            if (protocol == Protocol.rest) {
                // The implementation of rest stores the rest xml inside a dummy soap 1.1 envelope.
                // So use the get body block logic.
                Block block = xmlPart.getBodyBlock(context, blockFactory);
                if (block != null) {
                    value = block.getBusinessObject(true);
                }
                
            } else {
                // Must be SOAP
                if (blockFactory instanceof SOAPEnvelopeBlockFactory) {
                    value = getAsSOAPMessage();
                } else {
                    // TODO: This doesn't seem right to me.
                    // We should not have an intermediate StringBlock.
                    // This is not performant. Scheu
                    OMElement messageOM = getAsOMElement();
                    String stringValue = messageOM.toString();
                    String soapNS =
                            (protocol == Protocol.soap11) ? SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE
                                    : SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE;
                    QName soapEnvQname = new QName(soapNS, "Envelope");


                    XMLStringBlockFactory stringFactory =
                            (XMLStringBlockFactory) 
                            FactoryRegistry.getFactory(XMLStringBlockFactory.class);
                    Block stringBlock = stringFactory.createFrom(stringValue, null, soapEnvQname);
                    Block block = blockFactory.createFrom(stringBlock, context);
                    value = block.getBusinessObject(true);
                }
            }
            return value;
        } catch (Throwable e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#getAttachmentIDs()
     */
    public List<String> getAttachmentIDs() {
        return attachments.getContentIDList();
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#getDataHandler(java.lang.String)
     */
    public DataHandler getDataHandler(String cid) {
        // if null DH was specified explicitly, just return
        if(cid == null) {
            return (DataHandler) null;
        }
        String bcid = getBlobCID(cid);
        return attachments.getDataHandler(bcid);
    }
    
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#removeDataHandler(java.lang.String)
     */
    public DataHandler removeDataHandler(String cid) {
        String bcid = getBlobCID(cid);
        DataHandler dh = attachments.getDataHandler(bcid);
        attachments.removeDataHandler(bcid);
        return dh;
    }
    
    private String getBlobCID(String cid) {
        String blobCID = cid;
        if (cid.startsWith("cid:")) {
            blobCID = cid.substring(4);  // Skip over cid:
        }
        return blobCID;
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.XMLPart#getProtocol()
     */
    public Protocol getProtocol() {
        return protocol;
    }
    
    
    public OMElement getAsOMElement() throws WebServiceException {
        return xmlPart.getAsOMElement();
    }
    
    public javax.xml.soap.SOAPEnvelope getAsSOAPEnvelope() throws WebServiceException {
        return xmlPart.getAsSOAPEnvelope();
    }
    
    public Block getBodyBlock(int index, Object context, BlockFactory blockFactory) 
    throws WebServiceException {
        return xmlPart.getBodyBlock(index, context, blockFactory);
    }
    
    public Block getHeaderBlock(String namespace, String localPart, Object context, 
                                BlockFactory blockFactory) 
    throws WebServiceException {
        return xmlPart.getHeaderBlock(namespace, localPart, context, blockFactory);
    }
    
    public List<Block> getHeaderBlocks(String namespace, String localPart, 
                                       Object context, BlockFactory blockFactory, 
                                       RolePlayer rolePlayer) throws WebServiceException {
        return xmlPart.getHeaderBlocks(namespace, localPart, context, blockFactory, rolePlayer);
    }

    public int getNumBodyBlocks() throws WebServiceException {
        return xmlPart.getNumBodyBlocks();
    }
    
    public List<QName> getBodyBlockQNames() throws WebServiceException {
        return xmlPart.getBodyBlockQNames();
    }
    
    public int getNumHeaderBlocks() throws WebServiceException {
        return xmlPart.getNumHeaderBlocks();
    }
    
    public XMLStreamReader getXMLStreamReader(boolean consume) 
    throws WebServiceException {
        return xmlPart.getXMLStreamReader(consume);
    }
    
    public boolean isConsumed() {
        return xmlPart.isConsumed();
    }
    
    public void outputTo(XMLStreamWriter writer, boolean consume) 
    throws XMLStreamException, WebServiceException {
        xmlPart.outputTo(writer, consume);
    }
    
    public void removeBodyBlock(int index) throws WebServiceException {
        xmlPart.removeBodyBlock(index);
    }
    
    public void removeHeaderBlock(String namespace, String localPart) 
    throws WebServiceException {
        xmlPart.removeHeaderBlock(namespace, localPart);
    }
    
    public void setBodyBlock(int index, Block block) 
    throws WebServiceException {
        xmlPart.setBodyBlock(index, block);
    }
    
    public void setHeaderBlock(String namespace, String localPart, Block block) 
    throws WebServiceException {
        xmlPart.setHeaderBlock(namespace, localPart, block);
    }
    
    public void appendHeaderBlock(String namespace, String localPart, Block block) 
    throws WebServiceException {
        xmlPart.appendHeaderBlock(namespace, localPart, block);
    }
    
    public String traceString(String indent) {
        return xmlPart.traceString(indent);
    }
    
    /**
     * Load the SAAJConverter
     * @return SAAJConverter
     */
    SAAJConverter converter = null;
    private SAAJConverter getSAAJConverter() {
        if (converter == null) {
            SAAJConverterFactory factory = (
                    SAAJConverterFactory)FactoryRegistry.getFactory(SAAJConverterFactory.class);
            converter = factory.getSAAJConverter();
        }
        return converter;
    }
    
    public void addDataHandler(DataHandler dh, String id) {
        if (id.startsWith("<")  && id.endsWith(">")) {
            id = id.substring(1, id.length()-1);
        }
        attachments.addDataHandler(id, dh);
    }
    
    public Message getParent() {
        return null;
    }
    
    public void setParent(Message msg) { 
        // A Message does not have a parent
        throw new UnsupportedOperationException();
    }
    
    /**
     * @return true if the binding for this message indicates mtom
     */
    public boolean isMTOMEnabled() {
        // If the message has SWA attachments, this "wins" over the mtom setting.
        return mtomEnabled && !doingSWA;
    }
    
    /**
     * @param true if the binding for this message indicates mtom
     */
    public void setMTOMEnabled(boolean b) {
        mtomEnabled = b;
    }
    
    public XMLFault getXMLFault() throws WebServiceException {
        return xmlPart.getXMLFault();
    }
    
    public void setXMLFault(XMLFault xmlFault) throws WebServiceException {
        xmlPart.setXMLFault(xmlFault);
    }
    
    public boolean isFault() throws WebServiceException {
        return xmlPart.isFault();
    }
    
    public String getXMLPartContentType() {
        return xmlPart.getXMLPartContentType();
    }
    
    public Style getStyle() {
        return xmlPart.getStyle();
    }
    
    public void setStyle(Style style) throws WebServiceException {
        xmlPart.setStyle(style);
    }
    
    public QName getOperationElement() throws WebServiceException {
        return xmlPart.getOperationElement();
    }
    
    public void setOperationElement(QName operationQName) throws WebServiceException {
        xmlPart.setOperationElement(operationQName);
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Attachment#getMimeHeaders()
     */
    public Map getMimeHeaders() {
        // Lazily create transport headers.
        if (transportHeaders == null) {
            transportHeaders = new HashMap();
        }
        return transportHeaders;
     }
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Attachment#setMimeHeaders(java.util.Map)
     */
    public void setMimeHeaders(Map map) {
        transportHeaders = map;
        if (transportHeaders == null) {
            transportHeaders = new HashMap();
          }
    }
    
    public Block getBodyBlock(Object context, BlockFactory blockFactory) 
    throws WebServiceException {
        return xmlPart.getBodyBlock(context, blockFactory);
    }
    
    public void setBodyBlock(Block block) throws WebServiceException {
        xmlPart.setBodyBlock(block);
    }
    
    public void setPostPivot() {
        this.postPivot = true;
    }
    
    public boolean isPostPivot() {
        return postPivot;
    }
    
    public int getIndirection() {
        return xmlPart.getIndirection();
    }
    
    public void setIndirection(int indirection) {
        xmlPart.setIndirection(indirection);
    }
    
    public MessageContext getMessageContext() {
        return messageContext;
    }
    
    public void setMessageContext(MessageContext messageContext) {
        if (this.messageContext != messageContext) {
            // Copy attachments to the new map
            Attachments newMap = messageContext.getAxisMessageContext().getAttachmentMap();
            Attachments oldMap = attachments;
            for (String cid:oldMap.getAllContentIDs()) {
                DataHandler dh = oldMap.getDataHandler(cid);
                if (dh != null) {
                    newMap.addDataHandler(cid, dh);
                }
            }
            // If not MTOM and there are attachments, set SWA style
            if (!isMTOMEnabled()) {
                String[] cids = newMap.getAllContentIDs();
                if (cids.length > 0) {
                    messageContext.setProperty(Configuration.ENABLE_SWA, "true");
                }
            }
            if (log.isDebugEnabled()) {
                for (String cid:newMap.getAllContentIDs()) {
                    log.debug("Message has an attachment with content id= " + cid);
                }
            }
            attachments = newMap;
        }
        
        // Check for cached attachment file(s) if attachments exist.
        if(attachments != null && !messageContext.getAxisMessageContext().isServerSide()){
        	AttachmentUtils.findCachedAttachment(attachments);
        }
        
        this.messageContext = messageContext;
    }
    public void setDoingSWA(boolean value) {
        doingSWA = value;
    }

    public boolean isDoingSWA() {
        return doingSWA;
    }
    
    public void close() {
        if (xmlPart != null) {
            xmlPart.close();
        }
    }

    public Set<QName> getHeaderQNames() {
        return (xmlPart == null) ? null : xmlPart.getHeaderQNames();     
    }

}
