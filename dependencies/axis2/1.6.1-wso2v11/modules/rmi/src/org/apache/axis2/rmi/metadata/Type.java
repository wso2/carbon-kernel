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
import org.apache.axis2.rmi.metadata.xml.XmlType;

import java.util.List;
import java.util.Map;

public interface Type {

    /**
     * popualate the meta data corresponding to this type
     * @param configurator
     */
    public void populateMetaData(Configurator configurator, Map processedTypeMap) throws MetaDataPopulateException;

    /**
     * this method sets the xmlType correctly. this method should only be invoked
     * if it has not already processed
     * @param configurator
     * @param schemaMap
     */

    public void generateSchema(Configurator configurator, Map schemaMap) throws SchemaGenerationException;

    public void populateAllElementFields(List elementFieldsList);

    public void populateAllAttributeFields(List attributeFieldsList);

    public List getAllElementFields();

    public List getAllAttributeFields();

    public boolean isSchemaGenerated();

    public void setSchemaGenerated(boolean schemaGenerated);

    public Class getJavaClass();

    public void setJavaClass(Class javaClass);

    public List getElementFields();

    public void setElementFields(List elementFields);

    public String getName();

    public void setName(String name);

    public String getNamespace();

    public void setNamespace(String namespace);

    public XmlType getXmlType();

    public void setXmlType(XmlType xmlType);

    public Type getParentType();

    public void setParentType(Type parentType);

    public List getAttributeFields();

    public void setAttributeFields(List attributeFields);

}
