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

package org.apache.axiom.soap.impl.dom;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;

public class SOAPTextImpl extends SOAPElement {

    protected SOAPTextImpl(OMElement parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, true, factory);
    }

    protected SOAPTextImpl(OMElement parent, OMXMLParserWrapper builder,
                           SOAPFactory factory) {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, builder, factory);
    }

    public void setLang(String lang) {
        // TODO : Chinthaka fix me
    }

    public String getLang() {
        // TODO Chinthaka fix me
        return null;
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        // do nothing
    }
}
