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

package org.apache.axis2.jaxws.addressing;

import javax.xml.ws.WebServiceFeature;

public final class SubmissionAddressingFeature extends WebServiceFeature {
    public static final String ID = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    
    protected boolean required;
    
    public SubmissionAddressingFeature() {
        this(true, false);
    }
    
    public SubmissionAddressingFeature(boolean enabled) {
        this(enabled, false);
    }
    
    public SubmissionAddressingFeature(boolean enabled, boolean required) {
        this.enabled  = enabled;
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }
    
    @Override
    public String getID() {
        return ID;
    }
}
