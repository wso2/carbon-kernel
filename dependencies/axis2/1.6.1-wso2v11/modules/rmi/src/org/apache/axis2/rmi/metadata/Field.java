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
import org.apache.axis2.rmi.util.Constants;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;


public abstract class Field {

    /**
     * property descriptor for this attribute
     */
    protected PropertyDescriptor propertyDescriptor;
    /**
     * name of the attribute this is the name of the XmlElement as well
     */
    protected String name;

    /**
     * namespce of this attribute
     * this is always the namespace of the parent type
     */
    protected String namespace;

    /**
     * getter method of the attribute
     */
    protected Method getterMethod;

    /**
     * setter method of the attribute
     */
    protected Method setterMethod;

    /**
     * attribute metadata type
     */
    protected Type type;

    /**
     * boolean variable to check to see whether we generated the
     * element for this schema.
     * although we can check for null value of the element we prefer
     * to keep a seperate variable.
     */
    protected boolean isSchemaGenerated;

    /**
     * default constructor
     */
    public Field() {
    }

    /**
     * constructor with the property descriptor
     *
     * @param propertyDescriptor
     */
    public Field(PropertyDescriptor propertyDescriptor,
                 String namespace) {
        this.propertyDescriptor = propertyDescriptor;
        if (Constants.RMI_TYPE_NAMSPACE.equals(namespace)) {
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

    }


    /**
     * this method sets the XMLElement correctly. this method should be called only
     * if this is not processed
     *
     * @param configurator
     * @param schemaMap
     * @throws org.apache.axis2.rmi.exception.SchemaGenerationException
     *
     */
    public void generateSchema(Configurator configurator,
                               Map schemaMap)
            throws SchemaGenerationException {
        // here we have to send the XmlElement correctly
        this.isSchemaGenerated = true;

        if (!this.type.isSchemaGenerated()) {
            this.type.generateSchema(configurator, schemaMap);
        }

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

}
