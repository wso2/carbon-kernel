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

package org.apache.axis2.saaj;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The <code>NodeList</code> interface provides the abstraction of an ordered collection of nodes,
 * without defining or constraining how this collection is implemented. <code>NodeList</code>
 * objects in the DOM are live. <p>The items in the <code>NodeList</code> are accessible via an
 * integral index, starting from 0. <p>See also the <a href='http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113'>Document
 * Object Model (DOM) Level 2 Core Specification</a>.
 */
public class NodeListImpl implements NodeList {

    List mNodes;

    public static final NodeList EMPTY_NODELIST = new NodeListImpl(
            Collections.EMPTY_LIST);

    /** Constructor and Setter is intensionally made package access only. */
    NodeListImpl() {
        mNodes = new ArrayList();
    }

    NodeListImpl(List nodes) {
        this();
        mNodes.addAll(nodes);
    }

    void addNode(org.w3c.dom.Node node) {
        mNodes.add(node);
    }

    void addNodeList(org.w3c.dom.NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            mNodes.add(nodes.item(i));
        }
    }

    /**
     * Returns the <code>index</code>th item in the collection. If <code>index</code> is greater
     * than or equal to the number of nodes in the list, this returns <code>null</code>.
     *
     * @param index Index into the collection.
     * @return The node at the <code>index</code>th position in the <code>NodeList</code>, or
     *         <code>null</code> if that is not a valid index.
     */
    public Node item(int index) {
        if (mNodes != null && mNodes.size() > index) {
            return (Node)mNodes.get(index);
        } else {
            return null;
        }
    }

    /**
     * The number of nodes in the list. The range of valid child node indices is 0 to
     * <code>length-1</code> inclusive.
     */
    public int getLength() {
        return mNodes.size();
    }

}
