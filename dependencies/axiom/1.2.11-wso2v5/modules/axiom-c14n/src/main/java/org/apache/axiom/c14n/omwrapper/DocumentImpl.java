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
import org.apache.axiom.c14n.omwrapper.interfaces.Document;
import org.apache.axiom.c14n.omwrapper.interfaces.Element;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.omwrapper.interfaces.NodeList;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class DocumentImpl extends NodeImpl implements Document {
    private OMDocument doc = null;
    private List list = null;
    private NodeList nl = null;

    public DocumentImpl(OMDocument doc, WrapperFactory fac){
        this.fac = fac;
        this.doc = doc;
    }

    public Element getDocumentElement() {
        return (Element)fac.getNode(doc.getOMDocumentElement());
    }

    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }

    public Node getFirstChild() {
        // an XML Document node should not give Text nodes as children.
        OMNode n = doc.getFirstOMChild();
        do {
            if (!(n instanceof OMText)){
                return fac.getNode(n);
            }
            n = n.getNextOMSibling();
        } while (true);

    }

    public NodeList getChildNodes() {
        Iterator itr = null;
        if (nl == null) { // ok then this is the first call to this method
            list = new ArrayList();
            nl = new NodeListImpl(list, fac);
            itr = doc.getChildren();
            Object o;
            while(itr.hasNext()) {
                o = itr.next();
                // Axiom returns OMText nodes even for newline characters
                // we don't want these to be returned from an XML Document node
                if (o instanceof OMText) {
                    continue;
                }
                list.add(o);
            }
        }
        return nl;
    }

    public Node getNextSibling() {
        return null;
    }

    public Node getParentNode() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

}
