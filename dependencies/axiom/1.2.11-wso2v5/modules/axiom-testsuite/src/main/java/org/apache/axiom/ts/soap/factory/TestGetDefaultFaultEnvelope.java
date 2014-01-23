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
package org.apache.axiom.ts.soap.factory;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

/**
 * Tests the behavior of {@link org.apache.axiom.soap.SOAPFactory#getDefaultFaultEnvelope()}.
 */
public class TestGetDefaultFaultEnvelope extends SOAPTestCase {
    public TestGetDefaultFaultEnvelope(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory, spec);
    }

    protected void runTest() throws Throwable {
        SOAPEnvelope envelope = soapFactory.getDefaultFaultEnvelope();

        assertNotNull("Default FaultEnvelope must have a SOAPFault in it",
                      envelope.getBody().getFault());
        assertNotNull(
                "Default FaultEnvelope must have a SOAPFaultCode in it",
                envelope.getBody().getFault().getCode());
        if (spec == SOAPSpec.SOAP12) {
            assertNotNull(
                    "Default FaultEnvelope must have a SOAPFaultCodeValue in it",
                    envelope.getBody().getFault().getCode().getValue());
        }
        assertNotNull(
                "Default FaultEnvelope must have a SOAPFaultReason in it",
                envelope.getBody().getFault().getReason());
        if (spec == SOAPSpec.SOAP12) {
            assertNotNull(
                    "Default FaultEnvelope must have a SOAPFaultText in it",
                    envelope.getBody().getFault().getReason().getFirstSOAPText());
        }
    }
}
