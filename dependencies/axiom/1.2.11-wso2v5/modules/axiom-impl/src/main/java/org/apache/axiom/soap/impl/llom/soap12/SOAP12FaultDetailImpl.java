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

package org.apache.axiom.soap.impl.llom.soap12;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.impl.llom.SOAPFaultDetailImpl;


public class SOAP12FaultDetailImpl extends SOAPFaultDetailImpl {

    public SOAP12FaultDetailImpl(SOAPFactory factory) {
        super(factory.getNamespace(), factory);
        this.localName = SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME;

    }

    public SOAP12FaultDetailImpl(SOAPFault parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, true, factory);
        this.localName = SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME;
    }

    public SOAP12FaultDetailImpl(SOAPFault parent, OMXMLParserWrapper builder,
                                 SOAPFactory factory) {
        super(parent, builder, factory);
        this.localName = SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME;
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP12FaultImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP12FaultImpl as parent, got " + parent.getClass());
        }
    }
}
