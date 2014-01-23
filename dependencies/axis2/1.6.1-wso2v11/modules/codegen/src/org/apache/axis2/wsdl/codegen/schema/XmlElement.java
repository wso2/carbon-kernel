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

package org.apache.axis2.wsdl.codegen.schema;

import org.apache.axis2.namespace.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * this class represents an xml element
 */
public class XmlElement {

    private String namespace;
    private String name;
    private QName type;

    public Element getSchemaElement(Document document, Map namespacePrefixMap) {
        Element element = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "xsd:element");
        element.setPrefix("xsd");
        element.setAttribute("name", this.name);
        if (this.type == null) {
            // this is an annnimous complex type element
            Element complextTypeElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "xsd:complexType");
            complextTypeElement.setPrefix("xsd");

            Element sequenceElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "xsd:sequence");
            sequenceElement.setPrefix("xsd");

            complextTypeElement.appendChild(sequenceElement);
            element.appendChild(complextTypeElement);
        } else if (Constants.URI_2001_SCHEMA_XSD.equals(this.type.getNamespaceURI())) {
            // this is a standard type
            element.setAttribute("type", "xsd:" + this.type.getLocalPart());
        } else {
            // this element points to an complex type
            String prefix = (String) namespacePrefixMap.get(this.type.getNamespaceURI());
            element.setAttribute("type", prefix + ":" + this.type.getLocalPart());
        }
        return element;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }
}
