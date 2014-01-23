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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * this class represents and schema object.
 * it is assumed that we consider the xsd schemaa
 * as the defualt name space.
 *
 */
public class XmlSchema {

    public static final String XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

    private Map namespacesPrefixMap;
    private Map imports;
    private Map elements;
    private Map complexTypes;
    private String targetNamespace;

    public XmlSchema() {
        this.namespacesPrefixMap = new HashMap();
        this.imports = new HashMap();
        this.elements = new HashMap();
        this.complexTypes = new HashMap();
    }

    public XmlSchema(String targetNamespace) {
        this();
        this.targetNamespace = targetNamespace;
        // add the target namespace to namespace prifix map
        this.namespacesPrefixMap.put(this.targetNamespace,"tns");
        this.namespacesPrefixMap.put(Constants.URI_2001_SCHEMA_XSD,"xsd");
    }

    public void addNamespace(String namespace,String prefix){
        this.namespacesPrefixMap.put(namespace,prefix);
    }

    public void addImport(XmlImport xmlImport){
       this.imports.put(xmlImport.getTargetNamespace(),xmlImport);
    }

    public boolean isExists(XmlImport xmlImport){
        return this.imports.containsKey(xmlImport.getTargetNamespace());
    }

    public void addElement(XmlElement xmlElement){
        this.elements.put(xmlElement.getName(),xmlElement);
    }

    public boolean isExists(XmlElement xmlElement){
        return this.elements.containsKey(xmlElement.getName());
    }

    public boolean isElementExists(String name){
        return this.elements.containsKey(name);
    }

    public void addComplexType(XmlComplexType xmlComplexType){
        this.complexTypes.put(xmlComplexType.getName(),xmlComplexType);
    }

    public boolean isExists(XmlComplexType xmlComplexType){
        return this.complexTypes.containsKey(xmlComplexType.getName());
    }

    public boolean isComplexTypeExists(String nameComplexType){
        return this.complexTypes.containsKey(nameComplexType);
    }

    public Element getSchemaElement(Document document){
        Element schemaElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD,"xsd:schema");
        schemaElement.setPrefix("xsd");

        // set target namesapce
        schemaElement.setAttribute("targetNamespace", this.targetNamespace);

        // register namesapce prefixes
        String namespace;
        String prefix;
        for (Iterator iter = this.namespacesPrefixMap.keySet().iterator(); iter.hasNext();){
            namespace = (String) iter.next();
            prefix = (String) this.namespacesPrefixMap.get(namespace);
            schemaElement.setAttributeNS(XMLNS_NAMESPACE_URI,"xmlns:" + prefix,namespace);
        }

        // adding imports
        XmlImport xmlImport;
        for (Iterator iter = this.imports.values().iterator();iter.hasNext();){
            xmlImport = (XmlImport) iter.next();
            schemaElement.appendChild(xmlImport.getXmlSchemaElement(document));
        }

        // adding elements
        XmlElement xmlElement;
        for (Iterator iter = this.elements.values().iterator();iter.hasNext();){
            xmlElement = (XmlElement) iter.next();
            schemaElement.appendChild(xmlElement.getSchemaElement(document, this.namespacesPrefixMap));
        }

        // adding complex types
        XmlComplexType xmlComplexType;
        for (Iterator iter = this.complexTypes.values().iterator();iter.hasNext();){
            xmlComplexType = (XmlComplexType) iter.next();
            schemaElement.appendChild(xmlComplexType.getSchemaElement(document));
        }
        return schemaElement;
    }

    public Map getNamespacesPrefixMap() {
        return namespacesPrefixMap;
    }

    public void setNamespacesPrefixMap(Map namespacesPrefixMap) {
        this.namespacesPrefixMap = namespacesPrefixMap;
    }

    public Map getImports() {
        return imports;
    }

    public void setImports(Map imports) {
        this.imports = imports;
    }

    public Map getElements() {
        return elements;
    }

    public void setElements(Map elements) {
        this.elements = elements;
    }

    public Map getComplexTypes() {
        return complexTypes;
    }

    public void setComplexTypes(Map complexTypes) {
        this.complexTypes = complexTypes;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

}
