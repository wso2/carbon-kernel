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

package org.apache.axis2.rmi.metadata.impl;

import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.config.ClassInfo;
import org.apache.axis2.rmi.config.FieldInfo;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.AttributeField;
import org.apache.axis2.rmi.metadata.ElementField;
import org.apache.axis2.rmi.metadata.Field;
import org.apache.axis2.rmi.metadata.Type;
import org.apache.axis2.rmi.metadata.xml.XmlImport;
import org.apache.axis2.rmi.metadata.xml.XmlSchema;
import org.apache.axis2.rmi.metadata.xml.XmlType;
import org.apache.axis2.rmi.metadata.xml.impl.XmlTypeImpl;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.JavaTypeToQNameMap;

import javax.xml.namespace.QName;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TypeImpl implements Type {
    /**
     * java class corresponds to this XmlType object
     */
    private Class javaClass;

    /**
     * list of element Field objects for this java class
     */
    private List elementFields;

    /**
     * list of attribute Field objects for this java class
     */
    private List attributeFields;

    /**
     * name of the Type : class name
     */
    private String name;

    /**
     * namespace of the type : depends on the package
     */
    private String namespace;

    /**
     * parent type for this type
     */
    private Type parentType;

    /**
     * xml metadata type correponding to this type object
     */
    private XmlType xmlType;

    private boolean isSchemaGenerated;


    public TypeImpl() {
        this.elementFields = new ArrayList();
        this.attributeFields = new ArrayList();
    }

    public TypeImpl(Class javaClass) {
        this();
        this.javaClass = javaClass;
    }

    /**
     * popualate the meta data corresponding to this type
     * @param configurator
     */
    public void populateMetaData(Configurator configurator,
                                 Map processedTypeMap)
            throws MetaDataPopulateException {
        // java class should alrady have populated.

        // if javaTypeToQNameMap contains this key then this is an either
        // primitive type or a Simple known type. we don't have to populate
        // the attribues
        try {
            if (!JavaTypeToQNameMap.containsKey(this.javaClass)) {
                this.name = this.javaClass.getName();
                this.name = this.name.substring(this.name.lastIndexOf(".") + 1);
                this.namespace = configurator.getNamespace(this.javaClass.getPackage().getName());

                Class superClass = this.javaClass.getSuperclass();

                // if the supper class is Object class nothing to warry
                if (!superClass.equals(Object.class) && !superClass.equals(Exception.class)) {
                    // then this is an extension class and we have to processit
                    if (!processedTypeMap.containsKey(superClass)) {
                        Type superClassType = new TypeImpl(superClass);
                        processedTypeMap.put(superClass, superClassType);
                        superClassType.populateMetaData(configurator, processedTypeMap);
                    }
                    this.setParentType((Type) processedTypeMap.get(superClass));
                }

                // we need informatin only about this class
                // supper class information is processed in the super class type
                ClassInfo customClassInfo = configurator.getClassInfo(this.javaClass);
                BeanInfo beanInfo = Introspector.getBeanInfo(this.javaClass, this.javaClass.getSuperclass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                Field field;
                for (int i = 0; i < propertyDescriptors.length; i++) {
                    // remove the class descriptor
                    if ((customClassInfo != null) &&
                            (customClassInfo.getFieldInfo(propertyDescriptors[i].getName()) != null)) {
                        FieldInfo fieldInfo = customClassInfo.getFieldInfo(propertyDescriptors[i].getName());
                        if (fieldInfo.isElement()) {
                            field = new ElementField(propertyDescriptors[i], this.namespace);
                            field.populateMetaData(configurator, processedTypeMap);
                            this.elementFields.add(field);
                        } else {
                            // we use the attribute name space as null
                            field = new AttributeField(propertyDescriptors[i], null);
                            field.populateMetaData(configurator, processedTypeMap);
                            this.attributeFields.add(field);
                        }
                        if (fieldInfo.getXmlName() != null) {
                            field.setName(fieldInfo.getXmlName());
                        }
                    } else {
                        field = new ElementField(propertyDescriptors[i], this.namespace);
                        field.populateMetaData(configurator, processedTypeMap);
                        this.elementFields.add(field);
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new MetaDataPopulateException(
                    "Error Occured while getting the Bean info of the class " + this.javaClass.getName(), e);
        }

    }

    /**
     * this method sets the xmlType correctly. this method should only be invoked
     * if it has not already processed
     * @param configurator
     * @param schemaMap
     */

    public void generateSchema(Configurator configurator,
                               Map schemaMap)
            throws SchemaGenerationException {

        // here we have to populate the xmlType object properly
        this.isSchemaGenerated = true;
        if (JavaTypeToQNameMap.containsKey(this.javaClass)){
            // i.e. this is a basic type
            // no need to process or add this to schema list
            this.xmlType = new XmlTypeImpl(JavaTypeToQNameMap.getTypeQName(this.javaClass));
            this.xmlType.setSimpleType(true);
        } else {

            // get the schema to add the complex type
            if (schemaMap.get(this.namespace) == null){
                // create a new namespace for this schema
                schemaMap.put(this.namespace, new XmlSchema(this.namespace));
            }
            XmlSchema xmlSchema = (XmlSchema) schemaMap.get(this.namespace);

            // we have to generate a complex type for this
            this.xmlType = new XmlTypeImpl(new QName(this.namespace,this.name));
            this.xmlType.setSimpleType(false);

             // set the parent type for this type
            if (this.parentType != null){
                Type parentType = this.parentType;
                if (!parentType.isSchemaGenerated()){
                    parentType.generateSchema(configurator,schemaMap);
                }
                this.xmlType.setParentType(parentType.getXmlType());
                // import the complex type namespace if needed.
                if (!xmlSchema.containsNamespace(this.xmlType.getParentType().getQname().getNamespaceURI())){
                    // if the element namespace does not exists we have to add it
                    if (!this.xmlType.getParentType().getQname().getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                        XmlImport xmlImport = new XmlImport(this.xmlType.getParentType().getQname().getNamespaceURI());
                        xmlSchema.addImport(xmlImport);
                    }
                    xmlSchema.addNamespace(this.xmlType.getParentType().getQname().getNamespaceURI());
                }

            }

            // add elements of the elementFields
            ElementField elementField;
            for (Iterator iter = this.elementFields.iterator();iter.hasNext();){
                elementField = (ElementField) iter.next();
                if (!elementField.isSchemaGenerated()){
                    // if it is not already processed process it.
                    elementField.generateSchema(configurator,schemaMap);
                }
                this.xmlType.addElement(elementField.getElement());
                // we have to set the namespaces of these element complex types properly
                QName elementTypeQName = elementField.getElement().getType().getQname();
                if (!xmlSchema.containsNamespace(elementTypeQName.getNamespaceURI())){
                    // if the element namespace does not exists we have to add it
                    if (!elementTypeQName.getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                        XmlImport xmlImport = new XmlImport(elementTypeQName.getNamespaceURI());
                        xmlSchema.addImport(xmlImport);
                    }
                    xmlSchema.addNamespace(elementTypeQName.getNamespaceURI());
                }

            }

            //add attribute fields
            AttributeField attributeField;
            for (Iterator iter = this.attributeFields.iterator(); iter.hasNext();){
                attributeField = (AttributeField) iter.next();
                if (!attributeField.isSchemaGenerated()){
                    // if it is not already processed process it.
                    attributeField.generateSchema(configurator,schemaMap);
                }
                this.xmlType.addAttribute(attributeField.getAttribute());
                // we have to set the namespaces of these element complex types properly
                QName attributeTypeQName = attributeField.getAttribute().getType().getQname();
                if (!xmlSchema.containsNamespace(attributeTypeQName.getNamespaceURI())){
                    // if the element namespace does not exists we have to add it
                    if (!attributeTypeQName.getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                        XmlImport xmlImport = new XmlImport(attributeTypeQName.getNamespaceURI());
                        xmlSchema.addImport(xmlImport);
                    }
                    xmlSchema.addNamespace(attributeTypeQName.getNamespaceURI());
                }

            }
            // finally add this complex type to schema map
            xmlSchema.addComplexType(this.xmlType);

        }

    }

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
