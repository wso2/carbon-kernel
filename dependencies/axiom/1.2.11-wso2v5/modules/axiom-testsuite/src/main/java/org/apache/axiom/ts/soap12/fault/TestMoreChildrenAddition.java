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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

public class TestMoreChildrenAddition extends SOAPTestCase {
    public TestMoreChildrenAddition(OMMetaFactory metaFactory) {
        super(metaFactory, SOAPSpec.SOAP12);
    }

    protected void runTest() throws Throwable {
        SOAPEnvelope envelope = soapFactory.getDefaultFaultEnvelope();

        SOAPEnvelope soapEnvelope = soapFactory.getDefaultFaultEnvelope();
        String errorCodeString = "Some Error occurred !!";
        soapEnvelope.getBody().getFault().getCode().getValue().setText(
                errorCodeString);

        SOAPFaultCode code = soapEnvelope.getBody().getFault().getCode();
        envelope.getBody().getFault().setCode(code);

        assertTrue("Parent Value of Code has not been set to new fault",
                   code.getParent() == envelope.getBody().getFault());
        assertTrue("Parent Value of Code is still pointing to old fault",
                   code.getParent() != soapEnvelope.getBody().getFault());
        assertNull("Old fault must not have a fault code",
                   soapEnvelope.getBody().getFault().getCode());
        assertEquals("The SOAP Code value must be " + errorCodeString,
                     errorCodeString,
                     envelope.getBody().getFault().getCode().getValue().getText());
    }
}
