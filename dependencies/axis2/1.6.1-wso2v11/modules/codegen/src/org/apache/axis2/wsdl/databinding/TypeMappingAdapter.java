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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.wsdl.i18n.CodegenMessages;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TypeMappingAdapter implements TypeMapper {

    /**
     * Default class name is the OMElement or the default case However the extensions can override
     * the default class to suit the databinding framework!
     */
    protected String defaultClassName = OMElement.class.getName();

    protected static final String XSD_SCHEMA_URL = Constants.URI_2001_SCHEMA_XSD;

    //hashmap that contains the type mapping names
    protected HashMap qName2NameMap = new HashMap();

    //hashmap that contains the type mapping objects
    protected HashMap qName2ObjectMap = new HashMap();

    //hashmap for keeping the status objects.
    protected HashMap qName2StatusMap = new HashMap();

    //counter variable to generate unique parameter ID's
    protected int counter = 0;

    // this is to keep generated parameter names
    protected List parameterNameList = new ArrayList();

    protected boolean isObject = false;

    //Upper limit for the paramete count
    protected static final int UPPER_PARAM_LIMIT = 1000;
    private static final String PARAMETER_NAME_SUFFIX = "param";


    /**
     * Gets the type mapping name. If type mapping is not found, returns default.
     *
     * @see TypeMapper#getTypeMappingName(javax.xml.namespace.QName)
     */
    public String getTypeMappingName(QName qname) {

        if ((qname != null)) {
            Object o = qName2NameMap.get(qname);
            if (o != null) {
                return (String)o;
            } else if (Constants.XSD_ANYTYPE.equals(qname) ||
                    Constants.XSD_ANY.equals(qname)) {
                return defaultClassName;
            } else if (Constants.XSD_STRING.equals(qname)) {
                return String.class.getName();
            } else {
                throw new UnmatchedTypeException(
                        CodegenMessages.getMessage("databinding.typemapper.typeunmatched",
                                                   qname.getLocalPart(),
                                                   qname.getNamespaceURI())
                );
            }
        } else {
            return null;
        }


    }

    /** @see TypeMapper#getParameterName(javax.xml.namespace.QName) */
    public String getParameterName(QName qname) {
        if (counter == UPPER_PARAM_LIMIT) {
            counter = 0;
        }
        if ((qname != null) && (qname.getLocalPart().length() != 0)) {
            String paramName = JavaUtils.xmlNameToJavaIdentifier(qname.getLocalPart());

            if (parameterNameList.contains(paramName)) {
                paramName = paramName + counter++;
            }
            parameterNameList.add(paramName);
            return paramName;
        } else {
            return PARAMETER_NAME_SUFFIX + counter++;
        }
    }

    /** @see TypeMapper#addTypeMappingName(javax.xml.namespace.QName,String) */
    public void addTypeMappingName(QName qname, String value) {
        qName2NameMap.put(qname, value);
    }

    /**
     * @param qname
     * @return Returns object representing a specific form of the XSD compilation.
     * @see TypeMapper#getTypeMappingObject(javax.xml.namespace.QName)
     */
    public Object getTypeMappingObject(QName qname) {
        return qName2ObjectMap.get(qname);
    }

    /**
     * return the class name for this QName
     * @param qname
     * @return return class names
     */

    public Object getQNameToMappingObject(QName qname){
        return qName2NameMap.get(qname);
    }

    /**
     * @param qname
     * @param value
     * @see TypeMapper#addTypeMappingObject(javax.xml.namespace.QName, Object)
     */
    public void addTypeMappingObject(QName qname, Object value) {
        isObject = true;
        qName2ObjectMap.put(qname, value);
    }

    /**
     * @return Returns boolean.
     * @see TypeMapper#isObjectMappingPresent()
     */
    public boolean isObjectMappingPresent() {
        return isObject;
    }

    /** @see TypeMapper#getAllMappedNames() */
    public Map getAllMappedNames() {
        return qName2NameMap;
    }

    /** @see TypeMapper#getAllMappedObjects() */
    public Map getAllMappedObjects() {
        return qName2ObjectMap;
    }

    /** @see TypeMapper#getDefaultMappingName() */
    public String getDefaultMappingName() {
        return defaultClassName;
    }

    /**
     * @param defaultMapping
     * @see TypeMapper#setDefaultMappingName(String)
     */
    public void setDefaultMappingName(String defaultMapping) {
        this.defaultClassName = defaultMapping;
    }

    /**
     * @param qName
     * @param status
     * @see TypeMapper#addTypeMappingStatus(javax.xml.namespace.QName, Object)
     */
    public void addTypeMappingStatus(QName qName, Object status) {
        this.qName2StatusMap.put(qName, status);
    }

    /**
     * @param qName
     * @see TypeMapper#getTypeMappingStatus(javax.xml.namespace.QName)
     */
    public Object getTypeMappingStatus(QName qName) {
        return this.qName2StatusMap.get(qName);
    }
}
