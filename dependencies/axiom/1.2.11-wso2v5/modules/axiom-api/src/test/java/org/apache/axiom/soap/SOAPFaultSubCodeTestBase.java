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


public class SOAPFaultSubCodeTestBase extends SOAPFaultSubCodeTestCase {

    public SOAPFaultSubCodeTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory);
    }

    //SOAP Fault SubCode(In Fault Code) Test (Programaticaly Created)
    public void testSetValueInFaultCode() {
        soap12FaultSubCodeInCode.setValue(
                soap12Factory.createSOAPFaultValue(soap12FaultSubCodeInCode));
        assertFalse(
                "SOAP 1.2 Subcode Test In Fault Code : - After calling setValue method, getValue method returns null",
                soap12FaultSubCodeInCode.getValue() == null);
        try {
            soap12FaultSubCodeInCode.setValue(
                    soap12Factory.createSOAPFaultValue(soap12FaultCode));
        } catch (SOAPProcessingException e) {
            fail(
                    "SOAP 1.2 SOAPFaultSubCode Test In FaultCode : - FaultValue whose parent is FaultCode should not be set in to FaultSubCode, as a child");
        }
    }

    public void testGetValueInFaultCode() {
        assertTrue(
                "After creating SOAP12FaultSubCode In Fault Code, it has a FaultValue",
                soap12FaultSubCodeInCode.getValue() == null);
        soap12FaultSubCodeInCode.setValue(
                soap12Factory.createSOAPFaultValue(soap12FaultSubCodeInCode));
        assertFalse(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultCode : - After calling setValue method, getValue method returns null",
                soap12FaultSubCodeInCode.getValue() == null);
    }

    public void testsetSubCodeInFaultCode() {
        soap12FaultSubCodeInCode.setSubCode(
                soap12Factory.createSOAPFaultSubCode(soap12FaultSubCodeInCode));
        assertFalse(
                "SOAP 1.2 Subcode Test In Fault Code : - After calling setSubCode method, getSubCode method returns null",
                soap12FaultSubCodeInCode.getSubCode() == null);
        try {
            soap12FaultSubCodeInCode.setSubCode(
                    soap12Factory.createSOAPFaultSubCode(soap12FaultCode));
        } catch (SOAPProcessingException e) {
            fail(
                    "SOAP 1.2 SOAPFaultSubCode Test In FaultCode : - FaultSubCode whose parent is FaultCode should not be set in to FaultSubCode, as a child");
        }
    }

    public void testGetSubCodeInFaultCode() {
        //soap12FaultSubCodeInCode has a SubCode because a SubCode was created in setUp method of super class
//        assertTrue("After creating SOAP12FaultSubCode In Fault Code, it has a FaultSubCode",soap12FaultSubCodeInCode.getSubCode() == null);
        soap12FaultSubCodeInCode.setSubCode(
                soap12Factory.createSOAPFaultSubCode(soap12FaultSubCodeInCode));
        assertFalse(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultCode : - After calling setSubCode method, getSubCode method returns null",
                soap12FaultSubCodeInCode.getSubCode() == null);
    }

    //SOAP Fault SubCode(In Fault SubCode) Test (Programaticaly Created)
    public void testSetValueInFaultSubCode() {
        soap12FaultSubCodeInSubCode.setValue(
                soap12Factory.createSOAPFaultValue(soap12FaultSubCodeInSubCode));
        assertFalse(
                "SOAP 1.2 Subcode Test In Fault SubCode : - After calling setValue method, getValue method returns null",
                soap12FaultSubCodeInSubCode.getValue() == null);
        try {
            soap12FaultSubCodeInSubCode.setValue(
                    soap12Factory.createSOAPFaultValue(soap12FaultCode));
        } catch (SOAPProcessingException e) {
            fail(
                    "SOAP 1.2 SOAPFaultSubCode Test In FaultCode : - FaultValue whose parent is FaultCode should not be set in to FaultSubCode, as a child");
        }
    }

    public void testGetValueInFaultSubCode() {
        assertTrue(
                "After creating SOAP12FaultSubCode In Fault SubCode, it has a Fault Value",
                soap12FaultSubCodeInSubCode.getValue() == null);
        soap12FaultSubCodeInSubCode.setValue(
                soap12Factory.createSOAPFaultValue(soap12FaultSubCodeInSubCode));
        assertFalse(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultSubCode : - After calling setValue method, getValue method returns null",
                soap12FaultSubCodeInSubCode.getValue() == null);
    }

    public void testsetSubCodeInFaultSubCode() {
        soap12FaultSubCodeInSubCode.setSubCode(
                soap12Factory.createSOAPFaultSubCode(
                        soap12FaultSubCodeInSubCode));
        assertFalse(
                "SOAP 1.2 Subcode Test In Fault SubCode : - After calling setSubCode method, getSubCode method returns null",
                soap12FaultSubCodeInSubCode.getSubCode() == null);
        try {
            soap12FaultSubCodeInSubCode.setSubCode(
                    soap12Factory.createSOAPFaultSubCode(soap12FaultCode));
        } catch (SOAPProcessingException e) {
            fail(
                    "SOAP 1.2 SOAPFaultSubCode Test In FaultSubCode : - FaultSubCode whose parent is FaultCode should not be set in to FaultSubCode, as a child");
        }
    }

    public void testGetSubCodeInFaultSubCode() {
        assertTrue(
                "After creating SOAP12FaultSubCode In Fault SubCode, it has a FaultSubCode",
                soap12FaultSubCodeInSubCode.getSubCode() == null);
        soap12FaultSubCodeInSubCode.setSubCode(
                soap12Factory.createSOAPFaultSubCode(
                        soap12FaultSubCodeInSubCode));
        assertFalse(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultSubCode : - After calling setSubCode method, getSubCode method returns null",
                soap12FaultSubCodeInSubCode.getSubCode() == null);
    }

    //SOAP Fault SubCode(In Fault Code) Test (With Parser)
    public void testGetValueInFaultCodeWithParser() {
        assertFalse(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultCode With Parser : - getValue method returns null",
                soap12FaultSubCodeInFaultCodeWithParser.getValue() == null);
        assertTrue(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultCode With Parser : - Value text mismatch",
                soap12FaultSubCodeInFaultCodeWithParser.getValue().getText()
                        .equals("m:MessageTimeout In First Subcode"));
    }

    public void testGetSubCodeInFaultCodeWithParser() {
        assertFalse(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultCode With Parser : - getSubCode method returns null",
                soap12FaultSubCodeInFaultCodeWithParser.getSubCode() == null);
        assertTrue(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultCode With Parser : - SubCode local name mismatch",
                soap12FaultSubCodeInFaultCodeWithParser.getSubCode()
                        .getLocalName()
                        .equals(SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME));
    }

    //SOAP Fault SubCode(In Fault SubCode) Test (With Parser)
    public void testGetValueInFaultSubCodeWithParser() {
        assertFalse(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultSubCode With Parser : - getValue method returns null",
                soap12FaultSubCodeInSubCodeWithParser.getValue() == null);
        assertTrue(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultSubCode With Parser : - Value text mismatch",
                soap12FaultSubCodeInSubCodeWithParser.getValue().getText()
                        .equals("m:MessageTimeout In Second Subcode"));
    }

    public void testGetSubCodeInFaultSubCodeWithParser() {
        assertFalse(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultSubCode With Parser : - getSubCode method returns null",
                soap12FaultSubCodeInSubCodeWithParser.getSubCode() == null);
        assertTrue(
                "SOAP 1.2 SOAPFaultSubCode Test In FaultSubCode With Parser : - SubCode local name mismatch",
                soap12FaultSubCodeInSubCodeWithParser.getSubCode()
                        .getLocalName()
                        .equals(SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME));
    }
}
