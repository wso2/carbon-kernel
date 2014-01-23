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

package org.apache.axis2.jaxws.wrapper;

import org.apache.axis2.jaxws.utility.PropertyDescriptorPlus;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;

import java.util.List;
import java.util.Map;

/**
 * The JAXBWrapper tool is used to create a JAXB Object from a series of child objects (wrap) or get
 * the child objects from a JAXB Object (unwrap)
 */
public interface JAXBWrapperTool {
    /**
     * unwrap Returns the list of child objects of the jaxb object
     *
     * @param jaxbObject that represents the type
     * @param childNames list of xml child names as String
     * @param pdMap      PropertyDescriptorMap describing the jaxbObject
     * @return list of Objects in the same order as the element names.
     */
    public Object[] unWrap(Object jaxbObject,
                           List<String> childNames,
                           Map<String, PropertyDescriptorPlus> pdMap) throws JAXBWrapperException;

    /**
     * unwrap Returns the list of child objects of the jaxb object
     *
     * @param jaxbObject that represents the type
     * @param childNames list of xml child names as String
     * @return list of Objects in the same order as the element names. Note: This method creates a
     *         PropertyDescriptor map; thus it is less performant than the other unWrap method
     */
    public Object[] unWrap(Object jaxbObject,
                           List<String> childNames) throws JAXBWrapperException;

    /**
     * Short cut if there is only one Object in the JAXB Object
     * 
     * @param jaxbObject that represents the type
     * @param childName  xml child names as String
     * @param pd        PropertyDescriptor 
     * @return child Object value
     */
    public Object unWrap(Object jaxbObject,
                         String childName,
                         PropertyDescriptorPlus pd) throws JAXBWrapperException;

    /**
     * wrap Creates a jaxb object that is initialized with the child objects.
     * <p/>
     * Note that the jaxbClass must be the class the represents the complexType. (It should never be
     * JAXBElement)
     *
     * @param jaxbClass
     * @param childNames    list of xml child names as String
     * @param childObjects, component type objects
     * @param pdMap         PropertyDescriptorMap describing the jaxbObject
     */
    public Object wrap(Class jaxbClass,
                       List<String> childNames,
                       Map<String, Object> childObjects,
                       Map<String, Class> declaredClassMap,
                       Map<String, PropertyDescriptorPlus> pdMap) throws JAXBWrapperException;

    /**
     * wrap Creates a jaxb object that is initialized with the child objects.
     * <p/>
     * Note that the jaxbClass must be the class the represents the complexType. (It should never be
     * JAXBElement)
     *
     * @param jaxbClass
     * @param childNames    list of xml child names as String
     * @param childObjects, component type objects
     * @param pdMap         PropertyDescriptorMap describing the jaxbObject Note: This method
     *                      creates a PropertyDescriptor map; thus it is less performant than the
     *                      other unWrap method
     */
    public Object wrap(Class jaxbClass,
                       List<String> childNames,
                       Map<String, Object> childObjects) throws JAXBWrapperException;


    /**
     * Short Cut for JAXB objects with one child
     * wrap Creates a jaxb object that is initialized with one child object.
     * <p/>
     * Note that the jaxbClass must be the class the represents the complexType. (It should never be
     * JAXBElement)
     *
     * @param jaxbClass
     * @param childName     xml child name as String or null if no child
     * @param childObject   component type object
     * @param declaredClass declared class
     * @param pd            PropertyDescriptor for this jaxbObject
     */
    public Object wrap(Class jaxbClass,
                       String childName,
                       Object childObject,
                       Class declaredClass,
                       PropertyDescriptorPlus pd) throws JAXBWrapperException;
}

