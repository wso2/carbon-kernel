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

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.OMContainerEx;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.OMBlockFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * XMLSpineImpl
 * <p/>
 * An XMLSpine consists is an OMEnvelope (either a default one or one create from an incoming
 * message). As Blocks are added or requested, they are placed in the tree as OMSourcedElements.
 * <p/>
 * NOTE: For XML/HTTP (REST) messages, a SOAP 1.1 envelope is built and the xml payload is 
 * placed in the body.  This purposely mimics the implementation used by Axis2.
 */
class XMLSpineImpl implements XMLSpine {

    private static Log log = LogFactory.getLog(XMLSpineImpl.class);
    private static OMBlockFactory obf =
            (OMBlockFactory)FactoryRegistry.getFactory(OMBlockFactory.class);

    private Protocol protocol = Protocol.unknown;
    private Style style = Style.DOCUMENT;
    private int indirection = 0;
    private SOAPEnvelope root = null;
    private SOAPFactory soapFactory = null;

    private boolean consumed = false;
    private Message parent = null;

    /**
     * Create a lightweight representation of this protocol (i.e. the Envelope, Header and Body)
     *
     * @param protocol       Protocol
     * @param style          Style
     * @param indirection    (0 or 1) indicates location of body blocks
     * @param initialPayload (OMElement or null...used to add rest payload)
     */
    public XMLSpineImpl(Protocol protocol, Style style, int indirection, OMElement payload) {
        super();
        this.protocol = protocol;
        this.style = style;
        this.indirection = indirection;
        soapFactory = _getFactory(protocol);
        root = _createEmptyEnvelope(style, soapFactory);
        if (payload != null) {
            ((SOAPEnvelope)root).getBody().addChild(payload);
        }
    }

    /**
     * Create spine from an existing OM tree
     *
     * @param envelope
     * @param style       Style
     * @param indirection (0 or 1) indicates location of body blocks
     * @throws WebServiceException
     */
    public XMLSpineImpl(SOAPEnvelope envelope, Style style, int indirection, Protocol protocol)
            throws WebServiceException {
        super();
        this.style = style;
        this.indirection = indirection;
        this.protocol = protocol;
        init(envelope);
        // If null, detect protocol from soap namespace
        if (protocol == null) {
            if (root.getNamespace().getNamespaceURI()
                    .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                this.protocol = Protocol.soap11;
            } else if (root.getNamespace().getNamespaceURI()
                    .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                this.protocol = Protocol.soap12;
            }
        }
    }

    /**
     * @param envelope
     * @throws WebServiceException
     */
    private void init(SOAPEnvelope envelope) throws WebServiceException {
        root = envelope;
        soapFactory = MessageUtils.getSOAPFactory(root);

        // Advance past the header
        SOAPHeader header = root.getHeader();
        if (header == null) {
            header = soapFactory.createSOAPHeader(root);
        }

        // Now advance the parser to the body element
        SOAPBody body = root.getBody();
        if (body == null) {
            // Create the body if one does not exist
            body = soapFactory.createSOAPBody(root);
        }
    }


    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.XMLPart#getProtocol()
      */
    public Protocol getProtocol() {
        return protocol;
    }

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.XMLPart#getParent()
    */
    public Message getParent() {
        return parent;
    }

    /*
    * Set the backpointer to this XMLPart's parent Message
    */
    public void setParent(Message p) {
        parent = p;
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.XMLPart#outputTo(javax.xml.stream.XMLStreamWriter, 
      * boolean)
      */
    public void outputTo(XMLStreamWriter writer, boolean consume)
            throws XMLStreamException, WebServiceException {
        Reader2Writer r2w = new Reader2Writer(getXMLStreamReader(consume));
        r2w.outputTo(writer);
    }

    public XMLStreamReader getXMLStreamReader(boolean consume) throws WebServiceException {
        if (consume) {
            if (root.getBuilder() != null && !root.getBuilder().isCompleted()) {
                return root.getXMLStreamReaderWithoutCaching();
            } else {
                return root.getXMLStreamReader();
            }
        } else {
            return root.getXMLStreamReader();
        }
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.impl.XMLSpine#getXMLFault()
      */
    public XMLFault getXMLFault() throws WebServiceException {
        if (!isFault()) {
            return null;
        }

        // Advance through all of the detail blocks
        int numDetailBlocks = getNumDetailBlocks();

        Block[] blocks = null;
        if (numDetailBlocks > 0) {
            blocks = new Block[numDetailBlocks];
            SOAPFaultDetail detail = root.getBody().getFault().getDetail();
            for (int i = 0; i < numDetailBlocks; i++) {
                OMElement om = this._getChildOMElement(detail, i);
                blocks[i] = this._getBlockFromOMElement(om, null, obf, false);

            }
        }

        XMLFault xmlFault = XMLFaultUtils.createXMLFault(root.getBody().getFault(), blocks);
        return xmlFault;
    }

    private int getNumDetailBlocks() throws WebServiceException {
        if (isFault()) {
            SOAPFault fault = root.getBody().getFault();
            return _getNumChildElements(fault.getDetail());
        }
        return 0;
    }

    public void setXMLFault(XMLFault xmlFault) throws WebServiceException {

        // Clear out the existing body and detail blocks
        SOAPBody body = root.getBody();
        getNumDetailBlocks(); // Forces parse of existing detail blocks
        getNumBodyBlocks();  // Forces parse over body
        OMNode child = body.getFirstOMChild();
        while (child != null) {
            child.detach();
            child = body.getFirstOMChild();
        }

        // Add a SOAPFault to the body.
        SOAPFault soapFault = XMLFaultUtils.createSOAPFault(xmlFault, body, false);
    }

    public boolean isConsumed() {
        return consumed;
    }

    public OMElement getAsOMElement() throws WebServiceException {
        return root;
    }


    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.XMLPart#getNumBodyBlocks()
      */
    public int getNumBodyBlocks() throws WebServiceException {
        return _getNumChildElements(_getBodyBlockParent());
    }
    
    /**
     * getBodyBlockQNames 
     * Calling this method will cache the OM.  Avoid it in performant situations.
     *
     * @return List of QNames
     * @throws WebServiceException
     */
    public List<QName> getBodyBlockQNames() throws WebServiceException {
        int numBlocks = getNumBodyBlocks();
        List<QName> qNames = new ArrayList<QName>();
        
        for (int i =0; i< numBlocks; i++ ) {
            OMElement omElement = _getChildOMElement(_getBodyBlockParent(), i);
            if (omElement != null) {
                qNames.add(omElement.getQName());
            } 
        }
        return qNames;
    }


    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.impl.XMLSpine#getBodyBlock(int, java.lang.Object, 
      * org.apache.axis2.jaxws.message.factory.BlockFactory)
      */
    public Block getBodyBlock(int index, Object context, BlockFactory blockFactory)
            throws WebServiceException {

        if (log.isDebugEnabled()) {
            log.debug("getBodyBlock: Get the " + index + "block using the block factory, " +
                    blockFactory);
        }

        // Forces the parser to read all of the blocks
        getNumBodyBlocks();

        // Get the indicated block
        OMElement omElement = _getChildOMElement(_getBodyBlockParent(), index);
        if (omElement == null) {
            // Null indicates that no block is available
            if (log.isDebugEnabled()) {
                log.debug("getBodyBlock: The block was not found ");
            }
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("getBodyBlock: Found omElement " + omElement.getQName());
        }
        return this._getBlockFromOMElement(omElement, context, blockFactory, false);
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.impl.XMLSpine#getBodyBlock(int, java.lang.Object, 
    * org.apache.axis2.jaxws.message.factory.BlockFactory)
    */
    public Block getBodyBlock(Object context, BlockFactory blockFactory)
            throws WebServiceException {

        if (log.isDebugEnabled()) {
            log.debug("getBodyBlock PERFORMANT: Get the block using the block factory, " +
                    blockFactory);
        }

        // TODO Need to upgrade the code to get Blocks that represent text and elements.

        // Calling getBodyBlock assumes that there is only one or zero body blocks in the message.
        // Subsequent Blocks are lost.  If the caller needs access to multiple body blocks, 
        // then getBodyBlocks(index,...) should be used

        // Get the indicated block
        OMElement omElement = _getChildOMElement(_getBodyBlockParent(), 0);
        if (omElement == null) {
            // Null indicates that no block is available
            if (log.isDebugEnabled()) {
                log.debug("getBodyBlock: The block was not found ");
            }
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("getBodyBlock: Found omElement " + omElement.getQName());
        }
        return this._getBlockFromOMElement(omElement, context, blockFactory, true);
    }

    public void setBodyBlock(int index, Block block) throws WebServiceException {

        // Forces the parser to read all of the blocks
        getNumBodyBlocks();

        block.setParent(getParent());
        OMElement bElement = _getBodyBlockParent();
        OMElement om = this._getChildOMElement(bElement, index);

        // The block is supposed to represent a single element.  
        // But if it does not represent an element , the following will fail.
        QName qName = block.getQName();
        OMNamespace ns = soapFactory.createOMNamespace(qName.getNamespaceURI(), 
                                                       qName.getPrefix());

        OMElement newOM = _createOMElementFromBlock(qName.getLocalPart(), ns, block, 
                                                    soapFactory, false);
        if (om == null) {
            bElement.addChild(newOM);
        } else {
            om.insertSiblingBefore(newOM);
            om.detach();
        }
    }

    public void setBodyBlock(Block block) throws WebServiceException {

        // Forces the parser to read all of the blocks
        getNumBodyBlocks();

        block.setParent(getParent());

        // Remove all of the children
        OMElement bElement = _getBodyBlockParent();
        Iterator it = bElement.getChildren();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }

        if (block.isElementData()) {
            // If the block is element data then  it is safe to create
            // an OMElement representing the block

            // The block is supposed to represent a single element.  
            // But if it does not represent an element , the following will fail.
            QName qName = block.getQName();
            OMNamespace ns = soapFactory.createOMNamespace(qName.getNamespaceURI(), 
                                                           qName.getPrefix());

            OMElement newOM = _createOMElementFromBlock(qName.getLocalPart(), ns,
                                                        block, soapFactory, false);
            bElement.addChild(newOM);
        } else {
            // This needs to be fixed, but for now we will require that there must be an 
            // element...otherwise no block is added
            try {
                QName qName = block.getQName();
                OMNamespace ns = soapFactory.createOMNamespace(qName.getNamespaceURI(), 
                                                               qName.getPrefix());

                OMElement newOM = _createOMElementFromBlock(qName.getLocalPart(), ns,
                                                            block, soapFactory, false);
                bElement.addChild(newOM);
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "An attempt was made to pass a Source or String that does not " +
                            "have an xml element. Processing continues with an empty payload.");
                }
            }
        }
    }

    public void removeBodyBlock(int index) throws WebServiceException {
        // Forces the parser to read all of the blocks
        getNumBodyBlocks();

        OMElement om = this._getChildOMElement(_getBodyBlockParent(), index);
        if (om != null) {
            om.detach();
        }
    }

    public int getNumHeaderBlocks() throws WebServiceException {
        return _getNumChildElements(root.getHeader());
    }

    public Block getHeaderBlock(String namespace, String localPart, Object context,
                                BlockFactory blockFactory) throws WebServiceException {
        OMElement om = _getChildOMElement(root.getHeader(), namespace, localPart);
        if (om == null) {
            return null;
        }
        return this._getBlockFromOMElement(om, context, blockFactory, false);
    }

    public List<Block> getHeaderBlocks(String namespace, 
                                       String localPart, 
                                       Object context, 
                                       BlockFactory blockFactory, 
                                       RolePlayer rolePlayer) throws WebServiceException {
        List<Block> blocks = new ArrayList<Block>();
        
        // Get the list of OMElements that have the same header name
        SOAPHeader header = root.getHeader();
        if (header == null) {
            return blocks;
        }
        
        // Get an iterator over the headers that have a acceptable role
        Iterator it = null;
        if (rolePlayer == null) {
            it = header.getChildElements();
        } else {
            it = header.getHeadersToProcess(rolePlayer);
        }
        while (it.hasNext()) {
            OMElement om = (OMElement) it.next();
            // Create a block out of each header that matches 
            // the requested namespace/localPart
            if (om.getNamespace().getNamespaceURI().equals(namespace) &&
                om.getLocalName().equals(localPart)) {
                Block block = _getBlockFromOMElement(om, context, blockFactory, false);
                blocks.add(block);
            }
        }
        return blocks;
    }

    public Set<QName> getHeaderQNames() {
        HashSet<QName> qnames = new HashSet<QName>();
        SOAPHeader header = root.getHeader();
        if (header != null) {
            Iterator it = header.getChildElements(); 
            while (it != null && it.hasNext()) {
                Object node = it.next();
                if (node instanceof OMElement) {
                    qnames.add(((OMElement) node).getQName());
                }
            }
        }
        return qnames;
    }
    
    public void setHeaderBlock(String namespace, String localPart, Block block)
            throws WebServiceException {
        block.setParent(getParent());
        OMNamespace ns = soapFactory.createOMNamespace(namespace, null);
        OMElement newOM =
                _createOMElementFromBlock(localPart, ns, block, soapFactory, true);
        OMElement om = this._getChildOMElement(root.getHeader(), namespace, localPart);
        if (om == null) {
            if (root.getHeader() == null) {
                soapFactory.createSOAPHeader(root);
            }
            root.getHeader().addChild(newOM);
        } else {
            om.insertSiblingBefore(newOM);
            om.detach();
        }
    }
    
    public void appendHeaderBlock(String namespace, String localPart, Block block)
    throws WebServiceException {
        block.setParent(getParent());
        OMNamespace ns = soapFactory.createOMNamespace(namespace, null);
        OMElement newOM =
            _createOMElementFromBlock(localPart, ns, block, soapFactory, true);
        if (root.getHeader() == null) {
            soapFactory.createSOAPHeader(root);
        }
        root.getHeader().addChild(newOM);
    }


    public void removeHeaderBlock(String namespace, String localPart) throws WebServiceException {
        OMElement om = this._getChildOMElement(root.getHeader(), namespace, localPart);
        while (om != null) {
            om.detach();
            om = this._getChildOMElement(root.getHeader(), namespace, localPart);
        }
    }

    public String traceString(String indent) {
        // TODO Trace String Support
        return null;
    }

    public String getXMLPartContentType() {
        return "SPINE";
    }

    public boolean isFault() throws WebServiceException {
        return XMLFaultUtils.isFault(root);
    }

    public Style getStyle() {
        return style;
    }

    public QName getOperationElement() {
        OMElement omElement = this._getBodyBlockParent();
        if (omElement instanceof SOAPBody) {
            return null;
        } else {
            return omElement.getQName();
        }
    }

    public void setOperationElement(QName operationQName) {
        OMElement opElement = this._getBodyBlockParent();
        if (!(opElement instanceof SOAPBody)) {
            OMNamespace ns = soapFactory.createOMNamespace(operationQName.getNamespaceURI(),
                                                           operationQName.getPrefix());
            opElement.setLocalName(operationQName.getLocalPart());
            opElement.setNamespace(ns);
            
            // Necessary to avoid duplicate namespaces later.
            opElement.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        }
    }

    private Block _getBlockFromOMElement(OMElement om, Object context, BlockFactory blockFactory,
                                         boolean setComplete) throws WebServiceException {
        try {
            QName qName = om.getQName();
            OMNamespace ns = om.getNamespace();
            
            // Save the ROLE
            // TODO Need to do the same for RELAY and MUSTUNDERSTAND
            String role = null;
            if (om instanceof SOAPHeaderBlock) {
                role = ((SOAPHeaderBlock)om).getRole();
            }
            
            /* TODO We could gain performance if OMSourcedElement exposed a getDataSource method 
             if (om instanceof OMSourcedElementImpl &&
             ((OMSourcedElementImpl) om).getDataSource() instanceof Block) {
             Block oldBlock = (Block) ((OMSourcedElementImpl) om).getDataSource();
             Block newBlock = blockFactory.createFrom(oldBlock, context);
             newBlock.setParent(getParent());
             if (newBlock != oldBlock) {
             // Replace the OMElement with the OMSourcedElement that delegates to the block
              OMSourcedElementImpl newOM = new OMSourcedElementImpl(qName, soapFactory, newBlock);
              om.insertSiblingBefore(newOM);
              om.detach();
              }
              return newBlock;
              } 
              */

            // Create the block
            Block block = blockFactory.createFrom(om, context, qName);
            block.setParent(getParent());
            if (om instanceof SOAPHeaderBlock) {
                block.setProperty(SOAPHeaderBlock.ROLE_PROPERTY, role);
            }

            // Get the business object to force a parse
            block.getBusinessObject(false);

            // Replace the OMElement with the OMSourcedElement that delegates to the block
            OMElement newOM = _createOMElementFromBlock(qName.getLocalPart(), ns, block, soapFactory, 
                                                            (om.getParent() instanceof SOAPHeader));
            om.insertSiblingBefore(newOM);

            // We want to set the om element and its parents to complete to 
            // shutdown the parsing.  
            if (setComplete) {
                
                // Get the root of the document
                OMElement root = om;
                while(root.getParent() instanceof OMElement) {
                    root = (OMElement) root.getParent();
                }
                
                try {   
                    if (!root.isComplete() && root.getBuilder() != null && 
                            !root.getBuilder().isCompleted()) {
                        // Forward the parser to the end so it will close
                        while (root.getBuilder().next() != XMLStreamConstants.END_DOCUMENT) {
                            //do nothing
                        }                    
                    }
                } catch (Exception e) {
                    // Log and continue
                    if (log.isDebugEnabled()) {
                        log.debug("Builder next error:" + e.getMessage());
                        log.trace(JavaUtils.stackToString(e));
                    }
                    
                }
                

                OMContainer o = om;
                while (o != null && o instanceof OMContainerEx) {
                    ((OMContainerEx)o).setComplete(true);
                    if ((o instanceof OMNode) &&
                            (((OMNode)o).getParent()) instanceof OMContainer) {
                        o = ((OMNode)o).getParent();
                    } else {
                        o = null;
                    }
                }
            }


            om.detach();
            return block;
        } catch (XMLStreamException xse) {
            throw ExceptionFactory.makeWebServiceException(xse);
        }
    }

    private static OMElement _createOMElementFromBlock(String localName, OMNamespace ns, Block b,
                                                       SOAPFactory soapFactory, boolean isHeaderBlock) {
        if (isHeaderBlock) {
            return soapFactory.createSOAPHeaderBlock(localName, ns, b);
        } else {
            return soapFactory.createOMElement(b, localName, ns);
        }
        
    }

    /**
     * Gets the OMElement that is the parent of where the the body blocks are located
     *
     * @return
     */
    private OMElement _getBodyBlockParent() {
        SOAPBody body = root.getBody();
        if (!body.hasFault() && indirection == 1) {
            //  For RPC the blocks are within the operation element
            OMElement op = body.getFirstElement();
            if (op == null) {
                // Create one
                OMNamespace ns = soapFactory.createOMNamespace("", "");
                op = soapFactory.createOMElement("PLACEHOLDER_OPERATION", ns, body);
            }
            return op;
        }
        return body;
    }

    /**
     * Create a factory for the specified protocol
     *
     * @param protocol
     * @return
     */
    private static SOAPFactory _getFactory(Protocol protocol) {
        SOAPFactory soapFactory;
        if (protocol == Protocol.soap11) {
            soapFactory = new SOAP11Factory();
        } else if (protocol == Protocol.soap12) {
            soapFactory = new SOAP12Factory();
        } else if (protocol == Protocol.rest) {
            // For REST, create a SOAP 1.1 Envelope to contain the message
            // This is consistent with Axis2.
            soapFactory = new SOAP11Factory();
        } else {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("RESTIsNotSupported"), null);
        }
        return soapFactory;
    }

    /**
     * Create an emtpy envelope
     *
     * @param protocol
     * @param style
     * @param factory
     * @return
     */
    private static SOAPEnvelope _createEmptyEnvelope(Style style, SOAPFactory factory) {
        SOAPEnvelope env = factory.createSOAPEnvelope();
        // Add an empty body and header
        factory.createSOAPBody(env);
        factory.createSOAPHeader(env);

        // Create a dummy operation element if this is an rpc message
        if (style == Style.RPC) {
            OMNamespace ns = factory.createOMNamespace("", "");
            factory.createOMElement("PLACEHOLDER_OPERATION", ns, env.getBody());
        }

        return env;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.XMLPart#getNumBodyBlocks()
     */
    private static int _getNumChildElements(OMElement om) throws WebServiceException {
        // Avoid calling this method.  It advances the parser.
        if (om == null) {
            return 0;
        }
        int num = 0;
        Iterator iterator = om.getChildElements();
        while (iterator.hasNext()) {
            num++;
            iterator.next();
        }
        return num;
    }

    /**
     * Get the child om at the indicated index
     *
     * @param om
     * @param index
     * @return child om or null
     */
    private static OMElement _getChildOMElement(OMElement om, int index) {
        if (om == null) {
            return null;
        }
        int i = 0;
        for (OMNode child = om.getFirstOMChild();
             child != null;
             child = child.getNextOMSibling()) {
            if (child instanceof OMElement) {
                if (i == index) {
                    return (OMElement)child;
                }
                i++;
            }
        }
        return null;
    }

    /**
     * Get the child om at the indicated index
     *
     * @param om
     * @param namespace,
     * @param localPart
     * @return child om or null
     */
    private static OMElement _getChildOMElement(OMElement om, String namespace, 
                                                String localPart) {
        if (om == null) {
            return null;
        }
        QName qName = new QName(namespace, localPart);
        Iterator it = om.getChildrenWithName(qName);
        if (it != null && it.hasNext()) {
            return (OMElement)it.next();
        }
        return null;
    }
}
