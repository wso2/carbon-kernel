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

package org.apache.axis2.rmi.metadata.xml.impl;

import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.metadata.xml.XmlType;
import org.apache.axis2.rmi.util.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;


public class XmlElementImpl implements XmlElement {
    /**
     * namespace for this element if it is an top level element
     */
    private String namespace;

    /**
     * name of this element
     */
    private String name;

    /**
     * is this element nillable
     */
    private boolean isNillable;

    /**
     * is minOccures zero for this element
     */
    private boolean isMinOccurs0;

    /**
     * is Array
     */
    private boolean isArray;

    /**
     * if this is an schema top level element
     */
    private boolean isTopElement;

    /**
     * xmlType of this element
     */
    private XmlType type;

    /**
     * schema element for this element
     */
    private Element element;

    public XmlElementImpl(boolean isPrimitiveType) {
        // set the nillable value to true
        this.isNillable = !isPrimitiveType;
        this.isMinOccurs0 = !isPrimitiveType;
    }

    public void generateWSDLSchema(Document document,
                                   Map namespacesToPrefixMap)
            throws SchemaGenerationException {
        String xsdPrefix = (String) namespacesToPrefixMap.get(Constants.URI_2001_SCHEMA_XSD);
        this.element = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "element");
        this.element.setPrefix(xsdPrefix);
        this.element.setAttribute("name", this.name);


        if (this.isArray && !this.isTopElement) {
            this.element.setAttribute("maxOccurs", "unbounded");
        }

        if (this.namespace == null){
            this.element.setAttribute("form", "unqualified");
        }

        // setting the type
        if (type == null) {
            // i.e this is corresponds to an void type so generate a empty annony mous complex type
            Element complexElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "complexType");
            complexElement.setPrefix(xsdPrefix);

            Element sequenceElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "sequence");
            sequenceElement.setPrefix(xsdPrefix);
            complexElement.appendChild(sequenceElement);
            this.element.appendChild(complexElement);
        } else if (type.isSimpleType()) {
            // this is an simple type element
            this.element.setAttribute("type", xsdPrefix + ":" + type.getQname().getLocalPart());
            if (this.isNillable) {
                this.element.setAttribute("nillable", "true");
            }
            if (this.isMinOccurs0 && !this.isTopElement){
                this.element.setAttribute("minOccurs","0");
            }
        } else if (!type.isAnonymous()) {
            String prefix = (String) namespacesToPrefixMap.get(type.getQname().getNamespaceURI());
            this.element.setAttribute("type", prefix + ":" + type.getQname().getLocalPart());
            if (this.isNillable) {
                this.element.setAttribute("nillable", "true");
            }
            if (this.isMinOccurs0 && !this.isTopElement){
                this.element.setAttribute("minOccurs","0");
            }
        } else {
            // i.e this is and annonymous complex type
            type.generateWSDLSchema(document, namespacesToPrefixMap);
            this.element.appendChild(type.getTypeElement());
        }

    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isNillable() {
        return isNillable;
    }

    public void setNillable(boolean nillable) {
        isNillable = nillable;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public boolean isTopElement() {
        return isTopElement;
    }

    public void setTopElement(boolean topElement) {
        isTopElement = topElement;
    }

    public XmlType getType() {
        return type;
    }

    public void setType(XmlType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public boolean isMinOccurs0() {
        return isMinOccurs0;
    }

    public void setMinOccurs0(boolean minOccurs0) {
        isMinOccurs0 = minOccurs0;
    }
}
