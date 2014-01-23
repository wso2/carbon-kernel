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

package org.apache.axis2.jaxws.description.builder;

import java.lang.annotation.ElementType;

/**
 * This interface is intended to represent annotation instances 
 * that are not explicitly defined by the MDQ layer. This will 
 * allow the use of custom annotations or those outside the scope 
 * of the metadata that is intended for use within MDQ. The provider
 * of CustomAnnotationInstances will be responsbile for mapping
 * specific CustomAnnotationInstance implementations to actual
 * annotation types.
 *
 */
public interface CustomAnnotationInstance {
    
    /**
     * This returns a string that represents the fully qualified name
     * of the annotation type this instance represents.
     */
    public String getAnnotationClassName();
    
    /**
     * This sets a string that represents the fully qualified name
     * of the annotation type this instance represents.
     */
    public void setAnnotationClassName(String annotationClassName);
    
    /**
     * This method sets the ElementType Enum that represents the
     * target for this annotation instance.
     */
    public void setTarget(ElementType elementType);
    
    /**
     * This method returns the ElementType Enum that represents
     * the target for this annotation instance.
     */
    public ElementType getTarget();
    
    /**
     * This method stores parameter data in the CustomAnnotationInstance
     * based on the key that is supplied.
     */
    public void addParameterData(String paramName, Object paramValue) throws IllegalArgumentException;
    
    /**
     * This method retrieves the parameter data associated with the given
     * parameter name.
     */
    public Object getParameterData(String paramName) throws IllegalArgumentException;
    
}
