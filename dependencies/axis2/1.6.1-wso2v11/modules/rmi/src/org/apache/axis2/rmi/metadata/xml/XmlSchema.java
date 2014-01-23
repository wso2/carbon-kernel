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
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XmlSchema {

    /**
     * target namespace of the schema
     */
    private String targetNamespace;

    /**
     * other declared namespaces
     */
    private List namespaces;

    /**
     * top level elements for this schema
     */
    private List elements;

    /**
     * top level complex types for this schema
     */
    private List complexTypes;

    /**
     * other schema imports
     */
    private List imports;

    /**
     * wsdl schema element corresponding to this schema element
     */
    private Schema wsdlSchema;

    /**
     * default constructor
     */
    public XmlSchema() {
        this.elements = new ArrayList();
        this.complexTypes = new ArrayList();
        this.namespaces = new ArrayList();
        this.imports = new ArrayList();
    }

    /**
     * constructor with target namespace
     * @param targetNamespace
     */
    public XmlSchema(String targetNamespace) {
        this();
        this.targetNamespace = targetNamespace;
        this.namespaces.add(this.targetNamespace);
    }

    /**
     * sets the wsdl schema for this schema object
     * @throws SchemaGenerationException
     */
    public void generateWSDLSchema()
            throws SchemaGenerationException {
        try {
            ExtensionRegistry extensionRegistry = WSDLFactory.newInstance().newPopulatedExtensionRegistry();
            this.wsdlSchema = (Schema) extensionRegistry.createExtension(
                    Types.class, new QName(Constants.URI_2001_SCHEMA_XSD, "schema"));

            // create the schema element
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element schemaElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "xsd:schema");

            schemaElement.setAttributeNS(Constants.XMLNS_ATTRIBUTE_NS_URI,"xmlns:xsd",Constants.URI_2001_SCHEMA_XSD);

            Map namespacesToPrefixMap = new HashMap();
            namespacesToPrefixMap.put(Constants.URI_2001_SCHEMA_XSD,"xsd");
            // set default namespace attribute

            this.wsdlSchema.setElement(schemaElement);

            //set the target namesapce and other namespaces
            schemaElement.setAttribute("targetNamespace", this.targetNamespace);
            schemaElement.setAttribute("elementFormDefault", "qualified");

            // add other namesapces
            String namespace;
            String prefix;
            for (Iterator iter = this.namespaces.iterator();iter.hasNext();){
                namespace = (String) iter.next();
                if (!namespacesToPrefixMap.containsKey(namespace)){
                    prefix = Util.getNextNamespacePrefix();
                    schemaElement.setAttributeNS(Constants.XMLNS_ATTRIBUTE_NS_URI,"xmlns:" + prefix,namespace);
                    namespacesToPrefixMap.put(namespace,prefix);
                }
            }

            // add imports
            XmlImport xmlImport;
            Element importElement;
            for (Iterator iter = this.imports.iterator();iter.hasNext();){
                xmlImport = (XmlImport) iter.next();
                importElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD, "xsd:import");
                importElement.setAttribute("namespace", xmlImport.getNamespace());
                schemaElement.appendChild(importElement);
            }


            // create complex type elements
            XmlType xmlType;
            for (Iterator iter = this.complexTypes.iterator();iter.hasNext();){
                 xmlType = (XmlType) iter.next();
                 if (!xmlType.isAnonymous() && !xmlType.isSimpleType()){
                     xmlType.generateWSDLSchema(document, namespacesToPrefixMap);
                     schemaElement.appendChild(xmlType.getTypeElement());
                 }
            }

            // create elements
            XmlElement xmlElement;
            for (Iterator iter = this.elements.iterator(); iter.hasNext();){
                 xmlElement = (XmlElement) iter.next();
                 xmlElement.generateWSDLSchema(document, namespacesToPrefixMap);
                 schemaElement.appendChild(xmlElement.getElement());
            }

        } catch (WSDLException e) {
            throw new SchemaGenerationException("Error while creating the extension registry",e);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void addElement(XmlElement xmlElement){
        this.elements.add(xmlElement);
    }

    public void addComplexType(XmlType xmlType){
        this.complexTypes.add(xmlType);
    }

    public void addNamespace(String namespace){
        this.namespaces.add(namespace);
    }

    public void addImport(XmlImport xmlImport){
        this.imports.add(xmlImport);
    }

    public boolean containsNamespace(String namespace){
        return this.namespaces.contains(namespace);
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public List getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List namespaces) {
        this.namespaces = namespaces;
    }

    public List getElements() {
        return elements;
    }

    public void setElements(List elements) {
        this.elements = elements;
    }

    public List getComplexTypes() {
        return complexTypes;
    }

    public void setComplexTypes(List complexTypes) {
        this.complexTypes = complexTypes;
    }

    public List getImports() {
        return imports;
    }

    public void setImports(List imports) {
        this.imports = imports;
    }

    public Schema getWsdlSchema() {
        return wsdlSchema;
    }

    public void setWsdlSchema(Schema wsdlSchema) {
        this.wsdlSchema = wsdlSchema;
    }
}
