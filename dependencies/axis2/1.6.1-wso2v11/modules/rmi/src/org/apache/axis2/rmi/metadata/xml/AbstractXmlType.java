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

package org.apache.axis2.rmi.metadata.xml;

import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;


public abstract class AbstractXmlType implements XmlType{

    /**
     * Qualified name of the xmlType
     */
    protected QName qname;

    /**
     * is this an anAnonymous type this case qname can be null
     */
    protected boolean isAnonymous;

    /**
     * is this is a basic type
     */
    protected boolean isSimpleType;

    /**
     * list of child elements
     */
    protected List elements;

    /**
     * list of child attributes
     */
    protected List attributes;

    /**
     * complex type element for this XmlType
     */
    protected Element typeElement;

    /**
     * parent type for this xml type if it is an extension
     */
    protected XmlType parentType;

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
    public abstract void generateWSDLSchema(Document document,
                                   Map namespacesToPrefixMap)
            throws SchemaGenerationException;

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
