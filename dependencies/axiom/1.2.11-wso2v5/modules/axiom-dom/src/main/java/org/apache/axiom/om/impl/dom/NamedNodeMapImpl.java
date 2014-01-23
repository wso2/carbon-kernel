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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Vector;

/** Most of the implementation is taken from org.apache.xerces.dom.NamedNodeMapImpl */
public class NamedNodeMapImpl implements NamedNodeMap {

    Vector nodes;

    ParentNode ownerNode;

    //
    // Data
    //

    protected short flags;

    protected final static short READONLY = 0x1 << 0;

    protected final static short CHANGED = 0x1 << 1;

    protected final static short HASDEFAULTS = 0x1 << 2;
                            
    protected NamedNodeMapImpl(ParentNode ownerNode) {
        this.ownerNode = ownerNode;
    }

    /**
     * 
     */
    public Node getNamedItem(String name) {
        int i = findNamePoint(name, 0);
        return (i < 0) ? null : (Node) (nodes.elementAt(i));

    }

    /** From org.apache.xerces.dom.NamedNodeMapImpl */
    public Node item(int index) {
        return (nodes != null && index < nodes.size()) ? (Node) (nodes
                .elementAt(index)) : null;
    }

    /** From org.apache.xerces.dom.NamedNodeMapImpl */
    public int getLength() {
        return (nodes != null) ? nodes.size() : 0;
    }

    /**
     * Removes a node specified by name.
     * 
     * @param name
     *            The name of a node to remove.
     * @return Returns the node removed from the map if a node with such a name
     *         exists.
     */
    /***/
    public Node removeNamedItem(String name) throws DOMException {

        if (isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        int i = findNamePoint(name, 0);
        if (i < 0) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.NOT_FOUND_ERR, null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }

        NodeImpl n = (NodeImpl) nodes.elementAt(i);
        nodes.removeElementAt(i);

        return n;

    } // removeNamedItem(String):Node

    /**
     * Introduced in DOM Level 2. Retrieves a node specified by local name and namespace URI.
     *
     * @param namespaceURI The namespace URI of the node to retrieve. When it is null or an empty
     *                     string, this method behaves like getNamedItem.
     * @param localName    The local name of the node to retrieve.
     * @return Returns s Node (of any type) with the specified name, or null if the specified name
     *         did not identify any node in the map.
     */
    public Node getNamedItemNS(String namespaceURI, String localName) {

        int i = findNamePoint(namespaceURI, localName);
        return (i < 0) ? null : (Node) (nodes.elementAt(i));

    } // getNamedItemNS(String,String):Node

    /**
     * Adds a node using its namespaceURI and localName.
     *
     * @param arg A node to store in a named node map. The node will later be accessible using the
     *            value of the namespaceURI and localName attribute of the node. If a node with
     *            those namespace URI and local name is already present in the map, it is replaced
     *            by the new one.
     * @return Returns the replaced Node if the new Node replaces an existing node else returns
     *         null.
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     */
    public Node setNamedItemNS(Node arg) throws DOMException {

        DocumentImpl ownerDocument = (DocumentImpl) ownerNode
                .getOwnerDocument();
        if (isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }

        if (arg.getOwnerDocument() != ownerDocument) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.WRONG_DOCUMENT_ERR, null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
        }

        int i = findNamePoint(arg.getNamespaceURI(), arg.getLocalName());
        NodeImpl previous = null;
        if (i >= 0) {
            previous = (NodeImpl) nodes.elementAt(i);
            nodes.setElementAt(arg, i);
        } else {
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i = findNamePoint(arg.getNodeName(), 0);
            if (i >= 0) {
                previous = (NodeImpl) nodes.elementAt(i);
                nodes.insertElementAt(arg, i);
            } else {
                i = -1 - i; // Insert point (may be end of list)
                if (null == nodes) {
                    nodes = new Vector(5, 10);
                }
                nodes.insertElementAt(arg, i);
            }
        }
        return previous;

    } // setNamedItemNS(Node):Node

    /**
     * Introduced in DOM Level 2. Removes a node specified by local name and namespace URI.
     *
     * @param namespaceURI The namespace URI of the node to remove. When it is null or an empty
     *                     string, this method behaves like removeNamedItem.
     * @param name         The local name of the node to remove.
     * @return Returns the node removed from the map if a node with such a local name and namespace
     *         URI exists.
     * @throws DOMException: Raised if there is no node named name in the map.
     */
    public Node removeNamedItemNS(String namespaceURI, String name)
            throws DOMException {

        if (isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        int i = findNamePoint(namespaceURI, name);
        if (i < 0) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.NOT_FOUND_ERR, null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }

        NodeImpl n = (NodeImpl) nodes.elementAt(i);
        nodes.removeElementAt(i);

        return n;

    } // removeNamedItem(String):Node

    /**
     * Adds a node using its nodeName attribute. As the nodeName attribute is used to derive the
     * name which the node must be stored under, multiple nodes of certain types (those that have a
     * "special" string value) cannot be stored as the names would clash. This is seen as preferable
     * to allowing nodes to be aliased.
     *
     * @param arg A node to store in a named node map. The node will later be accessible using the
     *            value of the namespaceURI and localName attribute of the node. If a node with
     *            those namespace URI and local name is already present in the map, it is replaced
     *            by the new one.
     * @return Returns the replaced Node if the new Node replaces an existing node, otherwise
     *         returns null.
     * @throws org.w3c.dom.DOMException The exception description.
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     */
    public Node setNamedItem(Node arg) throws DOMException {

        DocumentImpl ownerDocument = (DocumentImpl) ownerNode
                .getOwnerDocument();

        if (isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   msg);
        }
        if (arg.getOwnerDocument() != ownerDocument) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN, DOMException.WRONG_DOCUMENT_ERR, null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
        }

        int i = findNamePoint(arg.getNodeName(), 0);
        NodeImpl previous = null;
        if (i >= 0) {
            previous = (NodeImpl) nodes.elementAt(i);
            nodes.setElementAt(arg, i);
        } else {
            i = -1 - i; // Insert point (may be end of list)
            if (null == nodes) {
                nodes = new Vector(5, 10);
            }
            nodes.insertElementAt(arg, i);
        }
        return previous;

    } // setNamedItem(Node):Node

    final boolean isReadOnly() {
        return (flags & READONLY) != 0;
    }

    final void isReadOnly(boolean value) {
        flags = (short) (value ? flags | READONLY : flags & ~READONLY);
    }

    final boolean changed() {
        return (flags & CHANGED) != 0;
    }

    final void changed(boolean value) {
        flags = (short) (value ? flags | CHANGED : flags & ~CHANGED);
    }

    final boolean hasDefaults() {
        return (flags & HASDEFAULTS) != 0;
    }

    final void hasDefaults(boolean value) {
        flags = (short) (value ? flags | HASDEFAULTS : flags & ~HASDEFAULTS);
    }

    /**
     * From org.apache.xerces.dom.NamedNodeMapImpl
     * <p/>
     * Subroutine: Locates the named item, or the point at which said item should be added.
     *
     * @param name Name of a node to look up.
     * @return If positive or zero, the index of the found item. If negative, index of the
     *         appropriate point at which to insert the item, encoded as -1-index and hence
     *         reconvertable by subtracting it from -1. (Encoding because I don't want to recompare
     *         the strings but don't want to burn bytes on a datatype to hold a flagged value.)
     */
    protected int findNamePoint(String name, int start) {

        // Binary search
        int i = 0;
        if (nodes != null) {
            int first = start;
            int last = nodes.size() - 1;

            while (first <= last) {
                i = (first + last) / 2;
                int test = name.compareTo(((Node) (nodes.elementAt(i)))
                        .getNodeName());
                if (test == 0) {
                    return i; // Name found
                } else if (test < 0) {
                    last = i - 1;
                } else {
                    first = i + 1;
                }
            }

            if (first > i) {
                i = first;
            }
        }

        return -1 - i; // not-found has to be encoded.

    } // findNamePoint(String):int

    /** This findNamePoint is for DOM Level 2 Namespaces. */
    protected int findNamePoint(String namespaceURI, String name) {

        if (nodes == null)
            return -1;
        if (name == null)
            return -1;

        // This is a linear search through the same nodes Vector.
        // The Vector is sorted on the DOM Level 1 nodename.
        // The DOM Level 2 NS keys are namespaceURI and Localname,
        // so we must linear search thru it.
        // In addition, to get this to work with nodes without any namespace
        // (namespaceURI and localNames are both null) we then use the nodeName
        // as a secondary key.
        for (int i = 0; i < nodes.size(); i++) {
            NodeImpl a = (NodeImpl) nodes.elementAt(i);
            String aNamespaceURI = a.getNamespaceURI();
            String aLocalName = a.getLocalName();
            if (namespaceURI == null) {
                if (aNamespaceURI == null && (name.equals(aLocalName) ||
                        (aLocalName == null && name.equals(a.getNodeName()))))
                    return i;
            } else {
                if (namespaceURI.equals(aNamespaceURI)
                        && name.equals(aLocalName))
                    return i;
            }
        }
        return -1;
    }

    // Compare 2 nodes in the map. If a precedes b, return true, otherwise
    // return false
    protected boolean precedes(Node a, Node b) {

        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                Node n = (Node) nodes.elementAt(i);
                if (n == a)
                    return true;
                if (n == b)
                    return false;
            }
        }

        return false;
    }

    /** NON-DOM: Remove attribute at specified index. */
    protected void removeItem(int index) {
        if (nodes != null && index < nodes.size()) {
            nodes.removeElementAt(index);
        }
    }

    protected Object getItem(int index) {
        if (nodes != null) {
            return nodes.elementAt(index);
        }
        return null;
    }

    protected int addItem(Node arg) {
        int i = findNamePoint(arg.getNamespaceURI(), arg.getLocalName());
        if (i >= 0) {
            nodes.setElementAt(arg, i);
        } else {
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i = findNamePoint(arg.getNodeName(), 0);
            if (i >= 0) {
                nodes.insertElementAt(arg, i);
            } else {
                i = -1 - i; // Insert point (may be end of list)
                if (null == nodes) {
                    nodes = new Vector(5, 10);
                }
                nodes.insertElementAt(arg, i);
            }
        }
        return i;
    }

    /**
     * NON-DOM: copy content of this map into the specified vector
     *
     * @param list Vector to copy information into.
     * @return Returns a copy of this node named map.
     */
    protected Vector cloneMap(Vector list) {
        if (list == null) {
            list = new Vector(5, 10);
        }
        list.setSize(0);
        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                list.insertElementAt(nodes.elementAt(i), i);
            }
        }

        return list;
    }

    protected int getNamedItemIndex(String namespaceURI, String localName) {
        return findNamePoint(namespaceURI, localName);
    }

    /** NON-DOM remove all elements from this map. */
    public void removeAll() {
        if (nodes != null) {
            nodes.removeAllElements();
        }
    }

}
