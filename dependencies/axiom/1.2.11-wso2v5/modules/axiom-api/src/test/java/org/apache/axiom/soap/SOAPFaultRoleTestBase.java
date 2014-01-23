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

public class SOAPFaultRoleTestBase extends SOAPFaultTestCase {

    protected SOAPFaultRole soap11FaultRole;
    protected SOAPFaultRole soap12FaultRole;
    protected SOAPFaultRole soap11FaultRoleWithParser;
    protected SOAPFaultRole soap12FaultRoleWithParser;

    public SOAPFaultRoleTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory);
    }

    protected void setUp() throws Exception {
        super.setUp();
        soap11FaultRole = soap11Factory.createSOAPFaultRole(soap11Fault);
        soap12FaultRole = soap12Factory.createSOAPFaultRole(soap12Fault);
        soap11FaultRoleWithParser = soap11FaultWithParser.getRole();
        soap12FaultRoleWithParser = soap12FaultWithParser.getRole();
    }

    //SOAP 1.1 Fault Role Test (Programaticaly Created)
    public void testSOAP11SetRoleValue() {
        soap11FaultRole.setRoleValue("This is only a test");
        assertTrue(
                "SOAP 1.1 Fault Role Test : - After calling setRoleValue method, getRoleValue method returns incorrect value",
                soap11FaultRole.getRoleValue().trim().equals("This is only a test"));
    }

    public void testSOAP11GetRoleValue() {
        assertTrue(
                "SOAP 1.1 Fault Role Test : - After creating SOAPFaultRole, it has a value",
                soap11FaultRole.getRoleValue().equals(""));
        soap11FaultRole.setRoleValue("This is only a test");
        assertTrue(
                "SOAP 1.1 Fault Role Test : - After calling setRoleValue method, getRoleValue method returns incorrect value",
                soap11FaultRole.getRoleValue().trim().equals("This is only a test"));
    }

    //SOAP 1.2 Fault Role Test (Programaticaly Created)
    public void testSOAP12SetRoleValue() {
        soap12FaultRole.setRoleValue("This is only a test");
        assertTrue(
                "SOAP 1.2 Fault Role Test : - After calling setRoleValue method, getRoleValue method returns incorrect value",
                soap12FaultRole.getRoleValue().trim().equals("This is only a test"));
    }

    public void testSOAP12GetRoleValue() {
        assertTrue(
                "SOAP 1.2 Fault Role Test : - After creating SOAPFaultRole, it has a value",
                soap12FaultRole.getRoleValue().trim().equals(""));
        soap12FaultRole.setRoleValue("This is only a test");
        assertTrue(
                "SOAP 1.2 Fault Role Test : - After calling setRoleValue method, getRoleValue method returns incorrect value",
                soap12FaultRole.getRoleValue().trim().equals("This is only a test"));
    }

    //SOAP 1.1 Fault Role Test (With Parser)
    public void testSOAP11GetRoleValueWithParser() {
        assertTrue(
                "SOAP 1.1 Fault Role Test With Parser : - getRoleValue method returns incorrect value",
                soap11FaultRoleWithParser.getRoleValue().trim().equals(
                        "http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver"));
    }

    //SOAP 1.2 Fault Role Test (With Parser)
    public void testSOAP12GetRoleValueWithParser() {
        assertTrue(
                "SOAP 1.2 Fault Role Test With Parser : - getRoleValue method returns incorrect value",
                soap12FaultRoleWithParser.getRoleValue().trim().equals(
                        "ultimateReceiver"));
    }
}
