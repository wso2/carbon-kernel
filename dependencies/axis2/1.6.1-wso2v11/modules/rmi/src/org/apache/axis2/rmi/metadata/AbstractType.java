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

package org.apache.axis2.rmi.metadata;

import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.xml.XmlSchema;
import org.apache.axis2.rmi.metadata.xml.XmlType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractType implements Type {

    /**
     * java class corresponds to this XmlType object
     */
    protected Class javaClass;

    /**
     * list of element Field objects for this java class
     */
    protected List elementFields;

    /**
     * list of attribute Field objects for this java class
     */
    protected List attributeFields;

    /**
     * name of the Type : class name
     */
    protected String name;

    /**
     * namespace of the type : depends on the package
     */
    protected String namespace;

    /**
     * parent type for this type
     */
    protected Type parentType;

    /**
     * xml metadata type correponding to this type object
     */
    protected XmlType xmlType;

    protected boolean isSchemaGenerated;


    /**
     * popualate the meta data corresponding to this type
     * @param configurator
     */
    public abstract void populateMetaData(Configurator configurator,
                                 Map processedTypeMap)
            throws MetaDataPopulateException;

    /**
     * this method sets the xmlType correctly. this method should only be invoked
     * if it has not already processed
     * @param configurator
     * @param schemaMap
     */

    public abstract void generateSchema(Configurator configurator,
                               Map schemaMap)
            throws SchemaGenerationException;

    public void populateAllElementFields(List elementFieldsList){
        // we have to first add the parent details to keep the order.
        if (this.parentType != null){
            this.parentType.populateAllElementFields(elementFieldsList);
        }
        elementFieldsList.addAll(this.elementFields);
    }

    public void populateAllAttributeFields(List attributeFieldsList) {
        // we have to first add the parent details to keep the order.
        if (this.parentType != null){
            this.parentType.populateAllAttributeFields(attributeFieldsList);
        }
        attributeFieldsList.addAll(this.attributeFields);
    }

    public List getAllElementFields(){
        List allElementsList = new ArrayList();
        populateAllElementFields(allElementsList);
        return allElementsList;
    }

    public List getAllAttributeFields() {
        List allAttributesList = new ArrayList();
        populateAllAttributeFields(allAttributesList);
        return allAttributesList;
    }

    protected void registerXmlType(Map schemaMap) {
        if (schemaMap.get(this.namespace) == null) {
            // create a new namespace for this schema
            schemaMap.put(this.namespace, new XmlSchema(this.namespace));
        }
        XmlSchema xmlSchema = (XmlSchema) schemaMap.get(this.namespace);
        xmlSchema.addComplexType(this.xmlType);
    }

    public boolean isSchemaGenerated() {
        return isSchemaGenerated;
    }

    public void setSchemaGenerated(boolean schemaGenerated) {
        isSchemaGenerated = schemaGenerated;
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }

    public List getElementFields() {
        return elementFields;
    }

    public void setElementFields(List elementFields) {
        this.elementFields = elementFields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public XmlType getXmlType() {
        return xmlType;
    }

    public void setXmlType(XmlType xmlType) {
        this.xmlType = xmlType;
    }

    public Type getParentType() {
        return parentType;
    }

    public void setParentType(Type parentType) {
        this.parentType = parentType;
    }

    public List getAttributeFields() {
        return attributeFields;
    }

    public void setAttributeFields(List attributeFields) {
        this.attributeFields = attributeFields;
    }
}
