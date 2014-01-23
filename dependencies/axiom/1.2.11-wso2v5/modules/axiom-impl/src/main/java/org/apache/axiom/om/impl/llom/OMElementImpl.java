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

package org.apache.axiom.om.impl.llom;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.OMXMLStreamReader;
import org.apache.axiom.om.impl.OMContainerEx;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.builder.XOPAwareStAXOMBuilder;
import org.apache.axiom.om.impl.builder.XOPBuilder;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListImplFactory;
import org.apache.axiom.om.impl.traverse.OMChildElementIterator;
import org.apache.axiom.om.impl.traverse.OMChildrenIterator;
import org.apache.axiom.om.impl.traverse.OMChildrenLegacyQNameIterator;
import org.apache.axiom.om.impl.traverse.OMChildrenLocalNameIterator;
import org.apache.axiom.om.impl.traverse.OMChildrenNamespaceIterator;
import org.apache.axiom.om.impl.traverse.OMChildrenQNameIterator;
import org.apache.axiom.om.impl.util.EmptyIterator;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/** Class OMElementImpl */
public class OMElementImpl extends OMNodeImpl
        implements OMElement, OMConstants, OMContainerEx {

    private static final Log log = LogFactory.getLog(OMElementImpl.class);
    private static boolean DEBUG_ENABLED = log.isDebugEnabled();
    
    public static final OMNamespace DEFAULT_DEFAULT_NS_OBJECT = new OMNamespaceImpl("", "");

    /** Field ns */
    protected OMNamespace ns;

    /** Field localName */
    protected String localName;

    protected QName qName;

    /** Field firstChild */
    protected OMNode firstChild;

    /** Field namespaces */
    protected HashMap namespaces = null;
    
    /** Field attributes */
    protected HashMap attributes = null;

    /** Field noPrefixNamespaceCounter */
    protected int noPrefixNamespaceCounter = 0;
    protected OMNode lastChild;
    private int lineNumber;
    private static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();

    /**
     * Constructor OMElementImpl. A null namespace indicates that the default namespace in scope is
     * used
     */
    public OMElementImpl(String localName, OMNamespace ns, OMContainer parent,
                         OMXMLParserWrapper builder, OMFactory factory) {
        super(parent, factory, false);
        this.localName = localName;
        if (ns != null) {
            setNamespace(ns);
        }
        this.builder = builder;
        firstChild = null;
    }


    /** Constructor OMElementImpl. */
    public OMElementImpl(String localName, OMNamespace ns, OMFactory factory) {
        this(localName, ns, null, factory);
    }

    /**
     * This is the basic constructor for OMElement. All the other constructors depends on this.
     *
     * @param localName - this MUST always be not null
     * @param ns        - can be null
     * @param parent    - this should be an OMContainer
     * @param factory   - factory that created this OMElement
     *                  <p/>
     *                  A null namespace indicates that the default namespace in scope is used
     */
    public OMElementImpl(String localName, OMNamespace ns, OMContainer parent,
                         OMFactory factory) {
        super(parent, factory, true);
        if (localName == null || localName.trim().isEmpty()) {
            throw new OMException("localname can not be null or empty");
        }
        this.localName = localName;
        if (ns != null) {
            setNamespace(ns);
        }
    }

    /**
     * It is assumed that the QName passed contains, at least, the localName for this element.
     *
     * @param qname - this should be valid qname according to javax.xml.namespace.QName
     * @throws OMException
     */
    public OMElementImpl(QName qname, OMContainer parent, OMFactory factory)
            throws OMException {
        this(qname.getLocalPart(), null, parent, factory);
        this.ns = handleNamespace(qname);
    }

    /** Method handleNamespace. */
    OMNamespace handleNamespace(QName qname) {
        OMNamespace ns = null;

        // first try to find a namespace from the scope
        String namespaceURI = qname.getNamespaceURI();
        if (namespaceURI != null && namespaceURI.length() > 0) {
            String prefix = qname.getPrefix();
            ns = findNamespace(qname.getNamespaceURI(),
                               prefix);

            /**
             * What is left now is
             *  1. nsURI = null & parent != null, but ns = null
             *  2. nsURI != null, (parent doesn't have an ns with given URI), but ns = null
             */
            if (ns == null) {
                if (prefix.isEmpty()) {
                    prefix = OMSerializerUtil.getNextNSPrefix();
                }
                ns = declareNamespace(namespaceURI, prefix);
            }
            if (ns != null) {
                this.ns = ns;
            }
        }
        return ns;
    }

    /**
     * Method handleNamespace.
     *
     * @return Returns namespace.
     */
    private OMNamespace handleNamespace(OMNamespace ns) {
        OMNamespace namespace = findNamespace(ns.getNamespaceURI(),
                                              ns.getPrefix());
        if (namespace == null) {
            namespace = declareNamespace(ns);
        }
        return namespace;
    }

    OMNamespace handleNamespace(String namespaceURI, String prefix) {
        OMNamespace namespace = findNamespace(namespaceURI,
                                              prefix);
        if (namespace == null) {
            namespace = declareNamespace(namespaceURI, prefix);
        }
        return namespace;
    }

    /**
     * Adds child to the element. One can decide whether to append the child or to add to the front
     * of the children list.
     */
    public void addChild(OMNode child) {
        if (child.getOMFactory() instanceof OMLinkedListImplFactory) {
            addChild((OMNodeImpl) child);
        } else {
            addChild(importNode(child));
        }
    }


    /**
     * Searches for children with a given QName and returns an iterator to traverse through the
     * OMNodes. This QName can contain any combination of prefix, localname and URI.
     *
     * @throws OMException
     */
    public Iterator getChildrenWithName(QName elementQName) {
        OMNode firstChild = getFirstOMChild();
        Iterator it =  new OMChildrenQNameIterator(firstChild, elementQName);
        
        // The getChidrenWithName method used to tolerate an empty namespace
        // and interpret that as getting any element that matched the local
        // name.  There are custmers of axiom that have hard-coded dependencies
        // on this semantic.
        // The following code falls back to this legacy behavior only if
        // (a) elementQName has no namespace, (b) the new iterator finds no elements
        // and (c) there are children.
        if (elementQName.getNamespaceURI().isEmpty() &&
            firstChild != null &&
            !it.hasNext()) {
            if (log.isTraceEnabled()) {
                log.trace("There are no child elements that match the unqualifed name: " +
                          elementQName);
                log.trace("Now looking for child elements that have the same local name.");
            }
            it = new OMChildrenLegacyQNameIterator(getFirstOMChild(), elementQName);
        }
        
        return it;
    }
    

    public Iterator getChildrenWithLocalName(String localName) {
        return new OMChildrenLocalNameIterator(getFirstOMChild(),
                                               localName);
    }


    public Iterator getChildrenWithNamespaceURI(String uri) {
        return new OMChildrenNamespaceIterator(getFirstOMChild(),
                                               uri);
    }


    /**
     * Method getFirstChildWithName.
     *
     * @throws OMException
     */
    public OMElement getFirstChildWithName(QName elementQName) throws OMException {
        OMChildrenQNameIterator omChildrenQNameIterator =
                new OMChildrenQNameIterator(getFirstOMChild(),
                                            elementQName);
        OMNode omNode = null;
        if (omChildrenQNameIterator.hasNext()) {
            omNode = (OMNode) omChildrenQNameIterator.next();
        }

        return ((omNode != null) && (OMNode.ELEMENT_NODE == omNode.getType())) ?
                (OMElement) omNode : null;

    }

    /** Method addChild. */
    private void addChild(OMNodeImpl child) {
        if (child.parent == this &&
            child == lastChild) {
            // The child is already the last node. 
            // We don't need to detach and re-add it.
        } else {
            // Normal Case
            
            // The order of these statements is VERY important
            // Since setting the parent has a detach method inside
            // it strips down all the rerefences to siblings.
            // setting the siblings should take place AFTER setting the parent

            child.setParent(this);

            if (firstChild == null) {
                firstChild = child;
                child.previousSibling = null;
            } else {
                child.previousSibling = (OMNodeImpl) lastChild;
                ((OMNodeImpl) lastChild).nextSibling = child;
            }

            child.nextSibling = null;
            lastChild = child;
        }

        // For a normal OMNode, the incomplete status is
        // propogated up the tree.  
        // However, a OMSourcedElement is self-contained 
        // (it has an independent parser source).
        // So only propogate the incomplete setting if this
        // is a normal OMNode
        if (!child.isComplete() && 
            !(child instanceof OMSourcedElement)) {
            this.setComplete(false);
        }

    }

    /**
     * Gets the next sibling. This can be an OMAttribute or OMText or OMELement for others.
     *
     * @throws OMException
     */
    public OMNode getNextOMSibling() throws OMException {
        while (!done && builder != null ) {
            if (builder.isCompleted()) {
                if (DEBUG_ENABLED) {
                    log.debug("Builder is complete.  Setting OMElement to complete.");
                }
                setComplete(true);
            } else {
                int token = builder.next();
                if (token == XMLStreamConstants.END_DOCUMENT) {
                    throw new OMException(
                    "Parser has already reached end of the document. No siblings found");
                }
            }
        }
        return super.getNextOMSibling();
    }

    /**
     * Returns a collection of this element. Children can be of types OMElement, OMText.
     *
     * @return Returns children.
     */
    public Iterator getChildren() {
        return new OMChildrenIterator(getFirstOMChild());
    }

    /**
     * Returns a filtered list of children - just the elements.
     *
     * @return Returns an iterator of the child elements.
     */
    public Iterator getChildElements() {
        return new OMChildElementIterator(getFirstElement());
    }

    /**
     * Creates a namespace in the current element scope.
     *
     * @return Returns namespace.
     */
    public OMNamespace declareNamespace(String uri, String prefix) {
        if (prefix != null && prefix.isEmpty())
            prefix = OMSerializerUtil.getNextNSPrefix();
        OMNamespaceImpl ns = new OMNamespaceImpl(uri, prefix);
        return declareNamespace(ns);
    }

    /**
     * We use "" to store the default namespace of this element. As one can see user can not give ""
     * as the prefix, when he declare a usual namespace.
     *
     * @param uri
     */
    public OMNamespace declareDefaultNamespace(String uri) {

        OMNamespaceImpl namespace = new OMNamespaceImpl(uri == null ? "" : uri, "");

        if (namespaces == null) {
            this.namespaces = new HashMap(5);
        }
        namespaces.put("", namespace);
        if (ns == null || ns.getPrefix() == null || ns.getPrefix().isEmpty()) {
            ns = namespace;
            this.qName = null;
        }
        return namespace;
    }

    public OMNamespace getDefaultNamespace() {
        OMNamespace defaultNS;
        if (namespaces != null && (defaultNS = (OMNamespace) namespaces.get("")) != null) {
            return defaultNS;
        }
        if (parent instanceof OMElementImpl) {
            return ((OMElementImpl) parent).getDefaultNamespace();

        }
        return null;
    }

    /** @return Returns namespace. */
    public OMNamespace declareNamespace(OMNamespace namespace) {
        if (namespaces == null) {
            this.namespaces = new HashMap(5);
        }
        String prefix = namespace.getPrefix();
        if (prefix == null) {
            prefix = OMSerializerUtil.getNextNSPrefix();
            namespace = new OMNamespaceImpl(namespace.getNamespaceURI(), prefix);
        }
        namespaces.put(prefix, namespace);
        return namespace;
    }

    /**
     * Finds a namespace with the given uri and prefix, in the scope of the document. Starts to find
     * from the current element and goes up in the hiararchy until one is found. If none is found,
     * returns null.
     */
    public OMNamespace findNamespace(String uri, String prefix) {

        // check in the current element
        OMNamespace namespace = findDeclaredNamespace(uri, prefix);
        if (namespace != null) {
            return namespace;
        }

        // go up to check with ancestors
        if (parent != null) {
            //For the OMDocumentImpl there won't be any explicit namespace
            //declarations, so going up the parent chain till the document
            //element should be enough.
            if (parent instanceof OMElement) {
                namespace = ((OMElementImpl) parent).findNamespace(uri, prefix);
            }
        }

        return namespace;
    }

    public OMNamespace findNamespaceURI(String prefix) {
        OMNamespace ns = this.namespaces == null ?
                null :
                (OMNamespace) this.namespaces.get(prefix);

        if (ns == null && this.parent instanceof OMElement) {
            // try with the parent
            ns = ((OMElement) this.parent).findNamespaceURI(prefix);
        }
        return ns;
    }

    // Constant
    static final OMNamespaceImpl xmlns =
            new OMNamespaceImpl(OMConstants.XMLNS_URI,
                                OMConstants.XMLNS_PREFIX);

    /**
     * Checks for the namespace <B>only</B> in the current Element. This is also used to retrieve
     * the prefix of a known namespace URI.
     */
    private OMNamespace findDeclaredNamespace(String uri, String prefix) {
        if (uri == null) {
            return namespaces == null ? null : (OMNamespace)namespaces.get(prefix);
        }

        //If the prefix is available and uri is available and its the xml namespace
        if (prefix != null && prefix.equals(OMConstants.XMLNS_PREFIX) &&
                uri.equals(OMConstants.XMLNS_URI)) {
            return xmlns;
        }

        if (namespaces == null) {
            return null;
        }

        if (prefix == null || prefix.isEmpty()) {

            OMNamespace defaultNamespace = this.getDefaultNamespace();
            if (defaultNamespace != null && uri.equals(defaultNamespace.getNamespaceURI())) {
                return defaultNamespace;
            }
            Iterator namespaceListIterator = namespaces.values().iterator();

            String nsUri;

            while (namespaceListIterator.hasNext()) {
                OMNamespace omNamespace =
                        (OMNamespace) namespaceListIterator.next();
                nsUri = omNamespace.getNamespaceURI();
                if (nsUri != null &&
                        nsUri.equals(uri)) {
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
     * Method getAllDeclaredNamespaces.
     *
     * @return Returns Iterator.
     */
    public Iterator getAllDeclaredNamespaces() {
        if (namespaces == null) {
            return EMPTY_ITERATOR;
        }
        return namespaces.values().iterator();
    }

    /**
     * Returns a List of OMAttributes.
     *
     * @return Returns iterator.
     */
    public Iterator getAllAttributes() {
        if (attributes == null) {
            return EMPTY_ITERATOR;
        }
        return attributes.values().iterator();
    }

    /**
     * Returns a named attribute if present.
     *
     * @param qname the qualified name to search for
     * @return Returns an OMAttribute with the given name if found, or null
     */
    public OMAttribute getAttribute(QName qname) {
        return attributes == null ? null : (OMAttribute) attributes.get(qname);
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
     * Inserts an attribute to this element. Implementor can decide as to insert this in the front
     * or at the end of set of attributes.
     *
     * <p>The owner of the attribute is set to be the particular <code>OMElement</code>.
     * If the attribute already has an owner then the attribute is cloned (i.e. its name,
     * value and namespace are copied to a new attribute) and the new attribute is added
     * to the element. It's owner is then set to be the particular <code>OMElement</code>.
     * 
     * @return The attribute that was added to the element. Note: The added attribute
     * may not be the same instance that was given to add. This can happen if the given
     * attribute already has an owner. In such case the returned attribute and the given
     * attribute are <i>equal</i> but not the same instance.
     *
     * @see OMAttributeImpl#equals(Object)
     */
    public OMAttribute addAttribute(OMAttribute attr){
        // If the attribute already has an owner element then clone the attribute (except if it is owned
        // by the this element)
        OMElement owner = attr.getOwner();
        if (owner != null) {
            if (owner == this) {
                return attr;
            }
            attr = new OMAttributeImpl(
                    attr.getLocalName(), attr.getNamespace(), attr.getAttributeValue(), attr.getOMFactory());
        }

        if (attributes == null) {
            this.attributes = new LinkedHashMap(5);
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

        // Set the owner element of the attribute
        ((OMAttributeImpl)attr).owner = this;
        OMAttributeImpl oldAttr = (OMAttributeImpl)attributes.put(attr.getQName(), attr);
        // Did we replace an existing attribute?
        if (oldAttr != null) {
            oldAttr.owner = null;
        }
        return attr;
    }

    /** Method removeAttribute. */
    public void removeAttribute(OMAttribute attr) {
        if (attributes != null) {
            // Remove the owner from this attribute
            ((OMAttributeImpl)attr).owner = null;
            attributes.remove(attr.getQName());
        }
    }

    public OMAttribute addAttribute(String attributeName, String value,
                                    OMNamespace ns) {
        OMNamespace namespace = null;
        if (ns != null) {
            String namespaceURI = ns.getNamespaceURI();
            String prefix = ns.getPrefix();
            namespace = findNamespace(namespaceURI, prefix);
            if (namespace == null) {
                namespace = new OMNamespaceImpl(namespaceURI, prefix);
            }
        }
        return addAttribute(new OMAttributeImpl(attributeName, namespace, value, this.factory));
    }

    /** Method setBuilder. */
    public void setBuilder(OMXMLParserWrapper wrapper) {
        this.builder = wrapper;
    }

    /**
     * Method getBuilder.
     *
     * @return Returns OMXMLParserWrapper.
     */
    public OMXMLParserWrapper getBuilder() {
        return builder;
    }

    /** Forces the parser to proceed, if parser has not yet finished with the XML input. */
    public void buildNext() {
        if (builder != null) {
            if (!builder.isCompleted()) {
                builder.next();
            } else {
                this.setComplete(true);
                if (DEBUG_ENABLED) {
                    log.debug("Builder is complete.  Setting OMElement to complete.");
                }
            }         
        }
    }

    /**
     * Method getFirstOMChild.
     *
     * @return Returns child.
     */
    public OMNode getFirstOMChild() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return firstChild;
    }

    public OMNode getFirstOMChildIfAvailable() {
        return firstChild;
    }


    /** Method setFirstChild. */
    public void setFirstChild(OMNode firstChild) {
        if (firstChild != null) {
            ((OMNodeEx) firstChild).setParent(this);
        }
        this.firstChild = firstChild;
    }


    public void setLastChild(OMNode omNode) {
         this.lastChild = omNode;
    }

    /**
     * Removes this information item and its children, from the model completely.
     *
     * @throws OMException
     */
    public OMNode detach() throws OMException {
        if (!done) {
            build();
        }
        super.detach();
        return this;
    }

    /** Gets the type of node, as this is the super class of all the nodes. */
    public int getType() {
        return OMNode.ELEMENT_NODE;
    }

    public void build() throws OMException {
        /**
         * builder is null. Meaning this is a programatical created element but it has children which are not completed
         * Build them all.
         */
        if (builder == null && !done) {
            for (Iterator childrenIterator = this.getChildren(); childrenIterator.hasNext();) {
                OMNode omNode = (OMNode) childrenIterator.next();
                omNode.build();
            }
        } else {
            super.build();
        }

    }

    public XMLStreamReader getXMLStreamReader() {
        return getXMLStreamReader(true);
    }

    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        return getXMLStreamReader(false);
    }

    public XMLStreamReader getXMLStreamReader(boolean cache) {
        return OMContainerHelper.getXMLStreamReader(this, cache);
    }

    /**
     * Sets the text of the given element. caution - This method will wipe out all the text elements
     * (and hence any mixed content) before setting the text.
     */
    public void setText(String text) {

        OMNode child = this.getFirstOMChild();
        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                child.detach();
            }
            child = child.getNextOMSibling();
        }

        OMAbstractFactory.getOMFactory().createOMText(this, text);
    }

    /**
     * Sets the text, as a QName, of the given element. caution - This method will wipe out all the
     * text elements (and hence any mixed content) before setting the text.
     */
    public void setText(QName text) {

        OMNode child = this.getFirstOMChild();
        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                child.detach();
            }
            child = child.getNextOMSibling();
        }

        OMAbstractFactory.getOMFactory().createOMText(this, text);
    }

    /**
     * Selects all the text children and concatenates them to a single string.
     *
     * @return Returns String.
     */
    public String getText() {
        String childText = null;
        StringBuffer buffer = null;
        OMNode child = this.getFirstOMChild();

        while (child != null) {
            final int type = child.getType();
            if (type == OMNode.TEXT_NODE || type == OMNode.CDATA_SECTION_NODE) {
                OMText textNode = (OMText) child;
                String textValue = textNode.getText();
                if (textValue != null && textValue.length() != 0) {
                    if (childText == null) {
                        // This is the first non empty text node. Just save the string.
                        childText = textValue;
                    } else {
                        // We've already seen a non empty text node before. Concatenate using
                        // a StringBuffer.
                        if (buffer == null) {
                            // This is the first text node we need to append. Initialize the
                            // StringBuffer.
                            buffer = new StringBuffer(childText);
                        }
                        buffer.append(textValue);
                    }
                }
            }
            child = child.getNextOMSibling();
        }

        if (childText == null) {
            // We didn't see any text nodes. Return an empty string.
            return "";
        } else if (buffer != null) {
            return buffer.toString();
        } else {
            return childText;
        }
    }

    public QName getTextAsQName() {
        String childText = getTrimmedText();
        if (childText != null) {
            return resolveQName(childText);
        }
        return null;
    }

    /**
     * Returns the concatination string of TRIMMED values of all OMText  child nodes of this
     * element. This is included purely to improve usability.
     */
    public String getTrimmedText() {
        String childText = null;
        StringBuffer buffer = null;
        OMNode child = this.getFirstOMChild();

        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                OMText textNode = (OMText) child;
                String textValue = textNode.getText();
                if (textValue != null && textValue.length() != 0) {
                    if (childText == null) {
                        // This is the first non empty text node. Just save the string.
                        childText = textValue.trim();
                    } else {
                        // We've already seen a non empty text node before. Concatenate using
                        // a StringBuffer.
                        if (buffer == null) {
                            // This is the first text node we need to append. Initialize the
                            // StringBuffer.
                            buffer = new StringBuffer(childText);
                        }
                        buffer.append(textValue.trim());
                    }
                }
            }
            child = child.getNextOMSibling();
        }

        if (childText == null) {
            // We didn't see any text nodes. Return an empty string.
            return "";
        } else if (buffer != null) {
            return buffer.toString();
        } else {
            return childText;
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

    public void internalSerialize(XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {

        if (cache || this.done || (this.builder == null)) {
            OMSerializerUtil.serializeStartpart(this, writer);
            OMSerializerUtil.serializeChildren(this, writer, cache);
            OMSerializerUtil.serializeEndpart(writer);
        } else {
            OMSerializerUtil.serializeByPullStream(this, writer, cache);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Gets first element.
     *
     * @return Returns element.
     */
    public OMElement getFirstElement() {
        OMNode node = getFirstOMChild();
        while (node != null) {
            if (node.getType() == OMNode.ELEMENT_NODE) {
                return (OMElement) node;
            } else {
                node = node.getNextOMSibling();
            }
        }
        return null;
    }

    /**
     * Method getLocalName.
     *
     * @return Returns local name.
     */
    public String getLocalName() {
        return localName;
    }

    /** Method setLocalName. */
    public void setLocalName(String localName) {
        this.localName = localName;
        this.qName = null;
    }

    public OMNamespace getNamespace() {
//        return ns != null ? ns : DEFAULT_DEFAULT_NS_OBJECT;
        if (ns == null) {
            // User wants to keep this element in the default default namespace. Let's try to see the default namespace
            // is overriden by some one up in the tree
            OMNamespace parentDefaultNS = this.findNamespaceURI("");

            if (parentDefaultNS != null && !"".equals(parentDefaultNS.getNamespaceURI())) {
                // if it was overriden, then we must explicitly declare default default namespace as the namespace
                // of this element
                ns = DEFAULT_DEFAULT_NS_OBJECT;
                this.qName = null;
            }
        }
        return ns;
    }

    /** Method setNamespace. */
    public void setNamespace(OMNamespace namespace) {
        OMNamespace nsObject = null;
        if (namespace != null) {
            nsObject = handleNamespace(namespace);
        }
        this.ns = nsObject;
        this.qName = null;
    }

    public void setNamespaceWithNoFindInCurrentScope(OMNamespace namespace) {
        this.ns = namespace;
        this.qName = null;
    }

    /**
     * Method getQName.
     *
     * @return Returns QName.
     */
    public QName getQName() {
        if (qName != null) {
            return qName;
        }

        if (ns != null) {
            if (ns.getPrefix() != null) {
                qName = new QName(ns.getNamespaceURI(), localName, ns.getPrefix());
            } else {
                qName = new QName(ns.getNamespaceURI(), localName);
            }
        } else {
            qName = new QName(localName);
        }
        return qName;
    }

    public String toStringWithConsume() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        XMLStreamWriter writer2 = StAXUtils.createXMLStreamWriter(writer);
        try {
            this.serializeAndConsume(writer2);
            writer2.flush();
        } finally {
            writer2.close();
        }
        return writer.toString();
    }

    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            XMLStreamWriter writer2 = StAXUtils.createXMLStreamWriter(writer);
            try {
                this.serialize(writer2);
                writer2.flush();
            } finally {
                writer2.close();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException("Can not serialize OM Element " + this.getLocalName(), e);
        }
        return writer.toString();
    }

    /**
     * Method discard.
     *
     * @throws OMException
     */
    public void discard() throws OMException {
        if (done || builder == null) {
            this.detach();
        } else {
            builder.discard(this);
        }
    }

    /**
     * Converts a prefix:local qname string into a proper QName, evaluating it in the OMElement
     * context. Unprefixed qnames resolve to the local namespace.
     *
     * @param qname prefixed qname string to resolve
     * @return Returns null for any failure to extract a qname.
     */
    public QName resolveQName(String qname) {
        ElementHelper helper = new ElementHelper(this);
        return helper.resolveQName(qname);
    }

    public OMElement cloneOMElement() {
        
        if (log.isDebugEnabled()) {
            log.debug("cloneOMElement start");
            log.debug("  element string =" + getLocalName());
            log.debug(" isComplete = " + isComplete());
            log.debug("  builder = " + builder);
        }
        // Make sure the source (this node) is completed
        if (!isComplete()) {
            this.build();
        }
        
        // Now get a parser for the full tree
        XMLStreamReader xmlStreamReader = this.getXMLStreamReader(true);
        if (log.isDebugEnabled()) {
            log.debug("  reader = " + xmlStreamReader);
        }
        
        // Get a new builder.  Use an xop aware builder if the original
        // builder is xop aware
        StAXOMBuilder newBuilder = null;
        if (builder instanceof XOPBuilder) {
            Attachments attachments = ((XOPBuilder)builder).getAttachments();
            attachments.getAllContentIDs();
            if (xmlStreamReader instanceof OMXMLStreamReader) {
                if (log.isDebugEnabled()) {
                    log.debug("  read optimized xop:include");
                }
                ((OMXMLStreamReader)xmlStreamReader).setInlineMTOM(false);
            }
            newBuilder = new XOPAwareStAXOMBuilder(xmlStreamReader, attachments);
        } else {
            newBuilder = new StAXOMBuilder(xmlStreamReader);
        }
        if (log.isDebugEnabled()) {
            log.debug("  newBuilder = " + newBuilder);
        }
        
        // Build the (target) clonedElement from the parser
        OMElement clonedElement = newBuilder.getDocumentElement();
        clonedElement.build();
        return clonedElement;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
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

    /** This method will be called when one of the children becomes complete. */
    protected void notifyChildComplete() {
        if (!this.done && builder == null) {
            Iterator iterator = getChildren();
            while (iterator.hasNext()) {
                OMNode node = (OMNode) iterator.next();
                if (!node.isComplete()) {
                    return;
                }
            }
            this.setComplete(true);
        }
    }
}

