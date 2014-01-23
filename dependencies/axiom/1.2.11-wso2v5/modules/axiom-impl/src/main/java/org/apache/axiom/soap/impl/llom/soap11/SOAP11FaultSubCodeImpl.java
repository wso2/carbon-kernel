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
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.llom.SOAPFaultSubCodeImpl;

public class SOAP11FaultSubCodeImpl extends SOAPFaultSubCodeImpl {


    public SOAP11FaultSubCodeImpl(SOAPFactory factory) {
        super(null, factory);
    }

    //changed
    public SOAP11FaultSubCodeImpl(SOAPFaultCode parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME, factory);
    }

    //changed
    public SOAP11FaultSubCodeImpl(SOAPFaultCode parent,
                                  OMXMLParserWrapper builder,
                                  SOAPFactory factory) {
        super(parent, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME, builder,
              factory);
    }

    public SOAP11FaultSubCodeImpl(SOAPFaultSubCode parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME, factory);
    }

    public SOAP11FaultSubCodeImpl(SOAPFaultSubCode parent,
                                  OMXMLParserWrapper builder,
                                  SOAPFactory factory) {
        super(parent, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME, builder,
              factory);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!((parent instanceof SOAP11FaultSubCodeImpl) ||
                (parent instanceof SOAP11FaultCodeImpl))) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultSubCodeImpl or SOAP11FaultCodeImpl, got " +
                            parent.getClass());
        }
    }

    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        if (!((parent instanceof SOAP11FaultSubCodeImpl) ||
                (parent instanceof SOAP11FaultCodeImpl))) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultSubCodeImpl or SOAP11FaultCodeImpl, got " +
                            subCode.getClass());
        }
        super.setSubCode(subCode);
    }

    public void setValue(SOAPFaultValue soapFaultSubCodeValue) throws SOAPProcessingException {
        if (!(soapFaultSubCodeValue instanceof SOAP11FaultValueImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultValueImpl, got " + soapFaultSubCodeValue.getClass());
        }
        super.setValue(soapFaultSubCodeValue);
    }
}
