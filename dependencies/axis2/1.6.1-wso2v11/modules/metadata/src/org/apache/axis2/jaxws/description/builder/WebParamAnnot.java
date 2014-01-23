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

public class WebParamAnnot implements javax.jws.WebParam {

    private String name;
    private String targetNamespace;
    private Mode mode = Mode.IN;
    private boolean header;
    private String partName;

    /** A WebParamAnnot cannot be instantiated. */
    private WebParamAnnot() {

    }

    public static WebParamAnnot createWebParamAnnotImpl() {
        return new WebParamAnnot();
    }

    /**
     * Get the 'name'
     *
     * @return String
     */
    public String name() {
        return this.name;
    }

    public String targetNamespace() {
        return this.targetNamespace;
    }

    public Mode mode() {
        return this.mode;
    }

    public boolean header() {
        return this.header;
    }

    public String partName() {
        return this.partName;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @param targetNamespace The targetNamespace to set. */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    /** @param mode The mode to set. */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /** @param header The header to set. */
    public void setHeader(boolean header) {
        this.header = header;
    }

    /** @param partName The partName to set. */
    public void setPartName(String partName) {
        this.partName = partName;
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
        sb.append("@WebParam.name= " + name);
        sb.append(newLine);
        sb.append("@WebParam.partName= " + partName);
        sb.append(newLine);
        sb.append("@WebParam.mode = " + mode.toString());
        sb.append(newLine);
        sb.append("@WebParam.targetNamespace= " + targetNamespace);
        sb.append(newLine);
        sb.append("@WebParam.header= ");
        if (header) {
            sb.append("true");
        } else {
            sb.append("false");
        }
        sb.append(newLine);
        return sb.toString();
	}

}
