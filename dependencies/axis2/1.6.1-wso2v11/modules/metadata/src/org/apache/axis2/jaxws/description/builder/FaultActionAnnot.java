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

import javax.xml.ws.FaultAction;
import java.lang.annotation.Annotation;

public class FaultActionAnnot implements FaultAction {
    
    private Class className;
    private String value;
    private String classNameString;

    private FaultActionAnnot() {
    }
    
    private FaultActionAnnot(Class className, String value) {
        this.className = className;
        this.value = value;
    }
    
    public static FaultActionAnnot createFaultActionAnnotImpl() {
        return new FaultActionAnnot();
    }
    
    public static FaultActionAnnot createFaultActionAnnotImpl(Class className, String value) {
        return new FaultActionAnnot(className, value);
    }
    
    public void setClassName(String classNameString) {
        this.classNameString = classNameString;
    }

    public void setClassName(Class className) {
        this.className = className;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Class className() {
        return className;
    }
    
    public String classNameString() {
        return classNameString;
    }

    public String value() {
        return value;
    }

    //hmm, should we really do this
    public Class<? extends Annotation> annotationType() {
        return Annotation.class;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        sb.append(newLine);
        sb.append("@FaultAction.className= " + className);
        sb.append(newLine);
        sb.append("@FaultAction.value= " + value);
        sb.append(newLine);
        return sb.toString();
    }
}
