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
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** Implementation of <code>org.w3c.dom.Attr</code> and <code>org.apache.axiom.om.OMAttribute</code> */
public class AttrImpl extends NodeImpl implements OMAttribute, Attr {

    /** Name of the attribute */
    private String attrName;

    /** Attribute value */
    private TextImpl attrValue;

    /** Attribute type */
    private String attrType;

    /** Attribute namespace */
    private NamespaceImpl namespace;

    /** Flag to indicate whether this attr is used or not */
    private boolean used;

    /** Owner of this attribute */
    protected ParentNode parent;

    /** Flag used to mark an attribute as per the DOM Level 3 specification */
    protected boolean isId;

    protected AttrImpl(DocumentImpl ownerDocument, OMFactory factory) {
        super(ownerDocument, factory);
    }

    public AttrImpl(DocumentImpl ownerDocument, String localName,
                    OMNamespace ns, String value, OMFactory factory) {
        super(ownerDocument, factory);
        this.attrName = localName;
        this.attrValue = new TextImpl(ownerDocument, value, factory);
        this.attrType = OMConstants.XMLATTRTYPE_CDATA;
        this.namespace = (NamespaceImpl) ns;
    }

    public AttrImpl(DocumentImpl ownerDocument, String name, String value,
                    OMFactory factory) {
        super(ownerDocument, factory);
        this.attrName = name;
        this.attrValue = new TextImpl(ownerDocument, value, factory);
        this.attrType = OMConstants.XMLATTRTYPE_CDATA;
    }

    public AttrImpl(DocumentImpl ownerDocument, String name, OMFactory factory) {
        super(ownerDocument, factory);
        this.attrName = name;
        //If this is a default namespace attr
        if (OMConstants.XMLNS_NS_PREFIX.equals(name)) {
            this.namespace = new NamespaceImpl(
                    OMConstants.XMLNS_NS_URI, OMConstants.XMLNS_NS_PREFIX);
        }
        this.attrType = OMConstants.XMLATTRTYPE_CDATA;
    }

    public AttrImpl(DocumentImpl ownerDocument, String localName,
                    OMNamespace namespace, OMFactory factory) {
        super(ownerDocument, factory);
        this.attrName = localName;
        this.namespace = (NamespaceImpl) namespace;
        this.attrType = OMConstants.XMLATTRTYPE_CDATA;
    }

    // /
    // /org.w3c.dom.Node methods
    // /

    /** Returns the name of this attribute. */
    public String getNodeName() {
        return (this.namespace != null
                && !"".equals(this.namespace.getPrefix()) &&
                !(OMConstants.XMLNS_NS_PREFIX.equals(this.attrName)))
                ? this.namespace.getPrefix() + ":" + this.attrName
                : this.attrName;
    }

    /**
     * Returns the node type.
     *
     * @see org.w3c.dom.Node#getNodeType()
     */
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }

    /**
     * Returns the value of this attribute.
     *
     * @see org.w3c.dom.Node#getNodeValue()
     */
    public String getNodeValue() throws DOMException {
        return (this.attrValue == null) ? "" : this.attrValue.getData();
    }

    /**
     * Returns the value of this attribute.
     *
     * @see org.w3c.dom.Attr#getValue()
     */
    public String getValue() {
        return (this.attrValue == null) ? null : this.attrValue.getText();
    }

    // /
    // /org.w3c.dom.Attr methods
    // /
    public String getName() {
        if (this.namespace != null) {
            if ((OMConstants.XMLNS_NS_PREFIX.equals(this.attrName))) {
                return this.attrName;
            } else if (OMConstants.XMLNS_NS_URI.equals(this.namespace.getNamespaceURI())) {
                return OMConstants.XMLNS_NS_PREFIX + ":" + this.attrName;
            } else if (this.namespace.getPrefix().equals("")) {
                return this.attrName;
            } else {
                return this.namespace.getPrefix() + ":" + this.attrName;
            }
        } else {
            return this.attrName;
        }
    }

    /**
     * Returns the owner element.
     *
     * @see org.w3c.dom.Attr#getOwnerElement()
     */
    public Element getOwnerElement() {
        // Owned is set to an element instance when the attribute is added to an
        // element
        return (Element) (isOwned() ? parent : null);
    }

    public boolean getSpecified() {
        // Since we don't support DTD or schema, we always return true
        return true;
    }

    /**
     * Not supported: Cannot detach attributes. Use the operations available in the owner node.
     *
     * @see org.apache.axiom.om.OMNode#detach()
     */
    public OMNode detach() throws OMException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Not supported: Cannot discard attributes. Use the operations available in the owner node.
     *
     * @see org.apache.axiom.om.OMNode#discard()
     */
    public void discard() throws OMException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Returns the type of this attribute node.
     *
     * @see org.apache.axiom.om.OMNode#getType()
     */
    public int getType() {
        return -1;
    }

    /**
     * This is not supported since attributes serialization is handled by the serialization of the
     * owner nodes.
     */
    public void internalSerialize(XMLStreamWriter writer, boolean cache) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Returns the namespace of the attribute as an <code>OMNamespace</code>.
     *
     * @see org.apache.axiom.om.OMAttribute#getNamespace()
     */
    public OMNamespace getNamespace() {
        return this.namespace;
    }

    /**
     * Returns a qname representing the attribute.
     *
     * @see org.apache.axiom.om.OMAttribute#getQName()
     */
    public QName getQName() {
        return (namespace == null) ?
                new QName(this.attrName) :
                // This next bit is because QName is kind of stupid, and throws an
                // IllegalArgumentException on null prefix instead of treating it exactly
                // as if no prefix had been passed.  Grr.
                (namespace.getPrefix() == null ?
                        new QName(namespace.getNamespaceURI(), attrName) :
                        new QName(namespace.getNamespaceURI(),
                                  attrName,
                                  namespace.getPrefix()));

    }

    /**
     * Returns the attribute value.
     *
     * @see org.apache.axiom.om.OMAttribute#getAttributeValue()
     */
    public String getAttributeValue() {
        return this.attrValue.getText();
    }

    /**
     * Returns the attribute value.
     *
     * @see org.apache.axiom.om.OMAttribute#getAttributeType()
     */
    public String getAttributeType() {
        return this.attrType;
    }

    /**
     * Sets the name of attribute.
     *
     * @see org.apache.axiom.om.OMAttribute#setLocalName(String)
     */
    public void setLocalName(String localName) {
        this.attrName = localName;
    }

    /**
     * Sets the namespace of this attribute node.
     *
     * @see org.apache.axiom.om.OMAttribute#setOMNamespace (org.apache.axiom.om.OMNamespace)
     */
    public void setOMNamespace(OMNamespace omNamespace) {
        this.namespace = (NamespaceImpl) omNamespace;
    }

    /**
     * Sets the attribute value.
     *
     * @see org.apache.axiom.om.OMAttribute#setAttributeValue(String)
     */
    public void setAttributeValue(String value) {
        if (isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        this.attrValue = (TextImpl) this.getOwnerDocument().createTextNode(
                value);
    }

    /**
     * Sets the attribute value.
     *
     * @see org.apache.axiom.om.OMAttribute#setAttributeType(String)
     */
    public void setAttributeType(String attrType) {    
    	this.attrType = attrType;
    }

    /**
     * Sets the parent element to the given OMContainer.
     *
     * @see org.apache.axiom.om.impl.OMNodeEx#setParent (org.apache.axiom.om.OMContainer)
     */
    public void setParent(OMContainer element) {
        this.parent = (ParentNode) element;
    }

    /**
     * Sets the type. NOT IMPLEMENTED: Unnecessary.
     *
     * @see org.apache.axiom.om.impl.OMNodeEx#setType(int)
     */
    public void setType(int nodeType) throws OMException {
        // not necessary ???
    }

    /** @return Returns boolean. */
    protected boolean isUsed() {
        return used;
    }

    /** @param used The used to set. */
    protected void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * Sets the value of the attribute.
     *
     * @see org.w3c.dom.Attr#setValue(String)
     */
    public void setValue(String value) throws DOMException {
        this.attrValue = (TextImpl) this.getOwnerDocument().createTextNode(
                value);
    }

    /**
     * Returns the parent node of this attribute.
     *
     * @see org.apache.axiom.om.OMNode#getParent()
     */
    public OMContainer getParent() {
        return this.parent;
    }

    /**
     * Returns the attribute name.
     *
     * @see org.w3c.dom.Node#getLocalName()
     */
    public String getLocalName() {
        return (this.namespace == null) ? this.attrName : DOMUtil
                .getLocalName(this.attrName);
        
    }

    /**
     * Returns the namespace URI of this attr node.
     *
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    public String getNamespaceURI() {
        return (this.namespace != null) ? namespace.getNamespaceURI() : null;
    }

    /**
     * Returns the namespace prefix of this attr node.
     *
     * @see org.w3c.dom.Node#getPrefix()
     */
    public String getPrefix() {
        // TODO Error checking
        return (this.namespace == null) ? null : this.namespace.getPrefix();
    }

    public Node cloneNode(boolean deep) {

        AttrImpl clone = (AttrImpl) super.cloneNode(deep);

        if (clone.attrValue == null) {
            // Need to break the association w/ original kids
            clone.attrValue = new TextImpl(this.attrValue.toString(), factory);
            if (this.attrValue.nextSibling != null) {
                throw new UnsupportedOperationException(
                        "Attribute value can contain only a text " +
                                "node with out any siblings");
            }
        }
        clone.isSpecified(true);
        clone.setParent(null);
        clone.setUsed(false);
        return clone;
    }

    /*
     * DOM-Level 3 methods
     */
    public TypeInfo getSchemaTypeInfo() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isId() {
        return isId;
    }

    public String toString() {
        return (this.namespace == null) ? this.attrName : this.namespace
                .getPrefix()
                + ":" + this.attrName;
    }

    /**
     * Returns the owner element of this attribute
     * @return OMElement - if the parent OMContainer is an instanceof OMElement
     * we return that OMElement else return null. To get the OMContainer itself use
     * getParent() method.
     */
    public OMElement getOwner() {
        return (parent instanceof OMElement) ? (OMElement)parent : null;
    }

    /**
     * An instance of <code>AttrImpl</code> can act as an <code>OMAttribute</code> and as well as an
     * <code>org.w3c.dom.Attr</code>. So we first check if the object to compare with (<code>obj</code>)
     * is of type <code>OMAttribute</code> (this includes instances of <code>OMAttributeImpl</code> or
     * <code>AttrImpl</code> (instances of this class)). If so we check for the equality
     * of namespaces first (note that if the namespace of this instance is null then for the <code>obj</code>
     * to be equal its namespace must also be null). This condition solely doesn't determine the equality.
     * So we check for the equality of names and values (note that the value can also be null in which case
     * the same argument holds as that for the namespace) of the two instances. If all three conditions are
     * met then we say the two instances are equal.
     *
     * <p>If <code>obj</code> is of type <code>org.w3c.dom.Attr</code> then we perform the same equality check
     * as before. Note that, however, the implementation of the test for equality in this case is little different
     * than before.
     *
     * <p>If <code>obj</code> is neither of type <code>OMAttribute</code> nor of type <code>org.w3c.dom.Attr</code>
     * then we return false.
     *
     * @param obj The object to compare with this instance
     * @return True if the two objects are equal or else false. The equality is checked as explained above.
     */
    public boolean equals(Object obj) {
        if (obj instanceof OMAttribute) { // Checks equality of an OMAttributeImpl or an AttrImpl with this instance
            OMAttribute other = (OMAttribute) obj;
            return (namespace == null ? other.getNamespace() == null :
                    namespace.equals(other.getNamespace()) &&
                    attrName.equals(other.getLocalName()) &&
                    (attrValue == null ? other.getAttributeValue() == null :
                            attrValue.toString().equals(other.getAttributeValue())));
        } else if (obj instanceof Attr) {// Checks equality of an org.w3c.dom.Attr with this instance
            Attr other = (Attr)obj;
            String otherNs = other.getNamespaceURI();
            if (namespace == null) { // I don't have a namespace
                if (otherNs != null) {
                    return false; // I don't have a namespace and the other has. So return false
                } else {
                    // Both of us don't have namespaces. So check for name and value equality only
                    return (attrName.equals(other.getLocalName()) &&
                            (attrValue == null ? other.getValue() == null :
                                    attrValue.toString().equals(other.getValue())));
                }
            } else { // Ok, now I've a namespace
                String ns = namespace.getNamespaceURI();
                String prefix = namespace.getPrefix();
                String otherPrefix = other.getPrefix();
                // First check for namespaceURI equality. Then check for prefix equality.
                // Then check for name and value equality
                return (ns.equals(otherNs) && (prefix == null ? otherPrefix == null : prefix.equals(otherPrefix)) &&
                        (attrName.equals(other.getLocalName())) &&
                        (attrValue == null ? other.getValue() == null :
                                attrValue.toString().equals(other.getValue())));
            }
        }
        return false;
    }

    public int hashCode() {
        return attrName.hashCode() ^ (attrValue != null ? attrValue.toString().hashCode() : 0) ^
                (namespace != null ? namespace.hashCode() : 0);
    }

}
