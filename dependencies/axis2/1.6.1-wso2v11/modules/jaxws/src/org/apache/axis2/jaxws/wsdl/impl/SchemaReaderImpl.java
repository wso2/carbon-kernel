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

package org.apache.axis2.jaxws.wsdl.impl;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.jaxws.wsdl.SchemaReader;
import org.apache.axis2.jaxws.wsdl.SchemaReaderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class SchemaReaderImpl implements SchemaReader {

    private static String JAXB_SCHEMA_BINDING = "schemaBindings";
    private static String JAXB_SCHEMA_BINDING_PACKAGE = "package";
    private static String JAXB_SCHEMA_Binding_PACKAGENAME = "name";
    private static String SCHEMA_TARGETNAMESPACE = "targetNamespace";
    private Definition wsdlDefinition = null;
    private static Log log = LogFactory.getLog(SchemaReaderImpl.class);
    
    
    // The following list of schema should be ignored by the schema->packages
    // algorithm.
    private static List<String> ignoreSchema = null;
    static {
        ignoreSchema = new ArrayList<String>();
        ignoreSchema.add("http://schemas.xmlsoap.org/ws/2004/08/addressing");
        ignoreSchema.add("http://www.w3.org/2005/08/addressing");
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.wsdl.SchemaReader#readPackagesFromSchema(javax.wsdl.Definition)
      */
    public Set<String> readPackagesFromSchema(Definition wsdlDefinition)
            throws SchemaReaderException {
        if (wsdlDefinition == null) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid wsdl definition provided, NULL");
            }
            throw new SchemaReaderException(Messages.getMessage("SchemaReaderErr1"));
        }
        this.wsdlDefinition = wsdlDefinition;
        List<Schema> schemaList = new ArrayList<Schema>();
        Set<String> packageList = new TreeSet<String>();
        //Add WSDL TargetNamespace
        String namespace = wsdlDefinition.getTargetNamespace();
        List packages = JavaUtils.getPackagesFromNamespace(namespace);
        if (packages != null && packages.size() > 0) {
            packageList.addAll(packages);   
        }

        //Read All Schema Definition in wsdl;
        Types types = wsdlDefinition.getTypes();
        if (types == null) {
            if (log.isDebugEnabled()) {
                log.debug("WARNING: Could not find any Schema/Types from WSDL");
                log.debug("no packages will derived from WSDL schema");
            }
            return packageList;
        }
        List extensibilityElements = types.getExtensibilityElements();
        for (Object obj : extensibilityElements) {
            if (obj != null && isSchema((ExtensibilityElement)obj)) {
                Schema schema = (Schema)obj;
                //process schemas and read packages from them.
                processSchema(schema, schemaList, packageList);
            }
        }

        //Set always stores unique objects, so I dont have to worry about removing duplicates from this set.
        return packageList;
    }

    private void processSchema(Schema schema, List<Schema> schemaList, Set<String> packageList)
            throws SchemaReaderException {
        if (schemaList.contains(schema)) {
            return;
        }
        List<SchemaImport> importList = new ArrayList<SchemaImport>();
        //Start reading inline schema
        //Check if there is Binding customization and read package from it.
        String packageString = readSchemaBindingPackageName(schema);
        //No binding customization present then get the Targetnamespace of Schema
        List packages = null;
        if (packageString == null) {
            //no Schema Binding package name found, this means no jaxb customizations in schema, lets read wsdl
            //targetnamespace. Thats what will be used by RI tooling to store java Beans
            String namespace = readSchemaTargetnamespace(schema);
            if (ignoreSchema.contains(namespace)) {
                // ignore this schema and its contents...continue
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring targetnamespace " + namespace);
                }
                schemaList.add(schema);
                return;
            }
            if (namespace != null) {
                packages = JavaUtils.getPackagesFromNamespace(namespace);
            }
        }
        //Gather all imports and process Schema from these imports
        Map map = schema.getImports();
        Collection collection = map.values();
        for (Iterator i = collection.iterator(); i.hasNext();) {
            Vector value = (Vector)i.next();
            for (Object vectorObj : value) {
                SchemaImport si = (SchemaImport)vectorObj;
                importList.add(si);
                if (log.isDebugEnabled()) {
                    if (si != null)
                        log.debug(
                                "Reading import for SchemaLocation =" + si.getSchemaLocationURI());
                }
            }
        }

        //Get namespace and flag the schema as read
        schemaList.add(schema);
        //Package String could be null if there is no schema defintion inside types
        if (packageString != null) {
            packageList.add(packageString);
        }
        if (packages != null && packages.size() > 0) {
            packageList.addAll(packages);
        }
        for (SchemaImport si : importList) {
            processImport(si, schemaList, packageList);
        }

    }

    private void processImport(SchemaImport si, List<Schema> schemaList, Set<String> packageList)
            throws SchemaReaderException {
        Schema refSchema = si.getReferencedSchema();
        if (refSchema != null) {
            processSchema(refSchema, schemaList, packageList);
        }
    }

    private String readSchemaTargetnamespace(Schema schema) {
        Node root = schema.getElement();
        if (root != null) {
            NamedNodeMap nodeMap = root.getAttributes();
            Node attributeNode = nodeMap.getNamedItem(SCHEMA_TARGETNAMESPACE);
            if (attributeNode != null) {
                return attributeNode.getNodeValue();
            }
        }
        return null;
    }

    private String readSchemaBindingPackageName(Schema schema) {

        /* JAXB Specification section 7.6 have following important points
           * 1) <schemaBindings> binding declaration have schema scope
           * 2) For inline annotation  a <schemaBindings> is valid only in the annotation element of the <schema> element.
           * 3) There must only be a single instance of <schemaBindings> declaration in the annotation element of the <schema> element.
           */

        //Get root node for schema.
        Node root = schema.getElement();
        if (root.hasChildNodes()) {

            //get all child nodes for schema
            NodeList list = root.getChildNodes();

            //search for JAXB schemaBinding customization in schema element definitions.
            for (int i = 0; i < list.getLength(); i++) {
                Node childNode = list.item(i);
                if (isElementName(JAXB_SCHEMA_BINDING, childNode)) {

                    //SchemaBinding has been defined, so lets look for package element.
                    NodeList schemaBindingNodeList = childNode.getChildNodes();
                    for (int j = 0; j < schemaBindingNodeList.getLength(); j++) {
                        Node schemaBindingNode = schemaBindingNodeList.item(j);
                        if (isElementName(JAXB_SCHEMA_BINDING_PACKAGE, schemaBindingNode)) {

                            //Package Element found, so lets read the package name attribute and return that.
                            NamedNodeMap nodeMap = schemaBindingNode.getAttributes();
                            Node attributeNode =
                                    nodeMap.getNamedItem(JAXB_SCHEMA_Binding_PACKAGENAME);
                            return attributeNode.getNodeValue();
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isElementName(String name, Node domNode) {
        if (domNode == null) {
            return false;
        }
        if (domNode.getNodeType() == Node.ELEMENT_NODE) {
            String localName = domNode.getLocalName();
            return localName != null && localName.equals(name);
        }
        return false;
    }

    private boolean isSchema(ExtensibilityElement exElement) {
        return WSDLWrapper.SCHEMA.equals(exElement.getElementType());
    }

}
