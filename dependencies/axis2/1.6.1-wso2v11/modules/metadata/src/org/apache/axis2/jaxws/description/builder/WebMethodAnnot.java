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

public class WebMethodAnnot implements javax.jws.WebMethod {

    private String operationName = "";
    private String action = "";
    private boolean exclude = false;

    /** A WebMethodAnnot cannot be instantiated. */
    private WebMethodAnnot() {

    }

    public static WebMethodAnnot createWebMethodAnnotImpl() {
        return new WebMethodAnnot();
    }

    public String operationName() {
        return this.operationName;
    }

    public String action() {
        return this.action;
    }

    public boolean exclude() {
        return this.exclude;
    }

    /** @param action The action to set. */
    public void setAction(String action) {
        this.action = action;
    }

    /** @param exclude The exclude to set. */
    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    /** @param operationName The operationName to set. */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
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
        sb.append("@WebMethod.operationName= " + operationName);
        sb.append(newLine);
        sb.append("@WebMethod.action= " + action);
        sb.append(newLine);
        sb.append("@WebMethod.exclude = ");
        if (exclude) {
            sb.append("true");
        } else {
            sb.append("false");
        }
        sb.append(newLine);
        return sb.toString();
	}

}
