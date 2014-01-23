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
import org.apache.axiom.c14n.omwrapper.interfaces.Attr;
import org.apache.axiom.c14n.omwrapper.interfaces.Element;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class AttrImpl extends NodeImpl implements Attr {
    public static final String XMLNS = "xmlns";
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    private boolean isOMAttribute = false;
    private OMElement parent = null;
    private OMNamespace omns = null;
    private OMAttribute omattr = null;

    public AttrImpl(Object o, OMElement parent, WrapperFactory fac){
        this.parent = parent;
        this.fac = fac;
        if (o instanceof OMAttribute) {
            isOMAttribute = true;
            omattr = (OMAttribute)o;
        } else {
            isOMAttribute = false;
            omns = (OMNamespace)o;
        }
    }

    public String getName() {
        // for attributes getName() is the same as getNodeName()
        return getNodeName();
    }

    public String getValue() {
        // for attributes getValue() is the same as getNodeValue()
        return getNodeValue();
    }

    public Element getOwnerElement() {
        return (Element) fac.getNode(parent);
    }

    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }

    public String getLocalName() {
        return (isOMAttribute) ? omattr.getLocalName() : getOMNamespaceLocalName();
    }

    public String getNamespaceURI() {
        return (isOMAttribute) ? getOMAttributeNsURI() : XMLNS_URI;
    }

    // overridden getNodeName() method
    public String getNodeName() {
        return (isOMAttribute) ? getOMAttributeName() : getOMNamespaceName();
    }

    // overridden getNodeValue() method
    public String getNodeValue() {
        return (isOMAttribute) ? omattr.getAttributeValue() : omns.getNamespaceURI();
    }

    public String getPrefix() {
        return (isOMAttribute) ? getOMAttributePrefix() : getOMNamespacePrefix();
    }

    // a call to this method assumes that AttrImpl wraps an OMAttribute
    private String getOMAttributeName(){
        QName qn = omattr.getQName();
        String qnPrefix = qn.getPrefix();
        if (qnPrefix == null || qnPrefix.isEmpty()) {
            return qn.getLocalPart();
        }
        return qnPrefix + ":" + qn.getLocalPart();
    }

    // a call to this method assumes that AttrImpl wraps an OMNamespace
    private String getOMNamespaceName() {
        String prefix = omns.getPrefix();
        if(prefix == null || prefix.isEmpty()) {
            return XMLNS;
        }
        return XMLNS + ":" + prefix;
    }

    // a call to this method assumes that AttrImpl wraps an OMNamespace
    private String getOMNamespaceLocalName() {
        String prefix = omns.getPrefix();
        if(prefix == null || prefix.isEmpty()) {
            return XMLNS;
        }
        return prefix;
    }

    // a call to this method assumes that AttrImpl wraps an OMAttribute
    private String getOMAttributeNsURI() {
        String nsURI = omattr.getQName().getNamespaceURI();
        if (nsURI == null || nsURI.isEmpty()) {
            return null;
        }
        return nsURI;
    }

    // a call to this method assumes that AttrImpl wraps an OMAttribute
    private String getOMAttributePrefix() {
        String prefix = omattr.getQName().getPrefix();
        if (prefix == null || prefix.isEmpty()){
            return null;
        }
        return prefix;
    }

    // a call to this method assumes that AttrImpl wraps an OMNamespace
    private String getOMNamespacePrefix() {
        // OMNamespace prefix is what you get after xmlns:
        // in DOM the prefix of an attribute is what you get before :
        String prefix = omns.getPrefix();
        if(prefix == null || prefix.isEmpty()){
            return null;
        }
        return XMLNS;
    }

    public Node getNextSibling() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Node getParentNode() {
        return null;
    }

}
