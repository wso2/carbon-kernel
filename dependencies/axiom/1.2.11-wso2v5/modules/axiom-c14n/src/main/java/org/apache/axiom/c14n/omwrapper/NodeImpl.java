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

package org.apache.axiom.c14n.omwrapper;

import org.apache.axiom.c14n.omwrapper.factory.WrapperFactory;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.omwrapper.interfaces.NodeList;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class NodeImpl implements Node {
    protected WrapperFactory fac = null;
    protected OMNode node = null;
    protected Node next = null;
    protected Node prev = null;
    public String getNodeName() {
        return null;
    }

    public short getNodeType() {
        return 0;
    }

    public String getNodeValue() {
        return null;
    }

    public String getNamespaceURI() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getNextSibling() {
       if (next == null) {
            if (node.getParent() instanceof OMDocument) {
                OMNode n = node.getNextOMSibling();
                do {
                    if (!(n instanceof OMText)) {
                        next = fac.getNode(n);
                        break;
                    }
                    n = n.getNextOMSibling();
                } while (true);
            } else {
                next = fac.getNode(node.getNextOMSibling());
            }
        }
        return next;
    }

    public Node getPreviousSibling() {
        if (prev == null) {
            if (node.getParent() instanceof OMDocument) {
                OMNode n = node.getPreviousOMSibling();
                do {
                    if (!(n instanceof OMText)) {
                        prev = fac.getNode(n);
                        break;
                    }
                    n = n.getPreviousOMSibling();
                } while (true);
            } else {
                prev = fac.getNode(node.getPreviousOMSibling());
            }
        }
        return prev;
    }

    public Node getParentNode() {
        return fac.getNode(node.getParent());
    }

    public NodeList getChildNodes() {
        return null;
    }
}
