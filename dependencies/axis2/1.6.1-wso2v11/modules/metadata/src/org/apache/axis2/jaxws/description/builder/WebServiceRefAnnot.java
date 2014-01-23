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

import java.lang.annotation.Annotation;

public class WebServiceRefAnnot implements javax.xml.ws.WebServiceRef {

    private String name = "";
    private String wsdlLocation = "";
    private Class type;
    private Class value;
    private String mappedName = "";
    private String lookup = "";
    
    // TODO: Remove the String versions of the Class attributes when the associated deprecated 
    // methods are removed.
    private String typeString = "";
    private String valueString = "";

    /** A WebServiceRefAnnot cannot be instantiated. */
    private WebServiceRefAnnot() {

    }

    private WebServiceRefAnnot(
            String name,
            String wsdlLocation,
            Class type,
            Class value,
            String mappedName,
	    String lookup) {
        this.name = name;
        this.wsdlLocation = wsdlLocation;
        this.type = type;
        this.value = value;
        this.mappedName = mappedName;
	this.lookup = lookup;
    }

    private WebServiceRefAnnot(
            String name,
            String wsdlLocation,
            Class type,
            Class value,
            String mappedName) {
        this(name, wsdlLocation, type, value, mappedName, "");
    }

    /**
     * 
     * @deprecated The String values for type and value shouldn't be used.  Use {@link #WebServiceRefAnnot(String, String, Class, Class, String)}
     * 
     * @param name
     * @param wsdlLocation
     * @param type
     * @param value
     * @param mappedName
     * @param typeString
     * @param valueString
     */
    private WebServiceRefAnnot(
            String name,
            String wsdlLocation,
            Class type,
            Class value,
            String mappedName,
            String typeString,
            String valueString) {
        this.name = name;
        this.wsdlLocation = wsdlLocation;
        this.type = type;
        this.value = value;
        this.mappedName = mappedName;
        this.typeString = typeString;
        this.valueString = valueString;
    }

    public static WebServiceRefAnnot createWebServiceRefAnnotImpl() {
        return new WebServiceRefAnnot();
    }

    public static WebServiceRefAnnot createWebServiceRefAnnotImpl(
            String name,
            String wsdlLocation,
            Class type,
            Class value,
            String mappedName) {

        return new WebServiceRefAnnot(name,
                                      wsdlLocation,
                                      type,
                                      value,
                                      mappedName);
    }

    public static WebServiceRefAnnot createWebServiceRefAnnotImpl(
            String name,
            String wsdlLocation,
            Class type,
            Class value,
            String mappedName,
            String lookup) {
        return new WebServiceRefAnnot(name,
                                      wsdlLocation,
                                      type,
                                      value,
                                      mappedName,
				      lookup);
    }

    /**
     * 
     * @deprecated The String values for type and value should not be used.  Use {@link #createWebServiceRefAnnotImpl(String, String, Class, Class, String)}
     * 
     *  There shouldn't be both a class and String for type and value; there isn't on the actual 
     *  annotation.
     * 
     * @param name
     * @param wsdlLocation
     * @param type
     * @param value
     * @param mappedName
     * @param typeString
     * @param valueString
     * @return
     */
    public static WebServiceRefAnnot createWebServiceRefAnnotImpl(
            String name,
            String wsdlLocation,
            Class type,
            Class value,
            String mappedName,
            String typeString,
            String valueString
    ) {
        return new WebServiceRefAnnot(name,
                                      wsdlLocation,
                                      type,
                                      value,
                                      mappedName,
                                      typeString,
                                      valueString);
    }


    /** @return Returns the mappedName. */
    public String mappedName() {
        return mappedName;
    }

    /** @return Returns the name. */
    public String name() {
        return name;
    }

    /** @return Returns the type. */
    public Class type() {
        return type;
    }

    /** @return Returns the value. */
    public Class value() {
        return value;
    }

    /** @return Returns the wsdlLocation. */
    public String wsdlLocation() {
        return wsdlLocation;
    }

    /** @return Returns the lookup. */
    public String lookup() {
        return lookup;
    }

    /** 
     * @deprecated Use {@link #type()} 
     * @return Returns the typeString. 
     */
    public String getTypeString() {
        return typeString;
    }

    /**
     * @deprecated Use {@link #value()} 
     * @return Returns the valueString. */
    public String getValueString() {
        return valueString;
    }

    /** @param mappedName The mappedName to set. */
    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @param type The type to set. */
    public void setType(Class type) {
        this.type = type;
    }

    /** @param value The value to set. */
    public void setValue(Class value) {
        this.value = value;
    }

    /** @param wsdlLocation The wsdlLocation to set. */
    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    /** @return Returns the wsdlLocation. */
    public String getWsdlLocation() {
        return wsdlLocation;
    }

    /** @param lookup A portable JNDI lookup name that resolves to the target web service reference. */
    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    /** 
     * @deprecated Use {@link #setType(Class)}
     * @param typeString The typeString to set. */
    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }

    /**
     * @deprecated {@link #setValue(Class)} 
     * @param valueString The valueString to set. */
    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Class<Annotation> annotationType() {
        return Annotation.class;
    }

    /**
     * Convenience method for unit testing. We will print all of the
     * data members here.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        sb.append(newLine);
        sb.append("@WebServiceRef.name= " + name);
        sb.append(newLine);
        sb.append("@WebServiceRef.wsdlLocation= " + wsdlLocation);
        sb.append(newLine);
        sb.append("@WebServiceRef.mappedName= " + mappedName);
        sb.append(newLine);
        sb.append("@WebServiceRef.typeString= " + typeString);
        sb.append(newLine);
        sb.append("@WebServiceRef.type= " + ((type != null) ? type.toString() : null));
        sb.append(newLine);
        sb.append("@WebServiceRef.valueString= " + valueString);
        sb.append(newLine);
        sb.append("@WebServiceRef.value= " + ((value != null) ? value.toString() : null));
        sb.append(newLine);
        sb.append("@WebServiceRef.lookup= " + lookup);
        sb.append(newLine);
        return sb.toString();
	}
}
