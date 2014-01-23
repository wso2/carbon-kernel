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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import java.util.Vector;

public class AttributeMap extends NamedNodeMapImpl {

    /** @param ownerNode  */
    protected AttributeMap(ParentNode ownerNode) {
        super(ownerNode);
    }

    public Node removeNamedItem(String name) throws DOMException {
        // TODO Set used to false
        return super.removeNamedItem(name);
    }

    public Node removeNamedItemNS(String namespaceURI, String name)
            throws DOMException {
        // TODO
        return super.removeNamedItemNS(namespaceURI, name);
    }

    /** Almost a copy of the Xerces impl. */
    public Node setNamedItem(Node attribute) throws DOMException {

        if (isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        if (attribute.getOwnerDocument() != ownerNode.getOwnerDocument()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.WRONG_DOCUMENT_ERR, null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
        }
        if (attribute.getNodeType() != Node.ATTRIBUTE_NODE) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.HIERARCHY_REQUEST_ERR,
                    null);
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
        }

        AttrImpl attr = (AttrImpl) attribute;
        if (attr.isOwned()) { // If the attribute is owned then:
            if (attr.getOwnerElement() != this.ownerNode) // the owner must be
                // the owner of this
                // list
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR,
                                       DOMMessageFormatter.formatMessage(
                                               DOMMessageFormatter.DOM_DOMAIN,
                                               DOMException.INUSE_ATTRIBUTE_ERR, null));
            else
                return attr; // No point adding the 'same' attr again to the
            // same element
        }

        attr.parent = this.ownerNode; // Set the owner node
        attr.isOwned(true); // To indicate that this attr belong to an element
        attr.setUsed(true); // Setting used to true

        int i = findNamePoint(attr.getNodeName(), 0);

        AttrImpl previous = null;
        if (i >= 0) { // There's an attribute already with this attr's name
            previous = (AttrImpl) nodes.elementAt(i);
            nodes.setElementAt(attr, i);
            previous.parent = null;
            previous.isOwned(false);

            // make sure it won't be mistaken with defaults in case it's reused
            previous.isSpecified(true);
        } else {
            i = -1 - i; // Insert point (may be end of list)
            if (null == nodes) {
                nodes = new Vector(5, 10);
            }
            nodes.insertElementAt(attr, i);
        }

        // - Not sure whether we really need this
        // // notify document
        // ownerNode.getOwnerDocument().setAttrNode(attr, previous);

        // If the new attribute is not normalized,
        // the owning element is inherently not normalized.
        if (!attr.isNormalized()) {
            ownerNode.isNormalized(false);
        }
        return previous;

    }

    /** Almost a copy of the Xerces impl. */
    public Node setNamedItemNS(Node attribute) throws DOMException {
        if (isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        if (attribute.getOwnerDocument() != ownerNode.getOwnerDocument()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.WRONG_DOCUMENT_ERR, null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
        }
        if (attribute.getNodeType() != Node.ATTRIBUTE_NODE) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.HIERARCHY_REQUEST_ERR,
                    null);
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
        }

        AttrImpl attr = (AttrImpl) attribute;
        if (attr.isOwned()) { // If the attribute is owned then:
            //the owner must be the owner of this list
            if (attr.getOwnerElement() != this.ownerNode)
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR,
                                       DOMMessageFormatter.formatMessage(
                                               DOMMessageFormatter.DOM_DOMAIN,
                                               DOMException.INUSE_ATTRIBUTE_ERR, null));
            else
                return attr; // No point adding the 'same' attr again to the
            // same element
        }
        //Set the owner node
        attr.ownerNode = (DocumentImpl) this.ownerNode.getOwnerDocument();
        attr.parent = this.ownerNode;
        attr.isOwned(true); // To indicate that this attr belong to an element

        int i = findNamePoint(attr.getNamespaceURI(), attr.getLocalName());
        AttrImpl previous = null;

        if (i >= 0) {
            previous = (AttrImpl) nodes.elementAt(i);
            nodes.setElementAt(attr, i);
            previous.ownerNode = (DocumentImpl) this.ownerNode
                    .getOwnerDocument();
            previous.parent = null;
            previous.isOwned(false);
            // make sure it won't be mistaken with defaults in case it's reused
            previous.isSpecified(true);
        } else {
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i = findNamePoint(attr.getNodeName(), 0);
            if (i >= 0) {
                previous = (AttrImpl) nodes.elementAt(i);
                nodes.insertElementAt(attr, i);
            } else {
                i = -1 - i; // Insert point (may be end of list)
                if (null == nodes) {
                    nodes = new Vector(5, 10);
                }
                nodes.insertElementAt(attr, i);
            }
        }

        // If the new attribute is not normalized,
        // the owning element is inherently not normalized.
        if (!attr.isNormalized()) {
            ownerNode.isNormalized(false);
        }
        return previous;
    }

    /**
     * BORROWED from Xerces impl. Cloning a NamedNodeMap is a DEEP OPERATION; it always clones all
     * the nodes contained in the map.
     */

    public NamedNodeMapImpl cloneMap(NodeImpl ownerNode) {
        AttributeMap newmap = new AttributeMap((ParentNode) ownerNode);
        newmap.hasDefaults(hasDefaults());
        newmap.cloneContent(this);
        return newmap;
    } // cloneMap():AttributeMap

    /** BORROWED from Xerces impl. */
    protected void cloneContent(NamedNodeMapImpl srcmap) {
        Vector srcnodes = srcmap.nodes;
        if (srcnodes != null) {
            int size = srcnodes.size();
            if (size != 0) {
                if (nodes == null) {
                    nodes = new Vector(size);
                }
                nodes.setSize(size);
                for (int i = 0; i < size; ++i) {
                    NodeImpl n = (NodeImpl) srcnodes.elementAt(i);
                    NodeImpl clone = (NodeImpl) n.cloneNode(true);
                    clone.isSpecified(n.isSpecified());
                    nodes.setElementAt(clone, i);
                    clone.ownerNode = this.ownerNode.ownerNode;
                    clone.isOwned(true);
                }
            }
        }
    } // cloneContent():AttributeMap

}
