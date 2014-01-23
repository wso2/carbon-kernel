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

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMNode;

/**
 * Iterator that iterates over all descendants in document order.
 */
public class OMDescendantsIterator extends OMAbstractIterator {
    private int level;
    
    public OMDescendantsIterator(OMNode firstNode) {
        super(firstNode);
    }

    protected OMNode getNextNode(OMNode currentNode) {
        if (currentNode instanceof OMContainer) {
            OMNode firstChild = ((OMContainer)currentNode).getFirstOMChild();
            if (firstChild != null) {
                level++;
                return firstChild;
            }
        }
        OMNode node = currentNode;
        while (true) {
            OMNode nextSibling = node.getNextOMSibling();
            if (nextSibling != null) {
                return nextSibling;
            } else if (level == 0) {
                return null;
            } else {
                node = (OMNode)node.getParent();
                level--;
            }
        }
    }
}
