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
import org.apache.axiom.c14n.omwrapper.interfaces.Element;
import org.apache.axiom.c14n.omwrapper.interfaces.NamedNodeMap;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.omwrapper.interfaces.NodeList;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class ElementImpl extends NodeImpl implements Element {
    private OMElement e = null;
    private NamedNodeMap nnm = null;
    private List list = null;
    private List nodes = null;
    private NodeList nl = null;

    public ElementImpl(OMElement e, WrapperFactory fac) {
        this.fac = fac;
        this.e = e;
        node = e;
    }


    public String getTagName() {
        // for Element type getTagName() is similar to its getNodeName();
        return getNodeName();
    }

    public NamedNodeMap getAttributes() {
        if (nnm == null) { // ok now nnm == null implies this is the first time call to this method
            list = new ArrayList();
            nnm = new NamedNodeMapImpl(list, e, fac);
            Iterator itr = e.getAllAttributes();
            while (itr.hasNext()) {
                list.add(itr.next());
            }
            itr = e.getAllDeclaredNamespaces();
            while (itr.hasNext()) {
                list.add(itr.next());
            }

            Object parent = e.getParent();
            if (parent instanceof OMElement) {
                OMNamespace defaultNS = e.getDefaultNamespace();
                OMNamespace defaultParentNS = ((OMElement) parent).getDefaultNamespace();
                if (defaultNS != null && defaultParentNS != null &&
                        defaultNS.getNamespaceURI().equals(defaultParentNS.getNamespaceURI())) {
                    list.remove(defaultNS);
                }
            }
        }
        return nnm;
    }

    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }

    public boolean hasAttributes() {
        // no worries only the first time call would be expensive
        return getAttributes().getLength() != 0;
    }

    public String getNodeName() {
        // overriden getNodeName()
        QName qn = e.getQName();
        String qnPrefix = qn.getPrefix();
        if (qnPrefix == null || qnPrefix.isEmpty()) {
            return qn.getLocalPart();
        } else {
            return qnPrefix + ":" + qn.getLocalPart();
        }
    }

// For Element getNodeValue is null so no need to override
//    public String getNodeValue() {
//        return super.getNodeValue();   
//    }

    public Node getFirstChild() {
        return fac.getNode(e.getFirstOMChild());
    }

    public NodeList getChildNodes() {
        Iterator itr = null;
        if (nl == null) { // ok then this is the first call to this method
            nodes = new ArrayList();
            nl = new NodeListImpl(nodes, fac);
            itr = e.getChildren();
            while(itr.hasNext()) {
                nodes.add(itr.next());
            }
        }
        return nl;
    }

    public String getNamespaceURI() {
        OMNamespace ns = e.getNamespace();
        if (ns != null){
            return ns.getNamespaceURI();
        }
        return null;        
    }

    public String toString() {
        return e.toString();
    }

    public String getPrefix() {
        String prefix = e.getQName().getPrefix();
        if (prefix == null || prefix.isEmpty()){
            return null;
        }
        return prefix;
    }
}
