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

package org.apache.axiom.soap.impl.llom.soap11;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.llom.SOAPFaultValueImpl;

public class SOAP11FaultValueImpl extends SOAPFaultValueImpl {

    public SOAP11FaultValueImpl(OMElement parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME, parent, factory);
    }

    public SOAP11FaultValueImpl(SOAPFactory factory) throws SOAPProcessingException {
        super((OMNamespace) null, SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME, factory);
    }

    public SOAP11FaultValueImpl(OMElement parent, OMXMLParserWrapper builder, SOAPFactory factory) {
        super(parent, SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME, builder, factory);
    }


    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!((parent instanceof SOAP11FaultSubCodeImpl) ||
                (parent instanceof SOAP11FaultCodeImpl))) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultSubCodeImpl or SOAP11FaultCodeImpl, got " +
                            parent.getClass());
        }
    }
}
