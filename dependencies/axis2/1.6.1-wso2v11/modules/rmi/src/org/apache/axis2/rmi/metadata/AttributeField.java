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
import org.apache.axis2.rmi.metadata.xml.XmlAttribute;
import org.apache.axis2.rmi.metadata.xml.impl.XmlAttributeImpl;
import org.apache.axis2.rmi.util.JavaTypeToQNameMap;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public class AttributeField extends Field {

    /**
     * xml attribute representing this attribute
     */
    protected XmlAttribute attribute;

    private boolean isRequried;

    public AttributeField() {
    }

    public AttributeField(PropertyDescriptor propertyDescriptor, String namespace) {
        super(propertyDescriptor, namespace);
    }

    public void populateMetaData(Configurator configurator, Map processedTypeMap)
            throws MetaDataPopulateException {
        super.populateMetaData(configurator, processedTypeMap);
        if (!this.propertyDescriptor.getPropertyType().isArray()) {
            Class baseClass = this.propertyDescriptor.getPropertyType();
            if (JavaTypeToQNameMap.containsKey(baseClass) ||
                    RMIBean.class.isAssignableFrom(baseClass)) {

                if (processedTypeMap.containsKey(baseClass)) {
                    this.type = (Type) processedTypeMap.get(baseClass);
                } else if (RMIBean.class.isAssignableFrom(baseClass)) {
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
                    } catch (IllegalAccessException e) {
                        throw new MetaDataPopulateException("Can not invoke rmi bean class " +
                                baseClass.getName());
                    } catch (InstantiationException e) {
                        throw new MetaDataPopulateException("Can not instantiate RMI bean class " +
                                baseClass.getName());
                    }
                } else {
                    this.type = new TypeImpl(baseClass);
                    processedTypeMap.put(baseClass, this.type);
                    this.type.populateMetaData(configurator, processedTypeMap);
                }
            } else {
                throw new MetaDataPopulateException("Attribute element must have a known simpe type");
            }

        } else {
            throw new MetaDataPopulateException("Attribute element can not be an array type");
        }

    }

    public void generateSchema(Configurator configurator, Map schemaMap)
            throws SchemaGenerationException {
        super.generateSchema(configurator, schemaMap);
        this.attribute = new XmlAttributeImpl(this.getPropertyDescriptor().getPropertyType().isPrimitive());
        this.attribute.setName(this.name);
        this.attribute.setNamespace(this.namespace);
        this.attribute.setType(this.type.getXmlType());
        this.attribute.setRequired(this.isRequried);
    }

    public XmlAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(XmlAttribute attribute) {
        this.attribute = attribute;
    }

    public boolean isRequried() {
        return isRequried;
    }

    public void setRequried(boolean requried) {
        isRequried = requried;
    }

}
