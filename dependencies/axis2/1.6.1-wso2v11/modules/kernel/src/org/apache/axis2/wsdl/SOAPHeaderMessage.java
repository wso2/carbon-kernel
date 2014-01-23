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

package org.apache.axis2.wsdl;

import org.apache.axis2.namespace.Constants;

import javax.xml.namespace.QName;

public class SOAPHeaderMessage {

    public static final QName SOAP_11_HEADER = new QName(
            Constants.URI_WSDL11_SOAP, "header");
    public static final QName SOAP_12_HEADER = new QName(
            Constants.URI_WSDL12_SOAP, "header");

    private QName messageName = null;
    private String part = null;
    private QName element = null;
    private QName type;
    private String use;
    private String namespaceURI;
    private boolean required;
    private boolean mustUnderstand;


    public SOAPHeaderMessage() {
        this.type = SOAP_11_HEADER;
    }

    public SOAPHeaderMessage(QName type) {
        this.type = type;
    }

    public QName getMessage() {
        return messageName;
    }

    public void setMessage(QName message) {
        this.messageName = message;
    }

    public String part() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public QName getElement() {
        return element;
    }

    public void setElement(QName element) {
        this.element = element;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }


    public boolean isMustUnderstand() {
        return mustUnderstand;
    }

    public void setMustUnderstand(boolean mustUnderstand) {
        this.mustUnderstand = mustUnderstand;
    }


}
