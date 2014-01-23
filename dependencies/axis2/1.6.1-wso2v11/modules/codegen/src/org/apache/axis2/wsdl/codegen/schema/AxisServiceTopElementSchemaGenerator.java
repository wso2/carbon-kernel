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

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.codegen.schema.exception.DummySchemaGenerationException;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * this class is used to generate dummy schema with only top level
 * elements to support xmlbeans
 */
public class AxisServiceTopElementSchemaGenerator {

    private AxisService axisService;

    public AxisServiceTopElementSchemaGenerator(AxisService service) {
        this.axisService = service;
    }

    public List getDummySchemaList() throws DummySchemaGenerationException {
        Set topElements = getTopElements();
        Map schemaMap = getSchemaMap(topElements);
        return getXmlSchemaList(schemaMap);
    }

    public List getXmlSchemaList(Map schemaMap) throws DummySchemaGenerationException {
        List xmlSchemaList = new ArrayList();

        // creates the builder factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            XmlSchema xmlSchema;
            Element element;
            for (Iterator iter = schemaMap.values().iterator(); iter.hasNext();) {
                xmlSchema = (XmlSchema) iter.next();
                element = xmlSchema.getSchemaElement(document);
                XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
                xmlSchemaList.add(xmlSchemaCollection.read(element));
            }
        } catch (ParserConfigurationException e) {
            throw new DummySchemaGenerationException("Can not build the dom tree", e);
        }
        return xmlSchemaList;
    }

    public Map getSchemaMap(Set topElements) {
        Map schemaMap = new HashMap();
        TopElement topElement;
        XmlSchema xmlSchema;
        XmlElement xmlElement;
        NamespacePrefix namespacePrefix = new NamespacePrefix();
        for (Iterator iter = topElements.iterator(); iter.hasNext();) {
            topElement = (TopElement) iter.next();
            xmlSchema = getXmlSchemaForNamespace(topElement.getElementQName().getNamespaceURI(), schemaMap);
            if (!xmlSchema.isElementExists(topElement.getElementQName().getLocalPart())) {
                if (topElement.getTypeQName() == null) {
                    //i.e this element is an annonymous complex element
                    // then we can add this element with out any problem
                    xmlSchema.addElement(getXmlElement(topElement));
                } else if (topElement.getTypeQName().getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                    // this element has a standard schema type then we do not have to warry about the type
                    xmlSchema.addElement(getXmlElement(topElement));
                } else {
                    // then we have an element with a complex type.
                    // first creates the complex type if it is note exists
                    XmlSchema complexElementSchema =
                            getXmlSchemaForNamespace(topElement.getTypeQName().getNamespaceURI(), schemaMap);
                    if (!complexElementSchema.isComplexTypeExists(topElement.getTypeQName().getLocalPart())) {
                        XmlComplexType xmlComplexType = new XmlComplexType();
                        xmlComplexType.setName(topElement.getTypeQName().getLocalPart());
                        xmlComplexType.setNamespace(topElement.getTypeQName().getNamespaceURI());
                        complexElementSchema.addComplexType(xmlComplexType);
                    }

                    // adding the namesapce if not exists.
                    if (!xmlSchema.getNamespacesPrefixMap()
                            .containsKey(topElement.getTypeQName().getNamespaceURI())) {
                        xmlSchema.getNamespacesPrefixMap().put(
                                topElement.getTypeQName().getNamespaceURI(),
                                namespacePrefix.getNextNamespacePrefix());
                        XmlImport xmlImport = new XmlImport();
                        xmlImport.setTargetNamespace(topElement.getTypeQName().getNamespaceURI());
                        xmlSchema.addImport(xmlImport);
                    }

                    // finally add the namespace
                    xmlSchema.addElement(getXmlElement(topElement));
                }
            }
        }
        return schemaMap;
    }

    private XmlElement getXmlElement(TopElement topElement) {
        XmlElement xmlElement = new XmlElement();
        xmlElement.setName(topElement.getElementQName().getLocalPart());
        xmlElement.setNamespace(topElement.getElementQName().getNamespaceURI());
        xmlElement.setType(topElement.getTypeQName());
        return xmlElement;
    }

    private XmlSchema getXmlSchemaForNamespace(String targetNamespace, Map schemaMap) {
        if (!schemaMap.containsKey(targetNamespace)) {
            XmlSchema xmlSchema = new XmlSchema(targetNamespace);
            schemaMap.put(targetNamespace, xmlSchema);
        }
        return (XmlSchema) schemaMap.get(targetNamespace);
    }

    public Set getTopElements() {

        Set topSchemaElements = new HashSet();
        AxisOperation axisOperation;
        AxisMessage axisMessage;
        TopElement topElement;
        XmlSchemaElement xmlSchemaElement;
        SOAPHeaderMessage soapHeaderMessage;

        for (Iterator operationIter = axisService.getOperations(); operationIter.hasNext();) {
            axisOperation = (AxisOperation) operationIter.next();
            for (Iterator messageIter = axisOperation.getMessages(); messageIter.hasNext();) {
                axisMessage = (AxisMessage) messageIter.next();
                if (axisMessage.getElementQName() != null) {
                    topElement = new TopElement(axisMessage.getElementQName());
                    xmlSchemaElement = axisMessage.getSchemaElement();
                    topElement.setTypeQName(xmlSchemaElement.getSchemaTypeName());
                    topSchemaElements.add(topElement);
                    // adding header messages
                    for (Iterator soapHeaderIter = axisMessage.getSoapHeaders().iterator(); soapHeaderIter.hasNext();) {
                        soapHeaderMessage = (SOAPHeaderMessage) soapHeaderIter.next();
                        topElement = new TopElement(soapHeaderMessage.getElement());
                        topSchemaElements.add(topElement);
                        xmlSchemaElement = getSchemaElement(soapHeaderMessage.getElement());
                        topElement.setTypeQName(xmlSchemaElement.getSchemaTypeName());
                        topSchemaElements.add(topElement);
                    }
                }
            }

            for (Iterator faultMessagesIter = axisOperation.getFaultMessages().iterator();
                 faultMessagesIter.hasNext();) {
                axisMessage = (AxisMessage) faultMessagesIter.next();
                topElement = new TopElement(axisMessage.getElementQName());
                xmlSchemaElement = axisMessage.getSchemaElement();
                topElement.setTypeQName(xmlSchemaElement.getSchemaTypeName());
                topSchemaElements.add(topElement);
            }
        }
        return topSchemaElements;
    }

    public XmlSchemaElement getSchemaElement(QName elementQName) {
        XmlSchemaElement xmlSchemaElement = null;
        ArrayList schemas = this.axisService.getSchema();
        for (Iterator schemaIter = schemas.iterator(); schemaIter.hasNext();){
            xmlSchemaElement = getSchemaElement(
                    (org.apache.ws.commons.schema.XmlSchema)schemaIter.next(),elementQName);
            if (xmlSchemaElement != null){
                break;
            }
        }
        return xmlSchemaElement;
    }

    private XmlSchemaElement getSchemaElement(org.apache.ws.commons.schema.XmlSchema schema,
                                              QName elementQName) {
        XmlSchemaElement xmlSchemaElement = null;
        if (schema != null) {
            xmlSchemaElement = schema.getElementByName(elementQName);
            if (xmlSchemaElement == null) {
                // try to find in an import or an include
                XmlSchemaObjectCollection includes = schema.getIncludes();
                if (includes != null) {
                    Iterator includesIter = includes.getIterator();
                    Object object;
                    while (includesIter.hasNext()) {
                        object = includesIter.next();
                        if (object instanceof XmlSchemaImport) {
                            org.apache.ws.commons.schema.XmlSchema schema1 =
                                    ((XmlSchemaImport) object).getSchema();
                            xmlSchemaElement = getSchemaElement(schema1,elementQName);
                        }
                        if (object instanceof XmlSchemaInclude) {
                            org.apache.ws.commons.schema.XmlSchema schema1 =
                                    ((XmlSchemaInclude) object).getSchema();
                            xmlSchemaElement = getSchemaElement(schema1,elementQName);
                        }
                        if (xmlSchemaElement != null){
                            break;
                        }
                    }
                }
            }
        }
        return xmlSchemaElement;
    }
}
