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

/**
 * this class represents an xml complex type
 *
 */
public class XmlComplexType {

    private String namespace;
    private String name;

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

    public Element getSchemaElement(Document document){
        Element complexElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD,"xsd:complexType");
        complexElement.setPrefix("xsd");
        complexElement.setAttribute("name", this.name);

        Element sequenceElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "xsd:sequence");
        sequenceElement.setPrefix("xsd");

        complexElement.appendChild(sequenceElement);
        return complexElement;
    }
}
