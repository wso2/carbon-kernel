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

package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.om.impl.OMDocumentImplUtil;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class DocumentImpl extends ParentNode implements Document, OMDocument {

    private String xmlVersion;

    private boolean xmlStandalone = false;
    
    private String charEncoding;

    private Vector idAttrs;

    protected ElementImpl documentElement;

    protected Hashtable identifiers;

    /** @param ownerDocument  */
    public DocumentImpl(DocumentImpl ownerDocument, OMFactory factory) {
        super(ownerDocument, factory);
        ((OMDOMFactory) factory).setDocument(this);
        this.done = true;
    }

    public DocumentImpl(OMXMLParserWrapper parserWrapper, OMFactory factory) {
        super(factory);
        this.builder = parserWrapper;
        ((OMDOMFactory) factory).setDocument(this);
    }

    public DocumentImpl(OMFactory factory) {
        super(factory);
        ((OMDOMFactory) factory).setDocument(this);
        this.done = true;
    }

    // /
    // /OMNode methods
    // //
    public void setType(int nodeType) throws OMException {
        throw new UnsupportedOperationException(
                "In OM Document object doesn't have a type");
    }

    public int getType() throws OMException {
        throw new UnsupportedOperationException(
                "In OM Document object doesn't have a type");
    }

    public void internalSerialize(XMLStreamWriter writer, boolean cache) throws XMLStreamException {
        internalSerialize(writer, cache, !((MTOMXMLStreamWriter) writer).isIgnoreXMLDeclaration());
    }

    // /
    // /Overrides ChildNode specific methods.
    // /
    public OMNode getNextOMSibling() throws OMException {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public OMContainer getParent() throws OMException {
        throw new UnsupportedOperationException("This is the document node");
    }

    public OMNode getPreviousOMSibling() {
        throw new UnsupportedOperationException("This is the document node");
    }

    public Node getPreviousSibling() {
        return null;
    }

    public void setNextOMSibling(OMNode node) {
        throw new UnsupportedOperationException("This is the document node");
    }

    public void setParent(OMContainer element) {
        throw new UnsupportedOperationException("This is the document node");
    }

    public void setPreviousOMSibling(OMNode node) {
        throw new UnsupportedOperationException("This is the document node");
    }

    // /
    // /org.w3c.dom.Node methods
    // /
    public String getNodeName() {
        return "#document";
    }

    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }

    // /org.w3c.dom.Document methods
    // /

    public Attr createAttribute(String name) throws DOMException {
        if (!DOMUtil.isQualifiedName(name)) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.INVALID_CHARACTER_ERR,
                    null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        return new AttrImpl(this, name, this.factory);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        String localName = DOMUtil.getLocalName(qualifiedName);
        String prefix = DOMUtil.getPrefix(qualifiedName);

        if (!OMConstants.XMLNS_NS_PREFIX.equals(localName)) {
            this.checkQName(prefix, localName);
        } else {
            return this.createAttribute(localName);
        }

        return new AttrImpl(this, localName, new NamespaceImpl(
                namespaceURI, prefix), this.factory);
    }

    public CDATASection createCDATASection(String arg0) throws DOMException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Comment createComment(String data) {
        return new CommentImpl(this, data, this.factory);
    }

    public DocumentFragment createDocumentFragment() {
        return new DocumentFragmentImpl(this, this.factory);
    }

    public Element createElement(String tagName) throws DOMException {
        return new ElementImpl(this, tagName, this.factory);
    }

    public Element createElementNS(String ns, String qualifiedName)
            throws DOMException {

        if (ns == null) ns = "";

        String localName = DOMUtil.getLocalName(qualifiedName);
        String prefix = DOMUtil.getPrefix(qualifiedName);
        if(prefix == null) {
            prefix = "";
        }

        //When the namespace is a default namespace
        if (!prefix.isEmpty()) {
            this.checkQName(prefix, localName);
        }

        NamespaceImpl namespace = new NamespaceImpl(ns, prefix);
        return new ElementImpl(this, localName, namespace, this.factory);
    }

    public EntityReference createEntityReference(String arg0)
            throws DOMException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public ProcessingInstruction createProcessingInstruction(String target,
                                                             String data) throws DOMException {
        return new ProcessingInstructionImpl(this, target, data, factory);
    }

    public Text createTextNode(String value) {
        return new TextImpl(this, value, this.factory);
    }

    public DocumentType getDoctype() {
        Iterator it = getChildren();
        while (it.hasNext()) {
            Object child = it.next();
            if (child instanceof DocumentType) {
                return (DocumentType)child;
            } else if (child instanceof Element) {
                // A doctype declaration can only appear before the root element. Stop here.
                return null;
            }
        }
        return null;
    }

    public Element getElementById(String elementId) {

        //If there are no id attrs
        if (this.idAttrs == null) {
            return null;
        }

        Enumeration attrEnum = this.idAttrs.elements();
        while (attrEnum.hasMoreElements()) {
            Attr tempAttr = (Attr) attrEnum.nextElement();
            if (tempAttr.getValue().equals(elementId)) {
                return tempAttr.getOwnerElement();
            }
        }

        //If we reach this point then, there's no such attr 
        return null;
    }

    public NodeList getElementsByTagName(String arg0) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public NodeList getElementsByTagNameNS(String arg0, String arg1) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public DOMImplementation getImplementation() {
        return new DOMImplementationImpl();
    }

    public Node importNode(Node importedNode, boolean deep) throws DOMException {

        short type = importedNode.getNodeType();
        Node newNode = null;
        switch (type) {
            case Node.ELEMENT_NODE: {
                Element newElement;
                if (importedNode.getLocalName() == null) {
                    newElement = this.createElement(importedNode.getNodeName());
                } else {
                    
                    String ns = importedNode.getNamespaceURI();
                    ns = (ns != null) ? ns.intern() : null;
                    newElement = createElementNS(ns, importedNode.getNodeName());
                }

                // Copy element's attributes, if any.
                NamedNodeMap sourceAttrs = importedNode.getAttributes();
                if (sourceAttrs != null) {
                    int length = sourceAttrs.getLength();
                    for (int index = 0; index < length; index++) {
                        Attr attr = (Attr) sourceAttrs.item(index);
                        if (attr.getNamespaceURI() != null
                                && !attr.getNamespaceURI().equals(
                                OMConstants.XMLNS_NS_URI)) {
                            Attr newAttr = (Attr) importNode(attr, true);
                            newElement.setAttributeNodeNS(newAttr);
                        } else { // if (attr.getLocalName() == null) {
                            Attr newAttr = (Attr) importNode(attr, true);
                            newElement.setAttributeNode(newAttr);
                        }

                    }
                }
                newNode = newElement;
                break;
            }

            case Node.ATTRIBUTE_NODE: {
                if ("".equals(importedNode.getNamespaceURI())
                        || importedNode.getNamespaceURI() == null) {
                    newNode = createAttribute(importedNode.getNodeName());
                } else {
                    //Check whether it is a default ns decl
                    if (OMConstants.XMLNS_NS_PREFIX.equals(importedNode.getNodeName())) {
                        newNode = createAttribute(importedNode.getNodeName());
                    } else {
                        String ns = importedNode.getNamespaceURI();
                        ns = (ns != null) ? ns.intern() : null;
                        newNode = createAttributeNS(ns ,
                                                    importedNode.getNodeName());
                    }
                }
                ((Attr) newNode).setValue(importedNode.getNodeValue());
                break;
            }

            case Node.TEXT_NODE: {
                newNode = createTextNode(importedNode.getNodeValue());
                break;
            }

            case Node.COMMENT_NODE: {
                newNode = createComment(importedNode.getNodeValue());
                break;
            }
                
            case Node.DOCUMENT_FRAGMENT_NODE: {
                newNode = createDocumentFragment();
                // No name, kids carry value
                break;
            }

            case Node.CDATA_SECTION_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.ENTITY_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.NOTATION_NODE:
                throw new UnsupportedOperationException("TODO : Implement handling of org.w3c.dom.Node type == " + type );

            case Node.DOCUMENT_NODE: // Can't import document nodes
            default: { // Unknown node type
                String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN, DOMException.NOT_SUPPORTED_ERR, null);
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }

        }

        // If deep, replicate and attach the kids.
        if (deep && !(importedNode instanceof Attr)) {
            for (Node srckid = importedNode.getFirstChild(); srckid != null;
                 srckid = srckid.getNextSibling()) {
                newNode.appendChild(importNode(srckid, true));
            }
        }

        return newNode;

    }

    // /
    // /OMDocument Methods
    // /
    public String getCharsetEncoding() {
        return this.charEncoding;
    }

    public String getXMLVersion() {
        return this.xmlVersion;
    }

    public String isStandalone() {
        return (this.xmlStandalone) ? "yes" : "no";
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charEncoding = charsetEncoding;
    }

    public void setOMDocumentElement(OMElement rootElement) {
        this.firstChild = (ElementImpl) rootElement;
    }

    public void setStandalone(String isStandalone) {
        this.xmlStandalone = "yes".equalsIgnoreCase(isStandalone);
    }

    public void serializeAndConsume(OutputStream output, OMOutputFormat format)
            throws XMLStreamException {
        MTOMXMLStreamWriter writer = new MTOMXMLStreamWriter(output, format);
        internalSerialize(writer, false);
        writer.flush();
    }

    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        MTOMXMLStreamWriter writer = new MTOMXMLStreamWriter(output, format);
        internalSerialize(writer, true);
        writer.flush();
    }

    public void setXMLVersion(String version) {
        this.xmlVersion = version;
    }

    /**
     * Returns the document element.
     *
     * @see org.apache.axiom.om.OMDocument#getOMDocumentElement()
     */
    public OMElement getOMDocumentElement() {

        while (this.documentElement == null && !this.done) {
            this.builder.next();
        }
        return this.documentElement;
    }

    /**
     * Returns the document element.
     *
     * @see org.w3c.dom.Document#getDocumentElement()
     */
    public Element getDocumentElement() {

        return (Element) this.getOMDocumentElement();
    }

    /**
     * Borrowed from the Xerces impl. Checks if the given qualified name is legal with respect to
     * the version of XML to which this document must conform.
     *
     * @param prefix prefix of qualified name
     * @param local  local part of qualified name
     */
    protected final void checkQName(String prefix, String local) {

        // check that both prefix and local part match NCName
        boolean validNCName = (prefix == null || XMLChar.isValidNCName(prefix))
                && XMLChar.isValidNCName(local);

        if (!validNCName) {
            // REVISIT: add qname parameter to the message
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.INVALID_CHARACTER_ERR,
                    null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }

        if (prefix == null || prefix.equals("")) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.NAMESPACE_ERR, null);
            throw new DOMException(DOMException.NAMESPACE_ERR, msg);
        }
    }

    protected void addIdAttr(Attr attr) {
        if (this.idAttrs == null) {
            this.idAttrs = new Vector();
        }
        this.idAttrs.add(attr);
    }

    protected void removeIdAttr(Attr attr) {
        if (this.idAttrs != null) {
            this.idAttrs.remove(attr);
        }

    }

    /*
    * DOM-Level 3 methods
    */

    public String getTextContent() throws DOMException {
        return null;
    }

    public void setTextContent(String textContent) throws DOMException {
        // no-op
    }

    public Node adoptNode(Node node) throws DOMException {
        //OK... I'm cheating here,  a BIG TODO
        return this.importNode(node, true);
    }

    public String getDocumentURI() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public DOMConfiguration getDomConfig() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public String getInputEncoding() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean getStrictErrorChecking() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public String getXmlEncoding() {
        return this.charEncoding;
    }

    public boolean getXmlStandalone() {
        return this.xmlStandalone;
    }

    public String getXmlVersion() {
        return getXMLVersion();
    }

    public void normalizeDocument() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Node renameNode(Node arg0, String arg1, String arg2)
            throws DOMException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void setDocumentURI(String arg0) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void setStrictErrorChecking(boolean arg0) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void setXmlStandalone(boolean standalone) throws DOMException {
        this.xmlStandalone = standalone;
    }

    public void setXmlVersion(String version) throws DOMException {
        setXMLVersion(version);
    }

    protected void internalSerialize(XMLStreamWriter writer, boolean cache,
            boolean includeXMLDeclaration) throws XMLStreamException {
        OMDocumentImplUtil.internalSerialize(this, writer, cache, includeXMLDeclaration);
    }
}
