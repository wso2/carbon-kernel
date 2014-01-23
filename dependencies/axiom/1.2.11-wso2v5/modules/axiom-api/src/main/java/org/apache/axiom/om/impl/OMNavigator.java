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

package org.apache.axiom.om.impl;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMSerializable;
import org.apache.axiom.om.OMSourcedElement;

/**
 * Refer to the test, org.apache.axiom.om.OMNavigatorTest, to find out how to use features like
 * isNavigable, isComplete and step.
 */
public class OMNavigator {
    /** Field node */
    protected OMSerializable node;

    /** Field visited */
    private boolean visited;

    /** Field next */
    private OMSerializable next;

    // root is the starting element. Once the navigator comes back to the
    // root, the traversal is terminated

    /** Field root */
    private OMSerializable root;

    /** Field backtracked */
    private boolean backtracked;

    // flags that tell the status of the navigator

    /** Field end */
    private boolean end = false;

    /** Field start */
    private boolean start = true;
    
    // Indicates if an OMSourcedElement with an OMDataSource should
    // be considered as an interior node or a leaf node.
    private boolean isDataSourceALeaf = false;

    /** Constructor OMNavigator. */
    public OMNavigator() {
    }

    /**
     * Constructor OMNavigator.
     *
     * @param node
     */
    public OMNavigator(OMSerializable node) {
        init(node);
    }

    /**
     * Method init.
     *
     * @param node
     */
    public void init(OMSerializable node) {
        next = node;
        root = node;
        backtracked = false;
    }
    
    /**
     * Indicate if an OMSourcedElement with a OMDataSource
     * should be considered as an interior element node or as
     * a leaf.  
     * @param value boolean
     */
    public void setDataSourceIsLeaf(boolean value) {
        isDataSourceALeaf = value;
    }

    /**
     * Get the next information item.
     * 
     * @return the next information item in the sequence of preorder traversal. Note however that a
     *         container (document or element) is treated slightly differently. Once the container
     *         is passed it returns the same item in the next encounter as well.
     */
    public OMSerializable getNext() {
        if (next == null) {
            return null;
        }
        node = next;
        visited = backtracked;
        backtracked = false;
        updateNextNode();

        // set the starting and ending flags
        if (root.equals(node)) {
            if (!start) {
                end = true;
            } else {
                start = false;
            }
        }
        return node;
    }
    
    /**
     * Get the next node. This method only exists for compatibility with existing code. It may throw
     * a {@link ClassCastException} if an attempt is made to use it on a navigator that was created
     * from an {@link OMDocument}.
     * 
     * @return the next node
     * @see #getNext()
     */
    public OMNode next() {
        return (OMNode)getNext();
    }

    /** Private method to encapsulate the searching logic. */
    private void updateNextNode() {
        if (!isLeaf(next) && !visited) {
            OMNode firstChild = _getFirstChild((OMContainer) next);
            if (firstChild != null) {
                next = firstChild;
            } else if (next.isComplete()) {
                backtracked = true;
            } else {
                next = null;
            }
        } else {
            if (next instanceof OMDocument) {
                next = null;
            } else {
                OMNode nextNode = (OMNode)next;
                OMContainer parent = nextNode.getParent();
                OMNode nextSibling = getNextSibling(nextNode);
                if (nextSibling != null) {
                    next = nextSibling;
                } else if ((parent != null) && parent.isComplete()) {
                    next = parent;
                    backtracked = true;
                } else {
                    next = null;
                }
            }
        }
    }
    
    /**
     * @param n OMNode
     * @return true if this OMNode should be considered a leaf node
     */
    private boolean isLeaf(OMSerializable n) {
        if (n instanceof OMContainer) {
            if (this.isDataSourceALeaf && (n instanceof OMSourcedElement) && n != root) {
                OMDataSource ds = null;
                try {
                    ds = ((OMSourcedElement) n).getDataSource();
                } catch (UnsupportedOperationException e) {
                    ; // Operation unsupported for some implementations
                }
                if (ds != null) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param node
     * @return first child or null
     */
    private OMNode _getFirstChild(OMContainer node) {
        if (node instanceof OMSourcedElement) {
            OMNode first = node.getFirstOMChild();
            OMNode sibling = first;
            while (sibling != null) {
                sibling = sibling.getNextOMSibling();
            }
            return first;
        } else {
            return ((OMContainerEx) node).getFirstOMChildIfAvailable();
        }
    }

    /**
     * @param node
     * @return next sibling or null
     */
    private OMNode getNextSibling(OMNode node) {
        if (node instanceof OMSourcedElement) {
            return node.getNextOMSibling();
        } else {
            return ((OMNodeEx) node).getNextOMSiblingIfAvailable();
        }
    }

    /**
     * Method visited.
     *
     * @return Returns boolean.
     */
    public boolean visited() {
        return visited;
    }

    /**
     * This is a very special method. This allows the navigator to step once it has reached the
     * existing OM. At this point the isNavigable method will return false but the isComplete method
     * may return false which means that the navigating the given element is not complete and the
     * navigator cannot proceed.
     */
    public void step() {
        if (!end) {
            next = node;
            updateNextNode();
        }
    }

    /**
     * Returns the navigable status.
     *
     * @return Returns boolean.
     */
    public boolean isNavigable() {
        if (end) {
            return false;
        } else {
            return !(next == null);
        }
    }

    /**
     * Returns the completed status.
     *
     * @return Returns boolean.
     */
    public boolean isCompleted() {
        return end;
    }
}
