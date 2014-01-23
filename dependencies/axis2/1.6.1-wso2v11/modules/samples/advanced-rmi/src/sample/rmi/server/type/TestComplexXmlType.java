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
package sample.rmi.server.type;

import org.apache.axis2.rmi.metadata.xml.AbstractXmlType;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.metadata.xml.XmlAttribute;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.util.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.Iterator;


public class TestComplexXmlType extends AbstractXmlType {

    public void generateWSDLSchema(Document document,
                                   Map namespacesToPrefixMap)
            throws SchemaGenerationException {

        String xsdPrefix = (String) namespacesToPrefixMap.get(Constants.URI_2001_SCHEMA_XSD);
        this.typeElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "complexType");
        this.typeElement.setPrefix(xsdPrefix);
            this.typeElement.setAttribute("name", this.qname.getLocalPart());

        Element sequenceElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "sequence");
        sequenceElement.setPrefix(xsdPrefix);
        sequenceElement.setAttribute("minOccurs","0");
        sequenceElement.setAttribute("maxOccurs","unbounded");

        this.typeElement.appendChild(sequenceElement);
        // adding two elements
        Element param1 = document.createElementNS(Constants.URI_2001_SCHEMA_XSD,"element");
        param1.setAttribute("name","param1");
        param1.setAttribute("type", xsdPrefix + ":int");
        sequenceElement.appendChild(param1);

        Element param2 = document.createElementNS(Constants.URI_2001_SCHEMA_XSD,"element");
        param2.setAttribute("name","param2");
        param2.setAttribute("type", xsdPrefix + ":string");
        sequenceElement.appendChild(param2);

    }
}
