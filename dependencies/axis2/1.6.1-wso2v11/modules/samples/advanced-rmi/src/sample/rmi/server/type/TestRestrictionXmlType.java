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
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.util.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;


public class TestRestrictionXmlType extends AbstractXmlType {
    public void generateWSDLSchema(Document document,
                                   Map namespacesToPrefixMap)
            throws SchemaGenerationException {
         // generate the simple type Element here
        String xsdPrefix = (String) namespacesToPrefixMap.get(Constants.URI_2001_SCHEMA_XSD);
        this.typeElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "simpleType");
        this.typeElement.setPrefix(xsdPrefix);
        this.typeElement.setAttribute("name", this.qname.getLocalPart());

        Element restrictionElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "restriction");
        restrictionElement.setPrefix(xsdPrefix);
        restrictionElement.setAttribute("base", xsdPrefix + ":string");
        this.typeElement.appendChild(restrictionElement);

        Element enumerationElement = null;
        for (int i = 1; i < 5; i++) {
            enumerationElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "enumeration");
            enumerationElement.setPrefix(xsdPrefix);
            enumerationElement.setAttribute("value", "testValue" + i);
            restrictionElement.appendChild(enumerationElement);
        }
    }
}
