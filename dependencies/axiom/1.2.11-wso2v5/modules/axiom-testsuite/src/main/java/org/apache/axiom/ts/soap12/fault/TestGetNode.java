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
package org.apache.axiom.ts.soap12.fault;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

public class TestGetNode extends SOAPTestCase {
    public TestGetNode(OMMetaFactory metaFactory) {
        super(metaFactory, SOAPSpec.SOAP12);
    }

    protected void runTest() throws Throwable {
        SOAPFault soapFault = soapFactory.createSOAPFault();
        assertNull(
                "SOAP 1.2 Fault Test:- After creating a SOAP12Fault, it has a node",
                soapFault.getNode());
        soapFault.setNode(soapFactory.createSOAPFaultNode(soapFault));
        assertNotNull(
                "SOAP 1.2 Fault Test:- After calling setNode method, Fault has no node",
                soapFault.getNode());
        assertEquals("SOAP 1.2 Fault Test:- Fault node local name mismatch",
                SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME, soapFault.getNode().getLocalName());
    }
}
