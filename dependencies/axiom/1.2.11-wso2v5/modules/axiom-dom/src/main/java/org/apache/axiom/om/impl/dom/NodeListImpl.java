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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

/** Implementation of org.w3c.dom.NodeList */
public abstract class NodeListImpl implements NodeList {
    protected abstract Iterator getIterator();

    /**
     * Returns the number of nodes.
     *
     * @see org.w3c.dom.NodeList#getLength()
     */
    public int getLength() {
        Iterator children = getIterator();
        int count = 0;
        while (children.hasNext()) {
            count++;
            children.next();
        }
        return count;
    }

    /**
     * Returns the node at the given index. Returns null if the index is invalid.
     *
     * @see org.w3c.dom.NodeList#item(int)
     */
    public Node item(int index) {
        Iterator children = getIterator();
        int count = 0;
        while (children.hasNext()) {
            if (count == index) {
                return (Node) children.next();
            } else {
                children.next();
            }
            count++;
        }
        return null;
    }
}
