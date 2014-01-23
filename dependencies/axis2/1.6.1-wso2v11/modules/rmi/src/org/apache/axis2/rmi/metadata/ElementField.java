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
import org.apache.axis2.rmi.databind.RMIBean;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.impl.TypeImpl;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.metadata.xml.impl.XmlElementImpl;
import org.apache.axis2.rmi.types.MapType;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.Util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public class ElementField extends Field {

    /**
     * is this attribute an array
     */
    protected boolean isArray;

    /**
     * class type of the attribute class
     */
    protected int classType;

    /**
     * schmema element corresponding to this attribute
     * always an attribute refer to and schema XmlElement
     */
    protected XmlElement element;


    public ElementField() {
    }

    public ElementField(PropertyDescriptor propertyDescriptor, String namespace) {
        super(propertyDescriptor, namespace);
    }

    public void populateMetaData(Configurator configurator,
                                 Map processedTypeMap)
            throws MetaDataPopulateException {
        super.populateMetaData(configurator, processedTypeMap);
        Class baseClass = null;
        try {
            this.classType = Util.getClassType(this.propertyDescriptor.getPropertyType());

            if ((this.classType & Constants.COLLECTION_TYPE) == Constants.COLLECTION_TYPE) {
                // i.e. if this is collection type
                this.isArray = true;
                baseClass = Object.class;
            } else if ((this.classType & Constants.MAP_TYPE) == Constants.MAP_TYPE) {
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

            if (processedTypeMap.containsKey(baseClass)) {
                this.type = (Type) processedTypeMap.get(baseClass);
            } else if (RMIBean.class.isAssignableFrom(baseClass)){
                // if this bean is an RMIBean we have to use the type return
                // from that bean.
                try {
                    Method getBeanClassMethod = baseClass.getMethod("getBeanClass", new Class[]{});
                    Class rmiBeanType = (Class) getBeanClassMethod.invoke(null, new Object[]{});
                    if (rmiBeanType != null) {
                        this.type = (Type) rmiBeanType.newInstance();
                        processedTypeMap.put(baseClass, this.type);
                        this.type.populateMetaData(configurator, processedTypeMap);
                    } else {
                        throw new MetaDataPopulateException("there is no type class for rmi class "
                                + baseClass.getName());
                    }
                } catch (NoSuchMethodException e) {
                    throw new MetaDataPopulateException("No getBeanClass method is not defined for " +
                    " rmi bean class " + baseClass.getName());
                } catch (InvocationTargetException e) {
                    throw new MetaDataPopulateException("No getBeanClass method is not defined for " +
                    " rmi bean class " + baseClass.getName());
                }

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

    public void generateSchema(Configurator configurator, Map schemaMap) throws SchemaGenerationException {
        super.generateSchema(configurator, schemaMap);
        this.element = new XmlElementImpl(!this.isArray && this.type.getJavaClass().isPrimitive());
        this.element.setName(this.name);
        this.element.setNamespace(this.namespace);
        this.element.setTopElement(false);
        this.element.setType(this.type.getXmlType());
        this.element.setArray(this.isArray);

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

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

}
