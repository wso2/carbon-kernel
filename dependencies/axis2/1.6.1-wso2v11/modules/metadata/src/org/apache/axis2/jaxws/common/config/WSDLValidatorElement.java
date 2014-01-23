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

package org.apache.axis2.jaxws.common.config;

import javax.wsdl.extensions.ExtensibilityElement;

/**
 * WSDLValidator Element is used to store the state of the WSDL:Binding required=true
 * Elements.
 * State can be SUPPORTED, NOT_SUPPORTED, NOT_RECOGNIZED.
 * NOT_RECOGNIZED - This means JAX-WS runtime does not recoginze this element, this is default behavior.
 * 
 * SUPPORTED - This required element is recognized and understood by JAX-WS runtime.
 * 
 * NOT_SUPPORTED - This required element is NOT understood by JAX-WS runtime. 
 * This means that runtime recognize the element, but due to the current config we can't honor it
 * 
 * ERROR - There where errors while processing the extension, this state will populate the error 
 * message field.
 */
public class WSDLValidatorElement {
    private ExtensibilityElement extensionElement = null;
    private State state = State.NOT_RECOGNIZED;
    private String errorMessage = "";
    
    public ExtensibilityElement getExtensionElement() {
        return extensionElement;
    }
    public void setExtensionElement(ExtensibilityElement extensionElement) {
        this.extensionElement = extensionElement;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }
    
    public enum State {
        NOT_RECOGNIZED, 
        SUPPORTED, 
        NOT_SUPPORTED,
        ERROR
    }
}
