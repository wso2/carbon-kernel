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

public class BindingTypeAnnot implements javax.xml.ws.BindingType {

    private String value = "";

    /** A BindingTypeAnnot cannot be instantiated. */
    private BindingTypeAnnot() {

    }

    private BindingTypeAnnot(String value) {
        this.value = value;
    }

    public static BindingTypeAnnot createBindingTypeAnnotImpl() {
        return new BindingTypeAnnot();
    }

    public static BindingTypeAnnot createBindingTypeAnnotImpl(String value) {
        return new BindingTypeAnnot(value);
    }
    
    public static BindingTypeAnnot createFromAnnotation(Annotation annotation) {
        BindingTypeAnnot returnAnnot = null;
        if (annotation != null && annotation instanceof javax.xml.ws.BindingType) {
            javax.xml.ws.BindingType bt = (javax.xml.ws.BindingType) annotation;
            returnAnnot = new BindingTypeAnnot(bt.value());
        }
        return returnAnnot;
    }

    /** @return Returns the value. */
    public String value() {
        return value;
    }

    /** @param value The value to set. */
    public void setValue(String value) {
        this.value = value;
    }

    //hmm, should we really do this
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
        sb.append("@BindingType.value= " + value);
        sb.append(newLine);
        return sb.toString();
	}


}
