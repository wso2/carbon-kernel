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
import org.apache.axis2.rmi.metadata.xml.XmlAttribute;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.metadata.xml.XmlType;
import org.apache.axis2.rmi.util.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class XmlTypeImpl implements XmlType {
    /**
     * Qualified name of the xmlType
     */
    private QName qname;

    /**
     * is this an anAnonymous type this case qname can be null
     */
    private boolean isAnonymous;

    /**
     * is this is a basic type
     */
    private boolean isSimpleType;

    /**
     * list of child elements
     */
    private List elements;

    /**
     * list of child attributes
     */
    private List attributes;

    /**
     * complex type element for this XmlType
     */
    private Element typeElement;

    /**
     * parent type for this xml type if it is an extension
     */
    private XmlType parentType;

    public XmlTypeImpl() {
        this.elements = new ArrayList();
        this.attributes = new ArrayList();
    }

    public void addElement(XmlElement xmlElement) {
        this.elements.add(xmlElement);
    }

    public void addAttribute(XmlAttribute xmlAttribute) {
        this.attributes.add(xmlAttribute);
    }

    /**
     * this generates the complex type only if it is annonymous and
     * is not a simple type
     *
     * @param document
     * @param namespacesToPrefixMap
     * @throws org.apache.axis2.rmi.exception.SchemaGenerationException
     *
     */
    public void generateWSDLSchema(Document document,
                                   Map namespacesToPrefixMap)
            throws SchemaGenerationException {
        // here we have to generate the complex type element for this xmlType
        if (!this.isSimpleType) {
            String xsdPrefix = (String) namespacesToPrefixMap.get(Constants.URI_2001_SCHEMA_XSD);
            this.typeElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "complexType");
            this.typeElement.setPrefix(xsdPrefix);
            if (!this.isAnonymous) {
                this.typeElement.setAttribute("name", this.qname.getLocalPart());
            }

            Element sequenceElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "sequence");
            sequenceElement.setPrefix(xsdPrefix);

            // set the extension details if there are
            if (this.parentType != null) {

                // i.e this is an extension type
                Element complexContent = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "complexContent");
                complexContent.setPrefix(xsdPrefix);
                this.typeElement.appendChild(complexContent);

                Element extension = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "extension");
                extension.setPrefix(xsdPrefix);
                complexContent.appendChild(extension);

                String extensionPrefix =
                        (String) namespacesToPrefixMap.get(this.parentType.getQname().getNamespaceURI());
                String localPart = this.parentType.getQname().getLocalPart();
                if ((extensionPrefix == null) || extensionPrefix.equals("")) {
                    extension.setAttribute("base", localPart);
                } else {
                    extension.setAttribute("base", extensionPrefix + ":" + localPart);
                }
                extension.appendChild(sequenceElement);
            } else {
                this.typeElement.appendChild(sequenceElement);
            }

            // add the other element children
            XmlElement xmlElement;
            for (Iterator iter = this.elements.iterator(); iter.hasNext();) {
                xmlElement = (XmlElement) iter.next();
                xmlElement.generateWSDLSchema(document, namespacesToPrefixMap);
                sequenceElement.appendChild(xmlElement.getElement());
            }

            // add the attributes
            XmlAttribute xmlAttribute;
            for (Iterator iter = this.attributes.iterator(); iter.hasNext();) {
                xmlAttribute = (XmlAttribute) iter.next();
                xmlAttribute.generateWSDLSchema(document, namespacesToPrefixMap);
                this.typeElement.appendChild(xmlAttribute.getAttribute());
            }
        }
    }

    public XmlTypeImpl(QName qname) {
        this();
        this.qname = qname;
    }

    public QName getQname() {
        return qname;
    }

    public void setQname(QName qname) {
        this.qname = qname;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public boolean isSimpleType() {
        return isSimpleType;
    }

    public void setSimpleType(boolean simpleType) {
        isSimpleType = simpleType;
    }

    public List getElements() {
        return elements;
    }

    public void setElements(List elements) {
        this.elements = elements;
    }

    public Element getTypeElement() {
        return typeElement;
    }

    public void setTypeElement(Element typeElement) {
        this.typeElement = typeElement;
    }

    public XmlType getParentType() {
        return parentType;
    }

    public void setParentType(XmlType parentType) {
        this.parentType = parentType;
    }

    public List getAttributes() {
        return attributes;
    }

    public void setAttributes(List attributes) {
        this.attributes = attributes;
    }
}
