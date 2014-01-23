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

package org.apache.axis2.jaxws.runtime.description.marshal;

/** Annotation information cached for a particular class */
public interface AnnotationDesc {

    /** @return true if class has @XmlRootElement */
    public boolean hasXmlRootElement();

    /** @return @XmlRootElement name or defaulted name (null if !hasXmlRootElement) */
    public String getXmlRootElementName();

    /** @return @XmlRootElement namespace or default namespace (null if !hasXmlRootElement) */
    public String getXmlRootElementNamespace();

    /**
     * @return @XmlSeeAlso classes or null
     */
    public Class[] getXmlSeeAlsoClasses();
    
    /**
     * @return true if the class has an @XmlType
     */
    public boolean hasXmlType();
    
    /**
     * @return @XmlTypeName or (null if no @XmlType)
     */
    public String getXmlTypeName();
    
    /**
     * @return @XmlType namepsace (null if no @XmlType)
     */
    public String getXmlTypeNamespace();
}
