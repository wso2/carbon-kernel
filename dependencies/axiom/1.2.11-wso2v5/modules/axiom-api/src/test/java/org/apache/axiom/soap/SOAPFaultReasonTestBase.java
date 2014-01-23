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

package org.apache.axiom.soap;

import org.apache.axiom.om.OMMetaFactory;

public class SOAPFaultReasonTestBase extends SOAPFaultReasonTestCase {

    public SOAPFaultReasonTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory);
    }

    //SOAP 1.1 Fault Reason Test (Programaticaly Created)
    public void testSOAP11SOAPTextAPIs() {
        boolean gotFault = false;
        try {
            soap11FaultReason.addSOAPText(soap11Factory.createSOAPFaultText(soap11FaultReason));
        } catch (UnsupportedOperationException e) {
            // Cool, continue.
            gotFault = true;
        }
        assertTrue("Didn't get expected Exception for addSOAPText()!", gotFault);

        try {
            soap11FaultReason.getFirstSOAPText();
        } catch (UnsupportedOperationException e) {
            // Cool, continue.
            return;
        }
        fail("Didn't get expected Exception for getFirstSOAPText()!");
    }

    //SOAP 1.2 Fault Reason Test (Programaticaly Created)
    public void testSOAP12SetSOAPText() {
        soap12FaultReason.addSOAPText(
                soap12Factory.createSOAPFaultText(soap12FaultReason));
        assertFalse(
                "SOAP 1.2 FaultReason Test : - After calling addSOAPText, getFirstSOAPText returns null",
                soap12FaultReason.getFirstSOAPText() == null);
        try {
            soap12FaultReason.addSOAPText(
                    soap11Factory.createSOAPFaultText(soap11FaultReason));
            fail("SOAP11FaultText should not be added to SOAP12FaultReason");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP12GetSOAPText() {
        assertTrue(
                "SOAP 1.2 FaultReason Test : - After creating SOAP12FaultReason, it has a SOAPFaultText",
                soap12FaultReason.getFirstSOAPText() == null);
        soap12FaultReason.addSOAPText(
                soap12Factory.createSOAPFaultText(soap12FaultReason));
        assertFalse(
                "SOAP 1.2 FaultReason Test : - After calling addSOAPText, getFirstSOAPText returns null",
                soap12FaultReason.getFirstSOAPText() == null);
    }

    //SOAP 1.2 Fault Reason Test (With Parser)
    public void testSOAP12GetSOAPTextWithParser() {
        assertFalse(
                "SOAP 1.2 FaultReason Test With Parser : - getFirstSOAPText method returns null",
                soap12FaultReasonWithParser.getFirstSOAPText() == null);
    }

//    public void testMultipleSOAPReasonTexts() {
//        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
//
//        SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
////        soap
//    }
}
