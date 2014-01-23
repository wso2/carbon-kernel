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

import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;
import java.lang.annotation.Annotation;

public class AddressingAnnot implements Addressing {

    private boolean enabled = true;
    private boolean required;
    private AddressingFeature.Responses responses = AddressingFeature.Responses.ALL;
    
    public boolean enabled() {
        return enabled;
    }
    
    public void setEnabled(boolean e) {
        enabled = e;
    }

    public boolean required() {
        return required;
    }
    
    public void setRequired(boolean r) {
        required = r;
    }

    public AddressingFeature.Responses responses() {
      return responses;
    }
    
    public void setResponses(AddressingFeature.Responses r) {
      responses = r;
    }
    
    public Class<? extends Annotation> annotationType() {
        return Addressing.class;
    }

    public String toString() {
        String string = null;
        try {
            string = "@" + getClass().getName() 
                + "(enabled=" + enabled
                + ", required=" + required
                + ", responses=" + responses
                + ")";
        } catch (Throwable t) {
            string = super.toString() + ": caught exception in toString(): " + t;
        }
        return string;
    }
}
