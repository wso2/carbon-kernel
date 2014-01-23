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

public class ResponseWrapperAnnot implements javax.xml.ws.ResponseWrapper {

    private String localName = "";
    private String targetNamespace = "";
    private String className = "";
    private String partName = "";


    /** A ResponseWrapperAnnot cannot be instantiated. */
    private ResponseWrapperAnnot() {

    }

    private ResponseWrapperAnnot(
            String localName,
            String targetNamespace,
            String className) {
        this(localName, targetNamespace, className, "");
    }

    private ResponseWrapperAnnot(
            String localName,
            String targetNamespace,
            String className,
	    String partName) {
        this.localName = localName;
        this.targetNamespace = targetNamespace;
        this.className = className;
	this.partName = partName;
    }

    public static ResponseWrapperAnnot createResponseWrapperAnnotImpl() {
        return new ResponseWrapperAnnot();
    }

    public static ResponseWrapperAnnot createResponseWrapperAnnotImpl(
            String localName,
            String targetNamespace,
            String className
    ) {
        return new ResponseWrapperAnnot(localName,
                                        targetNamespace,
                                        className);
    }

    public static ResponseWrapperAnnot createResponseWrapperAnnotImpl(
            String localName,
            String targetNamespace,
            String className,
	    String partName
    ) {
        return new ResponseWrapperAnnot(localName,
                                        targetNamespace,
                                        className,
					partName);
    }

    /** @return Returns the name. */
    public String localName() {
        return this.localName;
    }

    /** @return Returns the targetNamespace. */
    public String targetNamespace() {
        return this.targetNamespace;
    }

    /** @return Returns the wsdlLocation. */
    public String className() {
        return this.className;
    }

    /** @return Returns the partName. */
    public String partName() {
        return this.partName;
    }

    /** @param name The name to set. */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /** @param targetNamespace The targetNamespace to set. */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    /** @param wsdlLocation The wsdlLocation to set. */
    public void setClassName(String className) {
        this.className = className;
    }

    /** @param partName The name of the wsdl:part. */
    public void setPartName(String partName) {
        this.partName = partName;
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
        sb.append("@ResponseWrapper.localName= " + localName);
        sb.append(newLine);
        sb.append("@ResponseWrapper.className= " + className);
        sb.append(newLine);
        sb.append("@ResponseWrapper.targetNamespace= " + targetNamespace);
        sb.append(newLine);
        sb.append("@ResponseWrapper.partName= " + partName);
        sb.append(newLine);
        return sb.toString();
	}


}
