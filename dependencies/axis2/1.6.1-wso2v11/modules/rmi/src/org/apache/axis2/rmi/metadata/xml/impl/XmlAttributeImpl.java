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
import org.apache.axis2.rmi.metadata.xml.XmlType;
import org.apache.axis2.rmi.util.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

public class XmlAttributeImpl implements XmlAttribute {
    /**
     * namespace for this attribute if it is an top level attribute
     */
    private String namespace;

    /**
     * name of this attribute
     */
    private String name;

    /**
     * is this attribute nillable
     */
    private boolean isRequired;

    /**
     * xmlType of this attribute
     */
    private XmlType type;

    /**
     * schema attribute for this attribute
     */
    private Element attribute;

    public XmlAttributeImpl(boolean isPrimitive) {
        // set the nillable value to true
        this.isRequired = isPrimitive;
    }

    public void generateWSDLSchema(Document document,
                                   Map namespacesToPrefixMap)
            throws SchemaGenerationException {
        String xsdPrefix = (String) namespacesToPrefixMap.get(Constants.URI_2001_SCHEMA_XSD);
        this.attribute = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "attribute");
        this.attribute.setPrefix(xsdPrefix);
        this.attribute.setAttribute("name", this.name);


        if (this.isRequired) {
            this.attribute.setAttribute("use", "required");
        }

        if (this.type != null){
            if (type.isSimpleType()){
                this.attribute.setAttribute("type", xsdPrefix + ":" + type.getQname().getLocalPart());
            } else {
                throw new SchemaGenerationException("Attribute type must be a simple type");
            }
        } else {
            throw new SchemaGenerationException("Type can not be null for an attribute");
        }

    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
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

    public Element getAttribute() {
        return attribute;
    }

    public void setAttribute(Element attribute) {
        this.attribute = attribute;
    }

}
