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

import org.apache.axiom.om.impl.dom.ElementImpl;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

public class SOAPBodyElementImpl extends SOAPElementImpl implements SOAPBodyElement {

    /** @param element  */
    public SOAPBodyElementImpl(ElementImpl element) {
        super(element);
    }

    public void setParentElement(SOAPElement parent) throws SOAPException {
        if (!(parent instanceof SOAPBody)) {
            throw new IllegalArgumentException("Parent is not a SOAPBody");
        }
        this.parentElement = parent;
    }

    public SOAPElement addAttribute(QName qname, String value) throws SOAPException {
        return super.addAttribute(qname, value);
    }

    public SOAPElement addChildElement(QName qname) throws SOAPException {
        return super.addChildElement(qname);
    }

    public QName createQName(String localName, String prefix) throws SOAPException {
        return super.createQName(localName, prefix);
    }

    public Iterator getAllAttributesAsQNames() {
        return super.getAllAttributesAsQNames();
    }

    public String getAttributeValue(QName qname) {
        return super.getAttributeValue(qname);
    }

    public Iterator getChildElements(QName qname) {
        return super.getChildElements(qname);
    }

    public QName getElementQName() {
        return super.getElementQName();
    }

    public boolean removeAttribute(QName qname) {
        return super.removeAttribute(qname);
    }

    public SOAPElement setElementQName(QName newName) throws SOAPException {
        return super.setElementQName(newName);
    }
}
