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

import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;

public abstract class SOAPFaultCodeImpl extends SOAPElement implements SOAPFaultCode {

    /**
     * Constructor OMElementImpl
     *
     * @param parent
     * @param builder
     */
    public SOAPFaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder,
                             SOAPFactory factory) {
        super(parent, factory.getSOAPVersion().getFaultCodeQName().getLocalPart(), builder,
              factory);
    }

    /** @param parent  */
    public SOAPFaultCodeImpl(SOAPFault parent,
                             boolean extractNamespaceFromParent,
                             SOAPFactory factory) throws SOAPProcessingException {
        super(parent, factory.getSOAPVersion().getFaultCodeQName().getLocalPart(),
              extractNamespaceFromParent, factory);
    }

    /** Eran Chinthaka (chinthaka@apache.org) */
    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, value, value);
    }

    public SOAPFaultValue getValue() {
        return (SOAPFaultValue)getFirstChildWithName(SOAP12Constants.QNAME_FAULT_VALUE);
    }

    public void setSubCode(SOAPFaultSubCode value) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, getSubCode(), value);
    }

    public SOAPFaultSubCode getSubCode() {
        return (SOAPFaultSubCode)getFirstChildWithName(SOAP12Constants.QNAME_FAULT_SUBCODE);
    }

}
