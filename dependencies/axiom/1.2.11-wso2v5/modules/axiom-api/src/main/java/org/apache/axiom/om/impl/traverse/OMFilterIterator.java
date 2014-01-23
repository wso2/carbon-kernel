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

package org.apache.axiom.om.impl.traverse;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.axiom.om.OMNode;

/**
 * Abstract iterator that returns matching nodes from another iterator.
 */
public abstract class OMFilterIterator implements Iterator {
    private final Iterator parent;
    private OMNode nextNode;
    private boolean noMoreNodes;
    
    public OMFilterIterator(Iterator parent) {
        this.parent = parent;
    }

    /**
     * Determine whether the given node matches the filter criteria.
     * 
     * @param node the node to test
     * @return true if the node matches, i.e. if it should be returned
     *              by a call to {@link #next()}
     */
    protected abstract boolean matches(OMNode node);

    public boolean hasNext() {
        if (noMoreNodes) {
            return false;
        } else if (nextNode != null) {
            return true;
        } else {
            while (parent.hasNext()) {
                OMNode node = (OMNode)parent.next();
                if (matches(node)) {
                    nextNode = node;
                    return true;
                }
            }
            noMoreNodes = true;
            return false;
        }
    }

    public Object next() {
        if (hasNext()) {
            OMNode result = nextNode;
            nextNode = null;
            return result;
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        parent.remove();
    }
}
