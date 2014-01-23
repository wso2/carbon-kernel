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

public class SOAPFaultValueTestBase extends SOAPFaultSubCodeTestCase {

    public SOAPFaultValueTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory);
    }

    protected void setUp() throws Exception {
        super.setUp();
        soap12FaultValueInFaultSubCode =
                soap12Factory.createSOAPFaultValue(soap12FaultSubCodeInCode);
    }

    //SOAP 1.1 Fault Value Test (Programaticaly Created)
    public void testSOAP11setText() {
        soap11FaultValue.setText("This is only Test");
        assertTrue("SOAP 1.1 Fault Value Test : - value text mismatch",
                   soap11FaultValue.getText().equals("This is only Test"));
    }

    public void testSOAP11GetText() {
        assertTrue(
                "SOAP 1.1 Fault Value Test : - After creating Fault Value, it has a text",
                soap11FaultValue.getText().equals(""));
        soap11FaultValue.setText("This is only Test");
        assertFalse(
                "SOAP 1.1 Fault Value Test : - After calling setText method, getText method returns null",
                soap11FaultValue.getText().equals(""));
        assertTrue("SOAP 1.1 Fault Value Test : - value text mismatch",
                   soap11FaultValue.getText().equals("This is only Test"));
    }

    //SOAP 1.2 Fault Value(In Fault Code) Test (Programaticaly Created)
    public void testSOAP12setTextInFaultCode() {
        soap12FaultValueInFaultCode.setText("This is only Test");
        assertTrue(
                "SOAP 1.2 Fault Value Test in Fault Code : - value text mismatch",
                soap12FaultValueInFaultCode.getText().equals(
                        "This is only Test"));
    }

    public void testSOAP12GetTextInFaultCode() {
        assertTrue(
                "SOAP 1.2 Fault Value Test in Fault Code : - After creating Fault Value, it has a text",
                soap12FaultValueInFaultCode.getText().equals(""));
        soap12FaultValueInFaultCode.setText("This is only Test");
        assertFalse(
                "SOAP 1.2 Fault Value Test in Fault Code : - After calling setText method, getText method returns null",
                soap12FaultValueInFaultCode.getText().equals(""));
        assertTrue(
                "SOAP 1.2 Fault Value Test in Fault Code : - value text mismatch",
                soap12FaultValueInFaultCode.getText().equals(
                        "This is only Test"));
    }

    //SOAP 1.2 Fault Value(In Fault SubCode) Test (Programaticaly Created)
    public void testSOAP12setTextInFaultSubCode() {
        soap12FaultValueInFaultSubCode.setText("This is only Test");
        assertTrue(
                "SOAP 1.2 Fault Value Test in Fault SubCode : - value text mismatch",
                soap12FaultValueInFaultSubCode.getText().equals(
                        "This is only Test"));
    }

    public void testSOAP12GetTextInFaultSubCode() {
        assertTrue(
                "SOAP 1.2 Fault Value Test in Fault SubCode : - After creating Fault Value, it has a text",
                soap12FaultValueInFaultSubCode.getText().equals(""));
        soap12FaultValueInFaultSubCode.setText("This is only Test");
        assertFalse(
                "SOAP 1.2 Fault Value Test in Fault SubCode : - After calling setText method, getText method returns null",
                soap12FaultValueInFaultSubCode.getText().equals(""));
        assertTrue(
                "SOAP 1.2 Fault Value Test in Fault SubCode : - value text mismatch",
                soap12FaultValueInFaultSubCode.getText().equals(
                        "This is only Test"));
    }

    //SOAP 1.1 Fault Value Test (With Parser)
    public void testSOAP11GetTextWithParser() {
        assertTrue(
                "SOAP 1.1 Fault Value Test with parser : - value text mismatch",
                soap11FaultValueWithParser.trim().equals("env:Sender"));
    }

    //SOAP 1.2 Fault Value(In Fault Code) Test (With Parser)
    public void testSOAP12setTextWithParserInFaultCode() {
        assertTrue(
                "SOAP 1.2 Fault Value Test with parser in Fault Code : - value text mismatch",
                soap12FaultValueInFaultCodeWithParser.getText().equals(
                        "env:Sender"));
    }

    //SOAP 1.2 Fault Value(In Fault SubCode) Test (With Parser)
    public void testSOAP12setTextWithParserInFaultSubCode() {
        assertTrue(
                "SOAP 1.2 Fault Value Test with parser in Fault SubCode : - value text mismatch",
                soap12FaultValueInFaultSubCodeWithParser.getText().equals(
                        "m:MessageTimeout In First Subcode"));
    }
}
