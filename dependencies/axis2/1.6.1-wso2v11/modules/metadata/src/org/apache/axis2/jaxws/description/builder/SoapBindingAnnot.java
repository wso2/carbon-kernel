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

public class SoapBindingAnnot implements javax.jws.soap.SOAPBinding {

    private Style style = Style.DOCUMENT;
    private Use use = Use.LITERAL;
    private ParameterStyle parameterStyle = ParameterStyle.WRAPPED;

    /** A SoapBindingAnnot cannot be instantiated. */
    private SoapBindingAnnot() {

    }
    
    private SoapBindingAnnot(Style style, Use use, ParameterStyle paramStyle) {
        this.style = style;
        this.use = use;
        this.parameterStyle = paramStyle;
    }

    public static SoapBindingAnnot createSoapBindingAnnotImpl() {
        return new SoapBindingAnnot();
    }
    public static SoapBindingAnnot createFromAnnotation(Annotation annotation) {
        SoapBindingAnnot returnAnnot = null;
        if (annotation != null && annotation instanceof javax.jws.soap.SOAPBinding) {
            javax.jws.soap.SOAPBinding sb = (javax.jws.soap.SOAPBinding) annotation;
            returnAnnot = new SoapBindingAnnot(sb.style(),
                                               sb.use(),
                                               sb.parameterStyle());
        }
        
        return returnAnnot;
    }

    public Style style() {
        return this.style;
    }

    public Use use() {
        return this.use;
    }

    public ParameterStyle parameterStyle() {
        return this.parameterStyle;
    }


    /** @param parameterStyle The parameterStyle to set. */
    public void setParameterStyle(ParameterStyle parameterStyle) {
        this.parameterStyle = parameterStyle;
    }

    /** @param style The style to set. */
    public void setStyle(Style style) {
        this.style = style;
    }

    /** @param use The use to set. */
    public void setUse(Use use) {
        this.use = use;
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
        sb.append("@SOAPBinding.style= " + style.toString());
        sb.append("@SOAPBinding.parameterStyle= " + parameterStyle.toString());
        sb.append("@SOAPBinding.use= " + use.toString());
        sb.append(newLine);
        return sb.toString();
	}
}
