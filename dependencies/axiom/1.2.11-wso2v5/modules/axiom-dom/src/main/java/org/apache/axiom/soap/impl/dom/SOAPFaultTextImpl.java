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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.dom.AttrImpl;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPProcessingException;

import javax.xml.namespace.QName;

public abstract class SOAPFaultTextImpl extends SOAPElement implements SOAPFaultText {

    protected OMAttribute langAttr;

    protected final OMNamespace langNamespace;

    protected SOAPFaultTextImpl(SOAPFaultReason parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, true, factory);
        this.langNamespace = factory.createOMNamespace(
                SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_URI,
                SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX);
    }

    protected SOAPFaultTextImpl(SOAPFaultReason parent,
                                OMXMLParserWrapper builder, SOAPFactory factory) {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, builder,
              factory);
        this.langNamespace = factory.createOMNamespace(
                SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_URI,
                SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX);
    }


    public void setLang(String lang) {
        langAttr =
                new AttrImpl(this.ownerNode,
                             SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME,
                             langNamespace,
                             lang, this.factory);
        this.addAttribute(langAttr);
    }

    public String getLang() {
        if (langAttr == null) {
            langAttr =
                    this.getAttribute(
                            new QName(langNamespace.getNamespaceURI(),
                                      SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME,
                                      SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX));
        }

        return langAttr == null ? null : langAttr.getAttributeValue();
    }
}
