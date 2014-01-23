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

public class WebResultAnnot implements javax.jws.WebResult {

    private String name = "";
    private String targetNamespace = "";
    private boolean header = false;
    private String partName = "";

    /** A WebResultAnnot cannot be instantiated. */
    private WebResultAnnot() {

    }

    /** @return Returns an instance of WebResultAnnot. */
    public static WebResultAnnot createWebResultAnnotImpl() {
        return new WebResultAnnot();
    }

    /** @return Returns the header. */
    public boolean header() {
        return header;
    }

    /** @return Returns the name. */
    public String name() {
        return name;
    }

    /** @return Returns the partName. */
    public String partName() {
        return partName;
    }

    /** @return Returns the targetNamespace. */
    public String targetNamespace() {
        return targetNamespace;
    }

    /** @param header The header to set. */
    public void setHeader(boolean header) {
        this.header = header;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @param partName The partName to set. */
    public void setPartName(String partName) {
        this.partName = partName;
    }

    /** @param targetNamespace The targetNamespace to set. */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
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
        sb.append("@WebResult.name= " + name);
        sb.append(newLine);
        sb.append("@WebResult.partName= " + partName);
        sb.append(newLine);
        sb.append("@WebResult.targetNamespace= " + targetNamespace);
        sb.append(newLine);
        sb.append("@WebResult.header= ");
        if (header) {
            sb.append("true");
        } else {
            sb.append("false");
        }
        sb.append(newLine);
        return sb.toString();
	}

}
