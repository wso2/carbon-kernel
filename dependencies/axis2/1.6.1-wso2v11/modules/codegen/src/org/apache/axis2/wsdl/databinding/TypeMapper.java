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

package org.apache.axis2.wsdl.databinding;

import javax.xml.namespace.QName;
import java.util.Map;

public interface TypeMapper {


    /**
     * Returns whether the mapping is the object type or the normal class name type
     *
     * @return Returns boolean.
     */
    public boolean isObjectMappingPresent();

    /**
     * Gets the type mapping class name.
     *
     * @param qname name of the XML element to be mapped
     * @return Returns a string that represents the particular type.
     */
    public String getTypeMappingName(QName qname);

    /**
     * Gets the type mapping Object.
     *
     * @param qname name of the XML element to be mapped
     * @return Returns an Object that represents the particular class in a pre specified form. It
     *         can be a specific format to the databinding framework used. This allows tight
     *         integrations with the databinding framework, allowing the emitter to write the
     *         databinding classes in its own way.
     */
    public Object getTypeMappingObject(QName qname);

    public Object getQNameToMappingObject(QName qname);

    /**
     * Gets the parameter name.
     *
     * @param qname name of the XML element to get a parameter
     * @return Returns a unique parameter name.
     */
    public String getParameterName(QName qname);

    /**
     * Adds a type mapping name to the type mapper.
     *
     * @param qname
     * @param value
     * @see #getTypeMappingName(javax.xml.namespace.QName)
     */
    public void addTypeMappingName(QName qname, String value);

    /**
     * Adds a type mapping object to the type mapper.
     *
     * @param qname the xml Qname that this type refers to
     * @param value the type mapping object
     * @see #getTypeMappingObject(javax.xml.namespace.QName)
     */
    public void addTypeMappingObject(QName qname, Object value);

    /** @return Returns a map containing all type mapping names i.e. Qname to  classname */
    public Map getAllMappedNames();

    /** @return Returns a map containing all type mapping model objects i.e. Qname to model objects */
    public Map getAllMappedObjects();

    /** @return the default mapping name for this type mapper */
    public String getDefaultMappingName();

    /**
     * Sets the default type mapping - the databinders may change the default mapping to suit their
     * default mapping
     *
     * @param defaultMapping
     */
    public void setDefaultMappingName(String defaultMapping);


    /**
     * Allows the storage of a status object with a mapping to the qname. This may be used to store
     * certain status information that will be used by different type mappers. A given type mapper
     * may choose not to implement this!
     *
     * @param qname
     * @param status
     */
    public void addTypeMappingStatus(QName qName, Object status);

    /**
     * Returns the relevant status object given the qName of the xml element
     *
     * @param qName
     * @return the status object
     * @see #addTypeMappingStatus(javax.xml.namespace.QName, Object)
     */
    public Object getTypeMappingStatus(QName qName);
}
