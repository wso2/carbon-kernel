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
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;

public abstract class SOAPFaultSubCodeImpl extends SOAPElement implements SOAPFaultSubCode {

    protected SOAPFaultValue value;
    protected SOAPFaultSubCode subCode;


    protected SOAPFaultSubCodeImpl(OMElement parent, String localName, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, localName, true, factory);
    }

    protected SOAPFaultSubCodeImpl(OMElement parent,
                                   String localName,
                                   OMXMLParserWrapper builder,
                                   SOAPFactory factory) {
        super(parent, localName, builder, factory);
    }

    public void setValue(SOAPFaultValue soapFaultSubCodeValue) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, value, soapFaultSubCodeValue);
    }

    public SOAPFaultValue getValue() {
        if (value == null) {
            value = (SOAPFaultValue)getFirstChildWithName(SOAP12Constants.QNAME_FAULT_VALUE);
        }
        return value;
    }

    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, this.subCode, subCode);

    }

    public SOAPFaultSubCode getSubCode() {
        if (subCode == null) {
            subCode = (SOAPFaultSubCode)getFirstChildWithName(SOAP12Constants.QNAME_FAULT_SUBCODE);
        }
        return subCode;
    }
}
