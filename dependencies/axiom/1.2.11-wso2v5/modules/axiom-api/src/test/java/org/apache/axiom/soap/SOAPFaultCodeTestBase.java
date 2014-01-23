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


public class SOAPFaultCodeTestBase extends SOAPFaultCodeTestCase {

    public SOAPFaultCodeTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory);
    }

    //SOAP 1.1 Fault Code Test (Programaticaly Created)
//    public void testSOAP11SetValue() {
//        soap11FaultCode.setValue(
//                soap11Factory.createSOAPFaultValue(soap11FaultCode));
//        assertFalse(
//                "SOAP 1.1 Fault Code Test :- After calling setValue method, getValue method returns null",
//                soap11FaultCode.getValue() == null);
//        try {
//            soap11FaultCode.setValue(
//                    soap12Factory.createSOAPFaultValue(soap12FaultCode));
//            fail("SOAP12FaultValue should not be inserted to SOAP11FaultCode");
//        } catch (SOAPProcessingException e) {
//            assertTrue(true);
//        }
//
//    }

    public void testSOAP11GetValue() {
        assertNull(soap11FaultCode.getValue());
//        assertTrue(
//                "SOAP 1.1 Fault Code Test :- After creating soapfaultcode, it has a value",
//                soap11FaultCode.getValue() == null);
//        soap11FaultCode.setValue(
//                soap11Factory.createSOAPFaultValue(soap11FaultCode));
//        assertFalse(
//                "SOAP 1.1 Fault Code Test :- After calling setValue method, getValue method returns null",
//                soap11FaultCode.getValue() == null);
    }

    //SOAP 1.2 Fault Code Test (Programaticaly Created)
    public void testSOAP12SetValue() {
        soap12FaultCode.setValue(
                soap12Factory.createSOAPFaultValue(soap12FaultCode));
        assertFalse(
                "SOAP 1.2 Fault Code Test :- After calling setValue method, getValue method returns null",
                soap12FaultCode.getValue() == null);
        try {
            soap12FaultCode.setValue(
                    soap11Factory.createSOAPFaultValue(soap11FaultCode));
            fail("SOAP11FaultValue should not be inserted to SOAP12FaultCode");
        } catch (SOAPProcessingException e) {
            assertTrue(true);
        }

        try {
            soap12FaultCode.setValue(
                    soap12Factory.createSOAPFaultValue(
                            soap12Factory.createSOAPFaultSubCode(
                                    soap12FaultCode)));
        } catch (Exception e) {
            fail(
                    "SOAP 1.2 Fault Code Test :- When calling setValue method, parent of value element mismatch");
        }
    }

    public void testSOAP12GetValue() {
        assertTrue(
                "SOAP 1.2 Fault Code Test :- After creating soapfaultcode, it has a value",
                soap12FaultCode.getValue() == null);
        soap12FaultCode.setValue(
                soap12Factory.createSOAPFaultValue(soap12FaultCode));
        assertFalse(
                "SOAP 1.2 Fault Code Test :- After calling setValue method, getValue method returns null",
                soap12FaultCode.getValue() == null);
    }

    public void testSOAP12SetSubCode() {
        soap12FaultCode.setSubCode(
                soap12Factory.createSOAPFaultSubCode(soap12FaultCode));
        assertFalse(
                "SOAP 1.2 Fault Code Test :- After calling setSubCode method, getSubCode method returns null",
                soap12FaultCode.getSubCode() == null);
        try {
            soap12FaultCode.setSubCode(
                    soap11Factory.createSOAPFaultSubCode(soap11FaultCode));
            fail(
                    "SOAP11FaultSubCode should not be inserted to SOAP12FaultCode");
        } catch (SOAPProcessingException e) {
            assertTrue(true);
        }

        try {
            soap12FaultCode.setSubCode(
                    soap12Factory.createSOAPFaultSubCode(
                            soap12Factory.createSOAPFaultSubCode(
                                    soap12FaultCode)));
        } catch (Exception e) {
            fail(
                    "SOAP 1.2 Fault Code Test :- When calling setSubCode method, parent of subcode element mismatch");
        }
    }

    public void testSOAP12GetSubCode() {
        assertTrue(
                "SOAP 1.2 Fault Code Test :- After creating soapfaultcode, it has a subcode",
                soap12FaultCode.getSubCode() == null);
        soap12FaultCode.setSubCode(
                soap12Factory.createSOAPFaultSubCode(soap12FaultCode));
        assertFalse(
                "SOAP 1.2 Fault Code Test :- After calling setSubCode method, getSubCode method returns null",
                soap12FaultCode.getSubCode() == null);
    }

    //SOAP 1.1 Fault Code Test (With Parser)
    public void testSOAP11GetValueWithParser() {
        assertNull(soap11FaultCodeWithParser.getValue());
    }

    //SOAP 1.2 Fault Code Test (With Parser)
    public void testSOAP12GetValueWithParser() {
        assertFalse(
                "SOAP 1.2 Fault Code Test with parser : - getValue method returns null",
                soap12FaultCodeWithParser.getValue() == null);
        assertTrue(
                "SOAP 1.2 Fault Code Test with parser : - Value local name mismatch",
                soap12FaultCodeWithParser.getValue().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
    }

    public void testSOAP12GetSubCodeWithParser() {
        assertFalse(
                "SOAP 1.2 Fault Code Test with parser :- getSubCode method returns null",
                soap12FaultCodeWithParser.getSubCode() == null);
        assertTrue(
                "SOAP 1.2 Fault Code Test with parser : - subcode local name mismatch",
                soap12FaultCodeWithParser.getSubCode().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME));
    }
}
