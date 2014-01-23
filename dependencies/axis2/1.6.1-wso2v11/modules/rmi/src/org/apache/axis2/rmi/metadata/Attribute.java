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
import org.apache.axis2.rmi.metadata.impl.TypeImpl;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.metadata.xml.impl.XmlElementImpl;
import org.apache.axis2.rmi.types.MapType;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.Util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

public class Attribute {

    /**
     * property descriptor for this attribute
     */
    private PropertyDescriptor propertyDescriptor;
    /**
     * name of the attribute this is the name of the XmlElement as well
     */
    private String name;

    /**
     * namespce of this attribute
     * this is always the namespace of the parent type
     */
    private String namespace;

    /**
     * getter method of the attribute
     */
    private Method getterMethod;

    /**
     * setter method of the attribute
     */
    private Method setterMethod;

    /**
     * attribute metadata type
     */
    private Type type;

    /**
     * is this attribute an array
     */
    private boolean isArray;

    /**
     * schmema element corresponding to this attribute
     * always an attribute refer to and schema XmlElement
     */
    private XmlElement element;

    /**
     * boolean variable to check to see whether we generated the
     * element for this schema.
     * although we can check for null value of the element we prefer
     * to keep a seperate variable.
     */
    private boolean isSchemaGenerated;

    /**
     * class type of the attribute class
     */
    private int classType;

    /**
     * default constructor
     */
    public Attribute() {
    }

    /**
     * constructor with the property descriptor
     *
     * @param propertyDescriptor
     */
    public Attribute(PropertyDescriptor propertyDescriptor,
                     String namespace) {
        this.propertyDescriptor = propertyDescriptor;
        if (Constants.RMI_TYPE_NAMSPACE.equals(namespace)){
            // for rmi defined type elements we keep attributes as unqualified
            this.namespace = null;
        } else {
           this.namespace = namespace;
        }

    }

    public void populateMetaData(Configurator configurator,
                                 Map processedTypeMap)
            throws MetaDataPopulateException {
        this.name = this.propertyDescriptor.getName();
        this.getterMethod = this.propertyDescriptor.getReadMethod();
        this.setterMethod = this.propertyDescriptor.getWriteMethod();
        Class baseClass = null;
        try {
            this.classType = Util.getClassType(this.propertyDescriptor.getPropertyType());

            if ((this.classType & Constants.COLLECTION_TYPE) == Constants.COLLECTION_TYPE){
               // i.e. if this is collection type
               this.isArray = true;
               baseClass = Object.class;
            } else if ((this.classType & Constants.MAP_TYPE) == Constants.MAP_TYPE){
               // if the attribute is mep type we set a custom type for it.
               this.isArray = true;
               baseClass = MapType.class;
            } else {
                this.isArray = this.propertyDescriptor.getPropertyType().isArray();
                if (this.isArray) {
                    baseClass = this.propertyDescriptor.getPropertyType().getComponentType();
                } else {
                    baseClass = this.propertyDescriptor.getPropertyType();
                }
            }

            if (processedTypeMap.containsKey(baseClass)){
                this.type = (Type) processedTypeMap.get(baseClass);
            } else {
                this.type = new TypeImpl(baseClass);
                processedTypeMap.put(baseClass, this.type);
                this.type.populateMetaData(configurator, processedTypeMap);
            }
        } catch (IllegalAccessException e) {
            throw new MetaDataPopulateException("Can not instataite class "
                    + this.propertyDescriptor.getPropertyType().getName(), e);
        } catch (InstantiationException e) {
            throw new MetaDataPopulateException("Can not instataite class "
                    + this.propertyDescriptor.getPropertyType().getName(), e);
        }

    }


    /**
     * this method sets the XMLElement correctly. this method should be called only
     * if this is not processed
     * @param configurator
     * @param schemaMap
     * @throws SchemaGenerationException
     */
    public void generateSchema(Configurator configurator,
                               Map schemaMap)
            throws SchemaGenerationException {
        // here we have to send the XmlElement correctly
       this.isSchemaGenerated = true;
       this.element = new XmlElementImpl(!this.isArray && this.type.getJavaClass().isPrimitive());
       this.element.setName(this.name);
       this.element.setNamespace(this.namespace);
       this.element.setTopElement(false);

       if (!this.type.isSchemaGenerated()){
          this.type.generateSchema(configurator,schemaMap);
       }
       this.element.setType(this.type.getXmlType());
       this.element.setArray(this.isArray);

    }

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Method getGetterMethod() {
        return getterMethod;
    }

    public void setGetterMethod(Method getterMethod) {
        this.getterMethod = getterMethod;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public void setSetterMethod(Method setterMethod) {
        this.setterMethod = setterMethod;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public XmlElement getElement() {
        return element;
    }

    public void setElement(XmlElement element) {
        this.element = element;
    }

    public boolean isSchemaGenerated() {
        return isSchemaGenerated;
    }

    public void setSchemaGenerated(boolean schemaGenerated) {
        isSchemaGenerated = schemaGenerated;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

}
