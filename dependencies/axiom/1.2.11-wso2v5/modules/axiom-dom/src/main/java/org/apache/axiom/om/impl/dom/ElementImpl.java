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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axiom.om.impl.traverse.OMChildElementIterator;
import org.apache.axiom.om.impl.traverse.OMDescendantsIterator;
import org.apache.axiom.om.impl.traverse.OMQNameFilterIterator;
import org.apache.axiom.om.impl.traverse.OMQualifiedNameFilterIterator;
import org.apache.axiom.om.impl.util.EmptyIterator;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.om.util.ElementHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** Implementation of the org.w3c.dom.Element and org.apache.axiom.om.Element interfaces. */
public class ElementImpl extends ParentNode implements Element, OMElement,
        OMConstants {

    private int lineNumber;

    protected OMNamespace namespace;

    protected String localName;

    private AttributeMap attributes;

    private HashMap namespaces;

    private static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();

    private static final OMNamespace XMLNS_NAMESPACE_WITH_PREFIX = new NamespaceImpl(OMConstants.XMLNS_NS_URI, OMConstants.XMLNS_NS_PREFIX);
    
    private static final OMNamespace XMLNS_NAMESPACE_WITHOUT_PREFIX = new NamespaceImpl(OMConstants.XMLNS_NS_URI, null);
    
    /** @param ownerDocument  ownerDocument
     *  @param tagName tagName
     *  @param factory OMFactory
     *
     * */
    public ElementImpl(DocumentImpl ownerDocument, String tagName,
                       OMFactory factory) {
        super(ownerDocument, factory);
        this.localName = tagName;
        this.attributes = new AttributeMap(this);
        this.done = true;
    }

    /**
     * Creates a new element with the namespace.
     *
     * @param ownerDocument
     * @param tagName
     * @param ns
     * @param factory
     */
    public ElementImpl(DocumentImpl ownerDocument, String tagName,
                       NamespaceImpl ns, OMFactory factory) {
        super(ownerDocument, factory);
        this.localName = tagName;
        this.namespace = ns;
        this.declareNamespace(ns);
        this.attributes = new AttributeMap(this);
        this.done = true;
    }

    public ElementImpl(DocumentImpl ownerDocument, String tagName,
                       NamespaceImpl ns, OMXMLParserWrapper builder, OMFactory factory) {
        super(ownerDocument, factory);
        this.localName = tagName;
        this.namespace = ns;
        this.builder = builder;
        this.declareNamespace(ns);
        this.attributes = new AttributeMap(this);
    }

    public ElementImpl(ParentNode parentNode, String tagName, NamespaceImpl ns,
                       OMFactory factory) {
        this((DocumentImpl) parentNode.getOwnerDocument(), tagName, ns, factory);
        parentNode.addChild(this);
        this.done = true;
    }

    public ElementImpl(ParentNode parentNode, String tagName, NamespaceImpl ns,
                       OMXMLParserWrapper builder, OMFactory factory) {
        this(tagName, ns, builder, factory);
        if (parentNode != null) {
            this.ownerNode = (DocumentImpl) parentNode.getOwnerDocument();
            this.isOwned(true);
            parentNode.addChild(this);
        }

    }

    public ElementImpl(String tagName, NamespaceImpl ns,
                       OMXMLParserWrapper builder, OMFactory factory) {
        this(factory);
        this.localName = tagName;
        this.namespace = ns;
        this.builder = builder;
        if (ns != null) {
            this.declareNamespace(ns);
        }
        this.attributes = new AttributeMap(this);
    }

    public ElementImpl(OMFactory factory) {
        super(factory);
        this.ownerNode = ((OMDOMFactory) factory).getDocument();
    }

    // /
    // /org.w3c.dom.Node methods
    // /

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Node#getNodeType()
     */
    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Node#getNodeName()
     */ 
    public String getNodeName() {
        if (this.namespace != null) {
            String prefix = this.namespace.getPrefix();
            if (prefix == null || prefix.isEmpty()) {
                return this.localName;
            } else {
                return prefix + ":" + this.localName;
            }
        } else {
            return this.localName;
        }
    }

    /** Returns the value of the namespace URI. */
    public String getNamespaceURI() {
        if (this.namespace == null) {
            return null;
        } else {
            // If the element has no namespace, the result should be null, not
            // an empty string.
            String uri = this.namespace.getNamespaceURI();
            return uri.isEmpty() ? null : uri.intern();
        }
    }

    // /
    // /org.apache.axiom.om.OMNode methods
    // /

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axiom.om.OMNode#getType()
     */
    public int getType() throws OMException {
        return OMNode.ELEMENT_NODE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axiom.om.OMNode#setType(int)
     */
    public void setType(int nodeType) throws OMException {
        // Do nothing ...
        // This is an Eement Node...
    }

    // /
    // / org.w3c.dom.Element methods
    // /

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Element#getTagName()
     */
    public String getTagName() {
        return this.getNodeName();
    }

    /**
     * Removes an attribute by name.
     *
     * @param name The name of the attribute to remove
     * @see org.w3c.dom.Element#removeAttribute(String)
     */
    public void removeAttribute(String name) throws DOMException {
        if (this.isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }

        if (name.startsWith(OMConstants.XMLNS_NS_PREFIX)) {
            String namespacePrefix = DOMUtil.getLocalName(name);
            if (this.findNamespaceURI(namespacePrefix) != null) {
                this.removeNamespace(namespacePrefix);
            }
        }

        if (this.attributes != null) {
            this.attributes.removeNamedItem(name);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String,
     *      java.lang.String)
     */
    public void removeAttributeNS(String namespaceURI, String localName)
            throws DOMException {
        if (this.isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }

        if (OMConstants.XMLNS_NS_URI.equals(namespaceURI)) {
            //look in the ns list
            if (this.namespaces != null) {
                this.namespaces.remove(DOMUtil.getLocalName(localName));
            }

        } else if (this.attributes != null) {
            this.attributes.removeNamedItemNS(namespaceURI, localName);
        }
    }

    /**
     * Removes the specified attribute node.
     *
     * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
     */
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        if (isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        if (this.attributes == null
                || this.attributes.getNamedItem(oldAttr.getName()) == null) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.NOT_FOUND_ERR, null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }
        return (AttrImpl) this.attributes.removeNamedItem(oldAttr
                .getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(String name) {
        return this.getAttributeNode(name) != null;
    }

    /**
     * Returns whether the given attribute is available or not.
     *
     * @see org.w3c.dom.Element#hasAttributeNS(String, String)
     */
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return this.getAttributeNodeNS(namespaceURI, localName) != null;
    }

    /**
     * Looks in the local list of attributes and returns if found. If the local list is null,
     * returns "".
     *
     * @see org.w3c.dom.Element#getAttribute(String)
     */
    public String getAttribute(String name) {
        if (attributes == null) {
            return "";
        } else {
            Attr attr = ((Attr) attributes.getNamedItem(name));
            return (attr != null) ? attr.getValue() : "";
        }
    }

    /**
     * Retrieves an attribute node by name.
     *
     * @see org.w3c.dom.Element#getAttributeNode(String)
     */
    public Attr getAttributeNode(String name) {
        return (this.attributes == null) ? null : (AttrImpl) this.attributes
                .getNamedItem(name);
    }

    /**
     * Retrieves an attribute value by local name and namespace URI.
     *
     * @see org.w3c.dom.Element#getAttributeNS(String, String)
     */
    public String getAttributeNS(String namespaceURI, String localName) {
        if (this.attributes == null) {
            return "";
        }
        Attr attributeNodeNS = this.getAttributeNodeNS(namespaceURI, localName);
        return attributeNodeNS == null ? "" : attributeNodeNS.getValue();
    }

    /**
     * Retrieves an attribute node by local name and namespace URI.
     *
     * @see org.w3c.dom.Element#getAttributeNodeNS(String, String)
     */
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {

        if (OMConstants.XMLNS_NS_URI.equals(namespaceURI)) {
            OMNamespace ns = this.findNamespaceURI(localName);
            String nsuri = ns != null ? ns.getNamespaceURI() : "";

            AttrImpl namespaceAttr = new AttrImpl(this.ownerNode,
                                                  localName, nsuri, this.factory);
            NamespaceImpl xmlNs = new NamespaceImpl(OMConstants.XMLNS_NS_URI);
            namespaceAttr.setOMNamespace(xmlNs);
            return namespaceAttr;
        }

        return (this.attributes == null) ? null : (Attr) this.attributes
                .getNamedItemNS(namespaceURI, localName);

    }

    /**
     * Adds a new attribute node.
     *
     * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
     */
    public Attr setAttributeNode(Attr attr) throws DOMException {
        AttrImpl attrImpl = (AttrImpl) attr;

        if (attrImpl.isOwned()) {// check for ownership
            if (!this.getOwnerDocument().equals(attr.getOwnerDocument())) {
                String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN, DOMException.WRONG_DOCUMENT_ERR,
                        null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
        }

        if (this.isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }

        // check whether the attr is in use
        if (attrImpl.isUsed()) {
            String msg = DOMMessageFormatter
                    .formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                                   DOMException.INUSE_ATTRIBUTE_ERR, null);
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, msg);
        }

        if (attr.getNodeName().startsWith(OMConstants.XMLNS_NS_PREFIX + ":")) {
            // This is a ns declaration
            this.declareNamespace(attr.getNodeValue(), DOMUtil
                    .getLocalName(attr.getName()));

            //Don't add this to attr list, since its a namespace
            return attr;
        } else if (attr.getNodeName().equals(OMConstants.XMLNS_NS_PREFIX)) {
            this.declareDefaultNamespace(attr.getValue());

            //Don't add this to attr list, since its a namespace
            return attr;
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this);
        }

        return (Attr) this.attributes.setNamedItem(attr);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
     */
    public void setAttribute(String name, String value) throws DOMException {
        // Check for invalid charaters
        if (!DOMUtil.isQualifiedName(name)) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.INVALID_CHARACTER_ERR,
                    null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        if (name.startsWith(OMConstants.XMLNS_NS_PREFIX + ":")) {
            // This is a ns declaration
            this.declareNamespace(value, DOMUtil.getLocalName(name));
        } else if (name.equals(OMConstants.XMLNS_NS_PREFIX)) {
            this.declareDefaultNamespace(value);
        } else {
            this.setAttributeNode(new AttrImpl(this.ownerNode, name, value,
                                               this.factory));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
     */
    public Attr setAttributeNodeNS(Attr attr) throws DOMException {

        // Check whether the attr is a namespace declaration
        // if so add a namespace NOT an attribute
        if (attr.getNamespaceURI() != null
                && attr.getNamespaceURI().equals(OMConstants.XMLNS_NS_URI)) {
            this.declareNamespace(attr.getName(), attr.getValue());
            return attr;
        } else {
            AttrImpl attrImpl = (AttrImpl) attr;

            if (attrImpl.isOwned()) {// check for ownership
                if (!this.getOwnerDocument().equals(attr.getOwnerDocument())) {
                    String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            DOMException.WRONG_DOCUMENT_ERR, null);
                    throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
                }
            }

            if (this.isReadonly()) {
                String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
            }

            // check whether the attr is in use
            if (attrImpl.isUsed()) {
                String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN, DOMException.INUSE_ATTRIBUTE_ERR,
                        null);
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, msg);
            }

            if (this.attributes == null) {
                this.attributes = new AttributeMap(this);
            }

            // handle the namespaces
            if (attr.getNamespaceURI() != null
                    && findNamespace(attr.getNamespaceURI(), attr.getPrefix())
                    == null) {
                // TODO checkwhether the same ns is declared with a different
                // prefix and remove it
                this.declareNamespace(new NamespaceImpl(attr.getNamespaceURI(),
                                                        attr.getPrefix()));
            }

            return (Attr) this.attributes.setNamedItemNS(attr);
        }
    }

    /**
     * Adds a new attribute.
     *
     * @see org.w3c.dom.Element#setAttributeNS(String, String, String)
     */
    public void setAttributeNS(String namespaceURI, String qualifiedName,
                               String value) throws DOMException {

        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            if (namespaceURI.equals(OMConstants.XMLNS_NS_URI)) {
                this.declareNamespace(value, DOMUtil
                        .getLocalName(qualifiedName));
            } else {
                AttrImpl attr = new AttrImpl(this.ownerNode, DOMUtil
                        .getLocalName(qualifiedName), value, this.factory);
                attr.setOMNamespace(new NamespaceImpl(namespaceURI, DOMUtil
                        .getPrefix(qualifiedName)));

                this.setAttributeNodeNS(attr);
            }
        } else {
            // When the namespace is null, the attr name given better not be
            // a qualified name
            // But anyway check and set it
            this.setAttribute(DOMUtil.getLocalName(qualifiedName), value);
        }

    }

    private OMAttribute addAttribute(String namespaceURI, String qualifiedName,
                                     String value) throws DOMException {
        if (!DOMUtil.isQualifiedName(qualifiedName)) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.INVALID_CHARACTER_ERR ,
                    null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }

        if (this.isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }

        if (this.attributes == null) {
            this.attributes = new AttributeMap(this);
        }
        if (namespaceURI != null) {
            if (!DOMUtil.isValidNamespace(namespaceURI, qualifiedName)) {
                String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN, DOMException.NAMESPACE_ERR, null);
                throw new DOMException(DOMException.NAMESPACE_ERR, msg);
            }
            // Check whether there's an existing Attr with same local name and
            // namespace URI
            String localName = DOMUtil.getLocalName(qualifiedName);
            Attr attributeNode = this.getAttributeNodeNS(namespaceURI, localName);
            if (attributeNode != null) {
                AttrImpl tempAttr = ((AttrImpl) attributeNode);
                tempAttr.setOMNamespace(new NamespaceImpl(namespaceURI, DOMUtil
                        .getPrefix(qualifiedName)));
                tempAttr.setAttributeValue(value);
                this.attributes.setNamedItem(tempAttr);
                return tempAttr;
            } else {
                NamespaceImpl ns = new NamespaceImpl(namespaceURI, DOMUtil
                        .getPrefix(qualifiedName));
                AttrImpl attr = new AttrImpl((DocumentImpl) this
                        .getOwnerDocument(), localName, ns, value, this.factory);
                this.attributes.setNamedItem(attr);
                return attr;
            }
        } else {
            Attr attributeNode = this.getAttributeNode(qualifiedName);
            if (attributeNode != null) {
                AttrImpl tempAttr = ((AttrImpl) attributeNode);
                tempAttr.setAttributeValue(value);
                this.attributes.setNamedItem(tempAttr);
                return tempAttr;
            } else {
                AttrImpl attr = new AttrImpl((DocumentImpl) this
                        .getOwnerDocument(), qualifiedName, value, this.factory);
                this.attributes.setNamedItem(attr);
                return attr;
            }
        }
    }

    /** Returns whether this element contains any attribute or not. */
    public boolean hasAttributes() {
        // DOM represents namespace declarations as attributes; therefore
        // we need to check both "attributes" and "namespaces"
        return attributes != null && attributes.getLength() > 0
                || namespaces != null && !namespaces.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String,
     *      java.lang.String)
     */
    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        final QName qname = new QName(namespaceURI, localName);
        return new NodeListImpl() {
            protected Iterator getIterator() {
                return new OMQNameFilterIterator(
                        new OMDescendantsIterator(getFirstOMChild()), qname);
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
     */
    public NodeList getElementsByTagName(final String name) {
        if (name.equals("*")) {
            return new NodeListImpl() {
                protected Iterator getIterator() {
                    return new OMDescendantsIterator(getFirstOMChild());
                }
            };
        } else {
            return new NodeListImpl() {
                protected Iterator getIterator() {
                    return new OMQualifiedNameFilterIterator(
                            new OMDescendantsIterator(getFirstOMChild()), name);
                }
            };
        }
    }

    // /
    // /OmElement methods
    // /

    /** @see org.apache.axiom.om.OMElement#addAttribute (org.apache.axiom.om.OMAttribute) */
    public OMAttribute addAttribute(OMAttribute attr) {
        // If the attribute already has an owner element then clone the attribute (except if it is owned
        // by the this element)
        OMElement owner = attr.getOwner();
        if (owner != null) {
            if (owner == this) {
                return attr;
            }
            attr = (OMAttribute)((AttrImpl)attr).cloneNode(false);
        }
        
        OMNamespace namespace = attr.getNamespace();
        if (namespace != null) {
            String uri = namespace.getNamespaceURI();
            if (uri.length() > 0) {
                String prefix = namespace.getPrefix();
                OMNamespace ns2 = findNamespaceURI(prefix);
                if (ns2 == null || !uri.equals(ns2.getNamespaceURI())) {
                    declareNamespace(uri, prefix);
                }
            }
        }

        if (attr.getNamespace() != null) { // If the attr has a namespace
            this.setAttributeNodeNS((Attr) attr);
        } else {
            this.setAttributeNode((Attr) attr);
        }
        return attr;
    }

    public OMAttribute addAttribute(String localName, String value,
                                    OMNamespace ns) {
        if (ns != null) {
            String uri = ns.getNamespaceURI();
            if (uri.length() > 0) {
                String prefix = ns.getPrefix();
                OMNamespace ns2 = findNamespaceURI(prefix);
                if (ns2 == null || !uri.equals(ns2.getNamespaceURI())) {
                    declareNamespace(uri, prefix);
                }
            }
        }
        return addAttribute(new AttrImpl(ownerNode, localName, ns, value, factory));
    }

    /**
     * Allows overriding an existing declaration if the same prefix was used.
     *
     * @see org.apache.axiom.om.OMElement#declareNamespace (org.apache.axiom.om.OMNamespace)
     */
    public OMNamespace declareNamespace(OMNamespace namespace) {
        if (namespaces == null) {
            this.namespaces = new HashMap(5);
        }

        if (namespace != null) {
            String prefix = namespace.getPrefix();
            if (prefix == null) {
                prefix = OMSerializerUtil.getNextNSPrefix();
                namespace = new NamespaceImpl(namespace.getNamespaceURI(), prefix);
            } else if (prefix.isEmpty()) {
                namespace = declareDefaultNamespace(namespace.getNamespaceURI());
            }

            if (!namespace.getPrefix().startsWith(OMConstants.XMLNS_NS_PREFIX)) {
                namespaces.put(namespace.getPrefix(), namespace);
            }
        }
        return namespace;
    }

    /**
     * Allows overriding an existing declaration if the same prefix was used.
     *
     * @see org.apache.axiom.om.OMElement#declareNamespace(String, String)
     */
    public OMNamespace declareNamespace(String uri, String prefix) {
        if (prefix == null) {
            prefix = OMSerializerUtil.getNextNSPrefix();
        }
        
        NamespaceImpl ns = new NamespaceImpl(uri, prefix);
        return declareNamespace(ns);
    }

    /**
     * We use "" to store the default namespace of this element. As one can see user can not give ""
     * as the prefix, when he declare a usual namespace.
     *
     * @param uri
     */
    public OMNamespace declareDefaultNamespace(String uri) {
        NamespaceImpl ns = new NamespaceImpl(uri, "");
        if (namespaces == null) {
            this.namespaces = new HashMap(5);
        }
        namespaces.put("", ns);
        return ns;
    }

    public OMNamespace getDefaultNamespace() {
        if (namespaces != null) {
            NamespaceImpl defaultNS = (NamespaceImpl) namespaces.get("");
            if (defaultNS != null) {
                return defaultNS;
            }
        }

        if (parentNode instanceof ElementImpl) {
            ElementImpl element = (ElementImpl) parentNode;
            element.getDefaultNamespace();
        }
        return null;
    }

    /** @see org.apache.axiom.om.OMElement#findNamespace(String, String) */
    public OMNamespace findNamespace(String uri, String prefix) {

        // check in the current element
        OMNamespace namespace = findDeclaredNamespace(uri, prefix);
        if (namespace != null) {
            return namespace;
        }

        // go up to check with ancestors
        if (this.parentNode != null) {
            // For the OMDocumentImpl there won't be any explicit namespace
            // declarations, so going up the parent chain till the document
            // element should be enough.
            if (parentNode instanceof OMElement) {
                namespace = ((ElementImpl) parentNode).findNamespace(uri,
                                                                     prefix);
            }
        }

        if (namespace == null && uri != null && prefix != null
                && prefix.equals(OMConstants.XMLNS_PREFIX)
                && uri.equals(OMConstants.XMLNS_URI)) {
            declareNamespace(OMConstants.XMLNS_URI, OMConstants.XMLNS_PREFIX);
            namespace = findNamespace(uri, prefix);
        }
        return namespace;
    }

    public OMNamespace findNamespaceURI(String prefix) {
        OMNamespace ns = this.namespaces == null ?
                null :
                (OMNamespace) this.namespaces.get(prefix);

        if (ns == null && this.parentNode instanceof OMElement) {
            // try with the parent
            ns = ((OMElement) this.parentNode).findNamespaceURI(prefix);
        }
        return ns;
    }

    /**
     * Checks for the namespace <B>only</B> in the current Element. This can also be used to
     * retrieve the prefix of a known namespace URI.
     */
    private OMNamespace findDeclaredNamespace(String uri, String prefix) {

        if (uri == null) {
            return namespaces == null ? null : (OMNamespace)namespaces.get(prefix);
        }
        // If the prefix is available and uri is available and its the xml
        // namespace
        if (prefix != null && prefix.equals(OMConstants.XMLNS_PREFIX)
                && uri.equals(OMConstants.XMLNS_URI)) {
            return new NamespaceImpl(uri, prefix);
        }

        if (namespaces == null) {
            return null;
        }

        if (prefix == null || prefix.isEmpty()) {
            Iterator namespaceListIterator = namespaces.values().iterator();
            while (namespaceListIterator.hasNext()) {
                OMNamespace omNamespace = (OMNamespace) namespaceListIterator.next();
                String nsURI = omNamespace.getNamespaceURI();
                if (nsURI != null && nsURI.equals(uri)) {
                    return omNamespace;
                }
            }

        } else {
            OMNamespace namespace = (OMNamespace) namespaces.get(prefix);
            if (namespace != null && uri.equals(namespace.getNamespaceURI())) {
                return namespace;
            }
        }

        return null;
    }

    /**
     * Returns a named attribute if present.
     *
     * @see org.apache.axiom.om.OMElement#getAttribute (javax.xml.namespace.QName)
     */
    public OMAttribute getAttribute(QName qname) {
        if (this.attributes == null) {
            return null;
        }

        if (qname.getNamespaceURI() == null
                || qname.getNamespaceURI().equals("")) {
            return (AttrImpl) this.getAttributeNode(qname.getLocalPart());
        } else {
            return (AttrImpl) this.getAttributeNodeNS(qname.getNamespaceURI(),
                                                      qname.getLocalPart());
        }
    }

    /**
     * Returns a named attribute's value, if present.
     *
     * @param qname the qualified name to search for
     * @return Returns a String containing the attribute value, or null.
     */
    public String getAttributeValue(QName qname) {
        OMAttribute attr = getAttribute(qname);
        return (attr == null) ? null : attr.getAttributeValue();
    }

    /**
     * Returns the first Element node.
     *
     * @see org.apache.axiom.om.OMElement#getFirstElement()
     */
    public OMElement getFirstElement() {
        OMNode node = getFirstOMChild();
        while (node != null) {
            if (node.getType() == Node.ELEMENT_NODE) {
                return (OMElement) node;
            } else {
                node = node.getNextOMSibling();
            }
        }
        return null;
    }

    /**
     * Returns the namespace of this element.
     *
     * @see org.apache.axiom.om.OMElement#getNamespace()
     */
    public OMNamespace getNamespace() {
        return namespace != null ? namespace : getDefaultNamespace();
    }

    /**
     * Returns the QName of this element.
     *
     * @see org.apache.axiom.om.OMElement#getQName()
     */
    public QName getQName() {
        QName qName;
        if (namespace != null) {
            if (namespace.getPrefix() != null) {
                qName = new QName(namespace.getNamespaceURI(), this.localName,
                                  namespace.getPrefix());
            } else {
                qName = new QName(namespace.getNamespaceURI(), this.localName);
            }
        } else {
            qName = new QName(this.localName);
        }
        return qName;
    }

    /**
     * Gets all the text children and concatinates them to a single string.
     *
     * @see org.apache.axiom.om.OMElement#getText()
     */
    public String getText() {
        StringBuilder childText = new StringBuilder("");
        OMNode child = this.getFirstOMChild();
        OMText textNode;

        while (child != null) {
            final int type = child.getType();
            if (type == OMNode.TEXT_NODE || type == OMNode.CDATA_SECTION_NODE) {
                textNode = (OMText) child;
                String text = textNode.getText();
                if (text != null && !text.isEmpty()) {
                    childText.append(text);
                }
            }
            child = child.getNextOMSibling();
        }

        return childText.toString();
    }

    public QName getTextAsQName() {
        String childText = getTrimmedText();
        if (childText != null) {
            return resolveQName(childText);
        }
        return null;
    }

    public String getTrimmedText() {
        String childText = null;
        OMNode child = this.getFirstOMChild();
        OMText textNode;

        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                textNode = (OMText) child;
                String textValue = textNode.getText();
                if (textValue != null && !textValue.trim().isEmpty()) {
                    if (childText == null) childText = "";
                    childText += textValue.trim();
                }
            }
            child = child.getNextOMSibling();
        }

        return childText;
    }

    /**
     * Removes an attribute from the element.
     *
     * @see org.apache.axiom.om.OMElement#removeAttribute (org.apache.axiom.om.OMAttribute)
     */
    public void removeAttribute(OMAttribute attr) {
        this.removeAttributeNode((AttrImpl) attr);
    }

    /**
     * Sets the OM builder.
     *
     * @see org.apache.axiom.om.OMElement#setBuilder (org.apache.axiom.om.OMXMLParserWrapper)
     */
    public void setBuilder(OMXMLParserWrapper wrapper) {
        this.builder = wrapper;
    }

    /**
     * Sets the local name.
     *
     * @see org.apache.axiom.om.OMElement#setLocalName(String)
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Sets the namespace.
     *
     * @see org.apache.axiom.om.OMElement#setNamespace (org.apache.axiom.om.OMNamespace)
     */
    public void setNamespace(OMNamespace namespace) {
        this.namespace = namespace;
    }

    public void setNamespaceWithNoFindInCurrentScope(OMNamespace namespace) {
        this.namespace = namespace;
    }

    /**
     * Creates a text node with the given value and adds it to the element.
     *
     * @see org.apache.axiom.om.OMElement#setText(String)
     */
    public void setText(String text) {
        if (this.isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }

        // if we already have other text nodes remove them
        OMNode child = this.getFirstOMChild();
        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                child.detach();
            }
            child = child.getNextOMSibling();
        }

        TextImpl textNode = (TextImpl) (this.ownerNode)
                .createTextNode(text);
        this.addChild(textNode);
    }

    public void setText(QName text) {
        throw new UnsupportedOperationException();
    }

    public void internalSerialize(XMLStreamWriter writer,
                                     boolean cache) throws XMLStreamException {

        if (cache || this.done || (this.builder == null)) {
            OMSerializerUtil.serializeStartpart(this, writer);
            OMSerializerUtil.serializeChildren(this, writer, cache);
            OMSerializerUtil.serializeEndpart(writer);
        } else {
            OMSerializerUtil.serializeByPullStream(this, writer, cache);
        }
    }

    public String toStringWithConsume() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.serializeAndConsume(baos);
        return new String(baos.toByteArray());
    }

    /**
     * Overridden toString() for ease of debugging.
     *
     * @see Object#toString()
     */
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
//            this.build();
            this.serialize(baos);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Can not serialize OM Element " + this.getLocalName(), e);
        }
        return new String(baos.toByteArray());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axiom.om.OMElement#getChildElements()
     */
    public Iterator getChildElements() {
        return new OMChildElementIterator(getFirstElement());
    }

    /** @see org.apache.axiom.om.OMElement#getAllDeclaredNamespaces() */
    public Iterator getAllDeclaredNamespaces() throws OMException {
        if (namespaces == null) {
            return EMPTY_ITERATOR;
        }
        return namespaces.values().iterator();
    }

    /** @see org.apache.axiom.om.OMElement#getAllAttributes() */
    public Iterator getAllAttributes() {
        if (attributes == null) {
            return EMPTY_ITERATOR;
        }
        ArrayList list = new ArrayList();
        for (int i = 0; i < attributes.getLength(); i++) {
            OMAttribute item = (OMAttribute) attributes.getItem(i);
            if (item.getNamespace() == null
                    || !(item.getNamespace() != null && OMConstants.XMLNS_NS_URI
                    .equals(item.getNamespace().getNamespaceURI()))) {
                list.add(item);
            }
        }

        return list.iterator();
    }

    /**
     * Returns the local name of this element node
     *
     * @see org.w3c.dom.Node#getLocalName()
     */
    public String getLocalName() {
        return this.localName;
    }

    /**
     * Returns the namespace prefix of this element node
     *
     * @see org.w3c.dom.Node#getPrefix()
     */
    public String getPrefix() {
        return (this.namespace == null) ? null : this.namespace.getPrefix();
    }

    /** @see NodeImpl#setOwnerDocument (org.apache.axiom.om.impl.dom.DocumentImpl) */
    protected void setOwnerDocument(DocumentImpl document) {
        this.ownerNode = document;
        this.isOwned(true);
        if (document.firstChild == null) {
            document.firstChild = this;
        }
    }

    /**
     * Turn a prefix:local qname string into a proper QName, evaluating it in the OMElement context
     * unprefixed qnames resolve to the local namespace
     *
     * @param qname prefixed qname string to resolve
     * @return Returns null for any failure to extract a qname.
     */
    public QName resolveQName(String qname) {
        ElementHelper helper = new ElementHelper(this);
        return helper.resolveQName(qname);
    }

    /**
     * Creates a clone which belongs to a new document.
     *
     * @see org.apache.axiom.om.OMElement#cloneOMElement()
     */
    public OMElement cloneOMElement() {
        return (ElementImpl) this.cloneNode(true);
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Node cloneNode(boolean deep) {

        ElementImpl newnode = (ElementImpl) super.cloneNode(deep);
        // Replicate NamedNodeMap rather than sharing it.
        if (attributes != null) {
            newnode.attributes = (AttributeMap) attributes.cloneMap(newnode);
        }
        return newnode;

    }

    /** Returns the set of attributes of this node and the namespace declarations available. */
    public NamedNodeMap getAttributes() {
        AttributeMap attributeMap = new AttributeMap(this);

        // Add the set of existing attrs
        for (int i = 0; i < this.attributes.getLength(); i++) {
            attributeMap.addItem((Attr) this.attributes.getItem(i));
        }

        // Add the NS declarations
        if (this.namespaces != null) {
            Iterator nsDecls = this.namespaces.keySet().iterator();
            while (nsDecls.hasNext()) {
                String prefix = (String) nsDecls.next();
                if (prefix != null){

                    OMNamespace ns = (OMNamespace)this.namespaces.get(prefix);
                    
                    if (prefix.isEmpty()) {
                        AttrImpl attr = new AttrImpl(this.ownerNode, "xmlns", ns.getNamespaceURI(), this.factory);
                        attr.setOMNamespace(XMLNS_NAMESPACE_WITHOUT_PREFIX);
                        attributeMap.addItem(attr);
                    } else {
                        AttrImpl attr = new AttrImpl(this.ownerNode, prefix, ns.getNamespaceURI(), this.factory);
                        attr.setOMNamespace(XMLNS_NAMESPACE_WITH_PREFIX);
                        attributeMap.addItem(attr);
                    }
                }
            }
        }

        return attributeMap;
    }

    /**
     * Returns the namespace uri, given the prefix. If it is not found at this element, searches the
     * parent.
     *
     * @param prefix
     * @return Returns namespace.
     */
    public String getNamespaceURI(String prefix) {
        OMNamespace ns = this.findNamespaceURI(prefix);
        return (ns != null) ? ns.getNamespaceURI() : null;
    }

    /**
     * Removes a declared namespace given its prefix.
     *
     * @param prefix
     * @return Returns whether the namespace relevant to the given prefix was removed or not
     */
    public boolean removeNamespace(String prefix) {
        Object ns = this.namespaces.get(prefix);
        if (ns != null) {
            this.namespaces.remove(prefix);
            return true;
        } else {
            return false;
        }
    }

    public OMNode getNextOMSibling() throws OMException {
        while (!done) {
            int token = builder.next();
            if (token == XMLStreamConstants.END_DOCUMENT) {
                throw new OMException();
            }
        }
        return super.getNextOMSibling();
    }

    public void discard() throws OMException {
        if (done) {
            this.detach();
        } else {
            builder.discard(this);
        }
    }

    /*
     * DOM-Level 3 methods
     */

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        if (this.isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        //find the attr
        AttrImpl tempAttr = (AttrImpl) this.getAttributeNode(name);
        if (tempAttr == null) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NOT_FOUND_ERR, null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                                   msg);
        }

        this.updateIsId(isId, tempAttr);
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId)
            throws DOMException {
        if (this.isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        //find the attr
        AttrImpl tempAttr = (AttrImpl) this.getAttributeNodeNS(namespaceURI, localName);
        if (tempAttr == null) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NOT_FOUND_ERR, null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                                   msg);
        }

        this.updateIsId(isId, tempAttr);
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        if (this.isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        //find the attr
        Iterator attrIter = this.getAllAttributes();
        AttrImpl tempAttr = null;
        while (attrIter.hasNext()) {
            AttrImpl attr = (AttrImpl) attrIter.next();
            if (attr.equals(idAttr)) {
                tempAttr = attr;
                break;
            }
        }

        if (tempAttr == null) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NOT_FOUND_ERR, null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                                   msg);
        }

        this.updateIsId(isId, tempAttr);
    }

    /**
     * Updates the id state of the attr and notifies the document
     *
     * @param isId
     * @param tempAttr
     */
    private void updateIsId(boolean isId, AttrImpl tempAttr) {
        tempAttr.isId = isId;
        if (isId) {
            this.ownerNode.addIdAttr(tempAttr);
        } else {
            this.ownerNode.removeIdAttr(tempAttr);
        }
    }

    public TypeInfo getSchemaTypeInfo() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /* (non-Javadoc)
      * @see org.apache.axiom.om.OMNode#buildAll()
      */
    public void buildWithAttachments() {
        if (!done) {
            this.build();
        }
        Iterator iterator = getChildren();
        while (iterator.hasNext()) {
            OMNode node = (OMNode) iterator.next();
            node.buildWithAttachments();
        }
    }
}
