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

public class WebEndpointAnnot implements javax.xml.ws.WebEndpoint {

    private String name = "";

    /** A WebEndpointAnnot cannot be instantiated. */
    private WebEndpointAnnot(String name) {
        this.name = name;
    }

    private WebEndpointAnnot() {

    }

    public static WebEndpointAnnot createWebEndpointAnnotImpl() {
        return new WebEndpointAnnot();
    }

    public static WebEndpointAnnot createWebEndpointAnnotImpl(String name) {
        return new WebEndpointAnnot(name);
    }

    /** @return Returns the name. */
    public String name() {
        return name;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
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
        sb.append("@WebEndpoint.name= " + name);
        sb.append(newLine);
        return sb.toString();
	}
}
